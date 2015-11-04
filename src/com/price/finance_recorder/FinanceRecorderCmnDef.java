package com.price.finance_recorder;

import java.io.*;
import java.util.Calendar;
import java.util.List;
import java.util.regex.*;


public class FinanceRecorderCmnDef
{
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Return values
	public static final short RET_SUCCESS = 0;

	public static final short RET_FAILURE_UNKNOWN = 1;
	public static final short RET_FAILURE_INVALID_ARGUMENT = 2;
	public static final short RET_FAILURE_INVALID_POINTER = 3;
	public static final short RET_FAILURE_INSUFFICIENT_MEMORY = 4;
	public static final short RET_FAILURE_INCORRECT_OPERATION = 5;
	public static final short RET_FAILURE_NOT_FOUND = 6;
	public static final short RET_FAILURE_INCORRECT_CONFIG = 7;
	public static final short RET_FAILURE_HANDLE_THREAD = 8;
	public static final short RET_FAILURE_INCORRECT_PATH = 9;
	public static final short RET_FAILURE_IO_OPERATION = 10;

	public static final short RET_FAILURE_MYSQL_BASE = 100;
	public static final short RET_FAILURE_MYSQL = RET_FAILURE_MYSQL_BASE + 1;
	public static final short RET_FAILURE_MYSQL_UNKNOWN_DATABASE = RET_FAILURE_MYSQL_BASE + 2;
	public static final short RET_FAILURE_MYSQL_DATABASE_ALREADY_EXIST = RET_FAILURE_MYSQL_BASE + 3;

	public static boolean CheckSuccess(short x) {return (x == RET_SUCCESS ? true : false);}
	public static boolean CheckFailure(short x) {return !CheckSuccess(x);}

	public static boolean CheckFailureNotFound(short x) {return (x == RET_FAILURE_NOT_FOUND ? true : false);}
	public static boolean CheckMySQLFailureUnknownDatabase(short x) {return (x == RET_FAILURE_MYSQL_UNKNOWN_DATABASE ? true : false);}

	private static final String[] RetDescription = new String[]
	{
		"Success",
		"Failure Unknown",
		"Failure Invalid Argument",
		"Failure Invalid Pointer",
		"Failure Insufficient Memory",
		"Failure Incorrect Operation",
		"Failure Not Found",
		"Failure Incorrect Config",
		"Failure Handle Thread",
		"Failure Incorrect Path",
		"Failure IO Operation"
//		"Failure MySQL"
	};

	private static final String[] SQLRetDescription = new String[]
	{
		"SQL Success",
		"SQL Failure Common",
		"SQL Failure Unknown Database",
		"SQL Failure Database Already Exist"
	};

	public static String GetErrorDescription(short error_code)
	{
		if (error_code > RET_FAILURE_MYSQL_BASE)
			return SQLRetDescription[error_code - RET_FAILURE_MYSQL_BASE];
		else
			return RetDescription[error_code];
	}

//	public static final String CONF_FOLDERNAME = "conf";
	public static final String DATA_FOLDER_NAME = "/var/tmp/finance";
	public static final String DATA_SPLIT = ",";
	public static final String CONF_FOLDERNAME = "conf";
	public static final String CONF_ENTRY_IGNORE_FLAG = "#";
	public static final int MAX_CONCURRENT_THREAD = 4;
	public static final int MAX_MONTH_RANGE_IN_THREAD = 3;

	public static enum FinanceDataType
	{
		FinanceData_StockTop3LegalPersonsNetBuyOrSell(0),
		FinanceData_FutureTop3LegalPersonsOpenInterest(1),
		FinanceData_FutureTop10DealersAndLegalPersons(2);

		private int value = 0;
		private FinanceDataType(int value){this.value = value;}
		public static FinanceDataType valueOf(int value)
		{
			switch (value)
			{
			case 0: return FinanceData_StockTop3LegalPersonsNetBuyOrSell;
			case 1: return FinanceData_FutureTop3LegalPersonsOpenInterest;
			case 2: return FinanceData_FutureTop10DealersAndLegalPersons;
			default: return null;
			}
		}
		public int value(){return this.value;}
	};

	public static enum DatabaseNotExistIngoreType
	{
		DatabaseNotExistIngore_Yes,
		DatabaseNotExistIngore_No,
	};
	public static enum DatabaseCreateThreadType
	{
		DatabaseCreateThread_Single,
		DatabaseCreateThread_Multiple,
	};

	public static class TimeRangeCfg
	{
		public enum TimeType{TIME_NONE, TIME_DATE, TIME_MONTH};
		public TimeType time_type = null;
		public String time_start_str; // Format: "2015-09" or "2015-09-04"
		public String time_end_str; // Format: "2015-09" or "2015-09-04"

		public TimeRangeCfg(String start_str, String end_str)
		{
			if (parse_date_range(start_str) != null && parse_date_range(end_str) != null)
				time_type = TimeType.TIME_DATE;
			else if (parse_month_range(start_str) != null && parse_month_range(end_str) != null)
				time_type = TimeType.TIME_MONTH;
			else
				time_type = TimeType.TIME_NONE;
			assert time_type == TimeType.TIME_NONE : String.format("Incorrect time format: %s:%s", start_str, end_str); 
			time_start_str = start_str;
			time_end_str = end_str;
		}
		public TimeRangeCfg(int year_start, int month_start, int year_end, int month_end)
		{
			time_type = TimeType.TIME_MONTH;
			time_start_str = String.format("%04d-%02d", year_start, month_start);
			time_end_str = String.format("%04d-%02d", year_end, month_end);
		}
		public TimeRangeCfg(int year_start, int month_start, int day_start, int year_end, int month_end, int day_end)
		{
			time_type = TimeType.TIME_DATE;
			time_start_str = String.format("%04d-%02d-%02d", year_start, month_start, day_start);
			time_end_str = String.format("%04d-%02d-%02d", year_end, month_end, day_end);
		}
		@Override
		public String toString() 
		{
			// TODO Auto-generated method stub
			return String.format("%s:%s", time_start_str, time_end_str);
		}
	};

	public static final String[] FINANCE_DATA_NAME_LIST = new String[]
	{
		"stock_top3_legal_persons_net_buy_or_sell",
		"future_top3_legal_persons_open_interest",
		"future_top10_dealers_and_legal_persons"
	};
	public static final String[] FINANCE_DATA_DESCRIPTION_LIST = new String[]
	{
		"三大法人現貨買賣超",
		"三大法人期貨留倉淨額",
		"十大交易人及特定法人期貨資訊"
	};
	private static final String[] STOCK_TOP3_LEGAL_PERSONS_NET_BUY_OR_SELL_FIELD_DEFINITION = new String[]
	{
		"date DATE NOT NULL PRIMARY KEY", // 日期
		"value1 BIGINT", // 自營商(自行買賣)_買進金額
		"value2 BIGINT", // 自營商(自行買賣)_賣出金額
		"value3 BIGINT", // 自營商(自行買賣)_買賣差額
		"value4 BIGINT", // 自營商(避險)_買進金額
		"value5 BIGINT", // 自營商(避險)_賣出金額
		"value6 BIGINT", // 自營商(避險)_買賣差額
		"value7 BIGINT", // 投信_買進金額
		"value8 BIGINT", // 投信_賣出金額
		"value9 BIGINT", // 投信_買賣差額
		"value10 BIGINT", // 外資及陸資_買進金額
		"value11 BIGINT", // 外資及陸資_賣出金額
		"value12 BIGINT", // 外資及陸資_買賣差額
	};
	private static final String[] FUTURE_TOP3_LEGAL_PERSONS_OPEN_INTEREST_FIELD_DEFINITION = new String[]
	{
		"date DATE NOT NULL PRIMARY KEY", // 日期
		"value1 INT", // 自營商_多方_口數 int",
		"value2 INT", // 自營商_多方_契約金額 int",
		"value3 INT", // 自營商_空方_口數 int",
		"value4 INT", // 自營商_空方_契約金額 int",
		"value5 INT", // 自營商_多空淨額_口數 int",
		"value6 INT", // 自營商_多空淨額_契約金額 int",
		"value7 INT", // "投信_多方_口數 int",
		"value8 INT", // 投信_多方_契約金額 int",
		"value9 INT", // 投信_空方_口數 int",
		"value10 INT", // 投信_空方_契約金額 int",
		"value11 INT", // 投信_多空淨額_口數 int",
		"value12 INT", // 投信_多空淨額_契約金額 int",
		"value13 INT", // 外資_多方_口數 int",
		"value14 INT", // 外資_多方_契約金額 int",
		"value15 INT", // 外資_空方_口數 int",
		"value16 INT", // 外資_空方_契約金額 int",
		"value17 INT", // 外資_多空淨額_口數 int",
		"value18 INT", // 外資_多空淨額_契約金額 int",
	};
	private static final String[] FUTURE_TOP10_DEALERS_AND_LEGAL_PERSONS_FIELD_DEFINITION = new String[]
	{
		"date DATE NOT NULL PRIMARY KEY", // 日期
		"value1 INT", // 臺股期貨_到期月份_買方_前五大交易人合計_部位數
		"value2 FLOAT", // 臺股期貨_到期月份_買方_前五大交易人合計_百分比
		"value3 INT", // 臺股期貨_到期月份_買方_前十大交易人合計_部位數
		"value4 FLOAT", // 臺股期貨_到期月份_買方_前十大交易人合計_百分比
		"value5 INT", // 臺股期貨_到期月份_賣方_前五大交易人合計_部位數
		"value6 FLOAT", // 臺股期貨_到期月份_賣方_前五大交易人合計_百分比
		"value7 INT", // 臺股期貨_到期月份_賣方_前十大交易人合計_部位數
		"value8 FLOAT", // 臺股期貨_到期月份_賣方_前十大交易人合計_百分比
		"value9 INT", // 臺股期貨_到期月份_全市場未沖銷部位數
		"value10 INT", // 臺股期貨_所有契約_買方_前五大交易人合計_部位數
		"value11 FLOAT", // 臺股期貨_所有契約_買方_前五大交易人合計_百分比
		"value12 INT", // 臺股期貨_所有契約_買方_前十大交易人合計_部位數
		"value13 FLOAT", // 臺股期貨_所有契約_買方_前十大交易人合計_百分比
		"value14 INT", // 臺股期貨_所有契約_賣方_前五大交易人合計_部位數
		"value15 FLOAT", // 臺股期貨_所有契約_賣方_前五大交易人合計_百分比
		"value16 INT", // 臺股期貨_所有契約_賣方_前十大交易人合計_部位數
		"value17 FLOAT", // 臺股期貨_所有契約_賣方_前十大交易人合計_百分比
		"value18 INT", // 臺股期貨_所有契約_全市場未沖銷部位數
	};
	public static final String[] FINANCE_DATA_SQL_FIELD_LIST = new String[]
	{
		transform_array_to_sql_string(STOCK_TOP3_LEGAL_PERSONS_NET_BUY_OR_SELL_FIELD_DEFINITION),
		transform_array_to_sql_string(FUTURE_TOP3_LEGAL_PERSONS_OPEN_INTEREST_FIELD_DEFINITION),
		transform_array_to_sql_string(FUTURE_TOP10_DEALERS_AND_LEGAL_PERSONS_FIELD_DEFINITION)
	};

	public static final short NOTIFY_GET_DATA = 0;
	public static final int EACH_UPDATE_DATA_AMOUNT = 20;

	private static MsgDumperWrapper msg_dumper = MsgDumperWrapper.get_instance();

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Functions
	private static final String get_code_position(){return String.format("%s:%d", FinanceRecorderCmnBase.__FILE__(), FinanceRecorderCmnBase.__LINE__());}

	public static String field_array_to_string(String[] field_array)
	{
		String field_string = null;
		for (String field : field_array)
		{
			if (field_string == null)
				field_string = field;
			else
				field_string += (FinanceRecorderCmnDef.DATA_SPLIT + field);
		}

		return field_string;
	}

	public static String[] field_string_to_array(String field_string)
	{
		return field_string.split(DATA_SPLIT);
	}

	public static void debug(String msg) 
	{
		msg_dumper.write_debug_msg(String.format("[%s:%d] %s", FinanceRecorderCmnBase.__FILE__(), FinanceRecorderCmnBase.__LINE__(), msg));
	}
	public static void info(String msg) 
	{
		msg_dumper.write_info_msg(String.format("[%s:%d] %s", FinanceRecorderCmnBase.__FILE__(), FinanceRecorderCmnBase.__LINE__(), msg));
	}
	public static void warn(String msg)
	{
		msg_dumper.write_warn_msg(String.format("[%s:%d] %s", FinanceRecorderCmnBase.__FILE__(), FinanceRecorderCmnBase.__LINE__(), msg));
	}
	public static void error(String msg)
	{
		msg_dumper.write_error_msg(String.format("[%s:%d] %s", FinanceRecorderCmnBase.__FILE__(), FinanceRecorderCmnBase.__LINE__(), msg));
	}
	
	public static void format_debug(String format, Object... arguments) 
	{
		msg_dumper.write_debug_msg(String.format("[%s:%d] %s", FinanceRecorderCmnBase.__FILE__(), FinanceRecorderCmnBase.__LINE__(), String.format(format, arguments)));
	}
	public static void format_info(String format, Object... arguments)
	{
		msg_dumper.write_info_msg(String.format("[%s:%d] %s", FinanceRecorderCmnBase.__FILE__(), FinanceRecorderCmnBase.__LINE__(), String.format(format, arguments)));
	}
	public static void format_warn(String format, Object... arguments)
	{
		msg_dumper.write_warn_msg(String.format("[%s:%d] %s", FinanceRecorderCmnBase.__FILE__(), FinanceRecorderCmnBase.__LINE__(), String.format(format, arguments)));
	}
	public static void format_error(String format, Object... arguments)
	{
		msg_dumper.write_error_msg(String.format("[%s:%d] %s", FinanceRecorderCmnBase.__FILE__(), FinanceRecorderCmnBase.__LINE__(), String.format(format, arguments)));
	}

	public static String get_current_path()
	{
		String cur_path = null;
		try
		{
			File cur_dir = new File (".");
			cur_path = cur_dir.getCanonicalPath();
		}
		catch(Exception e)
		{
			String msg = String.format("Fail to get the current path: %s", e.toString());
			return null;
		}
		return cur_path;
	}

	public static String transform_array_to_sql_string(String[] string_arr)
	{
		String sql_string = null;
		for(String string : string_arr)
		{
//			try
//			{
				String encoded_string = string;//new String(string.getBytes("UTF-16"), "Big5");
				if (sql_string == null)
					sql_string = encoded_string;
				else
					sql_string += String.format(",%s", encoded_string);
//			}
//			catch (UnsupportedEncodingException e)
//			{
//				format_error("Fail to encode: %s", string);
//				return null;
//			}
		}
		return sql_string;
	}

	public static final String get_time_month_today()
	{
//		java.util.Date date_today = new java.util.Date();
		java.util.Date date = new java.util.Date(); // your date
	    Calendar cal = Calendar.getInstance();
	    cal.setTime(date);
//	    int year = cal.get(Calendar.YEAR);
//	    int month = cal.get(Calendar.MONTH);
//	    int day = cal.get(Calendar.DAY_OF_MONTH);
		String time_month_today = String.format("%04d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH));
		return time_month_today;
	}

	private static Matcher parse_time_range(String time_str, String search_pattern)
	{
// Time Format: yyyy-mm; Ex: 2015-09
// Time Format: yyyy-MM-dd; Ex: 2015-09-04		
		Pattern pattern = Pattern.compile(search_pattern);
		Matcher matcher = pattern.matcher(time_str);
		if (!matcher.find())
		{
//			FinanceRecorderCmnDef.format_error("Incorrect time format: %s", time_str);
			return null;
		}
		return matcher;
	}

	public static Matcher parse_month_range(String time_str){return parse_time_range(time_str, "([\\d]{4})-([\\d]{1,2})");}
	public static Matcher parse_date_range(String time_str){return parse_time_range(time_str, "([\\d]{4})-([\\d]{1,2})-([\\d]{1,2})");}

	public static int[] get_start_and_end_month_range(FinanceRecorderCmnDef.TimeRangeCfg time_range_cfg)
	{			
		Matcher month_start_matcher = parse_month_range(time_range_cfg.time_start_str);
		if (month_start_matcher == null)
		{
			FinanceRecorderCmnDef.format_error("Incorrect time format (start): %s", time_range_cfg.time_start_str);
			return null;
		}
		Matcher month_end_matcher = parse_month_range(time_range_cfg.time_end_str);
		if (month_end_matcher == null)
		{
			FinanceRecorderCmnDef.format_error("Incorrect time format (End): %s", time_range_cfg.time_end_str);
			return null;
		}
	
		int year_start = Integer.valueOf(month_start_matcher.group(1));
		int month_start = Integer.valueOf(month_start_matcher.group(2));
		int year_end = Integer.valueOf(month_end_matcher.group(1));
		int month_end = Integer.valueOf(month_end_matcher.group(2));

		return new int[]{year_start, month_start, year_end, month_end};
	}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Interface
	public interface FinanceObserverInf
	{
		public short notify(short type);
	}
//	public interface FinanceReaderInf
//	{
//		public short initialize(FinanceObserverInf observer, String data_filename);
//		public short read(List<String> data_list);
//		public short deinitialize();
//	}
//	public interface FinanceWriterInf
//	{
//		public short initialize(FinanceObserverInf observer, String database_name, List<String> sql_file_field_mapping);
//		public short write(List<String> data_list);
//		public short deinitialize();
//	}
}
