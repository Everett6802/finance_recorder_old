package com.price.finance_recorder;

import java.io.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.*;
import java.util.regex.*;

import com.price.finance_recorder.FinanceRecorderCmnDef.FinaceDataType;


public class FinanceRecorderMgr implements FinanceRecorderCmnDef.FinanceObserverInf
{
	private class TimeRangeCfg
	{
		public String month_start_str; // Format: "2015-10"
		public String month_end_str; // Format: "2015-10"

		public TimeRangeCfg(String start_str, String end_str)
		{
			month_start_str = start_str;
			month_end_str = end_str;
		}
		@Override
		public String toString() 
		{
			// TODO Auto-generated method stub
			return String.format("%s:%s", month_start_str, month_end_str);
		}
	};

	private HashMap<Integer,TimeRangeCfg> finance_source_time_range_table = new HashMap<Integer, TimeRangeCfg>();

	public short parse_config_file(String filename)
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
				int finace_data_type_index = Arrays.asList(FinanceRecorderCmnDef.FINANCE_DATA_DESCRIPTION_LIST).indexOf(data_source);
				if (finace_data_type_index == -1)
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
				if (parse_time_range(time_month_begin) == null)
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
				if (parse_time_range(time_month_end) == null)
				{
					FinanceRecorderCmnDef.format_error("Incorrect end time format[%s] in config file", time_month_end);
					ret = FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
					break OUT;
				}

				FinanceRecorderCmnDef.format_debug("New entry in config [%s %s:%s]", data_source, time_month_begin, time_month_end);
				TimeRangeCfg time_range_cfg = new TimeRangeCfg(time_month_begin, time_month_end);
				finance_source_time_range_table.put(finace_data_type_index, time_range_cfg);
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

	private Matcher parse_time_range(String time_str)
	{
		// Time Format: yyyy-mm; Ex: 2015-10 
		Pattern pattern = Pattern.compile("([\\d]{4})-([\\d]{1,2})");
		Matcher matcher = pattern.matcher(time_str);
		if (!matcher.find())
		{
			FinanceRecorderCmnDef.format_error("Incorrect time format: %s", time_str);
			return null;
		}
		return matcher;
	}

	private int[] get_start_and_end_time_range(String time_start_str, String time_end_str)
	{			
		Matcher month_start_matcher = parse_time_range(time_start_str);
		if (month_start_matcher == null)
		{
			FinanceRecorderCmnDef.format_error("Incorrect time format (start): %s", time_start_str);
			return null;
		}
		Matcher month_end_matcher = parse_time_range(time_end_str);
		if (month_end_matcher == null)
		{
			FinanceRecorderCmnDef.format_error("Incorrect time format (End): %s", time_end_str);
			return null;
		}
	
		int year_start = Integer.valueOf(month_start_matcher.group(1));
		int month_start = Integer.valueOf(month_start_matcher.group(2));
		int year_end = Integer.valueOf(month_end_matcher.group(1));
		int month_end = Integer.valueOf(month_end_matcher.group(2));

		return new int[]{year_start, month_start, year_end, month_end};
	}

	public short write()
	{
// Write the data into MySQL one by one
		short ret;
		for (Map.Entry<Integer, TimeRangeCfg> entry : finance_source_time_range_table.entrySet())
		{
			int finace_data_type_index = entry.getKey();
			FinanceRecorderWriter finance_recorder_writer = new FinanceRecorderWriter(FinanceRecorderCmnDef.FinaceDataType.valueOf(finace_data_type_index));
			TimeRangeCfg time_range_cfg = entry.getValue();

			FinanceRecorderCmnDef.format_debug("Try to write data [%s %s:%s] into MySQL......", finance_recorder_writer.get_description(), time_range_cfg.month_start_str, time_range_cfg.month_end_str);
// Setup the time range
			int[] time_list = get_start_and_end_time_range(time_range_cfg.month_start_str, time_range_cfg.month_end_str);
			if (time_list == null)
				return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
// Write the data into MySQL
			ret = finance_recorder_writer.write_to_sql(time_list[0], time_list[1], time_list[2], time_list[3], FinanceRecorderCmnDef.DatabaseCreateThreadType.DatabaseCreateThread_Single);
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
			for (Map.Entry<Integer, TimeRangeCfg> entry : finance_source_time_range_table.entrySet())
			{
				int finace_data_type_index = entry.getKey();
//				FinanceRecorderWriter finance_recorder_writer = new FinanceRecorderWriter(FinanceRecorderCmnDef.FinaceDataType.valueOf(finace_data_type_index));
				TimeRangeCfg time_range_cfg = entry.getValue();
// Setup the time range
				int[] time_list = get_start_and_end_time_range(time_range_cfg.month_start_str, time_range_cfg.month_end_str);
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
					year_cur_end = year_start + (int)((month_start + month_offset) / 12);
					month_cur_end = (month_start + month_offset) % 12;
					int year_to_month_cur_end = year_cur_end * 12 + month_cur_end;
					if (year_to_month_cur_end > year_to_month_end)
					{
						year_cur_end = year_end;
						month_cur_end = month_end;
					}
// Write the data into MySQL
					FinanceRecorderWriter finance_recorder_writer = new FinanceRecorderWriter(FinanceRecorderCmnDef.FinaceDataType.valueOf(finace_data_type_index));
//					FinanceRecorderCmnDef.format_debug("Try to write data [%s %04d%02d:%04d%02d] into MySQL......", finance_recorder_writer.get_description(), year_start, month_start, year_cur_end, month_cur_end);
					FinanceRecorderWriterTask task = new FinanceRecorderWriterTask(new FinanceRecorderWriter(FinanceRecorderCmnDef.FinaceDataType.valueOf(finace_data_type_index)), year_start, month_start, year_cur_end, month_cur_end);
					Future<Integer> res = executor.submit(task);
//					try{Thread.sleep(500);}
//					catch (InterruptedException e){}
					res_list.add(res);

					if (year_cur_end == year_end && month_cur_end == month_end)
						break;
					year_start = year_start + (int)((month_start + FinanceRecorderCmnDef.MAX_MONTH_RANGE_IN_THREAD) / 12);
					month_start = (month_start + FinanceRecorderCmnDef.MAX_MONTH_RANGE_IN_THREAD) % 12;
				}
			}
// Check the result
			for (Future<Integer> res : res_list)
			{
				try
				{
					ret = res.get().shortValue();
				}
				catch (Exception e)
				{
					FinanceRecorderCmnDef.format_error("Fail to get return value, due to: %s", e.toString());
					ret = FinanceRecorderCmnDef.RET_FAILURE_UNKNOWN;
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

	public short clear(int finance_data_type_index)
	{
		FinanceRecorderWriter finance_recorder_writer = new FinanceRecorderWriter(FinanceRecorderCmnDef.FinaceDataType.valueOf(finance_data_type_index));
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
		for (int finance_data_type_index = 0 ; finance_data_type_index < FinaceDataType.values().length ; finance_data_type_index++)
		{
			ret = clear(finance_data_type_index);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				return ret;
		}

		return ret;
	}
}
