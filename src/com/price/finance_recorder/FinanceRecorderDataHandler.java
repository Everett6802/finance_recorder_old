package com.price.finance_recorder;

import java.sql.*;
import java.text.*;
import java.util.*;
//import java.util.regex.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FinanceRecorderDataHandler extends FinanceRecorderCmnBase implements FinanceRecorderCmnDef.FinanceObserverInf
{
	private static final String CSV_FILE_FOLDER = "/var/tmp/finance";
//	private static final String DATE_FORMAT_STRING = "yyyy-MM";
//	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_STRING);
	private static final boolean IgnoreErrorIfCSVNotExist = true;

	private int finace_data_type_index;
	private FinanceRecorderCSVReader csv_reader = null;
	private FinanceRecorderSQLClient sql_client = null;
	private FinanceRecorderSQLClient sql_client_diff = null;
	private HashMap<Integer, LinkedList<Integer>> finance_source_time_range_table = new HashMap<Integer, LinkedList<Integer>>();

	private FinanceRecorderCmnClass.TimeRangeCfg database_time_range_cfg = null;

	public FinanceRecorderDataHandler(FinanceRecorderCmnDef.FinanceSourceType finance_data_type)
	{
		finace_data_type_index = finance_data_type.ordinal();
		csv_reader = new FinanceRecorderCSVReader(this);
		sql_client = new FinanceRecorderSQLClient(finance_data_type, this);
	}

	private short set_mapping_time_range(FinanceRecorderCmnClass.TimeRangeCfg time_range_cfg)
	{
		int[] time_list = FinanceRecorderCmnClass.TimeRangeCfg.get_start_and_end_month_value_range(time_range_cfg);
		assert time_list != null : "time_list should NOT be NULL";
		int year_start = time_list[0]; 
		int month_start = time_list[1];
		int year_end = time_list[2];
		int month_end = time_list[3];
		int year_cur = year_start;
		int month_cur = month_start;
		do 
		{
			if (!finance_source_time_range_table.containsKey(year_cur))
				finance_source_time_range_table.put(year_cur, new LinkedList<Integer>());
			LinkedList<Integer> entry = (LinkedList<Integer>)finance_source_time_range_table.get(year_cur);
			entry.addLast(month_cur);
			if (year_cur == year_end && month_cur == month_end)
				break;
			if (month_cur == 12)
			{
				year_cur++;
				month_cur = 1;
			}
			else
				month_cur++;
		}while(true);

		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	final String get_description(){return FinanceRecorderCmnDef.FINANCE_DATABASE_DESCRIPTION_LIST[finace_data_type_index];}

	short write_to_sql(FinanceRecorderCmnClass.TimeRangeCfg time_range_cfg, FinanceRecorderCmnDef.DatabaseCreateThreadType database_create_thread_type, FinanceRecorderCmnDef.DatabaseEnableBatchType database_enable_batch_type)
	{
// Set the mapping table of reading the specific CSV files and writing correct SQL database
		short ret = set_mapping_time_range(time_range_cfg);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;

		String csv_filepath = null;
// Establish the connection to the MySQL and create the database if not exist 
		ret = sql_client.try_connect_mysql(
				FinanceRecorderCmnDef.FINANCE_DATA_NAME_LIST[finace_data_type_index], 
				FinanceRecorderCmnDef.DatabaseNotExistIngoreType.DatabaseNotExistIngore_Yes,
				database_create_thread_type
			);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;
OUT:
		for (Map.Entry<Integer, LinkedList<Integer>> entry : finance_source_time_range_table.entrySet())
		{
			LinkedList<String> data_list = new LinkedList<String>();
			int year = entry.getKey();
			LinkedList<Integer> month_list = (LinkedList<Integer>)entry.getValue();
			for (int month : month_list)
			{
// Read the data from CSV file
				csv_filepath = String.format("%s/%s_%04d%02d.csv", FinanceRecorderCmnDef.DATA_FOLDER_NAME, FinanceRecorderCmnDef.FINANCE_DATA_NAME_LIST[finace_data_type_index], year, month);
//				FinanceRecorderCmnDef.format_debug("Try to read the CSV: %s", csv_filepath);
				ret = csv_reader.initialize(csv_filepath);
				if (FinanceRecorderCmnDef.CheckFailure(ret))
				{
					if (IgnoreErrorIfCSVNotExist && FinanceRecorderCmnDef.CheckFailureNotFound(ret))
					{
						FinanceRecorderCmnDef.format_warn("The CSV[%s] does NOT exist, just skip this error......", csv_filepath);
						csv_reader.deinitialize();
						ret = FinanceRecorderCmnDef.RET_SUCCESS;
						continue;
					}
					break OUT;
				}
				ret = csv_reader.read(data_list);
				csv_reader.deinitialize();
				if (FinanceRecorderCmnDef.CheckFailure(ret))
					break OUT;
			}
// Check if the data exist
			if (!data_list.isEmpty())
			{
// Create the table
				String table_name = String.format("year%04d", year);
				ret = sql_client.create_table(table_name, FinanceRecorderCmnDef.FINANCE_DATA_SQL_FIELD_LIST[finace_data_type_index]);
				if (FinanceRecorderCmnDef.CheckFailure(ret))
					break OUT;
// Write the data into MySQL database
				if (database_enable_batch_type == FinanceRecorderCmnDef.DatabaseEnableBatchType.DatabaseEnableBatch_Yes)
					ret = sql_client.insert_data_batch(table_name, data_list);
				else
					ret = sql_client.insert_data(table_name, data_list);
				if (FinanceRecorderCmnDef.CheckFailure(ret))
					break OUT;
				data_list.clear();
			}
		}
// Destroy the connection to the MySQL
		sql_client.disconnect_mysql();

		return ret;
	}

//	read_from_sql(FinanceRecorderCmnClass.TimeRangeCfg time_range_cfg, String cmd_table_field, LinkedList<String> data_list)
	short read_from_sql(FinanceRecorderCmnClass.TimeRangeCfg time_range_cfg, String cmd_table_field, FinanceRecorderCmnClass.ResultSet result_set)
	{
// Set the mapping table of reading the specific CSV files and writing correct SQL database
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;

		String csv_filepath = null;
		int[] time_list = null;
		if (time_range_cfg.is_month_type())
			time_list = FinanceRecorderCmnClass.TimeRangeCfg.get_start_and_end_month_value_range(time_range_cfg);
		else
			time_list = FinanceRecorderCmnClass.TimeRangeCfg.get_start_and_end_date_value_range(time_range_cfg);
		if (time_list == null)
		{
			FinanceRecorderCmnDef.format_error("Incorrect time format: %s", time_range_cfg.toString());
			return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
		}
		int start_year = time_list[0]; 
		int end_year = time_range_cfg.is_month_type() ? time_list[2] : time_list[3];

// Establish the connection to the MySQL and create the database if not exist 
		ret = sql_client.try_connect_mysql(
			FinanceRecorderCmnDef.FINANCE_DATA_NAME_LIST[finace_data_type_index], 
			FinanceRecorderCmnDef.DatabaseNotExistIngoreType.DatabaseNotExistIngore_No,
			FinanceRecorderCmnDef.DatabaseCreateThreadType.DatabaseCreateThread_Single
		);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
		{
//			FinanceRecorderCmnDef.format_warn("The MySQL database[%d, %s] does NOT exist", finace_data_type_index, FinanceRecorderCmnDef.FINANCE_DATA_NAME_LIST[finace_data_type_index]);
			return ret;
		}

//		String time_start_str = null;
//		String time_end_str = null;
//		int old_sum = 0;
//		int new_sum;
OUT:
// Search for each table year by year
		for (int year = start_year ; year <= end_year ; year++)
		{
			String table_name = String.format("%s%d", FinanceRecorderCmnDef.MYSQL_TABLE_NAME_BASE, year);
			FinanceRecorderCmnClass.TimeRangeCfg time_range_cfg_in_year = null;
			if (year == start_year || year == end_year)
			{
				if (start_year == end_year)
					time_range_cfg_in_year = new FinanceRecorderCmnClass.TimeRangeCfg(time_range_cfg.get_start_time().toString(), time_range_cfg.get_end_time().toString());
				else if (year == start_year)
					time_range_cfg_in_year = new FinanceRecorderCmnClass.TimeRangeCfg(time_range_cfg.get_start_time().toString(), null);
				else
					time_range_cfg_in_year = new FinanceRecorderCmnClass.TimeRangeCfg(null, time_range_cfg.get_end_time().toString());
			}
			ret = sql_client.select_data(table_name, cmd_table_field, time_range_cfg_in_year, result_set);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				break OUT;
		}
// Destroy the connection to the MySQL
		sql_client.disconnect_mysql();

		return ret;
	}

	short delete_sql()
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
// Establish the connection to the MySQL 
		ret = sql_client.try_connect_mysql(
				FinanceRecorderCmnDef.FINANCE_DATA_NAME_LIST[finace_data_type_index], 
				FinanceRecorderCmnDef.DatabaseNotExistIngoreType.DatabaseNotExistIngore_No,
				FinanceRecorderCmnDef.DatabaseCreateThreadType.DatabaseCreateThread_Single
			);
		boolean unknown_database = false;
		if (FinanceRecorderCmnDef.CheckFailure(ret))
		{
			if (!FinanceRecorderCmnDef.CheckMySQLFailureUnknownDatabase(ret))
				return ret;
			unknown_database = true;
//			FinanceRecorderCmnDef.format_warn("The database[type index: %d] does NOT exist, just skip this error", finace_data_type_index);
			ret = FinanceRecorderCmnDef.RET_SUCCESS;
		}
		if (!unknown_database)
			ret  = sql_client.delete_database();
// Destroy the connection to the MySQL
		sql_client.disconnect_mysql();
		return ret;
	}

	private short get_database_start_and_end_year(int[] year_array)
	{
		if (year_array.length != 2)
		{
			FinanceRecorderCmnDef.format_error("Unexpected array size: %d", year_array.length);
			return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
		}
// Search for the table name list in the database
		LinkedList<String> table_name_list = new LinkedList<String>();
		short ret = sql_client.get_table_name_list(table_name_list);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;
// Find the year from the table name
		int start_year = 9999;
		int end_year = 0;
		for (String table_name : table_name_list)
		{
			Pattern pattern = Pattern.compile("year([\\d]{4})");
			Matcher matcher = pattern.matcher(table_name);
			if (!matcher.find())
			{
				FinanceRecorderCmnDef.format_error("Incorrect table name format: %s", table_name);
				return FinanceRecorderCmnDef.RET_FAILURE_UNEXPECTED_VALUE;
			}
			int year = Integer.valueOf(matcher.group(1));
			if (year > end_year)
				end_year = year;
			if (year < start_year)
				start_year = year;
		}
		year_array[0] = start_year;
		year_array[1] = end_year;
		return ret;
	}

	public short find_database_time_range()
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
// Establish the connection to the MySQL and create the database if not exist 
		ret = sql_client.try_connect_mysql(
			FinanceRecorderCmnDef.FINANCE_DATA_NAME_LIST[finace_data_type_index], 
			FinanceRecorderCmnDef.DatabaseNotExistIngoreType.DatabaseNotExistIngore_No,
			FinanceRecorderCmnDef.DatabaseCreateThreadType.DatabaseCreateThread_Single
		);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
		{
//			FinanceRecorderCmnDef.format_warn("The MySQL database[%d, %s] does NOT exist", finace_data_type_index, FinanceRecorderCmnDef.FINANCE_DATA_NAME_LIST[finace_data_type_index]);
			return ret;
		}
// Search for the table name list in the database
		int[] year_array = new int[2];
		ret = get_database_start_and_end_year(year_array);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;

		int start_year = year_array[0];
		int end_year = year_array[1];
		FinanceRecorderCmnDef.format_debug("Search for the time range from year %d-%d in %s", start_year, end_year, FinanceRecorderCmnDef.FINANCE_DATABASE_DESCRIPTION_LIST[finace_data_type_index]);
		String start_table_name = String.format("year%04d", start_year);
		String end_table_name = String.format("year%04d", end_year);
// Find the start date
//		LinkedList<String> start_date_list = new LinkedList<String>();
		FinanceRecorderCmnClass.ResultSet start_date_result_set = new FinanceRecorderCmnClass.ResultSet();
		ret = sql_client.select_data(start_table_name, FinanceRecorderCmnDef.FINANCE_DATA_SQL_FIELD_DEFINITION_LIST[finace_data_type_index][0], start_date_result_set);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
		{
			FinanceRecorderCmnDef.format_error("Fail to find the start date in %s:%s", FinanceRecorderCmnDef.FINANCE_DATABASE_DESCRIPTION_LIST[finace_data_type_index], start_table_name);
			return ret;
		}
//		if (start_date_list.isEmpty())
		if (start_date_result_set.is_empty())
		{
			FinanceRecorderCmnDef.format_error("Fail to find any date in %s:%s", FinanceRecorderCmnDef.FINANCE_DATABASE_DESCRIPTION_LIST[finace_data_type_index], start_table_name);
			return ret;
		}
// Find the end date
//		LinkedList<String> end_date_list = new LinkedList<String>();
		FinanceRecorderCmnClass.ResultSet end_date_result_set = new FinanceRecorderCmnClass.ResultSet();
		ret = sql_client.select_data(end_table_name, FinanceRecorderCmnDef.FINANCE_DATA_SQL_FIELD_DEFINITION_LIST[finace_data_type_index][0], end_date_result_set);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
		{
			FinanceRecorderCmnDef.format_error("Fail to find the start date in %s:%s", FinanceRecorderCmnDef.FINANCE_DATABASE_DESCRIPTION_LIST[finace_data_type_index], start_table_name);
			return ret;
		}
//		if (end_date_list.isEmpty())
		if (end_date_result_set.is_empty())
		{
			FinanceRecorderCmnDef.format_error("Fail to find any date in %s:%s", FinanceRecorderCmnDef.FINANCE_DATABASE_DESCRIPTION_LIST[finace_data_type_index], end_table_name);
			return ret;
		}
		String start_date_str = start_date_result_set.get_date_array().get_index(0);
		String end_date_str = end_date_result_set.get_date_array().get_index(end_date_result_set.get_date_array().get_size() - 1);
		database_time_range_cfg = new FinanceRecorderCmnClass.TimeRangeCfg(start_date_str, end_date_str);
// Destroy the connection to the MySQL
		sql_client.disconnect_mysql();
		return ret;
	}

//	public short find_database_time_range_list(LinkedList<String> date_list)
	public short find_database_time_range_list(FinanceRecorderCmnClass.ResultSet result_set)
	{
		if (database_time_range_cfg == null)
		{
			FinanceRecorderCmnDef.format_error("Time range should be found first in %s", FinanceRecorderCmnDef.FINANCE_DATABASE_DESCRIPTION_LIST[finace_data_type_index]);
			return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
		}
		int[] time_list = FinanceRecorderCmnClass.TimeRangeCfg.get_start_and_end_month_value_range(database_time_range_cfg);
		if (time_list == null)
		{
			FinanceRecorderCmnDef.format_error("Incorrect time format: %s", database_time_range_cfg.toString());
			return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
		}
		int year_start = time_list[0]; 
		int year_end = time_list[2];
//		return find_database_time_range_list(year_start, year_end, date_list);
		return find_database_time_range_list(year_start, year_end, result_set);
	}

//	public short find_database_time_range_list(int start_year, int end_year, LinkedList<String> date_list)
	public short find_database_time_range_list(int start_year, int end_year, FinanceRecorderCmnClass.ResultSet result_set)
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
// Establish the connection to the MySQL and create the database if not exist 
		ret = sql_client.try_connect_mysql(
			FinanceRecorderCmnDef.FINANCE_DATA_NAME_LIST[finace_data_type_index], 
			FinanceRecorderCmnDef.DatabaseNotExistIngoreType.DatabaseNotExistIngore_No,
			FinanceRecorderCmnDef.DatabaseCreateThreadType.DatabaseCreateThread_Single
		);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
		{
//			FinanceRecorderCmnDef.format_warn("The MySQL database[%d, %s] does NOT exist", finace_data_type_index, FinanceRecorderCmnDef.FINANCE_DATA_NAME_LIST[finace_data_type_index]);
			return ret;
		}

		FinanceRecorderCmnDef.format_debug("Search for the time list from year %d-%d in %s", start_year, end_year, FinanceRecorderCmnDef.FINANCE_DATABASE_DESCRIPTION_LIST[finace_data_type_index]);
//		LinkedList<String> date_list = new LinkedList<String>();
		String table_name;
		for (int year = start_year ; year <= end_year ; year++)
		{
// Find the date in each table
			table_name = String.format("year%04d", year);
//			ret = sql_client.select_data(table_name, FinanceRecorderCmnDef.FINANCE_DATA_SQL_FIELD_DEFINITION_LIST[finace_data_type_index][0], date_list);
			ret = sql_client.select_data(table_name, FinanceRecorderCmnDef.FINANCE_DATA_SQL_FIELD_DEFINITION_LIST[finace_data_type_index][0], result_set);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
			{
				FinanceRecorderCmnDef.format_error("Fail to find the date list in %s:%s", FinanceRecorderCmnDef.FINANCE_DATABASE_DESCRIPTION_LIST[finace_data_type_index], table_name);
				return ret;
			}
		}
//		if (date_list.isEmpty())
		if (result_set.is_empty())
		{
			FinanceRecorderCmnDef.format_error("Fail to find any date in %s", FinanceRecorderCmnDef.FINANCE_DATABASE_DESCRIPTION_LIST[finace_data_type_index]);
			return ret;
		}

// Destroy the connection to the MySQL
		sql_client.disconnect_mysql();
		return ret;
	}

	short get_database_start_date(StringBuilder database_start_date_builder)
	{
		if (database_time_range_cfg == null)
		{
			FinanceRecorderCmnDef.format_error("Time range should be found first in %s", FinanceRecorderCmnDef.FINANCE_DATABASE_DESCRIPTION_LIST[finace_data_type_index]);
			return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
		}

		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		database_start_date_builder.append(database_time_range_cfg.get_start_time_str());
		return ret;
	}

	short get_database_time_range_str(StringBuilder time_range_str_builder)
	{
		if (database_time_range_cfg == null)
		{
			FinanceRecorderCmnDef.format_error("Time range should be found first in %s", FinanceRecorderCmnDef.FINANCE_DATABASE_DESCRIPTION_LIST[finace_data_type_index]);
			return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
		}

		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		time_range_str_builder.append(String.format("%s %s", FinanceRecorderCmnDef.FINANCE_DATABASE_DESCRIPTION_LIST[finace_data_type_index], database_time_range_cfg.toString()));
//		System.out.printf("%s: %s\n", FinanceRecorderCmnDef.FINANCE_DATABASE_DESCRIPTION_LIST[finace_data_type_index], database_time_range_cfg.toString());
		return ret;
	}

	@Override
	public short notify(short type) 
	{
		return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
	}
}
