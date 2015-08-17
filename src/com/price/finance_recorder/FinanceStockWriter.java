package com.price.finance_recorder;

import java.util.*;
import java.sql.*;
import java.text.*;


public class FinanceStockWriter extends FinanceWriterBase
{
	private static final String[] FIELD_LIST = new String[]{"date", "open", "high", "low", "close", "volume"};
	private static final String[] FIELD_TYPE_LIST = new String[]{"DATE", "FLOAT", "FLOAT", "FLOAT", "FLOAT", "FLOAT"};
	private static final int FIELD_DATE_INDEX = 0;
	private static final int FIELD_TYPE_DATE_INDEX = FIELD_DATE_INDEX;
	private static final int field_list_len = FIELD_LIST.length;

	public FinanceStockWriter() throws RuntimeException
	{
// Check the length of mapping table
		if (field_list_len != FIELD_TYPE_LIST.length)
		{
			String error_desc = String.format("The field length is NOT equal, field: %d, field type: %d", field_list_len, FIELD_TYPE_LIST.length);
			FinanceRecorderCmnDef.debug(error_desc);
			throw new RuntimeException(error_desc);
		}
// Check if the first field is date
		if (!FIELD_LIST[FIELD_DATE_INDEX].equals("date"))
		{
			String error_desc = String.format("The %drd field should be data, not %s", FIELD_DATE_INDEX, FIELD_LIST[FIELD_DATE_INDEX]);
			FinanceRecorderCmnDef.debug(error_desc);
			throw new RuntimeException(error_desc);
		}
// Check if the first field is date
		if (!FIELD_TYPE_LIST[FIELD_TYPE_DATE_INDEX].equals("DATE"))
		{
			String error_desc = String.format("The %drd field type should be DATE, not %s", FIELD_TYPE_DATE_INDEX, FIELD_TYPE_LIST[FIELD_TYPE_DATE_INDEX]);
			FinanceRecorderCmnDef.debug(error_desc);
			throw new RuntimeException(error_desc);
		}
	}

	protected int get_date_index() {return FIELD_DATE_INDEX;}

	protected short format_field_cmd()
	{
		cmd_create_table = String.format(format_cmd_create_table_head, table);
// Set the date field as the primary key
		cmd_create_table += String.format("%s %s PRIMARY KEY", FIELD_LIST[FIELD_DATE_INDEX], FIELD_TYPE_LIST[FIELD_TYPE_DATE_INDEX]);
		for (int i = 1 ; i < field_list_len ; i++)
		{
			if (i == FIELD_DATE_INDEX)
				continue;
			cmd_create_table += String.format(",%s %s", FIELD_LIST[i], FIELD_TYPE_LIST[i]);
		}
		cmd_create_table += format_cmd_create_table_tail;
		FinanceRecorderCmnDef.format_debug("Create the command of creating table[%s]: %s", table, cmd_create_table);

		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	protected short format_data_cmd(List<String> data_list)
	{
		if (data_list.size() > FinanceRecorderCmnDef.EACH_UPDATE_DATA_AMOUNT)
		{
			FinanceRecorderCmnDef.format_debug("The size of data_list is %d, larger than %d", data_list.size(), FinanceRecorderCmnDef.EACH_UPDATE_DATA_AMOUNT);
			return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
		}

		int split = -1;
		String cmd_field = null;
		String cmd_data = null;
		String cmd_update_data = null;
		String cmd_insert_data;
		int count = 0;
		java.sql.Date db_date = null;
		for (String data : data_list)
		{
			String file_item_arr[] = data.split(FinanceRecorderCmnDef.DATA_SPLIT);

			Iterator entries = sql_file_field_mapping_table.entrySet().iterator();
// Add the date field
			cmd_field = FIELD_LIST[FIELD_DATE_INDEX];
			cmd_data = "?";
//			cmd_data = file_item_arr[sql_file_field_mapping_table.get(FIELD_DATE_INDEX)];
			try
			{
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); // your template here
				java.util.Date dateStr = formatter.parse(file_item_arr[sql_file_field_mapping_table.get(FIELD_DATE_INDEX)]);
				db_date = new java.sql.Date(dateStr.getTime());
//				cmd_data = String.format("%s", db_date);
			}
			catch (ParseException e)
			{
				FinanceRecorderCmnDef.format_debug("Fail to transform the MySQL time format, due to: %s", e.toString());
				return FinanceRecorderCmnDef.RET_FAILURE_MYSQL;
			}
			cmd_update_data = null;
//			db_date = null;
			while (entries.hasNext())
			{
				Map.Entry entry = (Map.Entry) entries.next();
				int key_index = (Integer)entry.getKey();
				int value_index = (Integer)entry.getValue();
				if (key_index == FIELD_DATE_INDEX)
					continue;

				cmd_field += String.format(",%s", FIELD_LIST[key_index]);
				cmd_data += String.format(",%s", file_item_arr[value_index]);
				if (cmd_update_data == null)
					cmd_update_data = String.format(" %s=%s", FIELD_LIST[key_index], file_item_arr[value_index]);
				else
					cmd_update_data += String.format(",%s=%s", FIELD_LIST[key_index], file_item_arr[value_index]);
			}
			cmd_insert_data = format_cmd_insert_into_table_head_with_name + cmd_field + format_cmd_insert_into_table_mid + cmd_data + format_cmd_insert_into_table_tail + cmd_update_data;
			try
			{
				PreparedStatement pstmt = connection.prepareStatement(cmd_insert_data);
				pstmt.setDate(1, db_date);
//				StockRecorderCmnDef.format_debug("Create the command of inserting data: %s", pstmt);
				cmd_insert_data_list[count++] = pstmt;
			}
			catch (SQLException e)
			{
				FinanceRecorderCmnDef.format_debug("Fail to prepare MySQL command, due to: %s", e.toString());
				return FinanceRecorderCmnDef.RET_FAILURE_MYSQL;
			}
		}

		return FinanceRecorderCmnDef.RET_SUCCESS;
	}
}
