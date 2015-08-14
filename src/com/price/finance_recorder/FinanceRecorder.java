package com.price.finance_recorder;

import java.io.*;


public class FinanceRecorder 
{
	static FinanceRecorderMgr stock_recorder_mgr = null;
	public static void main(String args[])
	{
//		java.sql.Date sd;
//		java.util.Date ud;
//
//		ud = new java.util.Date();
//		sd = new java.sql.Date(ud.getTime());
//		System.exit(0);

//		try
//		{
//			Runtime.getRuntime().exec("python /home/super/Projects/stock_recorder_java/auto_grab_volume.py");
//		}
//		catch (Exception e)
//		{
//			
//		}
//		System.out.println("Done");
//		System.exit(0);

		stock_recorder_mgr = new FinanceRecorderMgr();
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;

		System.out.println("Start to record the Stock Information...\n ");
// Initialize the object
		ret = stock_recorder_mgr.initialize();
		if (FinanceRecorderCmnDef.CheckFailure(ret))
		{
			System.err.println("Fail to initialize...");
			System.exit(1);
		}

// Record the stock information
		ret = stock_recorder_mgr.record();
		if (FinanceRecorderCmnDef.CheckFailure(ret))
		{
			System.err.println("Fail to record...");
			System.exit(1);
		}

// De-Initialize the object
		ret = stock_recorder_mgr.deinitialize();
		if (FinanceRecorderCmnDef.CheckFailure(ret))
		{
			System.err.println("Fail to de-initialize...");
			System.exit(1);
		}

		System.out.println("Done");
	}
}
