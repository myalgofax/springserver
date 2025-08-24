package com.myalgofax.service;

import com.myalgofax.dto.ExecutedStrategyDTO;
import com.myalgofax.exception.StrategyNotFoundException;
import com.myalgofax.repository.ExecutedStrategyRepository;
import com.myalgofax.user.entity.ExecutedStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Optional;



@Service
public class ExecutedStrategyService {

    private static final Logger log = LoggerFactory.getLogger(ExecutedStrategyService.class);
    
    private final ExecutedStrategyRepository executedStrategyRepository;

    @Autowired
    public ExecutedStrategyService(ExecutedStrategyRepository executedStrategyRepository) {
        this.executedStrategyRepository = executedStrategyRepository;
    }

    @Transactional
    public Mono<ExecutedStrategy> createStrategy(ExecutedStrategyDTO dto) {
        ExecutedStrategy strategy = new ExecutedStrategy();
        
        strategy.setName(dto.getName());
        strategy.setDescription(dto.getDescription());
        strategy.setCategory(dto.getCategory());
        strategy.setRisk(dto.getRisk());
        strategy.setActive(dto.getActive() != null ? dto.getActive() : true);
        strategy.setCapital(dto.getCapital());
        strategy.setPositions(dto.getPositions());
        strategy.setPnl(dto.getPnl() != null ? dto.getPnl() : 0.0);
        strategy.setStatus(dto.getStatus());
        strategy.setLotSize(dto.getLotSize());
        strategy.setUnderlying(dto.getUnderlying());
        strategy.setSymbol(dto.getSymbol());
        strategy.setLegs(dto.getLegs());
        strategy.setUserId(dto.getUserId());

        return executedStrategyRepository.save(strategy)
            .doOnNext(savedStrategy -> log.info("Created strategy with ID: {} for user: {}", savedStrategy.getId(), savedStrategy.getUserId()));
    }

    public Flux<ExecutedStrategy> getStrategiesByUserId(String userId) {
        return executedStrategyRepository.findByUserId(userId);
    }

    public Flux<ExecutedStrategy> getActiveStrategiesByUserId(String userId) {
        return executedStrategyRepository.findByUserIdAndActive(userId, true);
    }

    public Flux<ExecutedStrategy> getStrategiesByUserIdAndStatus(String userId, String status) {
        return executedStrategyRepository.findByUserIdAndStatus(userId, status);
    }

    public Flux<ExecutedStrategy> getStrategiesByUserIdAndCategory(String userId, String category) {
        return executedStrategyRepository.findByUserIdAndCategory(userId, category);
    }

    public Mono<ExecutedStrategy> getStrategyByIdAndUserId(Long id, String userId) {
        return executedStrategyRepository.findByIdAndUserId(id, userId);
    }

    @Transactional
    public Mono<ExecutedStrategy> updateStrategy(Long id, String userId, ExecutedStrategyDTO dto) {
        return getStrategyByIdAndUserId(id, userId)
            .switchIfEmpty(Mono.error(new StrategyNotFoundException("Strategy not found with id: " + id)))
            .map(existingStrategy -> {
                if (dto.getName() != null) {
                    existingStrategy.setName(dto.getName());
                }
                if (dto.getDescription() != null) {
                    existingStrategy.setDescription(dto.getDescription());
                }
                if (dto.getCategory() != null) {
                    existingStrategy.setCategory(dto.getCategory());
                }
                if (dto.getRisk() != null) {
                    existingStrategy.setRisk(dto.getRisk());
                }
                if (dto.getActive() != null) {
                    existingStrategy.setActive(dto.getActive());
                }
                if (dto.getCapital() != null) {
                    existingStrategy.setCapital(dto.getCapital());
                }
                if (dto.getPositions() != null) {
                    existingStrategy.setPositions(dto.getPositions());
                }
                if (dto.getPnl() != null) {
                    existingStrategy.setPnl(dto.getPnl());
                }
                if (dto.getStatus() != null) {
                    existingStrategy.setStatus(dto.getStatus());
                }
                if (dto.getLotSize() != null) {
                    existingStrategy.setLotSize(dto.getLotSize());
                }
                if (dto.getUnderlying() != null) {
                    existingStrategy.setUnderlying(dto.getUnderlying());
                }
                if (dto.getSymbol() != null) {
                    existingStrategy.setSymbol(dto.getSymbol());
                }
                if (dto.getLegs() != null) {
                    existingStrategy.setLegs(dto.getLegs());
                }
                return existingStrategy;
            })
            .flatMap(executedStrategyRepository::save)
            .doOnNext(updatedStrategy -> log.info("Updated strategy with ID: {}", updatedStrategy.getId()));
    }
    

   

    public Flux<ExecutedStrategy> getProfitableStrategiesByUserId(String userId) {
        return executedStrategyRepository.findProfitableStrategiesByUserId(userId);
    }

    public Mono<Double> getTotalPnlByUserId(String userId) {
        return executedStrategyRepository.getTotalPnlByUserId(userId)
            .defaultIfEmpty(0.0);
    }
}