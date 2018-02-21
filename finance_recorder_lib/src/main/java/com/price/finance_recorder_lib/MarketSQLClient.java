package com.price.finance_recorder_lib;

import java.util.LinkedList;


class MarketSQLClient extends SQLClient
{
	MarketSQLClient()
	{
		if (!CmnDef.IS_FINANCE_MARKET_MODE)
		{
			String errmsg = "It's NOT Market mode";
			throw new IllegalStateException(errmsg);
		}
	}

	short try_connect_mysql()
	{
		return try_connect_mysql(CmnDef.SQL_MARKET_DATABASE_NAME);
	}

	short create_database()
	{
		return create_database(CmnDef.SQL_MARKET_DATABASE_NAME);
	}

	short delete_database()
	{
		return delete_database(CmnDef.SQL_MARKET_DATABASE_NAME);
	}

	short create_table(int source_type_index)
	{
		return create_table(CmnDef.FINANCE_DATA_NAME_LIST[source_type_index], CmnDef.FINANCE_DATA_SQL_FIELD_LIST[source_type_index]);
	}

	short delete_table(int source_type_index)
	{
		return delete_table(CmnDef.FINANCE_DATA_NAME_LIST[source_type_index]);
	}

	short check_table_exist(int source_type_index)
	{
		return check_table_exist(CmnDef.FINANCE_DATA_NAME_LIST[source_type_index]);
	}

	short insert_data(int source_type_index, final CSVHandler csv_reader)
	{
		return insert_data(CmnDef.FINANCE_DATA_NAME_LIST[source_type_index], csv_reader);
	}

	short select_data(int source_type_index, LinkedList<Integer> field_index_list, CmnClass.FinanceTimeRange finance_time_range, CmnClass.ResultSet result_set)
	{
		return select_data(CmnDef.FINANCE_DATA_NAME_LIST[source_type_index], source_type_index, field_index_list, finance_time_range, result_set);
	}
	short select_data(int source_type_index, CmnClass.FinanceTimeRange finance_time_range, CmnClass.ResultSet result_set)
	{
		return select_data(CmnDef.FINANCE_DATA_NAME_LIST[source_type_index], source_type_index, finance_time_range, result_set);
	}
}
