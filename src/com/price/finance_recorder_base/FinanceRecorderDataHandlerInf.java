package com.price.finance_recorder_base;

import java.util.ArrayList;
import com.price.finance_recorder_cmn.FinanceRecorderCmnClass;


public interface FinanceRecorderDataHandlerInf 
{
	void set_current_csv_working_folerpath(String csv_working_folerpath);
	void enable_operation_continue(boolean enable);
	boolean is_operation_continue();
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
// Check SQL exist
	short check_sql_exist(ArrayList<String> not_exist_list);
}
