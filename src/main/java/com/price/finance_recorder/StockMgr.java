package com.price.finance_recorder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;


public class StockMgr extends MgrBase
{
	static final public int COMPANY_PROFILE_ENTRY_FIELD_INDEX_COMPANY_CODE_NUMBER = CompanyProfile.COMPANY_PROFILE_ENTRY_FIELD_INDEX_COMPANY_CODE_NUMBER;
	static final public int COMPANY_PROFILE_ENTRY_FIELD_SIZE = CompanyProfile.COMPANY_PROFILE_ENTRY_FIELD_SIZE;

	private CmnClassStock.CompanyGroupSet company_group_set = null;
//	private DatabaseTimeRange database_time_range = null;

	protected CmnInf.DataHandlerInf get_data_handler()
	{
		assert method_index_list != null : "method_index_list should NOT be null";
		assert company_group_set != null : "company_group_set should NOT be null";
		CmnInf.DataHandlerInf data_handler = StockDataHandler.get_data_handler(method_index_list, company_group_set);
//		data_handler.set_finance_root_folerpath(finance_root_folderpath);
//		data_handler.set_finance_root_backup_folerpath(finance_root_backup_folderpath);
		return data_handler;
	}

	public short initialize()
	{
		short ret = CmnDef.RET_SUCCESS;
		ret = super.initialize();
		if (CmnDef.CheckFailure(ret))
			return ret;
//		database_time_range = DatabaseTimeRange.get_instance();
		ret = set_company(null);
		return ret;
	}

//	public short reset_data_source_rule()
//	{
//		short ret = super.reset_data_source_rule();
//		if (CmnDef.CheckSuccess(ret))
//		{
//			if (company_group_set != null)
//				company_group_set = null;
//		}
//		return ret;
//	}
//
//	public short set_data_source_rule_done()
//	{
//		short ret = super.set_data_source_rule_done();
//		if (CmnDef.CheckSuccess(ret))
//		{
//			if (company_group_set == null)
//				company_group_set = CmnClassStock.CompanyGroupSet.get_whole_company_group_set();
//		}
//		return ret;
//	}

	public void set_delete_sql_accuracy(CmnDef.DeleteSQLAccurancyType accurancy_type){delete_sql_accurancy_type = accurancy_type;}

	private short transform_company_word_list_to_group_set(List<String> company_word_list)
	{
/*
        The argument type:
        Company code number: 2347
        Company code number range: 2100-2200
        Company group number: [Gg]12
        Company code number/number range hybrid: 2347,2100-2200,2362,g2,1500-1510
*/
		assert company_word_list != null : "company_word_list should NOT be NULL";
		short ret = CmnDef.RET_SUCCESS;
		company_group_set = new CmnClassStock.CompanyGroupSet();
		for (String company_number : company_word_list)
		{
			Matcher matcher = CmnFunc.get_regex_matcher("([\\d]{4})-([\\d]{4})", company_number);
			if (matcher == null)
			{
// Check if data is company code/group number
				Matcher matcher1 = CmnFunc.get_regex_matcher("[Gg]([\\d]{1,})", company_number);
				if (matcher1 == null)
				{
// Company code number
					if (CmnFunc.get_regex_matcher("([\\d]{4})", company_number) == null)
						throw new IllegalStateException(String.format("Unknown company number format: %s", company_number));
					ret = company_group_set.add_company(company_number);
					if (CmnDef.CheckFailure(ret))
						return ret;
				}
				else
				{
// Company group number
					int company_group_number = Integer.valueOf(matcher1.group(1));
					ret = company_group_set.add_company_group(company_group_number);
					if (CmnDef.CheckFailure(ret))
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
				if (CmnDef.CheckFailure(ret))
					return ret;
			}
		}
		ret = company_group_set.add_done();
		return ret;
	}

	public short set_company_from_file(String filename)
	{
//		if (setup_data_source_rule_done)
//		{
//			CmnLogger.error("The data source rule has been initialized....");
//			return CmnDef.RET_FAILURE_INCORRECT_OPERATION;
//		}
		short ret = CmnDef.RET_SUCCESS;
		LinkedList<String> company_word_list = new LinkedList<String>();
// Read the content from the config file
		ret = CmnFunc.read_company_config_file(filename, company_word_list);
		if (CmnDef.CheckFailure(ret))
			return ret;
		return set_company(company_word_list);
	}

	public short set_company(List<String> company_number_list)
	{
//		if (setup_data_source_rule_done)
//		{
//			CmnLogger.error("The data source rule has been initialized....");
//			return CmnDef.RET_FAILURE_INCORRECT_OPERATION;
//		}
		short ret = CmnDef.RET_SUCCESS;
		if (company_number_list != null)
			ret = transform_company_word_list_to_group_set(company_number_list);
		else
			company_group_set = CmnClassStock.CompanyGroupSet.get_whole_company_group_set();
		return ret;
	}

	public short transfrom_csv_to_sql_multithread(int sub_company_group_set_amount, boolean stop_when_csv_not_foud)
	{
//		if (!setup_data_source_rule_done)
//		{
//			CmnLogger.error("The data source rule is NOT initialized....");
//			return CmnDef.RET_FAILURE_INCORRECT_OPERATION;
//		}
		if (current_csv_working_folerpath == null)
		{
			CmnLogger.debug("current_csv_working_folerpath should NOT be NULL");
			return CmnDef.RET_FAILURE_INCORRECT_OPERATION;
		}
		ArrayList<CmnClassStock.CompanyGroupSet> sub_company_group_set_list = company_group_set.get_sub_company_group_set_list(sub_company_group_set_amount);
// Create thread pool 
		ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(CmnDef.MAX_CONCURRENT_THREAD);
		LinkedList<Future<Integer>> res_list = new LinkedList<Future<Integer>>();
		for (CmnClassStock.CompanyGroupSet sub_company_group_set : sub_company_group_set_list)
		{
// Activate the task of writing data into SQL
			StockWriteSQLTask task = new StockWriteSQLTask(method_index_list, sub_company_group_set, current_csv_working_folerpath, stop_when_csv_not_foud);
			Future<Integer> res = executor.submit(task);
			res_list.add(res);
		}
// Check the result
		short ret = CmnDef.RET_SUCCESS;
		int res_index = 0;
		for (Future<Integer> res : res_list)
		{
			try
			{
				ret = res.get().shortValue();
			}
			catch (ExecutionException e)
			{
				CmnLogger.format_error("Fail to get return value in thread[%d], due to: %s", res_index, e.toString());
				ret = CmnDef.RET_FAILURE_UNKNOWN;
			}
			catch (InterruptedException e)
			{
				CmnLogger.format_error("Fail to get return value in thread[%d], due to: %s", res_index, e.toString());
				ret = CmnDef.RET_FAILURE_INCORRECT_OPERATION;
			}
			if (CmnDef.CheckFailure(ret))
			{
				CmnLogger.format_error("Fail to get return value in thread[%d], due to: %s", res_index, CmnDef.GetErrorDescription(ret));
				break;
			}
			res_index++;
		}
// Shut down the executor
		if(CmnDef.CheckSuccess(ret))
			executor.shutdown();
		else
			executor.shutdownNow();
		return ret;
	}
}
