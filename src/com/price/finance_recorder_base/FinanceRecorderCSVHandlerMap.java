package com.price.finance_recorder_base;

import java.util.ArrayList;
import java.util.TreeMap;
import com.price.finance_recorder_cmn.FinanceRecorderCmnDef;


public class FinanceRecorderCSVHandlerMap extends TreeMap<Integer, FinanceRecorderCSVHandler>
{
	private TreeMap<Integer, FinanceRecorderCSVHandler> csv_handler_map = null;

	public FinanceRecorderCSVHandlerMap(){csv_handler_map = new TreeMap<Integer, FinanceRecorderCSVHandler>();}

	public short register_csv_handler(int source_key, final FinanceRecorderCSVHandler csv_handler)
	{
		if (csv_handler_map.containsKey(source_key))
			throw new IllegalArgumentException(String.format("The CSV handler of source key[%s] already exist", source_key));
		csv_handler_map.put(source_key, csv_handler);
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	public final FinanceRecorderCSVHandler lookup_csv_handler(int source_key)
	{
		if (!csv_handler_map.containsKey(source_key)) 
			throw new IllegalArgumentException(String.format("Fail to find the CSV handler of source key: %d", source_key));
		return csv_handler_map.get(source_key);
	}

	public final ArrayList<String> get_csv_data(int source_key)
	{
		return lookup_csv_handler(source_key).get_read_data();
	}
}
