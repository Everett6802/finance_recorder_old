package com.price.finance_recorder_rest.entrypoints;

import java.util.Date;

public class TaiwanWeightedIndexAndVolumeGetRsp 
{
	private Date tradeDate; // 日期
	private long tradeVolume; // 成交股數
	private long turnoverInValue; // 成交金額
	private int numberOfTransactions; // 成交筆數
	private float weightedStockIndex; // 發行量加權股價指數
	private float netChange; // 漲跌點數

	public Date getTradeDate() {
		return tradeDate;
	}
	public void setTradeDate(Date tradeDate) {
		this.tradeDate = tradeDate;
	}
	public long getTradeVolume() {
		return tradeVolume;
	}
	public void setTradeVolume(long tradeVolume) {
		this.tradeVolume = tradeVolume;
	}
	public long getTurnoverInValue() {
		return turnoverInValue;
	}
	public void setTurnoverInValue(long turnoverInValue) {
		this.turnoverInValue = turnoverInValue;
	}
	public int getNumberOfTransactions() {
		return numberOfTransactions;
	}
	public void setNumberOfTransactions(int numberOfTransactions) {
		this.numberOfTransactions = numberOfTransactions;
	}
	public float getWeightedStockIndex() {
		return weightedStockIndex;
	}
	public void setWeightedStockIndex(float weightedStockIndex) {
		this.weightedStockIndex = weightedStockIndex;
	}
	public float getNetChange() {
		return netChange;
	}
	public void setNetChange(float netChange) {
		this.netChange = netChange;
	}
}
