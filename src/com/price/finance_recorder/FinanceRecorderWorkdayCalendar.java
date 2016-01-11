package com.price.finance_recorder;

import java.io.*;
import java.util.*;


public class FinanceRecorderWorkdayCalendar
{
	enum TRAVERSE_SEARCH_TYPE{TRAVERSE_SEARCH_EQUAL, TRAVERSE_SEARCH_PREV, TRAVERSE_SEARCH_NEXT};

	private FinanceRecorderWorkdayCalendar(){}
	public Object clone() throws CloneNotSupportedException {throw new CloneNotSupportedException();}

	private static FinanceRecorderWorkdayCalendar instance = null;
	public static FinanceRecorderWorkdayCalendar get_instance()
	{
		if (instance == null)
			allocate();
		return instance;
	}
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

	private FinanceRecorderCmnClass.TimeRangeCfg time_range_cfg = null;
	private HashMap<Integer, ArrayList<LinkedList<Integer>>> workday_map = new HashMap<Integer, ArrayList<LinkedList<Integer>>>();
	private ArrayList<Integer> workday_year_sort_array = new ArrayList<Integer>();
	int workday_year_sort_array_size;

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
					time_range_cfg = new FinanceRecorderCmnClass.TimeRangeCfg(start_time_str, end_time_str);
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
					for (int i = 0 ; i < 12 ; i++)
					{
						LinkedList<Integer> workday_day_list = new LinkedList<Integer>();
//						workday_day_list.clear();
						month_workday_array.add(workday_day_list);
					}

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

						LinkedList<Integer> workday_day_list = month_workday_array.get(month - 1);
						for (String workday : workday_list)
						{
							workday_day_list.add(Integer.parseInt(workday));
						}
//						if (month != month_workday_array.size())
//						{
//							FinanceRecorderCmnDef.format_error("Incorrect month, expected: %d, actual: %d", month, month_workday_array.size());
//							ret = FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
//							break OUT;
//						}
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

	private boolean check_in_range(int year, int month, int day)
	{
		return FinanceRecorderCmnClass.TimeRangeCfg.time_in_range(time_range_cfg, year, month, day);
	}

	private boolean check_in_range(final FinanceRecorderCmnClass.TimeCfg time_cfg)
	{
		return FinanceRecorderCmnClass.TimeRangeCfg.time_in_range(time_range_cfg, time_cfg);
	}

	private boolean check_greater_than_start(int year, int month, int day)
	{
		FinanceRecorderCmnClass.TimeCfg time_cfg = new FinanceRecorderCmnClass.TimeCfg(year, month, day);
		boolean check = check_greater_than_start(time_cfg);
		return check;
	}

	private boolean check_greater_than_start(final FinanceRecorderCmnClass.TimeCfg time_cfg)
	{
		if (time_range_cfg.get_start_time() == null)
			throw new RuntimeException("The start time in time_range_cfg should NOT be null");
		return (FinanceRecorderCmnClass.TimeCfg.get_int_value(time_cfg) >= FinanceRecorderCmnClass.TimeCfg.get_int_value(time_range_cfg.get_start_time()));
	}

	private boolean check_less_than_end(int year, int month, int day)
	{
		FinanceRecorderCmnClass.TimeCfg time_cfg = new FinanceRecorderCmnClass.TimeCfg(year, month, day);
		boolean check = check_less_than_end(time_cfg);
		return check;
	}

	private boolean check_less_than_end(final FinanceRecorderCmnClass.TimeCfg time_cfg)
	{
		if (time_range_cfg.get_end_time() == null)
			throw new RuntimeException("The end time in time_range_cfg should NOT be null");
		return (FinanceRecorderCmnClass.TimeCfg.get_int_value(time_cfg) <= FinanceRecorderCmnClass.TimeCfg.get_int_value(time_range_cfg.get_end_time()));
	}

	private boolean is_workday(int year, int month, int day)
	{
		int[] date_index_list = new int[3];

		return FinanceRecorderCmnDef.CheckSuccess(find_data_pos(year, month, day, date_index_list, TRAVERSE_SEARCH_TYPE.TRAVERSE_SEARCH_EQUAL));
	}

	private boolean is_workday(final FinanceRecorderCmnClass.TimeCfg time_cfg)
	{
		return is_workday(time_cfg.get_year(), time_cfg.get_month(), time_cfg.get_day());
	}

	private short find_data_pos(int year, int month, int day, int[] date_index_list, TRAVERSE_SEARCH_TYPE traverse_search_type)
	{
		if (!check_in_range(year, month, day))
		{
			FinanceRecorderCmnDef.format_error("The date [%04d-%02d-%02d] is out of range [%s]", year, month, day, time_range_cfg.toString());
			return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
		}

		ArrayList<LinkedList<Integer>> month_workday_array = workday_map.get(year);
		if (month_workday_array == null)
		{
			FinanceRecorderCmnDef.format_error("Incorrect year: %04d", year);
			return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
		}

		LinkedList<Integer> day_list = month_workday_array.get(month - 1);
		assert day_list != null : "day_list should NOT be NULL";
		int day_list_size = day_list.size();
		boolean found = false;
// Find the closest previous workday		
		if (traverse_search_type == TRAVERSE_SEARCH_TYPE.TRAVERSE_SEARCH_PREV)
		{
			int index = day_list_size - 1;
			ListIterator<Integer> iter = day_list.listIterator(index);
			while (iter.hasPrevious()) 
			{
				Integer day_in_list = iter.previous();
				if (day > day_in_list)
				{
					date_index_list[0] = year;
					date_index_list[1] = month - 1;
					date_index_list[2] = index;
					found = true;
					break;
				}
				index--;
			}
// Find the last day of last month
			if (!found)
			{
				if (month == 1)
				{
					year--;
					month = 12;
				}
				else
					month--;
				if (workday_map.get(year) == null)
				{
					FinanceRecorderCmnDef.format_error("The year [%04d] does NOT exist", year);
					return FinanceRecorderCmnDef.RET_FAILURE_NOT_FOUND;
				}
				LinkedList<Integer> day_list_prev_month = workday_map.get(year).get(month - 1);
				int day_list_prev_month_size = day_list_prev_month.size();
				if (day_list_prev_month_size == 0)
				{
					FinanceRecorderCmnDef.format_error("The day list in [%04d-%02d] should NOT be Empty", year, month);
					return FinanceRecorderCmnDef.RET_FAILURE_NOT_FOUND;
				}
				date_index_list[0] = year;
				date_index_list[1] = month - 1;
				date_index_list[2] = day_list_prev_month_size - 1;
				found = true;
			}
			if (!found)
			{
				FinanceRecorderCmnDef.format_error("Fails to find the closest previous workday from [%04d-%02d-%02d]", year, month, day);
				return FinanceRecorderCmnDef.RET_FAILURE_NOT_FOUND;
			}
		}
// Find the closest next workday
		else if (traverse_search_type == TRAVERSE_SEARCH_TYPE.TRAVERSE_SEARCH_NEXT)
		{
			int index = 0;
			ListIterator<Integer> iter = day_list.listIterator(index);
			while (iter.hasNext()) 
			{
				Integer day_in_list = iter.next();
				if (day < day_in_list)
				{
					date_index_list[0] = year;
					date_index_list[1] = month - 1;
					date_index_list[2] = index;
					found = true;
					break;
				}
				index++;
			}
// Find the first day of next month
			if (!found)
			{
				if (month == 12)
				{
					year++;
					month = 1;
				}
				else
					month++;
				if (workday_map.get(year) == null)
				{
					FinanceRecorderCmnDef.format_error("The year [%04d] does NOT exist", year);
					return FinanceRecorderCmnDef.RET_FAILURE_NOT_FOUND;
				}
				LinkedList<Integer> day_list_next_month = workday_map.get(year).get(month - 1);
				int day_list_next_month_size = day_list_next_month.size();
				if (day_list_next_month_size == 0)
				{
					FinanceRecorderCmnDef.format_error("The day list in [%04d-%02d] should NOT be Empty", year, month);
					return FinanceRecorderCmnDef.RET_FAILURE_NOT_FOUND;
				}
				date_index_list[0] = year;
				date_index_list[1] = month - 1;
				date_index_list[2] = 0;
				found = true;
			}
			if (!found)
			{
				FinanceRecorderCmnDef.format_error("Fails to find the closest next workday from [%04d-%02d-%02d]", year, month, day);
				return FinanceRecorderCmnDef.RET_FAILURE_NOT_FOUND;
			}
		}
		else
		{
			int index = day_list.indexOf(day);
			if (index != -1)
			{
				date_index_list[0] = year;
				date_index_list[1] = month - 1;
				date_index_list[2] = index;
				found = true;
			}

			if (!found)
			{
				FinanceRecorderCmnDef.format_warn("The date [%04d-%02d-%02d] is NOT FOUND", year, month, day);
				return FinanceRecorderCmnDef.RET_FAILURE_NOT_FOUND;
			}
		}
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	private short get_date(int year_key, int month_index, int day_index, int[] date_list)
	{
		// Check year key
		if (workday_map.get(year_key) == null)
		{
			FinanceRecorderCmnDef.format_error("Incorrect year key: %04d", year_key);
			return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
		}
	// Check month index
		if (month_index < 0 || month_index >= 12)
		{
			FinanceRecorderCmnDef.format_error("Incorrect month index: %d, should be in the range[0, 12)", month_index);
			return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
		}

		LinkedList<Integer> day_list = workday_map.get(year_key).get(month_index);
		assert day_list != null : "day_list should NOT be NULL";
		int day_list_size = day_list.size();
		if (day_index < 0 || day_index >= day_list_size)
		{
			FinanceRecorderCmnDef.format_error("Incorrect day index: %d, should be in the range[0, %d)", day_index, day_list_size);
			return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
		}
		date_list[0] = year_key;
		date_list[1] = month_index + 1;
		date_list[2] = day_list.get(day_index);
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	private short get_date(int year_key, int month_index, int day_index, FinanceRecorderCmnClass.TimeCfg time_cfg)
	{
		int[] date_list = new int[3];
		short ret = get_date(year_key, month_index, day_index, date_list);
		if (FinanceRecorderCmnDef.CheckSuccess(ret))
		{
			FinanceRecorderCmnClass.TimeCfg time_cfg_tmp = new FinanceRecorderCmnClass.TimeCfg(date_list[0], date_list[1], date_list[2]);
			time_cfg = time_cfg_tmp;
		}
		return ret;
	}

	public short get_prev_workday_array(int year_base, int month_base, int day_base, ArrayList<FinanceRecorderCmnClass.TimeCfg> workday_array, int max_workday_amount)
	{
		int[] start_date_index_list = new int[3];
		FinanceRecorderCmnDef.format_debug("Try to search for the previous workday list from the date %04d-%02d-%02d......", year_base, month_base, day_base);
		short ret = find_data_pos(year_base, month_base, day_base, start_date_index_list, TRAVERSE_SEARCH_TYPE.TRAVERSE_SEARCH_PREV);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;
		int start_year_key = start_date_index_list[0];
		int start_month_index = start_date_index_list[1];
		int start_day_index = start_date_index_list[2];

		int[] date_list = new int[3];
		ret = get_date(start_year_key, start_month_index, start_day_index, date_list);
// Should NOT fail
		if (FinanceRecorderCmnDef.CheckFailure(ret))
		{
			String errmsg = String.format("Fail to date from the parameter: year_key: %d, month_index: %d, day_index: %d, due to: %s", start_year_key, start_month_index, start_day_index, FinanceRecorderCmnDef.GetErrorDescription(ret));
			throw new RuntimeException(errmsg);
		}
		int year = date_list[0], month = date_list[1], day = date_list[2];
		FinanceRecorderCmnDef.format_info("First workday in the previous workday list: %04d-%02d-%02d......", year, month, day);

		boolean first_month = true;
		int start_year_index = start_year_key - workday_year_sort_array.get(0);
		int workday_array_count = 0;
		OUT:
		for (int year_index = start_year_index ; year_index >= 0 ; year_index--)
		{
			int cur_year = workday_year_sort_array.get(year_index);
			for (int month_index = start_month_index ; month_index >= 0 ; month_index--)
			{
				int cur_month = month_index + 1;
				LinkedList<Integer> day_list = workday_map.get(cur_year).get(month_index);
				if (!first_month)
					start_day_index = day_list.size();
//				for (int day_index = start_day_index ; day_index >= 0 ; day_index--)
				ListIterator<Integer> iter = day_list.listIterator(start_day_index);
				while (iter.hasPrevious()) 
				{
					Integer cur_day = iter.previous();
					FinanceRecorderCmnClass.TimeCfg time_cfg = new FinanceRecorderCmnClass.TimeCfg(cur_year, cur_month, cur_day);
					workday_array.add(time_cfg);
					workday_array_count++;
					if (max_workday_amount != -1 && workday_array_count == max_workday_amount)
						break OUT;
				}
				if (first_month)
				{
					start_month_index = 11;
					first_month = false;
				}
			}
		}

//		for (FinanceRecorderCmnClass.TimeCfg workday_cfg : workday_array)
//			System.out.printf("%s ", workday_cfg.toString());
//		System.out.printf("\n");
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	public short get_next_workday_array(int year_base, int month_base, int day_base, ArrayList<FinanceRecorderCmnClass.TimeCfg> workday_array, int max_workday_amount)
	{
		int[] start_date_index_list = new int[3];
		FinanceRecorderCmnDef.format_debug("Try to search for the next workday list from the date %04d-%02d-%02d......", year_base, month_base, day_base);
		short ret = find_data_pos(year_base, month_base, day_base, start_date_index_list, TRAVERSE_SEARCH_TYPE.TRAVERSE_SEARCH_NEXT);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;
		int start_year_key = start_date_index_list[0];
		int start_month_index = start_date_index_list[1];
		int start_day_index = start_date_index_list[2];

		int[] date_list = new int[3];
		ret = get_date(start_year_key, start_month_index, start_day_index, date_list);
// Should NOT fail
		if (FinanceRecorderCmnDef.CheckFailure(ret))
		{
			String errmsg = String.format("Fail to date from the parameter: year_key: %d, month_index: %d, day_index: %d, due to: %s", start_year_key, start_month_index, start_day_index, FinanceRecorderCmnDef.GetErrorDescription(ret));
			throw new RuntimeException(errmsg);
		}
		int year = date_list[0], month = date_list[1], day = date_list[2];
		FinanceRecorderCmnDef.format_info("First workday in the next workday list: %04d-%02d-%02d......", year, month, day);

		boolean first_month = true;
		int start_year_index = start_year_key - workday_year_sort_array.get(0);
		int workday_array_count = 0;
		int day_deque_size;
		OUT:
		for (int year_index = start_year_index ; year_index < workday_year_sort_array_size ; year_index++)
		{
			int cur_year = workday_year_sort_array.get(year_index);
			for (int month_index = start_month_index ; month_index < 12 ; month_index++)
			{
				int cur_month = month_index + 1;
//				PDAY_DEQUE day_deque = &workday_map[cur_year][month_index];
				LinkedList<Integer> day_list = workday_map.get(cur_year).get(month_index);
				if (!first_month)
					start_day_index = 0;
				ListIterator<Integer> iter = day_list.listIterator(start_day_index);
				while (iter.hasNext()) 
				{
					Integer cur_day = iter.next();
					FinanceRecorderCmnClass.TimeCfg time_cfg = new FinanceRecorderCmnClass.TimeCfg(cur_year, cur_month, cur_day);
					workday_array.add(time_cfg);
					workday_array_count++;
					if (max_workday_amount != -1 && workday_array_count == max_workday_amount)
						break OUT;
				}
				if (first_month)
				{
					start_month_index = 0;
					first_month = false;
				}
			}
		}
//		for (FinanceRecorderCmnClass.TimeCfg workday_cfg : workday_array)
//			System.out.printf("%s ", workday_cfg.toString());
//		System.out.printf("\n");
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	public short get_prev_workday(int year_base, int month_base, int day_base, int[] prev_date_list)
	{
// Find the date
		ArrayList<FinanceRecorderCmnClass.TimeCfg> workday_array = new ArrayList<FinanceRecorderCmnClass.TimeCfg>();
		short ret = get_prev_workday_array(year_base, month_base, day_base, workday_array, 1);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;
		if (workday_array.isEmpty())
		{
			FinanceRecorderCmnDef.warn("No data is FOUND");
			return FinanceRecorderCmnDef.RET_FAILURE_NOT_FOUND;
		}
// Update the data
		FinanceRecorderCmnClass.TimeCfg time_cfg = workday_array.get(0);
		prev_date_list[0] = time_cfg.get_year();
		prev_date_list[1] = time_cfg.get_month();
		prev_date_list[2] = time_cfg.get_day();

		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	public short get_prev_workday(final FinanceRecorderCmnClass.TimeCfg time_cfg, FinanceRecorderCmnClass.TimeCfg prev_time_cfg)
	{
		int[] prev_date_list = new int[3];
		short ret = get_prev_workday(time_cfg.get_year(), time_cfg.get_month(), time_cfg.get_day(), prev_date_list);
		if (FinanceRecorderCmnDef.CheckSuccess(ret))
		{
			int prev_year = prev_date_list[0], prev_month = prev_date_list[1], prev_day = prev_date_list[2];
			prev_time_cfg = new FinanceRecorderCmnClass.TimeCfg(prev_year, prev_month, prev_day);
		}
		return ret;
	}

	public short get_next_workday(int year_base, int month_base, int day_base, int[] next_date_list)
	{
// Find the date
		ArrayList<FinanceRecorderCmnClass.TimeCfg> workday_array = new ArrayList<FinanceRecorderCmnClass.TimeCfg>();
		short ret = get_next_workday_array(year_base, month_base, day_base, workday_array, 1);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;
		if (workday_array.isEmpty())
		{
			FinanceRecorderCmnDef.warn("No data is FOUND");
			return FinanceRecorderCmnDef.RET_FAILURE_NOT_FOUND;
		}
// Update the data
		FinanceRecorderCmnClass.TimeCfg time_cfg = workday_array.get(0);
		next_date_list[0] = time_cfg.get_year();
		next_date_list[1] = time_cfg.get_month();
		next_date_list[2] = time_cfg.get_day();

		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	public short get_next_workday(final FinanceRecorderCmnClass.TimeCfg time_cfg, FinanceRecorderCmnClass.TimeCfg next_time_cfg)
	{
		int[] next_date_list = new int[3];
		short ret = get_prev_workday(time_cfg.get_year(), time_cfg.get_month(), time_cfg.get_day(), next_date_list);
		if (FinanceRecorderCmnDef.CheckSuccess(ret))
		{
			int prev_year = next_date_list[0], prev_month = next_date_list[1], prev_day = next_date_list[2];
			next_time_cfg = new FinanceRecorderCmnClass.TimeCfg(prev_year, prev_month, prev_day);
		}
		return ret;
	}

	public short get_first_workday(int[] first_date_list)
	{
		Integer first_year = workday_year_sort_array.get(0);
		for (int month_index = 0 ; month_index < 12 ; month_index++)
		{
			LinkedList<Integer> day_list = workday_map.get(first_year).get(month_index);
			if (!day_list.isEmpty())
			{
				first_date_list[0] = first_year;
				first_date_list[1] = month_index + 1;
				first_date_list[2] = day_list.get(0);
				break;
			}
		}
		FinanceRecorderCmnDef.format_debug("The first workday: %04d-%02d-%02d", first_date_list[0], first_date_list[1], first_date_list[2]);
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	public short get_first_workday(FinanceRecorderCmnClass.TimeCfg first_time_cfg)
	{
		int[] first_date_list = new int[3];
		short ret = get_first_workday(first_date_list);
		if (FinanceRecorderCmnDef.CheckSuccess(ret))
			first_time_cfg = new FinanceRecorderCmnClass.TimeCfg(first_date_list[0], first_date_list[1], first_date_list[2]);
		return ret;
	}

	public short get_last_workday(int[] last_date_list)
	{
		Integer last_year = workday_year_sort_array.get(workday_year_sort_array.size() - 1);
		for (int month_index = 11 ; month_index >= 0 ; month_index--)
		{
			LinkedList<Integer> day_list = workday_map.get(last_year).get(month_index);
			if (!day_list.isEmpty())
			{
				last_date_list[0] = last_year;
				last_date_list[1] = month_index + 1;
				last_date_list[2] = day_list.get(day_list.size() - 1);
				break;
			}
		}
		FinanceRecorderCmnDef.format_debug("The last workday: %04d-%02d-%02d", last_date_list[0], last_date_list[1], last_date_list[2]);
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	public short get_last_workday(FinanceRecorderCmnClass.TimeCfg last_time_cfg)
	{
		int[] last_date_list = new int[3];
		short ret = get_last_workday(last_date_list);
		if (FinanceRecorderCmnDef.CheckSuccess(ret))
			last_time_cfg = new FinanceRecorderCmnClass.TimeCfg(last_date_list[0], last_date_list[1], last_date_list[2]);
		return ret;
	}
}
