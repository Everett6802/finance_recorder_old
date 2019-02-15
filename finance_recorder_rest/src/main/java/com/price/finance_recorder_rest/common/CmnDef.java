package com.price.finance_recorder_rest.common;


public class CmnDef
{
	public static String URL_REF = "";

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

	public static boolean CheckSuccess(short x)
	{
		return (x == RET_SUCCESS ? true : false);
	}

	public static boolean CheckFailure(short x)
	{
		return !CheckSuccess(x);
	}

	public static boolean CheckFailureWarnProcessContinue(short x)
	{
		return (x == RET_FAILURE_WARN_PROCESS_CONTINUE ? true : false);
	}
	public static boolean CheckFailureNotFound(short x)
	{
		return (x == RET_FAILURE_NOT_FOUND ? true : false);
	}

	public static boolean CheckMySQLFailureUnknownDatabase(short x)
	{
		return (x == RET_FAILURE_MYSQL_UNKNOWN_DATABASE ? true : false);
	}

	private static final String[] WarnRetDescription = new String[]{"Warning Base", "Warning Index Duplicate", "Warning Index Ignore", "Warning Process Continue"};

	private static final String[] ErrorRetDescription = new String[]{"Failure Base", "Failure Unknown", "Failure Invalid Argument", "Failure Invalid Pointer", "Failure Insufficient Memory", "Failure Incorrect Operation", "Failure Not Found", "Failure Incorrect Config", "Failure Handle Thread", "Failure Incorrect Path", "Failure IO Operation", "Failure Unexpected Value"};

	private static final String[] SQLErrorRetDescription = new String[]{"SQL Failure Base", "SQL Failure Common", "SQL Failure Unknown Database", "SQL Failure No Driver", "SQL Failure Execute Command", "SQL Failure Database Already Exist", "SQL Failure Data Not Consistent"};

	public static String GetErrorDescription(short error_code)
	{
		if (error_code >= RET_FAILURE_MYSQL_BASE)
			return SQLErrorRetDescription[error_code - RET_FAILURE_MYSQL_BASE];
		else
		{
			if ((error_code >= RET_FAILURE_BASE) && (error_code < RET_FAILURE_MYSQL_BASE))
				return ErrorRetDescription[error_code - RET_FAILURE_BASE];
			else
			{
				if (error_code >= RET_FAILURE_WARN_BASE)
					return WarnRetDescription[error_code - RET_FAILURE_WARN_BASE];
				else
					return "Success";
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Constants
	static final String DATASET_MARKET_FOLDERNAME = "market";
	static final String DATASET_STOCK_FOLDERNAME = "stock";
	static final String CONF_FOLDERNAME = "conf";
	static final String DATA_FOLDERNAME = "data";

	public static final String FINANCE_DATASET_PARENT_FOLDER_PATH = "/var/local";
	public static final String FINANCE_DATASET_FOLDER_NAME = "finance_dataset";
	public static final String FINANCE_DATASET_FOLDER_PATH = String.format("%s/%s", FINANCE_DATASET_PARENT_FOLDER_PATH, FINANCE_DATASET_FOLDER_NAME);
	public static final String COMPANY_PROFILE_CONF_FILENAME = ".company_profile.conf";
	public static final String COMPANY_GROUP_CONF_FILENAME = ".company_group.conf";
	public static final int FINANCE_DATA_START_INDEX = 0;
	public static final int FINANCE_DATA_LIMIT = 20;
//	public static final String FINANCE_DATA_START_INDEX_STRING = String.format("%d", FINANCE_DATA_START_INDEX);
//	public static final String FINANCE_DATA_LIMIT_STRING = String.format("%d", FINANCE_DATA_LIMIT);

	public static final String COMMA_DATA_SPLIT = ",";
	public static final String SPACE_DATA_SPLIT = " ";

// MySQL table name should be identical to the csv file name
	public static final String[] FINANCE_DATA_NAME_LIST = new String[]
	{
// Market Start
		"taiwan_weighted_index_and_volume", //
		"option_put_call_ratio", //
		"tfe_open_interest", //
//		"stock_top3_legal_persons_net_buy_or_sell", //
//		"stock_margin_trading_and_short_selling", //
//		"future_and_option_top3_legal_persons_open_interest", //
//		"future_or_option_top3_legal_persons_open_interest", //
//		"option_top3_legal_persons_buy_and_sell_option_open_interest", //
//		"future_top10_dealers_and_legal_persons", //
// Market End
///////////////////////////////////////////////////////////
// Stock Start
		"revenue",
		"profitability",
		"cashflow_statement",
		"dividend",
		"institutional_investor_net_buy_sell",
		"stock_price_and_volume",
//		"company_depository_shareholder_distribution_table", //
//		"stock_price_and_volume", //
//		"top3_legal_persons_stock_net_buy_and_sell_summary", //
// Stock End
	};
	public static final String[] FINANCE_METHOD_DESCRIPTION_LIST = new String[]
	{
// Market Start
		"臺股指數及成交量", //
		"臺指選擇權賣權買權比", //
		"台指期未平倉(大額近月、法人所有月)",
//			"三大法人現貨買賣超", //
//			"現貨融資融券餘額", //
//			"三大法人期貨和選擇權留倉淨額", //
//			"三大法人期貨或選擇權留倉淨額", //
//			"三大法人選擇權買賣權留倉淨額", //
//			"十大交易人及特定法人期貨資訊", //
			// Market End
///////////////////////////////////////////////////////////
// Stock Start
		"營收盈餘",
		"獲利能力",
		"現金流量表",
		"股利政策",
		"三大法人買賣超",
		"個股股價及成交量",
//			"個股集保戶股權分散表", //
//			"個股日股價及成交量", //
//			"三大法人個股買賣超日報", //
// Stock End
	};

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// enumeration

// CAUTION: The FinanceMethod enum can NOT exploit ordinal() to access the array element,
// since some enumeration such as FinanceMethod_Unknown, FinanceMethod_MarketStart, FinanceMethod_MarketEnd,... exist
// Exploit value() instead
	public static enum FinanceMethod
	{
		FinanceMethod_Unknown(-1),
		// //////////////////////////////////////////////////////////////////////////////////////////////
		// Market data source
		// Keep in mind to update the value at the right time
		FinanceMethod_MarketStart(0),
		FinanceMethod_TaiwanWeightedIndexAndVolume(0),
		FinanceMethod_OptionPutCallRatio(1),
		FinanceMethod_TfeOpenInterest(2),
//		FinanceMethod_StockTop3LegalPersonsNetBuyOrSell(1),
//		FinanceMethod_StockMarginTradingAndShortSelling(2),
//		FinanceMethod_FutureAndOptionTop3LegalPersonsOpenInterest(3),
//		FinanceMethod_FutureOrOptionTop3LegalPersonsOpenInterest(4),
//		FinanceMethod_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest(5),
//		FinanceMethod_FutureTop10DealersAndLegalPersons(7),
		FinanceMethod_MarketEnd(3),
		// Keep in mind to update the value at the right time, semi-open
		// interval

		// //////////////////////////////////////////////////////////////////////////////////////////////
		// Stock data source
		// Keep in mind to update the value at the right time
		FinanceMethod_StockStart(3),
//		FinanceMethod_StockPriceAndVolume(8),
		FinanceMethod_Revenue(3),
		FinanceMethod_Profitability(4),
		FinanceMethod_CashflowStatement(5),
		FinanceMethod_Dividend(6),
		FinanceMethod_InstitutionalInvestorNetBuySell(7),
		FinanceMethod_StockPriceAndVolume(8),
		FinanceMethod_StockEnd(9);
		// Keep in mind to update the value at the right time, semi-open
		// interval

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
				return FinanceMethod_TaiwanWeightedIndexAndVolume;
			case 1:
				return FinanceMethod_OptionPutCallRatio;
			case 2:
				return FinanceMethod_TfeOpenInterest;
// Market End
///////////////////////////////////////////////////////////////////
//Stock Start
			case 3:
				return FinanceMethod_Revenue;
			case 4:
				return FinanceMethod_Profitability;
			case 5:
				return FinanceMethod_CashflowStatement;
			case 6:
				return FinanceMethod_Dividend;
			case 7:
				return FinanceMethod_InstitutionalInvestorNetBuySell;
			case 8:
				return FinanceMethod_StockPriceAndVolume;
// Stock End
			default :
				return null;
			}
		}

		public int value()
		{
			return this.value;
		}
	};
}
