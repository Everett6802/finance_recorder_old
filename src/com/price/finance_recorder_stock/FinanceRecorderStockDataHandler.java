package com.price.finance_recorder_stock;

import java.util.*;
import java.io.*;

import com.price.finance_recorder_base.FinanceRecorderCSVHandler;
import com.price.finance_recorder_base.FinanceRecorderCSVHandlerMap;
import com.price.finance_recorder_base.FinanceRecorderDataHandlerBase;
import com.price.finance_recorder_base.FinanceRecorderDataHandlerInf;
//import com.price.finance_recorder_base.FinanceRecorderSQLClient;
//import com.price.finance_recorder_cmn.FinanceRecorderClassBase;
import com.price.finance_recorder_cmn.FinanceRecorderCmnClass;
//import com.price.finance_recorder_cmn.FinanceRecorderCmnClass.ResultSetMap;
import com.price.finance_recorder_cmn.FinanceRecorderCmnDef;
//import com.price.finance_recorder_market.FinanceRecorderStockDataHandler;
//import com.price.finance_recorder_market.FinanceRecorderStockSQLClient;
//import com.price.finance_recorder_market.FinanceRecorderMarketSQLClient;
import com.price.finance_recorder_market.FinanceRecorderMarketSQLClient;


public class FinanceRecorderStockDataHandler extends FinanceRecorderDataHandlerBase
{
//	private static FinanceRecorderCmnClassCompanyProfile company_profile = FinanceRecorderCmnClassCompanyProfile.get_instance();
	private static FinanceRecorderCmnClass.QuerySet whole_field_query_set = null;
	private static FinanceRecorderCompanyProfile company_profile = null;
	private static String get_csv_filepath(String csv_folderpath, int source_type_index, int company_group_number, String company_code_number)
	{
		return String.format("%s/%s%02d/%s/%s.csv", csv_folderpath, FinanceRecorderCmnDef.CSV_STOCK_FOLDERNAME, company_group_number, company_code_number, FinanceRecorderCmnDef.FINANCE_DATA_NAME_LIST[source_type_index]);
	}

//	private static String get_sql_database_name(int company_group_number)
//	{
//		return String.format("%s%02d", FinanceRecorderCmnDef.SQL_STOCK_DATABASE_NAME, company_group_number);
//	}
//
//	private static String get_sql_table_name(int source_type_index, String company_code_number)
//	{
//		return String.format("%s%s", company_code_number, FinanceRecorderCmnDef.FINANCE_DATA_NAME_LIST[source_type_index]);
//	}

	private static FinanceRecorderCompanyProfile get_company_profile()
	{
		if (company_profile == null)
			company_profile = FinanceRecorderCompanyProfile.get_instance();
		return company_profile;
	}

	public static FinanceRecorderDataHandlerInf get_data_handler(final LinkedList<Integer> source_type_index_list, final FinanceRecorderCompanyGroupSet company_group_set)
	{
		if (!FinanceRecorderCmnDef.IS_FINANCE_STOCK_MODE)
		{
			String errmsg = "It's NOT Stock mode";
			throw new IllegalStateException(errmsg);
		}
		for (Integer source_type_index : source_type_index_list)
		{
			if (!FinanceRecorderCmnDef.FinanceSourceType.is_stock_source_type(source_type_index))
			{
				String errmsg = String.format("The data source[%d] is NOT Stock source type", source_type_index);
				throw new IllegalArgumentException(errmsg);
			}
		}
		FinanceRecorderStockDataHandler data_handler_obj = new FinanceRecorderStockDataHandler();
		data_handler_obj.source_type_index_list = source_type_index_list;
		if (data_handler_obj.source_type_index_list == null)
		{
			data_handler_obj.source_type_index_list = new LinkedList<Integer>();
			int start_index = FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_StockStart.value();
			int end_index = FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_StockEnd.value();
			for (int source_type_index = start_index ; source_type_index < end_index ; source_type_index++)
				source_type_index_list.add(source_type_index);
		}
		data_handler_obj.company_group_set = company_group_set;
		if (data_handler_obj.company_group_set == null)
			data_handler_obj.company_group_set = FinanceRecorderCompanyGroupSet.get_whole_company_group_set();
		return data_handler_obj;
	}
	public static FinanceRecorderDataHandlerInf get_data_handler_whole()
	{
		return get_data_handler(null, null);
	}

	private FinanceRecorderCompanyGroupSet company_group_set = null;
	private FinanceRecorderCmnDef.DatabaseCreateThreadType database_create_thread_type = FinanceRecorderCmnDef.DatabaseCreateThreadType.DatabaseCreateThread_Single;
//	private LinkedList<FinanceRecorderCmnClass.SourceTypeTimeRange> source_type_time_range_list = null;
//	private String csv_backup_foldername = FinanceRecorderCmnDef.COPY_BACKUP_FOLDERPATH;

	private FinanceRecorderStockDataHandler()
	{
		if (whole_field_query_set == null)
		{
			whole_field_query_set = new FinanceRecorderCmnClass.QuerySet();
			int source_type_start_index = FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_StockStart.value();
			int source_type_end_index = FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_StockEnd.value();
			for (int source_type_index = source_type_start_index ; source_type_index < source_type_end_index ; source_type_index++)
				whole_field_query_set.add_query(source_type_index);
			whole_field_query_set.add_query_done();
		}
	}

	public short read_from_csv(FinanceRecorderCSVHandlerMap csv_data_map, boolean stop_when_csv_not_foud)
	{
		assert source_type_index_list != null : "source_type_index_list == NULL";
		assert company_group_set != null : "company_group_set == NULL";

		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		for (Map.Entry<Integer, ArrayList<String>> company_code_entry : company_group_set)
		{
			int company_group_number = company_code_entry.getKey();
			for(String company_code_number : company_code_entry.getValue())
			{
				for (Integer source_type_index : source_type_index_list)
				{
					FinanceRecorderCSVHandler csv_reader = FinanceRecorderCSVHandler.get_csv_reader(FinanceRecorderStockDataHandler.get_csv_filepath(finance_root_folerpath, source_type_index, company_group_number, company_code_number));
					if (csv_reader == null)
					{
						FinanceRecorderCmnDef.error(String.format("CSV NOT Found [%s:%d]", company_code_number, source_type_index));
						if (stop_when_csv_not_foud)
							return FinanceRecorderCmnDef.RET_FAILURE_NOT_FOUND;
						else
							continue;
					}
					ret = csv_reader.read();
					if (FinanceRecorderCmnDef.CheckFailure(ret))
						return ret;
					csv_data_map.put(FinanceRecorderCmnDef.get_source_key(source_type_index, company_group_number, company_code_number), csv_reader);
				}
			}
		}
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	public short write_into_sql(final FinanceRecorderCSVHandlerMap csv_data_map)
	{
		assert source_type_index_list != null : "source_type_index_list == NULL";
		assert company_group_set != null : "company_group_set == NULL";

		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		OUT:
		for (Map.Entry<Integer, ArrayList<String>> company_code_entry : company_group_set)
		{
			int company_group_number = company_code_entry.getKey();
// Establish the connection to the MySQL and create the database if not exist
			FinanceRecorderStockSQLClient sql_client = new FinanceRecorderStockSQLClient();
			String database_name = String.format("%s%02d", FinanceRecorderCmnDef.SQL_STOCK_DATABASE_NAME, company_group_number);
			ret = sql_client.try_connect_mysql(database_name, FinanceRecorderCmnDef.DatabaseNotExistIngoreType.DatabaseNotExistIngore_Yes, database_create_thread_type);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				break OUT;
			for(String company_code_number : company_code_entry.getValue())
			{
				for (Integer source_type_index : source_type_index_list)
				{
// Check data exist
					Integer source_key = FinanceRecorderCmnDef.get_source_key(source_type_index, company_group_number, company_code_number);
					if (!csv_data_map.containsKey(source_key))
					{
						FinanceRecorderCmnDef.format_error("The CSV data of source key[%d] (source_type: %d, company_code_number: %s)", source_key, source_type_index, company_code_number);
						ret = FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
						break OUT;
					}
// Create MySQL table
					ret = sql_client.create_table(source_type_index, company_code_number);
					if (FinanceRecorderCmnDef.CheckFailure(ret))
						break OUT;
// Write the data into MySQL database
					FinanceRecorderCSVHandler csv_reader = csv_data_map.get(source_key);
					ret = sql_client.insert_data(source_type_index, company_code_number, csv_reader);
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
//			FinanceRecorderStockSQLClient sql_client = new FinanceRecorderStockSQLClient();
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

	public short transfrom_csv_to_sql(boolean stop_when_csv_not_foud)
	{
		assert source_type_index_list != null : "source_type_index_list == NULL";
		assert company_group_set != null : "company_group_set == NULL";

		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		OUT:
		for (Map.Entry<Integer, ArrayList<String>> company_code_entry : company_group_set)
		{
			int company_group_number = company_code_entry.getKey();
// Establish the connection to the MySQL and create the database if not exist
			FinanceRecorderStockSQLClient sql_client = new FinanceRecorderStockSQLClient();
			String database_name = String.format("%s%02d", FinanceRecorderCmnDef.SQL_STOCK_DATABASE_NAME, company_group_number);
			ret = sql_client.try_connect_mysql(database_name, FinanceRecorderCmnDef.DatabaseNotExistIngoreType.DatabaseNotExistIngore_Yes, database_create_thread_type);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				break OUT;
// For each company
			for(String company_code_number : company_code_entry.getValue())
			{
// For each source type
				for (Integer source_type_index : source_type_index_list)
				{
// Read data from CSV
					FinanceRecorderCSVHandler csv_reader = FinanceRecorderCSVHandler.get_csv_reader(FinanceRecorderStockDataHandler.get_csv_filepath(finance_root_folerpath, source_type_index, company_group_number, company_code_number));
					if (csv_reader == null)
					{
						FinanceRecorderCmnDef.error(String.format("CSV NOT Found [%s:%d]", company_code_number, source_type_index));
						if (stop_when_csv_not_foud)
							return FinanceRecorderCmnDef.RET_FAILURE_NOT_FOUND;
						else
							continue;
					}
					ret = csv_reader.read();
					if (FinanceRecorderCmnDef.CheckFailure(ret))
						return ret;
// Create MySQL table
					ret = sql_client.create_table(source_type_index, company_code_number);
					if (FinanceRecorderCmnDef.CheckFailure(ret))
						break OUT;
// Write the data into MySQL database
					ret = sql_client.insert_data(source_type_index, company_code_number, csv_reader);
					if (FinanceRecorderCmnDef.CheckFailure(ret))
						break OUT;
				}
			}
// Destroy the connection to the MySQL
			sql_client.disconnect_mysql();
		}
		return ret;
	}

	public short read_from_sql(FinanceRecorderCmnClass.QuerySet query_set, FinanceRecorderCmnClass.FinanceTimeRange finance_time_range, FinanceRecorderCmnClass.ResultSetMap result_set_map)
	{
		assert source_type_index_list != null : "source_type_index_list == NULL";
		assert company_group_set != null : "company_group_set == NULL";

// CAUTION: The data set in the reslt_set variable should be added before calling this function 
// Set the mapping table of reading the specific CSV files and writing correct SQL database
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		FinanceRecorderCmnDef.ResultSetDataUnit data_unit = result_set_map.get_data_unit();
		FinanceRecorderStockSQLClient sql_client = new FinanceRecorderStockSQLClient();
		for (Map.Entry<Integer, ArrayList<String>> company_code_entry : company_group_set)
		{
			int company_group_number = company_code_entry.getKey();
// Establish the connection to the MySQL
			ret = sql_client.try_connect_mysql(company_group_number, FinanceRecorderCmnDef.DatabaseNotExistIngoreType.DatabaseNotExistIngore_No, FinanceRecorderCmnDef.DatabaseCreateThreadType.DatabaseCreateThread_Single);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				return ret;
			FinanceRecorderCmnClass.ResultSet result_set = null;
OUT:
			for(String company_code_number : company_code_entry.getValue())
			{
				switch (data_unit)
				{
				case ResultSetDataUnit_NoSourceType:
				{
					result_set = new FinanceRecorderCmnClass.ResultSet();
// Add query set
					for (Integer source_type_index : source_type_index_list)
					{
						ret = result_set.add_set(source_type_index, query_set.get_field_index_list(source_type_index));
						if (FinanceRecorderCmnDef.CheckFailure(ret))
							break OUT;
					}
// Query data from each source type
					for (Integer source_type_index : source_type_index_list)
					{
						ret = sql_client.select_data(source_type_index, company_code_number, finance_time_range, result_set);
						if (FinanceRecorderCmnDef.CheckFailure(ret))
							break OUT;
					}
// Keep track of the data in the designated data structure
					ret = result_set_map.register_result_set(Integer.valueOf(company_code_number), result_set);
					if (FinanceRecorderCmnDef.CheckFailure(ret))
						break OUT;
				}
				break;
				case ResultSetDataUnit_SourceType:
				{
					for (Integer source_type_index : source_type_index_list)
					{
						result_set = new FinanceRecorderCmnClass.ResultSet();
// Add query set
						ret = result_set.add_set(source_type_index, query_set.get_field_index_list(source_type_index));
						if (FinanceRecorderCmnDef.CheckFailure(ret))
							break OUT;
// Query data from each source type
						ret = sql_client.select_data(source_type_index, company_code_number, finance_time_range, result_set);
						if (FinanceRecorderCmnDef.CheckFailure(ret))
							break OUT;
// Keep track of the data in the designated data structure
						ret = result_set_map.register_result_set(FinanceRecorderCmnDef.get_source_key(source_type_index, company_group_number, company_code_number), result_set);
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
			}
	// Destroy the connection to the MySQL
			sql_client.disconnect_mysql();
		}

		return ret;
	}
	public short read_from_sql(FinanceRecorderCmnClass.FinanceTimeRange finance_time_range, FinanceRecorderCmnClass.ResultSetMap result_set_map)
	{
		return read_from_sql(whole_field_query_set, finance_time_range, result_set_map);
	}

	public short write_into_csv(FinanceRecorderCmnClass.ResultSetMap result_set_map)
	{
		assert source_type_index_list != null : "source_type_index_list == NULL";
		assert company_group_set != null : "company_group_set == NULL";

		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		FinanceRecorderCmnDef.ResultSetDataUnit data_unit = result_set_map.get_data_unit();
		FinanceRecorderCmnClass.ResultSet result_set = null;
OUT:
		switch (data_unit)
		{
		case ResultSetDataUnit_NoSourceType:
		{
			for (Map.Entry<Integer, FinanceRecorderCmnClass.ResultSet> entry : result_set_map)
			{
				int source_key = entry.getKey();
				String company_code_number = FinanceRecorderCmnDef.get_company_code_number(source_key);
				int company_group_number = FinanceRecorderCmnDef.get_company_group_number(source_key);
				result_set = entry.getValue();
				for (Integer source_type_index : source_type_index_list)
				{
					FinanceRecorderCSVHandler csv_writer = FinanceRecorderCSVHandler.get_csv_writer(FinanceRecorderStockDataHandler.get_csv_filepath(finance_root_backup_folerpath, source_type_index, company_group_number, company_code_number));
	//Assemble the data and write into CSV
					ArrayList<String> csv_data_list = result_set.to_string_array(source_type_index);
					csv_writer.set_write_data(csv_data_list);
					ret = csv_writer.write();
					if (FinanceRecorderCmnDef.CheckFailure(ret))
						break OUT;
				}
			}
		}
		break;
		case ResultSetDataUnit_SourceType:
		{
			for (Map.Entry<Integer, FinanceRecorderCmnClass.ResultSet> entry : result_set_map)
			{
				int source_key = entry.getKey();
				int source_type_index = FinanceRecorderCmnDef.get_source_type(source_key);
				String company_code_number = FinanceRecorderCmnDef.get_company_code_number(source_key);
				int company_group_number = FinanceRecorderCmnDef.get_company_group_number(source_key);
				result_set = entry.getValue();
// Ignore the data which is NOT in the list
				FinanceRecorderCSVHandler csv_writer = FinanceRecorderCSVHandler.get_csv_writer(FinanceRecorderStockDataHandler.get_csv_filepath(finance_root_folerpath, source_type_index, company_group_number, company_code_number));
// Assemble the data and write into CSV
				ArrayList<String> csv_data_list = result_set.to_string_array(source_type_index);
				csv_writer.set_write_data(csv_data_list);
				ret = csv_writer.write();
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
		return ret;
	}

	public short transfrom_sql_to_csv(FinanceRecorderCmnClass.QuerySet query_set, FinanceRecorderCmnClass.FinanceTimeRange finance_time_range)
	{
		assert source_type_index_list != null : "source_type_index_list == NULL";
		assert company_group_set != null : "company_group_set == NULL";

		if (!query_set.is_add_query_done())
		{
			FinanceRecorderCmnDef.error("The add-done flag in query_set is NOT true");
			return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
		}
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		FinanceRecorderStockSQLClient sql_client = new FinanceRecorderStockSQLClient();
		FinanceRecorderCmnClass.ResultSet result_set = new FinanceRecorderCmnClass.ResultSet();
// Add query set
		for (Integer source_type_index : source_type_index_list)
		{
			ret = result_set.add_set(source_type_index, query_set.get_field_index_list(source_type_index));
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				return ret;
		}
		for (Map.Entry<Integer, ArrayList<String>> company_code_entry : company_group_set)
		{
			int company_group_number = company_code_entry.getKey();
// Establish the connection to the MySQL
			ret = sql_client.try_connect_mysql(company_group_number, FinanceRecorderCmnDef.DatabaseNotExistIngoreType.DatabaseNotExistIngore_No, FinanceRecorderCmnDef.DatabaseCreateThreadType.DatabaseCreateThread_Single);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				return ret;
OUT:
			for(String company_code_number : company_code_entry.getValue())
			{
// Query data from each source type
				for (Integer source_type_index : source_type_index_list)
				{
					ret = sql_client.select_data(source_type_index, company_code_number, finance_time_range, result_set);
					if (FinanceRecorderCmnDef.CheckFailure(ret))
						break OUT;
					FinanceRecorderCSVHandler csv_writer = FinanceRecorderCSVHandler.get_csv_writer(FinanceRecorderStockDataHandler.get_csv_filepath(finance_root_backup_folerpath, source_type_index, company_group_number, company_code_number));
//Assemble the data and write into CSV
					ArrayList<String> csv_data_list = result_set.to_string_array(source_type_index);
					csv_writer.set_write_data(csv_data_list);
					ret = csv_writer.write();
					if (FinanceRecorderCmnDef.CheckFailure(ret))
						break OUT;
				}
// Cleanup the result data
				result_set.reset_result();
			}
// Destroy the connection to the MySQL
			sql_client.disconnect_mysql();
		}
		return ret;
	}
	public short transfrom_whole_sql_to_csv(FinanceRecorderCmnClass.FinanceTimeRange finance_time_range)
	{
		return transfrom_sql_to_csv(whole_field_query_set, finance_time_range);
	}

	private short delete_sql_table_by_source_type_and_company(List<Integer> in_source_type_index_list, FinanceRecorderCompanyGroupSet in_company_group_set)
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		for (Map.Entry<Integer, ArrayList<String>> company_code_entry : in_company_group_set)
		{
			FinanceRecorderStockSQLClient sql_client = new FinanceRecorderStockSQLClient();
			int company_group_number = company_code_entry.getKey();
// Establish the connection to the MySQL
			ret = sql_client.try_connect_mysql(company_group_number, FinanceRecorderCmnDef.DatabaseNotExistIngoreType.DatabaseNotExistIngore_No, FinanceRecorderCmnDef.DatabaseCreateThreadType.DatabaseCreateThread_Single);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				return ret;
OUT:
			for(String company_code_number : company_code_entry.getValue())
			{
// Delete each table
				for (Integer source_type_index : in_source_type_index_list)
				{
					ret = sql_client.delete_table(source_type_index, company_code_number);
					if (FinanceRecorderCmnDef.CheckFailure(ret))
						return ret;
				}
			}
// Destroy the connection to the MySQL
			sql_client.disconnect_mysql();
		}
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	public short delete_sql_by_source_type()
	{
		assert source_type_index_list != null : "source_type_index_list == NULL";

		return delete_sql_table_by_source_type_and_company(source_type_index_list, FinanceRecorderCompanyGroupSet.get_whole_company_group_set());
	}

	public short delete_sql_by_company() // Only useful in stock mode
	{
		assert company_group_set != null : "company_group_set == NULL";

		return delete_sql_table_by_source_type_and_company(FinanceRecorderCmnDef.get_all_source_type_index_list(), company_group_set);
	} 

	public short delete_sql_by_source_type_and_company() // Only useful in stock mode
	{
		assert source_type_index_list != null : "source_type_index_list == NULL";
		assert company_group_set != null : "company_group_set == NULL";

		return delete_sql_table_by_source_type_and_company(source_type_index_list, company_group_set);
	} 

	public short cleanup_sql()
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
// Establish the connection to the MySQL
		int company_group_size = get_company_profile().get_company_group_size();
OUT:
		for (int i = 0 ; i < company_group_size ; i++)
		{
			String database_name = FinanceRecorderStockSQLClient.get_database_name(i);
			FinanceRecorderStockSQLClient sql_client = new FinanceRecorderStockSQLClient();
			ret = sql_client.try_connect_mysql(database_name, FinanceRecorderCmnDef.DatabaseNotExistIngoreType.DatabaseNotExistIngore_Yes, FinanceRecorderCmnDef.DatabaseCreateThreadType.DatabaseCreateThread_Single);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
			{
				if (FinanceRecorderCmnDef.CheckMySQLFailureUnknownDatabase(ret))
					continue;
				else
					return ret;
			}
// Delete the database
			ret = sql_client.delete_database(i);
// Destroy the connection to the MySQL
			sql_client.disconnect_mysql();
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				break OUT;
		}
		return ret;
	}

	public short check_sql_exist(ArrayList<String> not_exist_list)
	{
		assert source_type_index_list != null : "source_type_index_list == NULL";
		assert company_group_set != null : "company_group_set == NULL";

		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
// Establish the connection to the MySQL
OUT:
		for (Map.Entry<Integer, ArrayList<String>> company_code_entry : company_group_set)
		{
			FinanceRecorderStockSQLClient sql_client = new FinanceRecorderStockSQLClient();
			int company_group_number = company_code_entry.getKey();
// Establish the connection to the MySQL
			ret = sql_client.try_connect_mysql(company_group_number, FinanceRecorderCmnDef.DatabaseNotExistIngoreType.DatabaseNotExistIngore_No, FinanceRecorderCmnDef.DatabaseCreateThreadType.DatabaseCreateThread_Single);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
			{
				if (FinanceRecorderCmnDef.CheckMySQLFailureUnknownDatabase(ret))
				{
					String database_name = String.format("%s%02d", FinanceRecorderCmnDef.SQL_STOCK_DATABASE_NAME, company_group_number);
					FinanceRecorderCmnDef.format_warn("The database[%s] does NOT exist, add all tables in the Not-Found List......", database_name);
					LinkedList<Integer> all_source_type_index_list = FinanceRecorderCmnDef.get_all_source_type_index_list();
					for(String company_code_number : company_code_entry.getValue())
					{
						for (Integer source_type_index : all_source_type_index_list)
							not_exist_list.add(String.format("%s:%d", company_code_number, source_type_index));
					}
					return FinanceRecorderCmnDef.RET_FAILURE_NOT_FOUND;
				}
				else
					return ret;
			}
// Check MySQL table exist
			for(String company_code_number : company_code_entry.getValue())
			{
				for (Integer source_type_index : source_type_index_list)
				{
					ret = sql_client.check_table_exist(source_type_index, company_code_number);
					if (FinanceRecorderCmnDef.CheckFailure(ret))
					{
						if (FinanceRecorderCmnDef.CheckFailureNotFound(ret))
							not_exist_list.add(String.format("%s:%d", company_code_number, source_type_index));
						else
							break OUT;
					}
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
