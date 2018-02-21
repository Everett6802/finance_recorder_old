package com.price.finance_recorder_lib;

import java.util.LinkedList;


public class StockSQLClient extends SQLClient
{
	private static String get_database_name(int company_group_number)
	{
		if (!CmnDef.IS_FINANCE_STOCK_MODE)
		{
			String errmsg = "It's NOT Stock mode";
			throw new IllegalStateException(errmsg);
		}
		return String.format("%s%02d", CmnDef.SQL_STOCK_DATABASE_NAME, company_group_number);
	}

	private static String get_table_name(int method_index, String company_code_number)
	{
		if (!CmnDef.IS_FINANCE_STOCK_MODE)
		{
			String errmsg = "It's NOT Stock mode";
			throw new IllegalStateException(errmsg);
		}
		String table_name = String.format("%s%s", company_code_number, CmnDef.FINANCE_DATA_NAME_LIST[method_index]);
		return table_name;
	}

	StockSQLClient()
	{
		if (!CmnDef.IS_FINANCE_STOCK_MODE)
		{
			String errmsg = "It's NOT STOCK mode";
			throw new IllegalStateException(errmsg);
		}
	}

	short try_connect_mysql(int company_group_number)
	{
		String database_name = get_database_name(company_group_number);
		return try_connect_mysql(database_name);
	}

	short create_database(int company_group_number)
	{
		String database_name = get_database_name(company_group_number);
		return create_database(database_name);
	}

	short delete_database(int company_group_number)
	{
		String database_name = get_database_name(company_group_number);
		return delete_database(database_name);
	}

	short create_table(int method_index, String company_code_number)
	{
		String table_name = get_table_name(method_index, company_code_number);
		return create_table(table_name, CmnDef.FINANCE_DATA_SQL_FIELD_LIST[method_index]);
	}

	short delete_table(int method_index, String company_code_number)
	{
		String table_name = get_table_name(method_index, company_code_number);
		return delete_table(table_name);
	}

	short check_table_exist(int method_index, String company_code_number)
	{
		String table_name = get_table_name(method_index, company_code_number);
		return check_table_exist(table_name);
	}

	short insert_data(int method_index, String company_code_number, final CSVHandler csv_reader)
	{
		String table_name = get_table_name(method_index, company_code_number);
		if (CmnFunc.check_statement_method_index_in_range(method_index))
			set_csv_time_unit(CmnDef.FinanceTimeUnit.FinanceTime_Quarter);
		return insert_data(table_name, csv_reader);
	}

	short select_data(int method_index, String company_code_number, LinkedList<Integer> field_index_list, CmnClass.FinanceTimeRange finance_time_range, CmnClass.ResultSet result_set)
	{
		String table_name = get_table_name(method_index, company_code_number);
		return select_data(table_name, method_index, field_index_list, finance_time_range, result_set);
	}
	short select_data(int method_index, String company_code_number, CmnClass.FinanceTimeRange finance_time_range, CmnClass.ResultSet result_set)
	{
		String table_name = get_table_name(method_index, company_code_number);
		return select_data(table_name, method_index, finance_time_range, result_set);
	}
}
