package com.price.finance_recorder;

import java.io.*;
import java.util.*;


public class FinanceRecorder 
{
	enum ActionType{Action_None, Action_Read, Action_Write, Action_ReadWrite};
	static final String PARAM_SPLIT = ",";
	static FinanceRecorderMgr finance_recorder_mgr = new FinanceRecorderMgr();

	public static void main(String args[])
	{
//		java.util.Date date_now = new java.util.Date();
//		Calendar cal = Calendar.getInstance();
//		cal.setTime(date_now);
//		String time_month = String.format("%02d-%02d-%02d %02d:%02d:%02d", cal.get(Calendar.YEAR) % 100, cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE), cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
//		System.out.print(time_month);
//		List<String> data_list = new ArrayList<String>();
//		data_list.add("1,2,3,4\n");
//		data_list.add("11,22,33,44\n");
//		data_list.add("111,222,333,444\n");
//		FinanceRecorderCSVHandler csv_writer = new FinanceRecorderCSVHandler(null);
//		String csv_filepath = String.format("%s/%s/%s_%04d%02d.csv", FinanceRecorderCmnDef.get_current_path(), FinanceRecorderCmnDef.BACKUP_FOLDERNAME, FinanceRecorderCmnDef.FINANCE_DATA_NAME_LIST[0], 2016, 1);
////		FinanceRecorderCmnDef.format_debug("Try to write the CSV: %s", csv_filepath);
//		csv_writer.initialize(csv_filepath, FinanceRecorderCSVHandler.HandlerMode.HandlerMode_Write);
//		csv_writer.write(data_list);
//		csv_writer.deinitialize();
//		System.exit(0);

		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		ret = finance_recorder_mgr.initialize();
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			show_error_and_exit(String.format("Faiil to initialize the Manager class, due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret)));
		
		ActionType action_type = ActionType.Action_None;
		boolean use_multithread = false;
		boolean check_error = false;
		boolean run_daily = false;
		boolean backup_database = false;
		LinkedList<Integer> remove_database_list = null;
		LinkedList<Integer> finance_data_type_index_list = null;
		String time_month_begin = null;
		String time_month_end = null;
		String conf_filename = null;

//		System.out.println("Parse the parameters......");
		int index = 0;
		int index_offset = 0;
		int args_len = args.length;

		while(index < args_len)
		{
			String option = args[index];
			if (option.equals("-h") || option.equals("--help"))
			{
				FinanceRecorderCmnDef.enable_show_console(true);
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
				String[] data_source_array = args[index + 1].split(PARAM_SPLIT);
				for (String data_source : data_source_array)
					remove_database_list.addLast(Integer.valueOf(data_source));
				index_offset = 2;
			}
			else if (option.equals("-s") || option.equals("--source"))
			{
				if (index + 1 >= args_len)
					show_error_and_exit(String.format("The option[%s] does NOT contain value", option));

				finance_data_type_index_list = new LinkedList<Integer>();
				if (args[index + 1].equals("all"))
				{
					for (int source_index = 0 ; source_index < FinanceRecorderCmnDef.FinanceSourceType.values().length ; source_index++)
						finance_data_type_index_list.addLast(source_index);
				}
				else
				{
					String[] data_source_array = args[index + 1].split(PARAM_SPLIT);
					for (String data_source : data_source_array)
						finance_data_type_index_list.addLast(Integer.valueOf(data_source));
				}
				index_offset = 2;
			}
			else if (option.equals("-t") || option.equals("--time"))
			{
				if (index + 1 >= args_len)
					show_error_and_exit(String.format("The option[%s] does NOT contain value", option));

				String[] time_month_array = args[index + 1].split(PARAM_SPLIT);
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
			else if (option.equals("-a") || option.equals("--action"))
			{
				if (index + 1 >= args_len)
					show_error_and_exit(String.format("The option[%s] does NOT contain value", option));

				String action = args[index + 1];
				boolean need_read = (action.indexOf('R') != -1 || action.indexOf('r') != -1 ? true : false);
				boolean need_write = (action.indexOf('W') != -1 || action.indexOf('w') != -1 ? true : false);
				if (need_read && need_write)
					action_type = ActionType.Action_ReadWrite;
				else if (need_read)
					action_type = ActionType.Action_Read;
				else if (need_write)
					action_type = ActionType.Action_Write;
				else
					show_error_and_exit(String.format("Unknown action type: %s", action));
				index_offset = 2;
			}
			else if (option.equals("--remove_old"))
			{
				if (remove_database_list != null)
					show_error_and_exit(String.format("The option[%s] is duplicate", option));

				remove_database_list = new LinkedList<Integer>();
				int data_name_list_length = FinanceRecorderCmnDef.FINANCE_DATA_NAME_LIST.length;
				for (int i = 0 ; i < data_name_list_length ; i++)
					remove_database_list.addLast(i);
				index_offset = 1;
			}
			else if (option.equals("--backup_database"))
			{
				backup_database = true;
				index_offset = 1;
			}
			else if (option.equals("--multi_thread"))
			{
				use_multithread = true;
				index_offset = 1;
			}
			else if (option.equals("--check_error"))
			{
				check_error = true;
				index_offset = 1;
			}
			else if (option.equals("--show_console"))
			{
				FinanceRecorderCmnDef.enable_show_console(true);
				index_offset = 1;
			}
			else if (option.equals("--run_daily"))
			{
				run_daily = true;
				index_offset = 1;
			}
			else
			{
				show_error_and_exit(String.format("Unknown argument: %s", option));
			}
			index += index_offset;
		}
// Setup parameter if necessary
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
			delete_sql(remove_database_list);
		}

// Setup the config for writing data into MySQL
		if (conf_filename != null)
		{
// If the config file is set, parse it...
			if(FinanceRecorderCmnDef.is_show_console())
			{
				if (finance_data_type_index_list != null || time_month_begin != null || time_month_end != null)
					System.out.println("Ingnore the Source/Time parameters");
				System.out.printf("Setup config from file[%s]\n", conf_filename);
			}
			setup_time_range_table(conf_filename);
		}
		else
		{
// If no config file is selected, check if the data source selection are set from the command line
			if (finance_data_type_index_list == null)
			{
				String msg = "No data sources are selected......";
				FinanceRecorderCmnDef.debug(msg);
				if (FinanceRecorderCmnDef.is_show_console())
					System.out.println(msg);
				action_type = ActionType.Action_None;
			}
			else
				setup_time_range_table(finance_data_type_index_list, time_month_begin, time_month_end);
		}
// Should be the first action since the database time range could probably be modified
		if (need_write(action_type))
		{
// Write the financial data into MySQL
			write_sql(use_multithread);
// Check the database and find the time range of each database. Only needed when the content of the MySQL is modified
			check_sql(check_error);
		}
// Initialize the database time range table
		init_database_time_range_table();

		if (run_daily)
		{
// Update the latest information to user
			run_daily();
		}

		if (backup_database)
		{
			backup_sql();
		}
		
		if (need_read(action_type))
		{
				
		}

		FinanceRecorderCmnDef.wait_for_logging();
		System.exit(0);
	}

	private static void show_error_and_exit(String err_msg)
	{
		FinanceRecorderCmnDef.error(err_msg);
		if (FinanceRecorderCmnDef.is_show_console())
			System.err.println(err_msg);
		FinanceRecorderCmnDef.wait_for_logging();
		System.exit(1);
	}

	private static void show_usage()
	{
		if (!FinanceRecorderCmnDef.is_show_console())
		{
			FinanceRecorderCmnDef.warn("STDOUT/STDERR are Disabled");
			return;
		}
		System.out.println("====================== Usage ======================");
		System.out.println("-h|--help\nDescription: The usage");
		System.out.println("-r|--remove\nDescription: Remove some MySQL database(s)");
		System.out.println("  Format: 1,2,3 (Start from 0)");
		System.out.println("-s|--source\nDescription: Type of CSV date file\nCaution: Ignored if -f|--file set");
		System.out.println("  Format: 1,2,3 (Start from 0)");
		System.out.println("  all: All types");
		for (int index = 0 ; index < FinanceRecorderCmnDef.FINANCE_DATABASE_DESCRIPTION_LIST.length ; index++)
			System.out.printf("  %d: %s\n", index, FinanceRecorderCmnDef.FINANCE_DATABASE_DESCRIPTION_LIST[index]);
		System.out.println("-t|--time\nTime: The time range of the CSV data file\nDefault: Current month\nCaution1: Ignored if -f|--file set\nCaution2: -s|--source SHOULD be set if set");
		System.out.println("  Format 1 (start_time): 2015-09");
		System.out.println("  Format 2 (start_time;end_time): 2015-01,2015-09");
		System.out.println("-f|--file\nDescription: Read Source/Time from config file\nCaution: -s|--source and -t|--time are Ignored if set");
		System.out.println("  Format: history.conf");
		System.out.println("-a|--action\nDescription: Read/Write the MySQLCaution: Not read/write MySQL if not set");
		System.out.println("  Type: {R(r), W(w), RW(rw)/WR(wr)");
	    System.out.println("--remove_old\nDescription: Remove the old MySQL databases");
	    System.out.println("--backup_database\nDescription: Backup the current databases");
		System.out.println("--multi_thread\nDescription: Write into MySQL database by using multiple threads");
		System.out.println("--check_error\nDescription: Check if the data in the MySQL database is correct");
		System.out.println("--run_daily\nDescription: Run daily data\nCaution: Executed after writing MySQL data if set");
		System.out.println("--show_console\nDescription: Print the runtime info on STDOUT/STDERR");
		System.out.println("===================================================");
	}

	private static boolean need_read(ActionType type){return (type == ActionType.Action_Read || type == ActionType.Action_ReadWrite) ? true : false;}
	private static boolean need_write(ActionType type){return (type == ActionType.Action_Write || type == ActionType.Action_ReadWrite) ? true : false;}

	private static short init_database_time_range_table()
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		ret = finance_recorder_mgr.init_database_time_range_table();
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			show_error_and_exit(String.format("Fail to initialize the database time range table, due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret)));

		return ret;
	}
	
	private static short setup_time_range_table(String filename)
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		ret = finance_recorder_mgr.setup_time_range_table_by_config_file(filename);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			show_error_and_exit(String.format("Fail to setup the parameters, due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret)));

		return ret;
	}

	private static short setup_time_range_table(LinkedList<Integer> finance_data_type_index_list, String time_month_begin, String time_month_end)
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		ret = finance_recorder_mgr.setup_time_range_table_by_parameter(finance_data_type_index_list, time_month_begin, time_month_end);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			show_error_and_exit(String.format("Fail to setup the parameters, due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret)));

		return ret;
	}

	private static short setup_backup_time_range_table(String filename)
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		ret = finance_recorder_mgr.setup_backup_time_range_table_by_config_file(filename);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			show_error_and_exit(String.format("Fail to setup the parameters, due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret)));

		return ret;
	}

	private static short setup_backup_time_range_table(LinkedList<Integer> finance_data_type_index_list, String time_month_begin, String time_month_end)
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		ret = finance_recorder_mgr.setup_backup_time_range_table_by_parameter(finance_data_type_index_list, time_month_begin, time_month_end);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			show_error_and_exit(String.format("Fail to setup the parameters, due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret)));

		return ret;
	}

	private static short delete_sql(LinkedList<Integer> finance_data_type_index_list)
	{
		if(FinanceRecorderCmnDef.is_show_console())
			System.out.println("Delete old MySQL data......");

		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		ret = finance_recorder_mgr.clear_multi(finance_data_type_index_list);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			show_error_and_exit(String.format("Fail to remove the old MySQL, due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret)));

		return ret;
	}

	private static short backup_sql()
	{
		if(FinanceRecorderCmnDef.is_show_console())
			System.out.println("Backup current MySQL data......");

		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		ret = finance_recorder_mgr.backup_by_multithread();
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			show_error_and_exit(String.format("Fail to backup the MySQL, due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret)));

		return ret;
	}

	private static short write_sql(boolean use_multithread)
	{
// Write the financial data into MySQL
		if(FinanceRecorderCmnDef.is_show_console())
			System.out.println("Write financial data into MySQL......");

		long time_start_millisecond = System.currentTimeMillis();
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		if (use_multithread)
			ret = finance_recorder_mgr.write_by_multithread();
		else
			ret = finance_recorder_mgr.write();
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			show_error_and_exit(String.format("Fail to write financial data into MySQL, due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret)));
		long time_end_millisecond = System.currentTimeMillis();

		if(FinanceRecorderCmnDef.is_show_console())
			System.out.println("Write financial data into MySQL...... Done");

		long time_lapse_millisecond = time_end_millisecond - time_start_millisecond;
		String time_lapse_msg; 
		if (time_lapse_millisecond >= 100 * 1000)
			time_lapse_msg = String.format("######### Time Lapse: %d second(s) #########", (int)((time_end_millisecond - time_start_millisecond) / 1000));
		else if (time_lapse_millisecond >= 10 * 1000)
			time_lapse_msg = String.format("######### Time Lapse: %d second(s) #########", (int)((time_end_millisecond - time_start_millisecond) / 1000));
		else
			time_lapse_msg = String.format("######### Time Lapse: %d second(s) #########", (int)((time_end_millisecond - time_start_millisecond) / 1000));
		FinanceRecorderCmnDef.info(time_lapse_msg);
		if(FinanceRecorderCmnDef.is_show_console())
			System.out.println(time_lapse_msg);

		return ret;
	}

	private static short run_daily()
	{
		if(FinanceRecorderCmnDef.is_show_console())
			System.out.println("Run daily data......");
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		ret = finance_recorder_mgr.run_daily();
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			show_error_and_exit(String.format("Fail to run daily data, due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret)));

		return ret;
	}

//	private static short read_sql()
//	{
//		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
//		FinanceRecorderCmnClass.ResultSet result_set = new FinanceRecorderCmnClass.ResultSet();
//		ret = finance_recorder_mgr.read(result_set);
//// Show the result statistics
//		int index = 0;
//		String msg;
//		for (List<String> data_list : total_data_list)
//		{
//			msg = String.format("%d: %d", index, data_list.size());
//			FinanceRecorderCmnDef.info(msg);
//			if (FinanceRecorderCmnDef.is_show_console())
//				System.out.println(msg);
//			index++;
//		}
////		List<List<String>> total_data_list = new ArrayList<List<String>>();
////		ret = finance_recorder_mgr.read(total_data_list);
////// Show the result statistics
////		int index = 0;
////		String msg;
////		for (List<String> data_list : total_data_list)
////		{
////			msg = String.format("%d: %d", index, data_list.size());
////			FinanceRecorderCmnDef.info(msg);
////			if (FinanceRecorderCmnDef.is_show_console())
////				System.out.println(msg);
////			index++;
////		}
//
//		return ret;
//	}

	private static short check_sql(boolean check_error)
	{
		if (FinanceRecorderCmnDef.is_show_console() && check_error)
			System.out.println("Let's check error......");
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		ret = finance_recorder_mgr.check(check_error);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			show_error_and_exit(String.format("Fail to check data in MySQL, due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret)));

		return ret;
	}
}
