package com.price.finance_recorder;


class MarketMgr extends MgrBase
{
//	private DatabaseTimeRange database_time_range = null;
	protected CmnInf.DataHandlerInf get_data_handler()
	{
		assert method_index_list != null : "method_index_list should NOT be null";
		CmnInf.DataHandlerInf data_handler = MarketDataHandler.get_data_handler(method_index_list);
		return data_handler;
	}

	public short initialize()
	{
		short ret = CmnDef.RET_SUCCESS;
		ret = super.initialize();
		if (CmnDef.CheckFailure(ret))
			return ret;
//// Initialize the database time range
//		database_time_range = DatabaseTimeRange.get_instance();
		return CmnDef.RET_SUCCESS;
	}

	public void set_delete_sql_accuracy(CmnDef.DeleteSQLAccurancyType accurancy_type){throw new RuntimeException("Unsupport finance mode !!!");}
}
