package com.price.finance_recorder_rest.service;

import java.util.LinkedList;
import java.util.List;

import com.price.finance_recorder_rest.common.CmnDef;
import com.price.finance_recorder_rest.common.CmnDef.FinanceMethod;
import com.price.finance_recorder_rest.common.CmnFunc;
import com.price.finance_recorder_rest.exceptions.ResourceNotFoundException;
import com.price.finance_recorder_rest.persistence.MySQLDAO;

public class FinanceServiceAdapter
{
	public static void create_table(FinanceMethod finance_method, String dataset_folderpath/*, MySQLDAO dao*/)
	{
		short ret = CmnDef.RET_SUCCESS;
// Cleanup the old data if table exist
		MySQLDAO.delete_if_exist(finance_method);
			
// Read data from the dataset
		String filepath = CmnFunc.get_dataset_market_csv_filepath(finance_method.value(), dataset_folderpath);
		LinkedList<String> data_line_list = new LinkedList<String>();
		ret = CmnFunc.read_file_lines(filepath, data_line_list);
		if (CmnDef.CheckFailure(ret))
		{
			String errmsg = String.format("Fail to read market[%d] dataset", finance_method.value());
			throw new ResourceNotFoundException(errmsg);
		}
		MySQLDAO.create(finance_method, data_line_list);
	}

	public static List<?> read_table(FinanceMethod finance_method, int start, int limit/*, MySQLDAO dao*/)
	{
		return MySQLDAO.read(finance_method, start, limit);
	}

	public static void update_table(FinanceMethod finance_method, String dataset_folderpath/*, MySQLDAO dao*/)
	{
		short ret = CmnDef.RET_SUCCESS;
// Read data from the dataset
		String filepath = CmnFunc.get_dataset_market_csv_filepath(finance_method.value(), dataset_folderpath);
		LinkedList<String> data_line_list = new LinkedList<String>();
		ret = CmnFunc.read_file_lines(filepath, data_line_list);
		if (CmnDef.CheckFailure(ret))
		{
			String errmsg = String.format("Fail to read market[%d] dataset", finance_method.value());
			throw new ResourceNotFoundException(errmsg);
		}
		MySQLDAO.update(finance_method, data_line_list);
	}

	public static void delete_table(FinanceMethod finance_method/*, MySQLDAO dao*/)
	{
		MySQLDAO.delete(finance_method);
	}
}
