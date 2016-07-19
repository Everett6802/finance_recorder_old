package com.price.finance_recorder;

import java.io.*;
import java.util.*;

import com.price.finance_recorder_cmn.FinanceRecorderCmnDef;


public class FinanceRecorderCSVHandler
{
	public enum HandlerMode{HandlerMode_Read, HandlerMode_Write};

	private final String NEW_LINE = "\n";
	private String csv_filepath;
	private BufferedReader br = null;
	private BufferedWriter bw = null;
	private FinanceRecorderCmnDef.FinanceObserverInf parent_observer = null;
	private HandlerMode handler_mode;
//	private boolean IgnoreErrorIfFileNotExist = true;

	public FinanceRecorderCSVHandler(FinanceRecorderCmnDef.FinanceObserverInf observer)
	{
		handler_mode = HandlerMode.HandlerMode_Read;
		FinanceRecorderCmnDef.FinanceObserverInf parent_observer = observer;
	}

	private short intialize_read_stream()
	{
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

	private short intialize_write_stream()
	{
		File file = new File(csv_filepath);
		try
		{
			file.createNewFile();
			bw = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
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

	public short initialize(String data_filepath, HandlerMode mode)
	{
		csv_filepath = data_filepath;
		handler_mode = mode;
//		FinanceRecorderCmnDef.format_debug("Open the CSV file: %s", csv_filepath);
		return ((mode == HandlerMode.HandlerMode_Read) ? intialize_read_stream() : intialize_write_stream());
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
		if (bw != null)
		{
			try 
			{
				bw.flush();
				bw.close();
			}
			catch (IOException e){}
		}

		csv_filepath = null;
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	public short read(List<String> data_list)
	{
		if (handler_mode != HandlerMode.HandlerMode_Read)
		{
			FinanceRecorderCmnDef.error("CSV Handler is NOT in REAM mode");
			return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
		}

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
		FinanceRecorderCmnDef.format_debug("Read %d data in: %s", count, csv_filepath);

		return ret;
	}

	public short write(final List<String> data_list)
	{
		if (handler_mode != HandlerMode.HandlerMode_Write)
		{
			FinanceRecorderCmnDef.error("CSV Handler is NOT in WRITE mode");
			return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
		}

		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		String line = null;
		int count = 0;
		String field_string = null;
		try
		{
			for (String data : data_list)
			{ 
				bw.write(data);
				bw.write(NEW_LINE);
			}
		}
		catch (IOException e) 
		{
			FinanceRecorderCmnDef.format_error("Error occur while writing the data, due to: %s", e.toString());
			ret = FinanceRecorderCmnDef.RET_FAILURE_IO_OPERATION;
		}
		FinanceRecorderCmnDef.format_debug("Write %d data in: %s", count, csv_filepath);

		return ret;
	}
}
