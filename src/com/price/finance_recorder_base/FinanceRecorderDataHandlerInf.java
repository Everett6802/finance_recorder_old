package com.price.finance_recorder_base;

import com.price.finance_recorder_cmn.FinanceRecorderCmnClass;


public interface FinanceRecorderDataHandlerInf 
{
	void set_finance_root_folerpath(String foldername);
	void set_finance_root_backup_folerpath(String backup_foldername);
// CSV -> SQL related function
// For transforming data from CSV into SQL, I assumed the time range of each CSV data source can be different
	public short read_from_csv(FinanceRecorderCSVHandlerMap csv_data_map);
	public short write_into_sql(final FinanceRecorderCSVHandlerMap csv_data_map);
	public short transfrom_csv_to_sql();
// SQL -> CSV related function
//	For transforming data from SQL into CSV, I assumed the time range of each SQL data source should be identical
	public short read_from_sql(FinanceRecorderCmnClass.QuerySet query_set, FinanceRecorderCmnClass.FinanceTimeRange finance_time_range, FinanceRecorderCmnClass.ResultSetMap result_set_map);
	public short read_from_sql(FinanceRecorderCmnClass.FinanceTimeRange finance_time_range, FinanceRecorderCmnClass.ResultSetMap result_set_map);
	public short write_into_csv(FinanceRecorderCmnClass.ResultSetMap result_set_map);
	public short transfrom_sql_to_csv(FinanceRecorderCmnClass.QuerySet query_set, FinanceRecorderCmnClass.FinanceTimeRange finance_time_range);
	public short transfrom_whole_sql_to_csv(FinanceRecorderCmnClass.FinanceTimeRange finance_time_range);
	
}
