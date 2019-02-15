package com.price.finance_recorder_rest.service;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

//import com.price.finance_recorder_rest.common.CmnDBDef;
import com.price.finance_recorder_rest.common.CmnDef;
import com.price.finance_recorder_rest.exceptions.MissingRequiredFieldException;

public class StockPriceAndVolumeDTO implements Serializable
{
	private static final long serialVersionUID = 5879054172613255738L;
// IN
	private String datasetFolderpath;
	private int start;
	private int limit;
// OUT
	private Date tradeDate; // 日期
	private long tradeVolume; // 成交股數
	private long turnoverInValue; // 成交金額
	private float open; // 開盤價
	private float high; // 最高價
	private float low; // 最低價
	private float close; // 收盤價
	private float netChange; // 漲跌點數
	private long numberOfTransactions; // 成交筆數

	public void validateRequiredFields() throws MissingRequiredFieldException
	{
		if (datasetFolderpath == null)
			datasetFolderpath = CmnDef.FINANCE_DATASET_FOLDER_PATH;
		if (start == 0)
			start = CmnDef.FINANCE_DATA_START_INDEX;
		if (limit == 0)
			limit = CmnDef.FINANCE_DATA_LIMIT;
	}

	void fillin_field(ArrayList<Object> field_list)
	{
//		String[] field_name_list = CmnDBDef.get_table_field_name_definition(CmnDef.FinanceMethod.FinanceMethod_StockPriceAndVolume);
		String trade_date_str = (String)field_list.get(0);
		try 
		{
			Date trade_date = new SimpleDateFormat("yyyy-MM-dd").parse(trade_date_str);
			setTradeDate(trade_date);
		} 
		catch (ParseException e) 
		{
			String errmsg = String.format("Fail to parse the trade date time in DTO[%s]", CmnDef.FINANCE_METHOD_DESCRIPTION_LIST[CmnDef.FinanceMethod.FinanceMethod_StockPriceAndVolume.value()]);
			throw new IllegalArgumentException(errmsg);
		}
		setTradeVolume((long)field_list.get(1));
		setTurnoverInValue((long)field_list.get(2));
		setOpen((float)field_list.get(3));
		setHigh((float)field_list.get(4));
		setLow((float)field_list.get(5));
		setClose((float)field_list.get(6));
		setNetChange((float)field_list.get(7));
		setNumberOfTransactions((long)field_list.get(8));
	}
	
	public String getDatasetFolderpath() 
	{
		return datasetFolderpath;
	}

	public void setDatasetFolderpath(String datasetFolderpath) 
	{
		this.datasetFolderpath = datasetFolderpath;
	}

	public int getStart() 
	{
		return start;
	}

	public void setStart(int start) 
	{
		this.start = start;
	}

	public int getLimit() 
	{
		return limit;
	}

	public void setLimit(int limit) 
	{
		this.limit = limit;
	}

	public Date getTradeDate() 
	{
		return tradeDate;
	}

	public void setTradeDate(Date tradeDate) 
	{
		this.tradeDate = tradeDate;
	}

	public long getTradeVolume() 
	{
		return tradeVolume;
	}

	public void setTradeVolume(long tradeVolume) 
	{
		this.tradeVolume = tradeVolume;
	}

	public long getTurnoverInValue() 
	{
		return turnoverInValue;
	}

	public void setTurnoverInValue(long turnoverInValue) 
	{
		this.turnoverInValue = turnoverInValue;
	}

	public float getOpen() 
	{
		return open;
	}

	public void setOpen(float open) 
	{
		this.open = open;
	}

	public float getHigh() 
	{
		return high;
	}

	public void setHigh(float high) 
	{
		this.high = high;
	}

	public float getLow() 
	{
		return low;
	}

	public void setLow(float low) 
	{
		this.low = low;
	}

	public float getClose() 
	{
		return close;
	}

	public void setClose(float close) 
	{
		this.close = close;
	}

	public float getNetChange() 
	{
		return netChange;
	}

	public void setNetChange(float netChange) 
	{
		this.netChange = netChange;
	}

	public long getNumberOfTransactions() 
	{
		return numberOfTransactions;
	}

	public void setNumberOfTransactions(long numberOfTransactions) 
	{
		this.numberOfTransactions = numberOfTransactions;
	}
}
