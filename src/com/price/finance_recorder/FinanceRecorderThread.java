package com.price.finance_recorder;

public class FinanceRecorderThread extends FinanceRecorderCmnBase implements Runnable 
{
	FinanceRecorderReader reader = null;
	FinanceRecorderWriterInf writer = null;

	FinanceRecorderThread(FinanceRecorderWriterInf writer_obj)
	{
		reader = new FinanceRecorderReader();
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
