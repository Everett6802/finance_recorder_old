package com.price.finance_recorder;

import java.util.*;


class MarketDataHandler extends DataHandlerBase
{
	private static CmnClass.QuerySet whole_field_query_set = null;
	private static String get_csv_filepath(String csv_folderpath, int method_index)
	{
		return String.format("%s/%s/%s.csv", csv_folderpath, CmnDef.CSV_MARKET_FOLDERNAME, CmnDef.FINANCE_DATA_NAME_LIST[method_index]);
	}
	private static String get_csv_urlpath(String csv_server_ip, int method_index)
	{
		//"http://localhost/finance_data/finance/market/stock_exchange_and_volume.csv"
		String csv_url_folderpath = String.format(CmnDef.CSV_REMOTE_ROOT_FOLDERPATH_FORMAT, csv_server_ip);
		return String.format("%s/%s/%s.csv", csv_url_folderpath, CmnDef.CSV_MARKET_FOLDERNAME, CmnDef.FINANCE_DATA_NAME_LIST[method_index]);
	}

//	private static String get_sql_database_name()
//	{
//		return CmnDef.SQL_MARKET_DATABASE_NAME;
//	}
//
//	private static String get_sql_table_name(int method_index)
//	{
//		return CmnDef.FINANCE_DATA_NAME_LIST[method_index];
//	}

	public static CmnInf.DataHandlerInf get_data_handler(final LinkedList<Integer> method_index_list)
	{
		if (!CmnDef.IS_FINANCE_MARKET_MODE)
		{
			String errmsg = "It's NOT Market mode";
			throw new IllegalStateException(errmsg);
		}
		for (Integer method_index : method_index_list)
		{
			if (!CmnDef.FinanceMethod.is_market_method(method_index))
			{
				String errmsg = String.format("The data source[%d] is NOT Market source type", method_index);
				throw new IllegalArgumentException(errmsg);
			}
		}
		MarketDataHandler data_handler_obj = new MarketDataHandler();
		data_handler_obj.method_index_list = method_index_list;
		if (data_handler_obj.method_index_list == null)
		{
			data_handler_obj.method_index_list = new LinkedList<Integer>();
			int start_index = CmnDef.FinanceMethod.FinanceMethod_MarketStart.value();
			int end_index = CmnDef.FinanceMethod.FinanceMethod_MarketEnd.value();
			for (int method_index = start_index ; method_index < end_index ; method_index++)
				method_index_list.add(method_index);
		}
		return data_handler_obj;
	}
	public static CmnInf.DataHandlerInf get_data_handler_whole()
	{
		return get_data_handler(null);
	}

	private ArrayList<Integer> missing_csv_list = null;
	private MarketSQLClient sql_client = null;
//	private LinkedList<CmnClass.MethodTimeRange> method_time_range_list = null;
//	private String current_csv_working_folerpath = CmnDef.BACKUP_CSV_ROOT_FOLDERPATH;

	private MarketDataHandler()
	{
		if (whole_field_query_set == null)
		{
			whole_field_query_set = new CmnClass.QuerySet();
			int method_start_index = CmnDef.FinanceMethod.FinanceMethod_MarketStart.ordinal();
			int method_end_index = CmnDef.FinanceMethod.FinanceMethod_MarketEnd.ordinal();
			for (int method_index = method_start_index ; method_index < method_end_index ; method_index++)
				whole_field_query_set.add_query(method_index);
			whole_field_query_set.add_query_done();
		}
		if (sql_client == null)
			sql_client = new MarketSQLClient();
	}

	protected short create_finance_folder_hierarchy(String root_folderpath)
	{
		short ret = CmnFunc.create_folder_if_not_exist(root_folderpath);
		if (CmnDef.CheckFailure(ret))
			CmnLogger.format_error("Fail to create the root folder[%s], due to: %s", root_folderpath, CmnDef.GetErrorDescription(ret));
		return ret;
	}

	protected short parse_missing_csv()
	{
		LinkedList<String> config_line_list = new LinkedList<String>();
		short ret = CmnFunc.read_config_file_lines(CmnDef.MISSING_CSV_MARKET_FILENAME, current_csv_working_folerpath, config_line_list);
		if (CmnDef.CheckFailure(ret))
		{
			if (!CmnDef.CheckFailureNotFound(ret))
				return ret;
			else
				CmnLogger.format_debug("The missing CSV file[%s] does NOT exist", CmnDef.MISSING_CSV_MARKET_FILENAME);
		}
		else
		{
// Parse the config
//			missing_csv_list = new ArrayList<Integer>();
			String missing_csv_title = config_line_list.pop();
			if (missing_csv_title.indexOf("[FileNotFound]") != -1)
				missing_csv_list = new ArrayList<Integer>();
			else
			{
				config_line_list.pop();
				missing_csv_title = config_line_list.pop();
				if (missing_csv_title.indexOf("[FileNotFound]") != -1)
					missing_csv_list = new ArrayList<Integer>();
			}
			if (missing_csv_list != null)
			{
				String missing_csv_string = config_line_list.pop();
				String[] missing_csv_array = missing_csv_string.split(";");
				for (String missing_csv : missing_csv_array)
				{
					Integer method_index = Integer.valueOf(missing_csv);
					missing_csv_list.add(method_index);
				}
			}
		}
		return CmnDef.RET_SUCCESS;
	}

	public short read_from_csv(CSVHandlerMap csv_data_map)
	{
		assert method_index_list != null : "method_index_list == NULL";

		short ret = CmnDef.RET_SUCCESS;
// Ignore the CSV file which is already in the Not Found list
		ret = parse_missing_csv();
		if (CmnDef.CheckFailure(ret))
			return ret;

		for (Integer method_index : method_index_list)
		{
			CSVHandler csv_reader = CSVHandler.get_csv_reader(MarketDataHandler.get_csv_filepath(current_csv_working_folerpath, method_index));
			if (csv_reader == null)
			{
				if (!is_operation_non_stop())
				{
//Check this missing CSV exist in the Not Found list
					if (missing_csv_list != null)
					{
						if (missing_csv_list.indexOf(method_index) != -1)
						{
							CmnLogger.format_debug("CSV[%d] already in the Not-Found list", method_index);
							continue;
						}
					}
					CmnLogger.error(String.format("CSV NOT Found [%s:%d]", method_index));
					return CmnDef.RET_FAILURE_NOT_FOUND;
				}
				else
					continue;
			}
			ret = csv_reader.read();
			if (CmnDef.CheckFailure(ret))
				return ret;
			csv_data_map.put(CmnFunc.get_source_key(method_index), csv_reader);
		}
		return ret;
	}

	public short write_into_sql(final CSVHandlerMap csv_data_map)
	{
		assert method_index_list != null : "method_index_list == NULL";

		short ret = CmnDef.RET_SUCCESS;
// Establish the connection to the MySQL and create the database if not exist
//		MarketSQLClient sql_client = new MarketSQLClient();
		ret = sql_client.try_connect_mysql();
		if (CmnDef.CheckFailure(ret))
		{
			if (connect_mysql_can_continue(ret))
			{
				CmnLogger.format_warn("Try to create the database: %s", CmnDef.SQL_MARKET_DATABASE_NAME);
				ret = sql_client.create_database();
				if (CmnDef.CheckFailure(ret))
					return ret;
// It's required to re-connect the database after creating it
				ret = sql_client.try_connect_mysql();
				if (CmnDef.CheckFailure(ret))
					return ret;
			}
			else
				return ret;
		}
OUT:
		for (Integer method_index : method_index_list)
		{
// Check data exist
			Integer source_key = CmnFunc.get_source_key(method_index);
			if (!csv_data_map.containsKey(source_key))
			{
				CmnLogger.format_error("The CSV data of source key[%d] (method: %d)", source_key, method_index);
				ret = CmnDef.RET_FAILURE_INVALID_ARGUMENT;
				break OUT;
			}
// Create MySQL table
			ret = sql_client.create_table(method_index);
			if (CmnDef.CheckFailure(ret))
			{
				if (operation_can_continue(ret))
					continue;
				else
					break OUT;
			}
// Write the data into MySQL database
			CSVHandler csv_reader = csv_data_map.get(source_key);
			ret = sql_client.insert_data(method_index, csv_reader);
			if (CmnDef.CheckFailure(ret))
				break OUT;
		}
// Destroy the connection to the MySQL
		sql_client.disconnect_mysql();
		return ret;
	}

	public short transfrom_csv_to_sql()
	{
		assert method_index_list != null : "method_index_list == NULL";

		short ret = CmnDef.RET_SUCCESS;
		if (is_operation_non_stop())
		{
// Ignore the CSV file which is already in the Not Found list
			ret = parse_missing_csv();
			if (CmnDef.CheckFailure(ret))
				return ret;
		}
// Establish the connection to the MySQL and create the database if not exist
//		MarketSQLClient sql_client = new MarketSQLClient();
		ret = sql_client.try_connect_mysql();
		if (CmnDef.CheckFailure(ret))
		{
			if (connect_mysql_can_continue(ret))
			{
				CmnLogger.format_warn("Try to create the database: %s", CmnDef.SQL_MARKET_DATABASE_NAME);
				ret = sql_client.create_database();
// Should NOT fail in this condition......
				if (CmnDef.CheckFailure(ret))
					return ret;
// It's required to re-connect the database after creating it
				ret = sql_client.try_connect_mysql();
				if (CmnDef.CheckFailure(ret))
					return ret;
			}
			else
				return ret;
		}
OUT:
// For each source type
		for (Integer method_index : method_index_list)
		{
// Read data from CSV
			CSVHandler csv_reader = CSVHandler.get_csv_reader(MarketDataHandler.get_csv_filepath(current_csv_working_folerpath, method_index));
			if (csv_reader == null)
			{
				CmnLogger.error(String.format("CSV NOT Found [%s:%d]", method_index));
				if (!is_operation_non_stop())
				{
//Check this missing CSV exist in the Not Found list
					if (missing_csv_list != null)
					{
						if (missing_csv_list.indexOf(method_index) != -1)
						{
							CmnLogger.format_debug("CSV[%d] already in the Not-Found list", method_index);
							continue;
						}
					}
					return CmnDef.RET_FAILURE_NOT_FOUND;
				}
				else
					continue;
			}
			ret = csv_reader.read();
			if (CmnDef.CheckFailure(ret))
				return ret;
// Create MySQL table
			ret = sql_client.create_table(method_index);
			if (CmnDef.CheckFailure(ret))
			{
				if (operation_can_continue(ret))
					continue;
				else
					break OUT;
			}
// Write the data into MySQL database
			ret = sql_client.insert_data(method_index, csv_reader);
			if (CmnDef.CheckFailure(ret))
				break OUT;
		}
// Destroy the connection to the MySQL
		sql_client.disconnect_mysql();
		return ret;
	}

	public short read_from_sql(CmnClass.QuerySet query_set, CmnClass.FinanceTimeRange finance_time_range, CmnClass.ResultSetMap result_set_map)
	{
		assert method_index_list != null : "method_index_list == NULL";

// CAUTION: The data set in the reslt_set variable should be added before calling this function 
// Set the mapping table of reading the specific CSV files and writing correct SQL database
		short ret = CmnDef.RET_SUCCESS;
		CmnDef.ResultSetDataUnit data_unit = result_set_map.get_data_unit();
// Establish the connection to the MySQL
//		MarketSQLClient sql_client = new MarketSQLClient();
		ret = sql_client.try_connect_mysql();
		if (CmnDef.CheckFailure(ret))
		{
			if (connect_mysql_can_continue(ret))
			{
// No data to read
					CmnLogger.warn(String.format("No data to read from %s", CmnDef.SQL_MARKET_DATABASE_NAME));
					return CmnDef.RET_SUCCESS;
			}
			else
				return ret;
		}
		CmnClass.ResultSet result_set = null;
OUT:
		switch (data_unit)
		{
		case ResultSetDataUnit_NoMethod:
		{
			result_set = new CmnClass.ResultSet();
// Add query set
//			for (CmnClass.MethodTimeRange method_time_range : method_time_range_list)
			for (Integer method_index : query_set.get_method_index_list())
			{
//				int method_index = method_time_range.get_method_index();
				ret = result_set.add_set(method_index, query_set.get_field_index_list(method_index));
				if (CmnDef.CheckFailure(ret))
					break OUT;
			}
// Query data from each source type
			for (Integer method_index : method_index_list)
			{
				ret = sql_client.select_data(method_index, finance_time_range, result_set);
				if (CmnDef.CheckFailure(ret))
				{
					if (operation_can_continue(ret))
						continue;
					else
						break OUT;
				}
			}
// Keep track of the data in the designated data structure
			ret = result_set_map.register_result_set(CmnDef.NO_SOURCE_TYPE_MARKET_SOURCE_KEY_VALUE, result_set);
			if (CmnDef.CheckFailure(ret))
				break OUT;
		}
		break;
		case ResultSetDataUnit_Method:
		{
			for (Integer method_index : method_index_list)
			{
				result_set = new CmnClass.ResultSet();
// Add query set
				ret = result_set.add_set(method_index, query_set.get_field_index_list(method_index));
				if (CmnDef.CheckFailure(ret))
					break OUT;
// Query data from each source type
				ret = sql_client.select_data(method_index, finance_time_range, result_set);
				if (CmnDef.CheckFailure(ret))
				{
					if (operation_can_continue(ret))
						continue;
					else
						break OUT;
				}
// Keep track of the data in the designated data structure
				ret = result_set_map.register_result_set(CmnFunc.get_source_key(method_index), result_set);
				if (CmnDef.CheckFailure(ret))
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
	public short read_from_sql(CmnClass.FinanceTimeRange finance_time_range, CmnClass.ResultSetMap result_set_map)
	{
		return read_from_sql(whole_field_query_set, finance_time_range, result_set_map);
	}

	public short write_into_csv(CmnClass.ResultSetMap result_set_map)
	{
		assert method_index_list != null : "method_index_list == NULL";

		short ret = CmnDef.RET_SUCCESS;
// Create the finance folder hierarchy for writing CSV
		ret = create_finance_folder_hierarchy(current_csv_working_folerpath);
		if (CmnDef.CheckFailure(ret))
			return ret;

		CmnDef.ResultSetDataUnit data_unit = result_set_map.get_data_unit();
OUT:
		switch (data_unit)
		{
		case ResultSetDataUnit_NoMethod:
		{
			CmnClass.ResultSet result_set = null;
			try
			{
				result_set = result_set_map.lookup_result_set(CmnDef.NO_SOURCE_TYPE_MARKET_SOURCE_KEY_VALUE);
			}
			catch (IllegalArgumentException e)
			{
				CmnLogger.format_error("No data found in the source key: %d", CmnDef.NO_SOURCE_TYPE_MARKET_SOURCE_KEY_VALUE);
				ret = CmnDef.RET_FAILURE_INVALID_ARGUMENT;
				break OUT;
			}
			for (Integer method_index : method_index_list)
			{
				CSVHandler csv_writer = CSVHandler.get_csv_writer(MarketDataHandler.get_csv_filepath(current_csv_working_folerpath, method_index));
//Assemble the data and write into CSV
				ArrayList<String> csv_data_list = result_set.to_string_array(method_index);
				csv_writer.set_write_data(csv_data_list);
				ret = csv_writer.write();
				if (CmnDef.CheckFailure(ret))
					break OUT;
			}
		}
		break;
		case ResultSetDataUnit_Method:
		{
			for (Integer method_index : method_index_list)
			{
				int source_key = CmnFunc.get_source_key(method_index);
				CmnClass.ResultSet result_set = null;
				try
				{
					result_set = result_set_map.lookup_result_set(source_key);
				}
				catch (IllegalArgumentException e)
				{
					CmnLogger.format_error("No data found in the source key: %d", source_key);
					ret = CmnDef.RET_FAILURE_INVALID_ARGUMENT;
					break OUT;
				}
				CSVHandler csv_writer = CSVHandler.get_csv_writer(MarketDataHandler.get_csv_filepath(current_csv_working_folerpath, method_index));
//Assemble the data and write into CSV
				ArrayList<String> csv_data_list = result_set.to_string_array(method_index);
				csv_writer.set_write_data(csv_data_list);
				ret = csv_writer.write();
				if (CmnDef.CheckFailure(ret))
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

	public short transfrom_sql_to_csv(CmnClass.QuerySet query_set, CmnClass.FinanceTimeRange finance_time_range)
	{
		assert method_index_list != null : "method_index_list == NULL";

		if (!query_set.is_add_query_done())
		{
			CmnLogger.error("The add-done flag in query_set is NOT true");
			return CmnDef.RET_FAILURE_INVALID_ARGUMENT;
		}

		short ret = CmnDef.RET_SUCCESS;
// Create the finance folder hierarchy for writing CSV
		ret = create_finance_folder_hierarchy(current_csv_working_folerpath);
		if (CmnDef.CheckFailure(ret))
			return ret;
// CAUTION: The data set in the reslt_set variable should be added before calling this function 
// Set the mapping table of reading the specific CSV files and writing correct SQL database
		CmnClass.ResultSet result_set = new CmnClass.ResultSet();
// Add query set
		for (Integer method_index : method_index_list)
		{
			ret = result_set.add_set(method_index, query_set.get_field_index_list(method_index));
			if (CmnDef.CheckFailure(ret))
				return ret;
		}
// Establish the connection to the MySQL
//		MarketSQLClient sql_client = new MarketSQLClient();
		ret = sql_client.try_connect_mysql();
		if (CmnDef.CheckFailure(ret))
		{
			if (connect_mysql_can_continue(ret))
			{
// No data to read
					CmnLogger.warn(String.format("No data to read from %s", CmnDef.SQL_MARKET_DATABASE_NAME));
					return CmnDef.RET_SUCCESS;
			}
			else
				return ret;
		}
OUT:
		for (Integer method_index : method_index_list)
		{
// Query data from each source type
			ret = sql_client.select_data(method_index, finance_time_range, result_set);
			if (CmnDef.CheckFailure(ret))
			{
				if (operation_can_continue(ret)) // No data to read
					continue OUT;
				else
					break OUT;
			}
			CSVHandler csv_writer = CSVHandler.get_csv_writer(MarketDataHandler.get_csv_filepath(current_csv_working_folerpath, method_index));
//Assemble the data and write into CSV
			ArrayList<String> csv_data_list = result_set.to_string_array(method_index);
			csv_writer.set_write_data(csv_data_list);
			ret = csv_writer.write();
			if (CmnDef.CheckFailure(ret))
				break OUT;
		}
// Destroy the connection to the MySQL
		sql_client.disconnect_mysql();
		return ret;
	}
	public short transfrom_whole_sql_to_csv(CmnClass.FinanceTimeRange finance_time_range)
	{
		return transfrom_sql_to_csv(whole_field_query_set, finance_time_range);
	}

	public short delete_sql_by_method()
	{
		assert method_index_list != null : "method_index_list == NULL";

		short ret = CmnDef.RET_SUCCESS;
// Establish the connection to the MySQL
//		MarketSQLClient sql_client = new MarketSQLClient();
		ret = sql_client.try_connect_mysql();
		if (CmnDef.CheckFailure(ret))
		{
			if (connect_mysql_can_continue(ret))
			{
				CmnLogger.warn(String.format("Database[%s] does NOT exist", CmnDef.SQL_MARKET_DATABASE_NAME));
				return CmnDef.RET_SUCCESS;
			}
			else
				return ret;
		}
// Delete each table
OUT:
		for (Integer method_index : method_index_list)
		{
			ret = sql_client.delete_table(method_index);
			if (CmnDef.CheckFailure(ret))
			{
				if (operation_can_continue(ret))
					continue;
				else
					break OUT;
			}
		}
// Destroy the connection to the MySQL
		sql_client.disconnect_mysql();
		return CmnDef.RET_SUCCESS;
	}

	public short delete_sql_by_company() // Only useful in stock mode
	{
		throw new IllegalStateException("Logfile cannot be read-only");
	} 

	public short delete_sql_by_method_and_company() // Only useful in stock mode
	{
		throw new IllegalStateException("Logfile cannot be read-only");
	} 

	public short cleanup_sql()
	{
		short ret = CmnDef.RET_SUCCESS;
// Establish the connection to the MySQL
//		MarketSQLClient sql_client = new MarketSQLClient();
		ret = sql_client.try_connect_mysql();
		if (CmnDef.CheckFailure(ret))
		{
			if (connect_mysql_can_continue(ret))
			{
				CmnLogger.warn(String.format("Database[%s] does NOT exist", CmnDef.SQL_MARKET_DATABASE_NAME));
				return CmnDef.RET_SUCCESS;
			}
			else
				return ret;
		}
// Delete the database
		ret = sql_client.delete_database();
		if (CmnDef.CheckFailure(ret))
		{
			if (operation_can_continue(ret))
			{
				ret = CmnDef.RET_FAILURE_NOT_FOUND;
				String errmsg = String.format("Fails to delete database[%s], due to: %s", CmnDef.SQL_MARKET_DATABASE_NAME, CmnDef.GetErrorDescription(ret));
				CmnLogger.error(errmsg);
			}
		}
// Destroy the connection to the MySQL
		sql_client.disconnect_mysql();
		return ret;
	}

	public short check_sql_exist(ArrayList<String> not_exist_list)
	{
		short ret = CmnDef.RET_SUCCESS;
// Establish the connection to the MySQL
//		MarketSQLClient sql_client = new MarketSQLClient();
		ret = sql_client.try_connect_mysql();
		if (CmnDef.CheckFailure(ret))
		{
			if (CmnDef.CheckMySQLFailureUnknownDatabase(ret))
			{
				CmnLogger.format_warn("The database[%s] does NOT exist, add all tables in the Not-Found List......", CmnDef.SQL_MARKET_DATABASE_NAME);
				LinkedList<Integer> all_method_index_list = CmnFunc.get_all_method_index_list();
				for (Integer method_index : all_method_index_list)
					not_exist_list.add(String.format("%d", method_index));
				return CmnDef.RET_SUCCESS;
			}
			else
				return ret;
		}
// Check MySQL table exist
		for (Integer method_index : method_index_list)
		{
			ret = sql_client.check_table_exist(method_index);
			if (CmnDef.CheckFailure(ret))
			{
				if (CmnDef.CheckFailureNotFound(ret))
				{
					not_exist_list.add(String.format("%d", method_index));
					ret = CmnDef.RET_SUCCESS;
				}
				else
					break;
			}
		}
// Destroy the connection to the MySQL
		sql_client.disconnect_mysql();
		return ret;
	}
}
