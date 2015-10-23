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
	private HashMap<Integer, LinkedList<Integer>> finance_source_time_range_table = new HashMap<Integer, LinkedList<Integer>>();

	public FinanceRecorderWriter(FinanceRecorderCmnDef.FinanceDataType finance_data_type)
	{
		finace_data_type_index = finance_data_type.ordinal();
		csv_reader = new FinanceRecorderCSVReader(this);
		sql_client = new FinanceRecorderSQLClient(this);
	}

//	private void set_mapping_today()
//	{
//		java.util.Date date_today = new java.util.Date();
//		LinkedList entry = new LinkedList();
//		entry.add(date_today.getMonth());
//		finance_source_time_range_table.put(date_today.getYear(), entry);
//	}

	private short set_mapping_time_range(int year_start, int month_start, int year_end, int month_end)
	{
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

	private short proccess(FinanceRecorderCmnDef.DatabaseCreateThreadType database_create_thread_type)
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;

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
				csv_filepath =  String.format("%s/%s_%04d%02d.csv", FinanceRecorderCmnDef.DATA_FOLDER_NAME, FinanceRecorderCmnDef.FINANCE_DATA_NAME_LIST[finace_data_type_index], year, month);
//				FinanceRecorderCmnDef.format_debug("Try to read the CSV: %s", csv_filepath);
				ret = csv_reader.initialize(csv_filepath);
				if (FinanceRecorderCmnDef.CheckFailure(ret))
				{
					if (IgnoreErrorIfCSVNotExist)
					{
						if (FinanceRecorderCmnDef.CheckFailureNotFound(ret))
						{
							FinanceRecorderCmnDef.format_warn("The CSV[%s] does NOT exist, just skip this error......", csv_filepath);
							continue;
						}
					}
					return ret;
				}
				ret = csv_reader.read(data_list);
				if (FinanceRecorderCmnDef.CheckFailure(ret))
					return ret;
				csv_reader.deinitialize();
			}
// Create the table
			ret = sql_client.open_table(String.format("year%04d", year), FinanceRecorderCmnDef.FINANCE_DATA_SQL_FIELD_LIST[finace_data_type_index]);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				break OUT;
// Write the data into MySQL database
			ret = sql_client.insert_data(data_list);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				break OUT;
			data_list.clear();
		}
// Destroy the connection to the MySQL
		sql_client.disconnect_mysql();

// Format the SQL command of inserting data
		return ret;
	}

	final String get_description(){return FinanceRecorderCmnDef.FINANCE_DATA_DESCRIPTION_LIST[finace_data_type_index];}

	short write_to_sql(int year_start, int month_start, int year_end, int month_end, FinanceRecorderCmnDef.DatabaseCreateThreadType database_create_thread_type)
	{
// Set the mapping table of reading the specific CSV files and writing correct SQL database
		short ret = set_mapping_time_range(year_start, month_start, year_end, month_end);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;
		return proccess(database_create_thread_type);
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
			FinanceRecorderCmnDef.format_warn("The database[%d] does NOT exist, just skip this error", finace_data_type_index);
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
