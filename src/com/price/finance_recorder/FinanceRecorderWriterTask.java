package com.price.finance_recorder;

import java.util.concurrent.Callable;


public class FinanceRecorderWriterTask implements Callable<FinanceRecorderWriter>
{
	private FinanceRecorderWriter finance_recorder_writer = null;
	private String start_month_str = null;
	private String end_month_str = null;

    public FinanceRecorderWriterTask(FinanceRecorderWriter finance_recorder_writer_obj, String start_month_string, String end_month_string)
    {
    	finance_recorder_writer = finance_recorder_writer_obj;
    	start_month_str = start_month_string;
    	end_month_str = end_month_string;
    }

	@Override
	public FinanceRecorderWriter call()
	{
		short ret = finance_recorder_writer.write_to_sql(start_month_str, end_month_str);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return null;
		return finance_recorder_writer;
	}

}
