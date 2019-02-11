package com.price.finance_recorder_rest.common;

import com.price.finance_recorder_rest.common.CmnDef.FinanceMethod;

public class CmnDBDef 
{
	public static final String[] STOCK_PRICE_AND_VOLUME_TABLE_FIELD_NAME_DEFINITION = new String[] 
	{
		"trade_date", // 日期
		"trade_volume", // 成交股數
		"turnover_in_value", // 成交金額
		"open", // 開盤價
		"high", // 最高價
		"low", // 最低價
		"close", // 收盤價
		"net_change", // 漲跌價差
		"number_of_transactions", // 成交筆數
	};
	public static final String[] STOCK_PRICE_AND_VOLUME_TABLE_FIELD_TYPE_DEFINITION = new String[] 
	{
		"DATE NOT NULL PRIMARY KEY", // 日期
		"BIGINT", // 成交股數
		"BIGINT", // 成交金額
		"FLOAT", // 開盤價
		"FLOAT", // 最高價
		"FLOAT", // 最低價
		"FLOAT", // 收盤價
		"FLOAT", // 漲跌價差
		"BIGINT", // 成交筆數
	};
	
	public static String[] get_table_field_name_definition(FinanceMethod finance_method)
	{
		switch (finance_method)
		{
		case FinanceMethod_StockPriceAndVolume :
			return STOCK_PRICE_AND_VOLUME_TABLE_FIELD_NAME_DEFINITION;
		default :
		{
			throw new IllegalStateException(String.format("Unknown finance method: %d", finance_method.ordinal()));
		}
		}
	}

	public static String[] get_table_field_type_definition(FinanceMethod finance_method)
	{
		switch (finance_method)
		{
		case FinanceMethod_StockPriceAndVolume :
			return STOCK_PRICE_AND_VOLUME_TABLE_FIELD_TYPE_DEFINITION;
		default :
		{
			throw new IllegalStateException(String.format("Unknown finance method: %d", finance_method.ordinal()));
		}
		}
	}
}
