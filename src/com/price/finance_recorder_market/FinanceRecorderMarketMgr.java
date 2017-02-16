package com.price.finance_recorder_market;

import com.price.finance_recorder_base.*;
import com.price.finance_recorder_cmn.FinanceRecorderCmnDef;


public class FinanceRecorderMarketMgr extends FinanceRecorderMgrBase
{
	private FinanceRecorderDatabaseTimeRange database_time_range = null;

	protected FinanceRecorderDataHandlerInf get_data_handler()
	{
		FinanceRecorderDataHandlerInf data_handler = FinanceRecorderMarketDataHandler.get_data_handler(source_type_index_list);
		return data_handler;
	}

	public void set_delete_sql_accuracy(FinanceRecorderCmnDef.DeleteSQLAccurancyType accurancy_type){throw new RuntimeException("Unsupport finance mode !!!");}

	public short initialize()
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		ret = super.initialize();
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;
// Initialize the database time range
		database_time_range = FinanceRecorderDatabaseTimeRange.get_instance();

		return FinanceRecorderCmnDef.RET_SUCCESS;
	}
}
