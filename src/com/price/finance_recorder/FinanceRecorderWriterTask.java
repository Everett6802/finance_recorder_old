package com.price.finance_recorder;

import java.util.concurrent.Callable;


public class FinanceRecorderWriterTask implements Callable<Integer>
{
	private FinanceRecorderDataHandler finance_recorder_writer = null;
	private FinanceRecorderCmnClass.TimeRangeCfg time_range_cfg = null;

    public FinanceRecorderWriterTask(FinanceRecorderDataHandler finance_recorder_writer_obj, FinanceRecorderCmnClass.TimeRangeCfg cfg)
    {
    	finance_recorder_writer = finance_recorder_writer_obj;
    	time_range_cfg = cfg;
    }

	@Override
	public Integer call()
	{
		FinanceRecorderCmnDef.format_debug("The thread for accessing [%s %s]", finance_recorder_writer.get_description(), time_range_cfg.toString());
		short ret = finance_recorder_writer.write_to_sql(time_range_cfg, FinanceRecorderCmnDef.DatabaseCreateThreadType.DatabaseCreateThread_Multiple, FinanceRecorderCmnDef.DatabaseEnableBatchType.DatabaseEnableBatch_Yes);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			FinanceRecorderCmnDef.format_error("The thread for accessing [%s %s] FAIL, due to: %s", finance_recorder_writer.get_description(), time_range_cfg.toString(), FinanceRecorderCmnDef.GetErrorDescription(ret));
		return Integer.valueOf(ret);
	}

}
