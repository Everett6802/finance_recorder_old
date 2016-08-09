package com.price.finance_recorder;

import com.price.finance_recorder_cmn.FinanceRecorderCmnClass;

public interface FinanceRecorderDataHandlerInf 
{
	public short read_from_csv(FinanceRecorderCSVHandlerMap csv_data_map);
	public short write_into_sql(final FinanceRecorderCSVHandlerMap csv_data_map);
	public short transfrom_csv_to_sql();
	public short read_from_sql(FinanceRecorderCmnClass.QuerySet query_set, FinanceRecorderCmnClass.TimeRangeCfg time_range_cfg, FinanceRecorderCmnClass.ResultSetMap result_set_map);
	public short read_from_sql(FinanceRecorderCmnClass.TimeRangeCfg time_range_cfg, FinanceRecorderCmnClass.ResultSetMap result_set_map);
	public short write_into_csv(FinanceRecorderCmnClass.ResultSetMap result_set_map);
	public short transfrom_sql_to_csv(FinanceRecorderCmnClass.QuerySet query_set, FinanceRecorderCmnClass.TimeRangeCfg time_range_cfg, String csv_backup_foldername);
	public short transfrom_sql_to_csv(FinanceRecorderCmnClass.TimeRangeCfg time_range_cfg, String csv_backup_foldername);
}
