package com.price.finance_recorder;

public class FinanceRecorderThread extends FinanceRecorderCmnBase implements Runnable 
{
	FinanceRecorderCSVReader reader = null;
	FinanceRecorderWriterInf writer = null;

	FinanceRecorderThread(FinanceRecorderWriterInf writer_obj)
	{
		writer = writer_obj;
	}

	short Initialize()
	{
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	@Override
	public void run() 
	{
	}

}
