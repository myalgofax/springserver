package com.myalgofax.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KotakNeoWebSocketDto {
    
    @JsonProperty("client_id")
    private String clientId;
    
    @JsonProperty("instrument_tokens")
    private List<KotakNeoQuoteDto.InstrumentToken> instrumentTokens;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public List<KotakNeoQuoteDto.InstrumentToken> getInstrumentTokens() {
        return instrumentTokens;
    }

    public void setInstrumentTokens(List<KotakNeoQuoteDto.InstrumentToken> instrumentTokens) {
        this.instrumentTokens = instrumentTokens;
    }
}