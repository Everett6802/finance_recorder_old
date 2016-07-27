package com.price.finance_recorder;

import java.util.TreeMap;

import com.price.finance_recorder_cmn.FinanceRecorderCmnDef;


public class FinanceRecorderCSVHandlerMap extends TreeMap<Integer, FinanceRecorderCSVHandler>
{
	private static final int SOURCE_TYPE_MASK = 0xFF << 8;
	public static int get_source_key(int source_type_index)
	{
		if (!FinanceRecorderCmnDef.IS_FINANCE_MARKET_MODE)
		{
			String errmsg = "It's NOT Market mode";
			throw new IllegalStateException(errmsg);
		}
		return source_type_index;
	}
	public static int get_source_key(int source_type_index, String company_code_number)
	{
		if (!FinanceRecorderCmnDef.IS_FINANCE_STOCK_MODE)
		{
			String errmsg = "It's NOT Stock mode";
			throw new IllegalStateException(errmsg);
		}
		int company_code_number_int = Integer.valueOf(company_code_number);
		return (company_code_number_int << 8 | source_type_index);
	}
	public static int get_source_type(int source_key)
	{
		return (source_key & ~SOURCE_TYPE_MASK);
	}
	public static String get_company_code_number(int source_key)
	{
		if (!FinanceRecorderCmnDef.IS_FINANCE_STOCK_MODE)
		{
			String errmsg = "It's NOT Stock mode";
			throw new IllegalStateException(errmsg);
		}
		return String.format("%04d", (source_key & source_key) >> 8);
	}
}
