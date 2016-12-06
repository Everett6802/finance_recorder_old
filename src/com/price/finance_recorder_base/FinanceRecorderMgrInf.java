package com.price.finance_recorder_base;

import java.util.List;
import com.price.finance_recorder_cmn.FinanceRecorderCmnDef;
import com.price.finance_recorder_cmn.FinanceRecorderCmnClass;


public interface FinanceRecorderMgrInf 
{
	void set_finance_folderpath(String finance_folderpath);
	void set_finance_backup_folderpath(String finance_backup_folderpath);
	void set_delete_sql_accuracy(FinanceRecorderCmnDef.DeleteSQLAccurancyType accurancy_type); // Only work in stock mode
	short set_source_type_from_file(String filename);
	short set_source_type(List<Integer> in_source_type_index_list);
	short set_company_from_file(String filename);
	short set_company(List<String> company_word_list);
	short initialize();
	short transfrom_csv_to_sql();
	short transfrom_sql_to_csv(FinanceRecorderCmnClass.QuerySet query_set, FinanceRecorderCmnClass.FinanceTimeRange finance_time_range);
	short transfrom_sql_to_csv(FinanceRecorderCmnClass.FinanceTimeRange finance_time_range);
	short delete_sql();
	short cleanup_sql();
}
