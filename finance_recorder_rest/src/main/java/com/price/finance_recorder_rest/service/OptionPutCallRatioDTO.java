package com.price.finance_recorder_rest.service;

import java.io.Serializable;
import java.util.Date;

import com.price.finance_recorder_rest.exceptions.MissingRequiredFieldException;

public class OptionPutCallRatioDTO implements Serializable
{
	private static final long serialVersionUID = -597635324732204033L;
// IN
	private String datasetFolderpath;
	private int start;
	private int limit;
// OUT
	private Date tradeDate; // 日期
	private long putTradeVolume; // 賣權成交量
	private long callTradeVolume; // 買權成交量
	private float putCallRatio; // 買賣權成交量比率
	private long putOITradeVolume; // 賣權未平倉量
	private long callOITradeVolume; // 買權未平倉量
	private float putCallOIRatio; // 買賣權未平倉量比率

	public void validateRequiredFields() throws MissingRequiredFieldException
	{
//		if (datasetFolderpath == null)
//			datasetFolderpath = CmnDef.FINANCE_DATASET_RELATIVE_FOLDERPATH;
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

	public long getPutTradeVolume()
	{
		return putTradeVolume;
	}

	public void setPutTradeVolume(long putTradeVolume)
	{
		this.putTradeVolume = putTradeVolume;
	}

	public long getCallTradeVolume()
	{
		return callTradeVolume;
	}

	public void setCallTradeVolume(long callTradeVolume)
	{
		this.callTradeVolume = callTradeVolume;
	}

	public float getPutCallRatio()
	{
		return putCallRatio;
	}

	public void setPutCallRatio(float putCallRatio)
	{
		this.putCallRatio = putCallRatio;
	}

	public long getPutOITradeVolume()
	{
		return putOITradeVolume;
	}

	public void setPutOITradeVolume(long putOITradeVolume)
	{
		this.putOITradeVolume = putOITradeVolume;
	}

	public long getCallOITradeVolume()
	{
		return callOITradeVolume;
	}

	public void setCallOITradeVolume(long callOITradeVolume)
	{
		this.callOITradeVolume = callOITradeVolume;
	}

	public float getPutCallOIRatio()
	{
		return putCallOIRatio;
	}

	public void setPutCallOIRatio(float putCallOIRatio)
	{
		this.putCallOIRatio = putCallOIRatio;
	}

//	public String getDatasetFolderpath()
//	{
//		return datasetFolderpath;
//	}
//
//	public void setDatasetFolderpath(String datasetFolderpath)
//	{
//		this.datasetFolderpath = datasetFolderpath;
//	}

}
