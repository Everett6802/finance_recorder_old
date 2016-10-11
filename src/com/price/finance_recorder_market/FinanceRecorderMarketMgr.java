package com.price.finance_recorder_market;

import com.price.finance_recorder_base.*;


public class FinanceRecorderMarketMgr extends FinanceRecorderMgrBase
{
	protected FinanceRecorderDataHandlerInf get_data_handler(){return FinanceRecorderMarketDataHandler.get_data_handler(source_type_index_list);}
}
