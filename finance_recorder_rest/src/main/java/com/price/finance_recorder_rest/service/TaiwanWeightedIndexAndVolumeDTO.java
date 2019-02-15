package com.price.finance_recorder_rest.service;

import java.io.Serializable;
import java.util.Date;

import com.price.finance_recorder_rest.common.CmnDef;
import com.price.finance_recorder_rest.exceptions.MissingRequiredFieldException;

public class TaiwanWeightedIndexAndVolumeDTO implements Serializable
{
	private static final long serialVersionUID = 7317922403273143822L;
// IN
	private String datasetFolderpath;
	private int start;
	private int limit;
// OUT
	private Date tradeDate; // 日期
	private long tradeVolume; // 成交股數
	private long turnoverInValue; // 成交金額
	private int numberOfTransactions; // 成交筆數
	private float weightedStockIndex; // 發行量加權股價指數
	private float netChange; // 漲跌點數

	public void validateRequiredFields() throws MissingRequiredFieldException
	{
		if (datasetFolderpath == null)
			datasetFolderpath = CmnDef.FINANCE_DATASET_FOLDER_PATH;
		if (start == 0)
			start = CmnDef.FINANCE_DATA_START_INDEX;
		if (limit == 0)
			limit = CmnDef.FINANCE_DATA_LIMIT;
//		if ((userDto.getFirstName() == null) || userDto.getFirstName().isEmpty() || (userDto.getLastName() == null) || userDto.getLastName().isEmpty() || (userDto.getEmail() == null) || userDto
//				.getEmail().isEmpty() || (userDto.getPassword() == null) || userDto.getPassword().isEmpty())
//		{
//			throw new MissingRequiredFieldException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());
//		}
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

////////////////////////////////////////////////////////////////////////////////////////

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

	public int getNumberOfTransactions()
	{
		return numberOfTransactions;
	}

	public void setNumberOfTransactions(int numberOfTransactions)
	{
		this.numberOfTransactions = numberOfTransactions;
	}

	public float getWeightedStockIndex()
	{
		return weightedStockIndex;
	}

	public void setWeightedStockIndex(float weightedStockIndex)
	{
		this.weightedStockIndex = weightedStockIndex;
	}

	public float getNetChange()
	{
		return netChange;
	}

	public void setNetChange(float netChange)
	{
		this.netChange = netChange;
	}

}
