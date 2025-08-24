package com.myalgofax.controller;

import com.myalgofax.api.responce.ApiResponse;
import com.myalgofax.dto.ExecutedStrategyDTO;
import com.myalgofax.security.util.jwt.JwtUtil;
import com.myalgofax.service.ExecutedStrategyService;
import com.myalgofax.user.entity.ExecutedStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/strategies")
public class ExecutedStrategyController {

    private static final Logger logger = LoggerFactory.getLogger(ExecutedStrategyController.class);

    private final ExecutedStrategyService executedStrategyService;
    private final JwtUtil jwtUtil;

    public ExecutedStrategyController(ExecutedStrategyService executedStrategyService, JwtUtil jwtUtil) {
        this.executedStrategyService = executedStrategyService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/save")
    public Mono<ResponseEntity<ApiResponse<ExecutedStrategy>>> createStrategy(@RequestBody Map<String, Object> request) {
        logger.info("Creating strategy");
        
        String accessToken = (String) request.get("accessBrokerToken");
        String userId = jwtUtil.extractUserId(accessToken);
        
        ExecutedStrategyDTO dto = assembleDTO(request, userId);
        
        return executedStrategyService.createStrategy(dto)
            .map(createdStrategy -> ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("success", "Strategy created successfully", createdStrategy, null, true)));
    }

    @PostMapping("/user")
    public Mono<ResponseEntity<ApiResponse<List<ExecutedStrategy>>>> getStrategiesByUserId(@RequestBody Map<String, Object> request) {
        logger.info("Getting strategies for user");
        
        String accessToken = (String) request.get("accessBrokerToken");
        String userId = jwtUtil.extractUserId(accessToken);
        
        return executedStrategyService.getStrategiesByUserId(userId)
            .collectList()
            .map(strategies -> ResponseEntity.ok(
                new ApiResponse<>("success", "Strategies retrieved successfully", strategies, null, true)));
    }


    @PostMapping("/user/active")
    public Mono<ResponseEntity<ApiResponse<List<ExecutedStrategy>>>> getActiveStrategiesByUserId(@RequestBody Map<String, Object> request) {
        logger.info("Getting active strategies for user");
        
        return validateRequestAndProcess(request, "accessBrokerToken", (accessToken, userId) -> 
            executedStrategyService.getActiveStrategiesByUserId(userId)
                .collectList()
                .map(strategies -> ResponseEntity.ok(new ApiResponse<>("success", "Active strategies retrieved successfully", strategies, null, true)))
        );
    }

    @PostMapping("/user/status")
    public Mono<ResponseEntity<ApiResponse<List<ExecutedStrategy>>>> getStrategiesByStatus(@RequestBody Map<String, Object> request) {
        logger.info("Getting strategies by status for user");
        
        return validateRequestAndProcess(request, List.of("accessBrokerToken", "status"), (accessToken, userId) -> {
            String status = (String) request.get("status");
            return executedStrategyService.getStrategiesByUserIdAndStatus(userId, status)
                .collectList()
                .map(strategies -> ResponseEntity.ok(new ApiResponse<>("success", "Strategies retrieved successfully", strategies, null, true)));
        });
    }

    @PostMapping("/strategy")
    public Mono<ResponseEntity<ApiResponse<ExecutedStrategy>>> getStrategyById(@RequestBody Map<String, Object> request) {
        logger.info("Getting strategy by ID");
        
        return validateRequestAndProcess(request, List.of("accessBrokerToken", "id"), (accessToken, userId) -> {
        	 Long id = Long.parseLong(request.get("id").toString());
            return executedStrategyService.getStrategyByIdAndUserId(id, userId)
                .map(strategy -> ResponseEntity.ok(
                    new ApiResponse<>("success", "Strategy retrieved successfully", strategy, null, true)))
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("error", "Strategy not found", null, null, false))));
        });
    }

    @PostMapping("/update")
    public Mono<ResponseEntity<ApiResponse<ExecutedStrategy>>> updateStrategy(@RequestBody Map<String, Object> request) {
        logger.info("Updating strategy");
        
        String accessToken = (String) request.get("accessBrokerToken");
        String userId = jwtUtil.extractUserId(accessToken);
      
      
        Long  id = Long.parseLong(request.get("id").toString());
        
        
        ExecutedStrategyDTO dto = assembleDTO(request, userId);
      
        
        return executedStrategyService.updateStrategy(id, userId, dto)
            .map(updatedStrategy -> ResponseEntity.ok(
                new ApiResponse<>("success", "Strategy updated successfully", updatedStrategy, null, true)));
    }

  

    @PostMapping("/user/profitable")
    public Mono<ResponseEntity<ApiResponse<List<ExecutedStrategy>>>> getProfitableStrategies(@RequestBody Map<String, Object> request) {
        logger.info("Getting profitable strategies for user");
        
        return validateRequestAndProcess(request, "accessBrokerToken", (accessToken, userId) -> 
            executedStrategyService.getProfitableStrategiesByUserId(userId)
                .collectList()
                .map(strategies -> ResponseEntity.ok(new ApiResponse<>("success", "Profitable strategies retrieved successfully", strategies, null, true)))
        );
    }

    @PostMapping("/user/total-pnl")
    public Mono<ResponseEntity<ApiResponse<Double>>> getTotalPnl(@RequestBody Map<String, Object> request) {
        logger.info("Getting total PnL for user");
        
        return validateRequestAndProcess(request, "accessBrokerToken", (accessToken, userId) -> 
            executedStrategyService.getTotalPnlByUserId(userId)
                .map(totalPnl -> ResponseEntity.ok(new ApiResponse<>("success", "Total PnL retrieved successfully", totalPnl, null, true)))
        );
    }

   

   


    @FunctionalInterface
    private interface RequestProcessor<T> {
        Mono<ResponseEntity<ApiResponse<T>>> process(String accessToken, String userId);
    }

    private <T> Mono<ResponseEntity<ApiResponse<T>>> validateRequestAndProcess(
            Map<String, Object> request, 
            String requiredField,
            RequestProcessor<T> processor) {
        return validateRequestAndProcess(request, List.of(requiredField), processor);
    }

    private <T> Mono<ResponseEntity<ApiResponse<T>>> validateRequestAndProcess(
            Map<String, Object> request, 
            List<String> requiredFields,
            RequestProcessor<T> processor) {
        
        // Validate required fields
        for (String field : requiredFields) {
            if (request.get(field) == null) {
                return Mono.just(ResponseEntity.badRequest()
                    .body(new ApiResponse<>("error", field + " is required", null, null, false)));
            }
        }

        String accessToken = (String) request.get("accessBrokerToken");
        return Mono.fromCallable(() -> {
            String userId = jwtUtil.extractUserId(accessToken);
            if (userId == null) {
                throw new RuntimeException("Invalid access token");
            }
            return userId;
        })
        .flatMap(userId -> processor.process(accessToken, userId))
        .onErrorResume(e -> {
            logger.error("Failed to process request", e);
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>("error", "Failed to process request: " + e.getMessage(), null, null, false)));
        });
    }
    
    private ExecutedStrategyDTO assembleDTO(Map<String, Object> request, String userId) {
        ExecutedStrategyDTO dto = new ExecutedStrategyDTO();
        dto.setName((String) request.get("name"));
        dto.setDescription((String) request.get("description"));
        dto.setCategory((String) request.get("category"));
        dto.setRisk((String) request.get("risk"));
        dto.setActive((Boolean) request.get("active"));
        dto.setCapital(request.get("capital") != null ? ((Number) request.get("capital")).doubleValue() : null);
        dto.setPositions(request.get("positions") != null ? ((Number) request.get("positions")).intValue() : null);
        dto.setPnl(request.get("pnl") != null ? ((Number) request.get("pnl")).doubleValue() : null);
        dto.setStatus((String) request.get("status"));
        dto.setLotSize(request.get("lotSize") != null ? ((Number) request.get("lotSize")).intValue() : null);
        dto.setUnderlying((String) request.get("underlying"));
        dto.setSymbol((String) request.get("symbol"));
        dto.setLegs((List<Map<String, Object>>) request.get("legs"));
        dto.setUserId(userId);
        return dto;
    }
}