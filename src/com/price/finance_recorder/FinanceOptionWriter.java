package com.price.finance_recorder;

import java.util.*;
import java.sql.*;
import java.text.*;


public class FinanceOptionWriter extends FinanceWriterBase
{
	@Override
	protected int get_date_index()
	{
		return 0;
	}

	@Override
	protected short format_field_cmd()
	{
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	@Override
	protected short format_data_cmd(List<String> data_list)
	{
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

}
