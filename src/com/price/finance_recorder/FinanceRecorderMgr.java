package com.price.finance_recorder;

import java.io.*;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.*;


public class FinanceRecorderMgr implements FinanceRecorderCmnDef.FinanceObserverInf
{
	private static FinanceRecorderWorkdayCalendar finance_recorder_workday_calendar = FinanceRecorderWorkdayCalendar.get_instance();
	private HashMap<Integer,FinanceRecorderCmnDef.TimeRangeCfg> finance_source_time_range_table = new HashMap<Integer, FinanceRecorderCmnDef.TimeRangeCfg>();

	public short update_by_config_file(String filename)
	{
		String current_path = FinanceRecorderCmnDef.get_current_path();
		String conf_filepath = String.format("%s/%s/%s", current_path, FinanceRecorderCmnDef.CONF_FOLDERNAME, filename);
		FinanceRecorderCmnDef.debug(String.format("Check the config file[%s] exist", conf_filepath));
		File f = new File(conf_filepath);
		if (!f.exists())
		{
			FinanceRecorderCmnDef.format_error("The configration file[%s] does NOT exist", conf_filepath);
			return FinanceRecorderCmnDef.RET_FAILURE_NOT_FOUND;
		}

// Open the config file for reading
		BufferedReader br = null;
		try
		{
			FileInputStream fis = new FileInputStream(f);
			InputStreamReader isr = new InputStreamReader(fis);
			br = new BufferedReader(isr);
		}
		catch (IOException e)
		{
			FinanceRecorderCmnDef.format_error("Fails to open %s file, due to: %s", conf_filepath, e.toString());
			return FinanceRecorderCmnDef.RET_FAILURE_IO_OPERATION;
		}

		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
// Read the conf file
		try
		{
			String line = null;
			String data_source;
			String time_month_start;
			String time_month_end;
			String time_month_today = null;
OUT:
			while ((line = br.readLine()) != null)
			{
				if (line.startsWith(FinanceRecorderCmnDef.CONF_ENTRY_IGNORE_FLAG) || line.length() == 0)
				{
					FinanceRecorderCmnDef.format_debug("Ignore the entry: %s", line);
					continue;
				}
				String[] entry_arr = line.split(" ");
// Get the type of data source
				data_source = entry_arr[0];
				int finance_data_type_index = Arrays.asList(FinanceRecorderCmnDef.FINANCE_DATA_DESCRIPTION_LIST).indexOf(data_source);
				if (finance_data_type_index == -1)
				{
					FinanceRecorderCmnDef.format_error("Unknown data source type[%s] in config file", data_source);
					ret = FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
					break OUT;
				}
// Get the time of start time
				if (entry_arr.length >= 2)
					time_month_start = entry_arr[1];
				else
				{
					if (time_month_today == null)
						time_month_today = FinanceRecorderCmnDef.get_time_month_today();
					time_month_start = time_month_today;
				}
				if (FinanceRecorderCmnDef.get_month_value_matcher(time_month_start) == null)
				{
					FinanceRecorderCmnDef.format_error("Incorrect start time format[%s] in config file", time_month_start);
					ret = FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
					break OUT;
				}
// Get the time of start time
				if (entry_arr.length >= 3)
					time_month_end = entry_arr[2];
				else
				{
					if (time_month_today == null)
						time_month_today = FinanceRecorderCmnDef.get_time_month_today();
					time_month_end = time_month_today;
				}
				if (FinanceRecorderCmnDef.get_month_value_matcher(time_month_end) == null)
				{
					FinanceRecorderCmnDef.format_error("Incorrect end time format[%s] in config file", time_month_end);
					ret = FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
					break OUT;
				}

				FinanceRecorderCmnDef.format_debug("New entry in config [%s %s:%s]", data_source, time_month_start, time_month_end);
//				FinanceRecorderCmnDef.TimeRangeCfg time_range_cfg = new FinanceRecorderCmnDef.TimeRangeCfg(time_month_start, time_month_end);
				FinanceRecorderCmnDef.TimeRangeCfg time_range_cfg = get_time_range_cfg_object_after_adjustment(finance_data_type_index, time_month_start, time_month_end);
				if (time_range_cfg == null)
					return FinanceRecorderCmnDef.RET_FAILURE_UNEXPECTED_VALUE;
				finance_source_time_range_table.put(finance_data_type_index, time_range_cfg);
			}
		}
		catch (IOException e)
		{
			FinanceRecorderCmnDef.format_error("Error occur while parsing the config file, due to: %s", e.toString());
			ret = FinanceRecorderCmnDef.RET_FAILURE_IO_OPERATION;
		}
		finally
		{
			if (br != null)
			{
				try{br.close();}
				catch(Exception e){}
				br = null;
			}
		}

		return ret;
	}

	public short update_by_parameter(LinkedList<Integer> finance_data_type_index_list, String time_month_start, String time_month_end)
	{
// Check the time of start time
		if (FinanceRecorderCmnDef.get_date_value_matcher(time_month_start) == null)
		{
			FinanceRecorderCmnDef.format_error("Incorrect start time format[%s] in config file", time_month_start);
			return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
		}
// Check the time of start time
		if (FinanceRecorderCmnDef.get_date_value_matcher(time_month_end) == null)
		{
			FinanceRecorderCmnDef.format_error("Incorrect end time format[%s] in config file", time_month_end);
			return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
		}

		for (Integer finance_data_type_index : finance_data_type_index_list)
		{
			FinanceRecorderCmnDef.format_debug("New entry in config [%s %s:%s]", finance_data_type_index, time_month_start, time_month_end);
//			FinanceRecorderCmnDef.TimeRangeCfg time_range_cfg = new FinanceRecorderCmnDef.TimeRangeCfg(time_month_start, time_month_end);
			FinanceRecorderCmnDef.TimeRangeCfg time_range_cfg = get_time_range_cfg_object_after_adjustment(finance_data_type_index, time_month_start, time_month_end);
			if (time_range_cfg == null)
				return FinanceRecorderCmnDef.RET_FAILURE_UNEXPECTED_VALUE;
			finance_source_time_range_table.put(finance_data_type_index, time_range_cfg);
		}
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	private short check_time_month_range_from_csv(int finance_data_type_index, StringBuilder csv_time_month_start_str_builder, StringBuilder csv_time_month_end_str_builder)
	{
		String command = String.format("ls %s | grep %s", FinanceRecorderCmnDef.DATA_FOLDER_NAME, FinanceRecorderCmnDef.FINANCE_DATA_NAME_LIST[finance_data_type_index]);
		FinanceRecorderCmnDef.format_debug("Find CSV time range by command: %s", finance_data_type_index, command);
		StringBuilder result_str_builder = new StringBuilder();
		short ret = FinanceRecorderCmnDef.execute_command(command, result_str_builder);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;
		String result_list_str = result_str_builder.toString();
		String[] result_list = result_list_str.split("\n");
		int result_list_len = result_list.length;
		if (result_list_len == 0)
		{
			FinanceRecorderCmnDef.format_error("No %s CSV files are found", FinanceRecorderCmnDef.FINANCE_DATA_DESCRIPTION_LIST[finance_data_type_index]);
			return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
		}
//		Pattern csv_filename_pattern = Pattern.compile(String.format("%s([\\d]{6}).csv", FinanceRecorderCmnDef.FINANCE_DATA_NAME_LIST[finance_data_type_index]));
		Pattern csv_filename_pattern = Pattern.compile("([\\d]{6})");
		Matcher csv_filename_start_matcher = csv_filename_pattern.matcher(result_list[0]);
		if (!csv_filename_start_matcher.find())
		{
			FinanceRecorderCmnDef.format_error("Incorrect start time filename format: %s", result_list[0]);
			return FinanceRecorderCmnDef.RET_FAILURE_UNEXPECTED_VALUE;
		}
		Matcher csv_filename_end_matcher = csv_filename_pattern.matcher(result_list[result_list_len - 1]);
		if (!csv_filename_end_matcher.find())
		{
			FinanceRecorderCmnDef.format_error("Incorrect end time filename format: %s", result_list[result_list_len - 1]);
			return FinanceRecorderCmnDef.RET_FAILURE_UNEXPECTED_VALUE;
		}
		String start_time_str = csv_filename_start_matcher.group(1);
		String end_time_str = csv_filename_end_matcher.group(1);
		int start_year = Integer.parseInt(start_time_str.substring(0, 4));
		int start_month = Integer.parseInt(start_time_str.substring(4));
		int end_year = Integer.parseInt(end_time_str.substring(0, 4));
		int end_month = Integer.parseInt(end_time_str.substring(4));

		csv_time_month_start_str_builder.append(String.format("%04d-%02d", start_year, start_month));
		csv_time_month_end_str_builder.append(String.format("%04d-%02d", end_year, end_month));
		FinanceRecorderCmnDef.format_debug("Time range from CSV: %s %s", csv_time_month_start_str_builder.toString(), csv_time_month_end_str_builder.toString());

		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	private FinanceRecorderCmnDef.TimeRangeCfg get_time_range_cfg_object_after_adjustment(int finance_data_type_index, String time_month_start, String time_month_end)
	{
		StringBuilder csv_time_month_start_str_builder = new StringBuilder();
		StringBuilder csv_time_month_end_str_builder = new StringBuilder();
		short ret = check_time_month_range_from_csv(finance_data_type_index, csv_time_month_start_str_builder, csv_time_month_end_str_builder);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return null;
		String month_start = time_month_start;
		String month_end = time_month_end;
		FinanceRecorderCmnDef.TimeRangeCfg time_range_cfg = null;
		try
		{
			java.util.Date csv_start_month_date = FinanceRecorderCmnDef.get_month_date(csv_time_month_start_str_builder.toString());
			java.util.Date csv_end_month_date = FinanceRecorderCmnDef.get_month_date(csv_time_month_end_str_builder.toString());
			java.util.Date start_month_date = FinanceRecorderCmnDef.get_month_date(time_month_start);
			java.util.Date end_month_date = FinanceRecorderCmnDef.get_month_date(time_month_end);
			if (start_month_date.before(csv_start_month_date))
			{
				FinanceRecorderCmnDef.format_warn("Out of range in %s! Change start month from %s to %s", FinanceRecorderCmnDef.FINANCE_DATA_DESCRIPTION_LIST[finance_data_type_index], time_month_start, csv_time_month_start_str_builder.toString());
				month_start = csv_time_month_start_str_builder.toString();
			}
			if (end_month_date.after(csv_end_month_date))
			{
				FinanceRecorderCmnDef.format_warn("Out of range in %s! Change end month from %s to %s", FinanceRecorderCmnDef.FINANCE_DATA_DESCRIPTION_LIST[finance_data_type_index], time_month_end, csv_time_month_end_str_builder.toString());
				month_end = csv_time_month_end_str_builder.toString();
			}
			time_range_cfg = new FinanceRecorderCmnDef.TimeRangeCfg(month_start, month_end);
		}
		catch (ParseException e)
		{
			FinanceRecorderCmnDef.format_error("Incorrect time format, due to: %s", e.toString());
		}
		return time_range_cfg;
	}

	@Override
	public short notify(short type)
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		switch (type)
		{
//		case StockRecorderCmnDef.NOTIFY_GET_DATA:
//			break;
		default:
			ret = FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
			break;
		}
		return ret;
	}

	public short write()
	{
// Write the data into MySQL one by one
		short ret;
		for (Map.Entry<Integer, FinanceRecorderCmnDef.TimeRangeCfg> entry : finance_source_time_range_table.entrySet())
		{
			int finance_data_type_index = entry.getKey();
			FinanceRecorderDataHandler finance_recorder_data_handler = new FinanceRecorderDataHandler(FinanceRecorderCmnDef.FinanceDataType.valueOf(finance_data_type_index));
			FinanceRecorderCmnDef.TimeRangeCfg time_range_cfg = entry.getValue();

			FinanceRecorderCmnDef.format_debug("Try to write data [%s %s] into MySQL......", finance_recorder_data_handler.get_description(), time_range_cfg.toString());
// Write the data into MySQL
			ret = finance_recorder_data_handler.write_to_sql(time_range_cfg, FinanceRecorderCmnDef.DatabaseCreateThreadType.DatabaseCreateThread_Single, FinanceRecorderCmnDef.DatabaseEnableBatchType.DatabaseEnableBatch_No);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				return ret;
		}
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	public short write_by_multithread()
	{
		ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(FinanceRecorderCmnDef.MAX_CONCURRENT_THREAD);
		LinkedList<Future<Integer>> res_list = new LinkedList<Future<Integer>>();
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
OUT:
		do
		{
			for (Map.Entry<Integer, FinanceRecorderCmnDef.TimeRangeCfg> entry : finance_source_time_range_table.entrySet())
			{
				int finance_data_type_index = entry.getKey();
//				FinanceRecorderWriter finance_recorder_data_handler = new FinanceRecorderWriter(FinanceRecorderCmnDef.financeDataType.valueOf(finance_data_type_index));
				FinanceRecorderCmnDef.TimeRangeCfg time_range_cfg = entry.getValue();
// Setup the time range
				int[] time_list = FinanceRecorderCmnDef.get_start_and_end_month_value_range(time_range_cfg);
				if (time_list == null)
				{
					ret = FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
					break OUT;
				}
				int year_start = time_list[0]; 
				int month_start = time_list[1];
				int year_end = time_list[2];
				int month_end = time_list[3];

				int year_cur_end = year_start;
				int month_cur_end = month_start;
				int month_offset = FinanceRecorderCmnDef.MAX_MONTH_RANGE_IN_THREAD - 1;
				int year_to_month_end = year_end * 12 + month_end;
				while(true)
				{
					month_cur_end = month_start + month_offset;
					year_cur_end = year_start;
					while (month_cur_end > 12)
					{
						year_cur_end++;
						month_cur_end -= 12;
					}

					int year_to_month_cur_end = year_cur_end * 12 + month_cur_end;
					if (year_to_month_cur_end > year_to_month_end)
					{
						year_cur_end = year_end;
						month_cur_end = month_end;
					}
// Write the data into MySQL
					FinanceRecorderDataHandler finance_recorder_data_handler = new FinanceRecorderDataHandler(FinanceRecorderCmnDef.FinanceDataType.valueOf(finance_data_type_index));
//					FinanceRecorderCmnDef.format_debug("Try to write data [%s %04d%02d:%04d%02d] into MySQL......", finance_recorder_data_handler.get_description(), year_start, month_start, year_cur_end, month_cur_end);
					FinanceRecorderWriterTask task = new FinanceRecorderWriterTask(new FinanceRecorderDataHandler(FinanceRecorderCmnDef.FinanceDataType.valueOf(finance_data_type_index)), new FinanceRecorderCmnDef.TimeRangeCfg(year_start, month_start, year_cur_end, month_cur_end));
					Future<Integer> res = executor.submit(task);
//					try{Thread.sleep(5);}
//					catch (InterruptedException e){}
					res_list.add(res);

					if (year_cur_end == year_end && month_cur_end == month_end)
						break;
					month_start = month_start + FinanceRecorderCmnDef.MAX_MONTH_RANGE_IN_THREAD;
					while (month_start > 12)
					{
						year_start++;
						month_start -= 12;
					}
				}
			}
// Check the result
			for (Future<Integer> res : res_list)
			{
				try
				{
					ret = res.get().shortValue();
				}
				catch (ExecutionException e)
				{
					FinanceRecorderCmnDef.format_error("Fail to get return value, due to: %s", e.toString());
					ret = FinanceRecorderCmnDef.RET_FAILURE_UNKNOWN;
				}
				catch (InterruptedException e)
				{
					FinanceRecorderCmnDef.format_error("Fail to get return value, due to: %s", e.toString());
					ret = FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
				}
				if (FinanceRecorderCmnDef.CheckFailure(ret))
					break OUT;
			}
		}while(false);
// Shut down the executor
		if(FinanceRecorderCmnDef.CheckSuccess(ret))
			executor.shutdown();
		else
			executor.shutdownNow();

		return ret;
	}

	public short read(	List<List<String>> total_data_list)
	{
// Write the data into MySQL one by one
		short ret;
		for (Map.Entry<Integer, FinanceRecorderCmnDef.TimeRangeCfg> entry : finance_source_time_range_table.entrySet())
		{
//			LinkedList<String> data_list = new LinkedList<String>();
			int finance_data_type_index = entry.getKey();
			FinanceRecorderDataHandler finance_recorder_data_handler = new FinanceRecorderDataHandler(FinanceRecorderCmnDef.FinanceDataType.valueOf(finance_data_type_index));
			FinanceRecorderCmnDef.TimeRangeCfg time_range_cfg = entry.getValue();

			FinanceRecorderCmnDef.format_debug("Try to read data [%s %s] from MySQL......", finance_recorder_data_handler.get_description(), time_range_cfg.toString());
// Read the data from MySQL
			total_data_list.add(new LinkedList<String>());
			LinkedList<String> data_list = (LinkedList<String>)total_data_list.get(total_data_list.size() - 1);
			ret = finance_recorder_data_handler.read_from_sql(time_range_cfg, "*", data_list);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
// If the database does NOT exist, just ignore the warning
				if (!FinanceRecorderCmnDef.CheckMySQLFailureUnknownDatabase(ret))
					return ret;
		}
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	public short clear(int finance_data_type_index)
	{
		FinanceRecorderDataHandler finance_recorder_data_handler = new FinanceRecorderDataHandler(FinanceRecorderCmnDef.FinanceDataType.valueOf(finance_data_type_index));
		FinanceRecorderCmnDef.format_debug("Try to delete database [%s]......", finance_recorder_data_handler.get_description());
		return finance_recorder_data_handler.delete_sql();
	}

	public short clear_multi(LinkedList<Integer> finance_data_type_index_list)
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		for (Integer finance_data_type_index : finance_data_type_index_list)
		{
			ret = clear(finance_data_type_index);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				return ret;
		}

		return ret;
	}

	public short clear_all()
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		for (int finance_data_type_index = 0 ; finance_data_type_index < FinanceRecorderCmnDef.FinanceDataType.values().length ; finance_data_type_index++)
		{
			ret = clear(finance_data_type_index);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				return ret;
		}

		return ret;
	}

	public short check(boolean check_error)
	{
// The class of keeping track of the relation between data type index and the database start date
		class DatabaseStartDateCfg implements Comparable
		{
			public java.util.Date database_start_date;
			public int finance_data_type_index;
//			private LinkedList<String> date_list;

			public DatabaseStartDateCfg(java.util.Date date, int index)
			{
				database_start_date = date;
				finance_data_type_index = index;
//				date_list = new LinkedList<String>();
			}
			public final java.util.Date get_start_date(){return database_start_date;}
			@Override
			public int compareTo(Object other) 
			{
				return database_start_date.compareTo(((DatabaseStartDateCfg)other).get_start_date());
			}
			@Override
			public String toString()
			{
				return String.format("%s: %s", FinanceRecorderCmnDef.FINANCE_DATA_DESCRIPTION_LIST[finance_data_type_index], FinanceRecorderCmnDef.get_date_str(database_start_date));
			}
		};

		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		ArrayList<DatabaseStartDateCfg> database_start_date_cfg_list = new ArrayList<DatabaseStartDateCfg>();
// Get the time range for each database
		String total_time_range_str = "";
		for (int finance_data_type_index = 0 ; finance_data_type_index < FinanceRecorderCmnDef.FinanceDataType.values().length ; finance_data_type_index++)
		{
			FinanceRecorderDataHandler finance_recorder_data_handler = new FinanceRecorderDataHandler(FinanceRecorderCmnDef.FinanceDataType.valueOf(finance_data_type_index));
			ret = finance_recorder_data_handler.find_database_time_range();
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				return ret;
			StringBuilder database_start_date_builder = new StringBuilder();
			finance_recorder_data_handler.get_database_start_date(database_start_date_builder);
			try
			{
				java.util.Date date_str = FinanceRecorderCmnDef.get_date(database_start_date_builder.toString());
				database_start_date_cfg_list.add(new DatabaseStartDateCfg(date_str, finance_data_type_index));
			}
			catch (ParseException e)
			{
				FinanceRecorderCmnDef.format_debug("Fail to transform the MySQL time format, due to: %s", e.toString());
				ret = FinanceRecorderCmnDef.RET_FAILURE_MYSQL;
			}
			StringBuilder time_range_str_builder = new StringBuilder();
			finance_recorder_data_handler.get_database_time_range_str(time_range_str_builder);
			total_time_range_str += String.format("%s\n", time_range_str_builder.toString());
			FinanceRecorderCmnDef.format_debug("Database[%s] start date: %s", finance_recorder_data_handler.get_description(), database_start_date_builder.toString());
		}
// Write the time range into the config file
		ret = direct_string_to_file(total_time_range_str);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;
		direct_string_to_stdout(total_time_range_str);

// Copy the database time range config file to the FinanceAnalyzer project
		String current_path = FinanceRecorderCmnDef.get_current_path();
		String working_folder = current_path.substring(0, current_path.lastIndexOf("/"));
		String dst_folderpath = String.format("%s/%s/%s", working_folder, FinanceRecorderCmnDef.DATABASE_TIME_RANGE_FILE_DST_PROJECT_NAME, FinanceRecorderCmnDef.CONF_FOLDERNAME);
		File f = new File(dst_folderpath);
		if (f.exists())
		{
			String src_filepath = String.format("%s/%s/%s", current_path, FinanceRecorderCmnDef.CONF_FOLDERNAME, FinanceRecorderCmnDef.DATABASE_TIME_RANGE_FILENAME);
			String dst_filepath = String.format("%s/%s", dst_folderpath, FinanceRecorderCmnDef.DATABASE_TIME_RANGE_FILENAME);
			ret = FinanceRecorderCmnDef.copy_file(src_filepath, dst_filepath);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				return ret;		
		}

		if (!check_error)
			return ret;
// Start to check error in the database
// Sort the data by date
		Collections.sort(database_start_date_cfg_list);
// Compare the date list in each database
		LinkedList<String> data_base_list = null;
		int data_base_list_size = 0;
		int data_base_finance_data_type_index = -1;
//		database_start_date_cfg_list.remove(0);
		String base_database_print_str = null;
		for (DatabaseStartDateCfg database_start_date_cfg : database_start_date_cfg_list)
		{
			FinanceRecorderDataHandler finance_recorder_data_handler = new FinanceRecorderDataHandler(FinanceRecorderCmnDef.FinanceDataType.valueOf(database_start_date_cfg.finance_data_type_index));
			if (data_base_list == null)
			{
				data_base_list = new LinkedList<String>();
//				ret = finance_recorder_data_handler.find_database_time_range();
				ret = finance_recorder_data_handler.read_from_sql(
						new FinanceRecorderCmnDef.TimeRangeCfg(FinanceRecorderCmnDef.get_date_str(database_start_date_cfg.database_start_date), FinanceRecorderCmnDef.get_time_date_today()), 
						FinanceRecorderCmnDef.FINANCE_DATA_SQL_FIELD_DEFINITION_LIST[database_start_date_cfg.finance_data_type_index][0], 
						data_base_list
					);
				if (FinanceRecorderCmnDef.CheckFailure(ret))
					return ret;
				base_database_print_str = String.format("The base database: %s\n", finance_recorder_data_handler.get_description());
				data_base_list_size = data_base_list.size();
				data_base_finance_data_type_index = database_start_date_cfg.finance_data_type_index;
			}
			else
			{
				LinkedList<String> data_compare_list = new LinkedList<String>();
				ret = finance_recorder_data_handler.read_from_sql(
						new FinanceRecorderCmnDef.TimeRangeCfg(FinanceRecorderCmnDef.get_date_str(database_start_date_cfg.database_start_date), FinanceRecorderCmnDef.get_time_date_today()), 
						FinanceRecorderCmnDef.FINANCE_DATA_SQL_FIELD_DEFINITION_LIST[database_start_date_cfg.finance_data_type_index][0], 
						data_compare_list
					);
				if (FinanceRecorderCmnDef.CheckFailure(ret))
					return ret;

				String start_compare_date = data_compare_list.get(0);
				int start_index = data_base_list.indexOf(start_compare_date);
				if (start_index == -1)
				{
					FinanceRecorderCmnDef.format_error("Fail to find Database[%s] start date", finance_recorder_data_handler.get_description());
					return FinanceRecorderCmnDef.RET_FAILURE_MYSQL_DATA_NOT_CONSISTENT;
				}
				List<String> sub_data_base_list = data_base_list.subList(start_index, data_base_list_size);
// Check if the sizes are identical
				if (sub_data_base_list.size() != data_compare_list.size())
				{
					if (base_database_print_str != null)
					{
						FinanceRecorderCmnDef.error(base_database_print_str);
						if(FinanceRecorderCmnDef.is_show_console())
							System.err.println(base_database_print_str);
						base_database_print_str = null;
					}
					String err_result = String.format("The size in NOT identical, %s: %d, %s: %d", 
						FinanceRecorderCmnDef.FINANCE_DATA_DESCRIPTION_LIST[database_start_date_cfg.finance_data_type_index], 
						data_compare_list.size(), 
						FinanceRecorderCmnDef.FINANCE_DATA_DESCRIPTION_LIST[data_base_finance_data_type_index], 
						sub_data_base_list.size()
					);
					FinanceRecorderCmnDef.format_error(err_result);
					if(FinanceRecorderCmnDef.is_show_console())
						System.err.println(err_result);
//					return FinanceRecorderCmnDef.RET_FAILURE_MYSQL_DATA_NOT_CONSISTENT;
// Show the error detail
					ListIterator<String> base_iter = sub_data_base_list.listIterator(); 
					ListIterator<String> compare_iter = data_compare_list.listIterator();
					String base_data, compare_data;
					while (base_iter.hasNext())
					{
						base_data = base_iter.next();
						compare_data = compare_iter.next();
						while (compare_data.compareTo(base_data) != 0)
						{
							String err_result_detail = String.format("Date NOT Found %s: %s",
									FinanceRecorderCmnDef.FINANCE_DATA_DESCRIPTION_LIST[database_start_date_cfg.finance_data_type_index], 
									base_data
								);
							FinanceRecorderCmnDef.format_error(err_result_detail);
							if(FinanceRecorderCmnDef.is_show_console())
								System.err.println(err_result_detail);
							base_data = base_iter.next();
//							return FinanceRecorderCmnDef.RET_FAILURE_MYSQL_DATA_NOT_CONSISTENT;
						}
					}
				}

//				if (!sub_data_base_list.equals(data_compare_list))
//				{
//					FinanceRecorderCmnDef.format_error(
//						"Date in Database(%s, %s) are NOT identical", 
//						FinanceRecorderCmnDef.FINANCE_DATA_SQL_FIELD_DEFINITION_LIST[data_base_finance_data_type_index],
//						FinanceRecorderCmnDef.FINANCE_DATA_SQL_FIELD_DEFINITION_LIST[database_start_date_cfg.finance_data_type_index]
//					);
//					return FinanceRecorderCmnDef.RET_FAILURE_MYSQL_DATA_NOT_CONSISTENT;
//				}
			}
//			System.out.println(database_start_date);
		}
		return ret;
	}

	private short direct_string_to_output_stream(String data, String conf_filename)
	{
// Open the config file for writing
		OutputStreamWriter osw = null;
		BufferedWriter bw = null;
		try
		{
			if(conf_filename != null)
			{
// To file
				String current_path = FinanceRecorderCmnDef.get_current_path();
				String conf_filepath = String.format("%s/%s/%s", current_path, FinanceRecorderCmnDef.CONF_FOLDERNAME, conf_filename);
				File f = new File(conf_filepath);
				FileOutputStream fos = new FileOutputStream(f);
				osw = new OutputStreamWriter(fos);
			}
			else
			{
// To Standard Output
				osw = new OutputStreamWriter(System.out);
			}
			bw = new BufferedWriter(osw);
		}
		catch (IOException e)
		{
			FinanceRecorderCmnDef.format_error("Error occur while directing to output stream, due to: %s", e.toString());
			return FinanceRecorderCmnDef.RET_FAILURE_IO_OPERATION;
		}

		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
// Read the conf file
		try
		{
			bw.write(data);
			bw.flush();
		}
		catch (IOException e)
		{
			FinanceRecorderCmnDef.format_error("Error occur while parsing the config file, due to: %s", e.toString());
			ret = FinanceRecorderCmnDef.RET_FAILURE_IO_OPERATION;
		}
		finally
		{
			if (bw != null)
			{
				try{bw.close();}
				catch(Exception e){}
				bw = null;
			}
		}
		return ret;
	}
	private short direct_string_to_file(String data){return direct_string_to_output_stream(data, FinanceRecorderCmnDef.DATABASE_TIME_RANGE_FILENAME);}
	private short direct_string_to_stdout(String data){return direct_string_to_output_stream(data, null);}

	public short run_daily()
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		ArrayList<FinanceRecorderCmnDef.TimeCfg> workday_array_next = new ArrayList<FinanceRecorderCmnDef.TimeCfg>();
		ret = finance_recorder_workday_calendar.get_next_workday_array(2015, 1, 6, workday_array_next, 40);
		for (FinanceRecorderCmnDef.TimeCfg workday_cfg : workday_array_next)
			System.out.printf("%s ", workday_cfg.toString());
		System.out.printf("\n");

		ArrayList<FinanceRecorderCmnDef.TimeCfg> workday_array_prev = new ArrayList<FinanceRecorderCmnDef.TimeCfg>();
		ret = finance_recorder_workday_calendar.get_prev_workday_array(2015, 1, 6, workday_array_prev, 60);
		for (FinanceRecorderCmnDef.TimeCfg workday_cfg : workday_array_prev)
			System.out.printf("%s ", workday_cfg.toString());
		System.out.printf("\n");
		return ret;
	}
}
