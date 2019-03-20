package com.price.finance_recorder_rest.service;

import java.util.LinkedList;
import java.util.List;

import com.price.finance_recorder_rest.common.CmnDef;
import com.price.finance_recorder_rest.common.CmnDef.FinanceMethod;
import com.price.finance_recorder_rest.common.CmnFunc;
import com.price.finance_recorder_rest.exceptions.ResourceNotFoundException;
import com.price.finance_recorder_rest.libs.CSVHandler;
import com.price.finance_recorder_rest.libs.CompanyProfile;
import com.price.finance_recorder_rest.persistence.MySQLDAO;

public class FinanceServiceDelegator
{
	private static CompanyProfile company_profile = null;
	
	private static synchronized CompanyProfile get_company_profile()
	{
		if (company_profile == null)
		{
			company_profile = CompanyProfile.get_instance();
		}
		return company_profile;
	}
	
	private static String get_dataset_csv_filepath(FinanceMethod finance_method, String company_number, String dataset_folderpath)
	{
		if (company_number == null)
		{
			return CmnFunc.get_dataset_market_csv_filepath(finance_method.value(), dataset_folderpath);
		}
		else
		{
			if (company_profile == null)
				company_profile = get_company_profile();
			int company_group_number = company_profile.lookup_company_group_number(company_number);
			return CmnFunc.get_dataset_stock_csv_filepath(finance_method.value(), company_number, company_group_number, dataset_folderpath);
		}
	}
	
	public static void create_table(FinanceMethod finance_method, String company_number, String dataset_folderpath/*, MySQLDAO dao*/)
	{
		short ret = CmnDef.RET_SUCCESS;
// Cleanup the old data if table exist
		if (company_number != null)
			MySQLDAO.delete_if_exist(finance_method, company_number);
		else
			MySQLDAO.delete_if_exist(finance_method);
			
// Read data from the dataset
		String filepath = get_dataset_csv_filepath(finance_method, company_number, dataset_folderpath);
		LinkedList<String> data_line_list = new LinkedList<String>();
		ret = CmnFunc.read_file_lines(filepath, data_line_list);
		if (CmnDef.CheckFailure(ret))
		{
			String errmsg = String.format("Fail to read market[%d] dataset", finance_method.value());
			throw new ResourceNotFoundException(errmsg);
		}
		if (company_number != null)
			MySQLDAO.create(finance_method, company_number, data_line_list);
		else
			MySQLDAO.create(finance_method, data_line_list);
	}
	public static void create_table(FinanceMethod finance_method, String dataset_folderpath/*, MySQLDAO dao*/)
	{
		create_table(finance_method, null, dataset_folderpath);
	}

	public static List<?> read_table(FinanceMethod finance_method, String company_number, int start, int limit/*, MySQLDAO dao*/)
	{
		if (company_number != null)
			return MySQLDAO.read(finance_method, company_number, start, limit);
		else
			return MySQLDAO.read(finance_method, start, limit);
	}
	public static List<?> read_table(FinanceMethod finance_method, int start, int limit/*, MySQLDAO dao*/)
	{
		return read_table(finance_method, null, start, limit);
	}

	public static void update_table(FinanceMethod finance_method, String company_number, String dataset_folderpath/*, MySQLDAO dao*/)
	{
		short ret = CmnDef.RET_SUCCESS;
// Read data from the dataset
		String filepath = get_dataset_csv_filepath(finance_method, company_number, dataset_folderpath);
		LinkedList<String> data_line_list = new LinkedList<String>();
		ret = CmnFunc.read_file_lines(filepath, data_line_list);
		if (CmnDef.CheckFailure(ret))
		{
			String errmsg = String.format("Fail to read market[%d] dataset", finance_method.value());
			throw new ResourceNotFoundException(errmsg);
		}
		if (company_number != null)
			MySQLDAO.update(finance_method, company_number, data_line_list);
		else
			MySQLDAO.update(finance_method, data_line_list);
	}
	public static void update_table(FinanceMethod finance_method, String dataset_folderpath/*, MySQLDAO dao*/)
	{
		update_table(finance_method, null, dataset_folderpath);
	}
	
	public static void delete_table(FinanceMethod finance_method, String company_number/*, MySQLDAO dao*/)
	{
		if (company_number != null)
			MySQLDAO.delete(finance_method, company_number);
		else
			MySQLDAO.delete(finance_method);
	}
	public static void delete_table(FinanceMethod finance_method)
	{
		delete_table(finance_method, null);
	}
// SQL table metadata
	public static String read_table_metadata(FinanceMethod finance_method, String company_number)
	{
		String metadata = ((company_number != null) ? String.format("%s\n", CmnDef.FINANCE_DATA_NAME_LIST[finance_method.value()]) : String.format("%s:%s\n", CmnDef.FINANCE_DATA_NAME_LIST[finance_method.value()], company_number));
		metadata += MySQLDAO.read_statistics(finance_method, company_number);
		metadata += "\n";
		metadata += MySQLDAO.read_column_name(finance_method, company_number);
		metadata += "\n";
		return metadata;
	}
// CSV file metadata
	public static String read_file_metadata(FinanceMethod finance_method, String company_number, String dataset_folderpath)
	{
		String metadata = ((company_number != null) ? String.format("%s\n", CmnDef.FINANCE_DATA_NAME_LIST[finance_method.value()]) : String.format("%s:%s\n", CmnDef.FINANCE_DATA_NAME_LIST[finance_method.value()], company_number));
		metadata += CSVHandler.read_statistics(finance_method, company_number, dataset_folderpath);
		metadata += "\n";
//		metadata += MySQLDAO.read_column_name(finance_method, company_number);
//		metadata += "\n";
		return metadata;
	}
}
