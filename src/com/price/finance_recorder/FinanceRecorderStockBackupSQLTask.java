package com.price.finance_recorder;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import com.price.finance_recorder_cmn.FinanceRecorderCmnClass;
import com.price.finance_recorder_cmn.FinanceRecorderCmnDef;
import com.price.finance_recorder_cmn.FinanceRecorderCmnClass.CompanyGroupSet;


public class FinanceRecorderStockBackupSQLTask implements Callable<Integer>
{
	static int thread_count = 0;
	private FinanceRecorderDataHandlerInf data_handler = null;
	private String thread_description = null;
	private String csv_backup_folderpath = null;
	FinanceRecorderCmnClass.TimeRangeCfg csv_time_range_cfg = null;

	public FinanceRecorderStockBackupSQLTask(final ArrayList<Integer> source_type_list, final CompanyGroupSet company_group_set, FinanceRecorderCmnClass.TimeRangeCfg time_range_cfg, String backup_folderpath)
	{
		this(source_type_list, company_group_set, time_range_cfg, backup_folderpath, String.format("StockBackupSQLThread:%d", ++thread_count));
	}
	public FinanceRecorderStockBackupSQLTask(final ArrayList<Integer> source_type_list, final CompanyGroupSet company_group_set, FinanceRecorderCmnClass.TimeRangeCfg time_range_cfg, String backup_folderpath, String description)
	{
		data_handler = FinanceRecorderStockDataHandler.get_data_handler(source_type_list, company_group_set);
		thread_description = description;
		csv_backup_folderpath = backup_folderpath;
		csv_time_range_cfg = time_range_cfg;
	}

	@Override
	public Integer call()
	{
		FinanceRecorderCmnDef.format_error("The thread [%s] of stock write Starts......", thread_description);
		short ret = data_handler.transfrom_sql_to_csv(csv_time_range_cfg, csv_backup_folderpath);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			FinanceRecorderCmnDef.format_error("The thread [%s] of transform stock SQL to CSV FAIL, due to: %s", thread_description, FinanceRecorderCmnDef.GetErrorDescription(ret));
		return Integer.valueOf(ret);
	}
}
