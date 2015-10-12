package com.price.finance_recorder;

import java.sql.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;

public class FinanceRecorderWriter extends FinanceRecorderCmnBase implements FinanceRecorderCmnDef.FinanceObserverInf
{
	private static final String CSV_FILE_FOLDER = "/var/tmp/finance";
	private static final String DATE_FORMAT_STRING = "yyyy-MM";
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_STRING);

	private int finace_data_type_index;
	private FinanceRecorderCSVReader csv_reader = null;
	private FinanceRecorderSQLClient sql_client = null;
//	private List<String> data_list = new LinkedList<String>();
	private HashMap<Integer, LinkedList<Integer>> time_range_mapping = new HashMap<Integer, LinkedList<Integer>>();

	public FinanceRecorderWriter(FinanceRecorderCmnDef.FinaceDataType finace_data_type)
	{
		finace_data_type_index = finace_data_type.ordinal();
		csv_reader = new FinanceRecorderCSVReader(this);
		sql_client = new FinanceRecorderSQLClient(this);
	}

	protected short open_csv(String csv_filename)
	{
		return csv_reader.initialize(csv_filename);
	}
	
	protected short open_sql(String datebase, String table, String sql_table_field)
	{
		return sql_client.initialize(datebase, table, sql_table_field);
	}

	protected void close_sql()
	{
		sql_client.deinitialize();
	}
	
	private void set_mapping_today()
	{
		java.util.Date date_today = new java.util.Date();
		LinkedList entry = new LinkedList();
		entry.add(date_today.getMonth());
		time_range_mapping.put(date_today.getYear(), entry);
	}

	private short set_mapping_time_range(String month_begin_str, String month_end_str)
	{
		Pattern month_pattern = Pattern.compile("([\\d]{4})-([\\d]{1,2})");
		Matcher month_begin_matcher = month_pattern.matcher(month_begin_str);
		if (!month_begin_matcher.find())
		{
			FinanceRecorderCmnDef.format_error("Incorrect time format (Begin): %s", month_begin_str);
			return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
		}
		Matcher month_end_matcher = month_pattern.matcher(month_end_str);
		if (!month_end_matcher.find())
		{
			FinanceRecorderCmnDef.format_error("Incorrect time format (End): %s", month_end_str);
			return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
		}
		int year_cur = Integer.valueOf(month_begin_matcher.group(1));
		int month_cur = Integer.valueOf(month_begin_matcher.group(2));
		int year_end = Integer.valueOf(month_end_matcher.group(1));
		int month_end = Integer.valueOf(month_end_matcher.group(2));
		do 
		{
			if (!time_range_mapping.containsKey(year_cur))
				time_range_mapping.put(year_cur, new LinkedList<Integer>());
			LinkedList<Integer> entry = (LinkedList<Integer>)time_range_mapping.get(year_cur);
			entry.push(month_cur);
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

	protected short proccess()
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;

		String csv_filepath = null;
		for (Map.Entry<Integer, LinkedList<Integer>> entry : time_range_mapping.entrySet())
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
					return ret;
				ret = csv_reader.read(data_list);
				if (FinanceRecorderCmnDef.CheckFailure(ret))
					return ret;
				csv_reader.deinitialize();
			}
// Write the data into MySQL database
			ret = sql_client.initialize(
				FinanceRecorderCmnDef.FINANCE_DATA_NAME_LIST[finace_data_type_index], 
				String.format("year%04d", year),
				FinanceRecorderCmnDef.FINANCE_DATA_SQL_FIELD_LIST[finace_data_type_index]
			);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				return ret;
			ret = sql_client.write(data_list);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				return ret;
			sql_client.deinitialize();
			data_list.clear();
		}

// Format the SQL command of inserting data
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}
	
	public short write_to_sql()
	{
// Set the mapping table of reading the specific CSV files and writing correct SQL database 
		set_mapping_today();

		return proccess();
	}

	public short write_to_sql(String month_begin_str, String month_end_str)
	{
// Set the mapping table of reading the specific CSV files and writing correct SQL database
		short ret = set_mapping_time_range(month_begin_str, month_end_str);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;
		return proccess();
	}

	@Override
	public short notify(short type) 
	{
		return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
	}
}
