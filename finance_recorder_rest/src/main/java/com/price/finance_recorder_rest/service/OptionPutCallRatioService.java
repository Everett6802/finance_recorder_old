package com.price.finance_recorder_rest.service;

import com.price.finance_recorder_rest.common.CmnDef;
import com.price.finance_recorder_rest.persistence.MySQLDAO;

public class OptionPutCallRatioService
{
	public void create(OptionPutCallRatioDTO dto)
	{
		FinanceServiceProxy.csv2sql(CmnDef.FinanceMethod.FinanceMethod_OptionPutCallRatio, dto.getDatasetFolderpath(), new MySQLDAO());
	}
}
