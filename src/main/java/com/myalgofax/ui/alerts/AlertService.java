package com.myalgofax.ui.alerts;

import com.myalgofax.ui.websocket.DashboardWebSocketHandler;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AlertService {
    
    private final DashboardWebSocketHandler dashboardHandler;
    private final Map<String, AlertThreshold> alertThresholds = new ConcurrentHashMap<>();
    
    public AlertService(DashboardWebSocketHandler dashboardHandler) {
        this.dashboardHandler = dashboardHandler;
        initializeDefaultThresholds();
    }
    
    public static class AlertThreshold {
        private final String type;
        private final double threshold;
        private final String condition; // "ABOVE" or "BELOW"
        
        public AlertThreshold(String type, double threshold, String condition) {
            this.type = type;
            this.threshold = threshold;
            this.condition = condition;
        }
        
        public String getType() { return type; }
        public double getThreshold() { return threshold; }
        public String getCondition() { return condition; }
    }
    
    public static class Alert {
        private final String id;
        private final String type;
        private final String message;
        private final String severity;
        private final LocalDateTime timestamp;
        private final Map<String, Object> data;
        
        public Alert(String type, String message, String severity, Map<String, Object> data) {
            this.id = "ALERT_" + System.currentTimeMillis();
            this.type = type;
            this.message = message;
            this.severity = severity;
            this.timestamp = LocalDateTime.now();
            this.data = data;
        }
        
        // Getters
        public String getId() { return id; }
        public String getType() { return type; }
        public String getMessage() { return message; }
        public String getSeverity() { return severity; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public Map<String, Object> getData() { return data; }
    }
    
    private void initializeDefaultThresholds() {
        alertThresholds.put("STRATEGY_LOSS", new AlertThreshold("STRATEGY_LOSS", -1000.0, "BELOW"));
        alertThresholds.put("PORTFOLIO_DRAWDOWN", new AlertThreshold("PORTFOLIO_DRAWDOWN", 0.05, "ABOVE"));
        alertThresholds.put("RISK_LIMIT", new AlertThreshold("RISK_LIMIT", 0.02, "ABOVE"));
        alertThresholds.put("MARGIN_CALL", new AlertThreshold("MARGIN_CALL", 0.8, "ABOVE"));
    }
    
    public Mono<Void> checkStrategyPnL(String strategyId, double pnl) {
        return Mono.fromRunnable(() -> {
            AlertThreshold threshold = alertThresholds.get("STRATEGY_LOSS");
            if (threshold != null && pnl < threshold.getThreshold()) {
                Alert alert = new Alert(
                    "STRATEGY_LOSS",
                    String.format("Strategy %s has exceeded loss threshold: %.2f", strategyId, pnl),
                    "HIGH",
                    Map.of("strategyId", strategyId, "pnl", pnl, "threshold", threshold.getThreshold())
                );
                broadcastAlert(alert);
            }
        });
    }
    
    public Mono<Void> checkPortfolioDrawdown(double drawdown) {
        return Mono.fromRunnable(() -> {
            AlertThreshold threshold = alertThresholds.get("PORTFOLIO_DRAWDOWN");
            if (threshold != null && drawdown > threshold.getThreshold()) {
                Alert alert = new Alert(
                    "PORTFOLIO_DRAWDOWN",
                    String.format("Portfolio drawdown exceeded threshold: %.2f%%", drawdown * 100),
                    "CRITICAL",
                    Map.of("drawdown", drawdown, "threshold", threshold.getThreshold())
                );
                broadcastAlert(alert);
            }
        });
    }
    
    public Mono<Void> checkRiskLimits(Map<String, Double> riskMetrics) {
        return Mono.fromRunnable(() -> {
            for (Map.Entry<String, Double> entry : riskMetrics.entrySet()) {
                String metric = entry.getKey();
                Double value = entry.getValue();
                
                if ("VAR".equals(metric) && value > 10000.0) {
                    Alert alert = new Alert(
                        "RISK_LIMIT",
                        String.format("VaR exceeded limit: %.2f", value),
                        "HIGH",
                        Map.of("metric", metric, "value", value, "limit", 10000.0)
                    );
                    broadcastAlert(alert);
                }
            }
        });
    }
    
    public Mono<Void> systemErrorAlert(String component, String error) {
        return Mono.fromRunnable(() -> {
            Alert alert = new Alert(
                "SYSTEM_ERROR",
                String.format("System error in %s: %s", component, error),
                "CRITICAL",
                Map.of("component", component, "error", error)
            );
            broadcastAlert(alert);
        });
    }
    
    public Mono<Void> connectivityAlert(String broker, String status) {
        return Mono.fromRunnable(() -> {
            if ("DISCONNECTED".equals(status)) {
                Alert alert = new Alert(
                    "CONNECTIVITY",
                    String.format("Lost connection to broker: %s", broker),
                    "HIGH",
                    Map.of("broker", broker, "status", status)
                );
                broadcastAlert(alert);
            }
        });
    }
    
    public Mono<Void> updateAlertThreshold(String alertType, double threshold, String condition) {
        return Mono.fromRunnable(() -> {
            alertThresholds.put(alertType, new AlertThreshold(alertType, threshold, condition));
        });
    }
    
    private void broadcastAlert(Alert alert) {
        dashboardHandler.broadcastRiskAlert(alert.getMessage());
        
        // Additional alert processing (email, SMS, etc.) could be added here
        System.out.println(String.format("[%s] %s: %s", 
            alert.getSeverity(), alert.getType(), alert.getMessage()));
    }
}