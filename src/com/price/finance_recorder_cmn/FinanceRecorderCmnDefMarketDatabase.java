package com.price.finance_recorder_cmn;

public class FinanceRecorderCmnDefMarketDatabase
{
	static final String[] STOCK_EXCHANGE_AND_VALUE_FIELD_DEFINITION = new String[]
	{
		"date", // 日期
		"value1", // 成交股數
		"value2", // 成交金額
		"value3", // 成交筆數
		"value4", // 發行量加權股價指數
		"value5", // 漲跌點數
	};
	static final String[] STOCK_EXCHANGE_AND_VALUE_FIELD_TYPE_DEFINITION = new String[]
	{
		"DATE NOT NULL PRIMARY KEY", // 日期
		"BIGINT", // 成交股數
		"BIGINT", // 成交金額
		"INT", // 成交筆數
		"FLOAT", // 發行量加權股價指數
		"FLOAT", // 漲跌點數
	};
	static final String[] STOCK_TOP3_LEGAL_PERSONS_NET_BUY_OR_SELL_FIELD_DEFINITION = new String[]
	{
		"date", // 日期
		"value1", // 自營商(自行買賣)_買進金額
		"value2", // 自營商(自行買賣)_賣出金額
		"value3", // 自營商(自行買賣)_買賣差額
		"value4", // 自營商(避險)_買進金額
		"value5", // 自營商(避險)_賣出金額
		"value6", // 自營商(避險)_買賣差額
		"value7", // 投信_買進金額
		"value8", // 投信_賣出金額
		"value9", // 投信_買賣差額
		"value10", // 外資及陸資_買進金額
		"value11", // 外資及陸資_賣出金額
		"value12", // 外資及陸資_買賣差額
	};
	static final String[] STOCK_TOP3_LEGAL_PERSONS_NET_BUY_OR_SELL_FIELD_TYPE_DEFINITION = new String[]
	{
		"DATE NOT NULL PRIMARY KEY", // 日期
		"BIGINT", // 自營商(自行買賣)_買進金額
		"BIGINT", // 自營商(自行買賣)_賣出金額
		"BIGINT", // 自營商(自行買賣)_買賣差額
		"BIGINT", // 自營商(避險)_買進金額
		"BIGINT", // 自營商(避險)_賣出金額
		"BIGINT", // 自營商(避險)_買賣差額
		"BIGINT", // 投信_買進金額
		"BIGINT", // 投信_賣出金額
		"BIGINT", // 投信_買賣差額
		"BIGINT", // 外資及陸資_買進金額
		"BIGINT", // 外資及陸資_賣出金額
		"BIGINT", // 外資及陸資_買賣差額
	};
	static final String[] STOCK_MARGIN_TRADING_AND_SHORT_SELLING_FIELD_DEFINITION = new String[]
	{
		"date", // 日期
		"value1", // 融資(交易單位)_買進
		"value2", // 融資(交易單位)_賣出
		"value3", // 融資(交易單位)_現金(券)償還
		"value4", // 融資(交易單位)_前日餘額
		"value5", // 融資(交易單位)_今日餘額
		"value6", // 融券(交易單位)_買進
		"value7", // 融券(交易單位)_賣出
		"value8", // 融券(交易單位)_現金(券)償還
		"value9", // 融券(交易單位)_前日餘額
		"value10", // 融券(交易單位)_今日餘額
		"value11", // 融資金額(仟元)_買進
		"value12", // 融資金額(仟元)_賣出
		"value13", // 融資金額(仟元)_現金(券)償還
		"value14", // 融資金額(仟元)_前日餘額
		"value15", // 融資金額(仟元)_今日餘額
	};
	static final String[] STOCK_MARGIN_TRADING_AND_SHORT_SELLING_FIELD_TYPE_DEFINITION = new String[]
	{
		"DATE NOT NULL PRIMARY KEY", // 日期
		"INT", // 融資(交易單位)_買進
		"INT", // 融資(交易單位)_賣出
		"INT", // 融資(交易單位)_現金(券)償還
		"INT", // 融資(交易單位)_前日餘額
		"INT", // 融資(交易單位)_今日餘額
		"INT", // 融券(交易單位)_買進
		"INT", // 融券(交易單位)_賣出
		"INT", // 融券(交易單位)_現金(券)償還
		"INT", // 融券(交易單位)_前日餘額
		"INT", // 融券(交易單位)_今日餘額
		"INT", // 融資金額(仟元)_買進
		"INT", // 融資金額(仟元)_賣出
		"INT", // 融資金額(仟元)_現金(券)償還
		"BIGINT", // 融資金額(仟元)_前日餘額
		"BIGINT", // 融資金額(仟元)_今日餘額
	};
	static final String[] FUTURE_AND_OPTION_TOP3_LEGAL_PERSONS_OPEN_INTEREST_FIELD_DEFINITION = new String[]
	{
		"date", // 日期
		"value1", // 自營商_多方_口數 int",
		"value2", // 自營商_多方_契約金額 int",
		"value3", // 自營商_空方_口數 int",
		"value4", // 自營商_空方_契約金額 int",
		"value5", // 自營商_多空淨額_口數 int",
		"value6", // 自營商_多空淨額_契約金額 int",
		"value7", // 投信_多方_口數 int",
		"value8", // 投信_多方_契約金額 int",
		"value9", // 投信_空方_口數 int",
		"value10", // 投信_空方_契約金額 int",
		"value11", // 投信_多空淨額_口數 int",
		"value12", // 投信_多空淨額_契約金額 int",
		"value13", // 外資_多方_口數 int",
		"value14", // 外資_多方_契約金額 int",
		"value15", // 外資_空方_口數 int",
		"value16", // 外資_空方_契約金額 int",
		"value17", // 外資_多空淨額_口數 int",
		"value18", // 外資_多空淨額_契約金額 int",
	};
	static final String[] FUTURE_AND_OPTION_TOP3_LEGAL_PERSONS_OPEN_INTEREST_FIELD_TYPE_DEFINITION = new String[]
	{
		"DATE NOT NULL PRIMARY KEY", // 日期
		"INT", // 自營商_多方_口數 int",
		"INT", // 自營商_多方_契約金額 int",
		"INT", // 自營商_空方_口數 int",
		"INT", // 自營商_空方_契約金額 int",
		"INT", // 自營商_多空淨額_口數 int",
		"INT", // 自營商_多空淨額_契約金額 int",
		"INT", // 投信_多方_口數 int",
		"INT", // 投信_多方_契約金額 int",
		"INT", // 投信_空方_口數 int",
		"INT", // 投信_空方_契約金額 int",
		"INT", // 投信_多空淨額_口數 int",
		"INT", // 投信_多空淨額_契約金額 int",
		"INT", // 外資_多方_口數 int",
		"INT", // 外資_多方_契約金額 int",
		"INT", // 外資_空方_口數 int",
		"INT", // 外資_空方_契約金額 int",
		"INT", // 外資_多空淨額_口數 int",
		"INT", // 外資_多空淨額_契約金額 int",
	};
	static final String[] FUTURE_OR_OPTION_TOP3_LEGAL_PERSONS_OPEN_INTEREST_FIELD_DEFINITION = new String[]
	{
		"date", // 日期
		"value1", // 自營商_多方_口數_期貨 int",
		"value2", // 自營商_多方_口數_選擇權 int",
		"value3", // 自營商_多方_契約金額_期貨 int",
		"value4", // 自營商_多方_契約金額_選擇權 int",
		"value5", // 自營商_空方_口數_期貨 int",
		"value6", // 自營商_空方_口數_選擇權 int",
		"value7", // 自營商_空方_契約金額_期貨 int",
		"value8", // 自營商_空方_契約金額_選擇權 int",
		"value9", // 自營商_多空淨額_口數_期貨 int",
		"value10", // 自營商_多空淨額_口數_選擇權 int",
		"value11", // 自營商_多空淨額_契約金額_期貨 int",
		"value12", // 自營商_多空淨額_契約金額_選擇權 int",
		"value13", // 投信_多方_口數_期貨 int",
		"value14", // 投信_多方_口數_選擇權 int",
		"value15", // 投信_多方_契約金額_期貨 int",
		"value16", // 投信_多方_契約金額_選擇權 int",
		"value17", // 投信_空方_口數_期貨 int",
		"value18", // 投信_空方_口數_選擇權 int",
		"value19", // 投信_空方_契約金額_期貨 int",
		"value20", // 投信_空方_契約金額_選擇權 int",
		"value21", // 投信_多空淨額_口數_期貨 int",
		"value22", // 投信_多空淨額_口數_選擇權 int",
		"value23", // 投信_多空淨額_契約金額_期貨 int",
		"value24", // 投信_多空淨額_契約金額_選擇權 int",
		"value25", // 外資_多方_口數_期貨 int",
		"value26", // 外資_多方_口數_選擇權 int",
		"value27", // 外資_多方_契約金額_期貨 int",
		"value28", // 外資_多方_契約金額_選擇權 int",
		"value29", // 外資_空方_口數_期貨 int",
		"value30", // 外資_空方_口數_選擇權 int",
		"value31", // 外資_空方_契約金額_期貨 int",
		"value32", // 外資_空方_契約金額_選擇權 int",
		"value33", // 外資_多空淨額_口數_期貨 int",
		"value34", // 外資_多空淨額_口數_選擇權 int",
		"value35", // 外資_多空淨額_契約金額_期貨 int",
		"value36", // 外資_多空淨額_契約金額_選擇權 int",
	};
	static final String[] FUTURE_OR_OPTION_TOP3_LEGAL_PERSONS_OPEN_INTEREST_FIELD_TYPE_DEFINITION = new String[]
	{
		"DATE NOT NULL PRIMARY KEY", // 日期
		"INT", // 自營商_多方_口數_期貨 int",
		"INT", // 自營商_多方_口數_選擇權 int",
		"INT", // 自營商_多方_契約金額_期貨 int",
		"INT", // 自營商_多方_契約金額_選擇權 int",
		"INT", // 自營商_空方_口數_期貨 int",
		"INT", // 自營商_空方_口數_選擇權 int",
		"INT", // 自營商_空方_契約金額_期貨 int",
		"INT", // 自營商_空方_契約金額_選擇權 int",
		"INT", // 自營商_多空淨額_口數_期貨 int",
		"INT", // 自營商_多空淨額_口數_選擇權 int",
		"INT", // 自營商_多空淨額_契約金額_期貨 int",
		"INT", // 自營商_多空淨額_契約金額_選擇權 int",
		"INT", // 投信_多方_口數_期貨 int",
		"INT", // 投信_多方_口數_選擇權 int",
		"INT", // 投信_多方_契約金額_期貨 int",
		"INT", // 投信_多方_契約金額_選擇權 int",
		"INT", // 投信_空方_口數_期貨 int",
		"INT", // 投信_空方_口數_選擇權 int",
		"INT", // 投信_空方_契約金額_期貨 int",
		"INT", // 投信_空方_契約金額_選擇權 int",
		"INT", // 投信_多空淨額_口數_期貨 int",
		"INT", // 投信_多空淨額_口數_選擇權 int",
		"INT", // 投信_多空淨額_契約金額_期貨 int",
		"INT", // 投信_多空淨額_契約金額_選擇權 int",
		"INT", // 外資_多方_口數_期貨 int",
		"INT", // 外資_多方_口數_選擇權 int",
		"INT", // 外資_多方_契約金額_期貨 int",
		"INT", // 外資_多方_契約金額_選擇權 int",
		"INT", // 外資_空方_口數_期貨 int",
		"INT", // 外資_空方_口數_選擇權 int",
		"INT", // 外資_空方_契約金額_期貨 int",
		"INT", // 外資_空方_契約金額_選擇權 int",
		"INT", // 外資_多空淨額_口數_期貨 int",
		"INT", // 外資_多空淨額_口數_選擇權 int",
		"INT", // 外資_多空淨額_契約金額_期貨 int",
		"INT", // 外資_多空淨額_契約金額_選擇權 int",
	};
	static final String[] OPTION_TOP3_LEGAL_PERSONS_BUY_AND_SELL_OPTION_OPEN_INTEREST_FIELD_DEFINITION = new String[]
	{
		"date", // 日期
		"value1", // 買權_自營商_買方_口數 int",
		"value2", // 買權_自營商_買方_契約金額 int",
		"value3", // 買權_自營商_賣方_口數 int",
		"value4", // 買權_自營商_賣方_契約金額 int",
		"value5", // 買權_自營商_買賣差額_口數 int",
		"value6", // 買權_自營商_買賣差額_契約金額 int",
		"value7", // 買權_投信_買方_口數 int",
		"value8", // 買權_投信_買方_契約金額 int",
		"value9", // 買權_投信_賣方_口數 int",
		"value10", // 買權_投信_賣方_契約金額 int",
		"value11", // 買權_投信_買賣差額_口數 int",
		"value12", // 買權_投信_買賣差額_契約金額 int",
		"value13", // 買權_外資_買方_口數 int",
		"value14", // 買權_外資_買方_契約金額 int",
		"value15", // 買權_外資_賣方_口數 int",
		"value16", // 買權_外資_賣方_契約金額 int",
		"value17", // 買權_外資_買賣差額_口數 int",
		"value18", // 買權_外資_買賣差額_契約金額 int",
		"value19", // 賣權_自營商_買方_口數 int",
		"value20", // 賣權_自營商_買方_契約金額 int",
		"value21", // 賣權_自營商_賣方_口數 int",
		"value22", // 賣權_自營商_賣方_契約金額 int",
		"value23", // 賣權_自營商_買賣差額_口數 int",
		"value24", // 賣權_自營商_買賣差額_契約金額 int",
		"value25", // 賣權_投信_買方_口數 int",
		"value26", // 賣權_投信_買方_契約金額 int",
		"value27", // 賣權_投信_賣方_口數 int",
		"value28", // 賣權_投信_賣方_契約金額 int",
		"value29", // 賣權_投信_買賣差額_口數 int",
		"value30", // 賣權_投信_買賣差額_契約金額 int",
		"value31", // 賣權_外資_買方_口數 int",
		"value32", // 賣權_外資_買方_契約金額 int",
		"value33", // 賣權_外資_賣方_口數 int",
		"value34", // 賣權_外資_賣方_契約金額 int",
		"value35", // 賣權_外資_買賣差額_口數 int",
		"value36", // 賣權_外資_買賣差額_契約金額 int",
	};
	static final String[] OPTION_TOP3_LEGAL_PERSONS_BUY_AND_SELL_OPTION_OPEN_INTEREST_FIELD_TYPE_DEFINITION = new String[]
	{
		"DATE NOT NULL PRIMARY KEY", // 日期
		"INT", // 買權_自營商_買方_口數 int",
		"INT", // 買權_自營商_買方_契約金額 int",
		"INT", // 買權_自營商_賣方_口數 int",
		"INT", // 買權_自營商_賣方_契約金額 int",
		"INT", // 買權_自營商_買賣差額_口數 int",
		"INT", // 買權_自營商_買賣差額_契約金額 int",
		"INT", // 買權_投信_買方_口數 int",
		"INT", // 買權_投信_買方_契約金額 int",
		"INT", // 買權_投信_賣方_口數 int",
		"INT", // 買權_投信_賣方_契約金額 int",
		"INT", // 買權_投信_買賣差額_口數 int",
		"INT", // 買權_投信_買賣差額_契約金額 int",
		"INT", // 買權_外資_買方_口數 int",
		"INT", // 買權_外資_買方_契約金額 int",
		"INT", // 買權_外資_賣方_口數 int",
		"INT", // 買權_外資_賣方_契約金額 int",
		"INT", // 買權_外資_買賣差額_口數 int",
		"INT", // 買權_外資_買賣差額_契約金額 int",
		"INT", // 賣權_自營商_買方_口數 int",
		"INT", // 賣權_自營商_買方_契約金額 int",
		"INT", // 賣權_自營商_賣方_口數 int",
		"INT", // 賣權_自營商_賣方_契約金額 int",
		"INT", // 賣權_自營商_買賣差額_口數 int",
		"INT", // 賣權_自營商_買賣差額_契約金額 int",
		"INT", // 賣權_投信_買方_口數 int",
		"INT", // 賣權_投信_買方_契約金額 int",
		"INT", // 賣權_投信_賣方_口數 int",
		"INT", // 賣權_投信_賣方_契約金額 int",
		"INT", // 賣權_投信_買賣差額_口數 int",
		"INT", // 賣權_投信_買賣差額_契約金額 int",
		"INT", // 賣權_外資_買方_口數 int",
		"INT", // 賣權_外資_買方_契約金額 int",
		"INT", // 賣權_外資_賣方_口數 int",
		"INT", // 賣權_外資_賣方_契約金額 int",
		"INT", // 賣權_外資_買賣差額_口數 int",
		"INT", // 賣權_外資_買賣差額_契約金額 int",
	};
	static final String[] OPTION_PUT_CALL_RATIO_FIELD_DEFINITION = new String[]
	{
		"date", // 日期
		"value1", // 賣權成交量
		"value2", // 買權成交量
		"value3", // 買賣權成交量比率%
		"value4", // 賣權未平倉量
		"value5", // 買權未平倉量
		"value6", // 買賣權未平倉量比率%
	};
	static final String[] OPTION_PUT_CALL_RATIO_FIELD_TYPE_DEFINITION = new String[]
	{
		"DATE NOT NULL PRIMARY KEY", // 日期
		"INT", // 賣權成交量
		"INT", // 買權成交量
		"FLOAT", // 買賣權成交量比率%
		"INT", // 賣權未平倉量
		"INT", // 買權未平倉量
		"FLOAT", // 買賣權未平倉量比率%
	};
	static final String[] FUTURE_TOP10_DEALERS_AND_LEGAL_PERSONS_FIELD_DEFINITION = new String[]
	{
		"date", // 日期
		"value1", // 臺股期貨_到期月份_買方_前五大交易人合計_部位數
		"value2", // 臺股期貨_到期月份_買方_前五大交易人合計_百分比
		"value3", // 臺股期貨_到期月份_買方_前十大交易人合計_部位數
		"value4", // 臺股期貨_到期月份_買方_前十大交易人合計_百分比
		"value5", // 臺股期貨_到期月份_賣方_前五大交易人合計_部位數
		"value6", // 臺股期貨_到期月份_賣方_前五大交易人合計_百分比
		"value7", // 臺股期貨_到期月份_賣方_前十大交易人合計_部位數
		"value8", // 臺股期貨_到期月份_賣方_前十大交易人合計_百分比
		"value9", // 臺股期貨_到期月份_全市場未沖銷部位數
		"value10", // 臺股期貨_所有契約_買方_前五大交易人合計_部位數
		"value11", // 臺股期貨_所有契約_買方_前五大交易人合計_百分比
		"value12", // 臺股期貨_所有契約_買方_前十大交易人合計_部位數
		"value13", // 臺股期貨_所有契約_買方_前十大交易人合計_百分比
		"value14", // 臺股期貨_所有契約_賣方_前五大交易人合計_部位數
		"value15", // 臺股期貨_所有契約_賣方_前五大交易人合計_百分比
		"value16", // 臺股期貨_所有契約_賣方_前十大交易人合計_部位數
		"value17", // 臺股期貨_所有契約_賣方_前十大交易人合計_百分比
		"value18", // 臺股期貨_所有契約_全市場未沖銷部位數
	};
	static final String[] FUTURE_TOP10_DEALERS_AND_LEGAL_PERSONS_FIELD_TYPE_DEFINITION = new String[]
	{
		"DATE NOT NULL PRIMARY KEY", // 日期
		"INT", // 臺股期貨_到期月份_買方_前五大交易人合計_部位數
		"FLOAT", // 臺股期貨_到期月份_買方_前五大交易人合計_百分比
		"INT", // 臺股期貨_到期月份_買方_前十大交易人合計_部位數
		"FLOAT", // 臺股期貨_到期月份_買方_前十大交易人合計_百分比
		"INT", // 臺股期貨_到期月份_賣方_前五大交易人合計_部位數
		"FLOAT", // 臺股期貨_到期月份_賣方_前五大交易人合計_百分比
		"INT", // 臺股期貨_到期月份_賣方_前十大交易人合計_部位數
		"FLOAT", // 臺股期貨_到期月份_賣方_前十大交易人合計_百分比
		"INT", // 臺股期貨_到期月份_全市場未沖銷部位數
		"INT", // 臺股期貨_所有契約_買方_前五大交易人合計_部位數
		"FLOAT", // 臺股期貨_所有契約_買方_前五大交易人合計_百分比
		"INT", // 臺股期貨_所有契約_買方_前十大交易人合計_部位數
		"FLOAT", // 臺股期貨_所有契約_買方_前十大交易人合計_百分比
		"INT", // 臺股期貨_所有契約_賣方_前五大交易人合計_部位數
		"FLOAT", // 臺股期貨_所有契約_賣方_前五大交易人合計_百分比
		"INT", // 臺股期貨_所有契約_賣方_前十大交易人合計_部位數
		"FLOAT", // 臺股期貨_所有契約_賣方_前十大交易人合計_百分比
		"INT", // 臺股期貨_所有契約_全市場未沖銷部位數
	};
}
