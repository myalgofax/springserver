package com.myalgofax.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import com.myalgofax.user.entity.ScripMaster;

import reactor.core.publisher.Flux;

@Repository
public interface ScripMasterRepository extends R2dbcRepository<ScripMaster, Long> {
    Flux<ScripMaster> findBypTrdSymbol(String pTrdSymbol);
    Flux<ScripMaster> findBypSegment(String pSegment);
    Flux<ScripMaster> findBypOptionType(String pOptionType);
    Flux<ScripMaster> findBypSymbolName(String pSymbolName);
}