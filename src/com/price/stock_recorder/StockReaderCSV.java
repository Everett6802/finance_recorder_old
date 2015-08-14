package com.price.stock_recorder;

import java.io.*;
import java.util.*;


public class StockReaderCSV extends StockReaderBase
{
	static protected String CSV_SPLIT = ";";

	private BufferedReader br = null;
	private StockRecorderCmnDef.StockObserverInf parent_observer = null;

	@Override
	public short initialize(StockRecorderCmnDef.StockObserverInf observer, String data_filename)
	{
		StockRecorderCmnDef.StockObserverInf parent_observer = observer;

		String current_path = StockRecorderCmnDef.get_current_path();
		String csv_filepath = String.format("%s/%s/%s", current_path, FILE_FOLDER_NAME, data_filename);
		StockRecorderCmnDef.format_debug("Open the CSV file: %s", csv_filepath);

		try
		{
			br = new BufferedReader(new FileReader(csv_filepath));
		}
		catch (FileNotFoundException e) 
		{
			StockRecorderCmnDef.format_error("The CSV file[%s] is NOT found", csv_filepath);
			return StockRecorderCmnDef.RET_FAILURE_INCORRECT_PATH;
		}
		catch (IOException e)
		{
			StockRecorderCmnDef.format_error("Error occur, due to: %s", e.toString());
			return StockRecorderCmnDef.RET_FAILURE_IO_OPERATION;
		} 

		return StockRecorderCmnDef.RET_SUCCESS;
	}

	@Override
	public short read(List<String> data_list)
	{
		short ret = StockRecorderCmnDef.RET_SUCCESS;
		String line = null;
		int count = 0;
		String field_string = null;
		data_list.clear();

		try
		{
			while ((line = br.readLine()) != null && count < StockRecorderCmnDef.EACH_UPDATE_DATA_AMOUNT) 
			{
				String[] field_array = line.split(CSV_SPLIT);
				data_list.add(format_data_to_string(line.split(CSV_SPLIT)));
				StockRecorderCmnDef.format_debug("New Data: %s", ((LinkedList<String>)data_list).peekLast());
				count++;
			}
		}
		catch (IOException e) 
		{
			StockRecorderCmnDef.format_error("Error occur while reading the data, due to: %s", e.toString());
			ret = StockRecorderCmnDef.RET_FAILURE_IO_OPERATION;
		}

		return ret;
	}

	@Override
	public short deinitialize()
	{
		if (br != null)
		{
			try 
			{
				br.close();
			}
			catch (IOException e){}
		}

		return StockRecorderCmnDef.RET_SUCCESS;
	}
}
