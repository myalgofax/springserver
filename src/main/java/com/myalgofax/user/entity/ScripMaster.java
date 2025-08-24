package com.myalgofax.user.entity;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("scrip_master")
public class ScripMaster {
	@Id
	private Long id;

	@Column("p_symbol_name")
	private String pSymbolName;
	
	@Column("p_symbol")
	private BigDecimal pSymbol;

	@Column("p_trd_symbol")
	private String pTrdSymbol;

	@Column("p_option_type")
	private String pOptionType;

	@Column("description")
	private String pDesc;

	@Column("d_strike_price")
	private BigDecimal dStrikePrice;

	@Column("p_segment")
	private String pSegment;
	
	@Column("p_exchange")
	private String pExchange;

	@Column("d_high_price_range")
	private BigDecimal dHighPriceRange;
	
	@Column("d_open_interest")
	private BigDecimal dOpenInterest;

	@Column("d_low_price_range")
	private BigDecimal dLowPriceRange;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPSymbolName() {
		return pSymbolName;
	}

	public void setPSymbolName(String pSymbolName) {
		this.pSymbolName = pSymbolName;
	}

	public void setpSymbolName(String pSymbolName) {
		this.pSymbolName = pSymbolName;
	}

	public String getPTrdSymbol() {
		return pTrdSymbol;
	}

	public void setPTrdSymbol(String pTrdSymbol) {
		this.pTrdSymbol = pTrdSymbol;
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

	

	public BigDecimal getPSymbol() {
		return pSymbol;
	}

	public void setPSymbol(BigDecimal pSymbol) {
		this.pSymbol = pSymbol;
	}

	public BigDecimal getDHighPriceRange() {
		return dHighPriceRange;
	}

	public void setDHighPriceRange(BigDecimal dHighPriceRange) {
		this.dHighPriceRange = dHighPriceRange;
	}

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

	public String getPExchange() {
		return pExchange;
	}

	public void setPExchange(String pExchange) {
		this.pExchange = pExchange;
	}
	
	
	

}