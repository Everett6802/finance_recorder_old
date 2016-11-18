package com.price.finance_recorder_stock;

import java.util.LinkedList;
import java.util.concurrent.Callable;
import com.price.finance_recorder_base.FinanceRecorderDataHandlerInf;
import com.price.finance_recorder_cmn.FinanceRecorderCmnClass;
import com.price.finance_recorder_cmn.FinanceRecorderCmnDef;


public class FinanceRecorderStockBackupSQLTask implements Callable<Integer>
{
	static int thread_count = 0;
	private FinanceRecorderDataHandlerInf data_handler = null;
	private String thread_description = null;
//	private String csv_backup_folderpath = null;
	FinanceRecorderCmnClass.FinanceTimeRange csv_finance_time_range = null;
	FinanceRecorderCmnClass.QuerySet csv_query_set = null;
	
	public FinanceRecorderStockBackupSQLTask(final LinkedList<Integer> source_type_index_list, final FinanceRecorderCompanyGroupSet company_group_set, FinanceRecorderCmnClass.QuerySet query_set, FinanceRecorderCmnClass.FinanceTimeRange finance_time_range, String finance_backup_root_folderpath)
	{
		this(source_type_index_list, company_group_set, query_set, finance_time_range, finance_backup_root_folderpath, String.format("StockBackupSQLThread:%d", ++thread_count));
	}
	public FinanceRecorderStockBackupSQLTask(final LinkedList<Integer> source_type_index_list, final FinanceRecorderCompanyGroupSet company_group_set, FinanceRecorderCmnClass.QuerySet query_set, FinanceRecorderCmnClass.FinanceTimeRange finance_time_range, String finance_backup_root_folderpath, String description)
	{
		data_handler = FinanceRecorderStockDataHandler.get_data_handler(source_type_index_list, company_group_set);
		data_handler.set_finance_root_backup_folerpath(finance_backup_root_folderpath);
		thread_description = description;
		csv_finance_time_range = finance_time_range;
		csv_query_set = query_set;
	}

	@Override
	public Integer call()
	{
		FinanceRecorderCmnDef.format_error("The thread [%s] of stock write Starts......", thread_description);
		short ret = data_handler.transfrom_sql_to_csv(csv_query_set, csv_finance_time_range);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			FinanceRecorderCmnDef.format_error("The thread [%s] of transform stock SQL to CSV FAIL, due to: %s", thread_description, FinanceRecorderCmnDef.GetErrorDescription(ret));
		return Integer.valueOf(ret);
	}
}
