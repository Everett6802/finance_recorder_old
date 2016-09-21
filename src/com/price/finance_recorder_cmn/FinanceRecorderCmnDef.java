package com.price.finance_recorder_cmn;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
//import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
//import java.util.TreeMap;
import java.util.regex.*;
import java.text.*;

public class FinanceRecorderCmnDef 
{
	private static FinanceRecorderLogger finance_recorder_logger = FinanceRecorderLogger.get_instance();
	public static final boolean IS_FINANCE_MARKET_MODE = is_market_mode();
	public static final boolean IS_FINANCE_STOCK_MODE = !IS_FINANCE_MARKET_MODE;

	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Return values
	public static final short RET_SUCCESS = 0;

	public static final short RET_WARN_BASE = 0x1;
	public static final short RET_WARN_INDEX_DUPLICATE = RET_WARN_BASE + 1;
	public static final short RET_WARN_INDEX_IGNORE = RET_WARN_BASE + 2;

	public static final short RET_FAILURE_BASE = 0x100;
	public static final short RET_FAILURE_UNKNOWN = RET_FAILURE_BASE + 1;
	public static final short RET_FAILURE_INVALID_ARGUMENT = RET_FAILURE_BASE + 2;
	public static final short RET_FAILURE_INVALID_POINTER = RET_FAILURE_BASE + 3;
	public static final short RET_FAILURE_INSUFFICIENT_MEMORY = RET_FAILURE_BASE + 4;
	public static final short RET_FAILURE_INCORRECT_OPERATION = RET_FAILURE_BASE + 5;
	public static final short RET_FAILURE_NOT_FOUND = RET_FAILURE_BASE + 6;
	public static final short RET_FAILURE_INCORRECT_CONFIG = RET_FAILURE_BASE + 7;
	public static final short RET_FAILURE_HANDLE_THREAD = RET_FAILURE_BASE + 8;
	public static final short RET_FAILURE_INCORRECT_PATH = RET_FAILURE_BASE + 9;
	public static final short RET_FAILURE_IO_OPERATION = RET_FAILURE_BASE + 10;
	public static final short RET_FAILURE_UNEXPECTED_VALUE = RET_FAILURE_BASE + 11;

	public static final short RET_FAILURE_MYSQL_BASE = 0x200;
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

	private static final String[] WarnRetDescription = new String[] 
	{
		"Warning Base", 
		"Warning Index Duplicate", 
		"Warning Index Ignore" 
	};

	private static final String[] ErrorRetDescription = new String[] 
	{
		"Failure Base", 
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

	private static final String[] SQLErrorRetDescription = new String[] 
	{
		"SQL Failure Base", 
		"SQL Failure Common",
		"SQL Failure Unknown Database", 
		"SQL Failure No Driver",
		"SQL Failure Execute Command",
		"SQL Failure Database Already Exist",
		"SQL Failure Data Not Consistent" 
	};

	public static String GetErrorDescription(short error_code) 
	{
		if (error_code >= RET_FAILURE_MYSQL_BASE)
			return SQLErrorRetDescription[error_code - RET_FAILURE_MYSQL_BASE];
		else if (error_code >= RET_FAILURE_BASE && error_code < RET_FAILURE_MYSQL_BASE)
			return ErrorRetDescription[error_code - RET_FAILURE_BASE];
		else if (error_code >= RET_WARN_BASE)
			return WarnRetDescription[error_code - RET_WARN_BASE];
		else
			return "Success";
	}

	public static final int DEF_START_YEAR = 2000;
	public static final int DEF_END_YEAR = 2100;
	public static final int DEF_START_QUARTER = 1;
	public static final int DEF_END_QUARTER = 4;
	public static final int DEF_START_MONTH = 1;
	public static final int DEF_END_MONTH = 12;
	public static final int DEF_START_DAY = 1;

	public static final String CSV_ROOT_FOLDERPATH = "/var/tmp/finance";
	public static final String COPY_BACKUP_FOLDERPATH = "/var/www/finance";
	public static final String CSV_MARKET_FOLDERNAME = "market";
	public static final String CSV_STOCK_FOLDERNAME = "stock";
	public static final String CSV_FOLDERPATH = String.format("%s/%s", CSV_ROOT_FOLDERPATH, (IS_FINANCE_MARKET_MODE ? CSV_MARKET_FOLDERNAME : CSV_STOCK_FOLDERNAME));
	public static final String SQL_MARKET_DATABASE_NAME = "market";
	public static final String SQL_STOCK_DATABASE_NAME = "stock";
	public static final String RESULT_FOLDERNAME = "result";
	public static final String CONF_FOLDERNAME = "conf";
	public static final String BACKUP_FOLDERNAME = ".backup";
	public static final String WORKDAY_CANLENDAR_CONF_FILENAME = ".workday_canlendar.conf";
	public static final String DATABASE_TIME_RANGE_CONF_FILENAME = ".database_time_range.conf";
	public static final String COMPANY_PROFILE_CONF_FILENAME = ".company_profile.conf";
	public static final String COMPANY_GROUP_CONF_FILENAME = ".company_group.conf";
	public static final String MARKET_STOCK_SWITCH_CONF_FILENAME = "market_stock_switch.conf";
	public static final String FINANCE_RECORDER_CONF_FILENAME = "finance_recorder.conf";
	public static final String BACKUP_FILENAME = "backup.conf";

	public static final String FINANCE_DATE_REGEX_STRING_FORMAT = "([\\d]{4})-([\\d]{1,2})-([\\d]{1,2})";
	public static final String FINANCE_MONTH_REGEX_STRING_FORMAT = "([\\d]{4})-([\\d]{1,2})";
	public static final String FINANCE_QUARTER_REGEX_STRING_FORMAT = "([\\d]{4})[Qq]([\\d]{1})";
	public static final String[] FINANCE_TIME_REGEX_STRING_FORMAT_ARRAY = new String[] {FINANCE_DATE_REGEX_STRING_FORMAT, FINANCE_MONTH_REGEX_STRING_FORMAT, FINANCE_QUARTER_REGEX_STRING_FORMAT };
	
	public static final String CSV_FILE_ROOT_FOLDERPATH = "/var/tmp/finance";
	public static final String COMMA_DATA_SPLIT = ",";
	public static final String SPACE_DATA_SPLIT = " ";
	public static final String DAILY_FINANCE_FILENAME_FORMAT = "daily_finance%04d%02d%02d";
	public static final String DAILY_FINANCE_EMAIL_TITLE_FORMAT = "daily_finance%04d%02d%02d";
	public static final String CONF_ENTRY_IGNORE_FLAG = "#";
	public static final int MAX_CONCURRENT_THREAD = 4;
	public static final int WRITE_SQL_MAX_MONTH_RANGE_IN_THREAD = 3;

	public static final int BACKUP_SQL_MAX_MONTH_RANGE_IN_THREAD = 2;
	public static final String DATABASE_TIME_RANGE_FILE_DST_PROJECT_NAME = "finance_analyzer";
	public static final int FINANCE_SOURCE_SIZE = FinanceSourceType.values().length;

	public static final int SOURCE_KEY_SOURCE_TYPE_INDEX_BIT_OFFSET = 0;
	public static final int SOURCE_KEY_COMPANY_CODE_NUMBER_BIT_OFFSET = 8;
	public static final int SOURCE_KEY_COMPANY_GROUP_NUMBER_BIT_OFFSET = 24;
	public static final int SOURCE_KEY_SOURCE_TYPE_INDEX_MASK = 0xFF << SOURCE_KEY_SOURCE_TYPE_INDEX_BIT_OFFSET;
	public static final int SOURCE_KEY_COMPANY_CODE_NUMBER_MASK = 0xFFFF << SOURCE_KEY_COMPANY_CODE_NUMBER_BIT_OFFSET;
	public static final int SOURCE_KEY_COMPANY_GROUP_NUMBER_MASK = 0xFF << SOURCE_KEY_COMPANY_GROUP_NUMBER_BIT_OFFSET;
	public static final int NO_SOURCE_TYPE_MARKET_SOURCE_KEY_VALUE = 0;

	public static final String MYSQL_TABLE_NAME_BASE = "year";
	public static final String MYSQL_DATE_FILED_NAME = "date";
	public static final String MYSQL_FILED_NAME_BASE = "value";

	public static final int DEF_MIN_DATE_STRING_LENGTH = 8;
	public static final int DEF_MAX_DATE_STRING_LENGTH = 10;
	public static final int DEF_MIN_MONTH_STRING_LENGTH = 5;
	public static final int DEF_MAX_MONTH_STRING_LENGTH = 7;
	public static final int DEF_MIN_QUARTER_STRING_LENGTH = 4;
	public static final int DEF_MAX_QUARTER_STRING_LENGTH = 6;

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// enumeration
	public static enum FinanceAnalysisMode 
	{
		FinanceAnalysis_Market(0), 
		FinanceAnalysis_Stock(1);

		private int value = 0;

		private FinanceAnalysisMode(int value) 
		{
			this.value = value;
		}

		public static FinanceAnalysisMode valueOf(int value)
		{
			switch (value) 
			{
			case 0:
				return FinanceAnalysis_Market;
			case 1:
				return FinanceAnalysis_Stock;
			default:
				return null;
			}
		}

		public int value() 
		{
			return this.value;
		}
	};

	public static enum FinanceSourceType 
	{
		FinanceSource_Unknown(-1),
		// //////////////////////////////////////////////////////////////////////////////////////////////
		// Market data source
		FinanceSource_MarketStart(0), // Keep in mind to update the value at the right time
		FinanceSource_StockExchangeAndVolume(0), 
		FinanceSource_StockTop3LegalPersonsNetBuyOrSell(1), 
		FinanceSource_StockMarginTradingAndShortSelling(2), 
		FinanceSource_FutureAndOptionTop3LegalPersonsOpenInterest(3), 
		FinanceSource_FutureOrOptionTop3LegalPersonsOpenInterest(4), 
		FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest(5), 
		FinanceSource_OptionPutCallRatio(6), 
		FinanceSource_FutureTop10DealersAndLegalPersons(7), 
		FinanceSource_MarketEnd(8), // Keep in mind to update the value at the right time
		// //////////////////////////////////////////////////////////////////////////////////////////////
		// Stock data source
		FinanceSource_StockStart(8), // Keep in mind to update the value at the right time
		FinanceSource_StockEnd(8); // Keep in mind to update the value at the right time

		private int value = 0;

		private FinanceSourceType(int value) 
		{
			this.value = value;
		}

		public static FinanceSourceType valueOf(int value) 
		{
			switch (value) 
			{
			case 0:
				return FinanceSource_StockExchangeAndVolume;
			case 1:
				return FinanceSource_StockTop3LegalPersonsNetBuyOrSell;
			case 2:
				return FinanceSource_StockMarginTradingAndShortSelling;
			case 3:
				return FinanceSource_FutureAndOptionTop3LegalPersonsOpenInterest;
			case 4:
				return FinanceSource_FutureOrOptionTop3LegalPersonsOpenInterest;
			case 5:
				return FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest;
			case 6:
				return FinanceSource_OptionPutCallRatio;
			case 7:
				return FinanceSource_FutureTop10DealersAndLegalPersons;
			default:
				return null;
			}
		}

		public int value() 
		{
			return this.value;
		}

		public static boolean is_market_source_type(int source_type_index) 
		{
			return (source_type_index >= FinanceSource_MarketStart.value && source_type_index < FinanceSource_MarketEnd.value);
		}

		public static boolean is_stock_source_type(int source_type_index) 
		{
			return (source_type_index >= FinanceSource_StockStart.value && source_type_index < FinanceSource_StockEnd.value);
		}
	};

	public static enum FinanceFieldType 
	{
		FinanceField_INT(0), 
		FinanceField_LONG(1), 
		FinanceField_FLOAT(2), 
		FinanceField_DATE(3);

		private int value = 0;

		private FinanceFieldType(int value) 
		{
			this.value = value;
		}

		public static FinanceFieldType valueOf(int value) 
		{
			switch (value) 
			{
			case 0:
				return FinanceField_INT;
			case 1:
				return FinanceField_LONG;
			case 2:
				return FinanceField_FLOAT;
			case 3:
				return FinanceField_DATE;
			default:
				return null;
			}
		}

		public int value() 
		{
			return this.value;
		}
	};

	public static enum FinanceTimeUnit
	{
		FinanceTime_Date(0), 
		FinanceTime_Month(1), 
		FinanceTime_Quarter(2), 
		FinanceTime_Undefined(3);

		private int value = 0;

		private FinanceTimeUnit(int value) 
		{
			this.value = value;
		}

		public static FinanceTimeUnit valueOf(int value) 
		{
			switch (value) 
			{
			case 0:
				return FinanceTime_Date;
			case 1:
				return FinanceTime_Month;
			case 2:
				return FinanceTime_Quarter;
			case 3:
				return FinanceTime_Undefined;
			default:
				return null;
			}
		}

		public int value() 
		{
			return this.value;
		}
	};

	public static enum FinanceTimeRangeType
	{
		FinanceTimeRange_Between(0), 
		FinanceTimeRange_Greater(1),
		FinanceTimeRange_GreaterEqual(2),
		FinanceTimeRange_Less(3),
		FinanceTimeRange_LessEqual(4),
		FinanceTimeRange_Undefined(5);

		private int value = 0;

		private FinanceTimeRangeType(int value) 
		{
			this.value = value;
		}

		public static FinanceTimeRangeType valueOf(int value)
		{
			switch (value) 
			{
			case 0:
				return FinanceTimeRange_Between;
			case 1:
				return FinanceTimeRange_Greater;
			case 2:
				return FinanceTimeRange_GreaterEqual;
			case 3:
				return FinanceTimeRange_Less;
			case 4:
				return FinanceTimeRange_LessEqual;
			default:
				return null;
			}
		}

		public int value() 
		{
			return this.value;
		}
		public boolean is_time_start_exist()
		{
			switch (value) 
			{
			case 0:
			case 1:
			case 2:
				return true;
			}
			return false;
		}
		public boolean is_time_end_exist()
		{
			switch (value) 
			{
			case 0:
			case 3:
			case 4:
				return true;
			}
			return false;
		}
		public boolean is_time_range_exist()
		{
			switch (value) 
			{
			case 0:
				return true;
			}
			return false;
		}
	};

	public static enum DatabaseNotExistIngoreType {DatabaseNotExistIngore_Yes, DatabaseNotExistIngore_No,};

	public static enum DatabaseCreateThreadType {DatabaseCreateThread_Single, DatabaseCreateThread_Multiple,};

	public static enum DatabaseEnableBatchType {DatabaseEnableBatch_Yes, DatabaseEnableBatch_No,};

	public static enum ResultSetDataUnit {ResultSetDataUnit_NoSourceType, ResultSetDataUnit_SourceType};

	public static final String[] FINANCE_DATA_NAME_LIST = new String[] 
	{
		"stock_exchange_and_volume",
		"stock_top3_legal_persons_net_buy_or_sell",
		"stock_margin_trading_and_short_selling",
		"future_and_option_top3_legal_persons_open_interest",
		"future_or_option_top3_legal_persons_open_interest",
		"option_top3_legal_persons_buy_and_sell_option_open_interest",
		"option_put_call_ratio", "future_top10_dealers_and_legal_persons" 
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
	public static final String[] FINANCE_DATA_SQL_FIELD_LIST = new String[] 
	{
		transform_array_to_sql_string(merge_string_array_element(FinanceRecorderCmnDefMarketDatabase.STOCK_EXCHANGE_AND_VALUE_FIELD_DEFINITION, FinanceRecorderCmnDefMarketDatabase.STOCK_EXCHANGE_AND_VALUE_FIELD_TYPE_DEFINITION)),
		transform_array_to_sql_string(merge_string_array_element(FinanceRecorderCmnDefMarketDatabase.STOCK_TOP3_LEGAL_PERSONS_NET_BUY_OR_SELL_FIELD_DEFINITION, FinanceRecorderCmnDefMarketDatabase.STOCK_TOP3_LEGAL_PERSONS_NET_BUY_OR_SELL_FIELD_TYPE_DEFINITION)),
		transform_array_to_sql_string(merge_string_array_element(FinanceRecorderCmnDefMarketDatabase.STOCK_MARGIN_TRADING_AND_SHORT_SELLING_FIELD_DEFINITION, FinanceRecorderCmnDefMarketDatabase.STOCK_MARGIN_TRADING_AND_SHORT_SELLING_FIELD_TYPE_DEFINITION)),
		transform_array_to_sql_string(merge_string_array_element(FinanceRecorderCmnDefMarketDatabase.FUTURE_AND_OPTION_TOP3_LEGAL_PERSONS_OPEN_INTEREST_FIELD_DEFINITION, FinanceRecorderCmnDefMarketDatabase.FUTURE_AND_OPTION_TOP3_LEGAL_PERSONS_OPEN_INTEREST_FIELD_TYPE_DEFINITION)),
		transform_array_to_sql_string(merge_string_array_element(FinanceRecorderCmnDefMarketDatabase.FUTURE_OR_OPTION_TOP3_LEGAL_PERSONS_OPEN_INTEREST_FIELD_DEFINITION, FinanceRecorderCmnDefMarketDatabase.FUTURE_OR_OPTION_TOP3_LEGAL_PERSONS_OPEN_INTEREST_FIELD_TYPE_DEFINITION)),
		transform_array_to_sql_string(merge_string_array_element(FinanceRecorderCmnDefMarketDatabase.OPTION_TOP3_LEGAL_PERSONS_BUY_AND_SELL_OPTION_OPEN_INTEREST_FIELD_DEFINITION, FinanceRecorderCmnDefMarketDatabase.OPTION_TOP3_LEGAL_PERSONS_BUY_AND_SELL_OPTION_OPEN_INTEREST_FIELD_TYPE_DEFINITION)),
		transform_array_to_sql_string(merge_string_array_element(FinanceRecorderCmnDefMarketDatabase.OPTION_PUT_CALL_RATIO_FIELD_DEFINITION, FinanceRecorderCmnDefMarketDatabase.OPTION_PUT_CALL_RATIO_FIELD_TYPE_DEFINITION)),
		transform_array_to_sql_string(merge_string_array_element(FinanceRecorderCmnDefMarketDatabase.FUTURE_TOP10_DEALERS_AND_LEGAL_PERSONS_FIELD_DEFINITION, FinanceRecorderCmnDefMarketDatabase.FUTURE_TOP10_DEALERS_AND_LEGAL_PERSONS_FIELD_TYPE_DEFINITION)) 
	};
	public static final String[][] FINANCE_DATA_SQL_FIELD_DEFINITION_LIST = new String[][] 
	{
		FinanceRecorderCmnDefMarketDatabase.STOCK_EXCHANGE_AND_VALUE_FIELD_DEFINITION,
		FinanceRecorderCmnDefMarketDatabase.STOCK_TOP3_LEGAL_PERSONS_NET_BUY_OR_SELL_FIELD_DEFINITION,
		FinanceRecorderCmnDefMarketDatabase.STOCK_MARGIN_TRADING_AND_SHORT_SELLING_FIELD_DEFINITION,
		FinanceRecorderCmnDefMarketDatabase.FUTURE_AND_OPTION_TOP3_LEGAL_PERSONS_OPEN_INTEREST_FIELD_DEFINITION,
		FinanceRecorderCmnDefMarketDatabase.FUTURE_OR_OPTION_TOP3_LEGAL_PERSONS_OPEN_INTEREST_FIELD_DEFINITION,
		FinanceRecorderCmnDefMarketDatabase.OPTION_TOP3_LEGAL_PERSONS_BUY_AND_SELL_OPTION_OPEN_INTEREST_FIELD_DEFINITION,
		FinanceRecorderCmnDefMarketDatabase.OPTION_PUT_CALL_RATIO_FIELD_DEFINITION,
		FinanceRecorderCmnDefMarketDatabase.FUTURE_TOP10_DEALERS_AND_LEGAL_PERSONS_FIELD_DEFINITION 
	};
	public static final String[][] FINANCE_DATA_SQL_FIELD_TYPE_DEFINITION_LIST = new String[][] 
	{
		FinanceRecorderCmnDefMarketDatabase.STOCK_EXCHANGE_AND_VALUE_FIELD_TYPE_DEFINITION,
		FinanceRecorderCmnDefMarketDatabase.STOCK_TOP3_LEGAL_PERSONS_NET_BUY_OR_SELL_FIELD_TYPE_DEFINITION,
		FinanceRecorderCmnDefMarketDatabase.STOCK_MARGIN_TRADING_AND_SHORT_SELLING_FIELD_TYPE_DEFINITION,
		FinanceRecorderCmnDefMarketDatabase.FUTURE_AND_OPTION_TOP3_LEGAL_PERSONS_OPEN_INTEREST_FIELD_TYPE_DEFINITION,
		FinanceRecorderCmnDefMarketDatabase.FUTURE_OR_OPTION_TOP3_LEGAL_PERSONS_OPEN_INTEREST_FIELD_TYPE_DEFINITION,
		FinanceRecorderCmnDefMarketDatabase.OPTION_TOP3_LEGAL_PERSONS_BUY_AND_SELL_OPTION_OPEN_INTEREST_FIELD_TYPE_DEFINITION,
		FinanceRecorderCmnDefMarketDatabase.OPTION_PUT_CALL_RATIO_FIELD_TYPE_DEFINITION,
		FinanceRecorderCmnDefMarketDatabase.FUTURE_TOP10_DEALERS_AND_LEGAL_PERSONS_FIELD_TYPE_DEFINITION 
		};
	public static final FinanceFieldType[][] FINANCE_DATABASE_FIELD_TYPE_LIST = new FinanceFieldType[][] 
	{
		TransformFieldTypeString2Enum(FinanceRecorderCmnDefMarketDatabase.STOCK_EXCHANGE_AND_VALUE_FIELD_TYPE_DEFINITION),
		TransformFieldTypeString2Enum(FinanceRecorderCmnDefMarketDatabase.STOCK_TOP3_LEGAL_PERSONS_NET_BUY_OR_SELL_FIELD_TYPE_DEFINITION),
		TransformFieldTypeString2Enum(FinanceRecorderCmnDefMarketDatabase.STOCK_MARGIN_TRADING_AND_SHORT_SELLING_FIELD_TYPE_DEFINITION),
		TransformFieldTypeString2Enum(FinanceRecorderCmnDefMarketDatabase.FUTURE_AND_OPTION_TOP3_LEGAL_PERSONS_OPEN_INTEREST_FIELD_TYPE_DEFINITION),
		TransformFieldTypeString2Enum(FinanceRecorderCmnDefMarketDatabase.FUTURE_OR_OPTION_TOP3_LEGAL_PERSONS_OPEN_INTEREST_FIELD_TYPE_DEFINITION),
		TransformFieldTypeString2Enum(FinanceRecorderCmnDefMarketDatabase.OPTION_TOP3_LEGAL_PERSONS_BUY_AND_SELL_OPTION_OPEN_INTEREST_FIELD_TYPE_DEFINITION),
		TransformFieldTypeString2Enum(FinanceRecorderCmnDefMarketDatabase.OPTION_PUT_CALL_RATIO_FIELD_TYPE_DEFINITION),
		TransformFieldTypeString2Enum(FinanceRecorderCmnDefMarketDatabase.FUTURE_TOP10_DEALERS_AND_LEGAL_PERSONS_FIELD_TYPE_DEFINITION) 
	};
	public static final int FINANCE_DATABASE_FIELD_AMOUNT_LIST[] = 
	{
		FinanceRecorderCmnDefMarketDatabase.STOCK_EXCHANGE_AND_VALUE_FIELD_TYPE_DEFINITION.length,
		FinanceRecorderCmnDefMarketDatabase.STOCK_TOP3_LEGAL_PERSONS_NET_BUY_OR_SELL_FIELD_TYPE_DEFINITION.length,
		FinanceRecorderCmnDefMarketDatabase.STOCK_MARGIN_TRADING_AND_SHORT_SELLING_FIELD_TYPE_DEFINITION.length,
		FinanceRecorderCmnDefMarketDatabase.FUTURE_AND_OPTION_TOP3_LEGAL_PERSONS_OPEN_INTEREST_FIELD_TYPE_DEFINITION.length,
		FinanceRecorderCmnDefMarketDatabase.FUTURE_OR_OPTION_TOP3_LEGAL_PERSONS_OPEN_INTEREST_FIELD_TYPE_DEFINITION.length,
		FinanceRecorderCmnDefMarketDatabase.OPTION_TOP3_LEGAL_PERSONS_BUY_AND_SELL_OPTION_OPEN_INTEREST_FIELD_TYPE_DEFINITION.length,
		FinanceRecorderCmnDefMarketDatabase.OPTION_PUT_CALL_RATIO_FIELD_TYPE_DEFINITION.length,
		FinanceRecorderCmnDefMarketDatabase.FUTURE_TOP10_DEALERS_AND_LEGAL_PERSONS_FIELD_TYPE_DEFINITION.length 
	};

	// Setter and Getter
	// Allow to assign the variable only once
	private static boolean set_show_console = false;
	private static boolean show_console = false;

	public static void enable_show_console(boolean show) 
	{
		if (!set_show_console) 
		{
			show_console = show;
			set_show_console = true;
		} 
		else
		{
			warn("The show_console variable has already been Set");
		}
	}
	public static boolean is_show_console() {return show_console;}

	public static void wait_for_logging() {finance_recorder_logger.deinitialize();}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Functions

	private static FinanceAnalysisMode get_finance_analysis_mode() 
	{
		FinanceAnalysisMode mode = null;
		String errmsg;
		//
		// // Open the file
		// // short ret = RET_SUCCESS;
		// BufferedReader reader = null;
		// String conf_filepath = String.format("%s/%s/%s", get_current_path(),
		// CONF_FOLDERNAME, MARKET_STOCK_SWITCH_CONF_FILENAME);
		// format_debug("Try to parse the configuration in %s", conf_filepath);
		// // Check the file exists or not
		// File fp = new File(conf_filepath);
		// if (!fp.exists())
		// {
		// errmsg = String.format("The configration file[%s] does NOT exist",
		// conf_filepath);
		// throw new RuntimeException(errmsg);
		// }
		// String buf = null;
		// // Try to parse the content of the config file
		// try
		// {
		// reader = new BufferedReader(new FileReader(fp));
		// buf = reader.readLine();
		// }
		// catch (IOException ex)
		// {
		// errmsg = String.format("Error occur due to %s", ex.toString());
		// throw new RuntimeException(errmsg);
		// }
		// finally
		// {
		// // Close the file
		// if (reader != null)
		// {
		// try {reader.close();}
		// catch (IOException e){}// nothing to do here except log the exception
		// }
		// }
		// Read the data from the config file
		LinkedList<String> config_line_list = new LinkedList<String>();
		short res = get_config_file_lines(MARKET_STOCK_SWITCH_CONF_FILENAME, config_line_list);
		if (CheckFailure(res)) 
		{
			errmsg = String.format("Fail to parse the config file: %s, due to: %s", MARKET_STOCK_SWITCH_CONF_FILENAME, GetErrorDescription(res));
			throw new RuntimeException(errmsg);
		}
		if (config_line_list.size() != 1) 
		{
			errmsg = String.format("Incorrect content in the config file: %s", MARKET_STOCK_SWITCH_CONF_FILENAME);
			throw new RuntimeException(errmsg);
		}

		mode = FinanceAnalysisMode.valueOf(Integer.parseInt(config_line_list.get(0)));
		if (mode == null) 
		{
			errmsg = String.format("Unknown finance analysis mode: %s", config_line_list.get(0));
			throw new RuntimeException(errmsg);
		}
		return mode;
	}

	private static boolean is_market_mode() 
	{
		return get_finance_analysis_mode() == FinanceAnalysisMode.FinanceAnalysis_Market;
	}

	private static boolean is_stock_mode() 
	{
		return get_finance_analysis_mode() == FinanceAnalysisMode.FinanceAnalysis_Stock;
	}

	public static int get_source_key(int source_type_index) 
	{
		if (!IS_FINANCE_MARKET_MODE) 
		{
			String errmsg = "It's NOT Market mode";
			throw new IllegalStateException(errmsg);
		}
		return source_type_index;
	}

	public static int get_source_key(int source_type_index, int company_group_number, String company_code_number) 
	{
		if (!IS_FINANCE_STOCK_MODE) 
		{
			String errmsg = "It's NOT Stock mode";
			throw new IllegalStateException(errmsg);
		}
		int company_code_number_int = Integer.valueOf(company_code_number);
		return (company_group_number << SOURCE_KEY_COMPANY_GROUP_NUMBER_BIT_OFFSET | company_code_number_int << SOURCE_KEY_COMPANY_CODE_NUMBER_BIT_OFFSET | source_type_index << SOURCE_KEY_SOURCE_TYPE_INDEX_BIT_OFFSET);
	}

	public static int get_source_type(int source_key) 
	{
		return ((source_key & SOURCE_KEY_SOURCE_TYPE_INDEX_MASK) >> SOURCE_KEY_SOURCE_TYPE_INDEX_BIT_OFFSET);
	}

	public static String get_company_code_number(int source_key)
	{
		if (!IS_FINANCE_STOCK_MODE) 
		{
			String errmsg = "It's NOT Stock mode";
			throw new IllegalStateException(errmsg);
		}
		return String.format("%04d", (source_key & SOURCE_KEY_COMPANY_GROUP_NUMBER_MASK) >> SOURCE_KEY_COMPANY_CODE_NUMBER_BIT_OFFSET);
	}

	public static int get_company_group_number(int source_key) 
	{
		if (!IS_FINANCE_STOCK_MODE) 
		{
			String errmsg = "It's NOT Stock mode";
			throw new IllegalStateException(errmsg);
		}
		return ((source_key & SOURCE_KEY_COMPANY_CODE_NUMBER_MASK) >> SOURCE_KEY_COMPANY_GROUP_NUMBER_BIT_OFFSET);
	}

	public static String transform_date_str(int year_value, int month_value, int day_value) 
	{
		return String.format("%d-%02d-%02d", year_value, month_value, day_value);
	}

	public static String transform_month_str(int year_value, int month_value) 
	{
		return String.format("%d-%02d", year_value, month_value);
	}

	public static String transform_quarter_str(int year_value, int quarter_value) 
	{
		return String.format("%dq%d", year_value, quarter_value);
	}

	static Matcher get_regex_matcher(String search_pattern, String data)
	{
		Pattern pattern = Pattern.compile(search_pattern);
		Matcher matcher = pattern.matcher(data);
		if (!matcher.find())
			return null;
		return matcher;
	}

	static Matcher get_finance_time_regex_matcher(FinanceTimeUnit finance_time_unit, String time_string)
	{
		return get_regex_matcher(FINANCE_TIME_REGEX_STRING_FORMAT_ARRAY[finance_time_unit.value()], time_string);
	}

	public static Matcher check_date_str_format(String date_string) 
	{
// Time Format: yyyy-MM-dd; Ex: 2015-09-04
		Matcher matcher = get_finance_time_regex_matcher(FinanceTimeUnit.FinanceTime_Date, date_string);
		if (matcher == null) 
		{
			String errmsg = String.format("The string[%s] is NOT date format", date_string);
			FinanceRecorderCmnDef.error(errmsg);
			throw new IllegalArgumentException(errmsg);
		}
		int date_string_len = date_string.length();
		if (date_string_len < DEF_MIN_DATE_STRING_LENGTH || date_string_len > DEF_MAX_DATE_STRING_LENGTH) 
		{
			String errmsg = String.format("The date stirng[%s] length is NOT in the range [%d, %d]", date_string_len, DEF_MIN_DATE_STRING_LENGTH, DEF_MAX_DATE_STRING_LENGTH);
			FinanceRecorderCmnDef.error(errmsg);
			throw new IllegalArgumentException(errmsg);
		}
		return matcher;
	}

	public static Matcher check_month_str_format(String month_string) 
	{
		// Time Format: yyyy-mm; Ex: 2015-09
		Matcher matcher = get_finance_time_regex_matcher(FinanceTimeUnit.FinanceTime_Month, month_string);
		if (matcher == null) 
		{
			String errmsg = String.format("The string[%s] is NOT month format", month_string);
			FinanceRecorderCmnDef.error(errmsg);
			throw new IllegalArgumentException(errmsg);
		}
		int month_string_len = month_string.length();
		if (month_string_len < DEF_MIN_MONTH_STRING_LENGTH || month_string_len > DEF_MAX_MONTH_STRING_LENGTH) 
		{
			String errmsg = String.format("The month stirng[%s] length is NOT in the range [%d, %d]", month_string_len, DEF_MIN_MONTH_STRING_LENGTH, DEF_MAX_MONTH_STRING_LENGTH);
			FinanceRecorderCmnDef.error(errmsg);
			throw new IllegalArgumentException(errmsg);
		}
		return matcher;
	}

	public static Matcher check_quarter_str_format(String quarter_string) 
	{
		// Time Format: yyyy[Qq]q; Ex: 2015q1
		Matcher matcher = get_finance_time_regex_matcher(FinanceTimeUnit.FinanceTime_Quarter, quarter_string);
		if (matcher == null) 
		{
			String errmsg = String.format("The string[%s] is NOT quarter format", quarter_string);
			FinanceRecorderCmnDef.error(errmsg);
			throw new IllegalArgumentException(errmsg);
		}
		int quarter_string_len = quarter_string.length();
		if (quarter_string_len < DEF_MIN_QUARTER_STRING_LENGTH || quarter_string_len > DEF_MAX_QUARTER_STRING_LENGTH) 
		{
			String errmsg = String.format("The quarter stirng[%s] length is NOT in the range [%d, %d]", quarter_string_len, DEF_MIN_QUARTER_STRING_LENGTH, DEF_MAX_QUARTER_STRING_LENGTH);
			FinanceRecorderCmnDef.error(errmsg);
			throw new IllegalArgumentException(errmsg);
		}
		return matcher;
	}

	static FinanceTimeUnit get_time_unit_from_string(String data)
	{
// Find the time unit of the string
		for (int i = 0 ; i < FinanceRecorderCmnDef.FINANCE_TIME_REGEX_STRING_FORMAT_ARRAY.length ; i++)
		{
			FinanceTimeUnit finance_time_unit = FinanceRecorderCmnDef.FinanceTimeUnit.values()[i];
			if (FinanceRecorderCmnDef.get_finance_time_regex_matcher(finance_time_unit, data) != null)
				return finance_time_unit;
		}
		return FinanceTimeUnit.FinanceTime_Undefined;
	}

	public static int[] get_date_value_list_from_str(String date_string)
	{
		Matcher matcher = check_date_str_format(date_string);
		return new int[] { Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher.group(3)) };
	}

	public static int[] get_month_value_list_from_str(String month_string) 
	{
		Matcher matcher = check_month_str_format(month_string);
		return new int[] { Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)) };
	}

	public static int[] get_quarter_value_list_from_str(String quarter_string) 
	{
		Matcher matcher = check_quarter_str_format(quarter_string);
		return new int[] { Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)) };
	}

	public static void check_year_range(int year_value) 
	{
		if (year_value < FinanceRecorderCmnDef.DEF_START_YEAR || year_value > FinanceRecorderCmnDef.DEF_END_YEAR)
			throw new IndexOutOfBoundsException(String.format("Year[%d] is NOT in range [%d, %d]", year_value, FinanceRecorderCmnDef.DEF_START_YEAR, FinanceRecorderCmnDef.DEF_END_YEAR));
	}

	public static void check_month_range(int month_value) 
	{
		if (month_value < FinanceRecorderCmnDef.DEF_START_MONTH || month_value > FinanceRecorderCmnDef.DEF_END_MONTH)
			throw new IndexOutOfBoundsException(String.format("Month[%d] is NOT in range [%d, %d]", month_value, FinanceRecorderCmnDef.DEF_START_MONTH, FinanceRecorderCmnDef.DEF_END_MONTH));
	}

	public static void check_quarter_range(int quarter_value) 
	{
		if (quarter_value < FinanceRecorderCmnDef.DEF_START_QUARTER || quarter_value > FinanceRecorderCmnDef.DEF_END_QUARTER)
			throw new IndexOutOfBoundsException(String.format("Quarter[%d] is NOT in range [%d, %d]", quarter_value, FinanceRecorderCmnDef.DEF_START_QUARTER, FinanceRecorderCmnDef.DEF_END_QUARTER));
	}

	public static void check_day_range(int day_value, int year_value, int month_value)
	{
		int end_day_in_month = get_month_last_day(year_value, month_value);
		if (day_value < FinanceRecorderCmnDef.DEF_START_DAY || day_value > end_day_in_month)
			throw new IndexOutOfBoundsException(String.format("Day[%d] is NOT in range [%d, %d]", day_value, FinanceRecorderCmnDef.DEF_START_DAY, end_day_in_month));
	}

	public static int get_quarter_from_month(int month)
	{
		check_month_range(month);
		switch (month)
		{
		case 1:
		case 2:
		case 3:
			return 1;
		case 4:
		case 5:
		case 6:
			return 2;
		case 7:
		case 8:
		case 9:
			return 3;
		case 10:
		case 11:
		case 12:
			return 4;
		}
// Impossible to reach
		return 0;
	}
	
	public static short get_config_file_lines(String filename, LinkedList<String> config_line_list) 
	{
		String current_path = get_current_path();
		String conf_filepath = String.format("%s/%s/%s", current_path, CONF_FOLDERNAME, filename);
		debug(String.format("Check the config file[%s] exist", conf_filepath));
		File f = new File(conf_filepath);
		if (!f.exists()) 
		{
			format_error("The configration file[%s] does NOT exist", conf_filepath);
			return RET_FAILURE_NOT_FOUND;
		}

		// Open the config file for reading
		BufferedReader br = null;
		try 
		{
			FileInputStream fis = new FileInputStream(f);
			InputStreamReader isr = new InputStreamReader(fis);
			br = new BufferedReader(isr);
		} 
		catch (IOException e) 
		{
			format_error("Fails to open %s file, due to: %s", conf_filepath, e.toString());
			return RET_FAILURE_IO_OPERATION;
		}

		short ret = RET_SUCCESS;
		// Read the conf file
		try 
		{
			String line = null;
			while ((line = br.readLine()) != null) 
			{
				if (line.startsWith("#"))
					continue;
				String line_strip = line.replace("\n", "");
				if (line_strip.isEmpty())
					continue;
				config_line_list.add(line_strip);
			}
		} 
		catch (IOException e) 
		{
			format_error("IO Error occur while parsing the config file, due to: %s", e.toString());
			ret = RET_FAILURE_IO_OPERATION;
		} 
		catch (Exception e) 
		{
			format_error("Error occur while parsing the config file, due to: %s", e.toString());
			ret = RET_FAILURE_UNKNOWN;
		} 
		finally 
		{
			if (br != null) 
			{
				try 
				{
					br.close();
				} 
				catch (Exception e) {}
				br = null;
			}
		}
		return ret;
	}

	private static FinanceFieldType[] TransformFieldTypeString2Enum(String[] field_type_string_list) 
	{
		FinanceFieldType[] file_type_int_list = new FinanceFieldType[field_type_string_list.length];
		for (int i = 0; i < field_type_string_list.length; i++) 
		{
			String field_type_string = field_type_string_list[i].split(" ")[0];
			switch (field_type_string) 
			{
			case "INT":
				file_type_int_list[i] = FinanceFieldType.FinanceField_INT;
				break;
			case "BIGINT":
				file_type_int_list[i] = FinanceFieldType.FinanceField_LONG;
				break;
			case "FLOAT":
				file_type_int_list[i] = FinanceFieldType.FinanceField_FLOAT;
				break;
			case "DATE":
				file_type_int_list[i] = FinanceFieldType.FinanceField_DATE;
				break;
			default:
				throw new IllegalArgumentException(String.format("Unknown field type: %s", field_type_string));
			}
		}
		return file_type_int_list;
	}

	private static final String get_code_position() 
	{
		return String.format("%s:%d", FinanceRecorderCmnBase.__FILE__(), FinanceRecorderCmnBase.__LINE__());
	}

	// public static String field_array_to_string(String[] field_array)
	// {
	// String field_string = null;
	// for (String field : field_array)
	// {
	// if (field_string == null)
	// field_string = field;
	// else
	// field_string += (COMMA_DATA_SPLIT + field);
	// }
	//
	// return field_string;
	// }

	// public static String[] field_string_to_array(String field_string)
	// {
	// return field_string.split(DATA_SPLIT);
	// }

	public static void debug(String msg) 
	{
		finance_recorder_logger.write_debug_msg(String.format("[%s:%d] %s", FinanceRecorderCmnBase.__FILE__(), FinanceRecorderCmnBase.__LINE__(), msg));
	}

	public static void info(String msg) 
	{
		finance_recorder_logger.write_info_msg(String.format("[%s:%d] %s", FinanceRecorderCmnBase.__FILE__(), FinanceRecorderCmnBase.__LINE__(), msg));
	}

	public static void warn(String msg) 
	{
		finance_recorder_logger.write_warn_msg(String.format("[%s:%d] %s", FinanceRecorderCmnBase.__FILE__(), FinanceRecorderCmnBase.__LINE__(), msg));
	}

	public static void error(String msg) 
	{
		finance_recorder_logger.write_error_msg(String.format("[%s:%d] %s", FinanceRecorderCmnBase.__FILE__(), FinanceRecorderCmnBase.__LINE__(), msg));
	}

	public static void format_debug(String format, Object... arguments) 
	{
		finance_recorder_logger.write_debug_msg(String.format("[%s:%d] %s", FinanceRecorderCmnBase.__FILE__(), FinanceRecorderCmnBase.__LINE__(), String.format(format, arguments)));
	}

	public static void format_info(String format, Object... arguments) 
	{
		finance_recorder_logger.write_info_msg(String.format("[%s:%d] %s", FinanceRecorderCmnBase.__FILE__(), FinanceRecorderCmnBase.__LINE__(), String.format(format, arguments)));
	}

	public static void format_warn(String format, Object... arguments)
	{
		finance_recorder_logger.write_warn_msg(String.format("[%s:%d] %s", FinanceRecorderCmnBase.__FILE__(), FinanceRecorderCmnBase.__LINE__(), String.format(format, arguments)));
	}

	public static void format_error(String format, Object... arguments) 
	{
		finance_recorder_logger.write_error_msg(String.format("[%s:%d] %s", FinanceRecorderCmnBase.__FILE__(), FinanceRecorderCmnBase.__LINE__(), String.format(format, arguments)));
	}

	public static String get_current_path() 
	{
		String cur_path = null;
		try 
		{
			File cur_dir = new File(".");
			cur_path = cur_dir.getCanonicalPath();
		} 
		catch (Exception e) 
		{
			error(String.format("Fail to get the current path: %s", e.toString()));
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
		for (int index = 0; index < string_arr_len; index++)
			string_arr[index] = String.format("%s %s", string_arr1[index], string_arr2[index]);
		return string_arr;
	}

	public static String transform_array_to_sql_string(String[] string_arr) 
	{
		String sql_string = null;
		for (String string : string_arr) 
		{
			String encoded_string = string; // new String(string.getBytes("UTF-16"), "Big5");
			if (sql_string == null)
				sql_string = encoded_string;
			else
				sql_string += String.format(",%s", encoded_string);
		}
		return sql_string;
	}

	public static int get_month_last_day(int year, int month) 
	{
		check_year_range(year);
		check_month_range(month);
		Calendar calendar = Calendar.getInstance();
		// passing month-1 because 0-->jan, 1-->feb... 11-->dec
		calendar.set(year, month - 1, 1);
		calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));
		return calendar.get(Calendar.DATE);
	}

	public static int get_quarter_first_month(int quarter) 
	{
		check_quarter_range(quarter);
		return (quarter * 3 - 2);
	}
	public static int get_quarter_last_month(int quarter) 
	{
		check_quarter_range(quarter);
		return (quarter * 3);
	}

	public static int get_quarter_last_day(int quarter) 
	{
		check_quarter_range(quarter);
		switch (quarter)
		{
		case 1:
		case 4:
			return 31;
		case 2:
		case 3:
			return 30;
		}
// Should not reach
		return 0;
	}

	public static java.util.Date get_date(String date_str) throws ParseException 
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); // your template here
		java.util.Date date = formatter.parse(date_str);
		return date;
	}

	public static java.util.Date get_month_date(String date_str) throws ParseException 
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM"); // your template here
		java.util.Date date = formatter.parse(date_str);
		return date;
	}

//	public static final String get_month_str(java.util.Date date) 
//	{
//		Calendar cal = Calendar.getInstance();
//		cal.setTime(date);
//		String time_month = String.format("%04d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);
//		return time_month;
//	}
//
//	public static final String get_date_str(java.util.Date date)
//	{
//		Calendar cal = Calendar.getInstance();
//		cal.setTime(date);
//		String time_date = String.format("%04d-%02d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE));
//		return time_date;
//	}

//	public static final String get_time_month_today() 
//	{
//		return get_month_str(new java.util.Date());
//	}
//
//	public static final String get_time_date_today() 
//	{
//		return get_date_str(new java.util.Date());
//	}

	public static short copy_file(String src_filepath, String dst_filepath) 
	{
		Path src_filepath_obj = Paths.get(src_filepath);
		Path dst_filepath_obj = Paths.get(dst_filepath);
		try 
		{
			CopyOption[] options = new CopyOption[] 
			{
				StandardCopyOption.REPLACE_EXISTING,
				StandardCopyOption.COPY_ATTRIBUTES 
			};
			format_debug("Copy File; From: %s, To: %s", src_filepath, dst_filepath);
			Files.copy(src_filepath_obj, dst_filepath_obj, options);
		} 
		catch (IOException e) 
		{
			format_error("Fail to copy file, due to: %s", e.toString());
			return RET_FAILURE_IO_OPERATION;
		}
		return RET_SUCCESS;
	}

	public static short direct_string_to_output_stream(String data, String filepath) 
	{
		// Open the config file for writing
		OutputStreamWriter osw = null;
		BufferedWriter bw = null;
		try 
		{
			if (filepath != null) // To file
			{
				File f = new File(filepath);
				FileOutputStream fos = new FileOutputStream(f);
				osw = new OutputStreamWriter(fos);
			} else // To Standard Output
			{
				osw = new OutputStreamWriter(System.out);
			}
			bw = new BufferedWriter(osw);
		} 
		catch (IOException e) 
		{
			format_error("Error occur while directing to output stream, due to: %s", e.toString());
			return RET_FAILURE_IO_OPERATION;
		}

		short ret = RET_SUCCESS;
		// Read the conf file
		try 
		{
			bw.write(data);
			bw.flush();
		} 
		catch (IOException e) 
		{
			format_error("Error occur while parsing the config file, due to: %s", e.toString());
			ret = RET_FAILURE_IO_OPERATION;
		} 
		finally 
		{
			if (bw != null) 
			{
				try 
				{
					bw.close();
				} 
				catch (Exception e) {}
				bw = null;
			}
		}
		return ret;
	}

	public static short direct_string_to_output_stream(String data) 
	{
		return direct_string_to_output_stream(data, null);
	}

	public static short execute_shell_command(String command, StringBuilder result_str_builder) 
	{
		StringBuffer output = new StringBuffer();
		Process p;
		try 
		{
			// In order to run the pipe command successfully
			String[] command_list = new String[] { "bash", "-c", command };
			p = Runtime.getRuntime().exec(command_list);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			String line = "";
			while ((line = reader.readLine()) != null)
				output.append(line + "\n");
		} 
		catch (InterruptedException e) 
		{
			format_error("Interrupted exception occurs while running command: %s", command);
			return RET_FAILURE_IO_OPERATION;
		} 
		catch (IOException e) 
		{
			format_error("Exception occurs while running command: %s, due to: %s", command, e.toString());
			return RET_FAILURE_IO_OPERATION;
		}

		result_str_builder.append(output.toString());
		return RET_SUCCESS;
	}

	public static boolean check_file_exist(final String filepath) 
	{
		File file = new File(filepath);
		return file.exists();
	}

	public static boolean check_config_file_exist(final String config_filename) 
	{
		String conf_filepath = String.format("%s/%s/%s", get_current_path(), CONF_FOLDERNAME, config_filename);
		return check_file_exist(conf_filepath);
	}

	public static short create_folder(final String path) 
	{
		short ret = RET_SUCCESS;
		File file = new File(path);
		if (!file.mkdirs()) 
		{
			format_error("Fail to create the folder: %s", path);
			ret = RET_FAILURE_IO_OPERATION;
		}
		return ret;
	}

	public static short create_folder_if_not_exist(final String path) 
	{
		if (!check_file_exist(path))
			return create_folder(path);
		return RET_SUCCESS;
	}

	public static short create_folder_in_project_if_not_exist(final String foldername_in_project) 
	{
		String folder_path = String.format("%s/%s", get_current_path(), foldername_in_project);
		return create_folder_if_not_exist(folder_path);
	}

	public static short send_email(String title, String address, String content) 
	{
		short ret = RET_SUCCESS;
		String cmd = String.format("echo \"%s\" | mail -s \"%s\" %s", content, title, address);
		try 
		{
			StringBuilder result_str_builder = new StringBuilder();
			ret = execute_shell_command(cmd, result_str_builder);
			if (CheckFailure(ret))
				return ret;
			// Process p = Runtime.getRuntime().exec(cmd);
			// int exit_val = p.waitFor();
			// format_debug("The process of the command[%s] exit value: %d",
			// cmd, exit_val);
		} 
		catch (Exception ex) 
		{
			format_error("Fail to run command: %s, due to: %s", cmd, ex.toString());
			return RET_FAILURE_UNKNOWN;
		}

		return ret;
	}

	// Index range: Positive: [0, data_size-1]; Negative: [-1, -data_size]
	public static int get_index_ex(int index, int data_size) 
	{
		return ((index < 0) ? index = data_size + index : index);
	}

	// Start index range: Positive: [0, data_size-1]; Negative: [-1, -data_size]
	public static int get_start_index_ex(int index, int data_size) 
	{
		return get_index_ex(index, data_size);
	}

	// End index range: Positive: [1, data_size]; Negative: [-1, -data_size]
	public static int get_end_index_ex(int end_index, int data_size) 
	{
		return ((end_index < 0) ? end_index = data_size + end_index + 1 : end_index);
	}

	public static boolean check_start_index_in_range(int start_index, int range_start, int range_end) 
	{
		assert range_start >= 0 : "range_start should be larger than 0";
		assert range_end >= 1 : "range_end should be larger than 1";
		return ((start_index >= range_start && start_index < range_end) ? true : false);
	}

	public static boolean check_end_index_in_range(int end_index, int range_start, int range_end) 
	{
		assert range_start >= 0 : "range_start should be larger than 0";
		assert range_end >= 1 : "range_end should be larger than 1";
		return ((end_index > range_start && end_index <= range_end) ? true : false);
	}

	public static short get_subfolder_list(String folderpath, List<String> subfolder_list) 
	{
		File dir = new File(folderpath);
		if (!dir.exists()) 
		{
			format_error("The folder does Not exist", folderpath);
			return RET_FAILURE_NOT_FOUND;
		}
		String[] children = dir.list();
		if (children != null) 
		{
			for (int i = 0; i < children.length; i++)
				subfolder_list.add(children[i]);
		}
		return RET_SUCCESS;
	}

	private static boolean delete_dir(File dir) 
	{
		if (dir.isDirectory()) 
		{
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) 
			{
				boolean success = delete_dir(new File(dir, children[i]));
				if (!success)
					return false;
			}
		}
		// The directory is now empty or this is a file so delete it
		return dir.delete();
	}

	public static short delete_subfolder(String folderpath) 
	{
		File dir = new File(folderpath);
		if (!dir.exists()) 
		{
			// format_error("The folder does Not exist", folderpath);
			return RET_FAILURE_NOT_FOUND;
		}
		return delete_dir(dir) ? RET_SUCCESS : RET_FAILURE_UNKNOWN;
	}

	public static short copy_dir(File src, File dest) 
	{
		if (src.isDirectory()) 
		{
			// if directory not exists, create it
			if (!dest.exists()) 
			{
				dest.mkdir();
				format_debug("The destination directory[%s] does NOT exist, create one", dest);
			}
			// list all the directory contents
			String files[] = src.list();
			for (String file : files) 
			{
				// construct the src and dest file structure
				File srcFile = new File(src, file);
				File destFile = new File(dest, file);
				// recursive copy
				copy_dir(srcFile, destFile);
			}
		} 
		else 
		{
			// if file, then copy it. Use bytes stream to support all file types
			try 
			{
				InputStream in = new FileInputStream(src);
				OutputStream out = new FileOutputStream(dest);
				byte[] buffer = new byte[1024];
				int length;
				// copy the file content in bytes
				while ((length = in.read(buffer)) > 0)
					out.write(buffer, 0, length);
				in.close();
				out.close();
				// System.out.println("File copied from " + src + " to " +
				// dest);
			} 
			catch (FileNotFoundException e) 
			{
				format_error("File Not Found, due to: %s", e.toString());
				return RET_FAILURE_NOT_FOUND;
			} 
			catch (IOException e) 
			{
				format_error("Error occurs while Operating IO, due to: %s", e.toString());
				return RET_FAILURE_IO_OPERATION;
			}
		}
		return RET_SUCCESS;
	}

	public static short copy_folder(String src_folderpath, String dest_folderpath) 
	{
		File src = new File(src_folderpath);
		if (!src.exists()) 
		{
			format_error("Source Folder[%s] Not Found", src_folderpath);
			return RET_FAILURE_NOT_FOUND;
		}
		File dest = new File(dest_folderpath);
		return copy_dir(src, dest);
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Interface
	public interface FinanceObserverInf 
	{
		public short notify(short type);
	}
}
