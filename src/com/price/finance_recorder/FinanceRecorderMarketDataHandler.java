package com.price.finance_recorder;

//import java.io.File;
//import java.io.File;
//import java.sql.*;
//import java.text.*;
import java.util.*;
import com.price.finance_recorder_cmn.FinanceRecorderCmnBase;
import com.price.finance_recorder_cmn.FinanceRecorderCmnDef;
//import com.price.finance_recorder_cmn.FinanceRecorderCmnClass.*;


public class FinanceRecorderMarketDataHandler extends FinanceRecorderCmnBase implements FinanceRecorderDataHandlerInf
{
	private static String get_csv_filepath(int source_type_index)
	{
		return String.format("%s/%s", FinanceRecorderCmnDef.CSV_FOLDERPATH, FinanceRecorderCmnDef.FINANCE_DATA_NAME_LIST[source_type_index]);
	}

	private static String get_sql_database_name()
	{
		return FinanceRecorderCmnDef.SQL_MARKET_DATABASE_NAME;
	}

	private static String get_sql_table_name(int source_type_index)
	{
		return FinanceRecorderCmnDef.FINANCE_DATA_NAME_LIST[source_type_index];
	}

	public static FinanceRecorderDataHandlerInf get_data_handler(final ArrayList<Integer> source_type_list)
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
		ArrayList<Integer> source_type_list = new ArrayList<Integer>();
		int start_index = FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_MarketStart.value();
		int end_index = FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_MarketEnd.value();
		for (int source_type_index = start_index ; source_type_index < end_index ; source_type_index++)
			source_type_list.add(source_type_index);
		return get_data_handler(source_type_list);
	}

	private ArrayList<Integer> source_type_list = null;

	private FinanceRecorderMarketDataHandler(){}

	public short read_from_csv(FinanceRecorderCSVHandlerMap csv_data_map)
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		for (int source_type_index : source_type_list)
		{
			FinanceRecorderCSVHandler csv_reader = FinanceRecorderCSVHandler.get_csv_reader(FinanceRecorderMarketDataHandler.get_csv_filepath(source_type_index));
			ret = csv_reader.read();
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				return ret;
			csv_data_map.put(FinanceRecorderCSVHandlerMap.get_source_key(source_type_index), csv_reader);
		}
		return ret;
	}

	public short write_into_sql(final FinanceRecorderCSVHandlerMap csv_data_map)
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
// Establish the connection to the MySQL and create the database if not exist
		FinanceRecorderSQLClient sql_client = new FinanceRecorderSQLClient();
		ret = sql_client.try_connect_mysql(FinanceRecorderCmnDef.SQL_MARKET_DATABASE_NAME, FinanceRecorderCmnDef.DatabaseNotExistIngoreType.DatabaseNotExistIngore_Yes, FinanceRecorderCmnDef.DatabaseCreateThreadType.DatabaseCreateThread_Single);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;
		OUT:
		for (int source_type_index : source_type_list)
		{
// Check data exist
			Integer source_key = FinanceRecorderCSVHandlerMap.get_source_key(source_type_index);
			if (!csv_data_map.containsKey(source_key))
			{
				FinanceRecorderCmnDef.format_error("The CSV data of source key[%d] (source_type: %d)", source_key, source_type_index);
				ret = FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
				break OUT;
			}
// Create MySQL table
			ret = sql_client.create_market_table(source_type_index);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				break OUT;
// Write the data into MySQL database
			FinanceRecorderCSVHandler csv_reader = csv_data_map.get(source_key);
			ret = sql_client.insert_market_data(source_type_index, csv_reader);
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
		FinanceRecorderSQLClient sql_client = new FinanceRecorderSQLClient();
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
			ret = sql_client.create_market_table(source_type_index);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				break OUT;
// Write the data into MySQL database
			ret = sql_client.insert_market_data(source_type_index, csv_reader);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				break OUT;
		}
// Destroy the connection to the MySQL
		sql_client.disconnect_mysql();
		return ret;
	}
}
