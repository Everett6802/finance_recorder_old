package com.price.finance_recorder_market;

//import java.io.*;
import java.util.*;

import com.price.finance_recorder_base.FinanceRecorderCSVHandler;
import com.price.finance_recorder_base.FinanceRecorderCSVHandlerMap;
import com.price.finance_recorder_base.FinanceRecorderDataHandlerBase;
import com.price.finance_recorder_base.FinanceRecorderDataHandlerInf;
//import com.price.finance_recorder_cmn.FinanceRecorderClassBase;
import com.price.finance_recorder_cmn.FinanceRecorderCmnClass;
import com.price.finance_recorder_cmn.FinanceRecorderCmnClass.FinanceTimeRange;
import com.price.finance_recorder_cmn.FinanceRecorderCmnClass.QuerySet;
//import com.price.finance_recorder_cmn.FinanceRecorderCmnClass.FinanceTimeRange;
//import com.price.finance_recorder_cmn.FinanceRecorderCmnClass.QuerySet;
import com.price.finance_recorder_cmn.FinanceRecorderCmnDef;


public class FinanceRecorderMarketDataHandler extends FinanceRecorderDataHandlerBase
{
	private static FinanceRecorderCmnClass.QuerySet whole_field_query_set = null;
	private static String get_csv_filepath(String csv_folderpath, int source_type_index)
	{
		return String.format("%s/%s/%s.csv", csv_folderpath, FinanceRecorderCmnDef.CSV_MARKET_FOLDERNAME, FinanceRecorderCmnDef.FINANCE_DATA_NAME_LIST[source_type_index]);
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

	public static FinanceRecorderDataHandlerInf get_data_handler(final LinkedList<Integer> source_type_index_list)
	{
		if (!FinanceRecorderCmnDef.IS_FINANCE_MARKET_MODE)
		{
			String errmsg = "It's NOT Market mode";
			throw new IllegalStateException(errmsg);
		}
		for (Integer source_type_index : source_type_index_list)
		{
			if (!FinanceRecorderCmnDef.FinanceSourceType.is_market_source_type(source_type_index))
			{
				String errmsg = String.format("The data source[%d] is NOT Market source type", source_type_index);
				throw new IllegalArgumentException(errmsg);
			}
		}
		FinanceRecorderMarketDataHandler data_handler_obj = new FinanceRecorderMarketDataHandler();
		data_handler_obj.source_type_index_list = source_type_index_list;
		if (data_handler_obj.source_type_index_list == null)
		{
			data_handler_obj.source_type_index_list = new LinkedList<Integer>();
			int start_index = FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_MarketStart.value();
			int end_index = FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_MarketEnd.value();
			for (int source_type_index = start_index ; source_type_index < end_index ; source_type_index++)
				source_type_index_list.add(source_type_index);
		}
		return data_handler_obj;
	}
	public static FinanceRecorderDataHandlerInf get_data_handler_whole()
	{
		return get_data_handler(null);
	}

	private ArrayList<Integer> missing_csv_list = null;
	private FinanceRecorderMarketSQLClient sql_client = null;
//	private LinkedList<FinanceRecorderCmnClass.SourceTypeTimeRange> source_type_time_range_list = null;
//	private String current_csv_working_folerpath = FinanceRecorderCmnDef.BACKUP_CSV_ROOT_FOLDERPATH;

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
		if (sql_client == null)
			sql_client = new FinanceRecorderMarketSQLClient();
	}

	protected short create_finance_folder_hierarchy(String root_folderpath)
	{
		short ret = FinanceRecorderCmnDef.create_folder_if_not_exist(root_folderpath);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			FinanceRecorderCmnDef.format_error("Fail to create the root folder[%s], due to: %s", root_folderpath, FinanceRecorderCmnDef.GetErrorDescription(ret));
		return ret;
	}

	protected short parse_missing_csv()
	{
		LinkedList<String> config_line_list = new LinkedList<String>();
		short ret = FinanceRecorderCmnDef.read_config_file_lines(FinanceRecorderCmnDef.MISSING_CSV_MARKET_FILENAME, current_csv_working_folerpath, config_line_list);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
		{
			if (!FinanceRecorderCmnDef.CheckFailureNotFound(ret))
				return ret;
			else
				FinanceRecorderCmnDef.format_debug("The missing CSV file[%s] does NOT exist", FinanceRecorderCmnDef.MISSING_CSV_MARKET_FILENAME);
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
					Integer source_type_index = Integer.valueOf(missing_csv);
					missing_csv_list.add(source_type_index);
				}
			}
		}
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	public short read_from_csv(FinanceRecorderCSVHandlerMap csv_data_map)
	{
		assert source_type_index_list != null : "source_type_index_list == NULL";

		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
// Ignore the CSV file which is already in the Not Found list
		ret = parse_missing_csv();
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;

		for (Integer source_type_index : source_type_index_list)
		{
			FinanceRecorderCSVHandler csv_reader = FinanceRecorderCSVHandler.get_csv_reader(FinanceRecorderMarketDataHandler.get_csv_filepath(current_csv_working_folerpath, source_type_index));
			if (csv_reader == null)
			{
				if (!is_operation_continue())
				{
//Check this missing CSV exist in the Not Found list
					if (missing_csv_list != null)
					{
						if (missing_csv_list.indexOf(source_type_index) != -1)
						{
							FinanceRecorderCmnDef.format_debug("CSV[%d] already in the Not-Found list", source_type_index);
							continue;
						}
					}
					FinanceRecorderCmnDef.error(String.format("CSV NOT Found [%s:%d]", source_type_index));
					return FinanceRecorderCmnDef.RET_FAILURE_NOT_FOUND;
				}
				else
					continue;
			}
			ret = csv_reader.read();
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				return ret;
			csv_data_map.put(FinanceRecorderCmnDef.get_source_key(source_type_index), csv_reader);
		}
		return ret;
	}

	public short write_into_sql(final FinanceRecorderCSVHandlerMap csv_data_map)
	{
		assert source_type_index_list != null : "source_type_index_list == NULL";

		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
// Establish the connection to the MySQL and create the database if not exist
//		FinanceRecorderMarketSQLClient sql_client = new FinanceRecorderMarketSQLClient();
		ret = sql_client.try_connect_mysql();
		if (FinanceRecorderCmnDef.CheckFailure(ret))
		{
			if (connect_mysql_can_continue(ret))
			{
				FinanceRecorderCmnDef.format_warn("Try to create the database: %s", FinanceRecorderCmnDef.SQL_MARKET_DATABASE_NAME);
				ret = sql_client.create_database();
				if (FinanceRecorderCmnDef.CheckFailure(ret))
					return ret;
// It's required to re-connect the database after creating it
				ret = sql_client.try_connect_mysql();
				if (FinanceRecorderCmnDef.CheckFailure(ret))
					return ret;
			}
			else
				return ret;
		}
OUT:
		for (Integer source_type_index : source_type_index_list)
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
			{
				if (operation_can_continue(ret))
					continue;
				else
					break OUT;
			}
// Write the data into MySQL database
			FinanceRecorderCSVHandler csv_reader = csv_data_map.get(source_key);
			ret = sql_client.insert_data(source_type_index, csv_reader);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				break OUT;
		}
// Destroy the connection to the MySQL
		sql_client.disconnect_mysql();
		return ret;
	}

	public short transfrom_csv_to_sql()
	{
		assert source_type_index_list != null : "source_type_index_list == NULL";

		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		if (is_operation_continue())
		{
// Ignore the CSV file which is already in the Not Found list
			ret = parse_missing_csv();
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				return ret;
		}
// Establish the connection to the MySQL and create the database if not exist
//		FinanceRecorderMarketSQLClient sql_client = new FinanceRecorderMarketSQLClient();
		ret = sql_client.try_connect_mysql();
		if (FinanceRecorderCmnDef.CheckFailure(ret))
		{
			if (connect_mysql_can_continue(ret))
			{
				FinanceRecorderCmnDef.format_warn("Try to create the database: %s", FinanceRecorderCmnDef.SQL_MARKET_DATABASE_NAME);
				ret = sql_client.create_database();
// Should NOT fail in this condition......
				if (FinanceRecorderCmnDef.CheckFailure(ret))
					return ret;
// It's required to re-connect the database after creating it
				ret = sql_client.try_connect_mysql();
				if (FinanceRecorderCmnDef.CheckFailure(ret))
					return ret;
			}
			else
				return ret;
		}
OUT:
// For each source type
		for (Integer source_type_index : source_type_index_list)
		{
// Read data from CSV
			FinanceRecorderCSVHandler csv_reader = FinanceRecorderCSVHandler.get_csv_reader(FinanceRecorderMarketDataHandler.get_csv_filepath(current_csv_working_folerpath, source_type_index));
			if (csv_reader == null)
			{
				FinanceRecorderCmnDef.error(String.format("CSV NOT Found [%s:%d]", source_type_index));
				if (!is_operation_continue())
				{
//Check this missing CSV exist in the Not Found list
					if (missing_csv_list != null)
					{
						if (missing_csv_list.indexOf(source_type_index) != -1)
						{
							FinanceRecorderCmnDef.format_debug("CSV[%d] already in the Not-Found list", source_type_index);
							continue;
						}
					}
					return FinanceRecorderCmnDef.RET_FAILURE_NOT_FOUND;
				}
				else
					continue;
			}
			ret = csv_reader.read();
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				return ret;
// Create MySQL table
			ret = sql_client.create_table(source_type_index);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
			{
				if (operation_can_continue(ret))
					continue;
				else
					break OUT;
			}
// Write the data into MySQL database
			ret = sql_client.insert_data(source_type_index, csv_reader);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				break OUT;
		}
// Destroy the connection to the MySQL
		sql_client.disconnect_mysql();
		return ret;
	}

	public short read_from_sql(FinanceRecorderCmnClass.QuerySet query_set, FinanceRecorderCmnClass.FinanceTimeRange finance_time_range, FinanceRecorderCmnClass.ResultSetMap result_set_map)
	{
		assert source_type_index_list != null : "source_type_index_list == NULL";

// CAUTION: The data set in the reslt_set variable should be added before calling this function 
// Set the mapping table of reading the specific CSV files and writing correct SQL database
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		FinanceRecorderCmnDef.ResultSetDataUnit data_unit = result_set_map.get_data_unit();
// Establish the connection to the MySQL
//		FinanceRecorderMarketSQLClient sql_client = new FinanceRecorderMarketSQLClient();
		ret = sql_client.try_connect_mysql();
		if (FinanceRecorderCmnDef.CheckFailure(ret))
		{
			if (connect_mysql_can_continue(ret))
			{
// No data to read
					FinanceRecorderCmnDef.warn(String.format("No data to read from %s", FinanceRecorderCmnDef.SQL_MARKET_DATABASE_NAME));
					return FinanceRecorderCmnDef.RET_SUCCESS;
			}
			else
				return ret;
		}
		FinanceRecorderCmnClass.ResultSet result_set = null;
OUT:
		switch (data_unit)
		{
		case ResultSetDataUnit_NoSourceType:
		{
			result_set = new FinanceRecorderCmnClass.ResultSet();
// Add query set
//			for (FinanceRecorderCmnClass.SourceTypeTimeRange source_type_time_range : source_type_time_range_list)
			for (Integer source_type_index : query_set.get_source_type_index_list())
			{
//				int source_type_index = source_type_time_range.get_source_type_index();
				ret = result_set.add_set(source_type_index, query_set.get_field_index_list(source_type_index));
				if (FinanceRecorderCmnDef.CheckFailure(ret))
					break OUT;
			}
// Query data from each source type
			for (Integer source_type_index : source_type_index_list)
			{
				ret = sql_client.select_data(source_type_index, finance_time_range, result_set);
				if (FinanceRecorderCmnDef.CheckFailure(ret))
				{
					if (operation_can_continue(ret))
						continue;
					else
						break OUT;
				}
			}
// Keep track of the data in the designated data structure
			ret = result_set_map.register_result_set(FinanceRecorderCmnDef.NO_SOURCE_TYPE_MARKET_SOURCE_KEY_VALUE, result_set);
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
				ret = sql_client.select_data(source_type_index, finance_time_range, result_set);
				if (FinanceRecorderCmnDef.CheckFailure(ret))
				{
					if (operation_can_continue(ret))
						continue;
					else
						break OUT;
				}
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
	public short read_from_sql(FinanceRecorderCmnClass.FinanceTimeRange finance_time_range, FinanceRecorderCmnClass.ResultSetMap result_set_map)
	{
		return read_from_sql(whole_field_query_set, finance_time_range, result_set_map);
	}

	public short write_into_csv(FinanceRecorderCmnClass.ResultSetMap result_set_map)
	{
		assert source_type_index_list != null : "source_type_index_list == NULL";

		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
// Create the finance folder hierarchy for writing CSV
		ret = create_finance_folder_hierarchy(current_csv_working_folerpath);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;

		FinanceRecorderCmnDef.ResultSetDataUnit data_unit = result_set_map.get_data_unit();
OUT:
		switch (data_unit)
		{
		case ResultSetDataUnit_NoSourceType:
		{
			FinanceRecorderCmnClass.ResultSet result_set = null;
			try
			{
				result_set = result_set_map.lookup_result_set(FinanceRecorderCmnDef.NO_SOURCE_TYPE_MARKET_SOURCE_KEY_VALUE);
			}
			catch (IllegalArgumentException e)
			{
				FinanceRecorderCmnDef.format_error("No data found in the source key: %d", FinanceRecorderCmnDef.NO_SOURCE_TYPE_MARKET_SOURCE_KEY_VALUE);
				ret = FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
				break OUT;
			}
			for (Integer source_type_index : source_type_index_list)
			{
				FinanceRecorderCSVHandler csv_writer = FinanceRecorderCSVHandler.get_csv_writer(FinanceRecorderMarketDataHandler.get_csv_filepath(current_csv_working_folerpath, source_type_index));
//Assemble the data and write into CSV
				ArrayList<String> csv_data_list = result_set.to_string_array(source_type_index);
				csv_writer.set_write_data(csv_data_list);
				ret = csv_writer.write();
				if (FinanceRecorderCmnDef.CheckFailure(ret))
					break OUT;
			}
		}
		break;
		case ResultSetDataUnit_SourceType:
		{
			for (Integer source_type_index : source_type_index_list)
			{
				int source_key = FinanceRecorderCmnDef.get_source_key(source_type_index);
				FinanceRecorderCmnClass.ResultSet result_set = null;
				try
				{
					result_set = result_set_map.lookup_result_set(source_key);
				}
				catch (IllegalArgumentException e)
				{
					FinanceRecorderCmnDef.format_error("No data found in the source key: %d", source_key);
					ret = FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
					break OUT;
				}
				FinanceRecorderCSVHandler csv_writer = FinanceRecorderCSVHandler.get_csv_writer(FinanceRecorderMarketDataHandler.get_csv_filepath(current_csv_working_folerpath, source_type_index));
//Assemble the data and write into CSV
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

		if (!query_set.is_add_query_done())
		{
			FinanceRecorderCmnDef.error("The add-done flag in query_set is NOT true");
			return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
		}

		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
// Create the finance folder hierarchy for writing CSV
		ret = create_finance_folder_hierarchy(current_csv_working_folerpath);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;
// CAUTION: The data set in the reslt_set variable should be added before calling this function 
// Set the mapping table of reading the specific CSV files and writing correct SQL database
		FinanceRecorderCmnClass.ResultSet result_set = new FinanceRecorderCmnClass.ResultSet();
// Add query set
		for (Integer source_type_index : source_type_index_list)
		{
			ret = result_set.add_set(source_type_index, query_set.get_field_index_list(source_type_index));
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				return ret;
		}
// Establish the connection to the MySQL
//		FinanceRecorderMarketSQLClient sql_client = new FinanceRecorderMarketSQLClient();
		ret = sql_client.try_connect_mysql();
		if (FinanceRecorderCmnDef.CheckFailure(ret))
		{
			if (connect_mysql_can_continue(ret))
			{
// No data to read
					FinanceRecorderCmnDef.warn(String.format("No data to read from %s", FinanceRecorderCmnDef.SQL_MARKET_DATABASE_NAME));
					return FinanceRecorderCmnDef.RET_SUCCESS;
			}
			else
				return ret;
		}
OUT:
		for (Integer source_type_index : source_type_index_list)
		{
// Query data from each source type
			ret = sql_client.select_data(source_type_index, finance_time_range, result_set);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
			{
				if (operation_can_continue(ret)) // No data to read
					continue OUT;
				else
					break OUT;
			}
			FinanceRecorderCSVHandler csv_writer = FinanceRecorderCSVHandler.get_csv_writer(FinanceRecorderMarketDataHandler.get_csv_filepath(current_csv_working_folerpath, source_type_index));
//Assemble the data and write into CSV
			ArrayList<String> csv_data_list = result_set.to_string_array(source_type_index);
			csv_writer.set_write_data(csv_data_list);
			ret = csv_writer.write();
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				break OUT;
		}
// Destroy the connection to the MySQL
		sql_client.disconnect_mysql();
		return ret;
	}
	public short transfrom_whole_sql_to_csv(FinanceRecorderCmnClass.FinanceTimeRange finance_time_range)
	{
		return transfrom_sql_to_csv(whole_field_query_set, finance_time_range);
	}

	public short delete_sql_by_source_type()
	{
		assert source_type_index_list != null : "source_type_index_list == NULL";

		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
// Establish the connection to the MySQL
//		FinanceRecorderMarketSQLClient sql_client = new FinanceRecorderMarketSQLClient();
		ret = sql_client.try_connect_mysql();
		if (FinanceRecorderCmnDef.CheckFailure(ret))
		{
			if (connect_mysql_can_continue(ret))
			{
				FinanceRecorderCmnDef.warn(String.format("Database[%s] does NOT exist", FinanceRecorderCmnDef.SQL_MARKET_DATABASE_NAME));
				return FinanceRecorderCmnDef.RET_SUCCESS;
			}
			else
				return ret;
		}
// Delete each table
OUT:
		for (Integer source_type_index : source_type_index_list)
		{
			ret = sql_client.delete_table(source_type_index);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
			{
				if (operation_can_continue(ret))
					continue;
				else
					break OUT;
			}
		}
// Destroy the connection to the MySQL
		sql_client.disconnect_mysql();
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	public short delete_sql_by_company() // Only useful in stock mode
	{
		throw new IllegalStateException("Logfile cannot be read-only");
	} 

	public short delete_sql_by_source_type_and_company() // Only useful in stock mode
	{
		throw new IllegalStateException("Logfile cannot be read-only");
	} 

	public short cleanup_sql()
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
// Establish the connection to the MySQL
//		FinanceRecorderMarketSQLClient sql_client = new FinanceRecorderMarketSQLClient();
		ret = sql_client.try_connect_mysql();
		if (FinanceRecorderCmnDef.CheckFailure(ret))
		{
			if (connect_mysql_can_continue(ret))
			{
				FinanceRecorderCmnDef.warn(String.format("Database[%s] does NOT exist", FinanceRecorderCmnDef.SQL_MARKET_DATABASE_NAME));
				return FinanceRecorderCmnDef.RET_SUCCESS;
			}
			else
				return ret;
		}
// Delete the database
		ret = sql_client.delete_database();
		if (FinanceRecorderCmnDef.CheckFailure(ret))
		{
			if (operation_can_continue(ret))
			{
				ret = FinanceRecorderCmnDef.RET_FAILURE_NOT_FOUND;
				String errmsg = String.format("Fails to delete database[%s], due to: %s", FinanceRecorderCmnDef.SQL_MARKET_DATABASE_NAME, FinanceRecorderCmnDef.GetErrorDescription(ret));
				FinanceRecorderCmnDef.error(errmsg);
			}
		}
// Destroy the connection to the MySQL
		sql_client.disconnect_mysql();
		return ret;
	}

	public short check_sql_exist(ArrayList<String> not_exist_list)
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
// Establish the connection to the MySQL
//		FinanceRecorderMarketSQLClient sql_client = new FinanceRecorderMarketSQLClient();
		ret = sql_client.try_connect_mysql();
		if (FinanceRecorderCmnDef.CheckFailure(ret))
		{
			if (FinanceRecorderCmnDef.CheckMySQLFailureUnknownDatabase(ret))
			{
				FinanceRecorderCmnDef.format_warn("The database[%s] does NOT exist, add all tables in the Not-Found List......", FinanceRecorderCmnDef.SQL_MARKET_DATABASE_NAME);
				LinkedList<Integer> all_source_type_index_list = FinanceRecorderCmnDef.get_all_source_type_index_list();
				for (Integer source_type_index : all_source_type_index_list)
					not_exist_list.add(String.format("%d", source_type_index));
				return FinanceRecorderCmnDef.RET_SUCCESS;
			}
			else
				return ret;
		}
// Check MySQL table exist
		for (Integer source_type_index : source_type_index_list)
		{
			ret = sql_client.check_table_exist(source_type_index);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
			{
				if (FinanceRecorderCmnDef.CheckFailureNotFound(ret))
				{
					not_exist_list.add(String.format("%d", source_type_index));
					ret = FinanceRecorderCmnDef.RET_SUCCESS;
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
