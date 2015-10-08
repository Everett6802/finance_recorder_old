package com.price.finance_recorder;

import java.io.*;
import java.util.List;


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
	public static final short RET_FAILURE_MYSQL = 11;

	public static boolean CheckSuccess(short x) {return (x == RET_SUCCESS ? true : false);}
	public static boolean CheckFailure(short x) {return !CheckSuccess(x);}

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
		"Failure MySQL"
	};

	public static String GetErrorDescription(short error_code)
	{
		return RetDescription[error_code];
	}

//	public static final String CONF_FOLDERNAME = "conf";
	public static final String DATA_FOLDER_NAME = "/var/tmp/finance";
	public static final String DATA_SPLIT = ":";

	public static enum FinaceDataType{
		FinaceData_FutureTop10DealersAndLegalPersons,
		FinaceData_FutureTop3LegalPersonsOpenInterest,
		FinaceData_FtockTop3LegalPersonsNetBuyOrSell
	};
	public static final String[] FINANCE_DATA_NAME_LIST = new String[]{
		"future_top10_dealers_and_legal_persons",
		"future_top3_legal_persons_open_interest",
		"stock_top3_legal_persons_net_buy_or_sell"
	};
	public static final String[] FINANCE_DATA_DESCRIPTION_LIST = new String[]{
		"十大交易人及特定法人期貨資訊",
		"三大法人期貨留倉淨額",
		"三大法人現貨買賣超"
    };
	private static final String[] future_top10_dealers_and_legal_persons_field_defintion = new String[]
	{
		"日期 date",
		"臺股期貨_到期月份_買方_前五大交易人合計_部位數 int",
		"臺股期貨_到期月份_買方_前五大交易人合計_百分比 float",
		"臺股期貨_到期月份_買方_前十大交易人合計_部位數 int",
		"臺股期貨_到期月份_買方_前十大交易人合計_百分比 float",
		"臺股期貨_到期月份_賣方_前五大交易人合計_部位數 int",
		"臺股期貨_到期月份_賣方_前五大交易人合計_百分比 float",
		"臺股期貨_到期月份_賣方_前十大交易人合計_部位數 int",
		"臺股期貨_到期月份_賣方_前十大交易人合計_百分比 float",
		"臺股期貨_到期月份_全市場未沖銷部位數 int",
		"臺股期貨_所有契約_買方_前五大交易人合計_部位數 int",
		"臺股期貨_所有契約_買方_前五大交易人合計_百分比 float",
		"臺股期貨_所有契約_買方_前十大交易人合計_部位數 int",
		"臺股期貨_所有契約_買方_前十大交易人合計_百分比 float",
		"臺股期貨_所有契約_賣方_前五大交易人合計_部位數 int",
		"臺股期貨_所有契約_賣方_前五大交易人合計_百分比 float",
		"臺股期貨_所有契約_賣方_前十大交易人合計_部位數 int",
		"臺股期貨_所有契約_賣方_前十大交易人合計_百分比 float",
		"臺股期貨_所有契約_全市場未沖銷部位數 int"
	};
	private static final String[] future_top3_legal_persons_open_interest_field_defintion = new String[]
	{
		"日期 date",
		"自營商_多方_口數 int",
		"自營商_多方_契約金額 int",
		"自營商_空方_口數 int",
		"自營商_空方_契約金額 int",
		"自營商_多空淨額_口數 int",
		"自營商_多空淨額_契約金額 int",
		"投信_多方_口數 int",
		"投信_多方_契約金額 int",
		"投信_空方_口數 int",
		"投信_空方_契約金額 int",
		"投信_多空淨額_口數 int",
		"投信_多空淨額_契約金額 int",
		"外資_多方_口數 int",
		"外資_多方_契約金額 int",
		"外資_空方_口數 int",
		"外資_空方_契約金額 int",
		"外資_多空淨額_口數 int",
		"外資_多空淨額_契約金額 int",
	};
	private static final String[] stock_top3_legal_persons_net_buy_or_sell_field_defintion = new String[]
	{
		"日期 date",
		"自營商(自行買賣)_買進金額 int",
		"自營商(自行買賣)_賣出金額 int",
		"自營商(自行買賣)_買賣差額 int",
		"自營商(避險)_買進金額 int",
		"自營商(避險)_賣出金額 int",
		"自營商(避險)_買賣差額 int",
		"投信_買進金額 int",
		"投信_賣出金額 int",
		"投信_買賣差額 int",
		"外資及陸資_買進金額 int",
		"外資及陸資_賣出金額 int",
		"外資及陸資_買賣差額 int"
	};
	public static final String[] FINANCE_DATA_SQL_FIELD_LIST = new String[]{
		transform_array_to_sql_string(future_top10_dealers_and_legal_persons_field_defintion),
		transform_array_to_sql_string(future_top3_legal_persons_open_interest_field_defintion),
		transform_array_to_sql_string(stock_top3_legal_persons_net_buy_or_sell_field_defintion)
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
			if (sql_string == null)
				sql_string = string;
			else
				sql_string += String.format(",%s", string);
		}
		return sql_string;
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
