package com.price.finance_recorder_lib;


public class FinanceRecorderCmnDef 
{
	public static final short RET_SUCCESS = 0;

	public static final short RET_FAILURE_WARN_BASE = 0x1;
	public static final short RET_FAILURE_WARN_INDEX_DUPLICATE = RET_FAILURE_WARN_BASE + 1;
	public static final short RET_FAILURE_WARN_INDEX_IGNORE = RET_FAILURE_WARN_BASE + 2;
	public static final short RET_FAILURE_WARN_PROCESS_CONTINUE = RET_FAILURE_WARN_BASE + 3;

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

	public static boolean CheckFailureWarnProcessContinue(short x) {return (x == RET_FAILURE_WARN_PROCESS_CONTINUE ? true : false);}
	public static boolean CheckFailureNotFound(short x) {return (x == RET_FAILURE_NOT_FOUND ? true : false);}

	public static boolean CheckMySQLFailureUnknownDatabase(short x) {return (x == RET_FAILURE_MYSQL_UNKNOWN_DATABASE ? true : false);}


	private static final String[] WarnRetDescription = new String[] 
	{
		"Warning Base", 
		"Warning Index Duplicate", 
		"Warning Index Ignore",
		"Warning Process Continue" 
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
		else if (error_code >= RET_FAILURE_WARN_BASE)
			return WarnRetDescription[error_code - RET_FAILURE_WARN_BASE];
		else
			return "Success";
	}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Constants
//	public static final String CSV_ROOT_FOLDERPATH = "/var/tmp/finance";
//	public static final String CSV_BACKUP_FOLDERPATH = "/tmp/.finance_backup";
//	public static final String CSV_RESTORE_FOLDERPATH = CSV_BACKUP_FOLDERPATH;
//	public static final String MARKET_STOCK_SWITCH_CONF_FILENAME = "market_stock_switch.conf";
//	public static final String MARKET_ALL_CONFIG_FILENAME = "market_all.conf";
//	public static final String STOCK_ALL_CONFIG_FILENAME = "stock_all.conf";
//	public static final String DEFAULT_FINANCE_ROOT_FOLDERPATH = "/opt/finance";
//	public static final String CONF_FOLDERNAME = "conf";
//	static final String RECORDER_PROJECT_NAME = "finance_recorder_java";
//	static final String FINANCE_RECORDER_ROOT_FOLDERPATH = String.format("%s/%s", DEFAULT_FINANCE_ROOT_FOLDERPATH, RECORDER_PROJECT_NAME);
//	public static final String DEFAULT_COMPANY_PROFILE_CONF_FOLDERPATH = String.format("%s/%s", FINANCE_SCRAPY_ROOT_FOLDERPATH, CONF_FOLDERNAME); 
//	public static final String DEFAULT_STATEMENT_PROFILE_CONF_FOLDERPATH = String.format("%s/%s", FINANCE_SCRAPY_ROOT_FOLDERPATH, CONF_FOLDERNAME); 

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Enumeration
	public static enum FinanceAnalysisMode 
	{
		FinanceAnalysis_None(-1), 
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

	public static enum DeleteSQLAccurancyType // Only useful for stock mode
	{
		DeleteSQLAccurancyType_METHOD_ONLY(0), 
		DeleteSQLAccurancyType_COMPANY_ONLY(1), 
		DeleteSQLAccurancyType_METHOD_AND_COMPANY(2);

		private int value = 0;

		private DeleteSQLAccurancyType(int value) 
		{
			this.value = value;
		}

		public static DeleteSQLAccurancyType valueOf(int value) 
		{
			switch (value) 
			{
			case 0:
				return DeleteSQLAccurancyType_METHOD_ONLY;
			case 1:
				return DeleteSQLAccurancyType_COMPANY_ONLY;
			case 2:
				return DeleteSQLAccurancyType_METHOD_AND_COMPANY;
			default:
				return null;
			}
		}

		public int value() 
		{
			return this.value;
		}
	};

	public static enum DefaultConstType // Only useful for stock mode
	{
		DefaultConstType_MARKET_STOCK_SWITCH_CONF_FILENAME(0),
		DefaultConstType_CSV_ROOT_FOLDERPATH(1),
		DefaultConstType_CSV_BACKUP_FOLDERPATH(2),
		DefaultConstType_CSV_RESTORE_FOLDERPATH(3),
		DefaultConstType_MARKET_ALL_CONFIG_FILENAME(4),
		DefaultConstType_STOCK_ALL_CONFIG_FILENAME(5),
		DefaultConstType_DEFAULT_COMPANY_PROFILE_CONF_FOLDERPATH(6),
		DefaultConstType_DEFAULT_STATEMENT_PROFILE_CONF_FOLDERPATH(7);
	
		private int value = 0;

		private DefaultConstType(int value) 
		{
			this.value = value;
		}

		public static DefaultConstType valueOf(int value) 
		{
			switch (value) 
			{
			case 0:
				return DefaultConstType_MARKET_STOCK_SWITCH_CONF_FILENAME;
			case 1:
				return DefaultConstType_CSV_ROOT_FOLDERPATH;
			case 2:
				return DefaultConstType_CSV_BACKUP_FOLDERPATH;
			case 3:
				return DefaultConstType_CSV_RESTORE_FOLDERPATH;
			case 4:
				return DefaultConstType_MARKET_ALL_CONFIG_FILENAME;
			case 5:
				return DefaultConstType_STOCK_ALL_CONFIG_FILENAME;
			case 6:
				return DefaultConstType_DEFAULT_COMPANY_PROFILE_CONF_FOLDERPATH;
			case 7:
				return DefaultConstType_DEFAULT_STATEMENT_PROFILE_CONF_FOLDERPATH;
			default:
				return null;
			}
		}

		public int value() 
		{
			return this.value;
		}
	};
}
