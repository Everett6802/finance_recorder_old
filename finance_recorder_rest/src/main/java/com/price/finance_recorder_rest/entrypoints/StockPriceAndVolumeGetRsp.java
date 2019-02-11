package com.price.finance_recorder_rest.entrypoints;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class StockPriceAndVolumeGetRsp 
{
	private Date tradeDate; // 日期
	private long tradeVolume; // 成交股數
	private long turnoverInValue; // 成交金額
	private float open; // 開盤價
	private float high; // 最高價
	private float low; // 最低價
	private float close; // 收盤價
	private float netChange; // 漲跌點數
	private long numberOfTransactions; // 成交筆數

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
	public float getOpen() {
		return open;
	}
	public void setOpen(float open) {
		this.open = open;
	}
	public float getHigh() {
		return high;
	}
	public void setHigh(float high) {
		this.high = high;
	}
	public float getLow() {
		return low;
	}
	public void setLow(float low) {
		this.low = low;
	}
	public float getClose() {
		return close;
	}
	public void setClose(float close) {
		this.close = close;
	}
	public float getNetChange() {
		return netChange;
	}
	public void setNetChange(float netChange) {
		this.netChange = netChange;
	}
	public long getNumberOfTransactions() {
		return numberOfTransactions;
	}
	public void setNumberOfTransactions(long numberOfTransactions) {
		this.numberOfTransactions = numberOfTransactions;
	}
}
