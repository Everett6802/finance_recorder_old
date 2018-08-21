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
@Table(name = "stock_exchange_and_volume")
public class StockExchangeAndVolumeEntity implements Serializable
{
	private static final long serialVersionUID = -126030805271520343L;

// http://www.baeldung.com/hibernate-date-time
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private int id;

//	@Temporal(TemporalType.DATE)
	@Column(name = "trade_date", updatable = false, nullable = false)
	private Date TradeDate; // 日期

	@Column(name = "trade_volume", nullable = false)
	private long TradeVolume; // 成交股數

	@Column(name = "turnover_in_value", nullable = false)
	private long TurnoverInValue; // 成交金額

	@Column(name = "number_of_transactions", nullable = false)
	private int NumberOfTransactions; // 成交筆數

	@Column(name = "weighted_stock_index", nullable = false)
	private float WeightedStockIndex; // 發行量加權股價指數

	@Column(name = "net_change", nullable = false)
	private float NetChange; // 漲跌點數

	public Date getTradeDate()
	{
		return TradeDate;
	}
	public void setTradeDate(Date tradeDate)
	{
		TradeDate = tradeDate;
	}
	public long getTradeVolume()
	{
		return TradeVolume;
	}
	public void setTradeVolume(long tradeVolume)
	{
		TradeVolume = tradeVolume;
	}
	public long getTurnoverInValue()
	{
		return TurnoverInValue;
	}
	public void setTurnoverInValue(long turnoverInValue)
	{
		TurnoverInValue = turnoverInValue;
	}
	public int getNumberOfTransactions()
	{
		return NumberOfTransactions;
	}
	public void setNumberOfTransactions(int numberOfTransactions)
	{
		NumberOfTransactions = numberOfTransactions;
	}
	public float getWeightedStockIndex()
	{
		return WeightedStockIndex;
	}
	public void setWeightedStockIndex(float weightedStockIndex)
	{
		WeightedStockIndex = weightedStockIndex;
	}
	public float getNetChange()
	{
		return NetChange;
	}
	public void setNetChange(float netChange)
	{
		this.NetChange = netChange;
	}

}
