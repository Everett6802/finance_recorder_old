package com.price.finance_recorder_cmn;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;

import com.price.finance_recorder_cmn.FinanceRecorderCmnDef.FinanceTimeUnit;


public class FinanceRecorderCmnClass 
{
	public static abstract class FinanceTimeBase
	{
		protected int year;
		protected String time_description = null;

		protected abstract int get_key_value();
		public abstract int[] get_time_value_list();
		public abstract void update_time_value_list(int[] time_value_list);
		public abstract FinanceTime get_time_object();
		public abstract FinanceRecorderCmnDef.FinanceTimeUnit get_time_unit();
	};

	public static class FinanceDate extends FinanceTimeBase implements Comparable
	{
		private static SimpleDateFormat date_formatter = new SimpleDateFormat("yyyy-MM-dd");

		public static void check_value_range(int year, int month, int day)
		{
// Check Year Range
			FinanceRecorderCmnDef.check_year_range(year);
// Check Month Range
			FinanceRecorderCmnDef.check_month_range(month);
// Check Month Range
			FinanceRecorderCmnDef.check_day_range(day, year, month);
		}
		public static int[] get_time_value_list_from_key_value(int key_value)
		{
			int year = (key_value >> 12) & 0xFFFF;
			int month = (key_value >> 8) & 0xF;
			int day = key_value & 0x3F;
			check_value_range(year, month, day);
			return new int[]{year, month, day};
		}
		public static int get_key_value_from_time_value(int year, int month, int day)
		{
			check_value_range(year, month, day);
			return (year << 12 | month << 8 | day);
		}
		public static int get_key_value_from_time_value_list(int[] time_value_list)
		{
			if (time_value_list.length != 3)
				throw new IllegalArgumentException(String.format("The date value list should be 3, not : %d", time_value_list.length));
			return get_key_value_from_time_value(time_value_list[0], time_value_list[1], time_value_list[2]);
		}
// For MySQL
		public static java.util.Date get_java_date_object(String finance_date_str) throws ParseException
		{
			java.util.Date java_date = date_formatter.parse(finance_date_str);
			return java_date;
		}
		public static java.util.Date get_java_date_object(FinanceDate finance_date) throws ParseException
		{
			return get_java_date_object(finance_date.toString());
		}
		public static java.util.Date get_java_date_object(int year, int month, int day) throws ParseException
		{
			return get_java_date_object(FinanceRecorderCmnDef.transform_date_str(year, month, day));
		}

		private int month;
		private int day;

		public FinanceDate(int[] time_value_list)
		{
			if (time_value_list.length != 3)
				throw new IllegalArgumentException(String.format("The date value list should be 3, not : %d", time_value_list.length));
			year = time_value_list[0];
			month = time_value_list[1];
			day = time_value_list[2];
		}
		public FinanceDate(int key_value)
		{
			int[] value = get_time_value_list_from_key_value(key_value);
			year = value[0];
			month = value[1];
			day = value[2];
		}
		public FinanceDate(int in_year, int in_month, int in_day)
		{
			year = in_year;
			month = in_month;
			day = in_day;
		}
		public FinanceDate(String finance_date_str)
		{
			int[] value = FinanceRecorderCmnDef.get_date_value_list_from_str(finance_date_str);
			year = value[0];
			month = value[1];
			day = value[2];
		}
		public FinanceDate(FinanceDate another)
		{
			year = another.year;
			month = another.month;
			day = another.day;
		}

		protected int get_key_value()
		{
			return get_key_value_from_time_value(year, month, day);
		}

		public int[] get_time_value_list()
		{
			return new int[]{year, month, day};
		}

		public void update_time_value_list(int[] time_value_list)
		{
			if (time_value_list.length != 3)
				throw new IllegalArgumentException(String.format("The date value list should be 3, not : %d", time_value_list.length));
			time_value_list[0] = year;
			time_value_list[1] = month;
			time_value_list[2] = day;
		}

		public FinanceTime get_time_object(){return new FinanceTime(this);}

		public FinanceRecorderCmnDef.FinanceTimeUnit get_time_unit(){return FinanceRecorderCmnDef.FinanceTimeUnit.FinanceTime_Date;}

		public int get_year(){return year;}
		public int get_month(){return  month;}
		public int get_day(){return day;}

		@Override
		public String toString()
		{
			if (time_description == null)
				time_description = FinanceRecorderCmnDef.transform_date_str(year, month, day);
			return time_description;
		}

		@Override
		public int compareTo(Object another) 
		{
			return (get_key_value() - ((FinanceDate)another).get_key_value());
		}

		@Override
		public boolean equals(Object object)
		{
			FinanceDate another_date = (FinanceDate)object;
			if (year != another_date.year)
				return false;
			if (month != another_date.month)
				return false;
			if (day != another_date.day)
				return false;
			return true;
		}

		public boolean less(FinanceDate another){return (compareTo(another) < 0);}
		public boolean less_equal(FinanceDate another){return (compareTo(another) <= 0);}
		public boolean equal(FinanceDate another){return (compareTo(another) == 0);}
		public boolean not_equal(FinanceDate another){return (compareTo(another) != 0);}
		public boolean greater(FinanceDate another){return (compareTo(another) > 0);}
		public boolean greater_equal(FinanceDate another){return (compareTo(another) >= 0);}

		public FinanceMonth get_month_object(){return new FinanceMonth(year, month);}
		public FinanceQuarter get_quarter_object(){return new FinanceQuarter(year, FinanceRecorderCmnDef.get_quarter_from_month(month));}
	};

	public static class FinanceMonth extends FinanceTimeBase implements Comparable
	{
		static public void check_value_range(int year, int month)
		{
// Check Year Range
			FinanceRecorderCmnDef.check_year_range(year);
// Check Month Range
			FinanceRecorderCmnDef.check_month_range(month);
		}
		public static int[] get_time_value_list_from_key_value(int key_value)
		{
			int year = (key_value >> 4) & 0xFFFF;
			int month = key_value & 0xF;
			check_value_range(year, month);
			return new int[]{year, month};
		}
		public static int get_key_value_from_time_value(int year, int month)
		{
			check_value_range(year, month);
			return (year << 4 | month);
		}
		public static int get_key_value_from_time_value_list(int[] time_value_list)
		{
			if (time_value_list.length != 2)
				throw new IllegalArgumentException(String.format("The month value list should be 2, not : %d", time_value_list.length));
			return get_key_value_from_time_value(time_value_list[0], time_value_list[1]);
		}

		private int month;

		public FinanceMonth(int[] time_value_list)
		{
			if (time_value_list.length != 2)
				throw new IllegalArgumentException(String.format("The month value list should be 2, not : %d", time_value_list.length));
			year = time_value_list[0];
			month = time_value_list[1];
		}
		public FinanceMonth(int key_value)
		{
			int[] value = get_time_value_list_from_key_value(key_value);
			year = value[0];
			month = value[1];
		}
		public FinanceMonth(int in_year, int in_month)
		{
			year = in_year;
			month = in_month;
		}
		public FinanceMonth(String finance_month_str)
		{
			int[] value = FinanceRecorderCmnDef.get_month_value_list_from_str(finance_month_str);
			year = value[0];
			month = value[1];
		}
		public FinanceMonth(FinanceMonth another)
		{
			year = another.year;
			month = another.month;
		}

		protected int get_key_value()
		{
			return get_key_value_from_time_value(year, month);
		}

		public int[] get_time_value_list()
		{
			return new int[]{year, month};
		}

		public void update_time_value_list(int[] time_value_list)
		{
			if (time_value_list.length != 2)
				throw new IllegalArgumentException(String.format("The month value list should be 2, not : %d", time_value_list.length));
			time_value_list[0] = year;
			time_value_list[1] = month;
		}

		public int get_year(){return year;}
		public int get_month(){return  month;}

		public FinanceTime get_time_object(){return new FinanceTime(this);}

		public FinanceRecorderCmnDef.FinanceTimeUnit get_time_unit(){return FinanceRecorderCmnDef.FinanceTimeUnit.FinanceTime_Month;}

		@Override
		public String toString()
		{
			if (time_description == null)
				time_description = FinanceRecorderCmnDef.transform_month_str(year, month);
			return time_description;
		}

		@Override
		public int compareTo(Object another) 
		{
			return (get_key_value() - ((FinanceMonth)another).get_key_value());
		}

		@Override
		public boolean equals(Object object)
		{
			FinanceMonth another_month = (FinanceMonth)object;
			if (year != another_month.year)
				return false;
			if (month != another_month.month)
				return false;
			return true;
		}

		public boolean less(FinanceMonth another){return (compareTo(another) < 0);}
		public boolean less_equal(FinanceMonth another){return (compareTo(another) <= 0);}
		public boolean equal(FinanceMonth another){return (compareTo(another) == 0);}
		public boolean not_equal(FinanceMonth another){return (compareTo(another) != 0);}
		public boolean greater(FinanceMonth another){return (compareTo(another) > 0);}
		public boolean greater_equal(FinanceMonth another){return (compareTo(another) >= 0);}

		public FinanceDate get_date_start_object(){return new FinanceDate(year, month, 1);}
		public FinanceDate get_date_end_object(){return new FinanceDate(year, month, FinanceRecorderCmnDef.get_month_last_day(year, month));}
		public FinanceQuarter get_quarter_object(){return new FinanceQuarter(year, FinanceRecorderCmnDef.get_quarter_from_month(month));}
	};

	public static class FinanceQuarter extends FinanceTimeBase implements Comparable
	{
		static public void check_value_range(int year, int quarter)
		{
// Check Year Range
			FinanceRecorderCmnDef.check_year_range(year);
// Check Quarter Range
			FinanceRecorderCmnDef.check_quarter_range(quarter);
		}
		public static int[] get_time_value_list_from_key_value(int key_value)
		{
			int year = (key_value >> 3) & 0xFFFF;
			int quarter = key_value & 0x7;
			check_value_range(year, quarter);
			return new int[]{year, quarter};
		}
		public static int get_key_value_from_time_value(int year, int quarter)
		{
			check_value_range(year, quarter);
			return (year << 3 | quarter);
		}
		public static int get_key_value_from_time_value_list(int[] time_value_list)
		{
			if (time_value_list.length != 2)
				throw new IllegalArgumentException(String.format("The quarter value list should be 2, not : %d", time_value_list.length));
			return get_key_value_from_time_value(time_value_list[0], time_value_list[1]);
		}

		private int quarter;

		public FinanceQuarter(int[] time_value_list)
		{
			if (time_value_list.length != 3)
				throw new IllegalArgumentException(String.format("The quarter value list should be 2, not : %d", time_value_list.length));
			year = time_value_list[0];
			quarter = time_value_list[1];
		}
		public FinanceQuarter(int key_value)
		{
			int[] value = get_time_value_list_from_key_value(key_value);
			year = value[0];
			quarter = value[1];
		}
		public FinanceQuarter(int in_year, int in_quarter)
		{
			year = in_year;
			quarter = in_quarter;
		}
		public FinanceQuarter(String finance_quarter_str)
		{
			int[] value = FinanceRecorderCmnDef.get_quarter_value_list_from_str(finance_quarter_str);
			year = value[0];
			quarter = value[1];
		}
		public FinanceQuarter(FinanceQuarter another)
		{
			year = another.year;
			quarter = another.quarter;
		}

		protected int get_key_value()
		{
			return get_key_value_from_time_value(year, quarter);
		}

		public int[] get_time_value_list()
		{
			return new int[]{year, quarter};
		}

		public void update_time_value_list(int[] time_value_list)
		{
			if (time_value_list.length != 2)
				throw new IllegalArgumentException(String.format("The quarter value list should be 2, not : %d", time_value_list.length));
			time_value_list[0] = year;
			time_value_list[1] = quarter;
		}

		public int get_year(){return year;}
		public int get_quarter(){return quarter;}
	
		public FinanceTime get_time_object(){return new FinanceTime(this);}

		public FinanceRecorderCmnDef.FinanceTimeUnit get_time_unit(){return FinanceRecorderCmnDef.FinanceTimeUnit.FinanceTime_Quarter;}

		@Override
		public String toString()
		{
			if (time_description == null)
				time_description = FinanceRecorderCmnDef.transform_quarter_str(year, quarter);
			return time_description;
		}

		@Override
		public int compareTo(Object another) 
		{
			return (get_key_value() - ((FinanceQuarter)another).get_key_value());
		}

		@Override
		public boolean equals(Object object)
		{
			FinanceQuarter another_quarter = (FinanceQuarter)object;
			if (year != another_quarter.year)
				return false;
			if (quarter != another_quarter.quarter)
				return false;
			return true;
		}

		public boolean less(FinanceQuarter another){return (compareTo(another) < 0);}
		public boolean less_equal(FinanceQuarter another){return (compareTo(another) <= 0);}
		public boolean equal(FinanceQuarter another){return (compareTo(another) == 0);}
		public boolean not_equal(FinanceQuarter another){return (compareTo(another) != 0);}
		public boolean greater(FinanceQuarter another){return (compareTo(another) > 0);}
		public boolean greater_equal(FinanceQuarter another){return (compareTo(another) >= 0);}

		public FinanceDate get_date_start_object(){return new FinanceDate(year, FinanceRecorderCmnDef.get_quarter_first_month(quarter), 1);}
		public FinanceDate get_date_end_object()
		{
			int month = FinanceRecorderCmnDef.get_quarter_last_month(quarter);
			return new FinanceDate(year, month, FinanceRecorderCmnDef.get_quarter_last_day(month));
		}
		public FinanceMonth get_month_start_object(){return new FinanceMonth(year, FinanceRecorderCmnDef.get_quarter_first_month(quarter));}
		public FinanceMonth get_month_end_object(){return new FinanceMonth(year, FinanceRecorderCmnDef.get_quarter_last_month(quarter));}
	};

	public static class FinanceTime
	{
		private FinanceTimeBase finance_time = null;
		private FinanceRecorderCmnDef.FinanceTimeUnit finance_time_unit = FinanceRecorderCmnDef.FinanceTimeUnit.FinanceTime_Undefined;

		public FinanceTime(FinanceDate in_finance_date)
		{
			finance_time = new FinanceDate(in_finance_date);
			finance_time_unit = FinanceRecorderCmnDef.FinanceTimeUnit.FinanceTime_Date;
		}
		public FinanceTime(FinanceMonth in_finance_month)
		{
			finance_time = new FinanceMonth(in_finance_month);
			finance_time_unit = FinanceRecorderCmnDef.FinanceTimeUnit.FinanceTime_Month;
		}
		public FinanceTime(FinanceQuarter in_finance_quarter)
		{
			finance_time = new FinanceQuarter(in_finance_quarter);
			finance_time_unit = FinanceRecorderCmnDef.FinanceTimeUnit.FinanceTime_Quarter;
		}
		public FinanceTime(String in_finance_time)
		{
			finance_time_unit = FinanceRecorderCmnDef.get_time_unit_from_string(in_finance_time);
// Initialize the instance
			switch (finance_time_unit)
			{
			case FinanceTime_Date:
			{
				finance_time = new FinanceDate(in_finance_time);
			}
			break;
			case FinanceTime_Month:
			{
				finance_time = new FinanceMonth(in_finance_time);
			}
			break;
			case FinanceTime_Quarter:
			{
				finance_time = new FinanceQuarter(in_finance_time);
			}
			break;
			default:
				throw new IllegalStateException(String.format("Unknow time unit: %d", finance_time_unit.value()));
			}
		}
		public FinanceTime(int[] time_value_list, FinanceRecorderCmnDef.FinanceTimeUnit in_finance_time_unit)
		{
// Initialize the instance
			switch (in_finance_time_unit)
			{
			case FinanceTime_Date:
			{
				finance_time = new FinanceDate(time_value_list);
			}
			break;
			case FinanceTime_Month:
			{
				finance_time = new FinanceMonth(time_value_list);
			}
			break;
			case FinanceTime_Quarter:
			{
				finance_time = new FinanceQuarter(time_value_list);
			}
			break;
			default:
				throw new IllegalStateException(String.format("Unknow time unit: %d", in_finance_time_unit.value()));
			}
			finance_time_unit = in_finance_time_unit;
		}
		public FinanceTime(int key_value, FinanceRecorderCmnDef.FinanceTimeUnit in_finance_time_unit)
		{
// Initialize the instance
			switch (in_finance_time_unit)
			{
			case FinanceTime_Date:
			{
				finance_time = new FinanceDate(key_value);
			}
			break;
			case FinanceTime_Month:
			{
				finance_time = new FinanceMonth(key_value);
			}
			break;
			case FinanceTime_Quarter:
			{
				finance_time = new FinanceQuarter(key_value);
			}
			break;
			default:
				throw new IllegalStateException(String.format("Unknow time unit: %d", in_finance_time_unit.value()));
			}
			finance_time_unit = in_finance_time_unit;
		}

		public int[] get_time_value_list(){return finance_time.get_time_value_list();}
		public FinanceRecorderCmnDef.FinanceTimeUnit get_time_unit(){return finance_time_unit;}
	};

	public abstract static class FinanceTimeRangeBase
	{
		protected static FinanceRecorderCmnDef.FinanceTimeRangeType get_time_range_type(Object time_start, Object time_end)
		{
			if (time_start == null && time_end == null)
				throw new IllegalArgumentException("start and end time can NOT be null at the same time");
			if (time_start == null)
				return FinanceRecorderCmnDef.FinanceTimeRangeType.FinanceTimeRange_LessEqual;
			else if (time_end == null)
				return FinanceRecorderCmnDef.FinanceTimeRangeType.FinanceTimeRange_GreaterEqual;
			return FinanceRecorderCmnDef.FinanceTimeRangeType.FinanceTimeRange_Between;
		}

		protected FinanceRecorderCmnDef.FinanceTimeRangeType finance_time_range_type = null;

		public FinanceRecorderCmnDef.FinanceTimeRangeType get_time_range_type(){return finance_time_range_type;}

		public abstract int[] get_time_start_value_list();
		public abstract int[] get_time_end_value_list();
		public abstract int[] get_time_range_value_list();
		public abstract String get_time_start_string();
		public abstract String get_time_end_string();
		public abstract int get_time_start_key_value();
		public abstract int get_time_end_key_value();
		public abstract FinanceTimeRange get_time_object();
		public abstract FinanceRecorderCmnDef.FinanceTimeUnit get_time_unit();
	};

	public static class FinanceDateRange extends FinanceTimeRangeBase
	{
		public static boolean is_in_range(FinanceDateRange finance_date_range, int year, int month, int day)
		{
			return is_in_range(finance_date_range, new FinanceDate(year, month, day));
		}
		public static boolean is_in_range(FinanceDateRange finance_date_range, String cur_finance_date_str)
		{
			return is_in_range(finance_date_range, new FinanceDate(cur_finance_date_str));
		}
		public static boolean is_in_range(FinanceDateRange finance_date_range, FinanceDate cur_finance_date)
		{
			switch (finance_date_range.finance_time_range_type)
			{
			case FinanceTimeRange_Between:
				return cur_finance_date.greater_equal(finance_date_range.get_date_start()) && cur_finance_date.less_equal(finance_date_range.get_date_end());
			case FinanceTimeRange_LessEqual:
				return cur_finance_date.less_equal(finance_date_range.get_date_end());
			case FinanceTimeRange_GreaterEqual:
				return cur_finance_date.greater_equal(finance_date_range.get_date_start());
			default:
				throw new IllegalArgumentException(String.format("Unsupported time range: %d", finance_date_range.finance_time_range_type.value()));
			}
//// Should not reach
//			return false;
		}

		public static boolean is_overlap(FinanceDateRange finance_date_range1, FinanceDateRange finance_date_range2)
		{
			boolean check_overlap1 = true;
			if (finance_date_range1.finance_time_range_type.is_time_start_exist() && finance_date_range2.finance_time_range_type.is_time_end_exist())
			{
				FinanceDate finance_date1_start = finance_date_range1.get_date_start();
				FinanceDate finance_date2_end = finance_date_range2.get_date_end();
				check_overlap1 = finance_date1_start.less_equal(finance_date2_end);
			}
			boolean check_overlap2 = true;
			if (finance_date_range2.finance_time_range_type.is_time_start_exist() && finance_date_range1.finance_time_range_type.is_time_end_exist())
			{
				FinanceDate finance_date2_start = finance_date_range2.get_date_start();
				FinanceDate finance_date1_end = finance_date_range1.get_date_end();
				check_overlap2 = finance_date2_start.less_equal(finance_date1_end);
			}
			return (check_overlap1 && check_overlap2);
		}

		private FinanceDate finance_date_start = null;
		private FinanceDate finance_date_end = null;
		protected String time_range_description = null;

		public FinanceDateRange(FinanceDate in_finance_date_start, FinanceDate in_finance_date_end)
		{
			finance_time_range_type = get_time_range_type(in_finance_date_start, in_finance_date_end);
			if (in_finance_date_start != null)
				finance_date_start = new FinanceDate(in_finance_date_start);
			if (in_finance_date_end != null)
				finance_date_end = new FinanceDate(in_finance_date_end);
		}
		public FinanceDateRange(int year_start, int month_start, int day_start, int year_end, int month_end, int day_end)
		{
			finance_time_range_type = FinanceRecorderCmnDef.FinanceTimeRangeType.FinanceTimeRange_Between;
			finance_date_start = new FinanceDate(year_start, month_start, day_start);
			finance_date_end = new FinanceDate(year_end, month_end, day_end);
		}
		public FinanceDateRange(String finance_date_start_str, String finance_date_end_str)
		{
			finance_time_range_type = get_time_range_type(finance_date_start_str, finance_date_end_str);
			if (finance_date_start_str != null)
				finance_date_start = new FinanceDate(finance_date_start_str);
			if (finance_date_end_str != null)
				finance_date_end = new FinanceDate(finance_date_end_str);
		}
		public FinanceDateRange(FinanceDateRange another)
		{
			this.finance_time_range_type = another.finance_time_range_type;
			this.finance_date_start = new FinanceDate(another.finance_date_start);
			this.finance_date_end = new FinanceDate(another.finance_date_end);
		}

		@Override
		public String toString() 
		{
			if (time_range_description == null)
			{
				switch (finance_time_range_type)
				{
				case FinanceTimeRange_Between:
					time_range_description = String.format("%s-%s", finance_date_start.toString(), finance_date_end.toString());
					break;
				case FinanceTimeRange_LessEqual:
					time_range_description = String.format("MinDate-%s", finance_date_end.toString());
					break;
				case FinanceTimeRange_GreaterEqual:
					time_range_description = String.format("%s-MaxDate", finance_date_start.toString());
					break;
				default:
					throw new IllegalArgumentException(String.format("Unsupported date range: %d", finance_time_range_type.value()));
				}
			}
			return time_range_description;
		}

		public final FinanceDate get_date_start(){return finance_date_start;}
		public final FinanceDate get_date_end(){return finance_date_end;}
		public int[] get_time_start_value_list()
		{
			if (!finance_time_range_type.is_time_start_exist())
				throw new IllegalStateException("Start Date does NOT exist");
			return finance_date_start.get_time_value_list();
		}
		public int[] get_time_end_value_list()
		{
			if (!finance_time_range_type.is_time_end_exist())
				throw new IllegalStateException("End Date does NOT exist");
			return finance_date_end.get_time_value_list();
		}
		public int[] get_time_range_value_list()
		{
			if (!finance_time_range_type.is_time_range_exist())
				throw new IllegalStateException("Start/End Date does NOT exist");
			int[] start_key_value_list = get_time_start_value_list();
			int[] end_key_value_list = get_time_end_value_list();
			return new int[]{start_key_value_list[0], start_key_value_list[1], start_key_value_list[2], end_key_value_list[0], end_key_value_list[1], end_key_value_list[2]};
		}
		public String get_time_start_string()
		{
			if (!finance_time_range_type.is_time_start_exist())
				throw new IllegalStateException("Start Date does NOT exist");
			return finance_date_start.toString();
		}
		public String get_time_end_string()
		{
			if (!finance_time_range_type.is_time_end_exist())
				throw new IllegalStateException("End Date does NOT exist");
			return finance_date_end.toString();
		}
		public int get_time_start_key_value()
		{
			if (!finance_time_range_type.is_time_start_exist())
				throw new IllegalStateException("Start Date does NOT exist");
			return finance_date_start.get_key_value();
		}
		public int get_time_end_key_value()
		{
			if (!finance_time_range_type.is_time_end_exist())
				throw new IllegalStateException("End Date does NOT exist");
			return finance_date_end.get_key_value();
		}

		public FinanceTimeRange get_time_object(){return new FinanceTimeRange(finance_date_start, finance_date_end);}
		public FinanceRecorderCmnDef.FinanceTimeUnit get_time_unit(){return FinanceRecorderCmnDef.FinanceTimeUnit.FinanceTime_Date;}

		public FinanceMonthRange get_month_object()
		{
			return new FinanceMonthRange(
				finance_time_range_type.is_time_start_exist() ? finance_date_start.get_month_object() : null, 
				finance_time_range_type.is_time_end_exist() ? finance_date_end.get_month_object() : null
			);
		}
		public FinanceQuarterRange get_quarter_object()
		{
			return new FinanceQuarterRange(
				finance_time_range_type.is_time_start_exist() ? finance_date_start.get_quarter_object() : null, 
				finance_time_range_type.is_time_end_exist() ? finance_date_end.get_quarter_object() : null
			);
		}
	};

	public static class FinanceMonthRange extends FinanceTimeRangeBase
	{
		public static boolean is_in_range(FinanceMonthRange finance_month_range, int year, int month)
		{
			return is_in_range(finance_month_range, new FinanceMonth(year, month));
		}
		public static boolean is_in_range(FinanceMonthRange finance_month_range, String cur_finance_month_str)
		{
			return is_in_range(finance_month_range, new FinanceMonth(cur_finance_month_str));
		}
		public static boolean is_in_range(FinanceMonthRange finance_month_range, FinanceMonth cur_finance_month)
		{
			switch (finance_month_range.finance_time_range_type)
			{
			case FinanceTimeRange_Between:
				return cur_finance_month.greater_equal(finance_month_range.get_month_start()) && cur_finance_month.less_equal(finance_month_range.get_month_end());
			case FinanceTimeRange_LessEqual:
				return cur_finance_month.less_equal(finance_month_range.get_month_end());
			case FinanceTimeRange_GreaterEqual:
				return cur_finance_month.greater_equal(finance_month_range.get_month_start());
			default:
				throw new IllegalArgumentException(String.format("Unsupported month range: %d", finance_month_range.finance_time_range_type.value()));
			}
		}

		public static boolean is_overlap(FinanceMonthRange finance_month_range1, FinanceMonthRange finance_month_range2)
		{
			boolean check_overlap1 = true;
			if (finance_month_range1.finance_time_range_type.is_time_start_exist() && finance_month_range2.finance_time_range_type.is_time_end_exist())
			{
				FinanceMonth finance_month1_start = finance_month_range1.get_month_start();
				FinanceMonth finance_month2_end = finance_month_range2.get_month_end();
				check_overlap1 = finance_month1_start.less_equal(finance_month2_end);
			}
			boolean check_overlap2 = true;
			if (finance_month_range2.finance_time_range_type.is_time_start_exist() && finance_month_range1.finance_time_range_type.is_time_end_exist())
			{
				FinanceMonth finance_month2_start = finance_month_range2.get_month_start();
				FinanceMonth finance_month1_end = finance_month_range1.get_month_end();
				check_overlap2 = finance_month2_start.less_equal(finance_month1_end);
			}
			return (check_overlap1 && check_overlap2);
		}

		private FinanceMonth finance_month_start = null;
		private FinanceMonth finance_month_end = null;
		protected String time_range_description = null;

		public FinanceMonthRange(FinanceMonth in_finance_month_start, FinanceMonth in_finance_month_end)
		{
			finance_time_range_type = get_time_range_type(in_finance_month_start, in_finance_month_end);
			if (in_finance_month_start != null)
				finance_month_start = new FinanceMonth(in_finance_month_start);
			if (in_finance_month_end != null)
				finance_month_end = new FinanceMonth(in_finance_month_end);
		}
		public FinanceMonthRange(int year_start, int month_start, int year_end, int month_end)
		{
			finance_time_range_type = FinanceRecorderCmnDef.FinanceTimeRangeType.FinanceTimeRange_Between;
			finance_month_start = new FinanceMonth(year_start, month_start);
			finance_month_end = new FinanceMonth(year_end, month_end);
		}
		public FinanceMonthRange(String finance_month_start_str, String finance_month_end_str)
		{
			finance_time_range_type = get_time_range_type(finance_month_start_str, finance_month_end_str);
			if (finance_month_start_str != null)
				finance_month_start = new FinanceMonth(finance_month_start_str);
			if (finance_month_end_str != null)
				finance_month_end = new FinanceMonth(finance_month_end_str);
		}
		public FinanceMonthRange(FinanceMonthRange another)
		{
			this.finance_time_range_type = another.finance_time_range_type;
			this.finance_month_start = new FinanceMonth(another.finance_month_start);
			this.finance_month_end = new FinanceMonth(another.finance_month_end);
		}

		@Override
		public String toString() 
		{
			if (time_range_description == null)
			{
				switch (finance_time_range_type)
				{
				case FinanceTimeRange_Between:
					time_range_description = String.format("%s-%s", finance_month_start.toString(), finance_month_end.toString());
					break;
				case FinanceTimeRange_LessEqual:
					time_range_description = String.format("MinMonth-%s", finance_month_end.toString());
					break;
				case FinanceTimeRange_GreaterEqual:
					time_range_description = String.format("%s-MaxMonth", finance_month_start.toString());
					break;
				default:
					throw new IllegalArgumentException(String.format("Unsupported month range: %d", finance_time_range_type.value()));
				}
			}
			return time_range_description;
		}

		public final FinanceMonth get_month_start(){return finance_month_start;}
		public final FinanceMonth get_month_end(){return finance_month_end;}
		public int[] get_time_start_value_list()
		{
			if (!finance_time_range_type.is_time_start_exist())
				throw new IllegalStateException("Start Month does NOT exist");
			return finance_month_start.get_time_value_list();
		}
		public int[] get_time_end_value_list()
		{
			if (!finance_time_range_type.is_time_end_exist())
				throw new IllegalStateException("End Month does NOT exist");
			return finance_month_end.get_time_value_list();
		}
		public int[] get_time_range_value_list()
		{
			if (!finance_time_range_type.is_time_range_exist())
				throw new IllegalStateException("Start/End Month does NOT exist");
			int[] start_key_value_list = get_time_start_value_list();
			int[] end_key_value_list = get_time_end_value_list();
			return new int[]{start_key_value_list[0], start_key_value_list[1], start_key_value_list[2], end_key_value_list[0], end_key_value_list[1], end_key_value_list[2]};
		}
		public String get_time_start_string()
		{
			if (!finance_time_range_type.is_time_start_exist())
				throw new IllegalStateException("Start Month does NOT exist");
			return finance_month_start.toString();
		}
		public String get_time_end_string()
		{
			if (!finance_time_range_type.is_time_end_exist())
				throw new IllegalStateException("End Month does NOT exist");
			return finance_month_end.toString();
		}
		public int get_time_start_key_value()
		{
			if (!finance_time_range_type.is_time_start_exist())
				throw new IllegalStateException("Start Month does NOT exist");
			return finance_month_start.get_key_value();
		}
		public int get_time_end_key_value()
		{
			if (!finance_time_range_type.is_time_end_exist())
				throw new IllegalStateException("End Month does NOT exist");
			return finance_month_end.get_key_value();
		}

		public FinanceTimeRange get_time_object(){return new FinanceTimeRange(finance_month_start, finance_month_end);}
		public FinanceRecorderCmnDef.FinanceTimeUnit get_time_unit(){return FinanceRecorderCmnDef.FinanceTimeUnit.FinanceTime_Month;}

		public FinanceDateRange get_date_object()
		{
			return new FinanceDateRange(
				finance_time_range_type.is_time_start_exist() ? finance_month_start.get_date_start_object() : null, 
				finance_time_range_type.is_time_end_exist() ? finance_month_end.get_date_end_object() : null
			);
		}
		public FinanceQuarterRange get_quarter_object()
		{
			return new FinanceQuarterRange(
				finance_time_range_type.is_time_start_exist() ? finance_month_start.get_quarter_object() : null, 
				finance_time_range_type.is_time_end_exist() ? finance_month_end.get_quarter_object() : null
			);
		}
	};

	public static class FinanceQuarterRange extends FinanceTimeRangeBase
	{
		public static boolean is_in_range(FinanceQuarterRange finance_quarter_range, int year, int month)
		{
			return is_in_range(finance_quarter_range, new FinanceQuarter(year, month));
		}
		public static boolean is_in_range(FinanceQuarterRange finance_quarter_range, String cur_finance_quarter_str)
		{
			return is_in_range(finance_quarter_range, new FinanceQuarter(cur_finance_quarter_str));
		}
		public static boolean is_in_range(FinanceQuarterRange finance_quarter_range, FinanceQuarter cur_finance_quarter)
		{
			switch (finance_quarter_range.finance_time_range_type)
			{
			case FinanceTimeRange_Between:
				return cur_finance_quarter.greater_equal(finance_quarter_range.get_quarter_start()) && cur_finance_quarter.less_equal(finance_quarter_range.get_quarter_end());
			case FinanceTimeRange_LessEqual:
				return cur_finance_quarter.less_equal(finance_quarter_range.get_quarter_end());
			case FinanceTimeRange_GreaterEqual:
				return cur_finance_quarter.greater_equal(finance_quarter_range.get_quarter_start());
			default:
				throw new IllegalArgumentException(String.format("Unsupported quarter range: %d", finance_quarter_range.finance_time_range_type.value()));
			}
		}

		public static boolean is_overlap(FinanceQuarterRange finance_quarter_range1, FinanceQuarterRange finance_quarter_range2)
		{
			boolean check_overlap1 = true;
			if (finance_quarter_range1.finance_time_range_type.is_time_start_exist() && finance_quarter_range2.finance_time_range_type.is_time_end_exist())
			{
				FinanceQuarter finance_quarter1_start = finance_quarter_range1.get_quarter_start();
				FinanceQuarter finance_quarter2_end = finance_quarter_range2.get_quarter_end();
				check_overlap1 = finance_quarter1_start.less_equal(finance_quarter2_end);
			}
			boolean check_overlap2 = true;
			if (finance_quarter_range2.finance_time_range_type.is_time_start_exist() && finance_quarter_range1.finance_time_range_type.is_time_end_exist())
			{
				FinanceQuarter finance_quarter2_start = finance_quarter_range2.get_quarter_start();
				FinanceQuarter finance_quarter1_end = finance_quarter_range1.get_quarter_end();
				check_overlap2 = finance_quarter2_start.less_equal(finance_quarter1_end);
			}
			return (check_overlap1 && check_overlap2);
		}

		private FinanceQuarter finance_quarter_start = null;
		private FinanceQuarter finance_quarter_end = null;
		protected String time_range_description = null;

		public FinanceQuarterRange(FinanceQuarter in_finance_quarter_start, FinanceQuarter in_finance_quarter_end)
		{
			finance_time_range_type = get_time_range_type(in_finance_quarter_start, in_finance_quarter_end);
			if (in_finance_quarter_start != null)
				finance_quarter_start = new FinanceQuarter(in_finance_quarter_start);
			if (in_finance_quarter_end != null)
				finance_quarter_end = new FinanceQuarter(in_finance_quarter_end);
		}
		public FinanceQuarterRange(int year_start, int quarter_start, int year_end, int quarter_end)
		{
			finance_time_range_type = FinanceRecorderCmnDef.FinanceTimeRangeType.FinanceTimeRange_Between;
			finance_quarter_start = new FinanceQuarter(year_start, quarter_start);
			finance_quarter_end = new FinanceQuarter(year_end, quarter_end);
		}
		public FinanceQuarterRange(String finance_quarter_start_str, String finance_quarter_end_str)
		{
			finance_time_range_type = get_time_range_type(finance_quarter_start_str, finance_quarter_end_str);
			if (finance_quarter_start_str != null)
				finance_quarter_start = new FinanceQuarter(finance_quarter_start_str);
			if (finance_quarter_end_str != null)
				finance_quarter_end = new FinanceQuarter(finance_quarter_end_str);
		}
		public FinanceQuarterRange(FinanceQuarterRange another)
		{
			this.finance_time_range_type = another.finance_time_range_type;
			this.finance_quarter_start = new FinanceQuarter(another.finance_quarter_start);
			this.finance_quarter_end = new FinanceQuarter(another.finance_quarter_end);
		}

		@Override
		public String toString() 
		{
			if (time_range_description == null)
			{
				switch (finance_time_range_type)
				{
				case FinanceTimeRange_Between:
					time_range_description = String.format("%s-%s", finance_quarter_start.toString(), finance_quarter_end.toString());
					break;
				case FinanceTimeRange_LessEqual:
					time_range_description = String.format("MinQuarter-%s", finance_quarter_end.toString());
					break;
				case FinanceTimeRange_GreaterEqual:
					time_range_description = String.format("%s-MaxQuarter", finance_quarter_start.toString());
					break;
				default:
					throw new IllegalArgumentException(String.format("Unsupported quarter range: %d", finance_time_range_type.value()));
				}
			}
			return time_range_description;
		}

		public final FinanceQuarter get_quarter_start(){return finance_quarter_start;}
		public final FinanceQuarter get_quarter_end(){return finance_quarter_end;}
		public int[] get_time_start_value_list()
		{
			if (!finance_time_range_type.is_time_start_exist())
				throw new IllegalStateException("Start Quarter does NOT exist");
			return finance_quarter_start.get_time_value_list();
		}
		public int[] get_time_end_value_list()
		{
			if (!finance_time_range_type.is_time_end_exist())
				throw new IllegalStateException("End Quarter does NOT exist");
			return finance_quarter_end.get_time_value_list();
		}
		public int[] get_time_range_value_list()
		{
			if (!finance_time_range_type.is_time_range_exist())
				throw new IllegalStateException("Start/End Quarter does NOT exist");
			int[] start_key_value_list = get_time_start_value_list();
			int[] end_key_value_list = get_time_end_value_list();
			return new int[]{start_key_value_list[0], start_key_value_list[1], start_key_value_list[2], end_key_value_list[0], end_key_value_list[1], end_key_value_list[2]};
		}
		public String get_time_start_string()
		{
			if (!finance_time_range_type.is_time_start_exist())
				throw new IllegalStateException("Start Quarter does NOT exist");
			return finance_quarter_start.toString();
		}
		public String get_time_end_string()
		{
			if (!finance_time_range_type.is_time_end_exist())
				throw new IllegalStateException("End Quarter does NOT exist");
			return finance_quarter_end.toString();
		}
		public int get_time_start_key_value()
		{
			if (!finance_time_range_type.is_time_start_exist())
				throw new IllegalStateException("Start Quarter does NOT exist");
			return finance_quarter_start.get_key_value();
		}
		public int get_time_end_key_value()
		{
			if (!finance_time_range_type.is_time_end_exist())
				throw new IllegalStateException("End Quarter does NOT exist");
			return finance_quarter_end.get_key_value();
		}

		public FinanceTimeRange get_time_object(){return new FinanceTimeRange(finance_quarter_start, finance_quarter_end);}
		public FinanceRecorderCmnDef.FinanceTimeUnit get_time_unit(){return FinanceRecorderCmnDef.FinanceTimeUnit.FinanceTime_Quarter;}

		public FinanceDateRange get_date_object()
		{
			return new FinanceDateRange(
				finance_time_range_type.is_time_start_exist() ? finance_quarter_start.get_date_start_object() : null, 
				finance_time_range_type.is_time_end_exist() ? finance_quarter_end.get_date_end_object() : null
			);
		}
		public FinanceMonthRange get_month_object()
		{
			return new FinanceMonthRange(
				finance_time_range_type.is_time_start_exist() ? finance_quarter_start.get_month_start_object() : null, 
				finance_time_range_type.is_time_end_exist() ? finance_quarter_end.get_month_end_object() : null
			);
		}
	};

	public static class FinanceTimeRange// extends FinanceTimeRangeBase
	{
		public static boolean is_overlap(FinanceTimeRange finance_time_range1, FinanceTimeRange finance_time_range2)
		{
			boolean check_overlap1 = true;
			if (finance_time_range1.finance_time_range_type.is_time_start_exist() && finance_time_range2.finance_time_range_type.is_time_end_exist())
			{
				int finance_time1_start_value = finance_time_range1.get_time_start_key_value();
				int finance_time2_end_value = finance_time_range2.get_time_end_key_value();
				check_overlap1 = finance_time1_start_value <= finance_time2_end_value;
			}
			boolean check_overlap2 = true;
			if (finance_time_range2.finance_time_range_type.is_time_start_exist() && finance_time_range1.finance_time_range_type.is_time_end_exist())
			{
				int finance_time2_start_value = finance_time_range2.get_time_start_key_value();
				int finance_time1_end_value = finance_time_range1.get_time_end_key_value();
				check_overlap2 = finance_time2_start_value <= finance_time1_end_value;
			}
			return (check_overlap1 && check_overlap2);
		}

		private FinanceTimeRangeBase finance_time_range = null;
		private FinanceRecorderCmnDef.FinanceTimeUnit finance_time_unit = FinanceRecorderCmnDef.FinanceTimeUnit.FinanceTime_Undefined;
		private FinanceRecorderCmnDef.FinanceTimeRangeType finance_time_range_type = FinanceRecorderCmnDef.FinanceTimeRangeType.FinanceTimeRange_Undefined;

		public FinanceTimeRange(FinanceDate in_finance_date_start, FinanceDate in_finance_date_end)
		{
			finance_time_range = new FinanceDateRange(in_finance_date_start, in_finance_date_end);
			finance_time_unit = FinanceRecorderCmnDef.FinanceTimeUnit.FinanceTime_Date;
		}
		public FinanceTimeRange(FinanceMonth in_finance_month_start, FinanceMonth in_finance_month_end)
		{
			finance_time_range = new FinanceMonthRange(in_finance_month_start, in_finance_month_end);
			finance_time_unit = FinanceRecorderCmnDef.FinanceTimeUnit.FinanceTime_Month;
		}
		public FinanceTimeRange(FinanceQuarter in_finance_quarter_start, FinanceQuarter in_finance_quarter_end)
		{
			finance_time_range = new FinanceQuarterRange(in_finance_quarter_start, in_finance_quarter_end);
			finance_time_unit = FinanceRecorderCmnDef.FinanceTimeUnit.FinanceTime_Quarter;
		}
		public FinanceTimeRange(String in_finance_time_start, String in_finance_time_end)
		{
			finance_time_range_type = FinanceTimeRangeBase.get_time_range_type(in_finance_time_start, in_finance_time_end);
			switch (finance_time_range_type)
			{
			case FinanceTimeRange_Between:
			{
				FinanceRecorderCmnDef.FinanceTimeUnit finance_start_time_unit = FinanceRecorderCmnDef.get_time_unit_from_string(in_finance_time_start);
				FinanceRecorderCmnDef.FinanceTimeUnit finance_end_time_unit = FinanceRecorderCmnDef.get_time_unit_from_string(in_finance_time_end);
				if (finance_start_time_unit != finance_end_time_unit)
					throw new IllegalStateException(String.format("Time unit of start[%s] and end[%s] is NOT identical: %d, %d", in_finance_time_start, in_finance_time_end, finance_start_time_unit.value(), finance_end_time_unit.value()));
				finance_time_unit = finance_start_time_unit;
			}
			break;
			case FinanceTimeRange_LessEqual:
			{
				finance_time_unit = FinanceRecorderCmnDef.get_time_unit_from_string(in_finance_time_end);
			}
			break;
			case FinanceTimeRange_GreaterEqual:
			{
				finance_time_unit = FinanceRecorderCmnDef.get_time_unit_from_string(in_finance_time_start);
			}
			break;
			default:
				throw new IllegalArgumentException(String.format("Unsupported time range: %d", finance_time_range_type.value()));
			}
// Check if the start and end time is the same unit

// Initialize the instance
			switch (finance_time_unit)
			{
			case FinanceTime_Date:
			{
				finance_time_range = new FinanceDateRange(in_finance_time_start, in_finance_time_end);
			}
			break;
			case FinanceTime_Month:
			{
				finance_time_range = new FinanceMonthRange(in_finance_time_start, in_finance_time_end);
			}
			break;
			case FinanceTime_Quarter:
			{
				finance_time_range = new FinanceQuarterRange(in_finance_time_start, in_finance_time_end);
			}
			break;
			default:
				throw new IllegalStateException(String.format("Unknow time unit: %d", finance_time_unit.value()));
			}
		}
		public FinanceTimeRange(FinanceTimeRange another)
		{
			this(another.get_time_start_string(), another.get_time_end_string());
		}

		public FinanceRecorderCmnDef.FinanceTimeRangeType get_time_range_type(){return finance_time_range_type;}
		
		public int[] get_time_start_value_list(){return finance_time_range.get_time_start_value_list();}
		public int[] get_time_end_value_list(){return finance_time_range.get_time_end_value_list();}
		public int[] get_time_range_value_list(){return finance_time_range.get_time_range_value_list();}
		public String get_time_start_string()
		{
			if (!finance_time_range_type.is_time_start_exist())
				throw new IllegalStateException("Start Time does NOT exist");
			return finance_time_range.get_time_start_string();
		}
		public String get_time_end_string()
		{
			if (!finance_time_range_type.is_time_end_exist())
				throw new IllegalStateException("End Time does NOT exist");
			return finance_time_range.get_time_end_string();
		}
		public int get_time_start_key_value()
		{
			if (!finance_time_range_type.is_time_start_exist())
				throw new IllegalStateException("Start Timer does NOT exist");
			return finance_time_range.get_time_start_key_value();
		}
		public int get_time_end_key_value()
		{
			if (!finance_time_range_type.is_time_end_exist())
				throw new IllegalStateException("End tIME does NOT exist");
			return finance_time_range.get_time_end_key_value();
		}

		public FinanceRecorderCmnDef.FinanceTimeUnit get_time_unit(){return finance_time_unit;}
		public FinanceTimeRange get_time_object(){return this;}

		public boolean is_time_start_exist(){return finance_time_range_type.is_time_start_exist();}
		public boolean is_time_end_exist(){return finance_time_range_type.is_time_end_exist();}
	};

	public static class SourceTypeTimeRange implements Comparable<SourceTypeTimeRange>
	{
		public static LinkedList<SourceTypeTimeRange> get_whole_source_type_time_range_list()
		{
			LinkedList<SourceTypeTimeRange> source_type_time_range_list = new LinkedList<SourceTypeTimeRange>();
			for(int source_type_index : FinanceRecorderCmnDef.get_all_source_type_index_list())
			{
				source_type_time_range_list.add(new SourceTypeTimeRange(source_type_index));
			}
			return source_type_time_range_list;
		}
		private int source_type_index;
		private FinanceTimeRange finance_time_range = null;

		public SourceTypeTimeRange(int in_source_type_index, FinanceTimeRange in_finance_time_range)
		{
			source_type_index = in_source_type_index;
			finance_time_range = new FinanceTimeRange(in_finance_time_range);
		}

		public SourceTypeTimeRange(int in_source_type_index, FinanceDate finance_date_start, FinanceDate finance_date_end)
		{
			this(in_source_type_index, new FinanceTimeRange(finance_date_start, finance_date_end));
//			this.source_type_index = in_source_type_index;
//			this.finance_time_range = new FinanceTimeRange(finance_date_start, finance_date_end);
		}
		public SourceTypeTimeRange(int in_source_type_index, FinanceMonth finance_month_start, FinanceMonth finance_month_end)
		{
			this(in_source_type_index, new FinanceTimeRange(finance_month_start, finance_month_end));
//			this.source_type_index = in_source_type_index;
//			this.finance_time_range = new FinanceTimeRange(finance_month_start, finance_month_end);
		}
		public SourceTypeTimeRange(int in_source_type_index, FinanceQuarter finance_quarter_start, FinanceQuarter finance_quarter_end)
		{
			this(in_source_type_index, new FinanceTimeRange(finance_quarter_start, finance_quarter_end));
//			this.source_type_index = in_source_type_index;
//			this.finance_time_range = new FinanceTimeRange(finance_quarter_start, finance_quarter_end);
		}
		public SourceTypeTimeRange(int in_source_type_index, String in_finance_time_start, String in_finance_time_end)
		{
			this.source_type_index = in_source_type_index;
			this.finance_time_range = new FinanceTimeRange(in_finance_time_start, in_finance_time_end);
		}

		public SourceTypeTimeRange(SourceTypeTimeRange another)
		{
			this.source_type_index = another.source_type_index;
			this.finance_time_range = new FinanceTimeRange(another.finance_time_range);
		}

		public SourceTypeTimeRange(int in_source_type_index)
		{
//			super(in_source_type_index, new FinanceTimeRange(finance_quarter_start, finance_quarter_end));
			this.source_type_index = in_source_type_index;
			this.finance_time_range = new FinanceTimeRange(FinanceRecorderCmnDef.DEF_START_DATE_STR, FinanceRecorderCmnDef.DEF_END_DATE_STR);
		}

		@Override
		public int compareTo(SourceTypeTimeRange other) 
		{
			return (source_type_index - other.source_type_index);
		}

		public int get_source_type_index(){return source_type_index;}
		public FinanceTimeRange get_time_range(){return finance_time_range;}
		public FinanceRecorderCmnDef.FinanceTimeRangeType get_time_range_type(){return finance_time_range.get_time_range_type();}
		public FinanceTimeUnit get_time_unit(){return finance_time_range.get_time_unit();}
		public String get_time_start_string(){return finance_time_range.get_time_start_string();}
		public String get_time_end_string(){return finance_time_range.get_time_end_string();}
		public int[] get_time_start_value_list(){return finance_time_range.get_time_start_value_list();}
		public int[] get_time_end_value_list(){return finance_time_range.get_time_end_value_list();}
	};

//	public static class TimeCfg
//	{
//		enum TimeType{TIME_MONTH, TIME_DATE};
////		private static final String DELIM = "-";
//		private TimeType time_type;
//		private int year;
//		private int month;
//		private int day;
//		private String time_str;
//
//		public static int get_int_value(int year, int month, int day)
//		{
//			return ((year & 0xFFFF) << 16) | ((month & 0xFF) << 8) | (day & 0xFF);
//		}
//		public static int get_int_value(String time_str)
//		{
//			int[] data = get_date_value(time_str);
//			assert data != null : String.format("Unsupported time format: %s", time_str);
//			return ((data[0] & 0xFFFF) << 16) | ((data[1] & 0xFF) << 8) | (data[2] & 0xFF);
//		}
//		public static int get_int_value(TimeCfg time_cfg)
//		{
//			return get_int_value(time_cfg.get_year(), time_cfg.get_month(), time_cfg.get_day());
//		}
//
//		private static Matcher get_time_value_matcher(String time_str, String search_pattern)
//		{
//	// Time Format: yyyy-mm; Ex: 2015-09
//	// Time Format: yyyy-MM-dd; Ex: 2015-09-04		
//			Pattern pattern = Pattern.compile(search_pattern);
//			Matcher matcher = pattern.matcher(time_str);
//			if (!matcher.find())
//			{
////				FinanceRecorderCmnDef.format_error("Incorrect time format: %s", time_str);
//				return null;
//			}
//			return matcher;
//		}
//
//		public static Matcher get_month_value_matcher(String time_str){return get_time_value_matcher(time_str, "([\\d]{4})-([\\d]{1,2})");}
//		public static Matcher get_date_value_matcher(String time_str){return get_time_value_matcher(time_str, "([\\d]{4})-([\\d]{1,2})-([\\d]{1,2})");}
//
//		public static int[] get_month_value(String time_str)
//		{
//			Matcher month_matcher = get_month_value_matcher(time_str);
//			if (month_matcher == null)
//			{
////				FinanceRecorderCmnDef.format_error("Incorrect time format: %s", time_str);
//				return null;
//			}
//			int year = Integer.valueOf(month_matcher.group(1));
//			int month = Integer.valueOf(month_matcher.group(2));
//
//			return new int[]{year, month};
//		}
//
//		public static int[] get_date_value(String time_str)
//		{
//			Matcher date_matcher = get_date_value_matcher(time_str);
//			if (date_matcher == null)
//			{
////				FinanceRecorderCmnDef.format_error("Incorrect time format: %s", time_str);
//				return null;
//			}
//			int year = Integer.valueOf(date_matcher.group(1));
//			int month = Integer.valueOf(date_matcher.group(2));
//			int day = Integer.valueOf(date_matcher.group(3));
//
//			return new int[]{year, month, day};
//		}
//
//		public TimeCfg(String cur_time_str) // Format: "2015-09" or "2015-09-04"
//		{
//			time_str = String.format("%s", cur_time_str);
//			int[] date_list = get_date_value(cur_time_str);
//			if (date_list != null)
//			{
//// Try to parse the date format
//				year = date_list[0];
//				month = date_list[1];
//				day = date_list[2];
//				time_type = TimeType.TIME_DATE;
//			}
//			else
//			{
//				int[] month_list = get_month_value(cur_time_str);
//				if (month_list == null)
//				{
////					assert false : String.format("Incorrect time format: %s", cur_time_str);
//					throw new IllegalArgumentException(String.format("Incorrect time format: %s", cur_time_str));
//				}
//// Try to parse the month format
//				year = month_list[0];
//				month = month_list[1];
//				time_type = TimeType.TIME_MONTH;
//			}
//		}
//
//		public TimeCfg(int cur_year, int cur_month)
//		{
//			year = cur_year;
//			month = cur_month;
//			day = 0;
//			time_str = String.format("%04d-%02d", year, month);
//			time_type = TimeType.TIME_MONTH;
//		}
//
//		public TimeCfg(int cur_year, int cur_month, int cur_day)
//		{
//			year = cur_year;
//			month = cur_month;
//			day = cur_day;
//			time_str = String.format("%04d-%02d-%02d", year, month, day);
//			time_type = TimeType.TIME_DATE;
//		}
//
//		public int get_year(){return year;}
//		public int get_month(){return month;}
//		public int get_day(){return day;}
//		@Override
//		public String toString(){return time_str;}
//
//		public boolean is_month_type(){return (time_type == TimeType.TIME_MONTH);}
//		@Override
//		public boolean equals(Object object)
//		{
//			TimeCfg another_time_cfg = (TimeCfg)object;
//			if (year != another_time_cfg.get_year())
//				return false;
//			if (month != another_time_cfg.get_month())
//				return false;
//			if (!is_month_type() && day != another_time_cfg.get_day())
//				return false;
//			return true;
//		}
//	};
//
//	public static class TimeRangeCfg
//	{
//		private TimeCfg time_start_cfg = null;
//		private TimeCfg time_end_cfg = null;
//		private String time_range_description = null;
//		private boolean type_is_month = false;
//
//		public static boolean time_in_range(TimeRangeCfg time_range_cfg, TimeCfg time_cfg)
//		{
//			int time_cfg_value = TimeCfg.get_int_value(time_cfg);
//			return (time_cfg_value >= TimeCfg.get_int_value(time_range_cfg.time_start_cfg) && time_cfg_value <= TimeCfg.get_int_value(time_range_cfg.time_end_cfg));
//		}
//
//		public static boolean time_in_range(TimeRangeCfg time_range_cfg, int year, int month, int day)
//		{
//			return time_in_range(time_range_cfg, new TimeCfg(year, month, day));
//		}
//
//		public TimeRangeCfg(String time_start_str, String time_end_str)
//		{
//			if (time_start_str != null)
//				time_start_cfg = new TimeCfg(time_start_str);
//			if (time_end_str != null)
//				time_end_cfg = new TimeCfg(time_end_str);
//			if (time_start_cfg != null && time_end_cfg != null)
//			{
//				if (time_start_cfg.is_month_type() != time_end_cfg.is_month_type())
//				{
//					String errmsg = String.format("The time format is NOT identical, start: %s, end: %s", time_start_cfg.toString(), time_end_cfg.toString());
//					throw new IllegalArgumentException(errmsg);
//				}
//				type_is_month = time_start_cfg.is_month_type();
//			}
//			else if (time_start_cfg != null)
//				type_is_month = time_start_cfg.is_month_type();
//			else if (time_end_cfg != null)
//				type_is_month = time_end_cfg.is_month_type();
//			else
//				throw new IllegalArgumentException("time_start_str and time_end_str should NOT be NULL simultaneously");
//		}
//		public TimeRangeCfg(int year_start, int month_start, int year_end, int month_end)
//		{
//			time_start_cfg = new TimeCfg(year_start, month_start);
//			time_end_cfg = new TimeCfg(year_end, month_end);
//			type_is_month = true;
//		}
//		public TimeRangeCfg(int year_start, int month_start, int day_start, int year_end, int month_end, int day_end)
//		{
//			time_start_cfg = new TimeCfg(year_start, month_start, day_start);
//			time_end_cfg = new TimeCfg(year_end, month_end, day_end);
//			type_is_month = false;
//		}
//
//		public boolean is_single_time()
//		{
//			if (time_start_cfg != null && time_end_cfg != null)
//				return time_start_cfg.equals(time_end_cfg);
//			return false;
//		}
//		public boolean is_month_type(){return type_is_month;}
//
//		@Override
//		public String toString() 
//		{
//			if (time_range_description == null)
//			{
//				if (time_start_cfg != null && time_end_cfg != null)
//					time_range_description = String.format("%s:%s", time_start_cfg.toString(), time_end_cfg.toString());
//				else if (time_start_cfg != null)
//					time_range_description = String.format("%s", time_start_cfg.toString());
//				else if (time_end_cfg != null)
//					time_range_description = String.format("%s", time_end_cfg.toString());
//			}
//			return time_range_description;
//		}
//
//		public final TimeCfg get_start_time()
//		{
////			assert(time_start_cfg != NULL && "time_start_cfg should NOT be NULL");
//			return time_start_cfg;
//		}
//
//		public String get_start_time_str()
//		{
////			assert(time_start_cfg != NULL && "time_start_cfg should NOT be NULL");
//			return (time_start_cfg != null ? time_start_cfg.toString() : null);
//		}
//
//		public final TimeCfg get_end_time()
//		{
////			assert(time_end_cfg != NULL && "time_end_cfg should NOT be NULL");
//			return time_end_cfg;
//		}
//
//		public String get_end_time_str()
//		{
////			assert(time_start_cfg != NULL && "time_start_cfg should NOT be NULL");
//			return (time_end_cfg != null ? time_end_cfg.toString() : null);
//		}
//
//		private void reset_time(TimeCfg new_start_time_cfg, TimeCfg new_end_time_cfg)
//		{
//			if (new_start_time_cfg != null)
//				time_start_cfg = new_start_time_cfg;
//			if (new_end_time_cfg != null)
//				time_end_cfg = new_end_time_cfg;
//			time_range_description = null;
//		}
//
//		public void set_start_time(TimeCfg new_start_time_cfg){reset_time(new_start_time_cfg, null);}
//		public void set_end_time(TimeCfg new_end_time_cfg){reset_time(null, new_end_time_cfg);}
//
//		public static int[] get_start_and_end_month_value_range(TimeRangeCfg time_range_cfg)
//		{	
//			assert time_range_cfg.is_month_type() : "The time_range_cfg should be Month type";
//			assert time_range_cfg.get_start_time() != null : "The start time in time_rangte_cfg should NOT be NULL";
//			int[] month_value_start = TimeCfg.get_month_value(time_range_cfg.get_start_time_str());
//			if (month_value_start == null)
//				return null;
//			assert time_range_cfg.get_end_time() != null : "The end time in time_rangte_cfg should NOT be NULL";
//			int[] month_value_end = TimeCfg.get_month_value(time_range_cfg.get_end_time_str());
//			if (month_value_end == null)
//				return null;
//
//			return new int[]{month_value_start[0], month_value_start[1], month_value_end[0], month_value_end[1]};
//		}
//		public static int[] get_start_and_end_date_value_range(TimeRangeCfg time_range_cfg)
//		{	
//			assert !time_range_cfg.is_month_type() : "The time_range_cfg should be Date type";
//			assert time_range_cfg.get_start_time() != null : "The start time in time_rangte_cfg should NOT be NULL";
//			int[] date_value_start = TimeCfg.get_date_value(time_range_cfg.get_start_time_str());
//			if (date_value_start == null)
//				return null;
//			assert time_range_cfg.get_end_time() != null : "The end time in time_rangte_cfg should NOT be NULL";
//			int[] date_value_end = TimeCfg.get_date_value(time_range_cfg.get_end_time_str());
//			if (date_value_end == null)
//				return null;
//
//			return new int[]{date_value_start[0], date_value_start[1], date_value_start[2], date_value_end[0], date_value_end[1], date_value_end[2]};
//		}
//	};
//
//	public static class SingleTimeRangeCfg extends TimeRangeCfg
//	{
//		public SingleTimeRangeCfg(String time_str) {super(time_str, time_str);} // Single day
//		SingleTimeRangeCfg(int year, int month) {super(year, month, year, month);} // Single month
//		SingleTimeRangeCfg(int year, int month, int day) {super(year, month, day, year, month, day);} // Single day
//	};

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
			array_data.add(data);
			array_size++;
		}

		public final T get_index(int index)
		{
			assert array_data != null : "array_data == NULL";
			if (index < 0)
				index = FinanceRecorderCmnDef.get_index_ex(index, array_size);
			if(index < 0 && index >= array_size)
			{
				String errmsg = String.format("index[%d] is NOT in ranage: [0, %d) or [-%d, 0)", index, array_size, array_size);
				FinanceRecorderCmnDef.error(errmsg);
				throw new IndexOutOfBoundsException(errmsg);
			}
			return array_data.get(index);
		}
	
		public void clear_array_data()
		{
			array_data.clear();
			array_size = 0;
		}
	};

	public static class FinanceIntDataArray extends FinanceDataArrayBase<Integer>{};
	public static class FinanceLongDataArray extends FinanceDataArrayBase<Long>{};
	public static class FinanceFloatDataArray extends FinanceDataArrayBase<Float>{};
	public static class FinanceStringDataArray extends FinanceDataArrayBase<String>{};

	public static class QuerySet
	{
		private HashMap<Integer, LinkedList<Integer>> query_map;
		private LinkedList<Integer> query_source_type_index_list = null;
		private boolean add_done;

		public QuerySet()
		{
			query_map = new HashMap<Integer, LinkedList<Integer>>();
//			for (int i = 0 ; i < FinanceRecorderCmnDef.FINANCE_SOURCE_SIZE ; i++)
//				query_array.add(new LinkedList<Integer>());
		}

		public short add_query(int source_type_index, int field_index)
		{
			if (add_done)
			{
				FinanceRecorderCmnDef.error("Fail to add another data");
				return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
			}

// Check if the index is out of range
			if(!FinanceRecorderCmnDef.check_source_type_index_in_range(source_type_index))
			{
				FinanceRecorderCmnDef.format_error("source_type_index[%d] is out of range in QuerySet", source_type_index);
				return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
			}

			if(field_index < 0 || field_index >= FinanceRecorderCmnDef.FINANCE_DATABASE_FIELD_AMOUNT_LIST[source_type_index])
			{
// If field_index == -1, it means select all field in the table
				if (field_index != -1)
				{
					FinanceRecorderCmnDef.error("field_index is out of range in QuerySet");
					return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
				}
			}
			if (!query_map.containsKey(source_type_index))
				query_map.put(source_type_index, new LinkedList<Integer>());
			else
			{
// Check the index is duplicate
				if (query_map.get(source_type_index).indexOf(field_index) != -1)
				{
					FinanceRecorderCmnDef.format_warn("Duplicate index: %d in %s", field_index, FinanceRecorderCmnDef.FINANCE_DATA_DESCRIPTION_LIST[source_type_index]);
					return FinanceRecorderCmnDef.RET_WARN_INDEX_DUPLICATE;
				}
// If all fields are selected, it's no need to add extra index
				if (!query_map.get(source_type_index).isEmpty() && query_map.get(source_type_index).get(0) == -1)
				{
					FinanceRecorderCmnDef.format_warn("Ignore index: %d in %s", field_index, FinanceRecorderCmnDef.FINANCE_DATA_DESCRIPTION_LIST[source_type_index]);
					return FinanceRecorderCmnDef.RET_WARN_INDEX_IGNORE;
				}
// Clear the old index if all data are selected
				if (field_index == -1)
					query_map.get(source_type_index).clear();
			}
// Add the index
			query_map.get(source_type_index).add(field_index);
			return FinanceRecorderCmnDef.RET_SUCCESS;
		}
		public short add_query(int source_type_index){return add_query(source_type_index, -1);}

		public short add_query_done()
		{
			if (add_done)
			{
				FinanceRecorderCmnDef.error("Fail to add another data");
				return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
			}
			for(Map.Entry<Integer, LinkedList<Integer>> entry : query_map.entrySet())
//			for (int i = 0 ; i < FinanceRecorderCmnDef.FINANCE_SOURCE_SIZE ; i++)
			{
				int source_type_index = entry.getKey();
				if (!FinanceRecorderCmnDef.check_source_type_index_in_range(source_type_index))
					throw new IllegalStateException(String.format("Unsupported source type index: %d", source_type_index));
				LinkedList<Integer> field_list = entry.getValue();
				if (field_list.isEmpty())
					continue;
//				WRITE_FORMAT_DEBUG("Transform the query data[source_type_index: %d]", i);
				if (field_list.get(0) == -1)
				{
					field_list.clear();
					for (int field_index = 1 ; field_index < FinanceRecorderCmnDef.FINANCE_DATABASE_FIELD_AMOUNT_LIST[source_type_index] ; field_index++) // Caution: Don't include the "date" field
						field_list.add(field_index);
				}
			}
			add_done = true;
			return FinanceRecorderCmnDef.RET_SUCCESS;
		}

		public boolean is_add_query_done() {return add_done;}

		public final LinkedList<Integer> get_source_type_index_list()
		{
			if (query_source_type_index_list == null)
			{
				query_source_type_index_list = new LinkedList<Integer>();
				for(Map.Entry<Integer, LinkedList<Integer>> entry : query_map.entrySet())
				{
					int source_type_index = entry.getKey();
					if (FinanceRecorderCmnDef.check_source_type_index_in_range(source_type_index))
						throw new IllegalStateException(String.format("Unsupported source type index: %d", source_type_index));
					query_source_type_index_list.add(source_type_index);
				}
			}
			return query_source_type_index_list;
		}

		public final LinkedList<Integer> get_field_index_list(int source_type_index)
		{
			int[] index_array = FinanceRecorderCmnDef.get_source_type_index_range();
			if (source_type_index < index_array[0] || source_type_index >= index_array[1])
			{
				String errmsg = String.format("The index[%d] is out of range(0, %d)", source_type_index , index_array[1] - 1);
				FinanceRecorderCmnDef.error(errmsg);
				throw new IndexOutOfBoundsException(errmsg);
			}
			return (query_map.containsKey(source_type_index) ? query_map.get(source_type_index) : null);
		}
	};

	static public short add_query(QuerySet query_set, FinanceRecorderCmnDef.FinanceSourceType source_type, int field_index){return query_set.add_query(source_type.value(), field_index);}
	static public short add_query_ex(QuerySet query_set, FinanceRecorderCmnDef.FinanceSourceType source_type, int field_index, HashSet<Integer> source_type_index_set)
	{
		source_type_index_set.add(source_type.value());
		return add_query(query_set, source_type, field_index);
	}

//	public static class CompanyGroupSet implements Iterable<Map.Entry<Integer, ArrayList<String>>>
//	{
//		private static TreeMap<Integer, ArrayList<String>> whole_company_number_in_group_map;
//		private TreeMap<Integer, ArrayList<String>> company_number_in_group_map = null;
//		private TreeMap<Integer, ArrayList<String>> altered_company_number_in_group_map = null;
//		private boolean is_add_done = false;
//
//		static void init_whole_company_number_in_group_map()
//		{
//			assert whole_company_number_in_group_map == null : "whole_company_number_in_group_map is NOT null";
//
//			FinanceRecorderCmnClassCompanyProfile company_profile = FinanceRecorderCmnClassCompanyProfile.get_instance();
//			whole_company_number_in_group_map = new TreeMap<Integer, ArrayList<String>>();
//			int company_group_size = company_profile.get_company_group_size();
//			for (int i = 0 ; i < company_group_size ; i++)
//			{
//				FinanceRecorderCmnClassCompanyProfile.TraverseEntry traverse_entry = company_profile.group_entry(i);
//				ArrayList<String> company_number_array = new ArrayList<String>();
//				for (ArrayList<String> entry : traverse_entry)
//					company_number_array.add(entry.get(FinanceRecorderCmnClassCompanyProfile.COMPANY_PROFILE_ENTRY_FIELD_INDEX_COMPANY_CODE_NUMBER));
//				whole_company_number_in_group_map.put(i, company_number_array);
//			}
//		}
//
//		static public CompanyGroupSet get_whole_company_group_set()
//		{
//			CompanyGroupSet company_group_set = new CompanyGroupSet();
//			company_group_set.add_done();
//			return company_group_set;
//		}
//
//		public CompanyGroupSet(){}
//
//		@Override
//		public Iterator<Map.Entry<Integer, ArrayList<String>>> iterator()
//		{
//			if (!is_add_done)
//			{
//				String errmsg = "The add_done flag is NOT set to True";
//				FinanceRecorderCmnDef.format_error(errmsg);
//				throw new IllegalStateException(errmsg);
//			}
//			Iterator<Map.Entry<Integer, ArrayList<String>>> it = new Iterator<Map.Entry<Integer, ArrayList<String>>>()
//			{
//				private Iterator<Map.Entry<Integer, ArrayList<String>>> iter = altered_company_number_in_group_map.entrySet().iterator();
//				@Override
//				public boolean hasNext()
//				{
//					return iter.hasNext();
//				}
//				@Override
//				public Map.Entry<Integer, ArrayList<String>> next()
//				{
//					return (Map.Entry<Integer, ArrayList<String>>)iter.next();
//				}
//				@Override
//				public void remove() {throw new UnsupportedOperationException();}
//			};
//			return it;
//		}
//
//		public short add_company_list(int company_group_number, ArrayList<String> company_code_number_in_group_array)
//		{
//			if (is_add_done)
//			{
//				FinanceRecorderCmnDef.format_error("The add_done flag has already been set to True");
//				return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
//			}
//			if (company_number_in_group_map == null)
//				company_number_in_group_map = new TreeMap<Integer, ArrayList<String>>();
//			if (!company_number_in_group_map.containsKey(company_group_number))
//			{
//				ArrayList<String> company_number_deque = new ArrayList<String>();
//				company_number_in_group_map.put(company_group_number, company_number_deque);
//			}
//			else
//			{
//				if (company_number_in_group_map.get(company_group_number) == null)
//				{
//					FinanceRecorderCmnDef.format_error("The company group[%d] has already been set to NULL", company_group_number);
//					return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
//				}
//			}
//			for (String company_code_number : company_code_number_in_group_array)
//			{
//				if (company_number_in_group_map.get(company_group_number).indexOf(company_code_number) != -1)
//				{
//					FinanceRecorderCmnDef.format_warn("The company code number[%s] has already been added to the group[%d]", company_code_number, company_group_number);
//					continue;
//				}
//				company_number_in_group_map.get(company_group_number).add(company_code_number);
//			}
//			return FinanceRecorderCmnDef.RET_SUCCESS;
//		}
//
//		public short add_company(int company_group_number, String company_code_number)
//		{
//			ArrayList<String> company_code_number_in_group_array = new ArrayList<String>();
//			company_code_number_in_group_array.add(company_code_number);
//			return add_company_list(company_group_number, company_code_number_in_group_array);
//		}
//
//		public short add_company_group(int company_group_number)
//		{
//			if (is_add_done)
//			{
//				FinanceRecorderCmnDef.format_error("The add_done flag has already been set to True");
//				return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
//			}
//
//			if (company_number_in_group_map == null)
//				company_number_in_group_map = new TreeMap<Integer, ArrayList<String>>();
//
//			if (company_number_in_group_map.containsKey(company_group_number))
//			{
//				if (company_number_in_group_map.get(company_group_number) != null)
//					FinanceRecorderCmnDef.format_warn("Select all company group[%d], ignore the original settings......", company_group_number);
//			}
//			company_number_in_group_map.put(company_group_number, null);
//			return FinanceRecorderCmnDef.RET_SUCCESS;
//		}
//
//		public short add_done()
//		{
//			if (is_add_done)
//			{
//				FinanceRecorderCmnDef.format_error("The add_done flag has already been set to True");
//				return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
//			}
//			setup_for_traverse();
//			is_add_done = true;
//			return FinanceRecorderCmnDef.RET_SUCCESS;
//		}
//
//		final ArrayList<String> get_company_number_in_group_list(int company_group_index)
//		{
//			if (!is_add_done)
//			{
//				String errmsg = "The add_done flag is NOT set to True";
//				FinanceRecorderCmnDef.format_error(errmsg);
//				throw new IllegalStateException(errmsg);
//			}
//			if (!altered_company_number_in_group_map.containsKey(company_group_index))
//			{
//				String errmsg = String.format("The company group index[%d] is NOT found in data structure", company_group_index);
//				FinanceRecorderCmnDef.format_error(errmsg);
//				throw new IllegalArgumentException(errmsg);
//			}
//			return altered_company_number_in_group_map.get(company_group_index);
//		}
//
//		private void setup_for_traverse()
//		{
//			if (whole_company_number_in_group_map == null)
//				init_whole_company_number_in_group_map();
//			if (company_number_in_group_map == null)
//				altered_company_number_in_group_map = whole_company_number_in_group_map;
//			else
//			{
//				altered_company_number_in_group_map = new TreeMap<Integer, ArrayList<String>>();
//				for (Map.Entry<Integer, ArrayList<String>> entry : company_number_in_group_map.entrySet())
//					altered_company_number_in_group_map.put(entry.getKey(), (entry.getValue() != null) ? entry.getValue() : whole_company_number_in_group_map.get(entry.getKey()));
//			}
//		}
//	};

//	public class StockQuerySet extends QuerySet
//	{
//		protected CompanyGroupSet company_group_set;
//
//		public StockQuerySet()
//		{
//			company_group_set = new CompanyGroupSet();
//		}
//
//		public short add_company_list(int company_group_number, final ArrayList<String> company_code_number_in_group_array)
//		{
//			return company_group_set.add_company_list(company_group_number, company_code_number_in_group_array);
//		}
//
//		public short add_company(int company_group_number, String company_code_number)
//		{
//			return company_group_set.add_company(company_group_number, company_code_number);
//		}
//
//		public short add_company_group(int company_group_number)
//		{
//			return company_group_set.add_company_group(company_group_number);
//		}
//
//		public final CompanyGroupSet get_company_group_set()
//		{
//			return company_group_set;
//		}
//
////		public CompanyGroupSet get_company_group_set()
////		{
////			return company_group_set;
////		}
//	};

	public static class ResultSet
	{
		private static short get_combined_index(int x, int y) {return (short)(((x & 0xFF) << 8) | (y & 0xFF));}
		private static short get_upper_subindex(short x) {return (short)((x >> 8) & 0xFF);}
		private static short get_lower_subindex(short x) {return (short)(x & 0xFF);}

		private HashMap<Integer, Integer> data_set_mapping;
		private LinkedList<Integer> source_type_index_list = null;
		private FinanceStringDataArray date_data;
		private ArrayList<FinanceIntDataArray> int_data_set;
		private ArrayList<FinanceLongDataArray> long_data_set;
		private ArrayList<FinanceFloatDataArray> float_data_set;
		private boolean check_date_data_mode;
		private int date_data_size;
		private int date_data_cur_pos;
		private int int_data_set_size;
		private int long_data_set_size;
		private int float_data_set_size;

		public ResultSet()
		{
			data_set_mapping = new HashMap<Integer, Integer>();
			source_type_index_list = new LinkedList<Integer>();
			date_data = new FinanceStringDataArray();
			int_data_set = new ArrayList<FinanceIntDataArray>();
			long_data_set = new ArrayList<FinanceLongDataArray>();
			float_data_set = new ArrayList<FinanceFloatDataArray>();
			check_date_data_mode = false;
			date_data_size = 0;
			date_data_cur_pos = 0;
			int_data_set_size = 0;
			long_data_set_size = 0;
			float_data_set_size = 0;
		}

		public void clear_array_data()
		{
			date_data.clear_array_data();
			for(FinanceIntDataArray int_data : int_data_set)
				int_data.clear_array_data();
			for(FinanceLongDataArray long_data : long_data_set)
				long_data.clear_array_data();
			for(FinanceFloatDataArray float_data : float_data_set)
				float_data.clear_array_data();
			check_date_data_mode = false;
			date_data_size = 0;
			date_data_cur_pos = 0;
		}

//		public void reset_result()
//		{
//			date_data = new FinanceStringDataArray();
//			int_data_set = new ArrayList<FinanceIntDataArray>();
//			long_data_set = new ArrayList<FinanceLongDataArray>();
//			float_data_set = new ArrayList<FinanceFloatDataArray>();
//			check_date_data_mode = false;
//			date_data_size = 0;
//			date_data_cur_pos = 0;
//			int_data_set_size = 0;
//			long_data_set_size = 0;
//			float_data_set_size = 0;
//		}

		public short add_set(int source_type_index, int field_index)
		{
			if(!FinanceRecorderCmnDef.check_source_type_index_in_range(source_type_index))
			{
				FinanceRecorderCmnDef.error("source_type_index is out of range in ResultSet");
				return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
			}
			if(field_index < 0 || field_index >= FinanceRecorderCmnDef.FINANCE_DATABASE_FIELD_AMOUNT_LIST[source_type_index])
			{
// If field_index == -1, it means select all field in the table
//				if (field_index != -1)
//				{
					FinanceRecorderCmnDef.error("field_index is out of range in ResultSet");
					return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
//				}
			}

			ArrayList<Integer> field_array = new ArrayList<Integer>();
//			if (field_index != -1)
				field_array.add(field_index);
//			else
//			{
//				for(int i = 1 ; i < FinanceRecorderCmnDef.FINANCE_DATABASE_FIELD_AMOUNT_LIST[source_type_index] ; i++)
//					field_array.add(i);
//			}
			for(Integer index : field_array)
			{
// Check if the source_type:field_type has been set
				short key = get_combined_index(source_type_index, index);
				if (data_set_mapping.get(key) != null)
				{
					FinanceRecorderCmnDef.format_error("The key[%d] from (%d, %d) is duplicate", key, source_type_index, index);
					return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
				}
// Check the lookup table to find the date type of the specific field
				short value;
				switch(FinanceRecorderCmnDef.FINANCE_DATABASE_FIELD_TYPE_LIST[source_type_index][field_index])
				{
				case FinanceField_INT:
					value = get_combined_index(FinanceRecorderCmnDef.FinanceFieldType.FinanceField_INT.value(), int_data_set.size());
					int_data_set.add(new FinanceIntDataArray());
					int_data_set_size = int_data_set.size();
					break;
				case FinanceField_LONG:
					value = get_combined_index(FinanceRecorderCmnDef.FinanceFieldType.FinanceField_LONG.value(), long_data_set.size());
					long_data_set.add(new FinanceLongDataArray());
					long_data_set_size = long_data_set.size();
					break;
				case FinanceField_FLOAT:
					value = get_combined_index(FinanceRecorderCmnDef.FinanceFieldType.FinanceField_FLOAT.value(), float_data_set.size());
					float_data_set.add(new FinanceFloatDataArray());
					float_data_set_size = float_data_set.size();
					break;
				case FinanceField_DATE:
					FinanceRecorderCmnDef.error("The DATE field type is NOT supported");
					return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
				default:
					FinanceRecorderCmnDef.format_error("The unsupported field type: %d", FinanceRecorderCmnDef.FINANCE_DATABASE_FIELD_TYPE_LIST[source_type_index][field_index]);
					return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
				}
				FinanceRecorderCmnDef.format_debug("ResultSet Map: %d, %d", key, value);
				data_set_mapping.put(Integer.valueOf(key), Integer.valueOf(value));
			}
// Keep track of the source type index
			if (source_type_index_list.indexOf(source_type_index) == -1)
				source_type_index_list.add(source_type_index);
			return FinanceRecorderCmnDef.RET_SUCCESS;
		}

		public short add_set(int source_type_index, final LinkedList<Integer> field_list)
		{
			short ret = FinanceRecorderCmnDef.RET_SUCCESS;
			ListIterator<Integer> iter = field_list.listIterator(0);
			while (iter.hasNext())
			{
				Integer field_index = iter.next();
				ret = add_set(source_type_index, field_index);
				if (FinanceRecorderCmnDef.CheckFailure(ret))
					return ret;
			}
			return ret;
		}

		public short set_date(String element_value)
		{
			if (check_date_data_mode)
			{
				if (!date_data.get_index(date_data_cur_pos).equals(element_value))
				{
					FinanceRecorderCmnDef.format_error("The date(%s, %s) is NOT equal", date_data.get_index(date_data_cur_pos), element_value);
					return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
				}
				date_data_cur_pos++;
			}
			else
			{
				date_data.add(element_value);
				date_data_size++;
			}
			return FinanceRecorderCmnDef.RET_SUCCESS;
		}

		public int find_date_index(String search_date, int search_start_index)
		{
			int index = -1;
			for (int i = search_start_index ; i < date_data_size ; i++)
			{
				if (date_data.get_index(i).equals(search_date))
					return index;
			}
			return -1;
		}

//		public int find_first_after_date_index(String search_date, int search_start_index)
//		{
//			int search_date_value = TimeCfg.get_int_value(search_date);
//			for (int index = search_start_index ; index < date_data_size ; index++)
//			{
//				int date_value = TimeCfg.get_int_value(date_data.get_index(index));
//				if (date_value >= search_date_value)
//					return index;
//			}
//			return date_data_size;
//		}

		public final FinanceStringDataArray get_date_array(){return date_data;}
		public final String get_date_array_element(int index)
		{
//			if (index < 0 || index >= date_data_size)
//				throw new IndexOutOfBoundsException(String.format("The Index [%d] is out of range [0, %d)", index, date_data_size));
			return date_data.get_index(index);
		}
		public String[] get_date_array_elements(int source_type_index, int field_index, int start_index, int end_index) 
		{
			if (start_index < 0)
				start_index = FinanceRecorderCmnDef.get_start_index_ex(start_index, date_data_size);
			if (end_index < 0)
				end_index = FinanceRecorderCmnDef.get_end_index_ex(end_index, date_data_size);
			if (!FinanceRecorderCmnDef.check_start_index_in_range(start_index, 0, date_data_size))
				throw new IndexOutOfBoundsException(String.format("The Start Index [%d] is NOT in range [0, %d)", start_index, date_data_size));
			if (!FinanceRecorderCmnDef.check_end_index_in_range(end_index, 1, date_data_size))
				throw new IndexOutOfBoundsException(String.format("The End Index [%d] is NOT in range (1, %d]", end_index, date_data_size));
			int data_len = end_index - start_index;
			assert data_len > 0 : String.format("End Index[%d] SHOULD be larger than Start Index[%d]", end_index, start_index);
			String data_array[] = new String[data_len];

			int data_pos = 0;
			for (int index = start_index ; index < end_index ; index++)
				data_array[data_pos++] = date_data.get_index(index);
			return data_array;
		}

		private short find_data_pos(int source_type_index, int field_index, short[] field_type_array)
		{
			short key = get_combined_index(source_type_index, field_index);
			if (data_set_mapping.get(Integer.valueOf(key)) == null)
			{
				FinanceRecorderCmnDef.format_error("The key[%d] from (%d, %d) is NOT FOUND", key, source_type_index, field_index);
				return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
			}
			short value = data_set_mapping.get(Integer.valueOf(key)).shortValue();
			field_type_array[0] = get_upper_subindex(value);
			field_type_array[1] = get_lower_subindex(value);
			return FinanceRecorderCmnDef.RET_SUCCESS;
		}

		private void find_and_check_data_pos(int source_type_index, int field_index, short[] field_type_array)
		{
			if (FinanceRecorderCmnDef.CheckFailure(find_data_pos(source_type_index, field_index, field_type_array)))
				throw new IllegalArgumentException(String.format("The key[%d] from (%d, %d) is NOT FOUND", source_type_index, field_index));
		}

		public short set_data(int source_type_index, int field_index, Object data_obj)
		{
			short[] field_type_array = new short[2];
			short ret = find_data_pos(source_type_index, field_index, field_type_array);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				return ret;

			short field_type_index = field_type_array[0];
			short field_type_pos = field_type_array[1];
			switch(FinanceRecorderCmnDef.FinanceFieldType.valueOf(field_type_index))
			{
			case FinanceField_INT:
				int_data_set.get(field_type_pos).add((Integer)data_obj);
				break;
			case FinanceField_LONG:
				long_data_set.get(field_type_pos).add((Long)data_obj);
				break;
			case FinanceField_FLOAT:
				float_data_set.get(field_type_pos).add((Float)data_obj);
				break;
			default:
				FinanceRecorderCmnDef.format_error("Unsupported field_type_index: %d", field_type_index);
				return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
			}
			return FinanceRecorderCmnDef.RET_SUCCESS;
		}

		public void switch_to_check_date_mode()
		{
			check_date_data_mode = true;
			date_data_cur_pos = 0;
		}

		public short check_data()
		{
			int date_data_size = date_data.get_size();
			short key;
			short value;
			short field_type_index;
			short field_type_pos;
			int data_size;
			for(Map.Entry<Integer, Integer> entry : data_set_mapping.entrySet())
			{
				value = entry.getValue().shortValue();
				field_type_index = get_upper_subindex(value);
				field_type_pos = get_lower_subindex(value);
				switch(FinanceRecorderCmnDef.FinanceFieldType.valueOf(field_type_index))
				{
				case FinanceField_INT:
					data_size = int_data_set.get(field_type_pos).get_size();
					break;
				case FinanceField_LONG:
					data_size = long_data_set.get(field_type_pos).get_size();
					break;
				case FinanceField_FLOAT:
					data_size = float_data_set.get(field_type_pos).get_size();
					break;
				default:
					FinanceRecorderCmnDef.format_error("Unsupported field_type_index: %d", field_type_index);
					return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
				}
// Check all the data size are equal
				if (date_data_size != data_size)
				{
					key = entry.getKey().shortValue();
					short source_type_index = get_upper_subindex(key);
					short field_index = get_lower_subindex(key);
					FinanceRecorderCmnDef.format_error("Incorrect data size in %d, %d, expected: %d, actual: %d", source_type_index, field_index, date_data_size, data_size);
					return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
				}
			}
			return FinanceRecorderCmnDef.RET_SUCCESS;
		}

		public final LinkedList<Integer> get_source_type_index_list()
		{
			return source_type_index_list;
		}

		public short show_data()
		{
			if (!FinanceRecorderCmnDef.is_show_console())
				return FinanceRecorderCmnDef.RET_SUCCESS;

			final int STAR_LEN = 120;
			short key;
			short value;
			short source_type_index;
			short field_index;
			short field_type_index;
			short field_type_pos;

		// Show the database:field info
			String star_str = "";
			for(int i = 0 ; i < STAR_LEN ; i++)
				star_str += "*";
			System.out.printf("%s\n| %s |", star_str, FinanceRecorderCmnDef.MYSQL_DATE_FILED_NAME);

			for(Map.Entry<Integer, Integer> entry : data_set_mapping.entrySet())
			{
				key = entry.getKey().shortValue();
				source_type_index = get_upper_subindex(key);
				field_index = get_lower_subindex(key);
				System.out.printf(" %s:%s%d |", FinanceRecorderCmnDef.FINANCE_DATA_DESCRIPTION_LIST[source_type_index], FinanceRecorderCmnDef.MYSQL_FILED_NAME_BASE, field_index);
			}

			System.out.printf("\n");
			star_str = "";
			for(int i = 0 ; i < STAR_LEN ; i++)
				star_str += "*";
			System.out.printf("%s\n\n", star_str);

// Show the data info
			int date_data_size = date_data.get_size();
			for(int i = 0 ; i < date_data_size ; i++)
			{
				System.out.printf("| %s |", date_data.get_index(i));
				for(Map.Entry<Integer, Integer> entry : data_set_mapping.entrySet())
				{
					value = entry.getValue().shortValue();
					field_type_index = get_upper_subindex(value);
					field_type_pos = get_lower_subindex(value);
					switch(FinanceRecorderCmnDef.FinanceFieldType.valueOf(field_type_index))
					{
					case FinanceField_INT:
						System.out.printf(" %10d |", int_data_set.get(field_type_pos).get_index(i));
						break;
					case FinanceField_LONG:
						System.out.printf(" %12ld |", long_data_set.get(field_type_pos).get_index(i));
						break;
					case FinanceField_FLOAT:
						System.out.printf(" %8.1f |", float_data_set.get(field_type_pos).get_index(i));
						break;
					default:
						FinanceRecorderCmnDef.format_error("Unsupported field_type_index: %d", field_type_index);
						return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
					}
				}
				System.out.printf("\n");
			}
			return FinanceRecorderCmnDef.RET_SUCCESS;
		}

		void check_data_pos_boundary(short field_type_index, short field_type_pos, int field_type_index_value, int field_type_pos_size)
		{
			if (field_type_index != field_type_index_value)
			{
				String errmsg = String.format("The field type[%d] is NOT int", field_type_index);
				FinanceRecorderCmnDef.error(errmsg);
				throw new IllegalArgumentException(errmsg);
			}
			if (field_type_pos >= field_type_pos_size || field_type_pos < 0)
			{
				String errmsg = String.format("The field pos[%d] is out of range", field_type_pos);
				FinanceRecorderCmnDef.error(errmsg);
				throw new IndexOutOfBoundsException(errmsg);
			}
		}

		public final FinanceIntDataArray get_int_array(int source_type_index, int field_index)
		{
			short[] field_type_array = new short[2];
			find_and_check_data_pos(source_type_index, field_index, field_type_array);
			check_data_pos_boundary(field_type_array[0], field_type_array[1], FinanceRecorderCmnDef.FinanceFieldType.FinanceField_INT.value(), int_data_set_size);
			assert field_type_array[0] == FinanceRecorderCmnDef.FinanceFieldType.FinanceField_INT.value() : String.format("The type[%d] is NOT INT", FinanceRecorderCmnDef.FinanceFieldType.FinanceField_FLOAT.value());
			return int_data_set.get(field_type_array[1]);
		}
		public final FinanceLongDataArray get_long_array(int source_type_index, int field_index)
		{
			short[] field_type_array = new short[2];
			find_and_check_data_pos(source_type_index, field_index, field_type_array);
			check_data_pos_boundary(field_type_array[0], field_type_array[1], FinanceRecorderCmnDef.FinanceFieldType.FinanceField_LONG.value(), long_data_set_size);
			assert field_type_array[0] == FinanceRecorderCmnDef.FinanceFieldType.FinanceField_LONG.value() : String.format("The type[%d] is NOT LONG", FinanceRecorderCmnDef.FinanceFieldType.FinanceField_FLOAT.value());
			return long_data_set.get(field_type_array[1]);
		}
		public final FinanceFloatDataArray get_float_array(int source_type_index, int field_index)
		{
			short[] field_type_array = new short[2];
			find_and_check_data_pos(source_type_index, field_index, field_type_array);
			check_data_pos_boundary(field_type_array[0], field_type_array[1], FinanceRecorderCmnDef.FinanceFieldType.FinanceField_FLOAT.value(), float_data_set_size);
			assert field_type_array[0] == FinanceRecorderCmnDef.FinanceFieldType.FinanceField_FLOAT.value() : String.format("The type[%d] is NOT FLOAT", FinanceRecorderCmnDef.FinanceFieldType.FinanceField_FLOAT.value());
			return float_data_set.get(field_type_array[1]);
		}
		public int get_int_array_element(int source_type_index, int field_index, int index) 
		{
			if (index < 0 || index >= date_data_size)
				throw new IndexOutOfBoundsException(String.format("The Index [%d] is out of range [0, %d)", index, date_data_size));
			return get_int_array(source_type_index, field_index).get_index(index);
		}
		public long get_long_array_element(int source_type_index, int field_index, int index) 
		{
			if (index < 0 || index >= date_data_size)
				throw new IndexOutOfBoundsException(String.format("The Index [%d] is out of range [0, %d)", index, date_data_size));
			return get_long_array(source_type_index, field_index).get_index(index);
		}
		public float get_float_array_element(int source_type_index, int field_index, int index) {return get_float_array(source_type_index, field_index).get_index(index);}

		public int[] get_int_array_elements(int source_type_index, int field_index, int start_index, int end_index) 
		{
			if (start_index < 0 || start_index >= date_data_size)
				throw new IndexOutOfBoundsException(String.format("The Start Index [%d] is out of range [0, %d)", start_index, date_data_size));
			if (end_index < 1 || end_index > date_data_size)
				throw new IndexOutOfBoundsException(String.format("The End Index [%d] is out of range (1, %d]", start_index, date_data_size));
			int data_len = end_index - start_index;
			assert data_len > 0 : String.format("End Index[%d] SHOULD be larger than Start Index[%d]", end_index, start_index);
			int data_array[] = new int[data_len];
			FinanceIntDataArray int_data_array = get_int_array(source_type_index, field_index);
			int data_pos = 0;
			for (int index = start_index ; index < end_index ; index++)
				data_array[data_pos++] = int_data_array.get_index(index);
			return data_array;
		}
		public long[] get_long_array_elements(int source_type_index, int field_index, int start_index, int end_index) 
		{
			if (start_index < 0 || start_index >= date_data_size)
				throw new IndexOutOfBoundsException(String.format("The Start Index [%d] is out of range [0, %d)", start_index, date_data_size));
			if (end_index < 1 || end_index > date_data_size)
				throw new IndexOutOfBoundsException(String.format("The End Index [%d] is out of range (1, %d]", start_index, date_data_size));
			int data_len = end_index - start_index;
			assert data_len > 0 : String.format("End Index[%d] SHOULD be larger than Start Index[%d]", end_index, start_index);
			long data_array[] = new long[data_len];
			FinanceLongDataArray long_data_array = get_long_array(source_type_index, field_index);
			int data_pos = 0;
			for (int index = start_index ; index < end_index ; index++)
				data_array[data_pos++] = long_data_array.get_index(index);
			return data_array;
		}
		public float[] get_float_array_elements(int source_type_index, int field_index, int start_index, int end_index) 
		{
			if (start_index < 0 || start_index >= date_data_size)
				throw new IndexOutOfBoundsException(String.format("The Start Index [%d] is out of range [0, %d)", start_index, date_data_size));
			if (end_index < 1 || end_index > date_data_size)
				throw new IndexOutOfBoundsException(String.format("The End Index [%d] is out of range (1, %d]", start_index, date_data_size));
			int data_len = end_index - start_index;
			assert data_len > 0 : String.format("End Index[%d] SHOULD be larger than Start Index[%d]", end_index, start_index);
			float data_array[] = new float[data_len];
			FinanceFloatDataArray float_data_array = get_float_array(source_type_index, field_index);
			int data_pos = 0;
			for (int index = start_index ; index < end_index ; index++)
				data_array[data_pos++] = float_data_array.get_index(index);
			return data_array;
		}

		public ArrayList<String> to_string_array(int source_type_index, int start_index, int end_index)
		{
			if (start_index < 0 || start_index >= date_data_size)
				throw new IndexOutOfBoundsException(String.format("The Start Index [%d] is out of range [0, %d)", start_index, date_data_size));
			if (end_index < 1 || end_index > date_data_size)
				throw new IndexOutOfBoundsException(String.format("The End Index [%d] is out of range (1, %d]", start_index, date_data_size));
			int data_len = end_index - start_index;
			assert data_len > 0 : String.format("End Index[%d] SHOULD be larger than Start Index[%d]", end_index, start_index);
// Keep track of the data string into array
			String data_string_array[] = new String[data_len];
			int data_pos = 0;
			for (int index = start_index ; index < end_index ; index++)
				data_string_array[data_pos++] = date_data.get_index(index);
			for (int field_index = 1 ; field_index < FinanceRecorderCmnDef.FINANCE_DATABASE_FIELD_AMOUNT_LIST[source_type_index] ; field_index++)
			{
				switch(FinanceRecorderCmnDef.FINANCE_DATABASE_FIELD_TYPE_LIST[source_type_index][field_index])
				{
				case FinanceField_INT:
				{
					int[] data_array = get_int_array_elements(source_type_index, field_index, start_index, end_index);
					data_pos = 0;
					for (int index = start_index ; index < end_index ; index++, data_pos++)
						data_string_array[data_pos] += String.format(",%d", data_array[data_pos]);
				}
				break;
				case FinanceField_LONG:
				{
					long[] data_array = get_long_array_elements(source_type_index, field_index, start_index, end_index);
					data_pos = 0;
					for (int index = start_index ; index < end_index ; index++, data_pos++)
						data_string_array[data_pos] += String.format(",%d", data_array[data_pos]);
				}
				break;
				case FinanceField_FLOAT:
				{
					float[] data_array = get_float_array_elements(source_type_index, field_index, start_index, end_index);
					data_pos = 0;
					for (int index = start_index ; index < end_index ; index++, data_pos++)
						data_string_array[data_pos] += String.format(",%.2f", data_array[data_pos]);
				}
				break;
				default:
				{
					String errmsg = String.format("The unsupported field type: %d", FinanceRecorderCmnDef.FINANCE_DATABASE_FIELD_TYPE_LIST[source_type_index][field_index]);
					FinanceRecorderCmnDef.format_error(errmsg);
					throw new IllegalArgumentException(errmsg);
				}
				}
			}

			ArrayList<String> string_array = new ArrayList<String>();
			for (String data_str : data_string_array)
				string_array.add(data_str);
			return string_array;
		}
		public ArrayList<String> to_string_array(int source_type_index)
		{
			return to_string_array(source_type_index, 0, date_data_size);
		}

//		public String[] get_array_all_elements_string_list(int source_type_index, int start_index, int end_index)
//		{
//			if (start_index < 0 || start_index >= date_data_size)
//				throw new IndexOutOfBoundsException(String.format("The Start Index [%d] is out of range [0, %d)", start_index, date_data_size));
//			if (end_index < 1 || end_index > date_data_size)
//				throw new IndexOutOfBoundsException(String.format("The End Index [%d] is out of range (1, %d]", start_index, date_data_size));
//			int data_len = end_index - start_index;
//			assert data_len > 0 : String.format("End Index[%d] SHOULD be larger than Start Index[%d]", end_index, start_index);
//			String data_string_array[] = new String[data_len];
//			int data_pos = 0;
//			for (int index = start_index ; index < end_index ; index++)
//				data_string_array[data_pos++] = date_data.get_index(index);
////			short ret = FinanceRecorderCmnDef.RET_SUCCESS;
////OUT:
//			for (int field_index = 1 ; field_index < FinanceRecorderCmnDef.FINANCE_DATABASE_FIELD_AMOUNT_LIST[source_type_index] ; field_index++)
//			{
//				switch(FinanceRecorderCmnDef.FINANCE_DATABASE_FIELD_TYPE_LIST[source_type_index][field_index])
//				{
//				case FinanceField_INT:
//				{
//					int[] data_array = get_int_array_elements(source_type_index, field_index, start_index, end_index);
//					data_pos = 0;
//					for (int index = start_index ; index < end_index ; index++, data_pos++)
//						data_string_array[data_pos] += String.format(",%d", data_array[data_pos]);
//				}
//				break;
//				case FinanceField_LONG:
//				{
//					long[] data_array = get_long_array_elements(source_type_index, field_index, start_index, end_index);
//					data_pos = 0;
//					for (int index = start_index ; index < end_index ; index++, data_pos++)
//						data_string_array[data_pos] += String.format(",%d", data_array[data_pos]);
//				}
//				break;
//				case FinanceField_FLOAT:
//				{
//					float[] data_array = get_float_array_elements(source_type_index, field_index, start_index, end_index);
//					data_pos = 0;
//					for (int index = start_index ; index < end_index ; index++, data_pos++)
//						data_string_array[data_pos] += String.format(",%.2f", data_array[data_pos]);
//				}
//				break;
//				default:
//				{
//					String errmsg = String.format("The unsupported field type: %d", FinanceRecorderCmnDef.FINANCE_DATABASE_FIELD_TYPE_LIST[source_type_index][field_index]);
//					FinanceRecorderCmnDef.format_error(errmsg);
//					throw new IllegalArgumentException(errmsg);
//				}
//				}
//			}
//			return data_string_array;
//		}

		public boolean is_empty(){return date_data.is_empty();}
		public int get_size()
		{
			assert date_data.get_size() == date_data_size : String.format("Incorrect data size, expected: %d, actual: %d", date_data.get_size(), date_data_size);
			return date_data_size;
		}
	};

	public static class ResultSetMap implements Iterable<Map.Entry<Integer, ResultSet>>
	{
		private TreeMap<Integer, ResultSet> result_set_map = new TreeMap<Integer, ResultSet>();
		private FinanceRecorderCmnDef.ResultSetDataUnit result_set_data_unit = FinanceRecorderCmnDef.ResultSetDataUnit.ResultSetDataUnit_NoSourceType;

		public ResultSetMap(){}
		public ResultSetMap(FinanceRecorderCmnDef.ResultSetDataUnit data_unit){result_set_data_unit = data_unit;}

		public FinanceRecorderCmnDef.ResultSetDataUnit get_data_unit(){return result_set_data_unit;}

		public short register_result_set(int source_key, final ResultSet result_set)
		{
			if (result_set_map.containsKey(source_key))
				throw new IllegalArgumentException(String.format("The source key[%d] of result set already exist", source_key));
			result_set_map.put(source_key, result_set);
			return FinanceRecorderCmnDef.RET_SUCCESS;
		}

		public final ResultSet lookup_result_set(int source_key)
		{
			if (!result_set_map.containsKey(source_key)) 
				throw new IllegalArgumentException(String.format("Fail to find the source key of result set: %d", source_key));
			return result_set_map.get(source_key);
		}

		@Override
		public Iterator<Map.Entry<Integer, ResultSet>> iterator()
		{
			Iterator<Map.Entry<Integer, ResultSet>> it = new Iterator<Map.Entry<Integer, ResultSet>>()
			{
				private Iterator<Map.Entry<Integer, ResultSet>> iter = result_set_map.entrySet().iterator();
				@Override
				public boolean hasNext()
				{
					return iter.hasNext();
				}
				@Override
				public Map.Entry<Integer, ResultSet> next()
				{
					return (Map.Entry<Integer, ResultSet>)iter.next();
				}
				@Override
				public void remove() {throw new UnsupportedOperationException();}
			};
			return it;
		}
	};
}
