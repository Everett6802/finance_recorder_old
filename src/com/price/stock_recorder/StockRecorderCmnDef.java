package com.price.stock_recorder;

import java.io.*;
import java.util.List;


public class StockRecorderCmnDef
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

	public static final String CONF_FOLDERNAME = "conf";
	public static final String DATA_FOLDERNAME = "data";
	public static final String DATA_SPLIT = ":";

	public static final short NOTIFY_GET_DATA = 0;
	public static final int EACH_UPDATE_DATA_AMOUNT = 20;
	private static MsgDumperWrapper msg_dumper = MsgDumperWrapper.get_instance();

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Functions
	private static final String get_code_position(){return String.format("%s:%d", StockRecorderCmnBase.__FILE__(), StockRecorderCmnBase.__LINE__());}

	public static void debug(String msg) 
	{
		msg_dumper.write_debug_msg(String.format("[%s:%d] %s", StockRecorderCmnBase.__FILE__(), StockRecorderCmnBase.__LINE__(), msg));
	}
	public static void info(String msg) 
	{
		msg_dumper.write_info_msg(String.format("[%s:%d] %s", StockRecorderCmnBase.__FILE__(), StockRecorderCmnBase.__LINE__(), msg));
	}
	public static void warn(String msg)
	{
		msg_dumper.write_warn_msg(String.format("[%s:%d] %s", StockRecorderCmnBase.__FILE__(), StockRecorderCmnBase.__LINE__(), msg));
	}
	public static void error(String msg)
	{
		msg_dumper.write_error_msg(String.format("[%s:%d] %s", StockRecorderCmnBase.__FILE__(), StockRecorderCmnBase.__LINE__(), msg));
	}
	
	public static void format_debug(String format, Object... arguments) 
	{
		msg_dumper.write_debug_msg(String.format("[%s:%d] %s", StockRecorderCmnBase.__FILE__(), StockRecorderCmnBase.__LINE__(), String.format(format, arguments)));
	}
	public static void format_info(String format, Object... arguments)
	{
		msg_dumper.write_info_msg(String.format("[%s:%d] %s", StockRecorderCmnBase.__FILE__(), StockRecorderCmnBase.__LINE__(), String.format(format, arguments)));
	}
	public static void format_warn(String format, Object... arguments)
	{
		msg_dumper.write_warn_msg(String.format("[%s:%d] %s", StockRecorderCmnBase.__FILE__(), StockRecorderCmnBase.__LINE__(), String.format(format, arguments)));
	}
	public static void format_error(String format, Object... arguments)
	{
		msg_dumper.write_error_msg(String.format("[%s:%d] %s", StockRecorderCmnBase.__FILE__(), StockRecorderCmnBase.__LINE__(), String.format(format, arguments)));
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

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Interface
	public interface StockObserverInf
	{
		public short notify(short type);
	}
	public interface StockReaderInf
	{
		public short initialize(StockObserverInf observer, String data_filename);
		public short read(List<String> data_list);
		public short deinitialize();
	}
	public interface StockWriterInf
	{
		public short initialize(StockObserverInf observer, String database_name, List<String> file_sql_field_mapping);
//		public short setup_field_mapping(List<String> file_sql_field_mapping);
		public short write(List<String> data_list);
		public short deinitialize();
	}
}
