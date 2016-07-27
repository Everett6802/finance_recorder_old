package com.price.finance_recorder;

//import java.io.File;
//import java.sql.*;
//import java.text.*;
import java.util.*;

import com.price.finance_recorder_cmn.FinanceRecorderCmnBase;
import com.price.finance_recorder_cmn.FinanceRecorderCmnClassCompanyProfile;
import com.price.finance_recorder_cmn.FinanceRecorderCmnDef;
import com.price.finance_recorder_cmn.FinanceRecorderCmnClass.*;


public class FinanceRecorderStockDataHandler extends FinanceRecorderCmnBase implements FinanceRecorderDataHandlerInf
{
	private static FinanceRecorderCmnClassCompanyProfile company_profile = FinanceRecorderCmnClassCompanyProfile.get_instance();
	private static String get_csv_filepath(int source_type_index, int company_group_number, String company_code_number)
	{
		return String.format("%s%02d/%s%s", FinanceRecorderCmnDef.CSV_FOLDERPATH, company_group_number, company_code_number, FinanceRecorderCmnDef.FINANCE_DATA_NAME_LIST[source_type_index]);
	}

	private static String get_sql_database_name(int company_group_number)
	{
		return String.format("%s%02d", FinanceRecorderCmnDef.SQL_STOCK_DATABASE_NAME, company_group_number);
	}

	private static String get_sql_table_name(int source_type_index, String company_code_number)
	{
		return String.format("%s%s", company_code_number, FinanceRecorderCmnDef.FINANCE_DATA_NAME_LIST[source_type_index]);
	}

	public static FinanceRecorderDataHandlerInf get_data_handler(final ArrayList<Integer> source_type_list, final CompanyGroupSet company_group_set)
	{
		if (!FinanceRecorderCmnDef.IS_FINANCE_STOCK_MODE)
		{
			String errmsg = "It's NOT Stock mode";
			throw new IllegalStateException(errmsg);
		}
		for (int source_type_index : source_type_list)
		{
			if (!FinanceRecorderCmnDef.FinanceSourceType.is_stock_source_type(source_type_index))
			{
				String errmsg = String.format("The data source[%d] is NOT Stock source type", source_type_index);
				throw new IllegalArgumentException(errmsg);
			}
		}
		FinanceRecorderStockDataHandler data_handler_obj = new FinanceRecorderStockDataHandler();
		data_handler_obj.source_type_list = source_type_list;
		data_handler_obj.company_group_set = company_group_set;
		return data_handler_obj;
	}
	public static FinanceRecorderDataHandlerInf get_data_handler_whole()
	{
		ArrayList<Integer> source_type_list = new ArrayList<Integer>();
		int start_index = FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_StockStart.value();
		int end_index = FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_StockEnd.value();
		for (int source_type_index = start_index ; source_type_index < end_index ; source_type_index++)
			source_type_list.add(source_type_index);
		CompanyGroupSet company_group_set = CompanyGroupSet.get_whole_company_group_set();
		return get_data_handler(source_type_list, company_group_set);
	}

	private ArrayList<Integer> source_type_list = null;
	private CompanyGroupSet company_group_set = null;
	private FinanceRecorderCmnDef.DatabaseCreateThreadType database_create_thread_type = FinanceRecorderCmnDef.DatabaseCreateThreadType.DatabaseCreateThread_Single;

	private FinanceRecorderStockDataHandler(){}

	public short read_from_csv(FinanceRecorderCSVHandlerMap csv_data_map)
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		for (Map.Entry<Integer, ArrayList<String>> company_code_entry : company_group_set)
		{
			int company_group_number = company_code_entry.getKey();
			for(String company_code_number : company_code_entry.getValue())
			{
				for (int source_type_index : source_type_list)
				{
					FinanceRecorderCSVHandler csv_reader = FinanceRecorderCSVHandler.get_csv_reader(FinanceRecorderStockDataHandler.get_csv_filepath(source_type_index, company_group_number, company_code_number));
					ret = csv_reader.read();
					if (FinanceRecorderCmnDef.CheckFailure(ret))
						return ret;
					csv_data_map.put(FinanceRecorderCSVHandlerMap.get_source_key(source_type_index, company_code_number), csv_reader);
				}
			}
		}
		return ret;
	}

	public short write_into_sql(final FinanceRecorderCSVHandlerMap csv_data_map)
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		OUT:
		for (Map.Entry<Integer, ArrayList<String>> company_code_entry : company_group_set)
		{
			int company_group_number = company_code_entry.getKey();
// Establish the connection to the MySQL and create the database if not exist
			FinanceRecorderSQLClient sql_client = new FinanceRecorderSQLClient();
			String database_name = String.format("%s%02d", FinanceRecorderCmnDef.SQL_STOCK_DATABASE_NAME, company_group_number);
			ret = sql_client.try_connect_mysql(database_name, FinanceRecorderCmnDef.DatabaseNotExistIngoreType.DatabaseNotExistIngore_Yes, database_create_thread_type);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				break OUT;
			for(String company_code_number : company_code_entry.getValue())
			{
				for (int source_type_index : source_type_list)
				{
// Check data exist
					Integer source_key = FinanceRecorderCSVHandlerMap.get_source_key(source_type_index, company_code_number);
					if (!csv_data_map.containsKey(source_key))
					{
						FinanceRecorderCmnDef.format_error("The CSV data of source key[%d] (source_type: %d, company_code_number: %s)", source_key, source_type_index, company_code_number);
						ret = FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
						break OUT;
					}
// Create MySQL table
					ret = sql_client.create_stock_table(source_type_index, company_code_number);
					if (FinanceRecorderCmnDef.CheckFailure(ret))
						break OUT;
// Write the data into MySQL database
					FinanceRecorderCSVHandler csv_reader = csv_data_map.get(source_key);
					ret = sql_client.insert_stock_data(source_type_index, company_code_number, csv_reader);
					if (FinanceRecorderCmnDef.CheckFailure(ret))
						break OUT;
				}
			}
// Destroy the connection to the MySQL
			sql_client.disconnect_mysql();
		}
//// Categorized into group
//		TreeMap<Integer, ArrayList<Integer>> company_group_source_key_map = new TreeMap<Integer, ArrayList<Integer>>();
//		for (Iterator iter = csv_data_map.keySet().iterator() ; iter.hasNext();)
//		{
//			Integer source_key = (Integer)iter.next();
//			String company_code_number = FinanceRecorderCSVHandlerMap.get_company_code_number(source_key);
//			Integer company_group_number = company_profile.lookup_company_group_number(company_code_number);
//			if (!company_group_source_key_map.containsKey(company_group_number))
//				company_group_source_key_map.put(company_group_number, new ArrayList<Integer>());
//			company_group_source_key_map.get(company_group_number).add(source_key);
//		}
//OUT:
//		for (Map.Entry<Integer, ArrayList<Integer>> entry : company_group_source_key_map.entrySet())
//		{
//// Connect to MySQL based on company group
//			Integer company_group_number = entry.getKey();
//// Establish the connection to the MySQL and create the database if not exist
//			FinanceRecorderSQLClient sql_client = new FinanceRecorderSQLClient();
//			String database_name = String.format("%s%02d", FinanceRecorderCmnDef.SQL_STOCK_DATABASE_NAME, company_group_number);
//			ret = sql_client.try_connect_mysql(database_name, FinanceRecorderCmnDef.DatabaseNotExistIngoreType.DatabaseNotExistIngore_Yes, database_create_thread_type);
//			if (FinanceRecorderCmnDef.CheckFailure(ret))
//				break OUT;
//// Traverse each company
//			ArrayList<Integer> source_key_array = entry.getValue();
//			for (Integer source_key : source_key_array)
//			{
//				Integer source_type_index = FinanceRecorderCSVHandlerMap.get_source_type(source_key);
//				String company_code_number = FinanceRecorderCSVHandlerMap.get_company_code_number(source_key);
//// Create MySQL table
//				ret = sql_client.create_stock_table(source_type_index, company_code_number);
//				if (FinanceRecorderCmnDef.CheckFailure(ret))
//					break OUT;
//// Write the data into MySQL database
//				FinanceRecorderCSVHandler csv_reader = csv_data_map.get(source_key);
//				ret = sql_client.insert_stock_data(source_type_index, company_code_number, csv_reader);
//				if (FinanceRecorderCmnDef.CheckFailure(ret))
//					break OUT;
//			}
//// Destroy the connection to the MySQL
//			sql_client.disconnect_mysql();
//		}
		return ret;
	}

	public short transfrom_csv_to_sql()
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		OUT:
		for (Map.Entry<Integer, ArrayList<String>> company_code_entry : company_group_set)
		{
			int company_group_number = company_code_entry.getKey();
// Establish the connection to the MySQL and create the database if not exist
			FinanceRecorderSQLClient sql_client = new FinanceRecorderSQLClient();
			String database_name = String.format("%s%02d", FinanceRecorderCmnDef.SQL_STOCK_DATABASE_NAME, company_group_number);
			ret = sql_client.try_connect_mysql(database_name, FinanceRecorderCmnDef.DatabaseNotExistIngoreType.DatabaseNotExistIngore_Yes, database_create_thread_type);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				break OUT;
// For each company
			for(String company_code_number : company_code_entry.getValue())
			{
// For each source type
				for (int source_type_index : source_type_list)
				{
// Read data from CSV
					FinanceRecorderCSVHandler csv_reader = FinanceRecorderCSVHandler.get_csv_reader(FinanceRecorderStockDataHandler.get_csv_filepath(source_type_index, company_group_number, company_code_number));
					ret = csv_reader.read();
					if (FinanceRecorderCmnDef.CheckFailure(ret))
						return ret;
// Create MySQL table
					ret = sql_client.create_stock_table(source_type_index, company_code_number);
					if (FinanceRecorderCmnDef.CheckFailure(ret))
						break OUT;
// Write the data into MySQL database
					ret = sql_client.insert_stock_data(source_type_index, company_code_number, csv_reader);
					if (FinanceRecorderCmnDef.CheckFailure(ret))
						break OUT;
				}
			}
// Destroy the connection to the MySQL
			sql_client.disconnect_mysql();
		}
		return ret;
	}

	public void enable_multi_thread_type(boolean enable)
	{
		database_create_thread_type = enable ? FinanceRecorderCmnDef.DatabaseCreateThreadType.DatabaseCreateThread_Multiple : FinanceRecorderCmnDef.DatabaseCreateThreadType.DatabaseCreateThread_Single;
	}
	public boolean is_multi_thread_type(){return database_create_thread_type == FinanceRecorderCmnDef.DatabaseCreateThreadType.DatabaseCreateThread_Multiple;}
}
