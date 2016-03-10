package com.price.finance_recorder;

import java.util.*;
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
		public static int get_int_value(String time_str)
		{
			int[] data = get_date_value(time_str);
			assert data != null : String.format("Unsupported time format: %s", time_str);
			return ((data[0] & 0xFFFF) << 16) | ((data[1] & 0xFF) << 8) | (data[2] & 0xFF);
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
				year = month_list[0];
				month = month_list[1];
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
		public boolean is_month_type(){return type_is_month;}

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

		public final TimeCfg get_start_time()
		{
//			assert(time_start_cfg != NULL && "time_start_cfg should NOT be NULL");
			return time_start_cfg;
		}

		public String get_start_time_str()
		{
//			assert(time_start_cfg != NULL && "time_start_cfg should NOT be NULL");
			return (time_start_cfg != null ? time_start_cfg.toString() : null);
		}

		public final TimeCfg get_end_time()
		{
//			assert(time_end_cfg != NULL && "time_end_cfg should NOT be NULL");
			return time_end_cfg;
		}

		public String get_end_time_str()
		{
//			assert(time_start_cfg != NULL && "time_start_cfg should NOT be NULL");
			return (time_end_cfg != null ? time_end_cfg.toString() : null);
		}

		private void reset_time(TimeCfg new_start_time_cfg, TimeCfg new_end_time_cfg)
		{
			if (new_start_time_cfg != null)
				time_start_cfg = new_start_time_cfg;
			if (new_end_time_cfg != null)
				time_end_cfg = new_end_time_cfg;
			time_range_description = null;
		}

		public void set_start_time(TimeCfg new_start_time_cfg){reset_time(new_start_time_cfg, null);}
		public void set_end_time(TimeCfg new_end_time_cfg){reset_time(null, new_end_time_cfg);}

		public static int[] get_start_and_end_month_value_range(TimeRangeCfg time_range_cfg)
		{	
			assert time_range_cfg.is_month_type() : "The time_range_cfg should be Month type";
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
		public static int[] get_start_and_end_date_value_range(TimeRangeCfg time_range_cfg)
		{	
			assert !time_range_cfg.is_month_type() : "The time_range_cfg should be Date type";
			assert time_range_cfg.get_start_time() != null : "The start time in time_rangte_cfg should NOT be NULL";
			int[] date_value_start = TimeCfg.get_date_value(time_range_cfg.get_start_time_str());
			if (date_value_start == null)
				return null;
			assert time_range_cfg.get_end_time() != null : "The end time in time_rangte_cfg should NOT be NULL";
			int[] date_value_end = TimeCfg.get_date_value(time_range_cfg.get_end_time_str());
			if (date_value_end == null)
				return null;

			return new int[]{date_value_start[0], date_value_start[1], date_value_start[2], date_value_end[0], date_value_end[1], date_value_end[2]};
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
			array_data.add(data);
			array_size++;
		}

		public final T get_index(int index)
		{
			assert array_data != null : "array_data == NULL";
			if (index < 0)
				index = FinanceRecorderCmnDef.get_end_index_ex(index, array_size);
			if(index < 0 && index >= array_size)
			{
				String errmsg = String.format("index[%d] is NOT in ranage: [0, %d) or [-%d, 0)", index, array_size, array_size);
				FinanceRecorderCmnDef.error(errmsg);
				throw new IndexOutOfBoundsException(errmsg);
			}
			return array_data.get(index);
		}
	};

	public static class FinanceIntDataArray extends FinanceDataArrayBase<Integer>{};
	public static class FinanceLongDataArray extends FinanceDataArrayBase<Long>{};
	public static class FinanceFloatDataArray extends FinanceDataArrayBase<Float>{};
	public static class FinanceStringDataArray extends FinanceDataArrayBase<String>{};

	public static class QuerySet
	{
		private ArrayList<LinkedList<Integer>> query_array;
		private boolean add_done;

		public QuerySet()
		{
			query_array = new ArrayList<LinkedList<Integer>>();
			for (int i = 0 ; i < FinanceRecorderCmnDef.FINANCE_SOURCE_SIZE ; i++)
				query_array.add(new LinkedList<Integer>());
		}

		public short add_query(int source_index, int field_index)
		{
			if (add_done)
			{
				FinanceRecorderCmnDef.error("Fail to add another data");
				return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
			}

// Check if the index is out of range
			if(source_index < 0 && source_index >= FinanceRecorderCmnDef.FINANCE_SOURCE_SIZE)
			{
				FinanceRecorderCmnDef.error("source_index is out of range in QuerySet");
				return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
			}

			if(field_index < 0 && field_index >= FinanceRecorderCmnDef.FINANCE_DATABASE_FIELD_AMOUNT_LIST[source_index])
			{
// If field_index == -1, it means select all field in the table
				if (field_index != -1)
				{
					FinanceRecorderCmnDef.error("field_index is out of range in QuerySet");
					return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
				}
			}
// Check the index is duplicate
			if (query_array.get(source_index).indexOf(field_index) != -1)
			{
				FinanceRecorderCmnDef.format_warn("Duplicate index: %d in %s", field_index, FinanceRecorderCmnDef.FINANCE_DATABASE_DESCRIPTION_LIST[source_index]);
				return FinanceRecorderCmnDef.RET_WARN_INDEX_DUPLICATE;
			}
// If all fields are selected, it's no need to add extra index
			if (!query_array.get(source_index).isEmpty() && query_array.get(source_index).get(0) == -1)
			{
				FinanceRecorderCmnDef.format_warn("Ignore index: %d in %s", field_index, FinanceRecorderCmnDef.FINANCE_DATABASE_DESCRIPTION_LIST[source_index]);
				return FinanceRecorderCmnDef.RET_WARN_INDEX_IGNORE;
			}

// Add the index
			if (field_index == -1)
				query_array.get(source_index).clear();
			query_array.get(source_index).add(field_index);
			return FinanceRecorderCmnDef.RET_SUCCESS;
		}
		public short add_query(int source_index){return add_query(source_index, -1);}

		public short add_query_done()
		{
			if (add_done)
			{
				FinanceRecorderCmnDef.error("Fail to add another data");
				return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
			}
			for (int i = 0 ; i < FinanceRecorderCmnDef.FINANCE_SOURCE_SIZE ; i++)
			{
				if (query_array.get(i).isEmpty())
					continue;
//				WRITE_FORMAT_DEBUG("Transform the query data[source_index: %d]", i);
				if (query_array.get(i).get(0) == -1)
				{
					query_array.get(i).clear();
					for (int field_index = 1 ; field_index < FinanceRecorderCmnDef.FINANCE_DATABASE_FIELD_AMOUNT_LIST[i] ; field_index++) // Caution: Don't include the "date" field
						query_array.get(i).add(field_index);
				}
			}
			add_done = true;
			return FinanceRecorderCmnDef.RET_SUCCESS;
		}

		public boolean is_add_query_done() {return add_done;}

		final LinkedList<Integer> get_index(int index)
		{
			if (index < 0 || index >= FinanceRecorderCmnDef.FINANCE_SOURCE_SIZE)
			{
				String errmsg = String.format("The index[%d] is out of range(0, %d)", index , FinanceRecorderCmnDef.FINANCE_SOURCE_SIZE - 1);
				FinanceRecorderCmnDef.error(errmsg);
				throw new IndexOutOfBoundsException(errmsg);
			}
			return query_array.get(index);
		}
	};

	static public short add_query(QuerySet query_set, FinanceRecorderCmnDef.FinanceSourceType source_type, int field_index){return query_set.add_query(source_type.value(), field_index);}
	static public short add_query_ex(QuerySet query_set, FinanceRecorderCmnDef.FinanceSourceType source_type, int field_index, HashSet<Integer> source_type_index_set)
	{
		source_type_index_set.add(source_type.value());
		return add_query(query_set, source_type, field_index);
	}

	public static class ResultSet
	{
		private static short get_combined_index(int x, int y) {return (short)(((x & 0xFF) << 8) | (y & 0xFF));}
		private static short get_upper_subindex(short x) {return (short)((x >> 8) & 0xFF);}
		private static short get_lower_subindex(short x) {return (short)(x & 0xFF);}

		private HashMap<Integer, Integer> data_set_mapping;
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

		public short add_set(int source_index, int field_index)
		{
			if(source_index < 0 && source_index >= FinanceRecorderCmnDef.FINANCE_SOURCE_SIZE)
			{
				FinanceRecorderCmnDef.error("source_index is out of range in ResultSet");
				return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
			}
			if(field_index < 0 && field_index >= FinanceRecorderCmnDef.FINANCE_DATABASE_FIELD_AMOUNT_LIST[source_index])
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
//				for(int i = 1 ; i < FinanceRecorderCmnDef.FINANCE_DATABASE_FIELD_AMOUNT_LIST[source_index] ; i++)
//					field_array.add(i);
//			}
			for(Integer index : field_array)
			{
// Check if the source_type:field_type has been set
				short key = get_combined_index(source_index, index);
				if (data_set_mapping.get(key) != null)
				{
					FinanceRecorderCmnDef.format_error("The key[%d] from (%d, %d) is duplicate", key, source_index, index);
					return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
				}
// Check the lookup table to find the date type of the specific field
				short value;
				switch(FinanceRecorderCmnDef.FINANCE_DATABASE_FIELD_TYPE_LIST[source_index][field_index])
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
					FinanceRecorderCmnDef.format_error("The unsupported field type: %d", FinanceRecorderCmnDef.FINANCE_DATABASE_FIELD_TYPE_LIST[source_index][field_index]);
					return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
				}
				FinanceRecorderCmnDef.format_debug("ResultSet Map: %d, %d", key, value);
				data_set_mapping.put(Integer.valueOf(key), Integer.valueOf(value));
			}
			return FinanceRecorderCmnDef.RET_SUCCESS;
		}

		public short add_set(int source_index, final LinkedList<Integer> field_list)
		{
			short ret = FinanceRecorderCmnDef.RET_SUCCESS;
			ListIterator<Integer> iter = field_list.listIterator(0);
			while (iter.hasNext())
			{
				Integer field_index = iter.next();
				ret = add_set(source_index, field_index);
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

		public int find_first_after_date_index(String search_date, int search_start_index)
		{
			int search_date_value = TimeCfg.get_int_value(search_date);
			for (int index = search_start_index ; index < date_data_size ; index++)
			{
				int date_value = TimeCfg.get_int_value(date_data.get_index(index));
				if (date_value >= search_date_value)
					return index;
			}
			return date_data_size;
		}

		public final FinanceStringDataArray get_date_array(){return date_data;}
		public final String get_date_array_element(int index)
		{
//			if (index < 0 || index >= date_data_size)
//				throw new IndexOutOfBoundsException(String.format("The Index [%d] is out of range [0, %d)", index, date_data_size));
			return date_data.get_index(index);
		}
		public String[] get_date_array_elements(int source_index, int field_index, int start_index, int end_index) 
		{
			if (start_index < 0)
				start_index = FinanceRecorderCmnDef.get_end_index_ex(start_index, date_data_size);
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

		private short find_data_pos(int source_index, int field_index, short[] field_type_array)
		{
			short key = get_combined_index(source_index, field_index);
			if (data_set_mapping.get(Integer.valueOf(key)) == null)
			{
				FinanceRecorderCmnDef.format_error("The key[%d] from (%d, %d) is NOT FOUND", key, source_index, field_index);
				return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
			}
			short value = data_set_mapping.get(Integer.valueOf(key)).shortValue();
			field_type_array[0] = get_upper_subindex(value);
			field_type_array[1] = get_lower_subindex(value);
			return FinanceRecorderCmnDef.RET_SUCCESS;
		}

		private void find_and_check_data_pos(int source_index, int field_index, short[] field_type_array)
		{
			if (FinanceRecorderCmnDef.CheckFailure(find_data_pos(source_index, field_index, field_type_array)))
				throw new IllegalArgumentException(String.format("The key[%d] from (%d, %d) is NOT FOUND", source_index, field_index));
		}

		public short set_data(int source_index, int field_index, Object data_obj)
		{
			short[] field_type_array = new short[2];
			short ret = find_data_pos(source_index, field_index, field_type_array);
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
					short source_index = get_upper_subindex(key);
					short field_index = get_lower_subindex(key);
					FinanceRecorderCmnDef.format_error("Incorrect data size in %d, %d, expected: %d, actual: %d", source_index, field_index, date_data_size, data_size);
					return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
				}
			}
			return FinanceRecorderCmnDef.RET_SUCCESS;
		}

		public short show_data()
		{
			if (!FinanceRecorderCmnDef.is_show_console())
				return FinanceRecorderCmnDef.RET_SUCCESS;

			final int STAR_LEN = 120;
			short key;
			short value;
			short source_index;
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
				source_index = get_upper_subindex(key);
				field_index = get_lower_subindex(key);
				System.out.printf(" %s:%s%d |", FinanceRecorderCmnDef.FINANCE_DATABASE_DESCRIPTION_LIST[source_index], FinanceRecorderCmnDef.MYSQL_FILED_NAME_BASE, field_index);
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

		public final FinanceIntDataArray get_int_array(int source_index, int field_index)
		{
			short[] field_type_array = new short[2];
			find_and_check_data_pos(source_index, field_index, field_type_array);
			check_data_pos_boundary(field_type_array[0], field_type_array[1], FinanceRecorderCmnDef.FinanceFieldType.FinanceField_INT.value(), int_data_set_size);
			assert field_type_array[0] == FinanceRecorderCmnDef.FinanceFieldType.FinanceField_INT.value() : String.format("The type[%d] is NOT INT", FinanceRecorderCmnDef.FinanceFieldType.FinanceField_FLOAT.value());
			return int_data_set.get(field_type_array[1]);
		}
		public final FinanceLongDataArray get_long_array(int source_index, int field_index)
		{
			short[] field_type_array = new short[2];
			find_and_check_data_pos(source_index, field_index, field_type_array);
			check_data_pos_boundary(field_type_array[0], field_type_array[1], FinanceRecorderCmnDef.FinanceFieldType.FinanceField_LONG.value(), long_data_set_size);
			assert field_type_array[0] == FinanceRecorderCmnDef.FinanceFieldType.FinanceField_LONG.value() : String.format("The type[%d] is NOT LONG", FinanceRecorderCmnDef.FinanceFieldType.FinanceField_FLOAT.value());
			return long_data_set.get(field_type_array[1]);
		}
		public final FinanceFloatDataArray get_float_array(int source_index, int field_index)
		{
			short[] field_type_array = new short[2];
			find_and_check_data_pos(source_index, field_index, field_type_array);
			check_data_pos_boundary(field_type_array[0], field_type_array[1], FinanceRecorderCmnDef.FinanceFieldType.FinanceField_FLOAT.value(), float_data_set_size);
			assert field_type_array[0] == FinanceRecorderCmnDef.FinanceFieldType.FinanceField_FLOAT.value() : String.format("The type[%d] is NOT FLOAT", FinanceRecorderCmnDef.FinanceFieldType.FinanceField_FLOAT.value());
			return float_data_set.get(field_type_array[1]);
		}
		public int get_int_array_element(int source_index, int field_index, int index) 
		{
			if (index < 0 || index >= date_data_size)
				throw new IndexOutOfBoundsException(String.format("The Index [%d] is out of range [0, %d)", index, date_data_size));
			return get_int_array(source_index, field_index).get_index(index);
		}
		public long get_long_array_element(int source_index, int field_index, int index) 
		{
			if (index < 0 || index >= date_data_size)
				throw new IndexOutOfBoundsException(String.format("The Index [%d] is out of range [0, %d)", index, date_data_size));
			return get_long_array(source_index, field_index).get_index(index);
		}
		public float get_float_array_element(int source_index, int field_index, int index) {return get_float_array(source_index, field_index).get_index(index);}

		public int[] get_int_array_elements(int source_index, int field_index, int start_index, int end_index) 
		{
			if (start_index < 0 || start_index >= date_data_size)
				throw new IndexOutOfBoundsException(String.format("The Start Index [%d] is out of range [0, %d)", start_index, date_data_size));
			if (end_index < 1 || end_index > date_data_size)
				throw new IndexOutOfBoundsException(String.format("The End Index [%d] is out of range (1, %d]", start_index, date_data_size));
			int data_len = end_index - start_index;
			assert data_len > 0 : String.format("End Index[%d] SHOULD be larger than Start Index[%d]", end_index, start_index);
			int data_array[] = new int[data_len];
			FinanceIntDataArray int_data_array = get_int_array(source_index, field_index);
			int data_pos = 0;
			for (int index = start_index ; index < end_index ; index++)
				data_array[data_pos++] = int_data_array.get_index(index);
			return data_array;
		}
		public long[] get_long_array_elements(int source_index, int field_index, int start_index, int end_index) 
		{
			if (start_index < 0 || start_index >= date_data_size)
				throw new IndexOutOfBoundsException(String.format("The Start Index [%d] is out of range [0, %d)", start_index, date_data_size));
			if (end_index < 1 || end_index > date_data_size)
				throw new IndexOutOfBoundsException(String.format("The End Index [%d] is out of range (1, %d]", start_index, date_data_size));
			int data_len = end_index - start_index;
			assert data_len > 0 : String.format("End Index[%d] SHOULD be larger than Start Index[%d]", end_index, start_index);
			long data_array[] = new long[data_len];
			FinanceLongDataArray long_data_array = get_long_array(source_index, field_index);
			int data_pos = 0;
			for (int index = start_index ; index < end_index ; index++)
				data_array[data_pos++] = long_data_array.get_index(index);
			return data_array;
		}
		public float[] get_float_array_elements(int source_index, int field_index, int start_index, int end_index) 
		{
			if (start_index < 0 || start_index >= date_data_size)
				throw new IndexOutOfBoundsException(String.format("The Start Index [%d] is out of range [0, %d)", start_index, date_data_size));
			if (end_index < 1 || end_index > date_data_size)
				throw new IndexOutOfBoundsException(String.format("The End Index [%d] is out of range (1, %d]", start_index, date_data_size));
			int data_len = end_index - start_index;
			assert data_len > 0 : String.format("End Index[%d] SHOULD be larger than Start Index[%d]", end_index, start_index);
			float data_array[] = new float[data_len];
			FinanceFloatDataArray float_data_array = get_float_array(source_index, field_index);
			int data_pos = 0;
			for (int index = start_index ; index < end_index ; index++)
				data_array[data_pos++] = float_data_array.get_index(index);
			return data_array;
		}

		public String[] get_array_all_elements_string_list(int source_index, int start_index, int end_index)
		{
			if (start_index < 0 || start_index >= date_data_size)
				throw new IndexOutOfBoundsException(String.format("The Start Index [%d] is out of range [0, %d)", start_index, date_data_size));
			if (end_index < 1 || end_index > date_data_size)
				throw new IndexOutOfBoundsException(String.format("The End Index [%d] is out of range (1, %d]", start_index, date_data_size));
			int data_len = end_index - start_index;
			assert data_len > 0 : String.format("End Index[%d] SHOULD be larger than Start Index[%d]", end_index, start_index);
			String data_array_str[] = new String[data_len];
			int data_pos = 0;
			for (int index = start_index ; index < end_index ; index++)
				data_array_str[data_pos++] = date_data.get_index(index);
			short ret = FinanceRecorderCmnDef.RET_SUCCESS;
OUT:
			for (int field_index = 1 ; field_index < FinanceRecorderCmnDef.FINANCE_DATABASE_FIELD_AMOUNT_LIST[source_index] ; field_index++)
			{
				switch(FinanceRecorderCmnDef.FINANCE_DATABASE_FIELD_TYPE_LIST[source_index][field_index])
				{
				case FinanceField_INT:
				{
					int[] data_array = get_int_array_elements(source_index, field_index, start_index, end_index);
					data_pos = 0;
					for (int index = start_index ; index < end_index ; index++, data_pos++)
						data_array_str[data_pos] += String.format(",%d", data_array[data_pos]);
				}
				break;
				case FinanceField_LONG:
				{
					long[] data_array = get_long_array_elements(source_index, field_index, start_index, end_index);
					data_pos = 0;
					for (int index = start_index ; index < end_index ; index++, data_pos++)
						data_array_str[data_pos] += String.format(",%d", data_array[data_pos]);
				}
				break;
				case FinanceField_FLOAT:
				{
					float[] data_array = get_float_array_elements(source_index, field_index, start_index, end_index);
					data_pos = 0;
					for (int index = start_index ; index < end_index ; index++, data_pos++)
						data_array_str[data_pos] += String.format(",%.2f", data_array[data_pos]);
				}
				break;
				default:
					FinanceRecorderCmnDef.format_error("The unsupported field type: %d", FinanceRecorderCmnDef.FINANCE_DATABASE_FIELD_TYPE_LIST[source_index][field_index]);
					ret = FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
					break OUT;
				}
			}
			return data_array_str;
		}

		public boolean is_empty(){return date_data.is_empty();}
		public int get_size()
		{
			assert date_data.get_size() == date_data_size : String.format("Incorrect data size, expected: %d, actual: %d", date_data.get_size(), date_data_size);
			return date_data_size;
		}
	};
}
