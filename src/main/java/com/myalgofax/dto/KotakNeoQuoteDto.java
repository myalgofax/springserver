package com.myalgofax.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KotakNeoQuoteDto {
    
    @JsonProperty("instrument_tokens")
    private List<InstrumentToken> instrumentTokens;
    
    @JsonProperty("quote_type")
    private String quoteType;
    
    @JsonProperty("exchange_segment")
    private String exchangeSegment;

    public List<InstrumentToken> getInstrumentTokens() {
        return instrumentTokens;
    }

    public void setInstrumentTokens(List<InstrumentToken> instrumentTokens) {
        this.instrumentTokens = instrumentTokens;
    }

    public String getQuoteType() {
        return quoteType;
    }

    public void setQuoteType(String quoteType) {
        this.quoteType = quoteType;
    }

    public String getExchangeSegment() {
        return exchangeSegment;
    }

    public void setExchangeSegment(String exchangeSegment) {
        this.exchangeSegment = exchangeSegment;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InstrumentToken {
        
        @JsonProperty("instrument_token")
        private String instrumentToken;
        
        @JsonProperty("exchange_segment")
        private String exchangeSegment;

        public String getInstrumentToken() {
            return instrumentToken;
        }

        public void setInstrumentToken(String instrumentToken) {
            this.instrumentToken = instrumentToken;
        }

        public String getExchangeSegment() {
            return exchangeSegment;
        }

        public void setExchangeSegment(String exchangeSegment) {
            this.exchangeSegment = exchangeSegment;
        }
    }
}