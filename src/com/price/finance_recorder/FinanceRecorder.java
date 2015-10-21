package com.price.finance_recorder;

import java.io.*;
import java.util.*;


public class FinanceRecorder 
{
	static FinanceRecorderMgr finance_recorder_mgr = new FinanceRecorderMgr();

	public static void main(String args[])
	{
		boolean use_multithread = false;
		boolean remove_old = false;
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;

		System.out.println("Parse the parameters......");
		int index = 0;
		int index_offset;
		for (String arg : args)
		{
//			if (arg.startsWith(prefix))
		}
//		ret = parse_param(args);
//		if (FinanceRecorderCmnDef.CheckFailure(ret))
//		{
//			System.err.println("Fail to write financial data into MySQL");
//			System.exit(1);
//		}

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

//	public static short parse_param(String args[])
//	{
//		
//		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
//		ret = finance_recorder_mgr.parse_config_file("history.conf");
//		return ret;
//	}

	private void show_usage()
	{
		System.out.println("====================== Usage ======================");
		System.out.println("-h|--help\nDescription: The usage");
		System.out.println("-r|--remove\nDescription: Remove some MySQL database(s)");
		System.out.println("  Format: 1;2;3");
		System.out.println("-s|--source\nDescription: Type of CSV date file\nDefault: All types");
		for (int index = 0 ; index < FinanceRecorderCmnDef.FINANCE_DATA_DESCRIPTION_LIST.length ; index++)
			System.out.printf("  %d: %s\n", index, FinanceRecorderCmnDef.FINANCE_DATA_DESCRIPTION_LIST[index]);
		System.out.println("-t|--time\nTime: The time range of the CSV data file\nDefault: Current month");
		System.out.println("  Format 1 (start_time): 2015-09");
		System.out.println("  Format 2 (start_time end_time): 2015-01 2015-09");
	    System.out.println("--remove_all\nDescription: Remove all the MySQL databases");
		System.out.println("--multiple_thread\nDescription: Write into MySQL database by using multiple threads");
		System.out.println("===================================================");
	}

	private static short write_sql(boolean use_multithread)
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		if (use_multithread)
			ret = finance_recorder_mgr.write_by_multithread();
		else
			ret = finance_recorder_mgr.write();
		return ret;
	}
}
