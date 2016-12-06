package com.price.finance_recorder_base;

import com.price.finance_recorder_cmn.FinanceRecorderCmnClass;
import com.price.finance_recorder_cmn.FinanceRecorderCmnDef;


public interface FinanceRecorderDataHandlerInf 
{
	void set_finance_root_folerpath(String foldername);
	void set_finance_root_backup_folerpath(String backup_foldername);
// CSV -> SQL related function
// For transforming data from CSV into SQL, I assumed the time range of each CSV data source can be different
	short read_from_csv(FinanceRecorderCSVHandlerMap csv_data_map);
	short write_into_sql(final FinanceRecorderCSVHandlerMap csv_data_map);
	short transfrom_csv_to_sql();
// SQL -> CSV related function
//	For transforming data from SQL into CSV, I assumed the time range of each SQL data source should be identical
	short read_from_sql(FinanceRecorderCmnClass.QuerySet query_set, FinanceRecorderCmnClass.FinanceTimeRange finance_time_range, FinanceRecorderCmnClass.ResultSetMap result_set_map);
	short read_from_sql(FinanceRecorderCmnClass.FinanceTimeRange finance_time_range, FinanceRecorderCmnClass.ResultSetMap result_set_map);
	short write_into_csv(FinanceRecorderCmnClass.ResultSetMap result_set_map);
	short transfrom_sql_to_csv(FinanceRecorderCmnClass.QuerySet query_set, FinanceRecorderCmnClass.FinanceTimeRange finance_time_range);
	short transfrom_whole_sql_to_csv(FinanceRecorderCmnClass.FinanceTimeRange finance_time_range);
// Delete SQL
	short delete_sql_by_source_type();
	short delete_sql_by_company(); // Only useful in stock mode
	short delete_sql_by_source_type_and_company(); // Only useful in stock mode
// Cleanup SQL
	short cleanup_sql();
}
