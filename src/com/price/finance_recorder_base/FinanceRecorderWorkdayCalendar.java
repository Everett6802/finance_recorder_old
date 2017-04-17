package com.price.finance_recorder_base;

import java.util.*;
import com.price.finance_recorder_cmn.FinanceRecorderCmnClass;
import com.price.finance_recorder_cmn.FinanceRecorderCmnDef;


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

	public static class TraverseEntry implements Iterable<FinanceRecorderCmnClass.FinanceDate>
	{
		private FinanceRecorderWorkdayCalendar workday_calendar = null;
		private ArrayList<Integer> workday_key_value_sort_array = null;
		private int cur_index = 0;
		private int array_size = 0;

		public TraverseEntry(FinanceRecorderWorkdayCalendar in_workday_calendar, int start_year, int start_month, int start_day)
		{
			workday_calendar = in_workday_calendar;
			workday_key_value_sort_array = workday_calendar.get_workday_key_value_sort_array();
			int start_index = -1;
			if (!workday_calendar.is_workday(start_year, start_month, start_day))
			{
				int[] next_date_value_list = new int[3];
				workday_calendar.get_next_workday(start_year, start_month, start_day, next_date_value_list);
				FinanceRecorderCmnDef.format_error("The day[%s] is NOT a workday, modify it the new one: %s", FinanceRecorderCmnDef.transform_date_str(start_year, start_month, start_day), FinanceRecorderCmnDef.transform_date_str(next_date_value_list[0], next_date_value_list[1], next_date_value_list[2]));
// Modify the start date if NOT a workday
				start_year = next_date_value_list[0];
				start_month = next_date_value_list[1];
				start_day = next_date_value_list[2];
			}
			start_index = Collections.binarySearch(workday_key_value_sort_array, FinanceRecorderCmnClass.FinanceDate.get_key_value_from_time_value(start_year, start_month, start_day));
			if (start_index == -1) 
				throw new IllegalArgumentException(String.format("The day[%s] is NOT a workday", FinanceRecorderCmnDef.transform_date_str(start_year, start_month, start_day)));
			cur_index = start_index;
			array_size = workday_key_value_sort_array.size();
		}

		@Override
		public Iterator<FinanceRecorderCmnClass.FinanceDate> iterator()
		{
			Iterator<FinanceRecorderCmnClass.FinanceDate> it = new Iterator<FinanceRecorderCmnClass.FinanceDate>()
			{
				private int cur_index = 0;
				@Override
				public boolean hasNext()
				{
					return cur_index < array_size;
				}
				@Override
				public FinanceRecorderCmnClass.FinanceDate next()
				{
					int[] time_value_list = FinanceRecorderCmnClass.FinanceDate.get_time_value_list_from_key_value(workday_key_value_sort_array.get(cur_index));
					cur_index++;
					return new FinanceRecorderCmnClass.FinanceDate(time_value_list);
				}
				@Override
				public void remove() {throw new UnsupportedOperationException();}
			};
			return it;
		}
	};

	public static class ReverseTraverseEntry implements Iterable<FinanceRecorderCmnClass.FinanceDate>
	{
		private FinanceRecorderWorkdayCalendar workday_calendar = null;
		private ArrayList<Integer> workday_key_value_sort_array = null;
		private int cur_index = 0;

		public ReverseTraverseEntry(FinanceRecorderWorkdayCalendar in_workday_calendar, int start_year, int start_month, int start_day)
		{
			workday_calendar = in_workday_calendar;
			workday_key_value_sort_array = workday_calendar.get_workday_key_value_sort_array();
			if (!workday_calendar.is_workday(start_year, start_month, start_day))
			{
				int[] prev_date_value_list = new int[3];
				workday_calendar.get_prev_workday(start_year, start_month, start_day, prev_date_value_list);
				FinanceRecorderCmnDef.format_error("The day[%s] is NOT a workday, modify it the new one: %s", FinanceRecorderCmnDef.transform_date_str(start_year, start_month, start_day), FinanceRecorderCmnDef.transform_date_str(prev_date_value_list[0], prev_date_value_list[1], prev_date_value_list[2]));
// Modify the start date if NOT a workday
				start_year = prev_date_value_list[0];
				start_month = prev_date_value_list[1];
				start_day = prev_date_value_list[2];
			}
			int start_index = Collections.binarySearch(workday_key_value_sort_array, FinanceRecorderCmnClass.FinanceDate.get_key_value_from_time_value(start_year, start_month, start_day));
			if (start_index == -1) 
				throw new IllegalArgumentException(String.format("The day[%s] is NOT a workday", FinanceRecorderCmnDef.transform_date_str(start_year, start_month, start_day)));
			cur_index = start_index;
		}

		@Override
		public Iterator<FinanceRecorderCmnClass.FinanceDate> iterator()
		{
			Iterator<FinanceRecorderCmnClass.FinanceDate> it = new Iterator<FinanceRecorderCmnClass.FinanceDate>()
			{
				private int cur_index = 0;
				@Override
				public boolean hasNext()
				{
					return cur_index >= 0;
				}
				@Override
				public FinanceRecorderCmnClass.FinanceDate next()
				{
					int[] time_value_list = FinanceRecorderCmnClass.FinanceDate.get_time_value_list_from_key_value(workday_key_value_sort_array.get(cur_index));
					cur_index--;
					return new FinanceRecorderCmnClass.FinanceDate(time_value_list);
				}
				@Override
				public void remove() {throw new UnsupportedOperationException();}
			};
			return it;
		}
	};

	private FinanceRecorderCmnClass.FinanceDateRange finance_date_range = null;
// Caution: the first/last workday is probably not identical to the value in finance date time
	private FinanceRecorderCmnClass.FinanceDate first_workday_finance_date = null;
	private FinanceRecorderCmnClass.FinanceDate last_workday_finance_date = null;
	private HashMap<Integer, ArrayList<LinkedList<Integer>>> workday_map = new HashMap<Integer, ArrayList<LinkedList<Integer>>>();
	private ArrayList<Integer> workday_year_sort_array = new ArrayList<Integer>();
	private ArrayList<Integer> workday_key_value_sort_array = null;
	int workday_year_sort_array_size;

	
	private short initialize()
	{
// Open the file
		short ret = parse_config();
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;
		return ret;
	}

	private short parse_config()
	{
		LinkedList<String> config_line_list = new LinkedList<String>();
		short ret = FinanceRecorderCmnDef.read_config_file_lines(FinanceRecorderCmnDef.WORKDAY_CANLENDAR_CONF_FILENAME, config_line_list);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;// Open the file

// Try to parse the content of the config file
		for (String config_line : config_line_list)
		{
			if (config_line.length() == 0)
				continue;
			if (finance_date_range == null)
			{
// Find the time range of the workday calendar in the first line
				int index = config_line.indexOf(' ');
				if (index == -1)
				{
					FinanceRecorderCmnDef.format_error("Incorrect time format in %s: %s", FinanceRecorderCmnDef.WORKDAY_CANLENDAR_CONF_FILENAME, config_line);
					return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
				}
				String start_date_str = config_line.substring(0, index);
				String end_date_str = config_line.substring(index + 1);
				finance_date_range = new FinanceRecorderCmnClass.FinanceDateRange(start_date_str, end_date_str);
				FinanceRecorderCmnDef.format_debug("Find the time range [%s %s] in %s", start_date_str, end_date_str, FinanceRecorderCmnDef.WORKDAY_CANLENDAR_CONF_FILENAME);
			}
			else
			{
// Parse the data in each year
				int year_end_index = config_line.indexOf(']');
				if (year_end_index == -1)
				{
					FinanceRecorderCmnDef.format_error("Incorrect data format in %s: %s", FinanceRecorderCmnDef.WORKDAY_CANLENDAR_CONF_FILENAME, config_line);
					return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
				}
				int year = Integer.valueOf(config_line.substring(1, year_end_index));
				workday_year_sort_array.add(year);

				ArrayList<LinkedList<Integer>> workday_month_array = new ArrayList<LinkedList<Integer>>();
				String[] workday_month_array_str =  config_line.substring(year_end_index + 1).split(";");
				for (int i = 0 ; i < 12 ; i++)
				{
					LinkedList<Integer> workday_day_list = new LinkedList<Integer>();
//					workday_day_list.clear();
					workday_month_array.add(workday_day_list);
				}
// Parse the date in each month
				for (String month_workday_str : workday_month_array_str)
				{
					String[] month_tmp = month_workday_str.split(":");
					if (month_tmp.length != 2)
					{
						FinanceRecorderCmnDef.format_error("Incorrect month workday list: %s", month_workday_str);
						ret = FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
						break;
					}
					int month = Integer.parseInt(month_tmp[0]);
					if (month < 1 || month > 12)
					{
						FinanceRecorderCmnDef.format_error("Incorrect month value: %d", month);
						ret = FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
						break;
					}
					String[] workday_list = month_tmp[1].split(",");
// Parse each date in a month
					LinkedList<Integer> workday_day_list = workday_month_array.get(month - 1);
					for (String workday : workday_list)
					{
						workday_day_list.add(Integer.parseInt(workday));
					}
//					if (month != workday_month_array.size())
//					{
//						FinanceRecorderCmnDef.format_error("Incorrect month, expected: %d, actual: %d", month, workday_month_array.size());
//						ret = FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
//						break OUT;
//					}
				}
				workday_map.put(year, workday_month_array);
			}
		}

		Collections.sort(workday_year_sort_array);
		workday_year_sort_array_size = workday_year_sort_array.size();
		return ret;
	}

	private final ArrayList<Integer> get_workday_key_value_sort_array()
	{
		if (workday_key_value_sort_array == null)
		{
			if (workday_map == null)
				throw new IllegalStateException("The workday_map should be initialized beforehand");
			workday_key_value_sort_array = new ArrayList<Integer>();
// Year
			for (Integer year : workday_year_sort_array)
			{
				ArrayList<LinkedList<Integer>> month_array = workday_map.get(year);
				if (month_array == null)
					throw new IllegalStateException(String.format("month_array is None in year: %04d", year));
// Month 
				for (int month = 1 ; month <= 12 ; month++)
				{
					LinkedList<Integer> day_list = month_array.get(month - 1);
					if (day_list == null)
						throw new IllegalStateException(String.format("workday_month_array is None in year: %04d-%d", year, month));
					if (day_list.isEmpty())
						continue;
// Day
					for (Integer day : day_list)
						workday_key_value_sort_array.add(FinanceRecorderCmnClass.FinanceDate.get_key_value_from_time_value(year, month, day));
				}
			}
		}
		return workday_key_value_sort_array;
	}
	
//	private boolean check_greater_than_start(int year, int month, int day)
//	{
//		FinanceRecorderCmnClass.FinanceDate finance_date = new FinanceRecorderCmnClass.FinanceDate(year, month, day);
//		boolean check = check_greater_than_start(finance_date);
//		return check;
//	}
//
//	private boolean check_greater_than_start(final FinanceRecorderCmnClass.FinanceDate finance_date)
//	{
//		if (finance_date_range.get_start_time() == null)
//			throw new RuntimeException("The start time in finance_date_range should NOT be null");
//		return (FinanceRecorderCmnClass.FinanceDate.get_int_value(finance_date) >= FinanceRecorderCmnClass.FinanceDate.get_int_value(finance_date_range.get_start_time()));
//	}

	private boolean check_less_than_end(int year, int month, int day)
	{
		FinanceRecorderCmnClass.FinanceDate finance_date = new FinanceRecorderCmnClass.FinanceDate(year, month, day);
		boolean check = check_less_than_end(finance_date);
		return check;
	}

	private boolean check_less_than_end(final FinanceRecorderCmnClass.FinanceDate finance_date)
	{
		if (finance_date_range.get_date_end() == null)
			throw new RuntimeException("The end time in finance_date_range should NOT be null");
		return finance_date.less_equal(finance_date_range.get_date_end());
	}

	private boolean is_workday(int year, int month, int day)
	{
		int[] date_index_list = new int[3];
		return FinanceRecorderCmnDef.CheckSuccess(find_data_pos(year, month, day, date_index_list, TRAVERSE_SEARCH_TYPE.TRAVERSE_SEARCH_EQUAL));
	}

	private boolean is_workday(final FinanceRecorderCmnClass.FinanceDate finance_date)
	{
		return is_workday(finance_date.get_year(), finance_date.get_month(), finance_date.get_day());
	}

	private short find_data_pos(int year, int month, int day, int[] date_index_list, TRAVERSE_SEARCH_TYPE traverse_search_type)
	{
		if (!FinanceRecorderCmnClass.FinanceDateRange.is_in_range(finance_date_range, year, month, day))
		{
			FinanceRecorderCmnDef.format_error("The date [%04d-%02d-%02d] is out of range [%s]", year, month, day, finance_date_range.toString());
			return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
		}

		ArrayList<LinkedList<Integer>> workday_month_array = workday_map.get(year);
		if (workday_month_array == null)
		{
			FinanceRecorderCmnDef.format_error("Incorrect year: %04d", year);
			return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
		}

		LinkedList<Integer> day_list = workday_month_array.get(month - 1);
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

//	private short get_date(int year_key, int month_index, int day_index, FinanceRecorderCmnClass.FinanceDate finance_date)
//	{
//		int[] date_list = new int[3];
//		short ret = get_date(year_key, month_index, day_index, date_list);
//		if (FinanceRecorderCmnDef.CheckSuccess(ret))
//		{
//			FinanceRecorderCmnClass.FinanceDate finance_date_tmp = new FinanceRecorderCmnClass.FinanceDate(date_list[0], date_list[1], date_list[2]);
//			finance_date = finance_date_tmp;
//		}
//		return ret;
//	}

	public short get_prev_workday_array(int year_base, int month_base, int day_base, ArrayList<FinanceRecorderCmnClass.FinanceDate> workday_array, int max_workday_amount)
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
					FinanceRecorderCmnClass.FinanceDate finance_date = new FinanceRecorderCmnClass.FinanceDate(cur_year, cur_month, cur_day);
					workday_array.add(finance_date);
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

//		for (FinanceRecorderCmnClass.FinanceDate workday_cfg : workday_array)
//			System.out.printf("%s ", workday_cfg.toString());
//		System.out.printf("\n");
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	public short get_next_workday_array(int year_base, int month_base, int day_base, ArrayList<FinanceRecorderCmnClass.FinanceDate> workday_array, int max_workday_amount)
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
//		int day_deque_size;
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
					FinanceRecorderCmnClass.FinanceDate finance_date = new FinanceRecorderCmnClass.FinanceDate(cur_year, cur_month, cur_day);
					workday_array.add(finance_date);
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
//		for (FinanceRecorderCmnClass.FinanceDate workday_cfg : workday_array)
//			System.out.printf("%s ", workday_cfg.toString());
//		System.out.printf("\n");
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	public short get_prev_workday(int year_base, int month_base, int day_base, int[] prev_date_value_list)
	{
// Find the date
		ArrayList<FinanceRecorderCmnClass.FinanceDate> workday_array = new ArrayList<FinanceRecorderCmnClass.FinanceDate>();
		short ret = get_prev_workday_array(year_base, month_base, day_base, workday_array, 1);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;
		if (workday_array.isEmpty())
		{
			FinanceRecorderCmnDef.warn("No data is FOUND");
			return FinanceRecorderCmnDef.RET_FAILURE_NOT_FOUND;
		}
// Update the data
		FinanceRecorderCmnClass.FinanceDate finance_date = workday_array.get(0);
		finance_date.update_time_value_list(prev_date_value_list);

		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

//	public short get_prev_workday(final FinanceRecorderCmnClass.FinanceDate finance_date, FinanceRecorderCmnClass.FinanceDate prev_finance_date)
//	{
//		int[] prev_date_value_list = new int[3];
//		short ret = get_prev_workday(finance_date.get_year(), finance_date.get_month(), finance_date.get_day(), prev_date_value_list);
//		if (FinanceRecorderCmnDef.CheckSuccess(ret))
//		{
//			int prev_year = prev_date_value_list[0], prev_month = prev_date_value_list[1], prev_day = prev_date_value_list[2];
//			prev_finance_date = new FinanceRecorderCmnClass.FinanceDate(prev_year, prev_month, prev_day);
//		}
//		return ret;
//	}

	public short get_next_workday(int year_base, int month_base, int day_base, int[] next_date_value_list)
	{
// Find the date
		ArrayList<FinanceRecorderCmnClass.FinanceDate> workday_array = new ArrayList<FinanceRecorderCmnClass.FinanceDate>();
		short ret = get_next_workday_array(year_base, month_base, day_base, workday_array, 1);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;
		if (workday_array.isEmpty())
		{
			FinanceRecorderCmnDef.warn("No data is FOUND");
			return FinanceRecorderCmnDef.RET_FAILURE_NOT_FOUND;
		}
// Update the data
		FinanceRecorderCmnClass.FinanceDate finance_date = workday_array.get(0);
		finance_date.update_time_value_list(next_date_value_list);

		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

//	public short get_next_workday(final FinanceRecorderCmnClass.FinanceDate finance_date, FinanceRecorderCmnClass.FinanceDate next_finance_date)
//	{
//		int[] next_date_value_list = new int[3];
//		short ret = get_prev_workday(finance_date.get_year(), finance_date.get_month(), finance_date.get_day(), next_date_value_list);
//		if (FinanceRecorderCmnDef.CheckSuccess(ret))
//		{
//			int prev_year = next_date_value_list[0], prev_month = next_date_value_list[1], prev_day = next_date_value_list[2];
//			next_finance_date = new FinanceRecorderCmnClass.FinanceDate(prev_year, prev_month, prev_day);
//		}
//		return ret;
//	}

	public short get_first_workday(int[] first_date_value_list)
	{
		if (first_workday_finance_date != null)
		{
			int year, month, day;
			Integer first_year = workday_year_sort_array.get(0);
			for (int month_index = 0 ; month_index < 12 ; month_index++)
			{
				LinkedList<Integer> day_list = workday_map.get(first_year).get(month_index);
				if (!day_list.isEmpty())
				{
					year = first_year;
					month = month_index + 1;
					day = day_list.get(0);
					first_workday_finance_date = new FinanceRecorderCmnClass.FinanceDate(year, month, day);
					break;
				}
			}
			assert last_workday_finance_date != null : "first_workday_finance_date should NOT be NULL";
			FinanceRecorderCmnDef.format_debug("The first workday: %s", first_workday_finance_date.toString());
		}
		first_workday_finance_date.update_time_value_list(first_date_value_list);

		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

//	public short get_first_workday(FinanceRecorderCmnClass.FinanceDate first_finance_date)
//	{
//		int[] first_date_list = new int[3];
//		short ret = get_first_workday(first_date_list);
//		if (FinanceRecorderCmnDef.CheckSuccess(ret))
//			first_finance_date = new FinanceRecorderCmnClass.FinanceDate(first_date_list[0], first_date_list[1], first_date_list[2]);
//		return ret;
//	}

	public short get_last_workday(int[] last_date_value_list)
	{
		if (last_workday_finance_date != null)
		{
			int year, month, day;
			Integer last_year = workday_year_sort_array.get(workday_year_sort_array.size() - 1);
			for (int month_index = 11 ; month_index >= 0 ; month_index--)
			{
				LinkedList<Integer> day_list = workday_map.get(last_year).get(month_index);
				if (!day_list.isEmpty())
				{
					year = last_year;
					month = month_index + 1;
					day = day_list.get(day_list.size() - 1);
					last_workday_finance_date = new FinanceRecorderCmnClass.FinanceDate(year, month, day);
					break;
				}
			}
			assert last_workday_finance_date != null : "last_workday_finance_date should NOT be NULL";
			FinanceRecorderCmnDef.format_debug("The last workday: %s", last_workday_finance_date.toString());
		}
		last_workday_finance_date.update_time_value_list(last_date_value_list);

		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

//	public short get_last_workday(FinanceRecorderCmnClass.FinanceDate last_finance_date)
//	{
//		int[] last_date_list = new int[3];
//		short ret = get_last_workday(last_date_list);
//		if (FinanceRecorderCmnDef.CheckSuccess(ret))
//			last_finance_date = new FinanceRecorderCmnClass.FinanceDate(last_date_list);
//		return ret;
//	}

//	static int main(String args[])
//	{
//		FinanceRecorderWorkdayCalendar workday_calendar = get_instance();
//		FinanceRecorderCmnClass.FinanceDate finance_date = null;
//
//		finance_date = new FinanceRecorderCmnClass.FinanceDate(2016, 6, 1);
//		workday_calendar.is_workday(finance_date);
//		return 0;
//	}
}
