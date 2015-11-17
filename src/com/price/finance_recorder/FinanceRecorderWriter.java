package com.price.finance_recorder;

import java.sql.*;
import java.text.*;
import java.util.*;
//import java.util.regex.*;


public class FinanceRecorderWriter extends FinanceRecorderCmnBase implements FinanceRecorderCmnDef.FinanceObserverInf
{
	private static final String CSV_FILE_FOLDER = "/var/tmp/finance";
	private static final String DATE_FORMAT_STRING = "yyyy-MM";
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_STRING);
	private static final boolean IgnoreErrorIfCSVNotExist = true;

	private int finace_data_type_index;
	private FinanceRecorderCSVReader csv_reader = null;
	private FinanceRecorderSQLClient sql_client = null;
	private FinanceRecorderSQLClient sql_client_diff = null;
	private HashMap<Integer, LinkedList<Integer>> finance_source_time_range_table = new HashMap<Integer, LinkedList<Integer>>();

	public FinanceRecorderWriter(FinanceRecorderCmnDef.FinanceDataType finance_data_type)
	{
		finace_data_type_index = finance_data_type.ordinal();
		csv_reader = new FinanceRecorderCSVReader(this);
		sql_client = new FinanceRecorderSQLClient(finance_data_type, this);
	}

//	private void set_mapping_today()
//	{
//		java.util.Date date_today = new java.util.Date();
//		LinkedList entry = new LinkedList();
//		entry.add(date_today.getMonth());
//		finance_source_time_range_table.put(date_today.getYear(), entry);
//	}

	private short set_mapping_time_range(FinanceRecorderCmnDef.TimeRangeCfg time_range_cfg)
	{
    	int[] time_list = FinanceRecorderCmnDef.get_start_and_end_month_value_range(time_range_cfg);
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

	final String get_description(){return FinanceRecorderCmnDef.FINANCE_DATA_DESCRIPTION_LIST[finace_data_type_index];}

	short write_to_sql(FinanceRecorderCmnDef.TimeRangeCfg time_range_cfg, FinanceRecorderCmnDef.DatabaseCreateThreadType database_create_thread_type, FinanceRecorderCmnDef.DatabaseEnableBatchType database_enable_batch_type)
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

	short read_from_sql(FinanceRecorderCmnDef.TimeRangeCfg time_range_cfg, LinkedList<String> data_list)
	{
// Set the mapping table of reading the specific CSV files and writing correct SQL database
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;

		String csv_filepath = null;
		int[] time_list = FinanceRecorderCmnDef.get_start_and_end_month_value_range(time_range_cfg);
		if (time_list == null)
		{
			FinanceRecorderCmnDef.format_error("Incorrect time format: %s", time_range_cfg.toString());
			return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
		}
		int year_start = time_list[0]; 
		int month_start = time_list[1];
		int year_end = time_list[2];
		int month_end = time_list[3];

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

		String time_start_str = null;
		String time_end_str = null;
		int old_sum = 0;
		int new_sum;
OUT:
		for (int year = year_start ; year <= year_end ; year++)
		{
			if (year == year_start)
				time_start_str = String.format("%04d-%02d", year, month_start);
			else
				time_start_str = null;
			if (year == year_end)
				time_end_str = String.format("%04d-%02d", year, month_end);
			else
				time_end_str = null;
			String table_name = String.format("year%04d", year);
			ret = sql_client.select_data(table_name, new FinanceRecorderCmnDef.TimeRangeCfg(time_start_str, time_end_str), data_list);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				break OUT;
			new_sum = data_list.size();
			FinanceRecorderCmnDef.format_debug("Got %d data in %s.%s[%s:%s]", new_sum - old_sum, FinanceRecorderCmnDef.FINANCE_DATA_NAME_LIST[finace_data_type_index], table_name, time_start_str, time_end_str);
			old_sum = new_sum;
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
			FinanceRecorderCmnDef.format_warn("The database[type index: %d] does NOT exist, just skip this error", finace_data_type_index);
			ret = FinanceRecorderCmnDef.RET_SUCCESS;
		}
		if (!unknown_database)
			ret  = sql_client.delete_database();
// Destroy the connection to the MySQL
		sql_client.disconnect_mysql();
		return ret;
	}

//	short write_to_sql(String month_begin_str)
//	{
//		java.util.Date date_today = new java.util.Date();
//		String month_end_str = String.format("%04d-%02d", date_today.getYear(), date_today.getMonth());
//		return write_to_sql(month_begin_str, month_end_str);
//	}
//
//	short write_to_sql()
//	{
//		java.util.Date date_today = new java.util.Date();
//		String month_begin_str = String.format("%04d-%02d", date_today.getYear(), date_today.getMonth());
//		return write_to_sql(month_begin_str);
//	}

	@Override
	public short notify(short type) 
	{
		return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
	}
}
