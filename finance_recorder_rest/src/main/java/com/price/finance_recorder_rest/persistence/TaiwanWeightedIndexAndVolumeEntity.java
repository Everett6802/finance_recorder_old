package com.price.finance_recorder_rest.persistence;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "taiwan_weighted_index_and_volume")
public class TaiwanWeightedIndexAndVolumeEntity implements Serializable
{
	private static final long serialVersionUID = -126030805271520343L;

// http://www.baeldung.com/hibernate-date-time
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private int id;

//	@Temporal(TemporalType.DATE)
	@Column(name = "trade_date", updatable = false, nullable = false)
	private Date tradeDate; // 日期

	@Column(name = "trade_volume", nullable = false)
	private long tradeVolume; // 成交股數

	@Column(name = "turnover_in_value", nullable = false)
	private long turnoverInValue; // 成交金額

	@Column(name = "number_of_transactions", nullable = false)
	private int numberOfTransactions; // 成交筆數

	@Column(name = "weighted_stock_index", nullable = false)
	private float weightedStockIndex; // 發行量加權股價指數

	@Column(name = "net_change", nullable = false)
	private float netChange; // 漲跌點數

	public Date getTradeDate()
	{
		return tradeDate;
	}
	public void setTradeDate(Date TradeDate)
	{
		tradeDate = TradeDate;
	}
	public long getTradeVolume()
	{
		return tradeVolume;
	}
	public void setTradeVolume(long TradeVolume)
	{
		tradeVolume = TradeVolume;
	}
	public long getTurnoverInValue()
	{
		return turnoverInValue;
	}
	public void setTurnoverInValue(long TurnoverInValue)
	{
		turnoverInValue = TurnoverInValue;
	}
	public int getNumberOfTransactions()
	{
		return numberOfTransactions;
	}
	public void setNumberOfTransactions(int NumberOfTransactions)
	{
		numberOfTransactions = NumberOfTransactions;
	}
	public float getWeightedStockIndex()
	{
		return weightedStockIndex;
	}
	public void setWeightedStockIndex(float WeightedStockIndex)
	{
		weightedStockIndex = WeightedStockIndex;
	}
	public float getNetChange()
	{
		return netChange;
	}
	public void setNetChange(float NetChange)
	{
		this.netChange = NetChange;
	}

}
