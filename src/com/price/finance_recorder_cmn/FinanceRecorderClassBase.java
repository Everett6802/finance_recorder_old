package com.price.finance_recorder_cmn;

public class FinanceRecorderClassBase
{
	protected static String __CLASSNAME__() 
	{
		StackTraceElement ste = Thread.currentThread().getStackTrace()[3];
		return ste.getClassName();
	}

	protected static String __FILE__() 
	{
		StackTraceElement ste = Thread.currentThread().getStackTrace()[3];
		return ste.getFileName();
	}

	protected static String __FUNC__() 
	{
		StackTraceElement ste = Thread.currentThread().getStackTrace()[3];
		return ste.getMethodName();
	}

	protected static int __LINE__() 
	{
		StackTraceElement ste = Thread.currentThread().getStackTrace()[3];
		return ste.getLineNumber();
	}
}