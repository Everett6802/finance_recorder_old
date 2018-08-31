package com.price.finance_recorder_rest.service;

import java.util.LinkedList;
import java.util.List;

import com.price.finance_recorder_rest.common.CmnDef;
import com.price.finance_recorder_rest.common.CmnDef.FinanceMethod;
import com.price.finance_recorder_rest.common.CmnFunc;
import com.price.finance_recorder_rest.exceptions.FinanceRecorderResourceNotFoundException;
import com.price.finance_recorder_rest.persistence.MySQLDAO;

public class FinanceServiceProxy
{
	public static void csv2sql(FinanceMethod finance_method, String dataset_folderpath, MySQLDAO dao)
	{
		short ret = CmnDef.RET_SUCCESS;
// Read data from the dataset
//		String filepath = String.format("%s/%s.csv", CmnFunc.get_dataset_market_data_path(), CmnDef.FINANCE_DATA_NAME_LIST[method_index]);
		String filepath = CmnFunc.get_dataset_market_csv_filepath(finance_method.value(), dataset_folderpath);
		LinkedList<String> data_line_list = new LinkedList<String>();
		ret = CmnFunc.read_file_lines(filepath, data_line_list);
		if (CmnDef.CheckFailure(ret))
		{
			String errmsg = String.format("Fail to read market[%d] dataset", finance_method.value());
			throw new FinanceRecorderResourceNotFoundException(errmsg);
		}
//		MySQLDAO dao = new MySQLDAO();
		dao.create(finance_method, data_line_list);
	}

	public static List<?> from_sql(FinanceMethod finance_method, int start, int limit, MySQLDAO dao)
	{
		return dao.read(finance_method, start, limit);
	}
}
