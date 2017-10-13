package com.price.finance_recorder;

//import java.io.*;
//import java.nio.file.*;
//import java.text.SimpleDateFormat;
////import java.util.ArrayList;
//import java.util.ArrayList;
import java.util.Arrays;
//import java.util.Calendar;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.LinkedList;
//import java.util.List;
////import java.util.TreeMap;
//import java.util.regex.*;
//import java.util.zip.GZIPOutputStream;
//import java.text.*;
//
//import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
//import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
//import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
//import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
//import org.apache.commons.compress.utils.IOUtils;


public class CmnDef extends FinanceRecorderCmnDef
{
// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Return values
//	public static final short RET_SUCCESS = 0;
//
//	public static final short RET_FAILURE_WARN_BASE = 0x1;
//	public static final short RET_FAILURE_WARN_INDEX_DUPLICATE = RET_FAILURE_WARN_BASE + 1;
//	public static final short RET_FAILURE_WARN_INDEX_IGNORE = RET_FAILURE_WARN_BASE + 2;
//	public static final short RET_FAILURE_WARN_PROCESS_CONTINUE = RET_FAILURE_WARN_BASE + 3;
//
//	public static final short RET_FAILURE_BASE = 0x100;
//	public static final short RET_FAILURE_UNKNOWN = RET_FAILURE_BASE + 1;
//	public static final short RET_FAILURE_INVALID_ARGUMENT = RET_FAILURE_BASE + 2;
//	public static final short RET_FAILURE_INVALID_POINTER = RET_FAILURE_BASE + 3;
//	public static final short RET_FAILURE_INSUFFICIENT_MEMORY = RET_FAILURE_BASE + 4;
//	public static final short RET_FAILURE_INCORRECT_OPERATION = RET_FAILURE_BASE + 5;
//	public static final short RET_FAILURE_NOT_FOUND = RET_FAILURE_BASE + 6;
//	public static final short RET_FAILURE_INCORRECT_CONFIG = RET_FAILURE_BASE + 7;
//	public static final short RET_FAILURE_HANDLE_THREAD = RET_FAILURE_BASE + 8;
//	public static final short RET_FAILURE_INCORRECT_PATH = RET_FAILURE_BASE + 9;
//	public static final short RET_FAILURE_IO_OPERATION = RET_FAILURE_BASE + 10;
//	public static final short RET_FAILURE_UNEXPECTED_VALUE = RET_FAILURE_BASE + 11;
//
//	public static final short RET_FAILURE_MYSQL_BASE = 0x200;
//	public static final short RET_FAILURE_MYSQL = RET_FAILURE_MYSQL_BASE + 1;
//	public static final short RET_FAILURE_MYSQL_UNKNOWN_DATABASE = RET_FAILURE_MYSQL_BASE + 2;
//	public static final short RET_FAILURE_MYSQL_NO_DRIVER = RET_FAILURE_MYSQL_BASE + 3;
//	public static final short RET_FAILURE_MYSQL_EXECUTE_COMMAND = RET_FAILURE_MYSQL_BASE + 4;
//	public static final short RET_FAILURE_MYSQL_DATABASE_ALREADY_EXIST = RET_FAILURE_MYSQL_BASE + 5;
//	public static final short RET_FAILURE_MYSQL_DATA_NOT_CONSISTENT = RET_FAILURE_MYSQL_BASE + 6;
//
//	public static boolean CheckSuccess(short x) {return (x == RET_SUCCESS ? true : false);}
//
//	public static boolean CheckFailure(short x) {return !CheckSuccess(x);}
//
//	public static boolean CheckFailureWarnProcessContinue(short x) {return (x == RET_FAILURE_WARN_PROCESS_CONTINUE ? true : false);}
//	public static boolean CheckFailureNotFound(short x) {return (x == RET_FAILURE_NOT_FOUND ? true : false);}
//
//	public static boolean CheckMySQLFailureUnknownDatabase(short x) {return (x == RET_FAILURE_MYSQL_UNKNOWN_DATABASE ? true : false);}

////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Constants
	static FinanceAnalysisMode FINANCE_MODE = null; // Will be determined after argument setting in main
	static boolean IS_FINANCE_MARKET_MODE;// = is_market_mode(); // Will be determined after argument setting in main
	static boolean IS_FINANCE_STOCK_MODE;// = !IS_FINANCE_MARKET_MODE; // Will be determined after argument setting in main

	static final int DEF_START_YEAR = 2000;
	static final int DEF_END_YEAR = 2100;
	static final String DEF_START_DATE_STR = String.format("%d-01-01", DEF_START_YEAR);
	static final String DEF_END_DATE_STR = String.format("%d-01-01", DEF_END_YEAR);
	static final int DEF_START_QUARTER = 1;
	static final int DEF_END_QUARTER = 4;
	static final int DEF_START_MONTH = 1;
	static final int DEF_END_MONTH = 12;
	static final int DEF_START_DAY = 1;

	static final String FINANCE_MARKET_MODE_DESCRIPTION = "market";
	static final String FINANCE_STOCK_MODE_DESCRIPTION = "stock";
	static final String CSV_ROOT_FOLDERPATH = "/var/tmp/finance";
	static final String CSV_BACKUP_FOLDERPATH = "/tmp/.finance_backup";
	static final String CSV_RESTORE_FOLDERPATH = CSV_BACKUP_FOLDERPATH;
	static final String CSV_MARKET_FOLDERNAME = "market";
	static final String CSV_STOCK_FOLDERNAME = "stock";
	static final String RESULT_FOLDERNAME = "result";
	static final String CONF_FOLDERNAME = "conf";
	static final String DEFAULT_FINANCE_ROOT_FOLDERPATH = "/opt/finance";
	static final String SCRAPY_PROJECT_NAME = "finance_scrapy_python";
	static final String RECORDER_PROJECT_NAME = "finance_recorder_java";
	static final String ANALYZER_PROJECT_NAME = "finance_analyzer";
	static final String FINANCE_SCRAPY_ROOT_FOLDERPATH = String.format("%s/%s", DEFAULT_FINANCE_ROOT_FOLDERPATH, SCRAPY_PROJECT_NAME);
	static final String FINANCE_RECORDER_ROOT_FOLDERPATH = String.format("%s/%s", DEFAULT_FINANCE_ROOT_FOLDERPATH, RECORDER_PROJECT_NAME);
	static final String FINANCE_ANALYZER_ROOT_FOLDERPATH = String.format("%s/%s", DEFAULT_FINANCE_ROOT_FOLDERPATH, ANALYZER_PROJECT_NAME);
	static final String DEFAULT_COMPANY_PROFILE_CONF_FOLDERPATH = String.format("%s/%s", FINANCE_SCRAPY_ROOT_FOLDERPATH, CONF_FOLDERNAME); 
	static final String DEFAULT_STATEMENT_PROFILE_CONF_FOLDERPATH = String.format("%s/%s", FINANCE_SCRAPY_ROOT_FOLDERPATH, CONF_FOLDERNAME); 
	static final String CONFIG_TIMESTAMP_STRING_PREFIX = "#time@";
//	static final String CSV_FOLDERPATH = String.format("%s/%s", CSV_ROOT_FOLDERPATH, (IS_FINANCE_MARKET_MODE ? CSV_MARKET_FOLDERNAME : CSV_STOCK_FOLDERNAME));
	static final String SQL_MARKET_DATABASE_NAME = "market";
	static final String SQL_STOCK_DATABASE_NAME = "stock";
//	static final String BACKUP_FOLDERNAME = ".backup";
	static final String MARKET_ALL_CONFIG_FILENAME = "market_all.conf";
	static final String STOCK_ALL_CONFIG_FILENAME = "stock_all.conf";
	static final String WORKDAY_CANLENDAR_CONF_FILENAME = ".workday_canlendar.conf";
	static final String MARKET_DATABASE_TIME_RANGE_CONF_FILENAME = ".market_database_time_range.conf";
	static final String STOCK_DATABASE_TIME_RANGE_CONF_FOLDERNAME = ".stock_database_time_range";
	static final String STOCK_DATABASE_TIME_RANGE_CONF_FILENAME_FORMAT = "%s.conf";
	static final String COMPANY_PROFILE_CONF_FILENAME = ".company_profile.conf";
	static final String COMPANY_GROUP_CONF_FILENAME = ".company_group.conf";
	static final String MARKET_STOCK_SWITCH_CONF_FILENAME = "market_stock_switch.conf";
	static final String FINANCE_RECORDER_CONF_FILENAME = "finance_recorder.conf";
	static final String BACKUP_CONF_FILENAME = "backup.conf";
	static final String BALANCE_SHEET_FIELD_NAME_CONF_FILENAME = "balance_sheet_field_name.conf";
	static final String INCOME_STATEMENT_FIELD_NAME_CONF_FILENAME = "income_statement_field_name.conf";
	static final String CASH_FLOW_STATEMENT_FIELD_NAME_CONF_FILENAME = "cash_flow_statement_field_name.conf";
	static final String STATEMENT_OF_CHANGES_IN_EQUITY_FIELD_NAME_CONF_FILENAME = "statement_of_changes_in_equity_field_name.conf";
	static final String[] STATEMENT_FIELD_NAME_CONF_FILENAME_ARRAY = new String[]{BALANCE_SHEET_FIELD_NAME_CONF_FILENAME, INCOME_STATEMENT_FIELD_NAME_CONF_FILENAME, CASH_FLOW_STATEMENT_FIELD_NAME_CONF_FILENAME, STATEMENT_OF_CHANGES_IN_EQUITY_FIELD_NAME_CONF_FILENAME}; 
	
	static final String MISSING_CSV_MARKET_FILENAME = ".missing_csv_market";
	static final String MISSING_CSV_STOCK_FILENAME = ".missing_csv_stock";
	
	static final String FINANCE_DATE_REGEX_STRING_FORMAT = "([\\d]{4})-([\\d]{1,2})-([\\d]{1,2})";
	static final String FINANCE_MONTH_REGEX_STRING_FORMAT = "([\\d]{4})-([\\d]{1,2})";
	static final String FINANCE_QUARTER_REGEX_STRING_FORMAT = "([\\d]{4})[Qq]([\\d]{1})";
	static final String[] FINANCE_TIME_REGEX_STRING_FORMAT_ARRAY = new String[] {FINANCE_DATE_REGEX_STRING_FORMAT, FINANCE_MONTH_REGEX_STRING_FORMAT, FINANCE_QUARTER_REGEX_STRING_FORMAT };

	static final String CSV_FILE_ROOT_FOLDERPATH = "/var/tmp/finance";
//	static final String MARKET_CSV_FILE_ROOT_FOLDERPATH = String.format("%s/market", CSV_FILE_ROOT_FOLDERPATH);
//	static final String STOCK_CSV_FILE_ROOT_FOLDERPATH = String.format("%s/stock", CSV_FILE_ROOT_FOLDERPATH);
	static final String COMMA_DATA_SPLIT = ",";
	static final String SPACE_DATA_SPLIT = " ";
	static final String COLON_DATA_SPLIT = ":";
	static final String ALIAS_DATA_SPLIT = ":ALIAS:";
	static final String DAILY_FINANCE_FILENAME_FORMAT = "daily_finance%04d%02d%02d";
	static final String DAILY_FINANCE_EMAIL_TITLE_FORMAT = "daily_finance%04d%02d%02d";
	static final String CONF_ENTRY_IGNORE_FLAG = "#";
	static final int MAX_CONCURRENT_THREAD = 4;
	static final int WRITE_SQL_MAX_MONTH_RANGE_IN_THREAD = 3;

	static final int BACKUP_SQL_MAX_MONTH_RANGE_IN_THREAD = 2;
	static final String DATABASE_TIME_RANGE_FILE_DST_PROJECT_NAME = "finance_analyzer";
	static final int FINANCE_SOURCE_SIZE = FinanceMethod.values().length;

	static final int SOURCE_KEY_SOURCE_TYPE_INDEX_BIT_OFFSET = 0;
	static final int SOURCE_KEY_COMPANY_CODE_NUMBER_BIT_OFFSET = 8;
	static final int SOURCE_KEY_COMPANY_GROUP_NUMBER_BIT_OFFSET = 24;
	static final int SOURCE_KEY_SOURCE_TYPE_INDEX_MASK = 0xFF << SOURCE_KEY_SOURCE_TYPE_INDEX_BIT_OFFSET;
	static final int SOURCE_KEY_COMPANY_CODE_NUMBER_MASK = 0xFFFF << SOURCE_KEY_COMPANY_CODE_NUMBER_BIT_OFFSET;
	static final int SOURCE_KEY_COMPANY_GROUP_NUMBER_MASK = 0xFF << SOURCE_KEY_COMPANY_GROUP_NUMBER_BIT_OFFSET;
	static final int NO_SOURCE_TYPE_MARKET_SOURCE_KEY_VALUE = 0;

	static final String MYSQL_TABLE_NAME_BASE = "year";
	static final String MYSQL_DATE_FILED_NAME = "date";
	static final String MYSQL_FILED_NAME_BASE = "value";

	static final int DEF_MIN_DATE_STRING_LENGTH = 8;
	static final int DEF_MAX_DATE_STRING_LENGTH = 10;
	static final int DEF_MIN_MONTH_STRING_LENGTH = 5;
	static final int DEF_MAX_MONTH_STRING_LENGTH = 7;
	static final int DEF_MIN_QUARTER_STRING_LENGTH = 4;
	static final int DEF_MAX_QUARTER_STRING_LENGTH = 6;

	static final CmnDef.FinanceTimeUnit DEF_CSV_TIME_UNIT = CmnDef.FinanceTimeUnit.FinanceTime_Date;
	static final CmnDef.FinanceTimeUnit DEF_SQL_TIME_UNIT = CmnDef.FinanceTimeUnit.FinanceTime_Date;
	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// enumeration

	static enum FinanceMethod 
	{
		FinanceMethod_Unknown(-1),
		// //////////////////////////////////////////////////////////////////////////////////////////////
		// Market data source
		FinanceMethod_MarketStart(0), // Keep in mind to update the value at the right time
		FinanceMethod_StockExchangeAndVolume(0), 
		FinanceMethod_StockTop3LegalPersonsNetBuyOrSell(1), 
		FinanceMethod_StockMarginTradingAndShortSelling(2), 
		FinanceMethod_FutureAndOptionTop3LegalPersonsOpenInterest(3), 
		FinanceMethod_FutureOrOptionTop3LegalPersonsOpenInterest(4), 
		FinanceMethod_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest(5), 
		FinanceMethod_OptionPutCallRatio(6), 
		FinanceMethod_FutureTop10DealersAndLegalPersons(7), 
		FinanceMethod_MarketEnd(8), // Keep in mind to update the value at the right time, semi-open interval
		// //////////////////////////////////////////////////////////////////////////////////////////////
		// Stock data source
		FinanceMethod_StockStart(8), // Keep in mind to update the value at the right time
		FinanceMethod_DepositoryShareholderDistributionTable(8),
		FinanceMethod_StockStatementStart(9), // Keep in mind to update the value at the right time
		FinanceMethod_BalanceSheet(9),
		FinanceMethod_IncomeStatement(10),
		FinanceMethod_CashFlowStatement(11),
		FinanceMethod_StatementOfChangesInEquity(12),
		FinanceMethod_StockEnd(13), // Keep in mind to update the value at the right time, semi-open interval
		FinanceMethod_StockStatementEnd(13); // Keep in mind to update the value at the right time

		private int value = 0;

		private FinanceMethod(int value) 
		{
			this.value = value;
		}

		static FinanceMethod valueOf(int value) 
		{
			switch (value) 
			{
// Market Start
			case 0:
				return FinanceMethod_StockExchangeAndVolume;
			case 1:
				return FinanceMethod_StockTop3LegalPersonsNetBuyOrSell;
			case 2:
				return FinanceMethod_StockMarginTradingAndShortSelling;
			case 3:
				return FinanceMethod_FutureAndOptionTop3LegalPersonsOpenInterest;
			case 4:
				return FinanceMethod_FutureOrOptionTop3LegalPersonsOpenInterest;
			case 5:
				return FinanceMethod_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest;
			case 6:
				return FinanceMethod_OptionPutCallRatio;
			case 7:
				return FinanceMethod_FutureTop10DealersAndLegalPersons;
// Market End
///////////////////////////////////////////////////////////////////
//Stock Start
			case 8:
				return FinanceMethod_DepositoryShareholderDistributionTable;
			case 9:
				return FinanceMethod_BalanceSheet;
			case 10:
				return FinanceMethod_IncomeStatement;
			case 11:
				return FinanceMethod_CashFlowStatement;
			case 12:
				return FinanceMethod_StatementOfChangesInEquity;
// Stock End
			default:
				return null;
			}
		}
		public int value() 
		{
			return this.value;
		}

//		static String descriptionOf(int value) 
//		{
//			switch (value) 
//			{
//// Market Start
//			case 0:
//				return "臺股指數及成交量";
//			case 1:
//				return "三大法人現貨買賣超";
//			case 2:
//				return "現貨融資融券餘額";
//			case 3:
//				return "三大法人期貨和選擇權留倉淨額";
//			case 4:
//				return "三大法人期貨或選擇權留倉淨額";
//			case 5:
//				return "三大法人選擇權買賣權留倉淨額";
//			case 6:
//				return "三大法人選擇權賣權買權比";
//			case 7:
//				return "十大交易人及特定法人期貨資訊";
//// Market End
/////////////////////////////////////////////////////////////////////
//// Stock Start
//			case 8:
//				return "個股集保戶股權分散表";
//			case 9:
//				return "資產負債表";
//			case 10:
//				return "損益表";
//			case 11:
//				return "現金流量表";
//			case 12:
//				return "股東權益變動表";
//// Stock End
//			default:
//				throw new IllegalArgumentException(String.format("Unknown Finance Source Type: %d", value));
//			}
//		}

		static boolean is_market_method(int method_index) 
		{
			return (method_index >= FinanceMethod_MarketStart.value && method_index < FinanceMethod_MarketEnd.value);
		}

		static boolean is_stock_method(int method_index) 
		{
			return (method_index >= FinanceMethod_StockStart.value && method_index < FinanceMethod_StockEnd.value);
		}

		static int get_market_method_amount()
		{
			return (FinanceMethod_MarketEnd.value - FinanceMethod_MarketStart.value);
		}

		static int get_stock_method_amount()
		{
			return (FinanceMethod_StockEnd.value - FinanceMethod_StockStart.value);
		}
	};

	static enum FinanceFieldType 
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

		static FinanceFieldType valueOf(int value) 
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

	static enum FinanceTimeUnit
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

		static FinanceTimeUnit valueOf(int value) 
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

	static enum FinanceTimeRangeType
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

		static FinanceTimeRangeType valueOf(int value)
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

	static enum OperationType {Operation_NonStop, Operation_Stop,};
	static enum DatabaseEnableBatchType {DatabaseEnableBatch_Yes, DatabaseEnableBatch_No,};
	static enum CreateThreadType {CreateThread_Single, CreateThread_Multiple,};
	static enum ResultSetDataUnit {ResultSetDataUnit_NoMethod, ResultSetDataUnit_Method};
	static enum CSVWorkingFolderType 
	{
		CSVWorkingFolder_Write(0),
		CSVWorkingFolder_Backup(1),
		CSVWorkingFolder_Restore(2),
		CSVWorkingFolder_Unknown(3);

		private int value = 0;

		private CSVWorkingFolderType(int value) 
		{
			this.value = value;
		}

		static CSVWorkingFolderType valueOf(int value) 
		{
			switch (value) 
			{
			case 0:
				return CSVWorkingFolder_Write;
			case 1:
				return CSVWorkingFolder_Backup;
			case 2:
				return CSVWorkingFolder_Restore;
			case 3:
				return CSVWorkingFolder_Unknown;
			default:
				return null;
			}
		}

		public int value() 
		{
			return this.value;
		}
	};

	static final String[] FINANCE_DATA_NAME_LIST = new String[] 
	{
// Market Start
		"stock_exchange_and_volume",
		"stock_top3_legal_persons_net_buy_or_sell",
		"stock_margin_trading_and_short_selling",
		"future_and_option_top3_legal_persons_open_interest",
		"future_or_option_top3_legal_persons_open_interest",
		"option_top3_legal_persons_buy_and_sell_option_open_interest",
		"option_put_call_ratio", 
		"future_top10_dealers_and_legal_persons",
// Market End
		///////////////////////////////////////////////////////////
// Stock Start
		"company_depository_shareholder_distribution_table",
		"balance_sheet",
		"income_statement",
		"cash_flow_statement",
		"statement_of_changes_in_equity",
// Stock End
	};
	static final String[] FINANCE_METHOD_DESCRIPTION_LIST = new String[] 
	{
// Market Start
		"臺股指數及成交量", 
		"三大法人現貨買賣超", 
		"現貨融資融券餘額", 
		"三大法人期貨和選擇權留倉淨額",
		"三大法人期貨或選擇權留倉淨額", 
		"三大法人選擇權買賣權留倉淨額", 
		"三大法人選擇權賣權買權比",
		"十大交易人及特定法人期貨資訊",
// Market End
		///////////////////////////////////////////////////////////
// Stock Start
		"個股集保戶股權分散表",
		"資產負債表",
		"損益表",
		"現金流量表",
		"股東權益變動表",
// Stock End
	};
	static final String[] FINANCE_DATA_FOLDER_MAPPING = new String[] 
	{
		"market",
		"market",
		"market",
		"market",
		"market",
		"market",
		"market",
		"market",
		"stock",
		"stock",
		"stock",
		"stock",
		"stock",
	};
	static final String[] FINANCE_DATA_SQL_FIELD_LIST = new String[] 
	{
// Market Start
		transform_array_to_sql_string(merge_string_array_element(CmnDefMarketDB.STOCK_EXCHANGE_AND_VALUE_FIELD_DEFINITION, CmnDefMarketDB.STOCK_EXCHANGE_AND_VALUE_FIELD_TYPE_DEFINITION)),
		transform_array_to_sql_string(merge_string_array_element(CmnDefMarketDB.STOCK_TOP3_LEGAL_PERSONS_NET_BUY_OR_SELL_FIELD_DEFINITION, CmnDefMarketDB.STOCK_TOP3_LEGAL_PERSONS_NET_BUY_OR_SELL_FIELD_TYPE_DEFINITION)),
		transform_array_to_sql_string(merge_string_array_element(CmnDefMarketDB.STOCK_MARGIN_TRADING_AND_SHORT_SELLING_FIELD_DEFINITION, CmnDefMarketDB.STOCK_MARGIN_TRADING_AND_SHORT_SELLING_FIELD_TYPE_DEFINITION)),
		transform_array_to_sql_string(merge_string_array_element(CmnDefMarketDB.FUTURE_AND_OPTION_TOP3_LEGAL_PERSONS_OPEN_INTEREST_FIELD_DEFINITION, CmnDefMarketDB.FUTURE_AND_OPTION_TOP3_LEGAL_PERSONS_OPEN_INTEREST_FIELD_TYPE_DEFINITION)),
		transform_array_to_sql_string(merge_string_array_element(CmnDefMarketDB.FUTURE_OR_OPTION_TOP3_LEGAL_PERSONS_OPEN_INTEREST_FIELD_DEFINITION, CmnDefMarketDB.FUTURE_OR_OPTION_TOP3_LEGAL_PERSONS_OPEN_INTEREST_FIELD_TYPE_DEFINITION)),
		transform_array_to_sql_string(merge_string_array_element(CmnDefMarketDB.OPTION_TOP3_LEGAL_PERSONS_BUY_AND_SELL_OPTION_OPEN_INTEREST_FIELD_DEFINITION, CmnDefMarketDB.OPTION_TOP3_LEGAL_PERSONS_BUY_AND_SELL_OPTION_OPEN_INTEREST_FIELD_TYPE_DEFINITION)),
		transform_array_to_sql_string(merge_string_array_element(CmnDefMarketDB.OPTION_PUT_CALL_RATIO_FIELD_DEFINITION, CmnDefMarketDB.OPTION_PUT_CALL_RATIO_FIELD_TYPE_DEFINITION)),
		transform_array_to_sql_string(merge_string_array_element(CmnDefMarketDB.FUTURE_TOP10_DEALERS_AND_LEGAL_PERSONS_FIELD_DEFINITION, CmnDefMarketDB.FUTURE_TOP10_DEALERS_AND_LEGAL_PERSONS_FIELD_TYPE_DEFINITION)),
// Market End
		///////////////////////////////////////////////////////////
// Stock Start
		transform_array_to_sql_string(merge_string_array_element(CmnDefStockDB.COMPANY_DEPOSITORY_SHAREHOLDER_DISTRIBUTION_TABLE_FIELD_DEFINITION, CmnDefStockDB.COMPANY_DEPOSITORY_SHAREHOLDER_DISTRIBUTION_TABLE_FIELD_TYPE_DEFINITION)),
		transform_array_to_sql_string(merge_string_array_element(CmnDefStockDB.BALANCE_SHEET_TABLE_FIELD_DEFINITION, CmnDefStockDB.BALANCE_SHEET_TABLE_FIELD_TYPE_DEFINITION)),
		transform_array_to_sql_string(merge_string_array_element(CmnDefStockDB.INCOME_STATEMENT_TABLE_FIELD_DEFINITION, CmnDefStockDB.INCOME_STATEMENT_TABLE_FIELD_TYPE_DEFINITION)),
		transform_array_to_sql_string(merge_string_array_element(CmnDefStockDB.CASH_FLOW_STATEMENT_TABLE_FIELD_DEFINITION, CmnDefStockDB.CASH_FLOW_STATEMENT_TABLE_FIELD_TYPE_DEFINITION)),
		transform_array_to_sql_string(merge_string_array_element(CmnDefStockDB.STATEMENT_OF_CHANGES_IN_EQUITY_TABLE_FIELD_DEFINITION, CmnDefStockDB.STATEMENT_OF_CHANGES_IN_EQUITY_TABLE_FIELD_TYPE_DEFINITION)),
// Stock End
	};
	static final String[][] FINANCE_DATA_SQL_FIELD_DEFINITION_LIST = new String[][] 
	{
// Market Start
		CmnDefMarketDB.STOCK_EXCHANGE_AND_VALUE_FIELD_DEFINITION,
		CmnDefMarketDB.STOCK_TOP3_LEGAL_PERSONS_NET_BUY_OR_SELL_FIELD_DEFINITION,
		CmnDefMarketDB.STOCK_MARGIN_TRADING_AND_SHORT_SELLING_FIELD_DEFINITION,
		CmnDefMarketDB.FUTURE_AND_OPTION_TOP3_LEGAL_PERSONS_OPEN_INTEREST_FIELD_DEFINITION,
		CmnDefMarketDB.FUTURE_OR_OPTION_TOP3_LEGAL_PERSONS_OPEN_INTEREST_FIELD_DEFINITION,
		CmnDefMarketDB.OPTION_TOP3_LEGAL_PERSONS_BUY_AND_SELL_OPTION_OPEN_INTEREST_FIELD_DEFINITION,
		CmnDefMarketDB.OPTION_PUT_CALL_RATIO_FIELD_DEFINITION,
		CmnDefMarketDB.FUTURE_TOP10_DEALERS_AND_LEGAL_PERSONS_FIELD_DEFINITION,
// Market End
		///////////////////////////////////////////////////////////
// Stock Start
		CmnDefStockDB.COMPANY_DEPOSITORY_SHAREHOLDER_DISTRIBUTION_TABLE_FIELD_DEFINITION,
		CmnDefStockDB.BALANCE_SHEET_TABLE_FIELD_DEFINITION,
		CmnDefStockDB.INCOME_STATEMENT_TABLE_FIELD_DEFINITION,
		CmnDefStockDB.CASH_FLOW_STATEMENT_TABLE_FIELD_DEFINITION,
		CmnDefStockDB.STATEMENT_OF_CHANGES_IN_EQUITY_TABLE_FIELD_DEFINITION,
// Stock End
	};
	static final String[][] FINANCE_DATA_SQL_FIELD_TYPE_DEFINITION_LIST = new String[][] 
	{
// Market Start
		CmnDefMarketDB.STOCK_EXCHANGE_AND_VALUE_FIELD_TYPE_DEFINITION,
		CmnDefMarketDB.STOCK_TOP3_LEGAL_PERSONS_NET_BUY_OR_SELL_FIELD_TYPE_DEFINITION,
		CmnDefMarketDB.STOCK_MARGIN_TRADING_AND_SHORT_SELLING_FIELD_TYPE_DEFINITION,
		CmnDefMarketDB.FUTURE_AND_OPTION_TOP3_LEGAL_PERSONS_OPEN_INTEREST_FIELD_TYPE_DEFINITION,
		CmnDefMarketDB.FUTURE_OR_OPTION_TOP3_LEGAL_PERSONS_OPEN_INTEREST_FIELD_TYPE_DEFINITION,
		CmnDefMarketDB.OPTION_TOP3_LEGAL_PERSONS_BUY_AND_SELL_OPTION_OPEN_INTEREST_FIELD_TYPE_DEFINITION,
		CmnDefMarketDB.OPTION_PUT_CALL_RATIO_FIELD_TYPE_DEFINITION,
		CmnDefMarketDB.FUTURE_TOP10_DEALERS_AND_LEGAL_PERSONS_FIELD_TYPE_DEFINITION,
// Market End
		///////////////////////////////////////////////////////////
// Stock Start
		CmnDefStockDB.COMPANY_DEPOSITORY_SHAREHOLDER_DISTRIBUTION_TABLE_FIELD_TYPE_DEFINITION,
		CmnDefStockDB.BALANCE_SHEET_TABLE_FIELD_TYPE_DEFINITION,
		CmnDefStockDB.INCOME_STATEMENT_TABLE_FIELD_TYPE_DEFINITION,
		CmnDefStockDB.CASH_FLOW_STATEMENT_TABLE_FIELD_TYPE_DEFINITION,
		CmnDefStockDB.STATEMENT_OF_CHANGES_IN_EQUITY_TABLE_FIELD_TYPE_DEFINITION,
// Stock End
	};
	static final FinanceFieldType[][] FINANCE_DATABASE_FIELD_TYPE_LIST = new FinanceFieldType[][] 
	{
// Market Start
		TransformFieldTypeString2Enum(CmnDefMarketDB.STOCK_EXCHANGE_AND_VALUE_FIELD_TYPE_DEFINITION),
		TransformFieldTypeString2Enum(CmnDefMarketDB.STOCK_TOP3_LEGAL_PERSONS_NET_BUY_OR_SELL_FIELD_TYPE_DEFINITION),
		TransformFieldTypeString2Enum(CmnDefMarketDB.STOCK_MARGIN_TRADING_AND_SHORT_SELLING_FIELD_TYPE_DEFINITION),
		TransformFieldTypeString2Enum(CmnDefMarketDB.FUTURE_AND_OPTION_TOP3_LEGAL_PERSONS_OPEN_INTEREST_FIELD_TYPE_DEFINITION),
		TransformFieldTypeString2Enum(CmnDefMarketDB.FUTURE_OR_OPTION_TOP3_LEGAL_PERSONS_OPEN_INTEREST_FIELD_TYPE_DEFINITION),
		TransformFieldTypeString2Enum(CmnDefMarketDB.OPTION_TOP3_LEGAL_PERSONS_BUY_AND_SELL_OPTION_OPEN_INTEREST_FIELD_TYPE_DEFINITION),
		TransformFieldTypeString2Enum(CmnDefMarketDB.OPTION_PUT_CALL_RATIO_FIELD_TYPE_DEFINITION),
		TransformFieldTypeString2Enum(CmnDefMarketDB.FUTURE_TOP10_DEALERS_AND_LEGAL_PERSONS_FIELD_TYPE_DEFINITION),
// Market End
		///////////////////////////////////////////////////////////
// Stock Start
		TransformFieldTypeString2Enum(CmnDefStockDB.COMPANY_DEPOSITORY_SHAREHOLDER_DISTRIBUTION_TABLE_FIELD_TYPE_DEFINITION),
		TransformFieldTypeString2Enum(CmnDefStockDB.BALANCE_SHEET_TABLE_FIELD_TYPE_DEFINITION),
		TransformFieldTypeString2Enum(CmnDefStockDB.INCOME_STATEMENT_TABLE_FIELD_TYPE_DEFINITION),
		TransformFieldTypeString2Enum(CmnDefStockDB.CASH_FLOW_STATEMENT_TABLE_FIELD_TYPE_DEFINITION),
		TransformFieldTypeString2Enum(CmnDefStockDB.STATEMENT_OF_CHANGES_IN_EQUITY_TABLE_FIELD_TYPE_DEFINITION),
// Stock End
	};
	static final int FINANCE_DATABASE_FIELD_AMOUNT_LIST[] = 
	{
// Market Start
		CmnDefMarketDB.STOCK_EXCHANGE_AND_VALUE_FIELD_TYPE_DEFINITION.length,
		CmnDefMarketDB.STOCK_TOP3_LEGAL_PERSONS_NET_BUY_OR_SELL_FIELD_TYPE_DEFINITION.length,
		CmnDefMarketDB.STOCK_MARGIN_TRADING_AND_SHORT_SELLING_FIELD_TYPE_DEFINITION.length,
		CmnDefMarketDB.FUTURE_AND_OPTION_TOP3_LEGAL_PERSONS_OPEN_INTEREST_FIELD_TYPE_DEFINITION.length,
		CmnDefMarketDB.FUTURE_OR_OPTION_TOP3_LEGAL_PERSONS_OPEN_INTEREST_FIELD_TYPE_DEFINITION.length,
		CmnDefMarketDB.OPTION_TOP3_LEGAL_PERSONS_BUY_AND_SELL_OPTION_OPEN_INTEREST_FIELD_TYPE_DEFINITION.length,
		CmnDefMarketDB.OPTION_PUT_CALL_RATIO_FIELD_TYPE_DEFINITION.length,
		CmnDefMarketDB.FUTURE_TOP10_DEALERS_AND_LEGAL_PERSONS_FIELD_TYPE_DEFINITION.length,
// Market End
		///////////////////////////////////////////////////////////
// Stock Start
		CmnDefStockDB.COMPANY_DEPOSITORY_SHAREHOLDER_DISTRIBUTION_TABLE_FIELD_TYPE_DEFINITION.length,
		CmnDefStockDB.BALANCE_SHEET_TABLE_FIELD_TYPE_DEFINITION.length,
		CmnDefStockDB.INCOME_STATEMENT_TABLE_FIELD_TYPE_DEFINITION.length,
		CmnDefStockDB.CASH_FLOW_STATEMENT_TABLE_FIELD_TYPE_DEFINITION.length,
		CmnDefStockDB.STATEMENT_OF_CHANGES_IN_EQUITY_TABLE_FIELD_TYPE_DEFINITION.length,
// Stock End
	};

// Semi-open interval
	static final int FINANCE_METHOD_MARKET_START = 0;
	static final int FINANCE_METHOD_MARKET_END = Arrays.asList(FINANCE_DATA_FOLDER_MAPPING).indexOf("stock");
	static final int FINANCE_METHOD_STOCK_START = FINANCE_METHOD_MARKET_END;
	static final int FINANCE_METHOD_STOCK_END = FINANCE_DATA_FOLDER_MAPPING.length;
	static final int FINANCE_METHOD_MARKET_SIZE = FINANCE_METHOD_MARKET_END - FINANCE_METHOD_MARKET_START;
	static final int FINANCE_METHOD_STOCK_SIZE = FINANCE_METHOD_STOCK_END - FINANCE_METHOD_STOCK_START;

	static final int FINANCE_METHOD_STOCK_STATMENT_START = FinanceMethod.FinanceMethod_StockStatementStart.value();
	static final int FINANCE_METHOD_STOCK_STATMENT_END = FinanceMethod.FinanceMethod_StockStatementEnd.value();

//// Setter and Getter
//// Allow to assign the variable only once
//	private static boolean set_show_console = false;
//	private static boolean show_console = true;
//
//	static void enable_show_console(boolean show) 
//	{
//		if (!set_show_console) 
//		{
//			show_console = show;
//			set_show_console = true;
//		} 
//		else
//		{
//			CmnLogger.warn("The show_console variable has already been Set");
//		}
//	}
//	static boolean is_show_console() {return show_console;}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Private function

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


	private static String transform_array_to_sql_string(String[] string_arr) 
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
}
