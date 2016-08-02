package com.price.finance_recorder;

import java.util.LinkedList;
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
			FinanceRecorderCmnDef.DatabaseNotExistIngoreType database_not_exist_ignore_type,
			FinanceRecorderCmnDef.DatabaseCreateThreadType database_create_thread_type
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

	short insert_data(int source_type_index, final FinanceRecorderCSVHandler csv_reader)
	{
		return insert_data(FinanceRecorderCmnDef.FINANCE_DATA_NAME_LIST[source_type_index], csv_reader);
	}

	short select_data(int source_type_index, LinkedList<Integer> field_index_list, FinanceRecorderCmnClass.TimeRangeCfg time_range_cfg, FinanceRecorderCmnClass.ResultSet result_set)
	{
		return select_data(FinanceRecorderCmnDef.FINANCE_DATA_NAME_LIST[source_type_index], source_type_index, field_index_list, time_range_cfg, result_set);
	}
}
