package com.price.finance_recorder_base;

import java.util.List;
import com.price.finance_recorder_cmn.FinanceRecorderCmnClass;


public interface FinanceRecorderMgrInf 
{
	short set_source_type_time_range_from_file(String filename);
	short set_source_type_time_range(List<FinanceRecorderCmnClass.SourceTypeTimeRange> in_source_type_time_range_list);
	short set_source_type(List<Integer> in_source_type_index_list);
	short set_company_from_file(String filename);
	short set_company(List<String> company_number_list);
	short initialize();
	short transfrom_csv_to_sql();
}
