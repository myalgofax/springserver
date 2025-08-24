package com.myalgofax.dto;

import java.math.BigDecimal;
import java.util.Date;


public class ScripMasterDTO {
    private String pTrdSymbol;
    private Date expiryDate;
    private String exchange;
    private String instrumentType;
    private String symbol;
    private String name;
    private Integer lotSize;
    private Double tickSize;
    
    private String accessBrokerToken;
    private BigDecimal dOpenInterest;
    private BigDecimal dLowPriceRange;
	private String pSymbolName;
	private String pOptionType;
	private String pDesc;

	private BigDecimal dStrikePrice;

	private String pSegment;

	private BigDecimal dHighPriceRange;
    
    

    public BigDecimal getDOpenInterest() {
		return dOpenInterest;
	}

	public void setDOpenInterest(BigDecimal dOpenInterest) {
		this.dOpenInterest = dOpenInterest;
	}

	public BigDecimal getDLowPriceRange() {
		return dLowPriceRange;
	}

	public void setDLowPriceRange(BigDecimal dLowPriceRange) {
		this.dLowPriceRange = dLowPriceRange;
	}

	public String getPSymbolName() {
		return pSymbolName;
	}

	public void setPSymbolName(String pSymbolName) {
		this.pSymbolName = pSymbolName;
	}

	public String getPOptionType() {
		return pOptionType;
	}

	public void setPOptionType(String pOptionType) {
		this.pOptionType = pOptionType;
	}

	public String getPDesc() {
		return pDesc;
	}

	public void setPDesc(String pDesc) {
		this.pDesc = pDesc;
	}

	public BigDecimal getDStrikePrice() {
		return dStrikePrice;
	}

	public void setDStrikePrice(BigDecimal dStrikePrice) {
		this.dStrikePrice = dStrikePrice;
	}

	public String getPSegment() {
		return pSegment;
	}

	public void setPSegment(String pSegment) {
		this.pSegment = pSegment;
	}

	public BigDecimal getDHighPriceRange() {
		return dHighPriceRange;
	}

	public void setDHighPriceRange(BigDecimal dHighPriceRange) {
		this.dHighPriceRange = dHighPriceRange;
	}

	// Getters and Setters
    public String getpTrdSymbol() {
        return pTrdSymbol;
    }

    public void setpTrdSymbol(String pTrdSymbol) {
        this.pTrdSymbol = pTrdSymbol;
    }



    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getInstrumentType() {
        return instrumentType;
    }

    public void setInstrumentType(String instrumentType) {
        this.instrumentType = instrumentType;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getLotSize() {
        return lotSize;
    }

    public void setLotSize(Integer lotSize) {
        this.lotSize = lotSize;
    }

    public Double getTickSize() {
        return tickSize;
    }

    public void setTickSize(Double tickSize) {
        this.tickSize = tickSize;
    }

	public String getAccessBrokerToken() {
		return accessBrokerToken;
	}

	public void setAccessBrokerToken(String accessBrokerToken) {
		this.accessBrokerToken = accessBrokerToken;
	}

    
    
}
