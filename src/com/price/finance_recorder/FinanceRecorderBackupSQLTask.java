package com.price.finance_recorder;

import java.util.concurrent.Callable;


public class FinanceRecorderBackupSQLTask implements Callable<Integer>
{
	private FinanceRecorderDataHandler finance_recorder_backuper = null;
	private FinanceRecorderCmnClass.TimeRangeCfg time_range_cfg = null;
	String backup_csv_foldername = null;
	FinanceRecorderCmnClass.ResultSet backup_result_set = null;

	public FinanceRecorderBackupSQLTask(FinanceRecorderDataHandler finance_recorder_backuper_obj, FinanceRecorderCmnClass.TimeRangeCfg cfg, String csv_foldername, FinanceRecorderCmnClass.ResultSet result_set)
	{
		finance_recorder_backuper = finance_recorder_backuper_obj;
		time_range_cfg = cfg;
		backup_csv_foldername = csv_foldername;
		backup_result_set = result_set;
	}

	@Override
	public Integer call()
	{
		FinanceRecorderCmnDef.format_debug("The thread for accessing [%s %s]", finance_recorder_backuper.get_description(), time_range_cfg.toString());
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		try
		{
			ret = finance_recorder_backuper.backup_from_sql(time_range_cfg, backup_csv_foldername, backup_result_set);
		}
		catch (IllegalArgumentException e)
		{
			ret = FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
		}
		catch (Exception e)
		{
			ret = FinanceRecorderCmnDef.RET_FAILURE_UNKNOWN;
		}
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			FinanceRecorderCmnDef.format_error("The thread for accessing [%s %s] FAIL, due to: %s", finance_recorder_backuper.get_description(), time_range_cfg.toString(), FinanceRecorderCmnDef.GetErrorDescription(ret));
		return Integer.valueOf(ret);
	}
}
