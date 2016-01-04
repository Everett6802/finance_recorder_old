package com.price.finance_recorder;

import com.price.msg_dumper.*;


public class FinanceRecorderLogger 
{
	private static String[] facility_name = {"Log", "Com", "Sql", "Remote", "Syslog"};
	private static short[] severity_arr = {MsgDumperCmnDef.MSG_DUMPER_SEVIRITY_ERROR, MsgDumperCmnDef.MSG_DUMPER_SEVIRITY_WARN};
	private static short facility = MsgDumperCmnDef.MSG_DUMPER_FACILITY_LOG | MsgDumperCmnDef.MSG_DUMPER_FACILITY_SYSLOG;
	private boolean show_console = false;

	private FinanceRecorderLogger(){}
	public Object clone() throws CloneNotSupportedException {throw new CloneNotSupportedException();}

	private static FinanceRecorderLogger msg_dumper_wrapper = null;
	public static FinanceRecorderLogger get_instance()
	{
		if (msg_dumper_wrapper == null)
			allocate();

		return msg_dumper_wrapper;
	}

	private static synchronized void allocate() // For thread-safe
	{
		if (msg_dumper_wrapper == null)
		{
			msg_dumper_wrapper = new FinanceRecorderLogger();
			msg_dumper_wrapper.initialize();
		} 
	}

	private void initialize()
	{
		short ret = MsgDumperCmnDef.MSG_DUMPER_SUCCESS;

		if (show_console)
			System.out.printf("MsgDumper API version: (%s)\n", MsgDumper.get_version());
// Set severity
		short flags = 0x1;
		for (int i = 0, severity_cnt = 0 ; i < facility_name.length ; i++)
		{
			if ((flags & facility) != 0)
			{
				if(show_console)
					System.out.printf("MsgDumper Set severity of facility[%s] to :%d\n", facility_name[i], severity_arr[severity_cnt]);
				ret = MsgDumper.set_severity(severity_arr[severity_cnt], flags);
				if (MsgDumperCmnDef.CheckMsgDumperFailure(ret))
				{
					String errmsg = String.format("MsgDumper set_severity() fails, due to %d", ret);
					if(show_console)
						System.err.println(errmsg);
					System.exit(1);
				}
				severity_cnt++;
			}
			flags <<= 1;
		}
// Set facility
		if(show_console)
			System.out.printf("MsgDumper Set facility to :%d\n", facility);
		ret = MsgDumper.set_facility(facility);
		if (MsgDumperCmnDef.CheckMsgDumperFailure(ret))
		{
			String errmsg = String.format("MsgDumper set_facility() fails, due to %d", ret);
			if(show_console)
				System.err.println(errmsg);
			System.exit(1);
		}
// Initialize the library
		if(show_console)
			System.out.printf("MsgDumper Initialize the library\n");
		ret = MsgDumper.initialize();
		if (MsgDumperCmnDef.CheckMsgDumperFailure(ret))
		{
			String errmsg = String.format("MsgDumper initialize() fails, due to: %d, reason: %s", ret, MsgDumper.get_error_description());
			if(show_console)
				System.err.println(errmsg);
			System.exit(1);
		}
	}

	public void deinitialize()
	{
		MsgDumper.deinitialize();
	}

//Write the Error message
	public void write_error_msg(String msg)
	{
		MsgDumper.write_msg(MsgDumperCmnDef.MSG_DUMPER_SEVIRITY_ERROR, msg);
	}

//Write the Warning message
	public void write_warn_msg(String msg)
	{
		MsgDumper.write_msg(MsgDumperCmnDef.MSG_DUMPER_SEVIRITY_WARN, msg);
	}

//Write the Info message
	public void write_info_msg(String msg)
	{
		MsgDumper.write_msg(MsgDumperCmnDef.MSG_DUMPER_SEVIRITY_INFO, msg);
	}

//Write the Debug message
	public void write_debug_msg(String msg)
	{
		MsgDumper.write_msg(MsgDumperCmnDef.MSG_DUMPER_SEVIRITY_DEBUG, msg);
	}
}
