package com.price.finance_recorder;

import java.io.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.*;
import java.util.concurrent.ExecutionException;;


public class FinanceRecorderMgr implements FinanceRecorderCmnDef.FinanceObserverInf
{
	private HashMap<Integer,FinanceRecorderCmnDef.TimeRangeCfg> finance_source_time_range_table = new HashMap<Integer, FinanceRecorderCmnDef.TimeRangeCfg>();

	public short update_by_config_file(String filename)
	{
		String current_path = FinanceRecorderCmnDef.get_current_path();
		String conf_filepath = String.format("%s/%s/%s", current_path, FinanceRecorderCmnDef.CONF_FOLDERNAME, filename);
		FinanceRecorderCmnDef.debug(String.format("Check the config file[%s] exist", conf_filepath));
		File f = new File(conf_filepath);
		if (!f.exists())
		{
			FinanceRecorderCmnDef.format_error("The configration file[%s] does NOT exist", conf_filepath);
			return FinanceRecorderCmnDef.RET_FAILURE_NOT_FOUND;
		}

// Open the config file for reading
		BufferedReader br = null;
		try
		{
			FileInputStream fis = new FileInputStream(f);
			InputStreamReader isr = new InputStreamReader(fis);
			br = new BufferedReader(isr);
		}
		catch (IOException e)
		{
			FinanceRecorderCmnDef.format_error("Fails to open %s file, due to: %s", conf_filepath, e.toString());
			return FinanceRecorderCmnDef.RET_FAILURE_IO_OPERATION;
		}

		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
// Read the conf file
		try
		{
			String line = null;
			String data_source;
			String time_month_begin;
			String time_month_end;
			String time_month_today = null;
OUT:
			while ((line = br.readLine()) != null)
			{
				if (line.startsWith(FinanceRecorderCmnDef.CONF_ENTRY_IGNORE_FLAG) || line.length() == 0)
				{
					FinanceRecorderCmnDef.format_debug("Ignore the entry: %s", line);
					continue;
				}

				String[] entry_arr = line.split(" ");
// Get the type of data source
				data_source = entry_arr[0];
				int finance_data_type_index = Arrays.asList(FinanceRecorderCmnDef.FINANCE_DATA_DESCRIPTION_LIST).indexOf(data_source);
				if (finance_data_type_index == -1)
				{
					FinanceRecorderCmnDef.format_error("Unknown data source type[%s] in config file", data_source);
					ret = FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
					break OUT;
				}
// Get the time of start time
				if (entry_arr.length >= 2)
					time_month_begin = entry_arr[1];
				else
				{
					if (time_month_today == null)
						time_month_today = FinanceRecorderCmnDef.get_time_month_today();
					time_month_begin = time_month_today;
				}
				if (FinanceRecorderCmnDef.get_month_value_matcher(time_month_begin) == null)
				{
					FinanceRecorderCmnDef.format_error("Incorrect begin time format[%s] in config file", time_month_begin);
					ret = FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
					break OUT;
				}
// Get the time of start time
				if (entry_arr.length >= 3)
					time_month_end = entry_arr[2];
				else
				{
					if (time_month_today == null)
						time_month_today = FinanceRecorderCmnDef.get_time_month_today();
					time_month_end = time_month_today;
				}
				if (FinanceRecorderCmnDef.get_month_value_matcher(time_month_end) == null)
				{
					FinanceRecorderCmnDef.format_error("Incorrect end time format[%s] in config file", time_month_end);
					ret = FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
					break OUT;
				}

				FinanceRecorderCmnDef.format_debug("New entry in config [%s %s:%s]", data_source, time_month_begin, time_month_end);
				FinanceRecorderCmnDef.TimeRangeCfg time_range_cfg = new FinanceRecorderCmnDef.TimeRangeCfg(time_month_begin, time_month_end);
				finance_source_time_range_table.put(finance_data_type_index, time_range_cfg);
			}
		}
		catch (IOException e)
		{
			FinanceRecorderCmnDef.format_error("Error occur while parsing the config file, due to: %s", e.toString());
			ret = FinanceRecorderCmnDef.RET_FAILURE_IO_OPERATION;
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

	public short update_by_parameter(LinkedList<Integer> finance_data_type_index_list, String time_month_begin, String time_month_end)
	{
// Check the time of start time
		if (FinanceRecorderCmnDef.get_date_value_matcher(time_month_begin) == null)
		{
			FinanceRecorderCmnDef.format_error("Incorrect begin time format[%s] in config file", time_month_begin);
			return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
		}
// Check the time of start time
		if (FinanceRecorderCmnDef.get_date_value_matcher(time_month_end) == null)
		{
			FinanceRecorderCmnDef.format_error("Incorrect end time format[%s] in config file", time_month_end);
			return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
		}

		for (Integer finance_data_type_index : finance_data_type_index_list)
		{
			FinanceRecorderCmnDef.format_debug("New entry in config [%s %s:%s]", finance_data_type_index, time_month_begin, time_month_end);
			FinanceRecorderCmnDef.TimeRangeCfg time_range_cfg = new FinanceRecorderCmnDef.TimeRangeCfg(time_month_begin, time_month_end);
			finance_source_time_range_table.put(finance_data_type_index, time_range_cfg);
		}
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	@Override
	public short notify(short type)
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		switch (type)
		{
//		case StockRecorderCmnDef.NOTIFY_GET_DATA:
//			break;
		default:
			ret = FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
			break;
		}
		return ret;
	}

	public short write()
	{
// Write the data into MySQL one by one
		short ret;
		for (Map.Entry<Integer, FinanceRecorderCmnDef.TimeRangeCfg> entry : finance_source_time_range_table.entrySet())
		{
			int finance_data_type_index = entry.getKey();
			FinanceRecorderWriter finance_recorder_writer = new FinanceRecorderWriter(FinanceRecorderCmnDef.FinanceDataType.valueOf(finance_data_type_index));
			FinanceRecorderCmnDef.TimeRangeCfg time_range_cfg = entry.getValue();

			FinanceRecorderCmnDef.format_debug("Try to write data [%s %s] into MySQL......", finance_recorder_writer.get_description(), time_range_cfg.toString());
// Write the data into MySQL
			ret = finance_recorder_writer.write_to_sql(time_range_cfg, FinanceRecorderCmnDef.DatabaseCreateThreadType.DatabaseCreateThread_Single, FinanceRecorderCmnDef.DatabaseEnableBatchType.DatabaseEnableBatch_No);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				return ret;
		}
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	public short write_by_multithread()
	{
		ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(FinanceRecorderCmnDef.MAX_CONCURRENT_THREAD);
		LinkedList<Future<Integer>> res_list = new LinkedList<Future<Integer>>();
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
OUT:
		do
		{
			for (Map.Entry<Integer, FinanceRecorderCmnDef.TimeRangeCfg> entry : finance_source_time_range_table.entrySet())
			{
				int finance_data_type_index = entry.getKey();
//				FinanceRecorderWriter finance_recorder_writer = new FinanceRecorderWriter(FinanceRecorderCmnDef.financeDataType.valueOf(finance_data_type_index));
				FinanceRecorderCmnDef.TimeRangeCfg time_range_cfg = entry.getValue();
// Setup the time range
				int[] time_list = FinanceRecorderCmnDef.get_start_and_end_month_value_range(time_range_cfg);
				if (time_list == null)
				{
					ret = FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
					break OUT;
				}
				int year_start = time_list[0]; 
				int month_start = time_list[1];
				int year_end = time_list[2];
				int month_end = time_list[3];

				int year_cur_end = year_start;
				int month_cur_end = month_start;
				int month_offset = FinanceRecorderCmnDef.MAX_MONTH_RANGE_IN_THREAD - 1;
				int year_to_month_end = year_end * 12 + month_end;
				while(true)
				{
					month_cur_end = month_start + month_offset;
					year_cur_end = year_start;
					while (month_cur_end > 12)
					{
						year_cur_end++;
						month_cur_end -= 12;
					}

					int year_to_month_cur_end = year_cur_end * 12 + month_cur_end;
					if (year_to_month_cur_end > year_to_month_end)
					{
						year_cur_end = year_end;
						month_cur_end = month_end;
					}
// Write the data into MySQL
					FinanceRecorderWriter finance_recorder_writer = new FinanceRecorderWriter(FinanceRecorderCmnDef.FinanceDataType.valueOf(finance_data_type_index));
//					FinanceRecorderCmnDef.format_debug("Try to write data [%s %04d%02d:%04d%02d] into MySQL......", finance_recorder_writer.get_description(), year_start, month_start, year_cur_end, month_cur_end);
					FinanceRecorderWriterTask task = new FinanceRecorderWriterTask(new FinanceRecorderWriter(FinanceRecorderCmnDef.FinanceDataType.valueOf(finance_data_type_index)), new FinanceRecorderCmnDef.TimeRangeCfg(year_start, month_start, year_cur_end, month_cur_end));
					Future<Integer> res = executor.submit(task);
//					try{Thread.sleep(5);}
//					catch (InterruptedException e){}
					res_list.add(res);

					if (year_cur_end == year_end && month_cur_end == month_end)
						break;
					month_start = month_start + FinanceRecorderCmnDef.MAX_MONTH_RANGE_IN_THREAD;
					while (month_start > 12)
					{
						year_start++;
						month_start -= 12;
					}
				}
			}
// Check the result
			for (Future<Integer> res : res_list)
			{
				try
				{
					ret = res.get().shortValue();
				}
				catch (ExecutionException e)
				{
					FinanceRecorderCmnDef.format_error("Fail to get return value, due to: %s", e.toString());
					ret = FinanceRecorderCmnDef.RET_FAILURE_UNKNOWN;
				}
				catch (InterruptedException e)
				{
					FinanceRecorderCmnDef.format_error("Fail to get return value, due to: %s", e.toString());
					ret = FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
				}
				if (FinanceRecorderCmnDef.CheckFailure(ret))
					break OUT;
			}
		}while(false);
// Shut down the executor
		if(FinanceRecorderCmnDef.CheckSuccess(ret))
			executor.shutdown();
		else
			executor.shutdownNow();

		return ret;
	}

	public short read(	List<List<String>> total_data_list)
	{
// Write the data into MySQL one by one
		short ret;
		for (Map.Entry<Integer, FinanceRecorderCmnDef.TimeRangeCfg> entry : finance_source_time_range_table.entrySet())
		{
//			LinkedList<String> data_list = new LinkedList<String>();
			int finance_data_type_index = entry.getKey();
			FinanceRecorderWriter finance_recorder_writer = new FinanceRecorderWriter(FinanceRecorderCmnDef.FinanceDataType.valueOf(finance_data_type_index));
			FinanceRecorderCmnDef.TimeRangeCfg time_range_cfg = entry.getValue();

			FinanceRecorderCmnDef.format_debug("Try to read data [%s %s] from MySQL......", finance_recorder_writer.get_description(), time_range_cfg.toString());
// Write the data into MySQL
			total_data_list.add(new LinkedList<String>());
			LinkedList<String> data_list = (LinkedList<String>)total_data_list.get(total_data_list.size() - 1);
			ret = finance_recorder_writer.read_from_sql(time_range_cfg, data_list);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
// If the database does NOT exist, just ignore the warning
				if (!FinanceRecorderCmnDef.CheckMySQLFailureUnknownDatabase(ret))
					return ret;
		}
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	public short clear(int finance_data_type_index)
	{
		FinanceRecorderWriter finance_recorder_writer = new FinanceRecorderWriter(FinanceRecorderCmnDef.FinanceDataType.valueOf(finance_data_type_index));
		FinanceRecorderCmnDef.format_debug("Try to delete database [%s]......", finance_recorder_writer.get_description());
		return finance_recorder_writer.delete_sql();
	}

	public short clear_multi(LinkedList<Integer> finance_data_type_index_list)
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		for (Integer finance_data_type_index : finance_data_type_index_list)
		{
			ret = clear(finance_data_type_index);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				return ret;
		}

		return ret;
	}

	public short clear_all()
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		for (int finance_data_type_index = 0 ; finance_data_type_index < FinanceRecorderCmnDef.FinanceDataType.values().length ; finance_data_type_index++)
		{
			ret = clear(finance_data_type_index);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				return ret;
		}

		return ret;
	}
}
