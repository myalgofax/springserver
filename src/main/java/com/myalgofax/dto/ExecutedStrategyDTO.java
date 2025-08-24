package com.myalgofax.dto;


import java.util.List;
import java.util.Map;


public class ExecutedStrategyDTO {
    private String name;
    private String description;
    private String category;
    private String risk;
    private Boolean active;
    private Double capital;
    private Integer positions;
    private Double pnl;
    private String status;
    private Integer lotSize;
    private String underlying;
    private String symbol;
    private List<Map<String, Object>> legs;
    private String userId;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getRisk() {
		return risk;
	}
	public void setRisk(String risk) {
		this.risk = risk;
	}
	public Boolean getActive() {
		return active;
	}
	public void setActive(Boolean active) {
		this.active = active;
	}
	public Double getCapital() {
		return capital;
	}
	public void setCapital(Double capital) {
		this.capital = capital;
	}
	public Integer getPositions() {
		return positions;
	}
	public void setPositions(Integer positions) {
		this.positions = positions;
	}
	public Double getPnl() {
		return pnl;
	}
	public void setPnl(Double pnl) {
		this.pnl = pnl;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Integer getLotSize() {
		return lotSize;
	}
	public void setLotSize(Integer lotSize) {
		this.lotSize = lotSize;
	}
	public String getUnderlying() {
		return underlying;
	}
	public void setUnderlying(String underlying) {
		this.underlying = underlying;
	}
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public List<Map<String, Object>> getLegs() {
		return legs;
	}
	public void setLegs(List<Map<String, Object>> legs) {
		this.legs = legs;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	@Override
	public String toString() {
		return "ExecutedStrategyDTO [name=" + name + ", description=" + description + ", category=" + category
				+ ", risk=" + risk + ", active=" + active + ", capital=" + capital + ", positions=" + positions
				+ ", pnl=" + pnl + ", status=" + status + ", lotSize=" + lotSize + ", underlying=" + underlying
				+ ", symbol=" + symbol + ", legs=" + legs + ", userId=" + userId + "]";
	}
	public ExecutedStrategyDTO(String name, String description, String category, String risk, Boolean active,
			Double capital, Integer positions, Double pnl, String status, Integer lotSize, String underlying,
			String symbol, List<Map<String, Object>> legs, String userId) {
		super();
		this.name = name;
		this.description = description;
		this.category = category;
		this.risk = risk;
		this.active = active;
		this.capital = capital;
		this.positions = positions;
		this.pnl = pnl;
		this.status = status;
		this.lotSize = lotSize;
		this.underlying = underlying;
		this.symbol = symbol;
		this.legs = legs;
		this.userId = userId;
	}
	public ExecutedStrategyDTO() {
		super();
		// TODO Auto-generated constructor stub
	}
    
    
    
}