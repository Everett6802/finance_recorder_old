package com.price.finance_recorder_rest.service;

import java.io.Serializable;

import com.price.finance_recorder_rest.common.CmnDef;
import com.price.finance_recorder_rest.exceptions.FinanceRecorderMissingRequiredFieldException;

public class StockExchangeAndVolumeDTO implements Serializable
{
	private static final long serialVersionUID = 7317922403273143822L;

	private String datasetFolderpath;

	public void validateRequiredFields() throws FinanceRecorderMissingRequiredFieldException
	{
		if (datasetFolderpath == null)
			datasetFolderpath = CmnDef.FINANCE_DATASET_RELATIVE_FOLDERPATH;
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
}
