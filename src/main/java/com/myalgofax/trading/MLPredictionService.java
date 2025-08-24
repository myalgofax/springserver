package com.myalgofax.trading;

import java.time.Duration;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import reactor.core.publisher.Mono;

/**
 * Reactive ML Prediction Service for options trading strategies
 * Provides probability of profit predictions with circuit breaker pattern
 */
@Service
public class MLPredictionService {
    
    private static final Logger logger = LoggerFactory.getLogger(MLPredictionService.class);
    
    private final WebClient webClient;
    private final CircuitBreaker circuitBreaker;
    
    @Value("${ml.model.endpoint:http://localhost:8000/predict}")
    private String mlEndpoint;
    
    @Value("${ml.model.timeout:2000}")
    private int timeoutMs;

    public MLPredictionService(WebClient.Builder webClientBuilder, CircuitBreaker circuitBreaker) {
        this.webClient = webClientBuilder
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
            .build();
        this.circuitBreaker = circuitBreaker;
    }

    /**
     * Get ML prediction for strategy probability of profit
     * @param features Feature vector for the strategy
     * @return Mono containing prediction result with POP and confidence
     */
    public Mono<MLPredictionResult> getPrediction(Map<String, Object> features) {
        return webClient.post()
            .uri(mlEndpoint)
            .bodyValue(Map.of("features", features))
            .retrieve()
            .bodyToMono(MLPredictionResponse.class)
            .timeout(Duration.ofMillis(timeoutMs))
            .map(this::mapToResult)
            .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
            .onErrorResume(this::handleMLError)
            .doOnNext(result -> logger.debug("ML prediction: POP={}, confidence={}", 
                result.getProbabilityOfProfit(), result.getConfidence()))
            .doOnError(error -> logger.error("ML prediction failed: {}", error.getMessage()));
    }

    /**
     * Fallback prediction when ML service is unavailable
     * Uses simple heuristic based on market conditions
     */
    private Mono<MLPredictionResult> handleMLError(Throwable error) {
        logger.warn("ML service unavailable, using fallback prediction: {}", error.getMessage());
        
        // Simple fallback: neutral prediction with low confidence
        return Mono.just(new MLPredictionResult(
            0.5, // 50% probability
            0.3, // Low confidence
            "FALLBACK",
            "ML service unavailable - using heuristic"
        ));
    }

    private MLPredictionResult mapToResult(MLPredictionResponse response) {
        return new MLPredictionResult(
            response.getProbabilityOfProfit(),
            response.getConfidence(),
            response.getModelVersion(),
            response.getReason()
        );
    }

    /**
     * ML Prediction Response DTO
     */
    public static class MLPredictionResponse {
        private double probabilityOfProfit;
        private double confidence;
        private String modelVersion;
        private String reason;

        public double getProbabilityOfProfit() { return probabilityOfProfit; }
        public void setProbabilityOfProfit(double probabilityOfProfit) { this.probabilityOfProfit = probabilityOfProfit; }

        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }

        public String getModelVersion() { return modelVersion; }
        public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    /**
     * ML Prediction Result
     */
    public static class MLPredictionResult {
        private final double probabilityOfProfit;
        private final double confidence;
        private final String modelVersion;
        private final String reason;

        public MLPredictionResult(double probabilityOfProfit, double confidence, String modelVersion, String reason) {
            this.probabilityOfProfit = probabilityOfProfit;
            this.confidence = confidence;
            this.modelVersion = modelVersion;
            this.reason = reason;
        }

        public double getProbabilityOfProfit() { return probabilityOfProfit; }
        public double getConfidence() { return confidence; }
        public String getModelVersion() { return modelVersion; }
        public String getReason() { return reason; }
        
        public boolean isHighConfidence() { return confidence > 0.7; }
        public boolean shouldTrade() { return probabilityOfProfit > 0.6 && confidence > 0.5; }
    }
}