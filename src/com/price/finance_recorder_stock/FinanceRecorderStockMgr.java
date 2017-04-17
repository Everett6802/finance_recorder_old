package com.price.finance_recorder_stock;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import com.price.finance_recorder_base.*;
//import com.price.finance_recorder_cmn.FinanceRecorderCmnClass;
import com.price.finance_recorder_cmn.FinanceRecorderCmnDef;
import com.price.finance_recorder_stock.FinanceRecorderDatabaseTimeRange;
import com.price.finance_recorder_stock.FinanceRecorderStockDataHandler;


public class FinanceRecorderStockMgr extends FinanceRecorderMgrBase
{
	private FinanceRecorderCompanyGroupSet company_group_set = null;
	private FinanceRecorderDatabaseTimeRange database_time_range = null;

	protected FinanceRecorderDataHandlerInf get_data_handler()
	{
		FinanceRecorderDataHandlerInf data_handler = FinanceRecorderStockDataHandler.get_data_handler(source_type_index_list, company_group_set);
//		data_handler.set_finance_root_folerpath(finance_root_folderpath);
//		data_handler.set_finance_root_backup_folerpath(finance_root_backup_folderpath);
		return data_handler;
	}

	public void set_delete_sql_accuracy(FinanceRecorderCmnDef.DeleteSQLAccurancyType accurancy_type){delete_sql_accurancy_type = accurancy_type;}

	private short transform_company_word_list_to_group_set(List<String> company_word_list)
	{
/*
        The argument type:
        Company code number: 2347
        Company code number range: 2100-2200
        Company group number: [Gg]12
        Company code number/number range hybrid: 2347,2100-2200,2362,g2,1500-1510
*/
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		company_group_set = new FinanceRecorderCompanyGroupSet();
		for (String company_number : company_word_list)
		{
			Matcher matcher = FinanceRecorderCmnDef.get_regex_matcher("([\\d]{4})-([\\d]{4})", company_number);
			if (matcher == null)
			{
// Check if data is company code/group number
				Matcher matcher1 = FinanceRecorderCmnDef.get_regex_matcher("[Gg]([\\d]{1,})", company_number);
				if (matcher1 == null)
				{
// Company code number
					if (FinanceRecorderCmnDef.get_regex_matcher("([\\d]{4})", company_number) == null)
						throw new IllegalStateException(String.format("Unknown company number format: %s", company_number));
					ret = company_group_set.add_company(company_number);
					if (FinanceRecorderCmnDef.CheckFailure(ret))
						return ret;
				}
				else
				{
// Company group number
					int company_group_number = Integer.valueOf(matcher1.group(1));
					ret = company_group_set.add_company_group(company_group_number);
					if (FinanceRecorderCmnDef.CheckFailure(ret))
						return ret;
				}
			}
			else
			{
// Company code number Range
				int start_company_number = Integer.valueOf(matcher.group(1));
				int end_company_number = Integer.valueOf(matcher.group(2));
				LinkedList<String> number_list = new LinkedList<String>();
				for (int number = start_company_number ; number <= end_company_number ; number++)
					number_list.add(String.format("%04d", number));
				ret = company_group_set.add_company_list(number_list);
				if (FinanceRecorderCmnDef.CheckFailure(ret))
					return ret;
			}
		}
		ret = company_group_set.add_done();
		return ret;
	}

	public short set_company_from_file(String filename)
	{
		if (setup_param_done)
		{
			FinanceRecorderCmnDef.error("The manager class has been initialized....");
			return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
		}

		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		LinkedList<String> company_word_list = new LinkedList<String>();
// Read the content from the config file
		ret = FinanceRecorderCmnDef.read_company_config_file(filename, company_word_list);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;

		return set_company(company_word_list);
	}

	public short set_company(List<String> company_number_list)
	{
		if (setup_param_done)
		{
			FinanceRecorderCmnDef.error("The manager class has been initialized....");
			return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
		}

		return transform_company_word_list_to_group_set(company_number_list);
	}

	public short initialize()
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		ret = super.initialize();
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;
		if (company_group_set == null)
			company_group_set = FinanceRecorderCompanyGroupSet.get_whole_company_group_set();
// Initialize the database time range
		database_time_range = FinanceRecorderDatabaseTimeRange.get_instance();

		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	public short transfrom_csv_to_sql_multithread(int sub_company_group_set_amount, boolean stop_when_csv_not_foud)
	{
		if (current_csv_working_folerpath == null)
		{
			FinanceRecorderCmnDef.debug("current_csv_working_folerpath should NOT be NULL");
			return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
		}
		ArrayList<FinanceRecorderCompanyGroupSet> sub_company_group_set_list = company_group_set.get_sub_company_group_set_list(sub_company_group_set_amount);
// Create thread pool 
		ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(FinanceRecorderCmnDef.MAX_CONCURRENT_THREAD);
		LinkedList<Future<Integer>> res_list = new LinkedList<Future<Integer>>();
		for (FinanceRecorderCompanyGroupSet sub_company_group_set : sub_company_group_set_list)
		{
// Activate the task of writing data into SQL
			FinanceRecorderStockWriteSQLTask task = new FinanceRecorderStockWriteSQLTask(source_type_index_list, sub_company_group_set, current_csv_working_folerpath, stop_when_csv_not_foud);
			Future<Integer> res = executor.submit(task);
			res_list.add(res);
		}
// Check the result
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		int res_index = 0;
		for (Future<Integer> res : res_list)
		{
			try
			{
				ret = res.get().shortValue();
			}
			catch (ExecutionException e)
			{
				FinanceRecorderCmnDef.format_error("Fail to get return value in thread[%d], due to: %s", res_index, e.toString());
				ret = FinanceRecorderCmnDef.RET_FAILURE_UNKNOWN;
			}
			catch (InterruptedException e)
			{
				FinanceRecorderCmnDef.format_error("Fail to get return value in thread[%d], due to: %s", res_index, e.toString());
				ret = FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
			}
			if (FinanceRecorderCmnDef.CheckFailure(ret))
			{
				FinanceRecorderCmnDef.format_error("Fail to get return value in thread[%d], due to: %s", res_index, FinanceRecorderCmnDef.GetErrorDescription(ret));
				break;
			}
			res_index++;
		}
// Shut down the executor
		if(FinanceRecorderCmnDef.CheckSuccess(ret))
			executor.shutdown();
		else
			executor.shutdownNow();
		return ret;
	}
}
