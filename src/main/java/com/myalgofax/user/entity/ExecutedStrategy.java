package com.myalgofax.user.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("com_executed_strategies")

public class ExecutedStrategy {

	@Id
    private Long id;

    @Column("name")
    private String name;

    @Column("description")
    private String description;

    @Column("category")
    private String category;

    @Column("risk")
    private String risk;

    @Column("active")
    private Boolean active = true;

    @Column("capital")
    private Double capital;

    @Column("positions")
    private Integer positions;

    @Column("pnl")
    private Double pnl = 0.0;

    @Column("status")
    private String status;

    @CreatedDate
    @Column("executed_at")
    private LocalDateTime executedAt;

    @Column("lot_size")
    private Integer lotSize;

    @Column("underlying")
    private String underlying;

    @Column("symbol")
    private String symbol;

    @Column("legs")
    private List<Map<String, Object>> legs;

    @Column("user_id")
    private String userId;

	public ExecutedStrategy() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ExecutedStrategy(Long id, String name, String description, String category, String risk, Boolean active,
			Double capital, Integer positions, Double pnl, String status, LocalDateTime executedAt, Integer lotSize,
			String underlying, String symbol, List<Map<String, Object>> legs, String userId) {
		super();
		this.id = id;
		this.name = name;
		this.description = description;
		this.category = category;
		this.risk = risk;
		this.active = active;
		this.capital = capital;
		this.positions = positions;
		this.pnl = pnl;
		this.status = status;
		this.executedAt = executedAt;
		this.lotSize = lotSize;
		this.underlying = underlying;
		this.symbol = symbol;
		this.legs = legs;
		this.userId = userId;
	}

	@Override
	public String toString() {
		return "ExecutedStrategy [id=" + id + ", name=" + name + ", description=" + description + ", category="
				+ category + ", risk=" + risk + ", active=" + active + ", capital=" + capital + ", positions="
				+ positions + ", pnl=" + pnl + ", status=" + status + ", executedAt=" + executedAt + ", lotSize="
				+ lotSize + ", underlying=" + underlying + ", symbol=" + symbol + ", legs=" + legs + ", userId="
				+ userId + "]";
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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

	public LocalDateTime getExecutedAt() {
		return executedAt;
	}

	public void setExecutedAt(LocalDateTime executedAt) {
		this.executedAt = executedAt;
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
}