package com.price.finance_recorder;

import java.io.*;
import java.util.*;


public class FinanceRecorder 
{
	static FinanceRecorderMgr finance_recorder_mgr = new FinanceRecorderMgr();

	public static void main(String args[])
	{
		boolean use_multithread = false;
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;

		System.out.println("Parse the parameters......");
		ret = parse_param(args);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
		{
			System.err.println("Fail to write financial data into MySQL");
			System.exit(1);
		}

// Write the financial data into MySQL
		System.out.println("Write financial data into MySQL data......");
		long time_start_millisecond = System.currentTimeMillis();
		ret = write_sql(use_multithread);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
		{
			System.err.println("Fail to write financial data into MySQL");
			System.exit(1);
		}
		long time_end_millisecond = System.currentTimeMillis();
		System.out.println("Write financial data into MySQL data...... Done");
		System.out.printf("######### Time Lapse: %d second(s) #########\n", (int)((time_end_millisecond - time_start_millisecond)));
		System.exit(0);
	}

	public static short parse_param(String args[])
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		ret = finance_recorder_mgr.parse_config_file("history.conf");
		return ret;
	}

	public static short write_sql(boolean use_multithread)
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		if (use_multithread)
			ret = finance_recorder_mgr.write_by_multithread();
		else
			ret = finance_recorder_mgr.write();
		return ret;
	}
}
