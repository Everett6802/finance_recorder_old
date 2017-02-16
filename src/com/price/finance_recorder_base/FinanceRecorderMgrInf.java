package com.price.finance_recorder_base;

import java.util.ArrayList;
import java.util.List;
import com.price.finance_recorder_cmn.FinanceRecorderCmnDef;
import com.price.finance_recorder_cmn.FinanceRecorderCmnClass;


public interface FinanceRecorderMgrInf 
{
	void set_finance_folderpath(String finance_folderpath);
	void set_finance_backup_folderpath(String finance_backup_folderpath);
	void set_finance_restore_folderpath(String finance_restore_folderpath);
	String get_finance_folderpath();
	String get_finance_backup_folderpath();
	String get_finance_restore_folderpath();
	void switch_current_csv_working_folerpath(FinanceRecorderCmnDef.CSVWorkingFolderType working_folder_type);

	void set_delete_sql_accuracy(FinanceRecorderCmnDef.DeleteSQLAccurancyType accurancy_type); // Only work in stock mode
	short get_backup_foldername_list(List<String> sorted_sub_foldername_list);
	short get_restore_foldername_list(List<String> sorted_sub_foldername_list);
	short set_source_type_from_file(String filename);
	short set_source_type(List<Integer> in_source_type_index_list);
	short set_company_from_file(String filename);
	short set_company(List<String> company_word_list);
	short initialize();
	short transfrom_csv_to_sql(boolean stop_when_csv_not_foud);
	short transfrom_csv_to_sql_multithread(int sub_company_group_set_amount, boolean stop_when_csv_not_foud);
	short transfrom_sql_to_csv(FinanceRecorderCmnClass.QuerySet query_set, FinanceRecorderCmnClass.FinanceTimeRange finance_time_range, boolean stop_when_sql_not_foud);
	short transfrom_sql_to_csv(FinanceRecorderCmnClass.FinanceTimeRange finance_time_range, boolean stop_when_sql_not_foud);
	short delete_sql();
	short cleanup_sql();
	short check_sql_exist(ArrayList<String> not_exist_list);
}
