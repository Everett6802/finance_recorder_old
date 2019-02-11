package com.price.finance_recorder_rest.common;


public class CmnLogger 
{
	private static boolean PRINT_TO_CONSOLE = true;
//	private static RecorderLogger recorder_logger = RecorderLogger.get_instance();
//
	public static void debug(String msg) 
	{
//		recorder_logger.write_debug_msg(String.format("[%s:%d] %s", ClassCmnBase.__FILE__(), ClassCmnBase.__LINE__(), msg));
	}

	public static void info(String msg) 
	{
//		recorder_logger.write_info_msg(String.format("[%s:%d] %s", ClassCmnBase.__FILE__(), ClassCmnBase.__LINE__(), msg));
	}

	public static void warn(String msg) 
	{
//		recorder_logger.write_warn_msg(String.format("[%s:%d] %s", ClassCmnBase.__FILE__(), ClassCmnBase.__LINE__(), msg));
	}

	public static void error(String msg) 
	{
//		recorder_logger.write_error_msg(String.format("[%s:%d] %s", ClassCmnBase.__FILE__(), ClassCmnBase.__LINE__(), msg));
//		if (PRINT_TO_CONSOLE)
//			System.err.println(msg);
	}

	public static void format_debug(String format, Object... arguments) 
	{
//		recorder_logger.write_debug_msg(String.format("[%s:%d] %s", ClassCmnBase.__FILE__(), ClassCmnBase.__LINE__(), String.format(format, arguments)));
	}

	public static void format_info(String format, Object... arguments) 
	{
//		recorder_logger.write_info_msg(String.format("[%s:%d] %s", ClassCmnBase.__FILE__(), ClassCmnBase.__LINE__(), String.format(format, arguments)));
	}

	public static void format_warn(String format, Object... arguments)
	{
//		recorder_logger.write_warn_msg(String.format("[%s:%d] %s", ClassCmnBase.__FILE__(), ClassCmnBase.__LINE__(), String.format(format, arguments)));
	}

	public static void format_error(String format, Object... arguments) 
	{
//		recorder_logger.write_error_msg(String.format("[%s:%d] %s", ClassCmnBase.__FILE__(), ClassCmnBase.__LINE__(), String.format(format, arguments)));
//		if (PRINT_TO_CONSOLE)
//			System.err.println(String.format(format, arguments));
	}

}
