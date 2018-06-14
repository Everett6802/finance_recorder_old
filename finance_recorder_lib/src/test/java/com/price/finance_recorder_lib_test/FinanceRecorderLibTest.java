package com.price.finance_recorder_lib_test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.price.finance_recorder_lib.FinanceRecorder;
import com.price.finance_recorder_lib.FinanceRecorderCmnDef;


public class FinanceRecorderLibTest
{
	
	@Test
	public void testSetMarketMode()
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		ret = FinanceRecorder.set_market_mode();
		assertEquals(ret, FinanceRecorderCmnDef.RET_SUCCESS);
	}
	
	@Test
	public void testSetStockMode()
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		ret = FinanceRecorder.set_stock_mode();
		assertEquals(ret, FinanceRecorderCmnDef.RET_SUCCESS);
	}
}
