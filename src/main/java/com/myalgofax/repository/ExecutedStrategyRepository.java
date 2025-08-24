package com.myalgofax.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import com.myalgofax.user.entity.ExecutedStrategy;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ExecutedStrategyRepository extends R2dbcRepository<ExecutedStrategy, Long> {

    /**
     * Finds all strategies for a given user ID
     * @param userId the user ID to search for
     * @return flux of strategies belonging to the user
     */
    Flux<ExecutedStrategy> findByUserId(String userId);
    
    /**
     * Finds all active/inactive strategies for a given user ID
     * @param userId the user ID to search for
     * @param active true for active strategies, false for inactive
     * @return flux of matching strategies
     */
    Flux<ExecutedStrategy> findByUserIdAndActive(String userId, Boolean active);
    
    /**
     * Finds all strategies with specific status for a given user ID
     * @param userId the user ID to search for
     * @param status the status to filter by
     * @return flux of matching strategies
     */
    Flux<ExecutedStrategy> findByUserIdAndStatus(String userId, String status);
    
    /**
     * Finds all strategies in specific category for a given user ID
     * @param userId the user ID to search for
     * @param category the category to filter by
     * @return flux of matching strategies
     */
    Flux<ExecutedStrategy> findByUserIdAndCategory(String userId, String category);
    
    /**
     * Finds a specific strategy by ID that belongs to a given user
     * @param id the strategy ID
     * @param userId the user ID that should own the strategy
     * @return Mono containing the strategy if found
     */
    Mono<ExecutedStrategy> findByIdAndUserId(Long id, String userId);

    /**
     * Finds all profitable strategies (pnl > 0) for a given user
     * @param userId the user ID to search for
     * @return flux of profitable strategies
     */
    @Query("SELECT * FROM com_executed_strategies WHERE user_id = :userId AND pnl > 0")
    Flux<ExecutedStrategy> findProfitableStrategiesByUserId(String userId);

    /**
     * Calculates the total PnL for all strategies of a given user
     * @param userId the user ID to calculate for
     * @return Mono containing the sum (0.0 if no strategies exist)
     */
    @Query("SELECT COALESCE(SUM(pnl), 0.0) FROM com_executed_strategies WHERE user_id = :userId")
    Mono<Double> getTotalPnlByUserId(String userId);

    /**
     * Checks if a strategy exists with given ID and user ID
     * @param id the strategy ID
     * @param userId the user ID
     * @return true if exists, false otherwise
     */
    Mono<Boolean> existsByIdAndUserId(Long id, String userId);
}