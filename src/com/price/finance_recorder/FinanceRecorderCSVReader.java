package com.price.finance_recorder;

import java.io.*;
import java.util.*;


public class FinanceRecorderCSVReader
{
	private String csv_filepath;
	private BufferedReader br = null;
	private FinanceRecorderCmnDef.FinanceObserverInf parent_observer = null;
//	private boolean IgnoreErrorIfFileNotExist = true;

	public FinanceRecorderCSVReader(FinanceRecorderCmnDef.FinanceObserverInf observer)
	{
		FinanceRecorderCmnDef.FinanceObserverInf parent_observer = observer;
	}
	
	public short initialize(String data_filepath)
	{
		csv_filepath = data_filepath;
		FinanceRecorderCmnDef.format_debug("Open the CSV file: %s", csv_filepath);

		try
		{
			br = new BufferedReader(new FileReader(csv_filepath));
		}
		catch (FileNotFoundException e)
		{
			FinanceRecorderCmnDef.format_error("The data file[%s] is NOT found", csv_filepath);
			return FinanceRecorderCmnDef.RET_FAILURE_NOT_FOUND;
		}
		catch (IOException e)
		{
			FinanceRecorderCmnDef.format_error("Error occur, due to: %s", e.toString());
			return FinanceRecorderCmnDef.RET_FAILURE_IO_OPERATION;
		} 

		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

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

		csv_filepath = null;
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	public short read(List<String> data_list)
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		String line = null;
		int count = 0;
		String field_string = null;
		try
		{
			while ((line = br.readLine()) != null) 
			{
//				String[] field_array = line.split(FinanceRecorderCmnDef.DATA_SPLIT);
//				data_list.add(format_data_to_string(field_array));
				data_list.add(line);
//				FinanceRecorderCmnDef.format_debug("New Data: %s", ((LinkedList<String>)data_list).peekLast());
				count++;
			}
		}
		catch (IOException e) 
		{
			FinanceRecorderCmnDef.format_error("Error occur while reading the data, due to: %s", e.toString());
			ret = FinanceRecorderCmnDef.RET_FAILURE_IO_OPERATION;
		}
		FinanceRecorderCmnDef.format_debug("Got %d data in: %s", count, csv_filepath);

		return ret;
	}
}
