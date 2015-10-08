package com.price.finance_recorder;

import java.io.*;
//import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.regex.*;

public class FinanceRecorder 
{
	static FinanceRecorderMgr stock_recorder_mgr = null;
	public static void main(String args[])
	{
		FinanceRecorderWriter finance_recorder_writer = new FinanceRecorderWriter(FinanceRecorderCmnDef.FinaceDataType.FinaceData_FutureTop10DealersAndLegalPersons);
		finance_recorder_writer.write_to_sql("2015-07", "2015-09");
		System.exit(0);
		
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
