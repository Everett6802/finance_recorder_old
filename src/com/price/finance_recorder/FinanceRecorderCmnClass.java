package com.price.finance_recorder;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FinanceRecorderCmnClass 
{
	public static class TimeCfg
	{
		enum TimeType{TIME_MONTH, TIME_DATE};
//		private static final String DELIM = "-";
		private TimeType time_type;
		private int year;
		private int month;
		private int day;
		private String time_str;

		public static int get_int_value(int year, int month, int day)
		{
			return ((year & 0xFFFF) << 16) | ((month & 0xFF) << 8) | (day & 0xFF);
		}
		public static int get_int_value(TimeCfg time_cfg)
		{
			return get_int_value(time_cfg.get_year(), time_cfg.get_month(), time_cfg.get_day());
		}

		private static Matcher get_time_value_matcher(String time_str, String search_pattern)
		{
	// Time Format: yyyy-mm; Ex: 2015-09
	// Time Format: yyyy-MM-dd; Ex: 2015-09-04		
			Pattern pattern = Pattern.compile(search_pattern);
			Matcher matcher = pattern.matcher(time_str);
			if (!matcher.find())
			{
//				FinanceRecorderCmnDef.format_error("Incorrect time format: %s", time_str);
				return null;
			}
			return matcher;
		}

		public static Matcher get_month_value_matcher(String time_str){return get_time_value_matcher(time_str, "([\\d]{4})-([\\d]{1,2})");}
		public static Matcher get_date_value_matcher(String time_str){return get_time_value_matcher(time_str, "([\\d]{4})-([\\d]{1,2})-([\\d]{1,2})");}

		public static int[] get_month_value(String time_str)
		{
			Matcher month_matcher = get_month_value_matcher(time_str);
			if (month_matcher == null)
			{
//				FinanceRecorderCmnDef.format_error("Incorrect time format: %s", time_str);
				return null;
			}
			int year = Integer.valueOf(month_matcher.group(1));
			int month = Integer.valueOf(month_matcher.group(2));

			return new int[]{year, month};
		}

		public static int[] get_date_value(String time_str)
		{
			Matcher date_matcher = get_date_value_matcher(time_str);
			if (date_matcher == null)
			{
//				FinanceRecorderCmnDef.format_error("Incorrect time format: %s", time_str);
				return null;
			}
			int year = Integer.valueOf(date_matcher.group(1));
			int month = Integer.valueOf(date_matcher.group(2));
			int day = Integer.valueOf(date_matcher.group(3));

			return new int[]{year, month, day};
		}

		public TimeCfg(String cur_time_str) // Format: "2015-09" or "2015-09-04"
		{
			time_str = String.format("%s", cur_time_str);
			int[] date_list = get_date_value(cur_time_str);
			if (date_list != null)
			{
// Try to parse the date format
				year = date_list[0];
				month = date_list[1];
				day = date_list[2];
				time_type = TimeType.TIME_DATE;
			}
			else
			{
				int[] month_list = get_month_value(cur_time_str);
				if (month_list == null)
				{
//					assert false : String.format("Incorrect time format: %s", cur_time_str);
					throw new IllegalArgumentException(String.format("Incorrect time format: %s", cur_time_str));
				}
// Try to parse the month format
				year = date_list[0];
				month = date_list[1];
				time_type = TimeType.TIME_MONTH;
			}
		}

		public TimeCfg(int cur_year, int cur_month)
		{
			year = cur_year;
			month = cur_month;
			day = 0;
			time_str = String.format("%04d-%02d", year, month);
			time_type = TimeType.TIME_MONTH;
		}

		public TimeCfg(int cur_year, int cur_month, int cur_day)
		{
			year = cur_year;
			month = cur_month;
			day = cur_day;
			time_str = String.format("%04d-%02d-%02d", year, month, day);
			time_type = TimeType.TIME_DATE;
		}

		public int get_year(){return year;}
		public int get_month(){return month;}
		public int get_day(){return day;}
		@Override
		public String toString(){return time_str;}

		public boolean is_month_type(){return (time_type == TimeType.TIME_MONTH);}
		@Override
		public boolean equals(Object object)
		{
			TimeCfg another_time_cfg = (TimeCfg)object;
			if (year != another_time_cfg.get_year())
				return false;
			if (month != another_time_cfg.get_month())
				return false;
			if (!is_month_type() && day != another_time_cfg.get_day())
				return false;
			return true;
		}
	};

	public static class TimeRangeCfg
	{
		private TimeCfg time_start_cfg = null;
		private TimeCfg time_end_cfg = null;
		private String time_range_description = null;
		private boolean type_is_month = false;

		public static boolean time_in_range(TimeRangeCfg time_range_cfg, TimeCfg time_cfg)
		{
			int time_cfg_value = TimeCfg.get_int_value(time_cfg);
			return (time_cfg_value >= TimeCfg.get_int_value(time_range_cfg.time_start_cfg) && time_cfg_value <= TimeCfg.get_int_value(time_range_cfg.time_end_cfg));
		}

		public static boolean time_in_range(TimeRangeCfg time_range_cfg, int year, int month, int day)
		{
			return time_in_range(time_range_cfg, new TimeCfg(year, month, day));
		}

		public TimeRangeCfg(String time_start_str, String time_end_str)
		{
			if (time_start_str != null)
				time_start_cfg = new TimeCfg(time_start_str);
			if (time_end_str != null)
				time_end_cfg = new TimeCfg(time_end_str);
			if (time_start_cfg != null && time_end_cfg != null)
			{
				if (time_start_cfg.is_month_type() != time_end_cfg.is_month_type())
				{
					String errmsg = String.format("The time format is NOT identical, start: %s, end: %s", time_start_cfg.toString(), time_end_cfg.toString());
					throw new IllegalArgumentException(errmsg);
				}
				type_is_month = time_start_cfg.is_month_type();
			}
			else if (time_start_cfg != null)
				type_is_month = time_start_cfg.is_month_type();
			else if (time_end_cfg != null)
				type_is_month = time_end_cfg.is_month_type();
			else
				throw new IllegalArgumentException("time_start_str and time_end_str should NOT be NULL simultaneously");
		}
		public TimeRangeCfg(int year_start, int month_start, int year_end, int month_end)
		{
			time_start_cfg = new TimeCfg(year_start, month_start);
			time_end_cfg = new TimeCfg(year_end, month_end);
			type_is_month = true;
		}
		public TimeRangeCfg(int year_start, int month_start, int day_start, int year_end, int month_end, int day_end)
		{
			time_start_cfg = new TimeCfg(year_start, month_start, day_start);
			time_end_cfg = new TimeCfg(year_end, month_end, day_end);
			type_is_month = false;
		}

		public boolean is_single_time()
		{
			if (time_start_cfg != null && time_end_cfg != null)
				return time_start_cfg.equals(time_end_cfg);
			return false;
		}

		public boolean is_month_type()
		{
			return type_is_month;
		}

		@Override
		public String toString() 
		{
			if (time_range_description == null)
			{
				if (time_start_cfg != null && time_end_cfg != null)
					time_range_description = String.format("%s:%s", time_start_cfg.toString(), time_end_cfg.toString());
				else if (time_start_cfg != null)
					time_range_description = String.format("%s", time_start_cfg.toString());
				else if (time_end_cfg != null)
					time_range_description = String.format("%s", time_end_cfg.toString());
			}
			return time_range_description;
		}

		public TimeCfg get_start_time()
		{
//			assert(time_start_cfg != NULL && "time_start_cfg should NOT be NULL");
			return time_start_cfg;
		}

		public String get_start_time_str()
		{
//			assert(time_start_cfg != NULL && "time_start_cfg should NOT be NULL");
			return (time_start_cfg != null ? time_start_cfg.toString() : null);
		}

		public TimeCfg get_end_time()
		{
//			assert(time_end_cfg != NULL && "time_end_cfg should NOT be NULL");
			return time_end_cfg;
		}

		public String get_end_time_str()
		{
//			assert(time_start_cfg != NULL && "time_start_cfg should NOT be NULL");
			return (time_end_cfg != null ? time_end_cfg.toString() : null);
		}

		public static int[] get_start_and_end_month_value_range(TimeRangeCfg time_range_cfg)
		{	
			assert time_range_cfg.get_start_time() != null : "The start time in time_rangte_cfg should NOT be NULL";
			int[] month_value_start = TimeCfg.get_month_value(time_range_cfg.get_start_time_str());
			if (month_value_start == null)
				return null;
			assert time_range_cfg.get_end_time() != null : "The end time in time_rangte_cfg should NOT be NULL";
			int[] month_value_end = TimeCfg.get_month_value(time_range_cfg.get_end_time_str());
			if (month_value_end == null)
				return null;

			return new int[]{month_value_start[0], month_value_start[1], month_value_end[0], month_value_end[1]};
		}
	};

	public static class SingleTimeRangeCfg extends TimeRangeCfg
	{
		public SingleTimeRangeCfg(String time_str) {super(time_str, time_str);} // Single day
		SingleTimeRangeCfg(int year, int month) {super(year, month, year, month);} // Single month
		SingleTimeRangeCfg(int year, int month, int day) {super(year, month, day, year, month, day);} // Single day
	};

	public static class FinanceDataArrayBase<T>
	{
		protected static int DEF_ARRAY_SIZE = 512;

//		protected T[] array_data;
		protected ArrayList<T> array_data = null;
		protected int array_size;
//		protected int array_pos;

		public FinanceDataArrayBase()
		{
//			array_data = (T[]) new Object [array_size];
			array_data = new ArrayList<T>();
			array_size = 0;
		}

//		protected void alloc_new()
//		{
//			
//		}

		public boolean is_empty(){return array_data.isEmpty();}
		public int get_size(){return array_size;}
//		public int get_array_size(){return array_size;}
//		public final T[] get_data_array(){return array_data;}
		public final ArrayList<T> get_data_array(){return array_data;}

		public void add(T data)
		{
//			if (array_pos + 1 >= array_size)
//				alloc_new();
//
//			array_data[array_pos++] = data;
			array_data.add(data);
			array_size++;
		}

		public final T get_index(int index)
		{
			assert array_data != null : "array_data == NULL";
			if(index < 0 && index >= array_size)
			{
				String errmsg = String.format("index[%d] is out of range: (0, %d)", index, array_size);
				FinanceRecorderCmnDef.error(errmsg);
				throw new IndexOutOfBoundsException(errmsg);
			}
//			return array_data[index];
			return array_data.get(index);
		}
	};

	public static class FinanceIntDataArray extends FinanceDataArrayBase<Integer>{};
	public static class FinanceLongDataArray extends FinanceDataArrayBase<Long>{};
	public static class FinanceFloatDataArray extends FinanceDataArrayBase<Float>{};
}
