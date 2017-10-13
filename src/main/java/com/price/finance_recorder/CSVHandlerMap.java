package com.price.finance_recorder;

import java.util.ArrayList;
import java.util.TreeMap;


class CSVHandlerMap extends TreeMap<Integer, CSVHandler>
{
	private TreeMap<Integer, CSVHandler> csv_handler_map = null;

	public CSVHandlerMap(){csv_handler_map = new TreeMap<Integer, CSVHandler>();}

	public short register_csv_handler(int source_key, final CSVHandler csv_handler)
	{
		if (csv_handler_map.containsKey(source_key))
			throw new IllegalArgumentException(String.format("The CSV handler of source key[%s] already exist", source_key));
		csv_handler_map.put(source_key, csv_handler);
		return CmnDef.RET_SUCCESS;
	}

	public final CSVHandler lookup_csv_handler(int source_key)
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
