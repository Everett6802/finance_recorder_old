package com.price.stock_recorder;

import java.util.*;
import java.sql.*;
import java.text.*;


public class StockWriterStock extends StockWriterBase
{
	private static final String[] FIELD_LIST = new String[]{"date", "open", "high", "low", "close", "volume"};
	private static final String[] FIELD_TYPE_LIST = new String[]{"DATE", "FLOAT", "FLOAT", "FLOAT", "FLOAT", "FLOAT"};
	private static final int FIELD_DATE_INDEX = 0;
	private static final int FIELD_TYPE_DATE_INDEX = 0;
	private static final int field_list_len = FIELD_LIST.length;

	public StockWriterStock() throws RuntimeException
	{
// Check the length of mapping table
		if (field_list_len != FIELD_TYPE_LIST.length)
		{
			String error_desc = String.format("The field length is NOT equal, field: %d, field type: %d", field_list_len, FIELD_TYPE_LIST.length);
			StockRecorderCmnDef.debug(error_desc);
			throw new RuntimeException(error_desc);
		}
// Check if the first field is date
		if (!FIELD_LIST[FIELD_DATE_INDEX].equals("date"))
		{
			String error_desc = String.format("The first field should be data, not %s", FIELD_LIST[FIELD_DATE_INDEX]);
			StockRecorderCmnDef.debug(error_desc);
			throw new RuntimeException(error_desc);
		}
// Check if the first field is date
		if (!FIELD_TYPE_LIST[FIELD_TYPE_DATE_INDEX].equals("DATE"))
		{
			String error_desc = String.format("The first field type should be DATE, not %s", FIELD_TYPE_LIST[FIELD_TYPE_DATE_INDEX]);
			StockRecorderCmnDef.debug(error_desc);
			throw new RuntimeException(error_desc);
		}
	}

	protected short format_field_cmd()
	{
		cmd_create_table = String.format(format_cmd_create_table_head, table);
		cmd_create_table += String.format("%s %s", FIELD_LIST[0], FIELD_TYPE_LIST[0]);
		for (int i = 1 ; i < field_list_len ; i++)
			cmd_create_table += String.format(",%s %s", FIELD_LIST[i], FIELD_TYPE_LIST[i]);
		cmd_create_table += format_cmd_create_table_tail;
		StockRecorderCmnDef.format_debug("Create the command of creating table[%s]: %s", table, cmd_create_table);

		return StockRecorderCmnDef.RET_SUCCESS;
	}

	protected short format_data_cmd(List<String> data_list)
	{
		if (data_list.size() > StockRecorderCmnDef.EACH_UPDATE_DATA_AMOUNT)
		{
			StockRecorderCmnDef.format_debug("The size of data_list is %d, larger than %d", data_list.size(), StockRecorderCmnDef.EACH_UPDATE_DATA_AMOUNT);
			return StockRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
		}

		int split = -1;
		String cmd_field = null;
		String cmd_data = null;
		String cmd_insert_data;
		int count = 0;
		java.sql.Date db_date = null;
		for (String data : data_list)
		{
			String file_item_arr[] = data.split(StockRecorderCmnDef.DATA_SPLIT);

			Iterator entries = file_sql_field_mapping_table.entrySet().iterator();
// Add the date field
			cmd_field = null;
			cmd_data = null;
			db_date = null;
			while (entries.hasNext())
			{
				Map.Entry entry = (Map.Entry) entries.next();
				int key_index = (Integer)entry.getKey();
				int value_index = (Integer)entry.getValue();

				if (cmd_field != null)
					cmd_field += String.format(",%s", FIELD_LIST[value_index]);
				else
					cmd_field = FIELD_LIST[value_index];

				if (value_index == FIELD_TYPE_DATE_INDEX)
				{
					try
					{
						SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); // your template here
						java.util.Date dateStr = formatter.parse(file_item_arr[key_index]);
						db_date = new java.sql.Date(dateStr.getTime());
						if (cmd_data != null)
							cmd_data += String.format(",?", db_date);
						else
							cmd_data = String.format("?", db_date);
					}
					catch (ParseException e)
					{
						StockRecorderCmnDef.format_debug("Fail to transform the MySQL time format, due to: %s", e.toString());
						return StockRecorderCmnDef.RET_FAILURE_MYSQL;
					}
				}
				else
				{
					if (cmd_data != null)
						cmd_data += String.format(",%s", file_item_arr[key_index]);
					else
						cmd_data = String.format("%s", file_item_arr[key_index]);
				}
			}
			cmd_insert_data = format_cmd_insert_into_table_head_with_name + cmd_field + format_cmd_insert_into_table_mid + cmd_data + format_cmd_insert_into_table_tail;
			try
			{
				PreparedStatement pstmt = connection.prepareStatement(cmd_insert_data);
				pstmt.setDate(1, db_date);
//				StockRecorderCmnDef.format_debug("Create the command of inserting data: %s", pstmt);
				cmd_insert_data_list[count++] = pstmt;
			}
			catch (SQLException e)
			{
				StockRecorderCmnDef.format_debug("Fail to prepare MySQL command, due to: %s", e.toString());
				return StockRecorderCmnDef.RET_FAILURE_MYSQL;
			}
		}

		return StockRecorderCmnDef.RET_SUCCESS;
	}
}
