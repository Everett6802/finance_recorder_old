package com.price.finance_recorder;

import java.util.concurrent.Callable;


public class FinanceRecorderWriterTask implements Callable<Integer>
{
	private FinanceRecorderWriter finance_recorder_writer = null;
	private int year_start_value; 
	private int month_start_value;
	private int year_end_value;
	private int month_end_value;

    public FinanceRecorderWriterTask(FinanceRecorderWriter finance_recorder_writer_obj, int year_start, int month_start, int year_end, int month_end)
    {
    	finance_recorder_writer = finance_recorder_writer_obj;
    	year_start_value = year_start; 
    	month_start_value = month_start;
    	year_end_value = year_end;
    	month_end_value = month_end;
    }

	@Override
	public Integer call()
	{
		FinanceRecorderCmnDef.format_debug("The thread for accessing [%s %04d%02d:%04d%02d]", finance_recorder_writer.get_description(), year_start_value, month_start_value, year_end_value, month_end_value);
		short ret = finance_recorder_writer.write_to_sql(year_start_value, month_start_value, year_end_value, month_end_value, FinanceRecorderCmnDef.DatabaseCreateThreadType.DatabaseCreateThread_Multiple);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			FinanceRecorderCmnDef.format_error("The thread for accessing [%s %04d%02d:%04d%02d] FAIL, due to: %s", finance_recorder_writer.get_description(), year_start_value, month_start_value, year_end_value, month_end_value, FinanceRecorderCmnDef.GetErrorDescription(ret));
		return Integer.valueOf(ret);
	}

}
