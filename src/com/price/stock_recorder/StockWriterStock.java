package com.price.stock_recorder;

import java.util.*;


public class StockWriterStock extends StockWriterBase
{
	private static final String[] FIELD_LIST = new String[]{"date", "open", "high", "low", "close", "volume"};
	private static final String[] FIELD_TYPE_LIST = new String[]{"DATE", "FLOAT", "FLOAT", "FLOAT", "FLOAT", "FLOAT"};
	private static final int field_list_len = FIELD_LIST.length;

	public StockWriterStock()
	{
//		int field_list_len = field_list.length;
//		if (field_list_len != field_type_list.length)
//		{
//			StockRecorderCmnDef.format_debug("The field length is NOT equal, field: %d, field type: %d", field_list_len, field_type_list.length);
//			return StockRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
//		}
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
		for (String data : data_list)
		{
			String file_item_arr[] = data.split(StockRecorderCmnDef.DATA_SPLIT);

			Iterator entries = file_sql_field_mapping_table.entrySet().iterator();
			Map.Entry entry = (Map.Entry) entries.next();
			cmd_field = FIELD_LIST[(Integer)entry.getValue()];
			cmd_data = file_item_arr[(Integer)entry.getKey()];
			while (entries.hasNext())
			{
				entry = (Map.Entry) entries.next();
				cmd_field += String.format(",%s", FIELD_LIST[(Integer)entry.getValue()]);
				cmd_data += String.format(",%s", file_item_arr[(Integer)entry.getKey()]);
			}
			cmd_insert_data = format_cmd_insert_into_table_head_with_name + cmd_field + format_cmd_insert_into_table_mid + cmd_data + format_cmd_insert_into_table_tail;
			StockRecorderCmnDef.format_debug("Create the command of inserting data: %s", cmd_insert_data);
			cmd_insert_data_list[count++] = cmd_insert_data;
		}

		return StockRecorderCmnDef.RET_SUCCESS;
	}
}
