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

import com.price.finance_recorder_cmn.FinanceRecorderCmnClass;
import com.price.finance_recorder_cmn.FinanceRecorderCmnDef;


public class FinanceRecorderMgr implements FinanceRecorderCmnDef.FinanceObserverInf
{
	private static FinanceRecorderWorkdayCalendar workday_calendar = null;// FinanceRecorderWorkdayCalendar.get_instance();
	private static FinanceRecorderDatabaseTimeRange database_time_range = null; //FinanceRecorderDatabaseTimeRange.get_instance();

	enum ConfigFieldType
	{
		ConfigField_Unknown(0), 
		ConfigField_EmailAddress(1);
		private int value = 0;
		private ConfigFieldType(int value){this.value = value;}
		public static ConfigFieldType valueOf(int value)
		{
			switch (value)
			{
			case 0: return ConfigField_Unknown;
			case 1: return ConfigField_EmailAddress;
			default: return null;
			}
		}
		public int value(){return this.value;}
	};

	private static final int SHOW_RESULT_STDOUT = 0x1;
	private static final int SHOW_RESULT_FILE = 0x2;
	private static final int SHOW_RESULT_EMAIL = 0x4;
	private static final String CONFIG_ALL_FIELD_STRING = "all";

	private HashMap<Integer, FinanceRecorderCmnClass.TimeRangeCfg> finance_source_time_range_table = null;
	private HashMap<Integer, FinanceRecorderCmnClass.TimeRangeCfg> finance_backup_source_time_range_table = null;
	private HashMap<Integer, LinkedList<Integer>> finance_backup_source_field_table = null;
	private LinkedList<String> email_address_list = new LinkedList<String>();

	public short initialize()
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		ret = parse_config();
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;

		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	private short get_config_file_lines(String filename, LinkedList<String> config_line_list)
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
			while ((line = br.readLine()) != null)
			{
				config_line_list.add(line);
			}
		}
		catch (IOException e)
		{
			FinanceRecorderCmnDef.format_error("IO Error occur while parsing the config file, due to: %s", e.toString());
			ret = FinanceRecorderCmnDef.RET_FAILURE_IO_OPERATION;
		}
		catch (Exception e)
		{
			FinanceRecorderCmnDef.format_error("Error occur while parsing the config file, due to: %s", e.toString());
			ret = FinanceRecorderCmnDef.RET_FAILURE_UNKNOWN;
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

	private short update_time_range_table_by_config_file(String filename, HashMap<Integer,FinanceRecorderCmnClass.TimeRangeCfg> time_range_table)
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		LinkedList<String> config_line_list = new LinkedList<String>();
// Read the content from the config file
		ret = get_config_file_lines(filename, config_line_list);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;

		String data_source;
		String time_month_start;
		String time_month_end;
		String time_month_today = null;
OUT:
		for (String line : config_line_list)
		{
			if (line.startsWith(FinanceRecorderCmnDef.CONF_ENTRY_IGNORE_FLAG) || line.length() == 0)
			{
				FinanceRecorderCmnDef.format_debug("Ignore the entry: %s", line);
				continue;
			}
			String[] entry_arr = line.split(" ");
			if (entry_arr.length < 1)
			{
				FinanceRecorderCmnDef.format_error("Incorrect format", line);
				return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
			}

//Get the type of data source
			data_source = entry_arr[0];
			int finance_source_type_index = Arrays.asList(FinanceRecorderCmnDef.FINANCE_DATA_DESCRIPTION_LIST).indexOf(data_source);
			if (finance_source_type_index == -1)
			{
				FinanceRecorderCmnDef.format_error("Unknown data source type[%s] in config file", data_source);
				ret = FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
				break OUT;
			}
//Get the time of start time
			if (entry_arr.length >= 2)
				time_month_start = entry_arr[1];
			else
			{
				if (time_month_today == null)
					time_month_today = FinanceRecorderCmnDef.get_time_month_today();
				time_month_start = time_month_today;
			}
			if (FinanceRecorderCmnClass.TimeCfg.get_month_value_matcher(time_month_start) == null)
			{
				FinanceRecorderCmnDef.format_error("Incorrect start time format[%s] in config file", time_month_start);
				ret = FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
				break OUT;
			}
//Get the time of end time
			if (entry_arr.length >= 3)
				time_month_end = entry_arr[2];
			else
			{
				if (time_month_today == null)
					time_month_today = FinanceRecorderCmnDef.get_time_month_today();
				time_month_end = time_month_today;
			}
			if (FinanceRecorderCmnClass.TimeCfg.get_month_value_matcher(time_month_end) == null)
			{
				FinanceRecorderCmnDef.format_error("Incorrect end time format[%s] in config file", time_month_end);
				ret = FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
				break OUT;
			}

			FinanceRecorderCmnDef.format_debug("New entry in config [%s %s:%s]", data_source, time_month_start, time_month_end);
//			FinanceRecorderCmnClass.TimeRangeCfg time_range_cfg = new FinanceRecorderCmnClass.TimeRangeCfg(time_month_start, time_month_end);
//Adjust the time range 
			FinanceRecorderCmnClass.TimeRangeCfg time_range_cfg = get_time_range_cfg_object_after_adjustment(finance_source_type_index, time_month_start, time_month_end);
			if (time_range_cfg == null)
			{
				ret = FinanceRecorderCmnDef.RET_FAILURE_UNEXPECTED_VALUE;
				break OUT;
			}
			time_range_table.put(finance_source_type_index, time_range_cfg);
		}
		return ret;
	}

	private short update_backup_by_config_file(String filename, HashMap<Integer,FinanceRecorderCmnClass.TimeRangeCfg> time_range_table, HashMap<Integer, LinkedList<Integer>> source_field_table)
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		LinkedList<String> config_line_list = new LinkedList<String>();
// Read the content from the config file
		ret = get_config_file_lines(filename, config_line_list);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;

		String data_source;
		String field_list_str;
		String time_month_start;
		String time_month_end;
		String time_month_today = null;
OUT:
		for (String line : config_line_list)
		{
			if (line.startsWith(FinanceRecorderCmnDef.CONF_ENTRY_IGNORE_FLAG) || line.length() == 0)
			{
				FinanceRecorderCmnDef.format_debug("Ignore the entry: %s", line);
				continue;
			}
			String[] entry_arr = line.split(" ");
			if (entry_arr.length < 2)
			{
				FinanceRecorderCmnDef.format_error("Incorrect format", line);
				return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
			}
//Get the type of data source
			data_source = entry_arr[0];
			int finance_source_type_index = Arrays.asList(FinanceRecorderCmnDef.FINANCE_DATA_DESCRIPTION_LIST).indexOf(data_source);
			if (finance_source_type_index == -1)
			{
				FinanceRecorderCmnDef.format_error("Unknown data source type[%s] in config file", data_source);
				ret = FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
				break OUT;
			}
// Parse the field
			LinkedList<Integer> field_list = new LinkedList<Integer>();
			field_list_str = entry_arr[1];
			if (field_list_str.equals(CONFIG_ALL_FIELD_STRING))
			{
// Start from 1; the date field should be ignored
				for (int i = 1 ; i < FinanceRecorderCmnDef.FINANCE_DATABASE_FIELD_AMOUNT_LIST[finance_source_type_index] ; i++)
					field_list.add(i);
			}
			else
			{
				String[] field_array = field_list_str.split(":");
				for (String field : field_array)
				{
// Ignore the date field
					if (field.equals("0"))
					{
						FinanceRecorderCmnDef.format_warn("Ignore the date filed in data source[%s]", data_source);
						continue;
					}
					field_list.add(Integer.getInteger(field));
				}
			}
// Get the time of start time
			if (entry_arr.length >= 3)
				time_month_start = entry_arr[2];
			else
			{
				if (time_month_today == null)
					time_month_today = FinanceRecorderCmnDef.get_time_month_today();
				time_month_start = time_month_today;
			}
			if (FinanceRecorderCmnClass.TimeCfg.get_month_value_matcher(time_month_start) == null)
			{
				FinanceRecorderCmnDef.format_error("Incorrect start time format[%s] in config file", time_month_start);
				ret = FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
				break OUT;
			}
// Get the time of end time
			if (entry_arr.length >= 4)
				time_month_end = entry_arr[3];
			else
			{
				if (time_month_today == null)
					time_month_today = FinanceRecorderCmnDef.get_time_month_today();
				time_month_end = time_month_today;
			}
			if (FinanceRecorderCmnClass.TimeCfg.get_month_value_matcher(time_month_end) == null)
			{
				FinanceRecorderCmnDef.format_error("Incorrect end time format[%s] in config file", time_month_end);
				ret = FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
				break OUT;
			}

			FinanceRecorderCmnDef.format_debug("New entry for backup in config [%s %s %s:%s]", data_source, field_list_str, time_month_start, time_month_end);
			source_field_table.put(finance_source_type_index, field_list);
//			FinanceRecorderCmnClass.TimeRangeCfg time_range_cfg = new FinanceRecorderCmnClass.TimeRangeCfg(time_month_start, time_month_end);
////Adjust the time range 
//			FinanceRecorderCmnClass.TimeRangeCfg time_range_cfg = get_time_range_cfg_object_after_adjustment(finance_source_type_index, time_month_start, time_month_end);
//			if (time_range_cfg == null)
//			{
//				ret = FinanceRecorderCmnDef.RET_FAILURE_UNEXPECTED_VALUE;
//				break OUT;
//			}
			time_range_table.put(finance_source_type_index, new FinanceRecorderCmnClass.TimeRangeCfg(time_month_start, time_month_end));

		}
		return ret;
	}

	private short update_time_range_table_by_parameter(LinkedList<Integer> finance_source_type_index_list, String time_month_start, String time_month_end, HashMap<Integer,FinanceRecorderCmnClass.TimeRangeCfg> time_range_table)
	{
// Check the time of start time
		if (FinanceRecorderCmnClass.TimeCfg.get_month_value_matcher(time_month_start) == null)
		{
			FinanceRecorderCmnDef.format_error("Incorrect start month format[%s] in config file", time_month_start);
			return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
		}
// Check the time of start time
		if (FinanceRecorderCmnClass.TimeCfg.get_month_value_matcher(time_month_end) == null)
		{
			FinanceRecorderCmnDef.format_error("Incorrect end month format[%s] in config file", time_month_end);
			return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
		}

		for (Integer finance_source_type_index : finance_source_type_index_list)
		{
			FinanceRecorderCmnDef.format_debug("New entry in config [%s %s:%s]", finance_source_type_index, time_month_start, time_month_end);
//			FinanceRecorderCmnClass.TimeRangeCfg time_range_cfg = new FinanceRecorderCmnClass.TimeRangeCfg(time_month_start, time_month_end);
			FinanceRecorderCmnClass.TimeRangeCfg time_range_cfg = get_time_range_cfg_object_after_adjustment(finance_source_type_index, time_month_start, time_month_end);
			if (time_range_cfg == null)
				return FinanceRecorderCmnDef.RET_FAILURE_UNEXPECTED_VALUE;
			time_range_table.put(finance_source_type_index, time_range_cfg);
		}
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	private short update_full_time_range_table(HashMap<Integer,FinanceRecorderCmnClass.TimeRangeCfg> time_range_table)
	{
		if (database_time_range == null)
		{
			FinanceRecorderCmnDef.error("The database time range object is NOT initialized");
			return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
		}
		return database_time_range.get_all_source_type_time_range(time_range_table);
	}

	public short setup_time_range_table_by_config_file(String filename)
	{
		if (finance_source_time_range_table != null)
		{
			FinanceRecorderCmnDef.error("The time range table has already been updated");
			return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
		}
		finance_source_time_range_table = new HashMap<Integer, FinanceRecorderCmnClass.TimeRangeCfg>();
		return update_time_range_table_by_config_file(filename, finance_source_time_range_table);
	}

	public short setup_time_range_table_by_parameter(LinkedList<Integer> finance_source_type_index_list, String time_month_start, String time_month_end)
	{
		if (finance_source_time_range_table != null)
		{
			FinanceRecorderCmnDef.error("The time range table has already been updated");
			return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
		}
		finance_source_time_range_table = new HashMap<Integer, FinanceRecorderCmnClass.TimeRangeCfg>();
		return update_time_range_table_by_parameter(finance_source_type_index_list, time_month_start, time_month_end, finance_source_time_range_table);
	}

	public short setup_backup_time_range_table_by_config_file(String filename)
	{
		if (finance_backup_source_time_range_table != null)
		{
			FinanceRecorderCmnDef.error("The time range table for backup has already been updated");
			return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
		}
		finance_backup_source_time_range_table = new HashMap<Integer, FinanceRecorderCmnClass.TimeRangeCfg>();
		return update_time_range_table_by_config_file(filename, finance_backup_source_time_range_table);
	}

	public short setup_backup_time_range_table_by_parameter(LinkedList<Integer> finance_source_type_index_list, String time_month_start, String time_month_end)
	{
		if (finance_backup_source_time_range_table != null)
		{
			FinanceRecorderCmnDef.error("The time range table for backup has already been updated");
			return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
		}
		finance_backup_source_time_range_table = new HashMap<Integer, FinanceRecorderCmnClass.TimeRangeCfg>();
		return update_time_range_table_by_parameter(finance_source_type_index_list, time_month_start, time_month_end, finance_backup_source_time_range_table);
	}

	private short parse_config()
	{
		final String config_field[] = {"Unknown", "[email_address]"};

		BufferedReader reader = null;
// Open the config file
		String filepath = String.format("%s/%s", FinanceRecorderCmnDef.CONF_FOLDERNAME, FinanceRecorderCmnDef.FINANCE_RECORDER_CONF_FILENAME);
		File file = new File(filepath);
		if (!file.exists())
		{
			FinanceRecorderCmnDef.format_error("The config file[%s] does NOT exist", FinanceRecorderCmnDef.FINANCE_RECORDER_CONF_FILENAME);
			return FinanceRecorderCmnDef.RET_FAILURE_NOT_FOUND;
		}
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
// Try to parse the content of the config file
		try
		{
			reader = new BufferedReader(new FileReader(file));
			String buf;
//			boolean param_start = false;
			ConfigFieldType config_field_type = ConfigFieldType.ConfigField_Unknown;
			OUT:
			while ((buf = reader.readLine()) != null)
			{
//				if (buf.length() == 0)
//					continue;
				if (buf.startsWith("["))
				{
					config_field_type = ConfigFieldType.ConfigField_Unknown;
					for (int i = 1 ; i < ConfigFieldType.values().length ; i++)
					{
						if (buf.indexOf(config_field[i]) != -1)
						{
							FinanceRecorderCmnDef.format_debug("Parse the parameter in the file: %s", config_field[i]);
							config_field_type = ConfigFieldType.valueOf(i);
							break;
						}
					}
					if (config_field_type == ConfigFieldType.ConfigField_Unknown)
					{
						FinanceRecorderCmnDef.format_error("Unknown field title in config[%s]: %s", filepath, buf);
						return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
					}
				}
				else
				{
//					if (strlen(buf) == 0)
//						continue;
					switch(config_field_type)
					{
					case ConfigField_EmailAddress:
					{
						email_address_list.add(buf);
					}
					break;
					default:
					{
						FinanceRecorderCmnDef.format_error("Unsupported field type: %d", config_field_type);
						return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
					}
					}
				}
			}
		}
		catch (IOException ex)
		{
			FinanceRecorderCmnDef.format_error("IO Error occur due to %s", ex.toString());
			ret = FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
		}
		catch (Exception ex)
		{
			FinanceRecorderCmnDef.format_error("Error occur due to %s", ex.toString());
			ret = FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
		}
		finally 
		{
// Close the file
			if (reader != null)
			{
				try {reader.close();}
				catch (IOException e){}// nothing to do here except log the exception
			}
		}
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	private short check_time_month_range_from_csv(int finance_source_type_index, StringBuilder csv_time_month_start_str_builder, StringBuilder csv_time_month_end_str_builder)
	{
		String command = String.format("ls %s | grep %s", FinanceRecorderCmnDef.DATA_ROOT_FOLDERPATH, FinanceRecorderCmnDef.FINANCE_DATA_NAME_LIST[finance_source_type_index]);
		FinanceRecorderCmnDef.format_debug("Find CSV time range by command: %s", finance_source_type_index, command);
		StringBuilder result_str_builder = new StringBuilder();
		short ret = FinanceRecorderCmnDef.execute_shell_command(command, result_str_builder);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;
// Check the search result
		String result_list_str = result_str_builder.toString();
		if (result_list_str.isEmpty())
		{
			FinanceRecorderCmnDef.format_error("No %s CSV files are found", FinanceRecorderCmnDef.FINANCE_DATA_DESCRIPTION_LIST[finance_source_type_index]);
			return FinanceRecorderCmnDef.RET_FAILURE_NOT_FOUND;
		}
// Find the csv time range from the result of shell command
		String[] result_list = result_list_str.split("\n");
		int result_list_len = result_list.length;
//		Pattern csv_filename_pattern = Pattern.compile(String.format("%s([\\d]{6}).csv", FinanceRecorderCmnDef.FINANCE_DATA_NAME_LIST[finance_source_type_index]));
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

	private FinanceRecorderCmnClass.TimeRangeCfg get_time_range_cfg_object_after_adjustment(int finance_source_type_index, String time_month_start, String time_month_end)
	{
		StringBuilder csv_time_month_start_str_builder = new StringBuilder();
		StringBuilder csv_time_month_end_str_builder = new StringBuilder();
		short ret = check_time_month_range_from_csv(finance_source_type_index, csv_time_month_start_str_builder, csv_time_month_end_str_builder);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return null;
		String month_start = time_month_start;
		String month_end = time_month_end;
		FinanceRecorderCmnClass.TimeRangeCfg time_range_cfg = null;
		try
		{
			java.util.Date csv_start_month_date = FinanceRecorderCmnDef.get_month_date(csv_time_month_start_str_builder.toString());
			java.util.Date csv_end_month_date = FinanceRecorderCmnDef.get_month_date(csv_time_month_end_str_builder.toString());
			java.util.Date start_month_date = FinanceRecorderCmnDef.get_month_date(time_month_start);
			java.util.Date end_month_date = FinanceRecorderCmnDef.get_month_date(time_month_end);
			if (start_month_date.before(csv_start_month_date))
			{
				FinanceRecorderCmnDef.format_warn("Out of range in %s! Change start month from %s to %s", FinanceRecorderCmnDef.FINANCE_DATA_DESCRIPTION_LIST[finance_source_type_index], time_month_start, csv_time_month_start_str_builder.toString());
				month_start = csv_time_month_start_str_builder.toString();
			}
			if (end_month_date.after(csv_end_month_date))
			{
				FinanceRecorderCmnDef.format_warn("Out of range in %s! Change end month from %s to %s", FinanceRecorderCmnDef.FINANCE_DATA_DESCRIPTION_LIST[finance_source_type_index], time_month_end, csv_time_month_end_str_builder.toString());
				month_end = csv_time_month_end_str_builder.toString();
			}
			time_range_cfg = new FinanceRecorderCmnClass.TimeRangeCfg(month_start, month_end);
		}
		catch (ParseException e)
		{
			FinanceRecorderCmnDef.format_error("Incorrect time format, due to: %s", e.toString());
		}
		catch (Exception e)
		{
			FinanceRecorderCmnDef.format_error("Error occur due to: %s", e.toString());
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
		for (Map.Entry<Integer, FinanceRecorderCmnClass.TimeRangeCfg> entry : finance_source_time_range_table.entrySet())
		{
			int finance_source_type_index = entry.getKey();
			FinanceRecorderMarketDataHandler finance_recorder_data_handler = new FinanceRecorderMarketDataHandler(FinanceRecorderCmnDef.FinanceSourceType.valueOf(finance_source_type_index));
			FinanceRecorderCmnClass.TimeRangeCfg time_range_cfg = entry.getValue();

			FinanceRecorderCmnDef.format_debug("Try to write data [%s %s] into MySQL......", finance_recorder_data_handler.get_description(), time_range_cfg.toString());
// Write the data into MySQL
			ret = finance_recorder_data_handler.write_to_sql(time_range_cfg, FinanceRecorderCmnDef.DatabaseCreateThreadType.DatabaseCreateThread_Single, FinanceRecorderCmnDef.DatabaseEnableBatchType.DatabaseEnableBatch_No);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				return ret;
		}
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	public short restore(String restore_folderpath, String restore_foldername)
	{
// Backup the workday calendar/database time range config files
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		String current_path = FinanceRecorderCmnDef.get_current_path();
		String src_folderpath = String.format("%s/%s/%s", (restore_folderpath != null ? restore_folderpath : current_path), FinanceRecorderCmnDef.BACKUP_FOLDERNAME, restore_foldername);
		String workday_canlendar_src_filepath = String.format("%s/%s", src_folderpath, FinanceRecorderCmnDef.WORKDAY_CANLENDAR_CONF_FILENAME);
		String database_time_range_src_filepath = String.format("%s/%s", src_folderpath,FinanceRecorderCmnDef.DATABASE_TIME_RANGE_CONF_FILENAME);
		String dst_folderpath = String.format("%s/%s", current_path, FinanceRecorderCmnDef.CONF_FOLDERNAME);
// Copy the workday calendar config file
		FinanceRecorderCmnDef.format_debug("Copy workday calendar config file[%s] from bakcup folder[%s]", FinanceRecorderCmnDef.WORKDAY_CANLENDAR_CONF_FILENAME, restore_foldername);
		File workday_calendar_file_handle = new File(workday_canlendar_src_filepath);
		if (!workday_calendar_file_handle.exists())
		{
			FinanceRecorderCmnDef.format_error("The workday calendar config file[%s] does NOT exist", FinanceRecorderCmnDef.WORKDAY_CANLENDAR_CONF_FILENAME);
			return FinanceRecorderCmnDef.RET_FAILURE_NOT_FOUND;	
		}
		ret = FinanceRecorderCmnDef.copy_file(workday_canlendar_src_filepath, String.format("%s/%s", dst_folderpath, FinanceRecorderCmnDef.WORKDAY_CANLENDAR_CONF_FILENAME));
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;
// Copy the database time range config file
		FinanceRecorderCmnDef.format_debug("Copy database time range config file[%s] from bakcup folder[%s]", FinanceRecorderCmnDef.DATABASE_TIME_RANGE_CONF_FILENAME, restore_foldername);
		File database_time_range_file_handle = new File(database_time_range_src_filepath);
		if (!database_time_range_file_handle.exists())
		{
			FinanceRecorderCmnDef.format_error("The database time range config file[%s] does NOT exist", FinanceRecorderCmnDef.DATABASE_TIME_RANGE_CONF_FILENAME);
			return FinanceRecorderCmnDef.RET_FAILURE_NOT_FOUND;	
		}
		ret = FinanceRecorderCmnDef.copy_file(database_time_range_src_filepath, String.format("%s/%s", dst_folderpath, FinanceRecorderCmnDef.DATABASE_TIME_RANGE_CONF_FILENAME));
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;
// We assume the process stops after restore so that the databse time range singleton can be initialized here !!!
		ret = init_database_time_range_table();
// Write the data into MySQL one by one
		int data_name_list_length = FinanceRecorderCmnDef.FINANCE_DATA_NAME_LIST.length;
		for (int finance_source_type_index = 0 ; finance_source_type_index < data_name_list_length ; finance_source_type_index++)
		{
			FinanceRecorderCmnClass.TimeRangeCfg time_range_cfg = null;
			try
			{
				time_range_cfg = database_time_range.get_source_type_time_range(finance_source_type_index);
				FinanceRecorderMarketDataHandler finance_recorder_data_handler = new FinanceRecorderMarketDataHandler(FinanceRecorderCmnDef.FinanceSourceType.valueOf(finance_source_type_index), src_folderpath);
				FinanceRecorderCmnDef.format_debug("Try to restore data [%s %s] into MySQL......", finance_recorder_data_handler.get_description(), time_range_cfg.toString());
	// Write the data into MySQL
				ret = finance_recorder_data_handler.write_to_sql(time_range_cfg, FinanceRecorderCmnDef.DatabaseCreateThreadType.DatabaseCreateThread_Single, FinanceRecorderCmnDef.DatabaseEnableBatchType.DatabaseEnableBatch_No);
				if (FinanceRecorderCmnDef.CheckFailure(ret))
					return ret;
			}
			catch (IllegalArgumentException e)
			{
				FinanceRecorderCmnDef.format_debug("No data[%d] to restore, skipped...", finance_source_type_index);
			}
		}
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	public short restore_latest(String restore_folderpath)
	{
// Write the data into MySQL one by one
		List<String> sorted_restore_list = new LinkedList<String>();
		short ret = get_sorted_backup_list(sorted_restore_list);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;
		if (sorted_restore_list.isEmpty())
		{
			FinanceRecorderCmnDef.error("The restore files are NOT found");
			return FinanceRecorderCmnDef.RET_FAILURE_NOT_FOUND;
		}
		String restore_foldername = sorted_restore_list.get(sorted_restore_list.size() - 1);

		return restore(restore_folderpath, restore_foldername);
	}

	private short get_time_range_slice(FinanceRecorderCmnClass.TimeRangeCfg time_range_cfg, int slice_size, ArrayList<FinanceRecorderCmnClass.TimeRangeCfg> time_range_slice_cfg_list)
	{
		if (slice_size <= 1)
		{
			FinanceRecorderCmnDef.error("The Slice Size should be larger than 1");
			return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
		}
		int[] time_list = FinanceRecorderCmnClass.TimeRangeCfg.get_start_and_end_month_value_range(time_range_cfg);
		if (time_list == null)
		{
			FinanceRecorderCmnDef.format_error("Incorrect time range format: %s",time_range_cfg.toString());
			return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
		}
		int year_start = time_list[0]; 
		int month_start = time_list[1];
		int year_end = time_list[2];
		int month_end = time_list[3];

		int year_cur_end = year_start;
		int month_cur_end = month_start;
		int month_offset = slice_size - 1;
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
			time_range_slice_cfg_list.add(new FinanceRecorderCmnClass.TimeRangeCfg(year_start, month_start, year_cur_end, month_cur_end));
			if (year_cur_end == year_end && month_cur_end == month_end)
				break;
			month_start = month_start + slice_size;
			while (month_start > 12)
			{
				year_start++;
				month_start -= 12;
			}
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
			for (Map.Entry<Integer, FinanceRecorderCmnClass.TimeRangeCfg> entry : finance_source_time_range_table.entrySet())
			{
				int finance_source_type_index = entry.getKey();
//				FinanceRecorderWriter finance_recorder_data_handler = new FinanceRecorderWriter(FinanceRecorderCmnDef.FinanceSourceType.valueOf(finance_source_type_index));
				FinanceRecorderCmnClass.TimeRangeCfg time_range_cfg = entry.getValue();
// Split the time range into several smaller time range slice
				ArrayList<FinanceRecorderCmnClass.TimeRangeCfg> time_range_slice_cfg_list = new ArrayList<FinanceRecorderCmnClass.TimeRangeCfg>();
				ret = get_time_range_slice(time_range_cfg, FinanceRecorderCmnDef.WRITE_SQL_MAX_MONTH_RANGE_IN_THREAD, time_range_slice_cfg_list);
				if (FinanceRecorderCmnDef.CheckFailure(ret))
					break OUT;
				for (FinanceRecorderCmnClass.TimeRangeCfg time_range_slice_cfg : time_range_slice_cfg_list)
				{
// Setup the time range
// Activate the task of writing data into SQL
					FinanceRecorderMarketDataHandler finance_recorder_data_handler = new FinanceRecorderMarketDataHandler(FinanceRecorderCmnDef.FinanceSourceType.valueOf(finance_source_type_index));
//					FinanceRecorderCmnDef.format_debug("Try to write data [%s %04d%02d:%04d%02d] into MySQL......", finance_recorder_data_handler.get_description(), time_range_slice_cfg.get_start_time().get_year(), time_range_slice_cfg.get_start_time().get_month(), time_range_slice_cfg.get_end_time().get_year(), time_range_slice_cfg.get_end_time().get_month());
					FinanceRecorderWriteSQLTask task = new FinanceRecorderWriteSQLTask(new FinanceRecorderMarketDataHandler(FinanceRecorderCmnDef.FinanceSourceType.valueOf(finance_source_type_index)), time_range_slice_cfg);
					Future<Integer> res = executor.submit(task);
//					try{Thread.sleep(5);}
//					catch (InterruptedException e){}
					res_list.add(res);
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

	public short init_workday_calendar_table()
	{
		if (workday_calendar != null)
		{
			FinanceRecorderCmnDef.error("The workday calendar object has already been initialized");
			return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
		}
		workday_calendar = FinanceRecorderWorkdayCalendar.get_instance();
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	public short init_database_time_range_table()
	{
		if (database_time_range != null)
		{
			FinanceRecorderCmnDef.error("The database time range object has already been initialized");
			return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
		}
		database_time_range = FinanceRecorderDatabaseTimeRange.get_instance();
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	public short query(FinanceRecorderCmnClass.TimeRangeCfg time_range_cfg, final FinanceRecorderCmnClass.QuerySet query_set, FinanceRecorderCmnClass.ResultSet result_set)
	{
		if (database_time_range == null)
		{
			FinanceRecorderCmnDef.error("The database time range object is NOT initialized");
			return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
		}

		if (time_range_cfg.get_start_time() == null || time_range_cfg.get_end_time() == null)
		{
			FinanceRecorderCmnDef.error("The start/end time in time_range_cfg should NOT be NULL");
			return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
		}
		if (time_range_cfg.is_month_type())
		{
			FinanceRecorderCmnDef.error("The time format of time_range_cfg should be Day type");
			return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
		}
		if (!query_set.is_add_query_done())
		{
			FinanceRecorderCmnDef.error("The setting of query data is NOT complete");
			return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
		}
// Collect the information that what kind of the data source will be queried
		HashSet<Integer> source_type_index_set = new HashSet<Integer>();
		for (int i = 0 ; i < FinanceRecorderCmnDef.FINANCE_SOURCE_SIZE ; i++)
		{
			if (!query_set.get_index(i).isEmpty())
				source_type_index_set.add(i);
		}
// Restrict the search time range
		FinanceRecorderCmnClass.TimeRangeCfg restrict_time_range_cfg = new FinanceRecorderCmnClass.TimeRangeCfg(time_range_cfg.get_start_time_str(), time_range_cfg.get_end_time_str());
		database_time_range.restrict_time_range(source_type_index_set, restrict_time_range_cfg);

		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
//		FinanceRecorderCmnClass.TimeRangeCfg time_range_cfg = null;
		for (int finance_source_type_index = 0 ; finance_source_type_index < FinanceRecorderCmnDef.FINANCE_SOURCE_SIZE ; finance_source_type_index++)
		{
			LinkedList<Integer> query_field = query_set.get_index(finance_source_type_index);
			if (query_field.isEmpty())
				continue;
// Add to the result set
			ret = result_set.add_set(finance_source_type_index, query_field);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				return ret;

			FinanceRecorderMarketDataHandler finance_recorder_data_handler = new FinanceRecorderMarketDataHandler(FinanceRecorderCmnDef.FinanceSourceType.valueOf(finance_source_type_index));
			FinanceRecorderCmnDef.format_debug("Try to read data [%s %s] from MySQL......", finance_recorder_data_handler.get_description(), restrict_time_range_cfg.toString());
// Format the SQL query command
			StringBuilder field_cmd_builder = new StringBuilder();
			ret = FinanceRecorderSQLClient.get_sql_field_command(finance_source_type_index, query_field, field_cmd_builder);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				return ret;
// Read the data from MySQL
			ret = finance_recorder_data_handler.read_from_sql(restrict_time_range_cfg, field_cmd_builder.toString(), result_set);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
			{
// If the database does NOT exist, just ignore the warning
				if (!FinanceRecorderCmnDef.CheckMySQLFailureUnknownDatabase(ret))
					return ret;
			}
			result_set.switch_to_check_date_mode();
		}
// Check the result
		ret = result_set.check_data();

		return ret;
	}

	public short read_all(FinanceRecorderCmnClass.ResultSet result_set)
	{
// Read all the data from MySQL one by one
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		for (Map.Entry<Integer, FinanceRecorderCmnClass.TimeRangeCfg> entry : finance_source_time_range_table.entrySet())
		{
//			LinkedList<String> data_list = new LinkedList<String>();
			int finance_source_type_index = entry.getKey();
			FinanceRecorderMarketDataHandler finance_recorder_data_handler = new FinanceRecorderMarketDataHandler(FinanceRecorderCmnDef.FinanceSourceType.valueOf(finance_source_type_index));
			FinanceRecorderCmnClass.TimeRangeCfg time_range_cfg = entry.getValue();

			FinanceRecorderCmnDef.format_debug("Try to read data [%s %s] from MySQL......", finance_recorder_data_handler.get_description(), time_range_cfg.toString());
// Read the data from MySQL
			ret = finance_recorder_data_handler.read_from_sql(time_range_cfg, "*", result_set);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
// If the database does NOT exist, just ignore the warning
				if (!FinanceRecorderCmnDef.CheckMySQLFailureUnknownDatabase(ret))
					return ret;
		}
		return ret;
	}

	public short clear(int finance_source_type_index)
	{
		FinanceRecorderMarketDataHandler finance_recorder_data_handler = new FinanceRecorderMarketDataHandler(FinanceRecorderCmnDef.FinanceSourceType.valueOf(finance_source_type_index));
		FinanceRecorderCmnDef.format_debug("Try to delete database [%s]......", finance_recorder_data_handler.get_description());
		return finance_recorder_data_handler.delete_sql();
	}

	public short clear_multi(LinkedList<Integer> finance_source_type_index_list)
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		for (Integer finance_source_type_index : finance_source_type_index_list)
		{
			ret = clear(finance_source_type_index);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				return ret;
		}

		return ret;
	}

	public short clear_all()
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		for (int finance_source_type_index = 0 ; finance_source_type_index < FinanceRecorderCmnDef.FinanceSourceType.values().length ; finance_source_type_index++)
		{
			ret = clear(finance_source_type_index);
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
			public int finance_source_type_index;
//			private LinkedList<String> date_list;

			public DatabaseStartDateCfg(java.util.Date date, int index)
			{
				database_start_date = date;
				finance_source_type_index = index;
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
				return String.format("%s: %s", FinanceRecorderCmnDef.FINANCE_DATA_DESCRIPTION_LIST[finance_source_type_index], FinanceRecorderCmnDef.get_date_str(database_start_date));
			}
		};

		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		ArrayList<DatabaseStartDateCfg> database_start_date_cfg_list = new ArrayList<DatabaseStartDateCfg>();
// Get the time range for each database
		String total_time_range_str = "";
		for (int finance_source_type_index = 0 ; finance_source_type_index < FinanceRecorderCmnDef.FinanceSourceType.values().length ; finance_source_type_index++)
		{
			FinanceRecorderMarketDataHandler finance_recorder_data_handler = new FinanceRecorderMarketDataHandler(FinanceRecorderCmnDef.FinanceSourceType.valueOf(finance_source_type_index));
			ret = finance_recorder_data_handler.find_database_time_range();
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				return ret;
			StringBuilder database_start_date_builder = new StringBuilder();
			finance_recorder_data_handler.get_database_start_date(database_start_date_builder);
			try
			{
				java.util.Date date_str = FinanceRecorderCmnDef.get_date(database_start_date_builder.toString());
				database_start_date_cfg_list.add(new DatabaseStartDateCfg(date_str, finance_source_type_index));
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
		String conf_filepath = String.format("%s/%s/%s", FinanceRecorderCmnDef.get_current_path(), FinanceRecorderCmnDef.CONF_FOLDERNAME, FinanceRecorderCmnDef.DATABASE_TIME_RANGE_CONF_FILENAME);
		ret = FinanceRecorderCmnDef.direct_string_to_output_stream(total_time_range_str, conf_filepath);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;
		if (FinanceRecorderCmnDef.is_show_console())
			FinanceRecorderCmnDef.direct_string_to_output_stream(total_time_range_str);

// Copy the database time range config file to the FinanceAnalyzer project
		String current_path = FinanceRecorderCmnDef.get_current_path();
		String working_folder = current_path.substring(0, current_path.lastIndexOf("/"));
		String dst_folderpath = String.format("%s/%s/%s", working_folder, FinanceRecorderCmnDef.DATABASE_TIME_RANGE_FILE_DST_PROJECT_NAME, FinanceRecorderCmnDef.CONF_FOLDERNAME);
		File f = new File(dst_folderpath);
		if (f.exists())
		{
			String src_filepath = String.format("%s/%s/%s", current_path, FinanceRecorderCmnDef.CONF_FOLDERNAME, FinanceRecorderCmnDef.DATABASE_TIME_RANGE_CONF_FILENAME);
			String dst_filepath = String.format("%s/%s", dst_folderpath, FinanceRecorderCmnDef.DATABASE_TIME_RANGE_CONF_FILENAME);
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
//		LinkedList<String> data_base_list = null;
		String[] data_base_array = null;
		int data_base_array_size = 0;
		int data_base_finance_source_type_index = -1;
//		database_start_date_cfg_list.remove(0);
		String base_database_print_str = null;
		for (DatabaseStartDateCfg database_start_date_cfg : database_start_date_cfg_list)
		{
			FinanceRecorderMarketDataHandler finance_recorder_data_handler = new FinanceRecorderMarketDataHandler(FinanceRecorderCmnDef.FinanceSourceType.valueOf(database_start_date_cfg.finance_source_type_index));
			if (data_base_array == null)
			{
//				data_base_list = new LinkedList<String>();
				FinanceRecorderCmnClass.ResultSet base_result_set = new FinanceRecorderCmnClass.ResultSet();
//				ret = finance_recorder_data_handler.find_database_time_range();
				ret = finance_recorder_data_handler.read_from_sql(
						new FinanceRecorderCmnClass.TimeRangeCfg(FinanceRecorderCmnDef.get_date_str(database_start_date_cfg.database_start_date), FinanceRecorderCmnDef.get_time_date_today()), 
						FinanceRecorderCmnDef.FINANCE_DATA_SQL_FIELD_DEFINITION_LIST[database_start_date_cfg.finance_source_type_index][0], 
						base_result_set
					);
				if (FinanceRecorderCmnDef.CheckFailure(ret))
					return ret;
// Get the data from the ResultSet data structure
				ArrayList<String> date_data_array = base_result_set.get_date_array().get_data_array();
				data_base_array = new String[date_data_array.size()];
				date_data_array.toArray(data_base_array);
				base_database_print_str = String.format("The base database: %s\n", finance_recorder_data_handler.get_description());
				data_base_array_size = data_base_array.length;
				data_base_finance_source_type_index = database_start_date_cfg.finance_source_type_index;
			}
			else
			{
				FinanceRecorderCmnClass.ResultSet compare_result_set = new FinanceRecorderCmnClass.ResultSet();
				ret = finance_recorder_data_handler.read_from_sql(
						new FinanceRecorderCmnClass.TimeRangeCfg(FinanceRecorderCmnDef.get_date_str(database_start_date_cfg.database_start_date), FinanceRecorderCmnDef.get_time_date_today()), 
						FinanceRecorderCmnDef.FINANCE_DATA_SQL_FIELD_DEFINITION_LIST[database_start_date_cfg.finance_source_type_index][0],
						compare_result_set
					);
				if (FinanceRecorderCmnDef.CheckFailure(ret))
					return ret;
// Get the data from the ResultSet data structure
				ArrayList<String> date_data_array = compare_result_set.get_date_array().get_data_array();
				String[] data_compare_array = new String[date_data_array.size()];
				date_data_array.toArray(data_compare_array);
				int data_compare_array_size = data_compare_array.length;
				String start_compare_date = data_compare_array[0];
//				int start_index = data_base_array.indexOf(start_compare_date);
				int start_index = -1;
// Find the start index in the base array
				for (int i = 0 ; i < data_base_array_size ; i++)
				{
					if (data_base_array[i].equals(start_compare_date))
					{
						start_index = i;
						break;
					}
				}
				if (start_index == -1)
				{
					FinanceRecorderCmnDef.format_error("Fail to find Database[%s] start date", finance_recorder_data_handler.get_description());
					return FinanceRecorderCmnDef.RET_FAILURE_MYSQL_DATA_NOT_CONSISTENT;
				}
//				ArrayList<String> sub_data_base_array = data_base_array.subList(start_index, data_base_array_size);
// Check if the sizes are identical
				int sub_data_base_array_size = data_base_array_size - start_index;
				if (sub_data_base_array_size != data_compare_array_size)
				{
					if (base_database_print_str != null)
					{
						FinanceRecorderCmnDef.error(base_database_print_str);
						if(FinanceRecorderCmnDef.is_show_console())
							System.err.println(base_database_print_str);
						base_database_print_str = null;
					}
					String err_result = String.format("The size in NOT identical, %s: %d, %s: %d",
						FinanceRecorderCmnDef.FINANCE_DATA_DESCRIPTION_LIST[database_start_date_cfg.finance_source_type_index],
						data_compare_array_size, 
						FinanceRecorderCmnDef.FINANCE_DATA_DESCRIPTION_LIST[data_base_finance_source_type_index],
						sub_data_base_array_size
					);
					FinanceRecorderCmnDef.format_error(err_result);
					if(FinanceRecorderCmnDef.is_show_console())
						System.err.println(err_result);
//					return FinanceRecorderCmnDef.RET_FAILURE_MYSQL_DATA_NOT_CONSISTENT;
// Show the error detail
					String base_data, compare_data;
					for (int i = 0 ; i < sub_data_base_array_size ; i++)
					{
						base_data = data_base_array[i + start_index];
						compare_data = data_compare_array[i];
						while (compare_data.compareTo(base_data) != 0)
						{
							String err_result_detail = String.format("Date NOT Found %s: %s",
									FinanceRecorderCmnDef.FINANCE_DATA_DESCRIPTION_LIST[database_start_date_cfg.finance_source_type_index],
									base_data
								);
							FinanceRecorderCmnDef.format_error(err_result_detail);
							if(FinanceRecorderCmnDef.is_show_console())
								System.err.println(err_result_detail);
//							return FinanceRecorderCmnDef.RET_FAILURE_MYSQL_DATA_NOT_CONSISTENT;
						}
					}
				}
//				if (sub_data_base_list.size() != compare_result_set.size())
//				{
//					if (base_database_print_str != null)
//					{
//						FinanceRecorderCmnDef.error(base_database_print_str);
//						if(FinanceRecorderCmnDef.is_show_console())
//							System.err.println(base_database_print_str);
//						base_database_print_str = null;
//					}
//					String err_result = String.format("The size in NOT identical, %s: %d, %s: %d",
//						FinanceRecorderCmnDef.FINANCE_DATA_DESCRIPTION_LIST[database_start_date_cfg.finance_source_type_index],
//						compare_result_set.size(), 
//						FinanceRecorderCmnDef.FINANCE_DATA_DESCRIPTION_LIST[data_base_finance_source_type_index],
//						sub_data_base_list.size()
//					);
//					FinanceRecorderCmnDef.format_error(err_result);
//					if(FinanceRecorderCmnDef.is_show_console())
//						System.err.println(err_result);
////					return FinanceRecorderCmnDef.RET_FAILURE_MYSQL_DATA_NOT_CONSISTENT;
//// Show the error detail
//					ListIterator<String> base_iter = sub_data_base_list.listIterator(); 
//					ListIterator<String> compare_iter = compare_result_set.listIterator();
//					String base_data, compare_data;
//					while (base_iter.hasNext())
//					{
//						base_data = base_iter.next();
//						compare_data = compare_iter.next();
//						while (compare_data.compareTo(base_data) != 0)
//						{
//							String err_result_detail = String.format("Date NOT Found %s: %s",
//									FinanceRecorderCmnDef.FINANCE_DATA_DESCRIPTION_LIST[database_start_date_cfg.finance_source_type_index], 
//									base_data
//								);
//							FinanceRecorderCmnDef.format_error(err_result_detail);
//							if(FinanceRecorderCmnDef.is_show_console())
//								System.err.println(err_result_detail);
//							base_data = base_iter.next();
////							return FinanceRecorderCmnDef.RET_FAILURE_MYSQL_DATA_NOT_CONSISTENT;
//						}
//					}
//				}

//				if (!sub_data_base_list.equals(compare_result_set))
//				{
//					FinanceRecorderCmnDef.format_error(
//						"Date in Database(%s, %s) are NOT identical", 
//						FinanceRecorderCmnDef.FINANCE_DATA_SQL_FIELD_DEFINITION_LIST[data_base_finance_source_type_index],
//						FinanceRecorderCmnDef.FINANCE_DATA_SQL_FIELD_DEFINITION_LIST[database_start_date_cfg.finance_source_type_index]
//					);
//					return FinanceRecorderCmnDef.RET_FAILURE_MYSQL_DATA_NOT_CONSISTENT;
//				}
			}
//			System.out.println(database_start_date);
		}
		return ret;
	}

	public short run_daily()
	{
		if (workday_calendar == null)
		{
			FinanceRecorderCmnDef.error("The workday calendar object is NOT initialized");
			return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
		}

// Find the latest workday
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		int[] date_list = new int[3];
		ret = workday_calendar.get_last_workday(date_list);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;
		int year = date_list[0], month = date_list[1], day = date_list[2];
		FinanceRecorderCmnDef.format_debug("The workday: %04d-%02d-%02d", year, month, day);
		int[] prev_date_list = new int[3];
		ret = workday_calendar.get_prev_workday(year, month, day, prev_date_list);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;
		int prev_year = prev_date_list[0], prev_month = prev_date_list[1], prev_day = prev_date_list[2];
		FinanceRecorderCmnDef.format_debug("The previous workday: %04d-%02d-%02d", prev_year, prev_month, prev_day);
/*
* 臺股指數及成交量
	成交金額(2), 發行量加權股價指數(4), 漲跌點數(5)
* 三大法人現貨買賣超
	自營商(自行買賣)_買賣差額(3), 自營商(避險)_買賣差額(6), 投信_買賣差額(9), 外資及陸資_買賣差額(12)
* 現貨融資融券餘額
	融資金額(仟元)_前日餘額(14), 融資金額(仟元)_今日餘額(15)
* 三大法人期貨和選擇權留倉淨額
	自營商_多空淨額_契約金額(6), 投信_多空淨額_契約金額(12), 外資_多空淨額_契約金額(18)
* 三大法人選擇權賣權買權比
	買賣權未平倉量比率%(6)
* 三大法人選擇權買賣權留倉淨額
	買權_自營商_買方_口數(1), 買權_自營商_賣方_口數(3), 買權_外資_買方_口數(13), 買權_外資_賣方_口數(15), 賣權_自營商_買方_口數(19), 賣權_自營商_賣方_口數(21), 賣權_外資_買方_口數(31), 賣權_外資_賣方_口數(33)
* 十大交易人及特定法人期貨資訊
	臺股期貨_到期月份_買方_前十大交易人合計_部位數(3), 臺股期貨_到期月份_賣方_前十大交易人合計_部位數(7), 臺股期貨_所有契約_買方_前十大交易人合計_部位數(12), 臺股期貨_所有契約_賣方_前十大交易人合計_部位數(16)
*/
		FinanceRecorderCmnClass.QuerySet query_set = new FinanceRecorderCmnClass.QuerySet();
		FinanceRecorderCmnClass.TimeRangeCfg time_range_cfg = new FinanceRecorderCmnClass.TimeRangeCfg(prev_year, prev_month, prev_day, year, month, day);
		FinanceRecorderCmnClass.ResultSet result_set = new FinanceRecorderCmnClass.ResultSet();
// Add the database field we are interested in
		FinanceRecorderCmnClass.add_query(query_set, FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_StockExchangeAndVolume, 2);
		FinanceRecorderCmnClass.add_query(query_set, FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_StockExchangeAndVolume, 4);
		FinanceRecorderCmnClass.add_query(query_set, FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_StockExchangeAndVolume, 5);
		FinanceRecorderCmnClass.add_query(query_set, FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell, 3);
		FinanceRecorderCmnClass.add_query(query_set, FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell, 6);
		FinanceRecorderCmnClass.add_query(query_set, FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell, 9);
		FinanceRecorderCmnClass.add_query(query_set, FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell, 12);
		FinanceRecorderCmnClass.add_query(query_set, FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_StockMarginTradingAndShortSelling, 14);
		FinanceRecorderCmnClass.add_query(query_set, FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_StockMarginTradingAndShortSelling, 15);
		FinanceRecorderCmnClass.add_query(query_set, FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_FutureAndOptionTop3LegalPersonsOpenInterest, 6);
		FinanceRecorderCmnClass.add_query(query_set, FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_FutureAndOptionTop3LegalPersonsOpenInterest, 12);
		FinanceRecorderCmnClass.add_query(query_set, FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_FutureAndOptionTop3LegalPersonsOpenInterest, 18);
		FinanceRecorderCmnClass.add_query(query_set, FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionPutCallRatio, 6);
		FinanceRecorderCmnClass.add_query(query_set, FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest, 1);
		FinanceRecorderCmnClass.add_query(query_set, FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest, 3);
		FinanceRecorderCmnClass.add_query(query_set, FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest, 13);
		FinanceRecorderCmnClass.add_query(query_set, FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest, 15);
		FinanceRecorderCmnClass.add_query(query_set, FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest, 19);
		FinanceRecorderCmnClass.add_query(query_set, FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest, 21);
		FinanceRecorderCmnClass.add_query(query_set, FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest, 31);
		FinanceRecorderCmnClass.add_query(query_set, FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest, 33);
		FinanceRecorderCmnClass.add_query(query_set, FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_FutureTop10DealersAndLegalPersons, 3);
		FinanceRecorderCmnClass.add_query(query_set, FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_FutureTop10DealersAndLegalPersons, 7);
		FinanceRecorderCmnClass.add_query(query_set, FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_FutureTop10DealersAndLegalPersons, 12);
		FinanceRecorderCmnClass.add_query(query_set, FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_FutureTop10DealersAndLegalPersons, 16);
		query_set.add_query_done();

// Query the data from MySQL
		ret = query(time_range_cfg, query_set, result_set);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;
// Show the result
		ret = result_set.show_data();
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;

// Write into file
		FinanceRecorderCmnClass.TimeCfg time_cfg = new FinanceRecorderCmnClass.TimeCfg(year, month, day);
		ret = show_daily(time_cfg, result_set, SHOW_RESULT_FILE | SHOW_RESULT_EMAIL);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;
		return ret;
	}

	public short show_daily(FinanceRecorderCmnClass.TimeCfg time_cfg, final FinanceRecorderCmnClass.ResultSet result_set, int show_result_type)
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
	// Check the folder of keeping track of the result exist
		ret = FinanceRecorderCmnDef.create_folder_in_project_if_not_exist(FinanceRecorderCmnDef.RESULT_FOLDERNAME);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;

		String buf_string = "";
// Assemble the data
		buf_string += String.format("日期: %04d-%02d-%02d\n", time_cfg.get_year(), time_cfg.get_month(), time_cfg.get_day());
		buf_string += String.format("發行量加權股價指數: %.2f, 漲跌: %.2f, 成交金額(億): %.2f, 變化(億): %.2f\n\n",
			result_set.get_float_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_StockExchangeAndVolume.value(), 4, 1),
			result_set.get_float_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_StockExchangeAndVolume.value(), 5, 1),
			result_set.get_long_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_StockExchangeAndVolume.value(), 2, 1) / 100000000.0,
			(result_set.get_long_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_StockExchangeAndVolume.value(), 2, 1) - result_set.get_long_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_StockExchangeAndVolume.value(), 2, 0)) / 100000000.0
			);
		buf_string += String.format("三大法人買賣超(億)\n外資及陸資: %.2f, 變化: %.2f\n投信: %.2f, 變化: %.2f\n自營商: %.2f, 變化: %.2f\n三大法人: %.2f, 變化: %.2f\n\n",
			result_set.get_long_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell.value(), 12, 1) / 100000000.0,
			(result_set.get_long_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell.value(), 12, 1) - result_set.get_long_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell.value(), 12, 0))  / 100000000.0,
			result_set.get_long_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell.value(), 9, 1) / 100000000.0,
			(result_set.get_long_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell.value(), 9, 1) - result_set.get_long_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell.value(), 9, 0)) / 100000000.0,
			(result_set.get_long_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell.value(), 3, 1) + result_set.get_long_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell.value(), 6, 1)) / 100000000.0,
			(result_set.get_long_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell.value(), 3, 1) + result_set.get_long_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell.value(), 6, 1) - result_set.get_long_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell.value(), 3, 0) - result_set.get_long_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell.value(), 6, 0)) / 100000000.0,
			(result_set.get_long_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell.value(), 3, 1) + result_set.get_long_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell.value(), 6, 1) + result_set.get_long_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell.value(), 9, 1) + result_set.get_long_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell.value(), 12, 1)) / 100000000.0,
			(result_set.get_long_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell.value(), 3, 1) + result_set.get_long_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell.value(), 6, 1) + result_set.get_long_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell.value(), 9, 1) + result_set.get_long_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell.value(), 12, 1) - result_set.get_long_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell.value(), 3, 0) - result_set.get_long_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell.value(), 6, 0) - result_set.get_long_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell.value(), 9, 0) - result_set.get_long_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell.value(), 12, 0)) / 100000000.0
			);
		buf_string += String.format("融資餘額(億): %.2f, 變化: %.2f\n\n",
				result_set.get_long_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_StockMarginTradingAndShortSelling.value(), 15, 1) / 100000.0,
				(result_set.get_long_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_StockMarginTradingAndShortSelling.value(), 15, 1) - result_set.get_long_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_StockMarginTradingAndShortSelling.value(), 14, 1)) / 100000.0
			);
		buf_string += String.format("三大法人期權留倉淨額(億)\n外資: %d, 變化: %d\n投信: %d, 變化: %d\n自營商: %d, 變化: %d\n三大法人: %d, 變化: %d\n\n",
			result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_FutureAndOptionTop3LegalPersonsOpenInterest.value(), 18, 1) / 100.0,
			(result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_FutureAndOptionTop3LegalPersonsOpenInterest.value(), 18, 1) - result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_FutureAndOptionTop3LegalPersonsOpenInterest.value(), 18, 0)) / 100.0,
			result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_FutureAndOptionTop3LegalPersonsOpenInterest.value(), 12, 1) / 100.0,
			(result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_FutureAndOptionTop3LegalPersonsOpenInterest.value(), 12, 1) - result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_FutureAndOptionTop3LegalPersonsOpenInterest.value(), 12, 0)) / 100.0,
			result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_FutureAndOptionTop3LegalPersonsOpenInterest.value(), 6, 1) / 100.0,
			(result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_FutureAndOptionTop3LegalPersonsOpenInterest.value(), 6, 1) - result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_FutureAndOptionTop3LegalPersonsOpenInterest.value(), 6, 0)) / 100.0,
			(result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_FutureAndOptionTop3LegalPersonsOpenInterest.value(), 6, 1) + result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_FutureAndOptionTop3LegalPersonsOpenInterest.value(), 12, 1) + result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_FutureAndOptionTop3LegalPersonsOpenInterest.value(), 18, 1)) / 100.0,
			(result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_FutureAndOptionTop3LegalPersonsOpenInterest.value(), 6, 1) + result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_FutureAndOptionTop3LegalPersonsOpenInterest.value(), 12, 1) + result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_FutureAndOptionTop3LegalPersonsOpenInterest.value(), 18, 1) - result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_FutureAndOptionTop3LegalPersonsOpenInterest.value(), 6, 0) - result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_FutureAndOptionTop3LegalPersonsOpenInterest.value(), 12, 0) - result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_FutureAndOptionTop3LegalPersonsOpenInterest.value(), 18, 0)) / 100.0
			);
		buf_string += String.format("未平倉Put/Call Ratio: %.2f, 變化: %.2f\n\n",
			result_set.get_float_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionPutCallRatio.value(), 6, 1),
			result_set.get_float_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionPutCallRatio.value(), 6, 1) - result_set.get_float_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionPutCallRatio.value(), 6, 0)
			);
		buf_string += String.format("選擇權買賣權留倉口數\n外資\n Buy Call: %d, 變化: %d\n Buy Put: %d, 變化: %d\n Sell Call: %d, 變化: %d\n Sell Put: %d, 變化: %d\n 多方: %d, 變化: %d\n 空方: %d, 變化: %d\n自營商\n Buy Call: %d, 變化: %d\n Buy Put: %d, 變化: %d\n Sell Call: %d, 變化: %d\n Sell Put: %d, 變化: %d\n 多方: %d, 變化: %d\n 空方: %d, 變化: %d\n\n",
			result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(), 13, 1),
			result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(), 13, 1) - result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(), 13, 0),
			result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(), 15, 1),
			result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(), 15, 1) - result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(), 15, 0),
			result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(), 31, 1),
			result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(), 31, 1) - result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(), 31, 0),
			result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(), 33, 1),
			result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(), 33, 1) - result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(), 33, 0),
			result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(), 13, 1) + result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(), 33, 1),
			result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(), 13, 1) + result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(), 33, 1) - result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(), 13, 0) - result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(), 33, 0),
			result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(), 15, 1) + result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(), 31, 1),
			result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(), 15, 1) + result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(), 31, 1) - result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(), 15, 0) - result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(), 31, 0),
			result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(), 1, 1),
			result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(), 1, 1) - result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(), 1, 0),
			result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(), 3, 1),
			result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(), 3, 1) - result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(), 3, 0),
			result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(), 19, 1),
			result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(), 19, 1) - result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(), 19, 0),
			result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(), 21, 1),
			result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(), 21, 1) - result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(), 21, 0),
			result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(), 1, 1) + result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(), 21, 1),
			result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(), 1, 1) + result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(), 21, 1) - result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(), 1, 0) - result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(), 21, 0),
			result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(), 3, 1) + result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(), 19, 1),
			result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(), 3, 1) + result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(), 19, 1) - result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(), 3, 0) - result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(), 19, 0)
			);
		buf_string += String.format("十大交易人及特法留倉淨口數\n近月: %d, 變化: %d\n全月: %d, 變化: %d\n\n",
			result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_FutureTop10DealersAndLegalPersons.value(), 3, 1) - result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_FutureTop10DealersAndLegalPersons.value(), 7, 1),
			result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_FutureTop10DealersAndLegalPersons.value(), 3, 1) - result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_FutureTop10DealersAndLegalPersons.value(), 7, 1) - result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_FutureTop10DealersAndLegalPersons.value(), 3, 0) + result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_FutureTop10DealersAndLegalPersons.value(), 7, 0),
			result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_FutureTop10DealersAndLegalPersons.value(), 12, 1) - result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_FutureTop10DealersAndLegalPersons.value(), 16, 1),
			result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_FutureTop10DealersAndLegalPersons.value(), 12, 1) - result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_FutureTop10DealersAndLegalPersons.value(), 16, 1) - result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_FutureTop10DealersAndLegalPersons.value(), 12, 0) + result_set.get_int_array_element(FinanceRecorderCmnDef.FinanceSourceType.FinanceSource_FutureTop10DealersAndLegalPersons.value(), 16, 0)
			);

// SHow result on the screen
		if ((show_result_type & SHOW_RESULT_STDOUT) != 0)
		{
// Write the data into STDOUT
			ret =  FinanceRecorderCmnDef.direct_string_to_output_stream(buf_string);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				return ret;
		}
// Write the data into file
		if ((show_result_type & SHOW_RESULT_FILE) != 0)
		{
			String filename = String.format(FinanceRecorderCmnDef.DAILY_FINANCE_FILENAME_FORMAT, time_cfg.get_year(), time_cfg.get_month(), time_cfg.get_day());
			String filepath = String.format("%s/%s/%s", FinanceRecorderCmnDef.get_current_path(), FinanceRecorderCmnDef.RESULT_FOLDERNAME, filename);
			FinanceRecorderCmnDef.format_debug("Write daily data to file[%s]", filepath);
			ret = FinanceRecorderCmnDef.direct_string_to_output_stream(buf_string, filepath);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				return ret;
//			System.out.printf("Check the result in file: %s\n", filepath);
		}
// Send the result by email
		if ((show_result_type & SHOW_RESULT_EMAIL) != 0)
		{
			String title = String.format(FinanceRecorderCmnDef.DAILY_FINANCE_EMAIL_TITLE_FORMAT, time_cfg.get_year(), time_cfg.get_month(), time_cfg.get_day());
			for (String email_address : email_address_list)
			{
				if (email_address.isEmpty())
					continue;
				FinanceRecorderCmnDef.format_debug("Write daily data by email[%s] to %s", title, email_address);
				ret = FinanceRecorderCmnDef.send_email(title, email_address, buf_string);
				if (FinanceRecorderCmnDef.CheckFailure(ret))
					return ret;
			}
		}
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	public short backup_by_multithread(boolean copy_backup_folder)
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		String backup_folderpath = String.format("%s/%s", FinanceRecorderCmnDef.get_current_path(), FinanceRecorderCmnDef.BACKUP_FOLDERNAME);
// Create a folder for MySQL backup if not exist 
		ret = FinanceRecorderCmnDef.create_folder_if_not_exist(backup_folderpath);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;
// Create a folder for storing CSV files
// Define the folder name due to current time
		java.util.Date date_now = new java.util.Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date_now);
		String csv_backup_foldername = String.format("%02d%02d%02d%02d%02d%02d", cal.get(Calendar.YEAR) % 100, cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE), cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
		FinanceRecorderCmnDef.format_debug("Create the folder for CSV backup file: %s", csv_backup_foldername);
		String csv_backup_folderpath = String.format("%s/%s", backup_folderpath, csv_backup_foldername);
		ret = FinanceRecorderCmnDef.create_folder(csv_backup_folderpath);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;
// Get the time range table/source field for backup
		finance_backup_source_time_range_table = new HashMap<Integer, FinanceRecorderCmnClass.TimeRangeCfg>();
		finance_backup_source_field_table = new HashMap<Integer, LinkedList<Integer>>();
//		ret = update_full_time_range_table(finance_backup_source_time_range_table);
		ret = update_backup_by_config_file(FinanceRecorderCmnDef.BACKUP_FILENAME, finance_backup_source_time_range_table, finance_backup_source_field_table);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;

// Create the worker threads for backup
		ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(FinanceRecorderCmnDef.MAX_CONCURRENT_THREAD);
		LinkedList<Future<Integer>> res_list = new LinkedList<Future<Integer>>();
OUT:
		do
		{
			for (Map.Entry<Integer, FinanceRecorderCmnClass.TimeRangeCfg> entry : finance_backup_source_time_range_table.entrySet())
			{
				int finance_source_type_index = entry.getKey();
				FinanceRecorderCmnClass.TimeRangeCfg time_range_cfg = entry.getValue();
// Restrict the search time range
				FinanceRecorderCmnClass.TimeRangeCfg restrict_time_range_cfg = new FinanceRecorderCmnClass.TimeRangeCfg(time_range_cfg.get_start_time_str(), time_range_cfg.get_end_time_str());
				database_time_range.restrict_time_range(finance_source_type_index, restrict_time_range_cfg);

// Split the time range into several smaller time range slice
				ArrayList<FinanceRecorderCmnClass.TimeRangeCfg> time_range_slice_cfg_list = new ArrayList<FinanceRecorderCmnClass.TimeRangeCfg>();
				ret = get_time_range_slice(restrict_time_range_cfg, FinanceRecorderCmnDef.BACKUP_SQL_MAX_MONTH_RANGE_IN_THREAD, time_range_slice_cfg_list);
				if (FinanceRecorderCmnDef.CheckFailure(ret))
					break OUT;

				for (FinanceRecorderCmnClass.TimeRangeCfg time_range_slice_cfg : time_range_slice_cfg_list)
				{
// Backup the data from MySQL
					FinanceRecorderMarketDataHandler finance_recorder_data_handler = new FinanceRecorderMarketDataHandler(FinanceRecorderCmnDef.FinanceSourceType.valueOf(finance_source_type_index));
					FinanceRecorderCmnDef.format_debug("Try to backup data [%s %04d%02d:%04d%02d] from MySQL......", finance_recorder_data_handler.get_description(), time_range_slice_cfg.get_start_time().get_year(), time_range_slice_cfg.get_start_time().get_month(), time_range_slice_cfg.get_end_time().get_year(), time_range_slice_cfg.get_end_time().get_month());
					FinanceRecorderBackupSQLTask task = new FinanceRecorderBackupSQLTask(new FinanceRecorderMarketDataHandler(FinanceRecorderCmnDef.FinanceSourceType.valueOf(finance_source_type_index)), time_range_slice_cfg, csv_backup_foldername, finance_backup_source_field_table.get(finance_source_type_index));
					Future<Integer> res = executor.submit(task);
					res_list.add(res);
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

// Backup the workday calendar/database time range config files
		String current_path = FinanceRecorderCmnDef.get_current_path();
		String workday_canlendar_src_filepath = String.format("%s/%s/%s", current_path, FinanceRecorderCmnDef.CONF_FOLDERNAME, FinanceRecorderCmnDef.WORKDAY_CANLENDAR_CONF_FILENAME);
		String database_time_range_src_filepath = String.format("%s/%s/%s", current_path, FinanceRecorderCmnDef.CONF_FOLDERNAME, FinanceRecorderCmnDef.DATABASE_TIME_RANGE_CONF_FILENAME);

		String dst_folderpath = String.format("%s/%s/%s", current_path, FinanceRecorderCmnDef.BACKUP_FOLDERNAME, csv_backup_foldername);
// Copy the workday calendar config file
		FinanceRecorderCmnDef.format_debug("Copy workday calendar config file[%s] to %s", FinanceRecorderCmnDef.WORKDAY_CANLENDAR_CONF_FILENAME, csv_backup_foldername);
		File workday_calendar_file_handle = new File(workday_canlendar_src_filepath);
		if (!workday_calendar_file_handle.exists())
		{
			FinanceRecorderCmnDef.format_error("The workday calendar config file[%s] does NOT exist", FinanceRecorderCmnDef.WORKDAY_CANLENDAR_CONF_FILENAME);
			return FinanceRecorderCmnDef.RET_FAILURE_NOT_FOUND;	
		}
		ret = FinanceRecorderCmnDef.copy_file(workday_canlendar_src_filepath, String.format("%s/%s", dst_folderpath, FinanceRecorderCmnDef.WORKDAY_CANLENDAR_CONF_FILENAME));
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;
// Copy the database time range config file
		FinanceRecorderCmnDef.format_debug("Copy database time range config file[%s] to %s", FinanceRecorderCmnDef.DATABASE_TIME_RANGE_CONF_FILENAME, csv_backup_foldername);
		File database_time_range_file_handle = new File(database_time_range_src_filepath);
		if (!database_time_range_file_handle.exists())
		{
			FinanceRecorderCmnDef.format_error("The database time range config file[%s] does NOT exist", FinanceRecorderCmnDef.DATABASE_TIME_RANGE_CONF_FILENAME);
			return FinanceRecorderCmnDef.RET_FAILURE_NOT_FOUND;	
		}
		ret = FinanceRecorderCmnDef.copy_file(database_time_range_src_filepath, String.format("%s/%s", dst_folderpath, FinanceRecorderCmnDef.DATABASE_TIME_RANGE_CONF_FILENAME));
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;

		if (copy_backup_folder)
		{
			FinanceRecorderCmnDef.delete_subfolder(FinanceRecorderCmnDef.COPY_BACKUP_FOLDERPATH);
			FinanceRecorderCmnDef.format_debug("Copy backup folder[%s] to %s", csv_backup_foldername, FinanceRecorderCmnDef.COPY_BACKUP_FOLDERPATH);
			ret = FinanceRecorderCmnDef.copy_folder(dst_folderpath, FinanceRecorderCmnDef.COPY_BACKUP_FOLDERPATH);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				return ret;
		}

		return ret;
	}

	public static short get_sorted_backup_list(List<String> sorted_backup_list)
	{
		class FoldernameCompare implements Comparable
		{
			public String foldername;
			public FoldernameCompare(String name)
			{
				foldername = name;
			}
			@Override
			public int compareTo(Object other) 
			{
				Long value = Long.parseLong(foldername);
				Long another_value = Long.parseLong(((FoldernameCompare)other).foldername);
				if (value > another_value)
					return 1;
				else if (value < another_value)
					return -1;
				else 
					return 0;
			}
			@Override
			public String toString()
			{
				return foldername;
			}
		};

		String filepath = String.format("%s/%s", FinanceRecorderCmnDef.get_current_path(), FinanceRecorderCmnDef.BACKUP_FOLDERNAME);
		List<String> subfolder_list = new ArrayList<String>();
		short ret = FinanceRecorderCmnDef.get_subfolder_list(filepath, subfolder_list);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
		{
			FinanceRecorderCmnDef.format_error("Fail to get backup folderlist of the MySQL, due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret));
			return ret;
		}
		List<FoldernameCompare> sorted_subfolder_list = new LinkedList<FoldernameCompare>();
		for (String subfolder : subfolder_list)
			sorted_subfolder_list.add(new FoldernameCompare(subfolder));
// Sort the data by number
		Collections.sort(sorted_subfolder_list);
		for (FoldernameCompare sorted_subfolder : sorted_subfolder_list)
			sorted_backup_list.add(sorted_subfolder.toString());
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}
}
