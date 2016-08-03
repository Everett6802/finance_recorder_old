package com.price.finance_recorder;

import java.util.*;
import com.price.finance_recorder_cmn.FinanceRecorderCmnBase;
import com.price.finance_recorder_cmn.FinanceRecorderCmnClass;
import com.price.finance_recorder_cmn.FinanceRecorderCmnDef;


public class FinanceRecorderMarketDataHandler extends FinanceRecorderCmnBase implements FinanceRecorderDataHandlerInf
{
	private static FinanceRecorderCmnClass.QuerySet whole_field_query_set = null;
	private static String get_csv_filepath(int source_type_index)
	{
		return String.format("%s/%s", FinanceRecorderCmnDef.CSV_FOLDERPATH, FinanceRecorderCmnDef.FINANCE_DATA_NAME_LIST[source_type_index]);
	}

//	private static String get_sql_database_name()
//	{
//		return FinanceRecorderCmnDef.SQL_MARKET_DATABASE_NAME;
//	}
//
//	private static String get_sql_table_name(int source_type_index)
//	{
//		return FinanceRecorderCmnDef.FINANCE_DATA_NAME_LIST[source_type_index];
//	}

	public static FinanceRecorderDataHandlerInf get_data_handler(final LinkedList<Integer> source_type_list)
	{
		if (!FinanceRecorderCmnDef.IS_FINANCE_MARKET_MODE)
		{
			String errmsg = "It's NOT Market mode";
			throw new IllegalStateException(errmsg);
		}
		for (int source_type_index : source_type_list)
		{
			if (!FinanceRecorderCmnDef.FinanceSourceType.is_market_source_type(source_type_index))
			{
				String errmsg = String.format("The data source[%d] is NOT Market source type", source_type_index);
				throw new IllegalArgumentException(errmsg);
			}
		}
		FinanceRecorderMarketDataHandler data_handler_obj = new FinanceRecorderMarketDataHandler();
		data_handler_obj.source_type_list = source_type_list;
		return data_handler_obj;
	}
	public static FinanceRecorderDataHandlerInf get_data_handler_whole()
	{
		LinkedList<Integer> source_type_list = new LinkedList<Integer>();
		int start_index = FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_MarketStart.value();
		int end_index = FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_MarketEnd.value();
		for (int source_type_index = start_index ; source_type_index < end_index ; source_type_index++)
			source_type_list.add(source_type_index);
		return get_data_handler(source_type_list);
	}

	private LinkedList<Integer> source_type_list = null;

	private FinanceRecorderMarketDataHandler()
	{
		if (whole_field_query_set == null)
		{
			whole_field_query_set = new FinanceRecorderCmnClass.QuerySet();
			int source_type_start_index = FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_MarketStart.ordinal();
			int source_type_end_index = FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_MarketEnd.ordinal();
			for (int source_type_index = source_type_start_index ; source_type_index < source_type_end_index ; source_type_index++)
				whole_field_query_set.add_query(source_type_index);
			whole_field_query_set.add_query_done();
		}
	}

	public short read_from_csv(FinanceRecorderCSVHandlerMap csv_data_map)
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		for (int source_type_index : source_type_list)
		{
			FinanceRecorderCSVHandler csv_reader = FinanceRecorderCSVHandler.get_csv_reader(FinanceRecorderMarketDataHandler.get_csv_filepath(source_type_index));
			ret = csv_reader.read();
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				return ret;
			csv_data_map.put(FinanceRecorderCmnDef.get_source_key(source_type_index), csv_reader);
		}
		return ret;
	}

	public short write_into_sql(final FinanceRecorderCSVHandlerMap csv_data_map)
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
// Establish the connection to the MySQL and create the database if not exist
		FinanceRecorderMarketSQLClient sql_client = new FinanceRecorderMarketSQLClient();
		ret = sql_client.try_connect_mysql(FinanceRecorderCmnDef.SQL_MARKET_DATABASE_NAME, FinanceRecorderCmnDef.DatabaseNotExistIngoreType.DatabaseNotExistIngore_Yes, FinanceRecorderCmnDef.DatabaseCreateThreadType.DatabaseCreateThread_Single);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;
		OUT:
		for (int source_type_index : source_type_list)
		{
// Check data exist
			Integer source_key = FinanceRecorderCmnDef.get_source_key(source_type_index);
			if (!csv_data_map.containsKey(source_key))
			{
				FinanceRecorderCmnDef.format_error("The CSV data of source key[%d] (source_type: %d)", source_key, source_type_index);
				ret = FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
				break OUT;
			}
// Create MySQL table
			ret = sql_client.create_table(source_type_index);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				break OUT;
// Write the data into MySQL database
			FinanceRecorderCSVHandler csv_reader = csv_data_map.get(source_key);
			ret = sql_client.insert_data(source_type_index, csv_reader);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				break OUT;
		}
//		for (Map.Entry<Integer, FinanceRecorderCSVHandler> entry : csv_data_map.entrySet())
//		{
//			Integer source_key = entry.getKey();
//			Integer source_type_index = FinanceRecorderCSVHandlerMap.get_source_type(source_key);
//// Create MySQL table
//			ret = sql_client.create_market_table(source_type_index);
//			if (FinanceRecorderCmnDef.CheckFailure(ret))
//				break OUT;
//// Write the data into MySQL database
//			FinanceRecorderCSVHandler csv_reader = entry.getValue();
//			ret = sql_client.insert_market_data(source_type_index, csv_reader);
//			if (FinanceRecorderCmnDef.CheckFailure(ret))
//				break OUT;
//		}
// Destroy the connection to the MySQL
		sql_client.disconnect_mysql();
		return ret;
	}

	public short transfrom_csv_to_sql()
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
// Establish the connection to the MySQL and create the database if not exist
		FinanceRecorderMarketSQLClient sql_client = new FinanceRecorderMarketSQLClient();
		ret = sql_client.try_connect_mysql(FinanceRecorderCmnDef.SQL_MARKET_DATABASE_NAME, FinanceRecorderCmnDef.DatabaseNotExistIngoreType.DatabaseNotExistIngore_Yes, FinanceRecorderCmnDef.DatabaseCreateThreadType.DatabaseCreateThread_Single);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;
		OUT:
// For each source type
		for (int source_type_index : source_type_list)
		{
// Read data from CSV
			FinanceRecorderCSVHandler csv_reader = FinanceRecorderCSVHandler.get_csv_reader(FinanceRecorderMarketDataHandler.get_csv_filepath(source_type_index));
			ret = csv_reader.read();
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				return ret;
// Create MySQL table
			ret = sql_client.create_table(source_type_index);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				break OUT;
// Write the data into MySQL database
			ret = sql_client.insert_data(source_type_index, csv_reader);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				break OUT;
		}
// Destroy the connection to the MySQL
		sql_client.disconnect_mysql();
		return ret;
	}

	public short read_from_sql(FinanceRecorderCmnClass.QuerySet query_set, FinanceRecorderCmnClass.TimeRangeCfg time_range_cfg, FinanceRecorderCmnClass.ResultSetMap result_set_map)
	{
// CAUTION: The data set in the reslt_set variable should be added before calling this function 
// Set the mapping table of reading the specific CSV files and writing correct SQL database
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		FinanceRecorderCmnDef.ResultSetDataUnit data_unit = result_set_map.get_data_unit();
// Establish the connection to the MySQL
		FinanceRecorderMarketSQLClient sql_client = new FinanceRecorderMarketSQLClient();
		ret = sql_client.try_connect_mysql(FinanceRecorderCmnDef.DatabaseNotExistIngoreType.DatabaseNotExistIngore_No, FinanceRecorderCmnDef.DatabaseCreateThreadType.DatabaseCreateThread_Single);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;
		FinanceRecorderCmnClass.ResultSet result_set = null;
OUT:
		switch (data_unit)
		{
		case ResultSetDataUnit_NoSourceType:
		{
			result_set = new FinanceRecorderCmnClass.ResultSet();
// Add query set
			for (int source_type_index : source_type_list)
			{
				ret = result_set.add_set(source_type_index, query_set.get_index(source_type_index));
				if (FinanceRecorderCmnDef.CheckFailure(ret))
					break OUT;
			}
// Query data from each source type
			for (int source_type_index : source_type_list)
			{
				ret = sql_client.select_data(source_type_index, time_range_cfg, result_set);
				if (FinanceRecorderCmnDef.CheckFailure(ret))
					break OUT;
			}
// Keep track of the data in the designated data structure
			ret = result_set_map.register_result_set(FinanceRecorderCmnDef.NO_SOURCE_TYPE_MARKET_SOURCE_KEY_VALUE, result_set);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				break OUT;
		}
		break;
		case ResultSetDataUnit_SourceType:
		{
			for (int source_type_index : source_type_list)
			{
				result_set = new FinanceRecorderCmnClass.ResultSet();
// Add query set
				ret = result_set.add_set(source_type_index, query_set.get_index(source_type_index));
				if (FinanceRecorderCmnDef.CheckFailure(ret))
					break OUT;
// Query data from each source type
				ret = sql_client.select_data(source_type_index, time_range_cfg, result_set);
				if (FinanceRecorderCmnDef.CheckFailure(ret))
					break OUT;
// Keep track of the data in the designated data structure
				ret = result_set_map.register_result_set(FinanceRecorderCmnDef.get_source_key(source_type_index), result_set);
				if (FinanceRecorderCmnDef.CheckFailure(ret))
					break OUT;
			}
		}
		break;
		default:
		{
			String errmsg = String.format("Unsupported data unit: %d", data_unit.ordinal());
			throw new IllegalArgumentException(errmsg);
		}
		}
// Destroy the connection to the MySQL
		sql_client.disconnect_mysql();
		return ret;
	}
	public short read_from_sql(FinanceRecorderCmnClass.TimeRangeCfg time_range_cfg, FinanceRecorderCmnClass.ResultSetMap result_set_map)
	{
		return read_from_sql(whole_field_query_set, time_range_cfg, result_set_map);
	}
}
