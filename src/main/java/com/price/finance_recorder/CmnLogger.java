package com.price.finance_recorder;

//import com.price.msg_dumper.*;


class CmnLogger 
{
	private static boolean PRINT_TO_CONSOLE = true;
//	private static RecorderLogger recorder_logger = RecorderLogger.get_instance();
//
	static void debug(String msg) 
	{
//		recorder_logger.write_debug_msg(String.format("[%s:%d] %s", ClassCmnBase.__FILE__(), ClassCmnBase.__LINE__(), msg));
	}

	static void info(String msg) 
	{
//		recorder_logger.write_info_msg(String.format("[%s:%d] %s", ClassCmnBase.__FILE__(), ClassCmnBase.__LINE__(), msg));
	}

	static void warn(String msg) 
	{
//		recorder_logger.write_warn_msg(String.format("[%s:%d] %s", ClassCmnBase.__FILE__(), ClassCmnBase.__LINE__(), msg));
	}

	static void error(String msg) 
	{
//		recorder_logger.write_error_msg(String.format("[%s:%d] %s", ClassCmnBase.__FILE__(), ClassCmnBase.__LINE__(), msg));
//		if (PRINT_TO_CONSOLE)
//			System.err.println(msg);
	}

	static void format_debug(String format, Object... arguments) 
	{
//		recorder_logger.write_debug_msg(String.format("[%s:%d] %s", ClassCmnBase.__FILE__(), ClassCmnBase.__LINE__(), String.format(format, arguments)));
	}

	static void format_info(String format, Object... arguments) 
	{
//		recorder_logger.write_info_msg(String.format("[%s:%d] %s", ClassCmnBase.__FILE__(), ClassCmnBase.__LINE__(), String.format(format, arguments)));
	}

	static void format_warn(String format, Object... arguments)
	{
//		recorder_logger.write_warn_msg(String.format("[%s:%d] %s", ClassCmnBase.__FILE__(), ClassCmnBase.__LINE__(), String.format(format, arguments)));
	}

	static void format_error(String format, Object... arguments) 
	{
//		recorder_logger.write_error_msg(String.format("[%s:%d] %s", ClassCmnBase.__FILE__(), ClassCmnBase.__LINE__(), String.format(format, arguments)));
//		if (PRINT_TO_CONSOLE)
//			System.err.println(String.format(format, arguments));
	}

	static void wait_for_logging() 
	{
//		recorder_logger.deinitialize();
	}
//
//	static class RecorderLogger 
//	{
//		private static String[] facility_name = {"Log", "Com", "Sql", "Remote", "Syslog"};
//		private static short[] severity_arr = {MsgDumperCmnDef.MSG_DUMPER_SEVIRITY_ERROR, MsgDumperCmnDef.MSG_DUMPER_SEVIRITY_WARN};
//		private static short facility = MsgDumperCmnDef.MSG_DUMPER_FACILITY_LOG | MsgDumperCmnDef.MSG_DUMPER_FACILITY_SYSLOG;
//		private boolean show_console = false;
//
//		private RecorderLogger(){}
//		public Object clone() throws CloneNotSupportedException {throw new CloneNotSupportedException();}
//
//		private static RecorderLogger msg_dumper_wrapper = null;
//		public static RecorderLogger get_instance()
//		{
//			if (msg_dumper_wrapper == null)
//				allocate();
//
//			return msg_dumper_wrapper;
//		}
//
//		private static synchronized void allocate() // For thread-safe
//		{
//			if (msg_dumper_wrapper == null)
//			{
//				msg_dumper_wrapper = new RecorderLogger();
//				msg_dumper_wrapper.initialize();
//			} 
//		}
//
//		private void initialize()
//		{
//			short ret = MsgDumperCmnDef.MSG_DUMPER_SUCCESS;
//
//			if (show_console)
//				System.out.printf("MsgDumper API version: (%s)\n", MsgDumper.get_version());
//	// Set severity
//			short flags = 0x1;
//			for (int i = 0, severity_cnt = 0 ; i < facility_name.length ; i++)
//			{
//				if ((flags & facility) != 0)
//				{
//					if(show_console)
//						System.out.printf("MsgDumper Set severity of facility[%s] to :%d\n", facility_name[i], severity_arr[severity_cnt]);
//					ret = MsgDumper.set_severity(severity_arr[severity_cnt], flags);
//					if (MsgDumperCmnDef.CheckMsgDumperFailure(ret))
//					{
//						String errmsg = String.format("MsgDumper set_severity() fails, due to %d", ret);
//						if(show_console)
//							System.err.println(errmsg);
//						System.exit(1);
//					}
//					severity_cnt++;
//				}
//				flags <<= 1;
//			}
//	// Set facility
//			if(show_console)
//				System.out.printf("MsgDumper Set facility to :%d\n", facility);
//			ret = MsgDumper.set_facility(facility);
//			if (MsgDumperCmnDef.CheckMsgDumperFailure(ret))
//			{
//				String errmsg = String.format("MsgDumper set_facility() fails, due to %d", ret);
//				if(show_console)
//					System.err.println(errmsg);
//				System.exit(1);
//			}
//	// Initialize the library
//			if(show_console)
//				System.out.printf("MsgDumper Initialize the library\n");
//			ret = MsgDumper.initialize();
//			if (MsgDumperCmnDef.CheckMsgDumperFailure(ret))
//			{
//				String errmsg = String.format("MsgDumper initialize() fails, due to: %d, reason: %s", ret, MsgDumper.get_error_description());
//				if(show_console)
//					System.err.println(errmsg);
//				System.exit(1);
//			}
//		}
//
//		public void deinitialize()
//		{
//			MsgDumper.deinitialize();
//		}
//
//	//Write the Error message
//		void write_error_msg(String msg)
//		{
//			MsgDumper.write_msg(MsgDumperCmnDef.MSG_DUMPER_SEVIRITY_ERROR, msg);
//		}
//
//	//Write the Warning message
//		void write_warn_msg(String msg)
//		{
//			MsgDumper.write_msg(MsgDumperCmnDef.MSG_DUMPER_SEVIRITY_WARN, msg);
//		}
//
//	//Write the Info message
//		void write_info_msg(String msg)
//		{
//			MsgDumper.write_msg(MsgDumperCmnDef.MSG_DUMPER_SEVIRITY_INFO, msg);
//		}
//
//	//Write the Debug message
//		void write_debug_msg(String msg)
//		{
//			MsgDumper.write_msg(MsgDumperCmnDef.MSG_DUMPER_SEVIRITY_DEBUG, msg);
//		}
//	}
}
