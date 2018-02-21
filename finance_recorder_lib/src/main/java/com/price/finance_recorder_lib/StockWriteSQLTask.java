package com.price.finance_recorder_lib;

import java.util.LinkedList;
import java.util.concurrent.Callable;


class StockWriteSQLTask implements Callable<Integer>
{
	static int thread_count = 0;
	private CmnInf.DataHandlerInf data_handler = null;
	private String thread_description = null;
//	private boolean thread_operation_non_stop = false;

	public StockWriteSQLTask(final LinkedList<Integer> source_type_index_list, final CmnClassStock.CompanyGroupSet company_group_set, String finance_root_folderpath, boolean operation_non_stop)
	{
		this(source_type_index_list, company_group_set, finance_root_folderpath, operation_non_stop, String.format("StockWriteSQLThread:%d", ++thread_count));
	}
	public StockWriteSQLTask(final LinkedList<Integer> source_type_index_list, final CmnClassStock.CompanyGroupSet company_group_set, String finance_root_folderpath, boolean operation_non_stop, String description)
	{
		data_handler = StockDataHandler.get_data_handler(source_type_index_list, company_group_set);
		data_handler.set_current_csv_working_folerpath(finance_root_folderpath);
		data_handler.set_operation_non_stop(operation_non_stop);
		thread_description = description;
//		thread_operation_non_stop = operation_non_stop;
	}

	@Override
	public Integer call()
	{
		CmnLogger.format_error("The thread [%s] of stock write Starts......", thread_description);
		short ret = data_handler.transfrom_csv_to_sql();
		if (CmnDef.CheckFailure(ret))
			CmnLogger.format_error("The thread [%s] of transform stock CSV to SQL FAIL, due to: %s", thread_description, CmnDef.GetErrorDescription(ret));
		return Integer.valueOf(ret);
//		CmnLogger.format_debug("The thread for accessing [%s %s]", writer.get_description(), time_range_cfg.toString());
//		short ret = writer.write_to_sql(time_range_cfg, CmnDef.DatabaseCreateThreadType.DatabaseCreateThread_Multiple, CmnDef.DatabaseEnableBatchType.DatabaseEnableBatch_Yes);
//		if (CmnDef.CheckFailure(ret))
//			CmnLogger.format_error("The thread for accessing [%s %s] FAIL, due to: %s", writer.get_description(), time_range_cfg.toString(), CmnDef.GetErrorDescription(ret));
//		return Integer.valueOf(ret);
	}
}
