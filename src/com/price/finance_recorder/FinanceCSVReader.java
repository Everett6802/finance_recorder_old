package com.price.finance_recorder;

import java.io.*;
import java.util.*;


public class FinanceCSVReader implements FinanceRecorderCmnDef.FinanceReaderInf
{
	protected static String CSV_SPLIT = ";";
	protected static final String FILE_FOLDER_NAME = "data";

	private BufferedReader br = null;
	private FinanceRecorderCmnDef.FinanceObserverInf parent_observer = null;

	protected String format_data_to_string(String[] field_array)
	{
		String field_string = null;
		for (String field : field_array)
		{
			if (field_string == null)
				field_string = field;
			else
				field_string += (FinanceRecorderCmnDef.DATA_SPLIT + field);
		}

		return field_string;
	}

	@Override
	public short initialize(FinanceRecorderCmnDef.FinanceObserverInf observer, String data_filename)
	{
		FinanceRecorderCmnDef.FinanceObserverInf parent_observer = observer;

		String current_path = FinanceRecorderCmnDef.get_current_path();
		String csv_filepath = String.format("%s/%s/%s", current_path, FILE_FOLDER_NAME, data_filename);
		FinanceRecorderCmnDef.format_debug("Open the CSV file: %s", csv_filepath);

		try
		{
			br = new BufferedReader(new FileReader(csv_filepath));
		}
		catch (FileNotFoundException e)
		{
			FinanceRecorderCmnDef.format_error("The CSV file[%s] is NOT found", csv_filepath);
			return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_PATH;
		}
		catch (IOException e)
		{
			FinanceRecorderCmnDef.format_error("Error occur, due to: %s", e.toString());
			return FinanceRecorderCmnDef.RET_FAILURE_IO_OPERATION;
		} 

		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	@Override
	public short read(List<String> data_list)
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		String line = null;
		int count = 0;
		String field_string = null;
		data_list.clear();

		try
		{
			while ((line = br.readLine()) != null && count < FinanceRecorderCmnDef.EACH_UPDATE_DATA_AMOUNT) 
			{
				String[] field_array = line.split(CSV_SPLIT);
				data_list.add(format_data_to_string(line.split(CSV_SPLIT)));
				FinanceRecorderCmnDef.format_debug("New Data: %s", ((LinkedList<String>)data_list).peekLast());
				count++;
			}
		}
		catch (IOException e) 
		{
			FinanceRecorderCmnDef.format_error("Error occur while reading the data, due to: %s", e.toString());
			ret = FinanceRecorderCmnDef.RET_FAILURE_IO_OPERATION;
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

		return FinanceRecorderCmnDef.RET_SUCCESS;
	}
}
