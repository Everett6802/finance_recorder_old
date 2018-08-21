package com.price.finance_recorder_rest.service;

import java.util.LinkedList;

import com.price.finance_recorder_rest.common.CmnDef;
import com.price.finance_recorder_rest.common.CmnFunc;
import com.price.finance_recorder_rest.exceptions.FinanceRecorderResourceNotFoundException;
import com.price.finance_recorder_rest.persistence.MySQLDAO;

public class StockExchangeAndVolumeService
{
	public void create(StockExchangeAndVolumeDTO dto)
	{
		short ret = CmnDef.RET_SUCCESS;
// Read data from the dataset
//		String filepath = String.format("%s/%s.csv", CmnFunc.get_dataset_market_data_path(), CmnDef.FINANCE_DATA_NAME_LIST[method_index]);
		String filepath = CmnFunc.get_dataset_market_csv_filepath(CmnDef.FinanceMethod.FinanceMethod_StockExchangeAndVolume.value(), dto.getDatasetFolderpath());
		LinkedList<String> data_line_list = new LinkedList<String>();
		ret = CmnFunc.read_file_lines(filepath, data_line_list);
		if (CmnDef.CheckFailure(ret))
		{
			String errmsg = String.format("Fail to read market[%d] dataset", CmnDef.FinanceMethod.FinanceMethod_StockExchangeAndVolume.value());
			throw new FinanceRecorderResourceNotFoundException(errmsg);
		}
		MySQLDAO dao = new MySQLDAO();
		dao.create(CmnDef.FinanceMethod.FinanceMethod_StockExchangeAndVolume, data_line_list);
	}

	public void read()
	{

	}
}
