package com.price.finance_recorder_market;

import java.util.LinkedList;
import com.price.finance_recorder_base.FinanceRecorderCSVHandler;
import com.price.finance_recorder_base.FinanceRecorderSQLClient;
import com.price.finance_recorder_cmn.FinanceRecorderCmnClass;
import com.price.finance_recorder_cmn.FinanceRecorderCmnDef;


public class FinanceRecorderMarketSQLClient extends FinanceRecorderSQLClient
{
	FinanceRecorderMarketSQLClient()
	{
		if (!FinanceRecorderCmnDef.IS_FINANCE_MARKET_MODE)
		{
			String errmsg = "It's NOT Market mode";
			throw new IllegalStateException(errmsg);
		}
	}

	short try_connect_mysql(
			FinanceRecorderCmnDef.NotExistIngoreType database_not_exist_ignore_type,
			FinanceRecorderCmnDef.CreateThreadType database_create_thread_type
		)
	{
		return try_connect_mysql(FinanceRecorderCmnDef.SQL_MARKET_DATABASE_NAME, database_not_exist_ignore_type, database_create_thread_type);
	}

	short delete_database()
	{
		return delete_database(FinanceRecorderCmnDef.SQL_MARKET_DATABASE_NAME);
	}

	short create_table(int source_type_index)
	{
		return create_table(FinanceRecorderCmnDef.FINANCE_DATA_NAME_LIST[source_type_index], FinanceRecorderCmnDef.FINANCE_DATA_SQL_FIELD_LIST[source_type_index]);
	}

	short delete_table(int source_type_index)
	{
		return delete_table(FinanceRecorderCmnDef.FINANCE_DATA_NAME_LIST[source_type_index]);
	}

	short check_table_exist(int source_type_index)
	{
		return check_table_exist(FinanceRecorderCmnDef.FINANCE_DATA_NAME_LIST[source_type_index]);
	}

	short insert_data(int source_type_index, final FinanceRecorderCSVHandler csv_reader)
	{
		return insert_data(FinanceRecorderCmnDef.FINANCE_DATA_NAME_LIST[source_type_index], csv_reader);
	}

	short select_data(int source_type_index, LinkedList<Integer> field_index_list, FinanceRecorderCmnClass.FinanceTimeRange finance_time_range, FinanceRecorderCmnClass.ResultSet result_set)
	{
		return select_data(FinanceRecorderCmnDef.FINANCE_DATA_NAME_LIST[source_type_index], source_type_index, field_index_list, finance_time_range, result_set);
	}
	short select_data(int source_type_index, FinanceRecorderCmnClass.FinanceTimeRange finance_time_range, FinanceRecorderCmnClass.ResultSet result_set)
	{
		return select_data(FinanceRecorderCmnDef.FINANCE_DATA_NAME_LIST[source_type_index], source_type_index, finance_time_range, result_set);
	}
}
