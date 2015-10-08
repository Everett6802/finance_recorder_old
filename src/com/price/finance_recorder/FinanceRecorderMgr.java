package com.price.finance_recorder;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;


public class FinanceRecorderMgr implements Runnable, FinanceRecorderCmnDef.FinanceObserverInf
{
	BufferedReader br = null;
	String config_filepath = null;

	private static final String[] CONF_FILE_TITLE_LIST = new String[]{"reader_method", "writer_method", "data_filename", "database_name"};
	private static final String[] SQL_TITLE_LIST = new String[]{"date", "open", "high", "low", "close", "volume"};
	private static final String[] READER_METHOD_LIST = new String[]{"CSV"};
	private static final String[] WRITER_METHOD_LIST = new String[]{"Stock", "Future", "Option"};
	private static final String CONF_FILENAME = "stock_recorder.conf";
	private static final String SQL_FIELD_POSITION_DEFINITION = "sql_field_position_definition";

	private String reader_method = null;
	private String writer_method = null;
	private String data_filename = null;
	private String database_name = null;

//	private FinanceRecorderCmnDef.FinanceReaderInf finance_reader = null;
//	private FinanceRecorderCmnDef.FinanceWriterInf finance_writer = null;
	private List<String> file_sql_field_mapping = new ArrayList();

	private Thread t = null;
	private AtomicInteger runtime_ret = new AtomicInteger(FinanceRecorderCmnDef.RET_SUCCESS);

	public short parse_config()
	{
//		String current_path = FinanceRecorderCmnDef.get_current_path();
//		String conf_filepath = String.format("%s/%s/%s", current_path, FinanceRecorderCmnDef.CONF_FOLDERNAME, CONF_FILENAME);
//
//		FinanceRecorderCmnDef.debug(String.format("Check the file[%s] exist", conf_filepath));
//		File f = new File(conf_filepath);
//		if (!f.exists())
//		{
//			FinanceRecorderCmnDef.format_error("The stock recoder configration file[%s] does NOT exist", conf_filepath);
//			return FinanceRecorderCmnDef.RET_FAILURE_NOT_FOUND;
//		}
//
//// Open the conf file for reading
//		BufferedReader br = null;
//		try
//		{
//			FileInputStream fis = new FileInputStream(f);
//			InputStreamReader isr = new InputStreamReader(fis);
//			br = new BufferedReader(isr);
//		}
//		catch (IOException e)
//		{
//			FinanceRecorderCmnDef.format_error("Fails to open %s file, due to: %s", conf_filepath, e.toString());
//			return FinanceRecorderCmnDef.RET_FAILURE_IO_OPERATION;
//		}
//		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
//// Read the conf file
//		try
//		{
//			boolean sql_field_position_start = false;
//			int index;
//			String line = null;
//			String title = null;
//			String value = null;
//			boolean date_field_found = false;
//			while ((line = br.readLine()) != null)
//			{
//				if (!sql_field_position_start)
//				{
//					if (line.startsWith(SQL_FIELD_POSITION_DEFINITION))
//					{
//						sql_field_position_start = true;
//						continue;
//					}
//				}
//
//				index = line.indexOf('=');
//				if (index == -1)
//				{
//					FinanceRecorderCmnDef.format_error("Incorrect config parameter: %s", line);
//					ret = FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
//					break;
//				}
//
//				title = line.substring(0, index);
//				value = line.substring(index + 1);
//				FinanceRecorderCmnDef.format_debug("Title: %s, Value: %s", title, value);
//				if (sql_field_position_start)
//				{
//					boolean found = false;
//					for (int i = 0 ; i < SQL_TITLE_LIST.length ; i++)
//					{
//						if (title.equals(SQL_TITLE_LIST[i]))
//						{
//							found = true;
//							if (!date_field_found)
//							{
//								if (title.equals(SQL_TITLE_LIST[0]))
//									date_field_found = true;
//							}
//							break;
//						}
//					}
//					if (!found)
//					{
//						FinanceRecorderCmnDef.format_error("Unknown SQL config parameter title: %s", title);
//						ret = FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
//						break;
//					}
//					file_sql_field_mapping.add(value);
//				}
//				else
//				{
//					OUT:
//					for (int i = 0 ; i < CONF_FILE_TITLE_LIST.length ; i++)
//					{
//						if (title.equals(CONF_FILE_TITLE_LIST[i]))
//						{
//							switch (i)
//							{
//							case 0:
//								reader_method = value;
//								break;
//							case 1:
//								writer_method = value;
//								break;
//							case 2:
//								data_filename = value;
//								break;
//							case 3:
//								database_name = value;
//								break;
//							default:
//								FinanceRecorderCmnDef.format_error("Unknown config parameter title: %s", title);
//								ret = FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
//								break OUT;
//							}
//						}
//					}
//					if (FinanceRecorderCmnDef.CheckFailure(ret))
//						break;
//				}
//			}
//			if (!date_field_found)
//			{
//				if (FinanceRecorderCmnDef.CheckSuccess(ret))
//				{
//					FinanceRecorderCmnDef.error("The 'date' field is a MUST");
//					ret = FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
//				}
//			}
//		}
//		catch (IOException e)
//		{
//			FinanceRecorderCmnDef.format_error("Error occur while parsing the config file, due to: %s", e.toString());
//			ret = FinanceRecorderCmnDef.RET_FAILURE_IO_OPERATION;
//		}
//		finally
//		{
//			if (br != null)
//			{
//				try{br.close();}
//				catch(Exception e){}
//				br = null;
//			}
//		}
//
//// Check the config
//		if (Arrays.asList(READER_METHOD_LIST).indexOf(reader_method) == -1)
//		{
//			FinanceRecorderCmnDef.format_error("UnSupported Reader Method: %s", reader_method);
//			return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
//		}
//		if (Arrays.asList(WRITER_METHOD_LIST).indexOf(writer_method) == -1)
//		{
//			FinanceRecorderCmnDef.format_error("UnSupported Writer Method: %s", writer_method);
//			return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
//		}

		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	public short initialize()
	{
//// Parse the config from the file
//		short ret = parse_config();
//		if (FinanceRecorderCmnDef.CheckFailure(ret))
//			return ret;
//
//// Initialize objects according to the conf file
//		try
//		{
//			FinanceRecorderCmnDef.format_debug("Try to initialize the Finance%sReader object", reader_method);
//			finance_reader = (FinanceRecorderCmnDef.FinanceReaderInf)Class.forName(String.format("com.price.finance_recorder.Finance%sReader",  reader_method)).newInstance();
//			FinanceRecorderCmnDef.format_debug("Try to initialize the Finance%sWriter object", writer_method);
//			finance_writer = (FinanceRecorderCmnDef.FinanceWriterInf)Class.forName(String.format("com.price.finance_recorder.Finance%sWriter", writer_method)).newInstance();
//		}
//		catch (ClassNotFoundException e)
//		{
//			FinanceRecorderCmnDef.format_error("Class not found, due to: %s", e.toString());
//			return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
//		} 
//		catch (InstantiationException e) 
//		{
//			FinanceRecorderCmnDef.format_error("Instantiation fails, due to: %s", e.toString());
//			return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
//		}
//		catch (IllegalAccessException e)
//		{
//			FinanceRecorderCmnDef.format_error("Illegal access, due to: %s", e.toString());
//			return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
//		}
//
//		ret = finance_reader.initialize(this, data_filename);
//		if (FinanceRecorderCmnDef.CheckFailure(ret))
//			return ret;
//		ret = finance_writer.initialize(this, database_name, file_sql_field_mapping);
//		if (FinanceRecorderCmnDef.CheckFailure(ret))
//			return ret;

		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	public short record()
	{
		FinanceRecorderCmnDef.debug("Start to record data to database...");
		t = new Thread(this);
		t.start();

		FinanceRecorderCmnDef.debug("Wait for the worker thread of recording data to database...");
		try
		{
			t.join();
		}
		catch (InterruptedException e)
		{
			FinanceRecorderCmnDef.debug("Got an Interrupted Exception while waiting for the death of the worker thread...");
		}

		short ret = runtime_ret.shortValue();
		return ret;
	}

	public short deinitialize()
	{
//		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
//		if (finance_reader != null)
//		{
//			ret = finance_reader.deinitialize();
//			if (FinanceRecorderCmnDef.CheckFailure(ret))
//				return ret;
//		}
//		if (finance_writer != null)
//		{
//			ret = finance_writer.deinitialize();
//			if (FinanceRecorderCmnDef.CheckFailure(ret))
//				return ret;
//		}

		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	@Override
	public void run()
	{
//		FinanceRecorderCmnDef.debug("The worker thread of recording data to database is working......");
//		List<String> data_list = new LinkedList<String>();
//		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
//		int count = 0;
//		while (true)
//		{
//// Read the data from the source
//			ret = finance_reader.read(data_list);
//			if (FinanceRecorderCmnDef.CheckFailure(ret))
//			{
//				runtime_ret.set(ret);
//				break;
//			}
//
//// There is no more data...
//			if (data_list.isEmpty())
//			{
//				FinanceRecorderCmnDef.format_info("All the data in %s are read", data_filename);
//				break;
//			}
//
//// Write the data into the sinker
//			ret = finance_writer.write(data_list);
//			if (FinanceRecorderCmnDef.CheckFailure(ret))
//			{
//				runtime_ret.set(ret);
//				break;
//			}
//			count += data_list.size();
//			data_list.clear();
//		}
//		FinanceRecorderCmnDef.format_debug("Got %d data", count);
//		FinanceRecorderCmnDef.debug("The worker thread of recording data to database is going to die !!!");
	}

	@Override
	public short notify(short type)
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
//		switch (type)
//		{
////		case StockRecorderCmnDef.NOTIFY_GET_DATA:
////			break;
//		default:
//			ret = FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
//			break;
//		}

		return ret;
	}

}
