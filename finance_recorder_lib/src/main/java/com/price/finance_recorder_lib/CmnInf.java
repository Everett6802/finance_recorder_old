package com.price.finance_recorder_lib;

import java.util.ArrayList;
import java.util.List;


class CmnInf
{
	public interface FinanceObserverInf
	{
		public short notify(short type);
	}
	
	interface DataHandlerInf
	{
		void set_current_csv_working_folerpath(String csv_working_folerpath);
		
		void set_operation_non_stop(boolean enable);
		
		boolean is_operation_non_stop();
		
		void set_csv_remote_source(String server_ip);
		
		boolean is_csv_remote_source();
		
		// CSV -> SQL related function
		// For transforming data from CSV into SQL, I assumed the time range of
		// each CSV data source can be different
		short read_from_csv(CSVHandlerMap csv_data_map);
		
		short write_into_sql(final CSVHandlerMap csv_data_map);
		
		short transfrom_csv_to_sql();
		
		// SQL -> CSV related function
		// For transforming data from SQL into CSV, I assumed the time range of
		// each SQL data source should be identical
		short read_from_sql(CmnClass.QuerySet query_set, CmnClass.FinanceTimeRange finance_time_range,
				CmnClass.ResultSetMap result_set_map);
		
		short read_from_sql(CmnClass.FinanceTimeRange finance_time_range, CmnClass.ResultSetMap result_set_map);
		
		short write_into_csv(CmnClass.ResultSetMap result_set_map);
		
		short transfrom_sql_to_csv(CmnClass.QuerySet query_set, CmnClass.FinanceTimeRange finance_time_range);
		
		short transfrom_whole_sql_to_csv(CmnClass.FinanceTimeRange finance_time_range);
		
		// Delete SQL
		short delete_sql_by_method();
		
		short delete_sql_by_company(); // Only useful in stock mode
		
		short delete_sql_by_method_and_company(); // Only useful in stock mode
		// Cleanup SQL
		
		short cleanup_sql();
		
		// Check SQL exist
		short check_sql_exist(ArrayList<String> not_exist_list);
	};
	
	public interface MgrInf
	{
		void set_finance_folderpath(String finance_folderpath);
		
		void set_finance_backup_folderpath(String finance_folderpath);
		
		// void set_finance_backup_foldername(String finance_foldername);
		void set_finance_restore_folderpath(String finance_folderpath);
		
		// void set_finance_restore_foldername(String finance_foldername);
		String get_finance_folderpath();
		
		String get_finance_backup_folderpath();
		
		// String get_finance_backup_foldername();
		String get_finance_restore_folderpath();
		
		// String get_finance_restore_foldername();
		void switch_current_csv_working_folerpath(CmnDef.CSVWorkingFolderType working_folder_type);
		
		void set_operation_non_stop(boolean enable);
		
		boolean is_operation_non_stop();
		
		short initialize();
		
		void set_delete_sql_accuracy(CmnDef.DeleteSQLAccurancyType accurancy_type); // Only
																					// work
																					// in
																					// stock
																					// mode
		
		short get_backup_foldername_list(List<String> sorted_sub_foldername_list);
		
		short get_restore_foldername_list(List<String> sorted_sub_foldername_list);
		
		// short reset_data_source_rule();
		// short set_data_source_rule_done();
		// short set_method_from_file(String filename);
		short set_method(List<Integer> in_method_index_list);
		
		// short set_company_from_file(String filename);
		short set_company(List<String> company_word_list);
		
		short transfrom_csv_to_sql();
		
		short transfrom_csv_to_sql_multithread(int sub_company_group_set_amount);
		
		short transfrom_sql_to_csv(CmnClass.QuerySet query_set, CmnClass.FinanceTimeRange finance_time_range);
		
		short transfrom_sql_to_csv(CmnClass.FinanceTimeRange finance_time_range);
		
		short delete_sql();
		
		short cleanup_sql();
		
		short check_sql_exist(ArrayList<String> not_exist_list);
	};
}
