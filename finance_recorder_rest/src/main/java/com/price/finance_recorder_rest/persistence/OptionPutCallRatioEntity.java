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
@Table(name = "option_put_call_ratio")
public class OptionPutCallRatioEntity implements Serializable
{
	private static final long serialVersionUID = 23799808326808514L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private int id;

//	@Temporal(TemporalType.DATE)
	@Column(name = "trade_date", updatable = false, nullable = false)
	private Date tradeDate; // 日期

	@Column(name = "put_trade_volume", nullable = false)
	private long putTradeVolume; // 賣權成交量

	@Column(name = "call_trade_volume", nullable = false)
	private long callTradeVolume; // 買權成交量

	@Column(name = "put_call_ratio", nullable = false)
	private float putCallRatio; // 買賣權成交量比率

	@Column(name = "put_oi_trade_volume", nullable = false)
	private long putOITradeVolume; // 賣權未平倉量

	@Column(name = "call_oi_trade_volume", nullable = false)
	private long callOITradeVolume; // 買權未平倉量

	@Column(name = "put_call_oi_ratio", nullable = false)
	private float putCallOIRatio; // 買賣權未平倉量比率

	public Date getTradeDate()
	{
		return tradeDate;
	}

	public void setTradeDate(Date TradeDate)
	{
		tradeDate = TradeDate;
	}

	public long getPutTradeVolume()
	{
		return putTradeVolume;
	}

	public void setPutTradeVolume(long PutTradeVolume)
	{
		putTradeVolume = PutTradeVolume;
	}

	public long getCallTradeVolume()
	{
		return callTradeVolume;
	}

	public void setCallTradeVolume(long CallTradeVolume)
	{
		callTradeVolume = CallTradeVolume;
	}

	public float getPutCallRatio()
	{
		return putCallRatio;
	}

	public void setPutCallRatio(float PutCallRatio)
	{
		putCallRatio = PutCallRatio;
	}

	public long getPutOITradeVolume()
	{
		return putOITradeVolume;
	}

	public void setPutOITradeVolume(long PutOITradeVolume)
	{
		putOITradeVolume = PutOITradeVolume;
	}

	public long getCallOITradeVolume()
	{
		return callOITradeVolume;
	}

	public void setCallOITradeVolume(long CallOITradeVolume)
	{
		callOITradeVolume = CallOITradeVolume;
	}

	public float getPutCallOIRatio()
	{
		return putCallOIRatio;
	}

	public void setPutCallOIRatio(float PutCallOIRatio)
	{
		putCallOIRatio = PutCallOIRatio;
	}
}
