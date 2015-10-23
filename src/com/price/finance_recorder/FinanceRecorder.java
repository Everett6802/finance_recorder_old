package com.price.finance_recorder;

import java.io.*;
import java.util.*;


public class FinanceRecorder 
{
	static FinanceRecorderMgr finance_recorder_mgr = new FinanceRecorderMgr();

	public static void main(String args[])
	{
		boolean use_multithread = false;
		LinkedList<Integer> remove_database_list = null;
		LinkedList<Integer> finance_data_type_index_list = null;
		String time_month_begin = null;
		String time_month_end = null;
		String conf_filename = null;
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;

		System.out.println("Parse the parameters......");
		int index = 0;
		int index_offset = 0;
		int args_len = args.length;

		while(index < args_len)
		{
			String option = args[index];
			if (option.equals("-h") || option.equals("--help"))
			{
				show_usage();
				System.exit(0);
			}
			else if (option.equals("-r") || option.equals("--remove"))
			{
				if (index + 1 >= args_len)
					show_error_and_exit(String.format("The option[%s] does NOT contain value", option));
				if (remove_database_list != null)
					show_error_and_exit(String.format("The option[%s] is duplicate", option));

				remove_database_list = new LinkedList<Integer>();
				String[] data_source_array = args[index + 1].split(";");
				for (String data_source : data_source_array)
					remove_database_list.addLast(Integer.valueOf(data_source));
				index_offset = 2;
			}
			else if (option.equals("-s") || option.equals("--source"))
			{
				if (index + 1 >= args_len)
					show_error_and_exit(String.format("The option[%s] does NOT contain value", option));

				finance_data_type_index_list = new LinkedList<Integer>();
				String[] data_source_array = args[index + 1].split(";");
				for (String data_source : data_source_array)
					finance_data_type_index_list.addLast(Integer.valueOf(data_source));
				index_offset = 2;
			}
			else if (option.equals("-t") || option.equals("--time"))
			{
				if (index + 1 >= args_len)
					show_error_and_exit(String.format("The option[%s] does NOT contain value", option));

				String[] time_month_array = args[index + 1].split(";");
				int time_month_array_len = time_month_array.length;
				if (time_month_array_len != 1 && time_month_array_len != 2)
					show_error_and_exit(String.format("Incorrect time format: %s", args[index + 1]));
				time_month_begin = time_month_array[0];
				if (time_month_array_len == 2)
					time_month_end = time_month_array[1];
				index_offset = 2;
			}
			else if (option.equals("-f") || option.equals("--file"))
			{
				if (index + 1 >= args_len)
					show_error_and_exit(String.format("The option[%s] does NOT contain value", option));

				conf_filename = args[index + 1];
				index_offset = 2;
			}
			else if (option.equals("--remove_all"))
			{
				if (remove_database_list != null)
					show_error_and_exit(String.format("The option[%s] is duplicate", option));

				remove_database_list = new LinkedList<Integer>();
				int data_name_list_length = FinanceRecorderCmnDef.FINANCE_DATA_NAME_LIST.length;
				for (int i = 0 ; i < data_name_list_length ; i++)
					remove_database_list.addLast(i);
				index_offset = 1;
			}
			else if (option.equals("--multi_thread"))
			{
				use_multithread = true;
				index_offset = 1;
			}
			else
			{
				show_error_and_exit(String.format("Unknown argument: %s", option));
			}
			index += index_offset;
		}
// Setup parameter is necessary
		if (finance_data_type_index_list != null)
		{
			if (time_month_begin == null)
				time_month_begin = FinanceRecorderCmnDef.get_time_month_today();
			if (time_month_end == null)
				time_month_end = FinanceRecorderCmnDef.get_time_month_today();
		}

// Remove the old database
		if (remove_database_list != null)
		{
			System.out.println("Delete old MySQL data......");
			ret = delete_sql(remove_database_list);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				show_error_and_exit("Fail to write financial data into MySQL");
		}

// Setup the config for writing data into MySQL
		if (conf_filename != null)
		{
			if (finance_data_type_index_list != null || time_month_begin != null || time_month_end != null)
				System.out.println("Ingnore the Source/Time parameters");
			System.out.printf("Setup config from file[%s]\n", conf_filename);
			ret = setup_param(conf_filename);
		}
		else
		{
			if (finance_data_type_index_list == null)
			{
				System.out.println("Nothing to be written into MySQL");
				System.exit(0);
			}
			ret = setup_param(finance_data_type_index_list,time_month_begin, time_month_end);
		}
			
// Write the financial data into MySQL
		System.out.println("Write financial data into MySQL data......");
		long time_start_millisecond = System.currentTimeMillis();
		ret = write_sql(use_multithread);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			show_error_and_exit("Fail to write financial data into MySQL");
		long time_end_millisecond = System.currentTimeMillis();

		System.out.println("Write financial data into MySQL data...... Done");
		System.out.printf("######### Time Lapse: %d second(s) #########\n", (int)((time_end_millisecond - time_start_millisecond) / 1000));
		System.exit(0);
	}

	private static void show_error_and_exit(String err_msg)
	{
		System.err.println(err_msg);
		System.exit(1);
	}

	private static void show_usage()
	{
		System.out.println("====================== Usage ======================");
		System.out.println("-h|--help\nDescription: The usage");
		System.out.println("-r|--remove\nDescription: Remove some MySQL database(s)");
		System.out.println("  Format: 1;2;3");
		System.out.println("-s|--source\nDescription: Type of CSV date file\nDefault: All types\nCaution: Ignored if -f|--file set");
		System.out.println("  Format: 1;2;3");
		for (int index = 0 ; index < FinanceRecorderCmnDef.FINANCE_DATA_DESCRIPTION_LIST.length ; index++)
			System.out.printf("  %d: %s\n", index, FinanceRecorderCmnDef.FINANCE_DATA_DESCRIPTION_LIST[index]);
		System.out.println("-t|--time\nTime: The time range of the CSV data file\nDefault: Current month\nCaution1: Ignored if -f|--file set\nCaution2: -s|--source SHOULD be set if set");
		System.out.println("  Format 1 (start_time): 2015-09");
		System.out.println("  Format 2 (start_time;end_time): 2015-01;2015-09");
		System.out.println("-f|--file\nDescription: Read Source/Time from config file\n\nCaution: -s|--source and -t|--time are Ignored if set");
		System.out.println("  Format: history.conf");
	    System.out.println("--remove_all\nDescription: Remove all the MySQL databases");
		System.out.println("--multiple_thread\nDescription: Write into MySQL database by using multiple threads");
		System.out.println("===================================================");
	}

	private static short setup_param(String filename)
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		finance_recorder_mgr.update_by_config_file(filename);
		return ret;
	}
	private static short setup_param(LinkedList<Integer> finance_data_type_index_list, String time_month_begin, String time_month_end)
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		finance_recorder_mgr.update_by_parameter(finance_data_type_index_list, time_month_begin, time_month_end);
		return ret;
	}

	private static short delete_sql(LinkedList<Integer> finance_data_type_index_list)
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		finance_recorder_mgr.clear_multi(finance_data_type_index_list);
		return ret;
	}

	private static short write_sql(boolean use_multithread)
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		if (use_multithread)
			ret = finance_recorder_mgr.write_by_multithread();
		else
			ret = finance_recorder_mgr.write();
		return ret;
	}
}
