package com.price.finance_recorder;

import java.util.*;
import java.sql.*;
import java.text.*;


public class FinanceRecorderFutureTop10DealersAndLegalPersonsWriter
{
	private static final String[] FIELD_LIST = new String[]{"date", "open", "high", "low", "close", "volume"};
	private static final String[] FIELD_TYPE_LIST = new String[]{"DATE", "FLOAT", "FLOAT", "FLOAT", "FLOAT", "FLOAT"};
	private static final int FIELD_DATE_INDEX = 0;

	public FinanceRecorderFutureTop10DealersAndLegalPersonsWriter()
	{

	}

	protected short format_insert_data_cmd(List<String> data_list)
	{
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}
}
