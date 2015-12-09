package com.price.finance_recorder;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.regex.*;
import java.text.*;


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
	public static final short RET_FAILURE_UNEXPECTED_VALUE = 11;

	public static final short RET_FAILURE_MYSQL_BASE = 100;
	public static final short RET_FAILURE_MYSQL = RET_FAILURE_MYSQL_BASE + 1;
	public static final short RET_FAILURE_MYSQL_UNKNOWN_DATABASE = RET_FAILURE_MYSQL_BASE + 2;
	public static final short RET_FAILURE_MYSQL_NO_DRIVER = RET_FAILURE_MYSQL_BASE + 3;
	public static final short RET_FAILURE_MYSQL_EXECUTE_COMMAND = RET_FAILURE_MYSQL_BASE + 4;
	public static final short RET_FAILURE_MYSQL_DATABASE_ALREADY_EXIST = RET_FAILURE_MYSQL_BASE + 5;
	public static final short RET_FAILURE_MYSQL_DATA_NOT_CONSISTENT = RET_FAILURE_MYSQL_BASE + 6;

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
		"Failure IO Operation",
		"Failure Unexpected Value"
	};

	private static final String[] SQLRetDescription = new String[]
	{
		"SQL Success",
		"SQL Failure Common",
		"SQL Failure Unknown Database",
		"SQL Failure No Driver",
		"SQL Failure Execute Command",
		"SQL Failure Database Already Exist",
		"SQL Failure Data Not Consistent"
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
	public static final String DATABASE_TIME_RANGE_FILENAME = ".database_time_range.conf";
	public static final String DATABASE_TIME_RANGE_FILE_DST_PROJECT_NAME = "finance_analyzer";

	public static enum FinanceDataType
	{
		FinanceData_StockExchangeAndVolume(0),
		FinanceData_StockTop3LegalPersonsNetBuyOrSell(1),
		FinanceData_StockMarginTradingAndShortSelling(2),
		FinanceData_FutureAndOptionTop3LegalPersonsOpenInterest(3),
		FinanceData_FutureOrOptionTop3LegalPersonsOpenInterest(4),
		FinanceData_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest(5),
		FinanceData_OptionPutCallRatio(6),
		FinanceData_FutureTop10DealersAndLegalPersons(7);

		private int value = 0;
		private FinanceDataType(int value){this.value = value;}
		public static FinanceDataType valueOf(int value)
		{
			switch (value)
			{
			case 0: return FinanceData_StockExchangeAndVolume;
			case 1: return FinanceData_StockTop3LegalPersonsNetBuyOrSell;
			case 2: return FinanceData_StockMarginTradingAndShortSelling;
			case 3: return FinanceData_FutureAndOptionTop3LegalPersonsOpenInterest;
			case 4: return FinanceData_FutureOrOptionTop3LegalPersonsOpenInterest;
			case 5: return FinanceData_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest;
			case 6: return FinanceData_OptionPutCallRatio;
			case 7: return FinanceData_FutureTop10DealersAndLegalPersons;
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
	public static enum DatabaseEnableBatchType
	{
		DatabaseEnableBatch_Yes,
		DatabaseEnableBatch_No,
	};

	public static class TimeRangeCfg
	{
		public enum TimeType{TIME_DATE, TIME_MONTH};
		public TimeType time_type = null;
		public String time_start_str = null; // Format: "2015-09" or "2015-09-04"
		public String time_end_str = null; // Format: "2015-09" or "2015-09-04"

		public TimeRangeCfg(String start_str, String end_str)
		{
			TimeType time_type_start = null;
			if (start_str != null)
			{
				if (get_date_value_matcher(start_str) != null)
					time_type_start = TimeType.TIME_DATE;
				else if (get_month_value_matcher(start_str) != null)
					time_type_start = TimeType.TIME_MONTH;
				else
					assert false : String.format("Incorrect start time format: %s", start_str);
			}
			TimeType time_type_end = null;
			if (end_str != null)
			{
				if (get_date_value_matcher(end_str) != null)
					time_type_end = TimeType.TIME_DATE;
				else if (get_month_value_matcher(end_str) != null)
					time_type_end = TimeType.TIME_MONTH;
				else
					assert false : String.format("Incorrect end time format: %s", end_str);
			}
			if (time_type_start != null && time_type_end != null)
			{
				assert time_type_start != time_type_end : String.format("Start and end time format is NOT consistent: %s:%s", start_str, end_str);
				time_type = time_type_start;
			}
			else if (time_type_start != null)
				time_type = time_type_start;
			else if (time_type_end != null)
				time_type = time_type_end;
//			assert time_type == TimeType.TIME_NONE : String.format("Incorrect time format: %s:%s", start_str, end_str); 
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
		public boolean is_date_type(){return time_type == TimeType.TIME_DATE;}

		@Override
		public String toString() 
		{
			// TODO Auto-generated method stub
			return String.format("%s:%s", time_start_str, time_end_str);
		}
	};

	public static final String[] FINANCE_DATA_NAME_LIST = new String[]
	{
		"stock_exchange_and_volume",
		"stock_top3_legal_persons_net_buy_or_sell",
		"stock_margin_trading_and_short_selling",
		"future_and_option_top3_legal_persons_open_interest",
		"future_or_option_top3_legal_persons_open_interest",
		"option_top3_legal_persons_buy_and_sell_option_open_interest",
		"option_put_call_ratio",
		"future_top10_dealers_and_legal_persons"
	};
	public static final String[] FINANCE_DATA_DESCRIPTION_LIST = new String[]
	{
		"臺股指數及成交量",
		"三大法人現貨買賣超",
		"現貨融資融券餘額",
		"三大法人期貨和選擇權留倉淨額",
		"三大法人期貨或選擇權留倉淨額",
		"三大法人選擇權買賣權留倉淨額",
		"三大法人選擇權賣權買權比",
		"十大交易人及特定法人期貨資訊"
	};
	private static final String[] STOCK_EXCHANGE_AND_VALUE_FIELD_DEFINITION = new String[]
	{
		"date", // 日期
		"value1", // 成交股數
		"value2", // 成交金額
		"value3", // 成交筆數
		"value4", // 發行量加權股價指數
		"value5", // 漲跌點數
	};
	private static final String[] STOCK_EXCHANGE_AND_VALUE_FIELD_TYPE_DEFINITION = new String[]
	{
		"DATE NOT NULL PRIMARY KEY", // 日期
		"BIGINT", // 成交股數
		"BIGINT", // 成交金額
		"INT", // 成交筆數
		"FLOAT", // 發行量加權股價指數
		"FLOAT", // 漲跌點數
	};
	private static final String[] STOCK_TOP3_LEGAL_PERSONS_NET_BUY_OR_SELL_FIELD_DEFINITION = new String[]
	{
		"date", // 日期
		"value1", // 自營商(自行買賣)_買進金額
		"value2", // 自營商(自行買賣)_賣出金額
		"value3", // 自營商(自行買賣)_買賣差額
		"value4", // 自營商(避險)_買進金額
		"value5", // 自營商(避險)_賣出金額
		"value6", // 自營商(避險)_買賣差額
		"value7", // 投信_買進金額
		"value8", // 投信_賣出金額
		"value9", // 投信_買賣差額
		"value10", // 外資及陸資_買進金額
		"value11", // 外資及陸資_賣出金額
		"value12", // 外資及陸資_買賣差額
	};
	private static final String[] STOCK_TOP3_LEGAL_PERSONS_NET_BUY_OR_SELL_FIELD_TYPE_DEFINITION = new String[]
	{
		"DATE NOT NULL PRIMARY KEY", // 日期
		"BIGINT", // 自營商(自行買賣)_買進金額
		"BIGINT", // 自營商(自行買賣)_賣出金額
		"BIGINT", // 自營商(自行買賣)_買賣差額
		"BIGINT", // 自營商(避險)_買進金額
		"BIGINT", // 自營商(避險)_賣出金額
		"BIGINT", // 自營商(避險)_買賣差額
		"BIGINT", // 投信_買進金額
		"BIGINT", // 投信_賣出金額
		"BIGINT", // 投信_買賣差額
		"BIGINT", // 外資及陸資_買進金額
		"BIGINT", // 外資及陸資_賣出金額
		"BIGINT", // 外資及陸資_買賣差額
	};
	private static final String[] STOCK_MARGIN_TRADING_AND_SHORT_SELLING_FIELD_DEFINITION = new String[]
	{
		"date", // 日期
		"value1", // 融資(交易單位)_買進
		"value2", // 融資(交易單位)_賣出
		"value3", // 融資(交易單位)_現金(券)償還
		"value4", // 融資(交易單位)_前日餘額
		"value5", // 融資(交易單位)_今日餘額
		"value6", // 融券(交易單位)_買進
		"value7", // 融券(交易單位)_賣出
		"value8", // 融券(交易單位)_現金(券)償還
		"value9", // 融券(交易單位)_前日餘額
		"value10", // 融券(交易單位)_今日餘額
		"value11", // 融資金額(仟元)_買進
		"value12", // 融資金額(仟元)_賣出
		"value13", // 融資金額(仟元)_現金(券)償還
		"value14", // 融資金額(仟元)_前日餘額
		"value15", // 融資金額(仟元)_今日餘額
	};
	private static final String[] STOCK_MARGIN_TRADING_AND_SHORT_SELLING_FIELD_TYPE_DEFINITION = new String[]
	{
		"DATE NOT NULL PRIMARY KEY", // 日期
		"INT", // 融資(交易單位)_買進
		"INT", // 融資(交易單位)_賣出
		"INT", // 融資(交易單位)_現金(券)償還
		"INT", // 融資(交易單位)_前日餘額
		"INT", // 融資(交易單位)_今日餘額
		"INT", // 融券(交易單位)_買進
		"INT", // 融券(交易單位)_賣出
		"INT", // 融券(交易單位)_現金(券)償還
		"INT", // 融券(交易單位)_前日餘額
		"INT", // 融券(交易單位)_今日餘額
		"INT", // 融資金額(仟元)_買進
		"INT", // 融資金額(仟元)_賣出
		"INT", // 融資金額(仟元)_現金(券)償還
		"BIGINT", // 融資金額(仟元)_前日餘額
		"BIGINT", // 融資金額(仟元)_今日餘額
	};
	private static final String[] FUTURE_AND_OPTION_TOP3_LEGAL_PERSONS_OPEN_INTEREST_FIELD_DEFINITION = new String[]
	{
		"date", // 日期
		"value1", // 自營商_多方_口數 int",
		"value2", // 自營商_多方_契約金額 int",
		"value3", // 自營商_空方_口數 int",
		"value4", // 自營商_空方_契約金額 int",
		"value5", // 自營商_多空淨額_口數 int",
		"value6", // 自營商_多空淨額_契約金額 int",
		"value7", // 投信_多方_口數 int",
		"value8", // 投信_多方_契約金額 int",
		"value9", // 投信_空方_口數 int",
		"value10", // 投信_空方_契約金額 int",
		"value11", // 投信_多空淨額_口數 int",
		"value12", // 投信_多空淨額_契約金額 int",
		"value13", // 外資_多方_口數 int",
		"value14", // 外資_多方_契約金額 int",
		"value15", // 外資_空方_口數 int",
		"value16", // 外資_空方_契約金額 int",
		"value17", // 外資_多空淨額_口數 int",
		"value18", // 外資_多空淨額_契約金額 int",
	};
	private static final String[] FUTURE_AND_OPTION_TOP3_LEGAL_PERSONS_OPEN_INTEREST_FIELD_TYPE_DEFINITION = new String[]
	{
		"DATE NOT NULL PRIMARY KEY", // 日期
		"INT", // 自營商_多方_口數 int",
		"INT", // 自營商_多方_契約金額 int",
		"INT", // 自營商_空方_口數 int",
		"INT", // 自營商_空方_契約金額 int",
		"INT", // 自營商_多空淨額_口數 int",
		"INT", // 自營商_多空淨額_契約金額 int",
		"INT", // 投信_多方_口數 int",
		"INT", // 投信_多方_契約金額 int",
		"INT", // 投信_空方_口數 int",
		"INT", // 投信_空方_契約金額 int",
		"INT", // 投信_多空淨額_口數 int",
		"INT", // 投信_多空淨額_契約金額 int",
		"INT", // 外資_多方_口數 int",
		"INT", // 外資_多方_契約金額 int",
		"INT", // 外資_空方_口數 int",
		"INT", // 外資_空方_契約金額 int",
		"INT", // 外資_多空淨額_口數 int",
		"INT", // 外資_多空淨額_契約金額 int",
	};
	private static final String[] FUTURE_OR_OPTION_TOP3_LEGAL_PERSONS_OPEN_INTEREST_FIELD_DEFINITION = new String[]
	{
		"date", // 日期
		"value1", // 自營商_多方_口數_期貨 int",
		"value2", // 自營商_多方_口數_選擇權 int",
		"value3", // 自營商_多方_契約金額_期貨 int",
		"value4", // 自營商_多方_契約金額_選擇權 int",
		"value5", // 自營商_空方_口數_期貨 int",
		"value6", // 自營商_空方_口數_選擇權 int",
		"value7", // 自營商_空方_契約金額_期貨 int",
		"value8", // 自營商_空方_契約金額_選擇權 int",
		"value9", // 自營商_多空淨額_口數_期貨 int",
		"value10", // 自營商_多空淨額_口數_選擇權 int",
		"value11", // 自營商_多空淨額_契約金額_期貨 int",
		"value12", // 自營商_多空淨額_契約金額_選擇權 int",
		"value13", // 投信_多方_口數_期貨 int",
		"value14", // 投信_多方_口數_選擇權 int",
		"value15", // 投信_多方_契約金額_期貨 int",
		"value16", // 投信_多方_契約金額_選擇權 int",
		"value17", // 投信_空方_口數_期貨 int",
		"value18", // 投信_空方_口數_選擇權 int",
		"value19", // 投信_空方_契約金額_期貨 int",
		"value20", // 投信_空方_契約金額_選擇權 int",
		"value21", // 投信_多空淨額_口數_期貨 int",
		"value22", // 投信_多空淨額_口數_選擇權 int",
		"value23", // 投信_多空淨額_契約金額_期貨 int",
		"value24", // 投信_多空淨額_契約金額_選擇權 int",
		"value25", // 外資_多方_口數_期貨 int",
		"value26", // 外資_多方_口數_選擇權 int",
		"value27", // 外資_多方_契約金額_期貨 int",
		"value28", // 外資_多方_契約金額_選擇權 int",
		"value29", // 外資_空方_口數_期貨 int",
		"value30", // 外資_空方_口數_選擇權 int",
		"value31", // 外資_空方_契約金額_期貨 int",
		"value32", // 外資_空方_契約金額_選擇權 int",
		"value33", // 外資_多空淨額_口數_期貨 int",
		"value34", // 外資_多空淨額_口數_選擇權 int",
		"value35", // 外資_多空淨額_契約金額_期貨 int",
		"value36", // 外資_多空淨額_契約金額_選擇權 int",
	};
	private static final String[] FUTURE_OR_OPTION_TOP3_LEGAL_PERSONS_OPEN_INTEREST_FIELD_TYPE_DEFINITION = new String[]
	{
		"DATE NOT NULL PRIMARY KEY", // 日期
		"INT", // 自營商_多方_口數_期貨 int",
		"INT", // 自營商_多方_口數_選擇權 int",
		"INT", // 自營商_多方_契約金額_期貨 int",
		"INT", // 自營商_多方_契約金額_選擇權 int",
		"INT", // 自營商_空方_口數_期貨 int",
		"INT", // 自營商_空方_口數_選擇權 int",
		"INT", // 自營商_空方_契約金額_期貨 int",
		"INT", // 自營商_空方_契約金額_選擇權 int",
		"INT", // 自營商_多空淨額_口數_期貨 int",
		"INT", // 自營商_多空淨額_口數_選擇權 int",
		"INT", // 自營商_多空淨額_契約金額_期貨 int",
		"INT", // 自營商_多空淨額_契約金額_選擇權 int",
		"INT", // 投信_多方_口數_期貨 int",
		"INT", // 投信_多方_口數_選擇權 int",
		"INT", // 投信_多方_契約金額_期貨 int",
		"INT", // 投信_多方_契約金額_選擇權 int",
		"INT", // 投信_空方_口數_期貨 int",
		"INT", // 投信_空方_口數_選擇權 int",
		"INT", // 投信_空方_契約金額_期貨 int",
		"INT", // 投信_空方_契約金額_選擇權 int",
		"INT", // 投信_多空淨額_口數_期貨 int",
		"INT", // 投信_多空淨額_口數_選擇權 int",
		"INT", // 投信_多空淨額_契約金額_期貨 int",
		"INT", // 投信_多空淨額_契約金額_選擇權 int",
		"INT", // 外資_多方_口數_期貨 int",
		"INT", // 外資_多方_口數_選擇權 int",
		"INT", // 外資_多方_契約金額_期貨 int",
		"INT", // 外資_多方_契約金額_選擇權 int",
		"INT", // 外資_空方_口數_期貨 int",
		"INT", // 外資_空方_口數_選擇權 int",
		"INT", // 外資_空方_契約金額_期貨 int",
		"INT", // 外資_空方_契約金額_選擇權 int",
		"INT", // 外資_多空淨額_口數_期貨 int",
		"INT", // 外資_多空淨額_口數_選擇權 int",
		"INT", // 外資_多空淨額_契約金額_期貨 int",
		"INT", // 外資_多空淨額_契約金額_選擇權 int",
	};
	private static final String[] OPTION_TOP3_LEGAL_PERSONS_BUY_AND_SELL_OPTION_OPEN_INTEREST_FIELD_DEFINITION = new String[]
	{
		"date", // 日期
		"value1", // 買權_自營商_買方_口數 int",
		"value2", // 買權_自營商_買方_契約金額 int",
		"value3", // 買權_自營商_賣方_口數 int",
		"value4", // 買權_自營商_賣方_契約金額 int",
		"value5", // 買權_自營商_買賣差額_口數 int",
		"value6", // 買權_自營商_買賣差額_契約金額 int",
		"value7", // 買權_投信_買方_口數 int",
		"value8", // 買權_投信_買方_契約金額 int",
		"value9", // 買權_投信_賣方_口數 int",
		"value10", // 買權_投信_賣方_契約金額 int",
		"value11", // 買權_投信_買賣差額_口數 int",
		"value12", // 買權_投信_買賣差額_契約金額 int",
		"value13", // 買權_外資_買方_口數 int",
		"value14", // 買權_外資_買方_契約金額 int",
		"value15", // 買權_外資_賣方_口數 int",
		"value16", // 買權_外資_賣方_契約金額 int",
		"value17", // 買權_外資_買賣差額_口數 int",
		"value18", // 買權_外資_買賣差額_契約金額 int",
		"value19", // 賣權_自營商_買方_口數 int",
		"value20", // 賣權_自營商_買方_契約金額 int",
		"value21", // 賣權_自營商_賣方_口數 int",
		"value22", // 賣權_自營商_賣方_契約金額 int",
		"value23", // 賣權_自營商_買賣差額_口數 int",
		"value24", // 賣權_自營商_買賣差額_契約金額 int",
		"value25", // 賣權_投信_買方_口數 int",
		"value26", // 賣權_投信_買方_契約金額 int",
		"value27", // 賣權_投信_賣方_口數 int",
		"value28", // 賣權_投信_賣方_契約金額 int",
		"value29", // 賣權_投信_買賣差額_口數 int",
		"value30", // 賣權_投信_買賣差額_契約金額 int",
		"value31", // 賣權_外資_買方_口數 int",
		"value32", // 賣權_外資_買方_契約金額 int",
		"value33", // 賣權_外資_賣方_口數 int",
		"value34", // 賣權_外資_賣方_契約金額 int",
		"value35", // 賣權_外資_買賣差額_口數 int",
		"value36", // 賣權_外資_買賣差額_契約金額 int",
	};
	private static final String[] OPTION_TOP3_LEGAL_PERSONS_BUY_AND_SELL_OPTION_OPEN_INTEREST_FIELD_TYPE_DEFINITION = new String[]
	{
		"DATE NOT NULL PRIMARY KEY", // 日期
		"INT", // 買權_自營商_買方_口數 int",
		"INT", // 買權_自營商_買方_契約金額 int",
		"INT", // 買權_自營商_賣方_口數 int",
		"INT", // 買權_自營商_賣方_契約金額 int",
		"INT", // 買權_自營商_買賣差額_口數 int",
		"INT", // 買權_自營商_買賣差額_契約金額 int",
		"INT", // 買權_投信_買方_口數 int",
		"INT", // 買權_投信_買方_契約金額 int",
		"INT", // 買權_投信_賣方_口數 int",
		"INT", // 買權_投信_賣方_契約金額 int",
		"INT", // 買權_投信_買賣差額_口數 int",
		"INT", // 買權_投信_買賣差額_契約金額 int",
		"INT", // 買權_外資_買方_口數 int",
		"INT", // 買權_外資_買方_契約金額 int",
		"INT", // 買權_外資_賣方_口數 int",
		"INT", // 買權_外資_賣方_契約金額 int",
		"INT", // 買權_外資_買賣差額_口數 int",
		"INT", // 買權_外資_買賣差額_契約金額 int",
		"INT", // 賣權_自營商_買方_口數 int",
		"INT", // 賣權_自營商_買方_契約金額 int",
		"INT", // 賣權_自營商_賣方_口數 int",
		"INT", // 賣權_自營商_賣方_契約金額 int",
		"INT", // 賣權_自營商_買賣差額_口數 int",
		"INT", // 賣權_自營商_買賣差額_契約金額 int",
		"INT", // 賣權_投信_買方_口數 int",
		"INT", // 賣權_投信_買方_契約金額 int",
		"INT", // 賣權_投信_賣方_口數 int",
		"INT", // 賣權_投信_賣方_契約金額 int",
		"INT", // 賣權_投信_買賣差額_口數 int",
		"INT", // 賣權_投信_買賣差額_契約金額 int",
		"INT", // 賣權_外資_買方_口數 int",
		"INT", // 賣權_外資_買方_契約金額 int",
		"INT", // 賣權_外資_賣方_口數 int",
		"INT", // 賣權_外資_賣方_契約金額 int",
		"INT", // 賣權_外資_買賣差額_口數 int",
		"INT", // 賣權_外資_買賣差額_契約金額 int",
	};
	private static final String[] OPTION_PUT_CALL_RATIO_FIELD_DEFINITION = new String[]
	{
		"date", // 日期
		"value1", // 賣權成交量
		"value2", // 買權成交量
		"value3", // 買賣權成交量比率%
		"value4", // 賣權未平倉量
		"value5", // 買權未平倉量
		"value6", // 買賣權未平倉量比率%
	};
	private static final String[] OPTION_PUT_CALL_RATIO_FIELD_TYPE_DEFINITION = new String[]
	{
		"DATE NOT NULL PRIMARY KEY", // 日期
		"INT", // 賣權成交量
		"INT", // 買權成交量
		"FLOAT", // 買賣權成交量比率%
		"INT", // 賣權未平倉量
		"INT", // 買權未平倉量
		"FLOAT", // 買賣權未平倉量比率%
	};
	private static final String[] FUTURE_TOP10_DEALERS_AND_LEGAL_PERSONS_FIELD_DEFINITION = new String[]
	{
		"date", // 日期
		"value1", // 臺股期貨_到期月份_買方_前五大交易人合計_部位數
		"value2", // 臺股期貨_到期月份_買方_前五大交易人合計_百分比
		"value3", // 臺股期貨_到期月份_買方_前十大交易人合計_部位數
		"value4", // 臺股期貨_到期月份_買方_前十大交易人合計_百分比
		"value5", // 臺股期貨_到期月份_賣方_前五大交易人合計_部位數
		"value6", // 臺股期貨_到期月份_賣方_前五大交易人合計_百分比
		"value7", // 臺股期貨_到期月份_賣方_前十大交易人合計_部位數
		"value8", // 臺股期貨_到期月份_賣方_前十大交易人合計_百分比
		"value9", // 臺股期貨_到期月份_全市場未沖銷部位數
		"value10", // 臺股期貨_所有契約_買方_前五大交易人合計_部位數
		"value11", // 臺股期貨_所有契約_買方_前五大交易人合計_百分比
		"value12", // 臺股期貨_所有契約_買方_前十大交易人合計_部位數
		"value13", // 臺股期貨_所有契約_買方_前十大交易人合計_百分比
		"value14", // 臺股期貨_所有契約_賣方_前五大交易人合計_部位數
		"value15", // 臺股期貨_所有契約_賣方_前五大交易人合計_百分比
		"value16", // 臺股期貨_所有契約_賣方_前十大交易人合計_部位數
		"value17", // 臺股期貨_所有契約_賣方_前十大交易人合計_百分比
		"value18", // 臺股期貨_所有契約_全市場未沖銷部位數
	};
	private static final String[] FUTURE_TOP10_DEALERS_AND_LEGAL_PERSONS_FIELD_TYPE_DEFINITION = new String[]
	{
		"DATE NOT NULL PRIMARY KEY", // 日期
		"INT", // 臺股期貨_到期月份_買方_前五大交易人合計_部位數
		"FLOAT", // 臺股期貨_到期月份_買方_前五大交易人合計_百分比
		"INT", // 臺股期貨_到期月份_買方_前十大交易人合計_部位數
		"FLOAT", // 臺股期貨_到期月份_買方_前十大交易人合計_百分比
		"INT", // 臺股期貨_到期月份_賣方_前五大交易人合計_部位數
		"FLOAT", // 臺股期貨_到期月份_賣方_前五大交易人合計_百分比
		"INT", // 臺股期貨_到期月份_賣方_前十大交易人合計_部位數
		"FLOAT", // 臺股期貨_到期月份_賣方_前十大交易人合計_百分比
		"INT", // 臺股期貨_到期月份_全市場未沖銷部位數
		"INT", // 臺股期貨_所有契約_買方_前五大交易人合計_部位數
		"FLOAT", // 臺股期貨_所有契約_買方_前五大交易人合計_百分比
		"INT", // 臺股期貨_所有契約_買方_前十大交易人合計_部位數
		"FLOAT", // 臺股期貨_所有契約_買方_前十大交易人合計_百分比
		"INT", // 臺股期貨_所有契約_賣方_前五大交易人合計_部位數
		"FLOAT", // 臺股期貨_所有契約_賣方_前五大交易人合計_百分比
		"INT", // 臺股期貨_所有契約_賣方_前十大交易人合計_部位數
		"FLOAT", // 臺股期貨_所有契約_賣方_前十大交易人合計_百分比
		"INT", // 臺股期貨_所有契約_全市場未沖銷部位數
	};
	public static final String[] FINANCE_DATA_SQL_FIELD_LIST = new String[]
	{
		transform_array_to_sql_string(merge_string_array_element(STOCK_EXCHANGE_AND_VALUE_FIELD_DEFINITION, STOCK_EXCHANGE_AND_VALUE_FIELD_TYPE_DEFINITION)),
		transform_array_to_sql_string(merge_string_array_element(STOCK_TOP3_LEGAL_PERSONS_NET_BUY_OR_SELL_FIELD_DEFINITION, STOCK_TOP3_LEGAL_PERSONS_NET_BUY_OR_SELL_FIELD_TYPE_DEFINITION)),
		transform_array_to_sql_string(merge_string_array_element(STOCK_MARGIN_TRADING_AND_SHORT_SELLING_FIELD_DEFINITION, STOCK_MARGIN_TRADING_AND_SHORT_SELLING_FIELD_TYPE_DEFINITION)),
		transform_array_to_sql_string(merge_string_array_element(FUTURE_AND_OPTION_TOP3_LEGAL_PERSONS_OPEN_INTEREST_FIELD_DEFINITION, FUTURE_AND_OPTION_TOP3_LEGAL_PERSONS_OPEN_INTEREST_FIELD_TYPE_DEFINITION)),
		transform_array_to_sql_string(merge_string_array_element(FUTURE_OR_OPTION_TOP3_LEGAL_PERSONS_OPEN_INTEREST_FIELD_DEFINITION, FUTURE_OR_OPTION_TOP3_LEGAL_PERSONS_OPEN_INTEREST_FIELD_TYPE_DEFINITION)),
		transform_array_to_sql_string(merge_string_array_element(OPTION_TOP3_LEGAL_PERSONS_BUY_AND_SELL_OPTION_OPEN_INTEREST_FIELD_DEFINITION, OPTION_TOP3_LEGAL_PERSONS_BUY_AND_SELL_OPTION_OPEN_INTEREST_FIELD_TYPE_DEFINITION)),
		transform_array_to_sql_string(merge_string_array_element(OPTION_PUT_CALL_RATIO_FIELD_DEFINITION, OPTION_PUT_CALL_RATIO_FIELD_TYPE_DEFINITION)),
		transform_array_to_sql_string(merge_string_array_element(FUTURE_TOP10_DEALERS_AND_LEGAL_PERSONS_FIELD_DEFINITION, FUTURE_TOP10_DEALERS_AND_LEGAL_PERSONS_FIELD_TYPE_DEFINITION))
	};
	public static final String[][] FINANCE_DATA_SQL_FIELD_DEFINITION_LIST = new String[][]
	{
		STOCK_EXCHANGE_AND_VALUE_FIELD_DEFINITION,
		STOCK_TOP3_LEGAL_PERSONS_NET_BUY_OR_SELL_FIELD_DEFINITION,
		STOCK_MARGIN_TRADING_AND_SHORT_SELLING_FIELD_DEFINITION,
		FUTURE_AND_OPTION_TOP3_LEGAL_PERSONS_OPEN_INTEREST_FIELD_DEFINITION,
		FUTURE_OR_OPTION_TOP3_LEGAL_PERSONS_OPEN_INTEREST_FIELD_DEFINITION,
		OPTION_TOP3_LEGAL_PERSONS_BUY_AND_SELL_OPTION_OPEN_INTEREST_FIELD_DEFINITION,
		OPTION_PUT_CALL_RATIO_FIELD_DEFINITION,
		FUTURE_TOP10_DEALERS_AND_LEGAL_PERSONS_FIELD_DEFINITION
	};
	public static final String[][] FINANCE_DATA_SQL_FIELD_TYPE_DEFINITION_LIST = new String[][]
	{
		STOCK_EXCHANGE_AND_VALUE_FIELD_TYPE_DEFINITION,
		STOCK_TOP3_LEGAL_PERSONS_NET_BUY_OR_SELL_FIELD_TYPE_DEFINITION,
		STOCK_MARGIN_TRADING_AND_SHORT_SELLING_FIELD_TYPE_DEFINITION,
		FUTURE_AND_OPTION_TOP3_LEGAL_PERSONS_OPEN_INTEREST_FIELD_TYPE_DEFINITION,
		FUTURE_OR_OPTION_TOP3_LEGAL_PERSONS_OPEN_INTEREST_FIELD_TYPE_DEFINITION,
		OPTION_TOP3_LEGAL_PERSONS_BUY_AND_SELL_OPTION_OPEN_INTEREST_FIELD_TYPE_DEFINITION,
		OPTION_PUT_CALL_RATIO_FIELD_TYPE_DEFINITION,
		FUTURE_TOP10_DEALERS_AND_LEGAL_PERSONS_FIELD_TYPE_DEFINITION
	};
//	public static final short NOTIFY_GET_DATA = 0;
//	public static final int EACH_UPDATE_DATA_AMOUNT = 20;

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

	private static String[] merge_string_array_element(String[] string_arr1, String[] string_arr2)
	{
		if (string_arr1.length != string_arr2.length)
		{
			assert false : String.format("The length of string_arr1 and string_arr2 are NOT equal: %d, %d", string_arr1.length, string_arr2.length);
			return null;
		}
		int string_arr_len = string_arr1.length;
		String[] string_arr = new String[string_arr_len];
		for (int index = 0 ; index < string_arr_len ; index++)
			string_arr[index] = String.format("%s %s", string_arr1[index], string_arr2[index]);
		return string_arr;
	}

	public static String transform_array_to_sql_string(String[] string_arr)
	{
		String sql_string = null;
		for(String string : string_arr)
		{
			String encoded_string = string;//new String(string.getBytes("UTF-16"), "Big5");
			if (sql_string == null)
				sql_string = encoded_string;
			else
				sql_string += String.format(",%s", encoded_string);
		}
		return sql_string;
	}

	public static 	java.util.Date get_date(String date_str) throws ParseException
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); // your template here
		java.util.Date date = formatter.parse(date_str);
		return date;
	}

	public static final String get_month_str(java.util.Date date)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		String time_month = String.format("%04d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);
		return time_month;
	}

	public static final String get_date_str(java.util.Date date)
	{
	    Calendar cal = Calendar.getInstance();
	    cal.setTime(date);
		String time_date = String.format("%04d-%02d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE));
		return time_date;
	}

	public static final String get_time_month_today()
	{
		return get_month_str(new java.util.Date());
	}

	public static final String get_time_date_today()
	{
		return get_date_str(new java.util.Date());
	}

	private static Matcher get_time_value_matcher(String time_str, String search_pattern)
	{
// Time Format: yyyy-mm; Ex: 2015-09
// Time Format: yyyy-MM-dd; Ex: 2015-09-04		
		Pattern pattern = Pattern.compile(search_pattern);
		Matcher matcher = pattern.matcher(time_str);
		if (!matcher.find())
		{
			FinanceRecorderCmnDef.format_error("Incorrect time format: %s", time_str);
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
			FinanceRecorderCmnDef.format_error("Incorrect time format: %s", time_str);
			return null;
		}
		int year = Integer.valueOf(month_matcher.group(1));
		int month = Integer.valueOf(month_matcher.group(2));

		return new int[]{year, month};
	}

	public static int[] get_start_and_end_month_value_range(FinanceRecorderCmnDef.TimeRangeCfg time_range_cfg)
	{			
		int[] month_value_start = get_month_value(time_range_cfg.time_start_str);
		if (month_value_start == null)
			return null;
		int[] month_value_end = get_month_value(time_range_cfg.time_end_str);
		if (month_value_end == null)
			return null;

		return new int[]{month_value_start[0], month_value_start[1], month_value_end[0], month_value_end[1]};
	}

	public static short copy_file(String src_filepath, String dst_filepath)
	{
		Path src_filepath_obj = Paths.get(src_filepath);
		Path dst_filepath_obj = Paths.get(dst_filepath);
		try
		{
			CopyOption[] options = new CopyOption[]{StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES};
			FinanceRecorderCmnDef.format_error("Copy File; From: %s, To: %s", src_filepath, dst_filepath);
			Files.copy(src_filepath_obj, dst_filepath_obj, options);
		}
		catch (IOException e)
		{
			FinanceRecorderCmnDef.format_error("Fail to copy file, due to: %s", e.toString());
			return FinanceRecorderCmnDef.RET_FAILURE_IO_OPERATION;
		}
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}
	
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Interface
	public interface FinanceObserverInf
	{
		public short notify(short type);
	}
}

