package com.price.finance_recorder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

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
				String errmsg = String.format("Fail to initialize the FinanceAnalyzerDatabaseTimeRange object , due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret));
				throw new RuntimeException(errmsg);
			}
		} 
	}

	private ArrayList<FinanceRecorderCmnClass.TimeRangeCfg> database_time_range_array = null; 
	private short initialize()
	{
		database_time_range_array = new ArrayList<FinanceRecorderCmnClass.TimeRangeCfg>();
// Open the file
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		BufferedReader reader = null;
		String conf_filepath = String.format("%s/%s/%s", FinanceRecorderCmnDef.get_current_path(), FinanceRecorderCmnDef.CONF_FOLDERNAME, FinanceRecorderCmnDef.DATABASE_TIME_RANGE_FILENAME);
		FinanceRecorderCmnDef.format_debug("Try to parse the configuration in %s", conf_filepath);
// Check the file exists or not
		File fp = new File(conf_filepath);
		if (!fp.exists())
		{
			FinanceRecorderCmnDef.format_error("The configration file[%s] does NOT exist", conf_filepath);
			return FinanceRecorderCmnDef.RET_FAILURE_NOT_FOUND;
		}
// Try to parse the content of the config file
		int source_type_index_count = 0;
		try
		{
			reader = new BufferedReader(new FileReader(fp));
			String buf;
			OUT:
			while ((buf = reader.readLine()) != null)
			{
				if (buf.length() == 0)
					continue;
// Check if the source type in the config file is in order
				String data_array[] = buf.split(" ");
				if (data_array.length != 2)
				{
					FinanceRecorderCmnDef.format_error("Incorrect config format: %s", buf);
					ret = FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
					break OUT;
				}
				String finance_database_description = data_array[0];
				if (!finance_database_description.equals(FinanceRecorderCmnDef.FINANCE_DATABASE_DESCRIPTION_LIST[source_type_index_count]))
				{
					String errmsg = String.format("The source type[%s] is NOT identical to %s in %s", finance_database_description, FinanceRecorderCmnDef.FINANCE_DATABASE_DESCRIPTION_LIST[source_type_index_count], FinanceRecorderCmnDef.DATABASE_TIME_RANGE_FILENAME);
					throw new RuntimeException(errmsg);
				}
				source_type_index_count++;
				String time_array[] = data_array[1].split(":");
// Find the start/end time string
				database_time_range_array.add(new FinanceRecorderCmnClass.TimeRangeCfg(time_array[0], time_array[1]));
			}
		}
		catch (IOException ex)
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
		return ret;
	}

	short restrict_time_range(final HashSet<Integer> source_type_index_set, FinanceRecorderCmnClass.TimeRangeCfg time_range_cfg)
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
			int time_int_value = FinanceRecorderCmnClass.TimeCfg.get_int_value(database_time_range_array.get(source_type_index).get_start_time());
			if (max_start_time_source_type_index == -1 || time_int_value > max_start_time_int_value)
			{
				max_start_time_source_type_index = source_type_index;
				max_start_time_int_value = time_int_value;
			}
			if (min_end_time_source_type_index == -1 || time_int_value < min_end_time_int_value)
			{
				min_end_time_source_type_index = source_type_index;
				min_end_time_int_value = time_int_value;
			}
		}
		FinanceRecorderCmnDef.format_debug("The available search time range:%s %s", database_time_range_array.get(max_start_time_source_type_index).get_start_time().toString(), database_time_range_array.get(min_end_time_source_type_index).get_end_time().toString());
// Check the start time boundary
		if (FinanceRecorderCmnClass.TimeCfg.get_int_value(database_time_range_array.get(max_start_time_source_type_index).get_start_time()) > FinanceRecorderCmnClass.TimeCfg.get_int_value(time_range_cfg.get_start_time()))
		{
			FinanceRecorderCmnDef.format_warn("Start search time out of range, restrict from %s to %s", time_range_cfg.get_start_time().toString(), database_time_range_array.get(max_start_time_source_type_index).get_start_time().toString());
			 time_range_cfg.set_start_time(database_time_range_array.get(max_start_time_source_type_index).get_start_time());
		}
// Check the end time boundary
		if (FinanceRecorderCmnClass.TimeCfg.get_int_value(database_time_range_array.get(min_end_time_source_type_index).get_end_time()) < FinanceRecorderCmnClass.TimeCfg.get_int_value(time_range_cfg.get_end_time()))
		{
			FinanceRecorderCmnDef.format_warn("End search time out of range, restrict from %s to %s", time_range_cfg.get_end_time().toString(), database_time_range_array.get(min_end_time_source_type_index).get_end_time().toString());
			time_range_cfg.set_end_time(database_time_range_array.get(min_end_time_source_type_index).get_end_time());
		}

		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	final FinanceRecorderCmnClass.TimeRangeCfg get_source_type_time_range(int finance_source_type_index)
	{
		if (finance_source_type_index < 0 || finance_source_type_index >= FinanceRecorderCmnDef.FINANCE_SOURCE_SIZE)
		{
			FinanceRecorderCmnDef.format_error("The index[%d] is out of range [0, %d)", finance_source_type_index, FinanceRecorderCmnDef.FINANCE_SOURCE_SIZE);
			throw new IllegalArgumentException();
//			return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
		}

		return database_time_range_array.get(finance_source_type_index);
	}

	short get_all_source_type_time_range(HashMap<Integer,FinanceRecorderCmnClass.TimeRangeCfg> time_range_table)
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		for (int i = 0 ; i < FinanceRecorderCmnDef.FINANCE_SOURCE_SIZE ; i++)
			time_range_table.put(i, database_time_range_array.get(i));

		return FinanceRecorderCmnDef.RET_SUCCESS;
	}
}
