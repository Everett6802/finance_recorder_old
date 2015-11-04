package com.price.finance_recorder;

import java.util.concurrent.Callable;


public class FinanceRecorderWriterTask implements Callable<Integer>
{
	private FinanceRecorderWriter finance_recorder_writer = null;
	private FinanceRecorderCmnDef.TimeRangeCfg time_range_cfg = null;
//	private int year_start_value; 
//	private int month_start_value;
//	private int year_end_value;
//	private int month_end_value;

    public FinanceRecorderWriterTask(FinanceRecorderWriter finance_recorder_writer_obj, FinanceRecorderCmnDef.TimeRangeCfg cfg)
    {
    	finance_recorder_writer = finance_recorder_writer_obj;
    	time_range_cfg = cfg;
//    	int[] time_list = FinanceRecorderCmnDef.get_start_and_end_time_range(time_range_cfg);
//    	assert time_list != null : "time_list should NOT be NULL";
//    	year_start_value = time_list[0]; 
//    	month_start_value = time_list[1];
//    	year_end_value = time_list[2];
//    	month_end_value = time_list[3];
    }

	@Override
	public Integer call()
	{
		FinanceRecorderCmnDef.format_debug("The thread for accessing [%s %04d%02d:%04d%02d]", finance_recorder_writer.get_description(), time_range_cfg.toString());
		short ret = finance_recorder_writer.write_to_sql(time_range_cfg, FinanceRecorderCmnDef.DatabaseCreateThreadType.DatabaseCreateThread_Multiple);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			FinanceRecorderCmnDef.format_error("The thread for accessing [%s %04d%02d:%04d%02d] FAIL, due to: %s", finance_recorder_writer.get_description(), time_range_cfg.toString(), FinanceRecorderCmnDef.GetErrorDescription(ret));
		return Integer.valueOf(ret);
	}

}
