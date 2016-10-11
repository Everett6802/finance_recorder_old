package com.price.finance_recorder_stock;

import com.price.finance_recorder_base.*;
import com.price.finance_recorder_stock.FinanceRecorderStockDataHandler;


public class FinanceRecorderStockMgr extends FinanceRecorderMgrBase
{
	private FinanceRecorderCompanyGroupSet company_group_set = null;

	protected FinanceRecorderDataHandlerInf get_data_handler(){return FinanceRecorderStockDataHandler.get_data_handler(source_type_index_list, company_group_set);}
}
