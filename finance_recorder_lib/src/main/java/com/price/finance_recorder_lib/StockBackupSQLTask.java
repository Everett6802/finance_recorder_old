package com.price.finance_recorder_lib;

import java.util.LinkedList;
import java.util.concurrent.Callable;


public class StockBackupSQLTask implements Callable<Integer>
{
	static int thread_count = 0;
	private CmnInf.DataHandlerInf data_handler = null;
	private String thread_description = null;
//	private String csv_backup_folderpath = null;
	CmnClass.FinanceTimeRange csv_finance_time_range = null;
	CmnClass.QuerySet csv_query_set = null;
//	boolean sql_select_operation_non_stop;
	
	public StockBackupSQLTask(final LinkedList<Integer> source_type_index_list, final CmnClassStock.CompanyGroupSet company_group_set, CmnClass.QuerySet query_set, CmnClass.FinanceTimeRange finance_time_range, String finance_backup_root_folderpath, boolean operation_non_stop)
	{
		this(source_type_index_list, company_group_set, query_set, finance_time_range, finance_backup_root_folderpath, String.format("StockBackupSQLThread:%d", ++thread_count), operation_non_stop);
	}
	public StockBackupSQLTask(final LinkedList<Integer> source_type_index_list, final CmnClassStock.CompanyGroupSet company_group_set, CmnClass.QuerySet query_set, CmnClass.FinanceTimeRange finance_time_range, String finance_backup_root_folderpath, String description, boolean operation_non_stop)
	{
		data_handler = StockDataHandler.get_data_handler(source_type_index_list, company_group_set);
		data_handler.set_current_csv_working_folerpath(finance_backup_root_folderpath);
		data_handler.set_operation_non_stop(operation_non_stop);
		thread_description = description;
		csv_finance_time_range = finance_time_range;
		csv_query_set = query_set;
//		sql_select_operation_non_stop = operation_non_stop;
	}

	@Override
	public Integer call()
	{
		CmnLogger.format_error("The thread [%s] of stock write Starts......", thread_description);
		short ret = data_handler.transfrom_sql_to_csv(csv_query_set, csv_finance_time_range);
		if (CmnDef.CheckFailure(ret))
			CmnLogger.format_error("The thread [%s] of transform stock SQL to CSV FAIL, due to: %s", thread_description, CmnDef.GetErrorDescription(ret));
		return Integer.valueOf(ret);
	}
}
