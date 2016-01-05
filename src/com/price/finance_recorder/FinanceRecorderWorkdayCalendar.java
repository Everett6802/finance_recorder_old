package com.price.finance_recorder;

import java.io.*;
import java.util.*;


public class FinanceRecorderWorkdayCalendar
{
	private FinanceRecorderWorkdayCalendar(){}
	public Object clone() throws CloneNotSupportedException {throw new CloneNotSupportedException();}

	private static FinanceRecorderWorkdayCalendar instance = null;
	public static FinanceRecorderWorkdayCalendar get_instance()
	{
		if (instance == null)
			allocate();

		return instance;
	}

	private FinanceRecorderCmnDef.TimeRangeCfg time_range_cfg = null;
	private HashMap<Integer, ArrayList<LinkedList<Integer>>> workday_map = new HashMap<Integer, ArrayList<LinkedList<Integer>>>();
	private ArrayList<Integer> workday_year_sort_array = new ArrayList<Integer>();
	int workday_year_sort_array_size;

	private static synchronized void allocate() // For thread-safe
	{
		if (instance == null)
		{
			instance = new FinanceRecorderWorkdayCalendar();
			short ret = instance.initialize();
			if (FinanceRecorderCmnDef.CheckFailure(ret))
			{
				String errmsg = String.format("Fail to initialize the FinanceAnalyzerWorkdayCanlendar object , due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret));
				throw new RuntimeException(errmsg);
			}
		} 
	}

	private short initialize()
	{
// Open the file
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		BufferedReader reader = null;
		String conf_filepath = String.format("%s/%s/%s", FinanceRecorderCmnDef.get_current_path(), FinanceRecorderCmnDef.CONF_FOLDERNAME, FinanceRecorderCmnDef.WORKDAY_CANLENDAR_FILENAME);
		FinanceRecorderCmnDef.format_debug("Try to parse the configuration in %s", conf_filepath);
// Check the file exists or not
		File fp = new File(conf_filepath);
		if (!fp.exists())
		{
			FinanceRecorderCmnDef.format_error("The configration file[%s] does NOT exist", conf_filepath);
			return FinanceRecorderCmnDef.RET_FAILURE_NOT_FOUND;
		}
// Try to parse the content of the config file
		try
		{
			reader = new BufferedReader(new FileReader(fp));
			String buf;
			boolean param_start = false;
			OUT:
			while ((buf = reader.readLine()) != null)
			{
				if (buf.length() == 0)
					continue;
				if (time_range_cfg == null)
				{
					int index = buf.indexOf(' ');
					if (index == -1)
					{
						FinanceRecorderCmnDef.format_error("Incorrect time format in %s: %s", FinanceRecorderCmnDef.WORKDAY_CANLENDAR_FILENAME, buf);
						return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
					}
					String start_time_str = buf.substring(0, index);
					String end_time_str = buf.substring(index + 1);
					time_range_cfg = new FinanceRecorderCmnDef.TimeRangeCfg(start_time_str, end_time_str);
					FinanceRecorderCmnDef.format_debug("Find the time range [%s %s] in %s", start_time_str, end_time_str, FinanceRecorderCmnDef.WORKDAY_CANLENDAR_FILENAME);
				}
				else
				{
					int year_end_index = buf.indexOf(']');
					if (year_end_index == -1)
					{
						FinanceRecorderCmnDef.format_error("Incorrect data format in %s: %s", FinanceRecorderCmnDef.WORKDAY_CANLENDAR_FILENAME, buf);
						return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
					}
					int year = Integer.valueOf(buf.substring(1, year_end_index));
					workday_year_sort_array.add(year);

					ArrayList<LinkedList<Integer>> month_workday_array = new ArrayList<LinkedList<Integer>>();
					String[] month_workday_array_str =  buf.substring(year_end_index + 1).split(";");
					for (String month_workday_str : month_workday_array_str)
					{
						String[] month_tmp = month_workday_str.split(":");
						if (month_tmp.length != 2)
						{
							FinanceRecorderCmnDef.format_error("Incorrect month workday list: %s", month_workday_str);
							ret = FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
							break OUT;
						}
						int month = Integer.parseInt(month_tmp[0]);
						if (month < 1 || month > 12)
						{
							FinanceRecorderCmnDef.format_error("Incorrect month value: %d", month);
							ret = FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
							break OUT;
						}
						String[] workday_list = month_tmp[1].split(",");
						LinkedList<Integer> workday_day_list = new LinkedList<Integer>();
						for (String workday : workday_list)
						{
							workday_day_list.add(Integer.getInteger(workday));
						}
						month_workday_array.add(workday_day_list);
						if (month != month_workday_array.size())
						{
							FinanceRecorderCmnDef.format_error("Incorrect month, expected: %d, actual: %d", month, month_workday_array.size());
							ret = FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
							break OUT;
						}
					}
					workday_map.put(year, month_workday_array);
				}
			}
		}
		catch (FileNotFoundException ex)
		{
			FinanceRecorderCmnDef.format_error("The config file[%s] does NOT exist", conf_filepath);
			return FinanceRecorderCmnDef.RET_FAILURE_NOT_FOUND;
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
				try 
				{
					reader.close();
				}
				catch (IOException e){}// nothing to do here except log the exception
			}
		}
		Collections.sort(workday_year_sort_array);
		workday_year_sort_array_size = workday_year_sort_array.size();
		return ret;
	}

}
