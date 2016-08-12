package com.price.finance_recorder;

import java.util.ArrayList;
import java.util.concurrent.Callable;
//import com.price.finance_recorder_cmn.FinanceRecorderCmnClass;
import com.price.finance_recorder_cmn.FinanceRecorderCmnDef;
import com.price.finance_recorder_cmn.FinanceRecorderCmnClass.CompanyGroupSet;


public class FinanceRecorderStockWriteSQLTask implements Callable<Integer>
{
	static int thread_count = 0;
	private FinanceRecorderDataHandlerInf writer = null;
	private String thread_description = null;

	public FinanceRecorderStockWriteSQLTask(final ArrayList<Integer> source_type_list, final CompanyGroupSet company_group_set)
	{
		this(source_type_list, company_group_set, String.format("StockWriteSQLThread:%d", ++thread_count));
	}
	public FinanceRecorderStockWriteSQLTask(final ArrayList<Integer> source_type_list, final CompanyGroupSet company_group_set, String description)
	{
		writer = FinanceRecorderStockDataHandler.get_data_handler(source_type_list, company_group_set);
		thread_description = description;
	}

	@Override
	public Integer call()
	{
		FinanceRecorderCmnDef.format_error("The thread [%s] of stock write Starts......", thread_description);
		short ret = writer.transfrom_csv_to_sql();
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			FinanceRecorderCmnDef.format_error("The thread [%s] of transform stock CSV to SQL FAIL, due to: %s", thread_description, FinanceRecorderCmnDef.GetErrorDescription(ret));
		return Integer.valueOf(ret);
//		FinanceRecorderCmnDef.format_debug("The thread for accessing [%s %s]", writer.get_description(), time_range_cfg.toString());
//		short ret = writer.write_to_sql(time_range_cfg, FinanceRecorderCmnDef.DatabaseCreateThreadType.DatabaseCreateThread_Multiple, FinanceRecorderCmnDef.DatabaseEnableBatchType.DatabaseEnableBatch_Yes);
//		if (FinanceRecorderCmnDef.CheckFailure(ret))
//			FinanceRecorderCmnDef.format_error("The thread for accessing [%s %s] FAIL, due to: %s", writer.get_description(), time_range_cfg.toString(), FinanceRecorderCmnDef.GetErrorDescription(ret));
//		return Integer.valueOf(ret);
	}
}
