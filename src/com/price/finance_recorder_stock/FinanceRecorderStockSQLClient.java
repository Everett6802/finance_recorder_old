package com.price.finance_recorder_stock;

import java.util.LinkedList;
import com.price.finance_recorder_base.FinanceRecorderCSVHandler;
import com.price.finance_recorder_base.FinanceRecorderSQLClient;
import com.price.finance_recorder_cmn.FinanceRecorderCmnClass;
import com.price.finance_recorder_cmn.FinanceRecorderCmnDef;


public class FinanceRecorderStockSQLClient extends FinanceRecorderSQLClient
{
	public static String get_database_name(int company_group_number)
	{
		if (!FinanceRecorderCmnDef.IS_FINANCE_STOCK_MODE)
		{
			String errmsg = "It's NOT Stock mode";
			throw new IllegalStateException(errmsg);
		}
		return String.format("%s%02d", FinanceRecorderCmnDef.SQL_STOCK_DATABASE_NAME, company_group_number);
	}

	public static String get_table_name(int source_type_index, String company_code_number)
	{
		if (!FinanceRecorderCmnDef.IS_FINANCE_STOCK_MODE)
		{
			String errmsg = "It's NOT Stock mode";
			throw new IllegalStateException(errmsg);
		}
		String table_name = String.format("%s%s", company_code_number, FinanceRecorderCmnDef.FINANCE_DATA_NAME_LIST[source_type_index]);
		return table_name;
	}

	FinanceRecorderStockSQLClient()
	{
		if (!FinanceRecorderCmnDef.IS_FINANCE_STOCK_MODE)
		{
			String errmsg = "It's NOT STOCK mode";
			throw new IllegalStateException(errmsg);
		}
	}

	short try_connect_mysql(
			int company_group_number,
			FinanceRecorderCmnDef.NotExistIngoreType database_not_exist_ignore_type,
			FinanceRecorderCmnDef.CreateThreadType database_create_thread_type
		)
	{
		String database_name = get_database_name(company_group_number);
		return try_connect_mysql(database_name, database_not_exist_ignore_type, database_create_thread_type);
	}

	short delete_database(int company_group_number)
	{
		String database_name = get_database_name(company_group_number);
		return delete_database(database_name);
	}

	short create_table(int source_type_index, String company_code_number)
	{
		String table_name = get_table_name(source_type_index, company_code_number);
		return create_table(table_name, FinanceRecorderCmnDef.FINANCE_DATA_SQL_FIELD_LIST[source_type_index]);
	}

	short delete_table(int source_type_index, String company_code_number)
	{
		String table_name = get_table_name(source_type_index, company_code_number);
		return delete_table(table_name);
	}

	short check_table_exist(int source_type_index, String company_code_number)
	{
		String table_name = get_table_name(source_type_index, company_code_number);
		return check_table_exist(table_name);
	}

	short insert_data(int source_type_index, String company_code_number, final FinanceRecorderCSVHandler csv_reader)
	{
		String table_name = get_table_name(source_type_index, company_code_number);
		if (FinanceRecorderCmnDef.check_statement_source_type_index_in_range(source_type_index))
			set_csv_time_unit(FinanceRecorderCmnDef.FinanceTimeUnit.FinanceTime_Quarter);
		return insert_data(table_name, csv_reader);
	}

	short select_data(int source_type_index, String company_code_number, LinkedList<Integer> field_index_list, FinanceRecorderCmnClass.FinanceTimeRange finance_time_range, FinanceRecorderCmnClass.ResultSet result_set)
	{
		String table_name = get_table_name(source_type_index, company_code_number);
		return select_data(table_name, source_type_index, field_index_list, finance_time_range, result_set);
	}
	short select_data(int source_type_index, String company_code_number, FinanceRecorderCmnClass.FinanceTimeRange finance_time_range, FinanceRecorderCmnClass.ResultSet result_set)
	{
		String table_name = get_table_name(source_type_index, company_code_number);
		return select_data(table_name, source_type_index, finance_time_range, result_set);
	}
}
