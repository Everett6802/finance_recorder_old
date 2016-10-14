package com.price.finance_recorder_market;

//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileReader;
//import java.io.IOException;
import java.util.ArrayList;
//import java.util.Collections;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
//import java.util.LinkedList;
import com.price.finance_recorder_cmn.FinanceRecorderCmnClass;
import com.price.finance_recorder_cmn.FinanceRecorderCmnDef;


public class FinanceRecorderDatabaseTimeRange
{
	private FinanceRecorderDatabaseTimeRange(){}
	public Object clone() throws CloneNotSupportedException {throw new CloneNotSupportedException();}

	private static FinanceRecorderDatabaseTimeRange instance = null;
	public static FinanceRecorderDatabaseTimeRange get_instance()
	{
		if (instance == null)
			allocate();
		return instance;
	}
	private static synchronized void allocate() // For thread-safe
	{
		if (instance == null)
		{
			instance = new FinanceRecorderDatabaseTimeRange();
			short ret = instance.initialize();
			if (FinanceRecorderCmnDef.CheckFailure(ret))
			{
				String errmsg = String.format("Fail to initialize the FinanceRecorderDatabaseTimeRange object , due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret));
				throw new RuntimeException(errmsg);
			}
		} 
	}
	private static synchronized void release() // For thread-safe
	{
		if (instance != null)
			instance = null;
	}

	private ArrayList<FinanceRecorderCmnClass.FinanceTimeRange> database_time_range_list = null; 

	private short parse_database_time_range_config()
	{
		int market_source_type_amount = FinanceRecorderCmnDef.FinanceSourceType.get_market_source_type_amount();
		database_time_range_list = new ArrayList<FinanceRecorderCmnClass.FinanceTimeRange>(market_source_type_amount);
		Collections.fill(database_time_range_list, null);
// Check if the config file exist
		if (FinanceRecorderCmnDef.check_config_file_exist(FinanceRecorderCmnDef.MARKET_DATABASE_TIME_RANGE_CONF_FILENAME))
		{
			FinanceRecorderCmnDef.format_warn("The database time range config file[%s] does NOT exist", FinanceRecorderCmnDef.MARKET_DATABASE_TIME_RANGE_CONF_FILENAME);
			return FinanceRecorderCmnDef.RET_SUCCESS;
		}
// Start to parse the time range from the config files if the file exists
		LinkedList<String> config_line_list = new LinkedList<String>();
		short ret = FinanceRecorderCmnDef.get_config_file_lines(FinanceRecorderCmnDef.MARKET_DATABASE_TIME_RANGE_CONF_FILENAME, config_line_list);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;// Open the file
// Try to parse the content of the config file
//		int source_type_index_count = 0;
		OUT:
		for (String config_line : config_line_list)
		{
// Check if the source type in the config file is in order
			String data_array[] = config_line.split(" ");
			if (data_array.length != 3)
			{
				FinanceRecorderCmnDef.format_error("Incorrect config format: %s", config_line);
				ret = FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
				break OUT;
			}
// Get source type index
			int source_type_index = FinanceRecorderCmnDef.get_source_type_index_from_description(data_array[0]);
			if (source_type_index == -1)
			{
				FinanceRecorderCmnDef.format_error("Unknown source type description[%s] in config file", data_array[0]);
				ret = FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
				break OUT;
			}
// Check the source type is in correct finance mode
			if (!FinanceRecorderCmnDef.check_source_type_index_in_range(source_type_index))
			{
				FinanceRecorderCmnDef.format_error("The source type[%s] does NOT belong to %s mode", FinanceRecorderCmnDef.FINANCE_DATA_DESCRIPTION_LIST[source_type_index], FinanceRecorderCmnDef.get_finance_mode_description());
				ret = FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
				break OUT;
			}
// Setup the start/end time string 
			if (database_time_range_list.get(source_type_index) != null)
			{
				FinanceRecorderCmnDef.format_error("The time range of source type[%s] has already been set", FinanceRecorderCmnDef.FINANCE_DATA_DESCRIPTION_LIST[source_type_index]);
				ret = FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
				break OUT;
			}
			database_time_range_list.set(source_type_index, new FinanceRecorderCmnClass.FinanceTimeRange(data_array[0], data_array[1]));
		}

		return ret;
	}

	private short initialize()
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		// Read the data from the config file
		ret = parse_database_time_range_config();
		if (FinanceRecorderCmnDef.CheckFailure(ret) && !FinanceRecorderCmnDef.CheckFailureNotFound(ret))
			return ret;
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	FinanceRecorderCmnClass.FinanceTimeRange find_database_time_range(final HashSet<Integer> source_type_index_set)
	{
		assert !source_type_index_set.isEmpty() : "source_type_index_set should NOT be empty";
// Search for the max start time and min end time to make sure the MySQL data is NOT out of range
		int max_start_time_source_type_index = -1;
		int max_start_time_int_value = 0;
		int min_end_time_source_type_index = -1;
		int min_end_time_int_value = 0;
// Find the max start time and min end time in the current selection
		for (Integer source_type_index : source_type_index_set)
		{
			int start_time_int_value = database_time_range_list.get(source_type_index).get_time_start_key_value();
			if (max_start_time_source_type_index == -1 || start_time_int_value > max_start_time_int_value)
			{
				max_start_time_source_type_index = source_type_index;
				max_start_time_int_value = start_time_int_value;
			}
			int end_time_int_value = database_time_range_list.get(source_type_index).get_time_start_key_value();
			if (min_end_time_source_type_index == -1 || end_time_int_value < min_end_time_int_value)
			{
				min_end_time_source_type_index = source_type_index;
				min_end_time_int_value = end_time_int_value;
			}
		}
		FinanceRecorderCmnDef.format_debug("The datebase search time range:%s %s", database_time_range_list.get(max_start_time_source_type_index).get_time_start_string(), database_time_range_list.get(min_end_time_source_type_index).get_time_end_string());
		return new FinanceRecorderCmnClass.FinanceTimeRange(database_time_range_list.get(max_start_time_source_type_index).get_time_start_string(), database_time_range_list.get(min_end_time_source_type_index).get_time_end_string());
	}

	FinanceRecorderCmnClass.FinanceTimeRange get_restricted_time_range(final HashSet<Integer> source_type_index_set, FinanceRecorderCmnClass.FinanceTimeRange finance_time_range)
	{
		FinanceRecorderCmnClass.FinanceTimeRange database_finance_time_range = find_database_time_range(source_type_index_set);
		boolean need_update_start_time = false, need_update_end_time = false;
// Check the start time boundary
//		if (FinanceRecorderCmnClass.TimeCfg.get_int_value(database_time_range_list.get(max_start_time_source_type_index).get_start_time()) > FinanceRecorderCmnClass.TimeCfg.get_int_value(finance_time_range.get_start_time()))
		if (finance_time_range.is_time_start_exist() && database_finance_time_range.get_time_start_key_value() > finance_time_range.get_time_start_key_value())
		{
			FinanceRecorderCmnDef.format_warn("Start search time out of range, restrict from %s to %s", finance_time_range.get_time_start_string(), database_finance_time_range.get_time_start_string());
			need_update_start_time = true;
		}
// Check the end time boundary
//		if (FinanceRecorderCmnClass.TimeCfg.get_int_value(database_time_range_list.get(min_end_time_source_type_index).get_end_time()) < FinanceRecorderCmnClass.TimeCfg.get_int_value(finance_time_range.get_end_time()))
		if (finance_time_range.is_time_end_exist() && database_finance_time_range.get_time_end_key_value() < finance_time_range.get_time_end_key_value())
		{
			FinanceRecorderCmnDef.format_warn("End search time out of range, restrict from %s to %s", finance_time_range.get_time_end_string(), database_finance_time_range.get_time_end_string());
			need_update_end_time = true;
		}
// Modify the time range if necessary
		FinanceRecorderCmnClass.FinanceTimeRange restricted_finance_time_range = null;
		if (need_update_start_time || need_update_end_time)
		{
			restricted_finance_time_range = new FinanceRecorderCmnClass.FinanceTimeRange(
				(need_update_start_time ? database_finance_time_range.get_time_start_string() : (finance_time_range.is_time_start_exist() ? finance_time_range.get_time_start_string() : null)),
				(need_update_end_time ? database_finance_time_range.get_time_end_string() : (finance_time_range.is_time_end_exist() ? finance_time_range.get_time_end_string() : null))
			);
		}
		else
			restricted_finance_time_range = finance_time_range;
		return restricted_finance_time_range;
	}
	FinanceRecorderCmnClass.FinanceTimeRange get_restricted_time_range(int source_type_index, FinanceRecorderCmnClass.FinanceTimeRange finance_time_range)
	{
		HashSet<Integer> source_type_index_set = new HashSet<Integer>();
		source_type_index_set.add(source_type_index);
		return get_restricted_time_range(source_type_index_set, finance_time_range);
	}

	final FinanceRecorderCmnClass.FinanceTimeRange get_source_type_time_range(int finance_source_type_index)
	{
		if (finance_source_type_index < 0 || finance_source_type_index >= FinanceRecorderCmnDef.FINANCE_SOURCE_SIZE)
		{
			FinanceRecorderCmnDef.format_error("The index[%d] is out of range [0, %d)", finance_source_type_index, FinanceRecorderCmnDef.FINANCE_SOURCE_SIZE);
			throw new IllegalArgumentException();
//			return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
		}

		return database_time_range_list.get(finance_source_type_index);
	}

	short get_all_source_type_time_range(HashMap<Integer,FinanceRecorderCmnClass.FinanceTimeRange> time_range_table)
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		for (int i = 0 ; i < FinanceRecorderCmnDef.FINANCE_SOURCE_SIZE ; i++)
			time_range_table.put(i, database_time_range_list.get(i));

		return FinanceRecorderCmnDef.RET_SUCCESS;
	}
}
