package com.price.finance_recorder_stock;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import com.price.finance_recorder_base.*;
//import com.price.finance_recorder_cmn.FinanceRecorderCmnClass;
import com.price.finance_recorder_cmn.FinanceRecorderCmnDef;
import com.price.finance_recorder_stock.FinanceRecorderDatabaseTimeRange;
import com.price.finance_recorder_stock.FinanceRecorderStockDataHandler;


public class FinanceRecorderStockMgr extends FinanceRecorderMgrBase
{
	private FinanceRecorderCompanyGroupSet company_group_set = null;
	private FinanceRecorderDatabaseTimeRange database_time_range = null;

	protected FinanceRecorderDataHandlerInf get_data_handler(){return FinanceRecorderStockDataHandler.get_data_handler(source_type_index_list, company_group_set);}

	private short transform_company_list_to_group_set(List<String> company_number_list)
	{
/*
        The argument type:
        Company code number: 2347
        Company code number range: 2100-2200
        Company group number: [Gg]12
        Company code number/number range hybrid: 2347,2100-2200,2362,g2,1500-1510
*/
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		company_group_set = new FinanceRecorderCompanyGroupSet();
		for (String company_number : company_number_list)
		{
			Matcher matcher = FinanceRecorderCmnDef.get_regex_matcher("([\\d]{4})-([\\d]{4})", company_number);
			if (matcher == null)
			{
// Check if data is company code/group number
				Matcher matcher1 = FinanceRecorderCmnDef.get_regex_matcher("[Gg]([\\d]{1,})", company_number);
				if (matcher1 == null)
				{
// Company code number
					if (FinanceRecorderCmnDef.get_regex_matcher("([\\d]{4})", company_number) == null)
						throw new IllegalStateException(String.format("Unknown company number format: %s", company_number));
					ret = company_group_set.add_company(company_number);
					if (FinanceRecorderCmnDef.CheckFailure(ret))
						return ret;
				}
				else
				{
// Company group number
					int company_group_number = Integer.valueOf(matcher1.group(1));
					ret = company_group_set.add_company_group(company_group_number);
					if (FinanceRecorderCmnDef.CheckFailure(ret))
						return ret;
				}
			}
			else
			{
// Company code number Range
				int start_company_number = Integer.valueOf(matcher.group(1));
				int end_company_number = Integer.valueOf(matcher.group(2));
				LinkedList<String> number_list = new LinkedList<String>();
				for (int number = start_company_number ; number <= end_company_number ; number++)
					number_list.add(String.format("%04d", number));
				ret = company_group_set.add_company_list(number_list);
				if (FinanceRecorderCmnDef.CheckFailure(ret))
					return ret;
			}
		}
		return ret;
	}

	public short set_company_from_file(String filename)
	{
		if (setup_param_done)
		{
			FinanceRecorderCmnDef.error("The manager class has been initialized....");
			return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
		}

		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		LinkedList<String> config_line_list = new LinkedList<String>();
// Read the content from the config file
		ret = FinanceRecorderCmnDef.get_config_file_lines(filename, config_line_list);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;

		return transform_company_list_to_group_set(config_line_list);
	}

	public short set_company(List<String> company_number_list)
	{
		if (setup_param_done)
		{
			FinanceRecorderCmnDef.error("The manager class has been initialized....");
			return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
		}

		return transform_company_list_to_group_set(company_number_list);
	}

	public short initialize()
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		ret = super.initialize();
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;
// Initialize the database time range
		database_time_range = FinanceRecorderDatabaseTimeRange.get_instance();

		return FinanceRecorderCmnDef.RET_SUCCESS;
	}
}
