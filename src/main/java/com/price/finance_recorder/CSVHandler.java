package com.price.finance_recorder;

import java.io.*;
import java.util.*;


class CSVHandler implements Iterable<String>
{
	public enum HandlerMode{HandlerMode_Read, HandlerMode_Write};

	public static CSVHandler get_csv_reader(String csv_filepath, boolean no_not_foud_exception)
	{
		CSVHandler csv_reader = new CSVHandler();
		csv_reader.handler_mode = HandlerMode.HandlerMode_Read;
		CmnLogger.debug(String.format("Ready to read CSV from: %s", csv_filepath));
		csv_reader.csv_filepath = csv_filepath;
		short ret = csv_reader.initialize_read_stream();
		if (CmnDef.CheckFailure(ret))
		{
			if (no_not_foud_exception)
			{
				if (CmnDef.CheckFailureNotFound(ret))
					return null;
			}
			else
				throw new RuntimeException(String.format("Fail to initialize the CSV reader, due to: %s", CmnDef.GetErrorDescription(ret)));
		}
		return csv_reader;
	}
	public static CSVHandler get_csv_reader(String csv_filepath){return get_csv_reader(csv_filepath, true);}

	public static CSVHandler get_csv_writer(String csv_filepath, boolean no_not_foud_exception)
	{
		CSVHandler csv_writer = new CSVHandler();
		csv_writer.handler_mode = HandlerMode.HandlerMode_Write;
		csv_writer.csv_filepath = csv_filepath;
		short ret = csv_writer.initialize_write_stream();
		if (CmnDef.CheckFailure(ret))
		{
			if (no_not_foud_exception)
			{
				if (CmnDef.CheckFailureNotFound(ret))
					return null;
			}
			else
				throw new RuntimeException(String.format("Fail to initialize the CSV writer, due to: %s", CmnDef.GetErrorDescription(ret)));
		}
		return csv_writer;
	}
	public static CSVHandler get_csv_writer(String csv_filepath){return get_csv_writer(csv_filepath, false);}

	private final String NEW_LINE = "\n";
	private String csv_filepath;
	private BufferedReader br = null;
	private BufferedWriter bw = null;
//	private CmnDef.FinanceObserverInf parent_observer = null;
	private HandlerMode handler_mode;
	private ArrayList<String> data_list = null;
//	private boolean IgnoreErrorIfFileNotExist = true;

	private CSVHandler()
	{
//		handler_mode = HandlerMode.HandlerMode_Read;
//		CmnDef.FinanceObserverInf parent_observer = observer;
	}

	private short initialize_read_stream() 
	{
		try
		{
			br = new BufferedReader(new FileReader(csv_filepath));
		}
		catch (FileNotFoundException e)
		{
			CmnLogger.format_error("The data file[%s] is NOT found", csv_filepath);
			return CmnDef.RET_FAILURE_NOT_FOUND;
		}
//		catch (IOException e)
//		{
//			CmnLogger.format_error("Error occur, due to: %s", e.toString());
//			return CmnDef.RET_FAILURE_IO_OPERATION;
//		} 

		return CmnDef.RET_SUCCESS;
	}

	private short initialize_write_stream()
	{
		File file = new File(csv_filepath);
		try
		{
			file.createNewFile();
			bw = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
		}
		catch (FileNotFoundException e)
		{
			CmnLogger.format_error("The data file[%s] is NOT found", csv_filepath);
			return CmnDef.RET_FAILURE_NOT_FOUND;
		}
		catch (IOException e)
		{
			CmnLogger.format_error("Error occur, due to: %s", e.toString());
			return CmnDef.RET_FAILURE_IO_OPERATION;
		} 

		return CmnDef.RET_SUCCESS;
	}

//	public short initialize(String data_filepath, HandlerMode mode)
//	{
//		csv_filepath = data_filepath;
//		handler_mode = mode;
////		CmnLogger.format_debug("Open the CSV file: %s", csv_filepath);
//		return ((mode == HandlerMode.HandlerMode_Read) ? initialize_read_stream() : initialize_write_stream());
//	}

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
		return CmnDef.RET_SUCCESS;
	}

	public short read()
	{
		if (handler_mode != HandlerMode.HandlerMode_Read)
		{
			CmnLogger.error("CSV Handler is NOT in READ mode");
			return CmnDef.RET_FAILURE_INCORRECT_OPERATION;
		}
		if (data_list != null)
		{
			CmnLogger.error("CSV data already exist in the list");
			return CmnDef.RET_FAILURE_INCORRECT_OPERATION;
		}
		data_list = new ArrayList<String>();

		short ret = CmnDef.RET_SUCCESS;
		String line = null;
		int count = 0;
//		String field_string = null;
		try
		{
			while ((line = br.readLine()) != null) 
			{
//				String[] field_array = line.split(CmnDef.DATA_SPLIT);
//				data_list.add(format_data_to_string(field_array));
				data_list.add(line);
//				CmnLogger.format_debug("New Data: %s", ((LinkedList<String>)data_list).peekLast());
				count++;
			}
		}
		catch (IOException e) 
		{
			CmnLogger.format_error("Error occur while reading the data, due to: %s", e.toString());
			ret = CmnDef.RET_FAILURE_IO_OPERATION;
		}
		CmnLogger.format_debug("Read %d data in: %s", count, csv_filepath);

		return ret;
	}

	public final ArrayList<String> get_read_data()
	{
		if (handler_mode != HandlerMode.HandlerMode_Read)
		{
			CmnLogger.error("CSV Handler is NOT in READ mode");
			throw new IllegalStateException("CSV Handler is NOT in READ mode");
		}
		if (data_list == null)
		{
			CmnLogger.error("No CSV data to read");
			throw new IllegalStateException("No CSV data to read");
		}
		return data_list;
	}

	public short write()
	{
		if (handler_mode != HandlerMode.HandlerMode_Write)
		{
			CmnLogger.error("CSV Handler is NOT in WRITE mode");
			return CmnDef.RET_FAILURE_INCORRECT_OPERATION;
		}

		short ret = CmnDef.RET_SUCCESS;
//		String line = null;
		int count = 0;
//		String field_string = null;
		try
		{
			for (String data : data_list)
			{ 
				bw.write(data);
				bw.write(NEW_LINE);
			}
			bw.flush();
		}
		catch (IOException e) 
		{
			CmnLogger.format_error("Error occur while writing the data, due to: %s", e.toString());
			ret = CmnDef.RET_FAILURE_IO_OPERATION;
		}
		CmnLogger.format_debug("Write %d data in: %s", count, csv_filepath);

		return ret;
	}

	public void set_write_data(final ArrayList<String> intput_data_list)
	{
		if (handler_mode != HandlerMode.HandlerMode_Write)
		{
			CmnLogger.error("CSV Handler is NOT in WRITE mode");
			throw new IllegalStateException("CSV Handler is NOT in WRITE mode");
		}
		if (data_list != null)
		{
			CmnLogger.error("CSV data has already been set");
			throw new IllegalStateException("CSV data has already been set");
		}
		data_list = intput_data_list;
	}

	@Override
	public Iterator<String> iterator()
	{
		if (handler_mode != HandlerMode.HandlerMode_Read)
		{
			CmnLogger.error("CSV Handler is NOT in READ mode");
			throw new IllegalStateException("CSV Handler is NOT in READ mode");
		}
		Iterator<String> it = new Iterator<String>()
		{
			int data_list_len = data_list.size();
			int cur_index = 0;
			@Override
			public boolean hasNext()
			{
				return cur_index < data_list_len;
			}
			@Override
			public String next()
			{
				return data_list.get(cur_index++);
			}
			@Override
			public void remove() {throw new UnsupportedOperationException();}
		};
		return it;
	}
}
