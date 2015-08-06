package com.price.stock_recorder;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;


public class StockRecorderMgr implements Runnable, StockRecorderCmnDef.StockObserverInf
{
	BufferedReader br = null;
	String config_filepath = null;

	private static final String[] CONF_FILE_TITLE_LIST = new String[]{"reader_method", "writer_method", "data_filename", "database_name"};
	private static final String[] SQL_TITLE_LIST = new String[]{"date", "open", "high", "low", "close", "volume"};
	private static final String CONF_FILENAME = "stock_recorder.conf";
	private static final String SQL_FIELD_POSITION_DEFINITION = "sql_field_position_definition";

	private String reader_method = null;
	private String writer_method = null;
	private String data_filename = null;
	private String database_name = null;

	private StockRecorderCmnDef.StockReaderInf stock_reader = null;
	private StockRecorderCmnDef.StockWriterInf stock_writer = null;
	private List<String> file_sql_field_mapping = new ArrayList();

	private Thread t = null;
	private AtomicInteger runtime_ret = new AtomicInteger(StockRecorderCmnDef.RET_SUCCESS);

	public short parse_config()
	{
		String current_path = StockRecorderCmnDef.get_current_path();
		String conf_filepath = String.format("%s/%s/%s", current_path, StockRecorderCmnDef.CONF_FOLDERNAME, CONF_FILENAME);

		StockRecorderCmnDef.debug(String.format("Check the file[%s] exist", conf_filepath));
		File f = new File(conf_filepath);
		if (!f.exists())
		{
			StockRecorderCmnDef.format_error("The stock recoder configration file[%s] does NOT exist", conf_filepath);
			return StockRecorderCmnDef.RET_FAILURE_NOT_FOUND;
		}

// Open the conf file for reading
		BufferedReader br = null;
		try
		{
			FileInputStream fis = new FileInputStream(f);
			InputStreamReader isr = new InputStreamReader(fis);
			br = new BufferedReader(isr);
		}
		catch (IOException e)
		{
			StockRecorderCmnDef.format_error("Fails to open %s file, due to: %s", conf_filepath, e.toString());
			return StockRecorderCmnDef.RET_FAILURE_IO_OPERATION;
		}
		short ret = StockRecorderCmnDef.RET_SUCCESS;
// Read the conf file
		try
		{
			boolean sql_field_position_start = false;
			int index;
			String line = null;
			String title = null;
			String value = null;
			boolean date_field_found = false;
			while ((line = br.readLine()) != null)
			{
				if (!sql_field_position_start)
				{
					if (line.startsWith(SQL_FIELD_POSITION_DEFINITION))
					{
						sql_field_position_start = true;
						continue;
					}
				}

				index = line.indexOf('=');
				if (index == -1)
				{
					StockRecorderCmnDef.format_error("Incorrect config parameter: %s", line);
					ret = StockRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
					break;
				}

				title = line.substring(0, index);
				value = line.substring(index + 1);
				StockRecorderCmnDef.format_debug("Title: %s, Value: %s", title, value);
				if (sql_field_position_start)
				{
					boolean found = false;
					for (int i = 0 ; i < SQL_TITLE_LIST.length ; i++)
					{
						if (title.equals(SQL_TITLE_LIST[i]))
						{
							found = true;
							if (!date_field_found)
							{
								if (title.equals(SQL_TITLE_LIST[0]))
									date_field_found = true;
							}
							break;
						}
					}
					if (!found)
					{
						StockRecorderCmnDef.format_error("Unknown SQL config parameter title: %s", title);
						ret = StockRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
						break;
					}
					file_sql_field_mapping.add(value);
				}
				else
				{
					OUT:
					for (int i = 0 ; i < CONF_FILE_TITLE_LIST.length ; i++)
					{
						if (title.equals(CONF_FILE_TITLE_LIST[i]))
						{
							switch (i)
							{
							case 0:
								reader_method = value;
								break;
							case 1:
								writer_method = value;
								break;
							case 2:
								data_filename = value;
								break;
							case 3:
								database_name = value;
								break;
							default:
								StockRecorderCmnDef.format_error("Unknown config parameter title: %s", title);
								ret = StockRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
								break OUT;
							}
						}
					}
					if (StockRecorderCmnDef.CheckFailure(ret))
						break;
				}
			}
			if (!date_field_found)
			{
				if (StockRecorderCmnDef.CheckSuccess(ret))
				{
					StockRecorderCmnDef.error("The 'date' field is a MUST");
					ret = StockRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
				}
			}
		}
		catch (IOException e)
		{
			StockRecorderCmnDef.format_error("Error occur while parsing the config file, due to: %s", e.toString());
			ret = StockRecorderCmnDef.RET_FAILURE_IO_OPERATION;
		}
		finally
		{
			if (br != null)
			{
				try{br.close();}
				catch(Exception e){}
				br = null;
			}
		}

		return ret;
	}

	public short initialize()
	{
// Parse the config from the file
		short ret = parse_config();
		if (StockRecorderCmnDef.CheckFailure(ret))
			return ret;

// Initialize objects according to the conf file
		try
		{
			StockRecorderCmnDef.format_debug("Try to initialize the StockReader%s object", reader_method);
			stock_reader = (StockRecorderCmnDef.StockReaderInf)Class.forName("com.price.stock_recorder.StockReader" + reader_method).newInstance();
			StockRecorderCmnDef.format_debug("Try to initialize the StockWriter%s object", writer_method);
			stock_writer = (StockRecorderCmnDef.StockWriterInf)Class.forName("com.price.stock_recorder.StockWriter" + writer_method).newInstance();
		}
		catch (ClassNotFoundException e)
		{
			StockRecorderCmnDef.format_error("Class not found, due to: %s", e.toString());
			return StockRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
		} 
		catch (InstantiationException e) 
		{
			StockRecorderCmnDef.format_error("Instantiation fails, due to: %s", e.toString());
			return StockRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
		}
		catch (IllegalAccessException e)
		{
			StockRecorderCmnDef.format_error("Illegal access, due to: %s", e.toString());
			return StockRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
		}

		ret = stock_reader.initialize(this, data_filename);
		if (StockRecorderCmnDef.CheckFailure(ret))
			return ret;
		ret = stock_writer.initialize(this, database_name, file_sql_field_mapping);
		if (StockRecorderCmnDef.CheckFailure(ret))
			return ret;

		return StockRecorderCmnDef.RET_SUCCESS;
	}

	public short record()
	{
		StockRecorderCmnDef.debug("Start to record data to database...");
		t = new Thread(this);
		t.start();

		StockRecorderCmnDef.debug("Wait for the worker thread of recording data to database...");
		try
		{
			t.join();
		}
		catch (InterruptedException e)
		{
			StockRecorderCmnDef.debug("Got an Interrupted Exception while waiting for the death of the worker thread...");
		}

		short ret = runtime_ret.shortValue();
		return ret;
	}

	public short deinitialize()
	{
		short ret = StockRecorderCmnDef.RET_SUCCESS;
		if (stock_reader != null)
		{
			ret = stock_reader.deinitialize();
			if (StockRecorderCmnDef.CheckFailure(ret))
				return ret;
		}
		if (stock_writer != null)
		{
			ret = stock_writer.deinitialize();
			if (StockRecorderCmnDef.CheckFailure(ret))
				return ret;
		}

		return StockRecorderCmnDef.RET_SUCCESS;
	}

	@Override
	public void run()
	{
		StockRecorderCmnDef.debug("The worker thread of recording data to database is working......");
		List<String> data_list = new LinkedList<String>();
		short ret = StockRecorderCmnDef.RET_SUCCESS;
		int count = 0;
		while (true)
		{
// Read the data from the source
			ret = stock_reader.read(data_list);
			if (StockRecorderCmnDef.CheckFailure(ret))
			{
				runtime_ret.set(ret);
				break;
			}

// There is no more data...
			if (data_list.isEmpty())
			{
				StockRecorderCmnDef.format_info("All the data in %s are read", data_filename);
				break;
			}

// Write the data into the sinker
			ret = stock_writer.write(data_list);
			if (StockRecorderCmnDef.CheckFailure(ret))
			{
				runtime_ret.set(ret);
				break;
			}
			count += data_list.size();
			data_list.clear();
		}
		StockRecorderCmnDef.format_debug("Got %d data", count);
		StockRecorderCmnDef.debug("The worker thread of recording data to database is going to die !!!");
	}

	@Override
	public short notify(short type)
	{
		short ret = StockRecorderCmnDef.RET_SUCCESS;
		switch (type)
		{
//		case StockRecorderCmnDef.NOTIFY_GET_DATA:
//			break;
		default:
			ret = StockRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
			break;
		}

		return ret;
	}

}
