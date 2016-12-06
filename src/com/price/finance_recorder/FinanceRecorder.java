package com.price.finance_recorder;

//import java.io.*;
//import java.util.*;
import java.util.LinkedList;
//import java.util.List;
import com.price.finance_recorder_cmn.FinanceRecorderCmnClass;
import com.price.finance_recorder_cmn.FinanceRecorderCmnDef;
import com.price.finance_recorder_base.FinanceRecorderMgrInf;
//import com.price.finance_recorder_stock.FinanceRecorderCmnClassCompanyProfile;
import com.price.finance_recorder_market.FinanceRecorderMarketMgr;
import com.price.finance_recorder_stock.FinanceRecorderStockMgr;


public class FinanceRecorder 
{
	private static final String PARAM_SPLIT = ",";
	private static final byte DATABASE_OPERATION_WRITE_MASK = 0x1;
	private static final byte DATABASE_OPERATION_BACKUP_MASK = 0x1 << 1;
	private static final byte DATABASE_OPERATION_DELETE_MASK = 0x1 << 2;
	private static final byte DATABASE_OPERATION_CLEANUP_MASK = 0x1 << 3;
	private static final byte DATABASE_OPERATION_RESTORE_MASK = 0x1 << 4;

	private static boolean help_param = false;
	private static String finance_folderpath_param = null;
	private static String finance_backup_folderpath_param = null;
	private static String source_from_file_param = null;
	private static String source_param = null;
	private static String time_range_param = null;
	private static String company_from_file_param = null;
	private static String company_param = null;
	private static String delete_sql_accurancy_param = null;
	private static String database_operation_param = null;

	private static FinanceRecorderMgrInf finance_recorder_mgr = null;
	private static byte database_operation = 0x0;
	private static FinanceRecorderCmnClass.FinanceTimeRange finance_time_range = null;
	private static FinanceRecorderCmnDef.DeleteSQLAccurancyType delete_sql_accurancy_type = null;

	private static boolean is_write_operation_enabled(){return (database_operation & DATABASE_OPERATION_WRITE_MASK) != 0;}
	private static boolean is_backup_operation_enabled(){return (database_operation & DATABASE_OPERATION_BACKUP_MASK) != 0;}
	private static boolean is_delete_operation_enabled(){return (database_operation & DATABASE_OPERATION_DELETE_MASK) != 0;}
	private static boolean is_cleanup_operation_enabled(){return (database_operation & DATABASE_OPERATION_CLEANUP_MASK) != 0;}
	private static boolean is_restore_operation_enabled(){return (database_operation & DATABASE_OPERATION_RESTORE_MASK) != 0;}
//	private static ActionType action_type = ActionType.Action_None;
//	static boolean use_multithread = false;
//	static boolean check_error = false;
//	static boolean run_daily = false;
//	static boolean backup_database = false;
//	static boolean list_database_folder = false;
//	boolean cleanup_database_folder = false;
//	boolean restore_database = false;
//	boolean copy_backup_folder = false;
//	String restore_folderpath = null;
//	String restore_foldername = null;
//	LinkedList<Integer> delete_database_list = null;
//	LinkedList<Integer> source_type_index_list = null;
//	String time_month_begin = null;
//	String time_month_end = null;
//	String conf_filename = null;


	private static short parse_param(String args[])
	{
		int index = 0;
		int index_offset = 0;
		int args_len = args.length;

// Parse the argument
		while(index < args_len)
		{
			String option = args[index];
			if (option.equals("--market_mode"))
			{
				FinanceRecorderCmnDef.FINANCE_MODE = FinanceRecorderCmnDef.FinanceAnalysisMode.FinanceAnalysis_Market;
				index_offset = 1;
			}
			else if (option.equals("--stock_mode"))
			{
				FinanceRecorderCmnDef.FINANCE_MODE = FinanceRecorderCmnDef.FinanceAnalysisMode.FinanceAnalysis_Stock;
				index_offset = 1;
			}
			else if (option.equals("-h") || option.equals("--help"))
			{
				FinanceRecorderCmnDef.enable_show_console(true);
				help_param = true;
				index_offset = 1;
			}
			else if (option.equals("--silent"))
			{
				FinanceRecorderCmnDef.enable_show_console(false);
				index_offset = 1;
			}
			else if (option.equals("--finance_folderpath"))
			{
				if (index + 1 >= args_len)
					show_error_and_exit(String.format("The option[%s] does NOT contain value", option));
				finance_folderpath_param = args[index + 1];
				index_offset = 2;
			}
			else if (option.equals("--finance_backup_folderpath"))
			{
				if (index + 1 >= args_len)
					show_error_and_exit(String.format("The option[%s] does NOT contain value", option));
				finance_backup_folderpath_param = args[index + 1];
				index_offset = 2;
			}
			else if (option.equals("--delete_sql_accurancy"))
			{
				if (index + 1 >= args_len)
					show_error_and_exit(String.format("The option[%s] does NOT contain value", option));
				if (FinanceRecorderCmnDef.IS_FINANCE_MARKET_MODE)
					FinanceRecorderCmnDef.warn("The delete_sql_accurancy arguemnt is ignored in the Market mode");
				else
					delete_sql_accurancy_param = args[index + 1];
				index_offset = 2;
			}
			else if (option.equals("--source_from_file"))
			{
				if (index + 1 >= args_len)
					show_error_and_exit(String.format("The option[%s] does NOT contain value", option));
				source_from_file_param = args[index + 1];
				index_offset = 2;
			}
			else if (option.equals("-s") || option.equals("--source"))
			{
				if (index + 1 >= args_len)
					show_error_and_exit(String.format("The option[%s] does NOT contain value", option));
				source_param = args[index + 1];
				index_offset = 2;
			}
			else if (option.equals("-t") || option.equals("--time_range"))
			{
				if (index + 1 >= args_len)
					show_error_and_exit(String.format("The option[%s] does NOT contain value", option));
				time_range_param = args[index + 1];
				index_offset = 2;
			}
			else if (option.equals("--company_from_file"))
			{
				if (index + 1 >= args_len)
					show_error_and_exit(String.format("The option[%s] does NOT contain value", option));
				if (FinanceRecorderCmnDef.IS_FINANCE_MARKET_MODE)
					FinanceRecorderCmnDef.warn("The company_from_file arguemnt is ignored in the Market mode");
				else
					company_from_file_param = args[index + 1];
				index_offset = 2;
			}
			else if (option.equals("-c") || option.equals("--company"))
			{
				if (index + 1 >= args_len)
					show_error_and_exit(String.format("The option[%s] does NOT contain value", option));
				if (FinanceRecorderCmnDef.IS_FINANCE_MARKET_MODE)
					FinanceRecorderCmnDef.warn("The company_from_file arguemnt is ignored in the Market mode");
				else
					company_param = args[index + 1];
				index_offset = 2;
			}
//			else if (option.equals("-r") || option.equals("--delete"))
//			{
//				if (index + 1 >= args_len)
//					show_error_and_exit(String.format("The option[%s] does NOT contain value", option));
//				if (delete_database_list != null)
//					show_error_and_exit(String.format("The option[%s] is duplicate", option));
//
//				delete_database_list = new LinkedList<Integer>();
//				String[] data_source_array = args[index + 1].split(PARAM_SPLIT);
//				for (String data_source : data_source_array)
//					delete_database_list.addLast(Integer.valueOf(data_source));
//				index_offset = 2;
//			}
//			else if (option.equals("-f") || option.equals("--file"))
//			{
//				if (index + 1 >= args_len)
//					show_error_and_exit(String.format("The option[%s] does NOT contain value", option));
//
//				conf_filename = args[index + 1];
//				index_offset = 2;
//			}
//			else if (option.equals("--restore"))
//			{
//				if (index + 1 >= args_len)
//					show_error_and_exit(String.format("The option[%s] does NOT contain value", option));
//
//				delete_database_list = new LinkedList<Integer>();
//				int data_name_list_length = FinanceRecorderCmnDef.FINANCE_DATA_NAME_LIST.length;
//				for (int i = 0 ; i < data_name_list_length ; i++)
//					delete_database_list.addLast(i);
//
//				restore_database = true;
//				restore_foldername = args[index + 1];
//				index_offset = 2;
//			}
//			else if (option.equals("--restore_latest"))
//			{
//				delete_database_list = new LinkedList<Integer>();
//				int data_name_list_length = FinanceRecorderCmnDef.FINANCE_DATA_NAME_LIST.length;
//				for (int i = 0 ; i < data_name_list_length ; i++)
//					delete_database_list.addLast(i);
//
//				restore_database = true;
//				index_offset = 1;
//			}
//			else if (option.equals("--restore_path"))
//			{
//				if (index + 1 >= args_len)
//					show_error_and_exit(String.format("The option[%s] does NOT contain value", option));
//
//				restore_folderpath = args[index + 1];
//				index_offset = 2;
//			}
//			else if (option.equals("--delete_old"))
//			{
//				if (delete_database_list != null)
//					show_error_and_exit(String.format("The option[%s] is duplicate", option));
//
//				delete_database_list = new LinkedList<Integer>();
//				int data_name_list_length = FinanceRecorderCmnDef.FINANCE_DATA_NAME_LIST.length;
//				for (int i = 0 ; i < data_name_list_length ; i++)
//					delete_database_list.addLast(i);
//				index_offset = 1;
//			}
//			else if (option.equals("--backup_list"))
//			{
//				list_database_folder = true;
//				index_offset = 1;
//			}
//			else if (option.equals("--backup_cleanup"))
//			{
//				cleanup_database_folder = true;
//				index_offset = 1;
//			}
//			else if (option.equals("--copy_backup"))
//			{
//				copy_backup_folder = true;
//				index_offset = 1;
//			}
//			else if (option.equals("--multi_thread"))
//			{
//				use_multithread = true;
//				index_offset = 1;
//			}
//			else if (option.equals("--check_error"))
//			{
//				check_error = true;
//				index_offset = 1;
//			}
//			else if (option.equals("-a") || option.equals("--action"))
//			{
//				if (index + 1 >= args_len)
//					show_error_and_exit(String.format("The option[%s] does NOT contain value", option));
//				action_param = args[index + 1];
//				index_offset = 2;
//			}
			else if (option.equals("--show_console"))
			{
				FinanceRecorderCmnDef.enable_show_console(true);
				index_offset = 1;
			}
//			else if (option.equals("--run_daily"))
//			{
//				run_daily = true;
//				index_offset = 1;
//			}
			else
			{
				show_error_and_exit(String.format("Unknown argument: %s", option));
			}
			index += index_offset;
		}
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	private static short check_param()
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		if (source_from_file_param != null)
		{
			if (source_param != null)
			{
				source_param = null;
				FinanceRecorderCmnDef.warn("The 'source' argument is ignored since 'source_from_file' is set");
			}
			if (time_range_param != null)
			{
				time_range_param = null;
				FinanceRecorderCmnDef.warn("The 'time_range' argument is ignored since 'source_from_file' is set");
			}
		}
		if (FinanceRecorderCmnDef.IS_FINANCE_MARKET_MODE)
		{
			if (company_param != null)
			{
				company_param = null;
				FinanceRecorderCmnDef.warn("The 'company' argument is ignored since it's Market mode");
			}
			if (company_from_file_param != null)
			{
				company_from_file_param = null;
				FinanceRecorderCmnDef.warn("The 'company_from_file' argument is ignored since it's Market mode");
			}
			if (delete_sql_accurancy_param != null)
			{
				delete_sql_accurancy_param = null;
				FinanceRecorderCmnDef.warn("The 'delete_sql_accurancy' argument is ignored since it's Market mode");
			}
		}
		else
		{
			if (company_from_file_param != null)
			{
				if (company_param != null)
				{
					company_param = null;
					FinanceRecorderCmnDef.warn("The 'company' argument is ignored since 'company_from_file' is set");
				}
			}
		}
		if (database_operation_param != null)
		{
			if (database_operation_param.indexOf('W') != -1 || database_operation_param.indexOf('w') != -1)
				database_operation |= DATABASE_OPERATION_WRITE_MASK;
			if (database_operation_param.indexOf('B') != -1 || database_operation_param.indexOf('b') != -1)
				database_operation |= DATABASE_OPERATION_BACKUP_MASK;
			if (database_operation_param.indexOf('M') != -1 || database_operation_param.indexOf('m') != -1)
				database_operation |= DATABASE_OPERATION_DELETE_MASK;
			if (database_operation_param.indexOf('C') != -1 || database_operation_param.indexOf('c') != -1)
				database_operation |= DATABASE_OPERATION_CLEANUP_MASK;
			if (database_operation_param.indexOf('R') != -1 || database_operation_param.indexOf('r') != -1)
				database_operation |= DATABASE_OPERATION_RESTORE_MASK;
			if ((database_operation & DATABASE_OPERATION_WRITE_MASK) != 0 && (database_operation & DATABASE_OPERATION_RESTORE_MASK) != 0 )
			{
				FinanceRecorderCmnDef.warn("The 'write' and 'resotre' operation can NOT be enabled simultaneously, ignore the 'restore' operation");
				database_operation &= ~DATABASE_OPERATION_RESTORE_MASK;
			}
			if ((database_operation & DATABASE_OPERATION_DELETE_MASK) != 0 && (database_operation & DATABASE_OPERATION_CLEANUP_MASK) != 0 )
			{
				FinanceRecorderCmnDef.warn("The 'delete' operation is ignored since 'cleanup' is set");
				database_operation &= ~DATABASE_OPERATION_RESTORE_MASK;
			}
		}
		if (is_delete_operation_enabled())
		{
			try
			{
				delete_sql_accurancy_type = FinanceRecorderCmnDef.DeleteSQLAccurancyType.valueOf(Integer.valueOf(delete_sql_accurancy_param));
			}
			catch (Exception e){}
			if (delete_sql_accurancy_type == null)
				throw new IllegalStateException(String.format("Unknown delete sql accurancy type: %s", delete_sql_accurancy_param));
		}
		else
		{
			if (delete_sql_accurancy_param != null)
			{
				delete_sql_accurancy_param = null;
				FinanceRecorderCmnDef.warn("The 'delete_sql_accurancy' argument is ignored since delete action is NOT set");
			}
		}

		return ret;
	}

	private static short setup_param()
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		String errmsg = null;
// Setup the finance root folder path
		if (finance_folderpath_param != null)
			finance_recorder_mgr.set_finance_folderpath(finance_folderpath_param);
// Setup the finance backup root folder path
		if (finance_backup_folderpath_param != null)
			finance_recorder_mgr.set_finance_backup_folderpath(finance_backup_folderpath_param);
		if (delete_sql_accurancy_param != null)
			finance_recorder_mgr.set_delete_sql_accuracy(delete_sql_accurancy_type);
// Set source type
		if (source_from_file_param != null)
			ret = finance_recorder_mgr.set_source_type_from_file(source_from_file_param);
		else
		{
// Parse the source type
			LinkedList<Integer> source_type_index_list = null;
			if (source_param != null)
			{
				String[] source_type_index_str_array = source_param.split(PARAM_SPLIT);
				source_type_index_list = new LinkedList<Integer>();
				for (String source_type_index_str : source_type_index_str_array)
				{
					int source_type_index = Integer.valueOf(source_type_index_str);
					if (!FinanceRecorderCmnDef.check_source_type_index_in_range(source_type_index))
					{
						errmsg = String.format("Unsupported source type index: %d", source_type_index);
						show_error_and_exit(errmsg);
					}
					source_type_index_list.add(source_type_index);
				}
			}
			else
				source_type_index_list = FinanceRecorderCmnDef.get_all_source_type_index_list();
			ret = finance_recorder_mgr.set_source_type(source_type_index_list);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				show_error_and_exit(String.format("Fail to set source type, due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret)));
// Parse the time range
			if (time_range_param != null)
			{
				String[] time_range_range_array = time_range_param.split(PARAM_SPLIT);
				String time_range_start = null;
				String time_range_end = null;
				if (time_range_range_array.length == 2)
				{
					time_range_start = time_range_range_array[0];
					time_range_end = time_range_range_array[1];
				}
				else if (time_range_range_array.length == 1)
				{
					if (time_range_param.startsWith(PARAM_SPLIT))
						time_range_end = time_range_range_array[0];
					else
						time_range_start = time_range_range_array[0];
				}
				else
					show_error_and_exit(String.format("Incorrect time range parameter format: %s", time_range_param));
				finance_time_range = new FinanceRecorderCmnClass.FinanceTimeRange(time_range_start, time_range_end);
			}
		}
// Set company list. For stock mode only
		if (FinanceRecorderCmnDef.IS_FINANCE_STOCK_MODE)
		{
			if (company_from_file_param != null)
			{
				ret = finance_recorder_mgr.set_company_from_file(company_from_file_param);
				if (FinanceRecorderCmnDef.CheckFailure(ret))
					show_error_and_exit(String.format("Fail to set company from file, due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret)));
			}
			else if (company_param != null)
			{
				LinkedList<String> company_word_list = new LinkedList<String>();
				String[] company_word_array = company_param.split(PARAM_SPLIT);
				for (String company_word : company_word_array)
					company_word_list.add(company_word);
				ret = finance_recorder_mgr.set_company(company_word_list);
				if (FinanceRecorderCmnDef.CheckFailure(ret))
					show_error_and_exit(String.format("Fail to set company, due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret)));
			}
		}

// Initialize the manager class
		ret = finance_recorder_mgr.initialize();
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			show_error_and_exit(String.format("Fail to initialize the Manager class, due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret)));
//
///////////////////////////////////////////////////////////////////
//// CAUTION: The process stops after restore !!!
///////////////////////////////////////////////////////////////////
//		if (restore_database)
//		{
//// Cleanup the old database
//			delete_database_list = new LinkedList<Integer>();
//			int data_name_list_length = FinanceRecorderCmnDef.FINANCE_DATA_NAME_LIST.length;
//			for (int i = 0 ; i < data_name_list_length ; i++)
//				delete_database_list.addLast(i);
//			delete_sql(delete_database_list);
//// Restore the database from the backup file
//			if (restore_folderpath == null)
//				restore_folderpath = FinanceRecorderCmnDef.get_current_path();
////			restore_sql(String.format("%s/%s/%s", restore_folderpath, FinanceRecorderCmnDef.BACKUP_FOLDERNAME, restore_foldername));
//			restore_sql(restore_folderpath, restore_foldername);
//// Check the database and find the time range of each database. Only needed when the content of the MySQL is modified
//			check_sql(check_error);
//
//			wait_to_exit();
//		}

//// Setup time range if necessary
//		if (source_type_index_list != null)
//		{
//			if (time_month_begin == null)
//				time_month_begin = FinanceRecorderCmnDef.get_time_month_today();
//			if (time_month_end == null)
//				time_month_end = FinanceRecorderCmnDef.get_time_month_today();
//		}
//// Setup the config for writing data into MySQL
//		if (conf_filename != null)
//		{
//// If the config file is set, parse it...
//			if(FinanceRecorderCmnDef.is_show_console())
//			{
//				if (source_type_index_list != null || time_month_begin != null || time_month_end != null)
//					System.out.println("Ingnore the Source/Time parameters");
//				System.out.printf("Setup config from file[%s]\n", conf_filename);
//			}
//			setup_time_range_table(conf_filename);
//		}
//		else
//		{
//// If no config file is selected, check if the data source selection are set from the command line
//			if (source_type_index_list == null)
//			{
//				String msg = "No data sources are selected......";
//				FinanceRecorderCmnDef.debug(msg);
////				if (FinanceRecorderCmnDef.is_show_console())
////					System.out.println(msg);
//				action_type = ActionType.Action_None;
//			}
//			else
//				setup_time_range_table(source_type_index_list, time_month_begin, time_month_end);
//		}
//
//// Should be the first action since the database time range could probably be modified
//		if (need_write(action_type))
//		{
//// Write the financial data into MySQL
//			write_sql(use_multithread);
//// Check the database and find the time range of each database. Only needed when the content of the MySQL is modified
//			check_sql(check_error);
//		}
//// Initialize the database time range table
//		init_database_time_range_table();
//
//		if (run_daily)
//		{
//// Update the latest information to user
//			run_daily();
//		}
//// Backup the database
//		if (cleanup_database_folder)
//		{
//			backup_sql_cleanup();
//		}
//		if (backup_database)
//		{
//			backup_sql(copy_backup_folder);
//		}
//		if (list_database_folder)
//		{
//			backup_sql_list();
//		}
		return ret;
	}

	private static void wait_to_exit()
	{
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
	private static void show_error_and_exit(short ret)
	{
		String err_msg = String.format("Error occur due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret));
		FinanceRecorderCmnDef.error(err_msg);
		if (FinanceRecorderCmnDef.is_show_console())
			System.err.println(err_msg);
		FinanceRecorderCmnDef.wait_for_logging();
		System.exit(1);
	}

	private static void show_usage_and_exit()
	{
		if (!FinanceRecorderCmnDef.is_show_console())
		{
			FinanceRecorderCmnDef.warn("STDOUT/STDERR are Disabled");
			return;
		}
		System.out.println("====================== Usage ======================");
		System.out.printf("--market_mode --stock_mode\nDescription: Switch the market/stock mode\nCaution: Read parameters from %s when NOT set", FinanceRecorderCmnDef.MARKET_STOCK_SWITCH_CONF_FILENAME);
		System.out.println("-h|--help\nDescription: The usage");
		System.out.printf("--finance_folderpath\nThe finance folder path\nDefault: %s", FinanceRecorderCmnDef.CSV_ROOT_FOLDERPATH);
		System.out.printf("--finance_backup_folderpath\nThe finance backup folder path\nDefault: %s", FinanceRecorderCmnDef.BACKUP_CSV_ROOT_FOLDERPATH);
		if (FinanceRecorderCmnDef.IS_FINANCE_STOCK_MODE)
		{
			System.out.println("--delete_sql_accurancy\nDescription: The accurancy of delete SQL\nDefault: Source type only\nCaution: Only useful for Delete action");
			System.out.println("  Format 1 Source Type Only: 0");
			System.out.println("  Format 2 Company Only: 1");
			System.out.println("  Format 3 Source Type and Company: 2");
		}
//		System.out.println("-r|--delete\nDescription: delete some MySQL database(s)\nCaution: Ignored if --backup set");
//		System.out.println("  Format: 1,2,3 (Start from 0)");
		System.out.printf(String.format("--source_from_all_default_file\nThe all finance data source in full time range from file: %s\nCaution: source is ignored when set", (FinanceRecorderCmnDef.IS_FINANCE_MARKET_MODE ? FinanceRecorderCmnDef.MARKET_ALL_CONFIG_FILENAME : FinanceRecorderCmnDef.STOCK_ALL_CONFIG_FILENAME)));
		System.out.printf(String.format("--source_from_file\nThe finance data source from file\nCaution: source is ignored when set", (FinanceRecorderCmnDef.IS_FINANCE_MARKET_MODE ? FinanceRecorderCmnDef.MARKET_ALL_CONFIG_FILENAME : FinanceRecorderCmnDef.STOCK_ALL_CONFIG_FILENAME)));
		System.out.println("-s|--source\nDescription: Type of CSV date file\nCaution: Ignored when --source_from_file/--source_from_all_default_file set");
		System.out.println("  Format: 1,2,3 (Start from 0)");
		System.out.println("  all: All types");
//		int[] source_type_index_array = FinanceRecorderCmnDef.get_source_type_index_range();
//		for (int index = source_type_index_array[0] ; index < source_type_index_array[1] ; index++)
		LinkedList<Integer> whole_source_type_index_list = FinanceRecorderCmnDef.get_all_source_type_index_list();
		for (Integer index : whole_source_type_index_list)
			System.out.printf("  %d: %s\n", index, FinanceRecorderCmnDef.FINANCE_DATA_DESCRIPTION_LIST[index]);
		System.out.println("-t|--time_range\nDescription: The time range of the SQL\nDefault: full range in SQL\nCaution: Only take effect for Database Operation: B(backup)");
		System.out.println("  Format 1 (start_time): 2015-09");
		System.out.println("  Format 1 (,end_time): ,2015-09");
		System.out.println("  Format 2 (start_time,end_time): 2015-01,2015-09");
		if (FinanceRecorderCmnDef.IS_FINANCE_STOCK_MODE)
		{
			System.out.println("--company_from_file\nDescription: The company code number from file\nDefault: All company code nubmers\nCaution: company is ignored when set");
			System.out.println("-c|--company\nDescription: The list of the company code number\nDefault: All company code nubmers\nCaution: Only work when company_from_file is NOT set");
			System.out.println("  Format 1 Company code number: 2347");
			System.out.println("  Format 2 Company code number range: 2100-2200");
			System.out.println("  Format 3 Company group number: [Gg]12");
			System.out.println("  Format 4 Company code number/number range/group hybrid: 2347,2100-2200,G12,2362,g2,1500-1510");
		}
		System.out.println("-o|--database_operation\nDescription: Operate the MySQL");
		System.out.println("  Type: {W(w), B(b), M(m), C(c), R(r)");
		System.out.println("  W(w): Write into SQL from CSV files");
		System.out.println("  B(b): Backup SQL to CSV files");
		System.out.println("  M(m): delete existing SQL");
		System.out.println("  C(c): Clean-up all existing SQL");
		System.out.println("  R(r): Restore SQL from CSV Tar file");
//		System.out.println("--restore\nDescription: Restore the MySQL databases from certain a backup folder\nDefault: $CurrentWorkingFolder/.backup\nCaution: delete old MySQL before backup. The proccess stops after restore");
//		System.out.println("  Format: 160313060053");
//		System.out.println("--restore_path\nDescription: The path where the backup folder is located in\nCaution: Enabled if --restore set");
//		System.out.println("  Format: /home/super/Projects/finance_recorder_java/.backup");
//		System.out.println("--delete_old\nDescription: delete the old MySQL databases\nCaution: Ignored if --restore set");
//		System.out.println("--backup\nDescription: Backup the current databases");
//		System.out.println("--backup_list\nDescription: List database backup folder");
//		System.out.println("--backup_cleanup\nDescription: CleanUp all database backup sub-folders");
//		System.out.println("--copy_backup\nDescription: Copy the backup folder to the designated path\nCaution: Enabled if --backup set");
//		System.out.println("--multi_thread\nDescription: Write into MySQL database by using multiple threads");
//		System.out.println("--check_error\nDescription: Check if the data in the MySQL database is correct");
//		System.out.println("--run_daily\nDescription: Run daily data\nCaution: Executed after writing MySQL data if set");
		System.out.println("===================================================");
		System.exit(0);
	}

//	private static boolean need_read(ActionType type){return (type == ActionType.Action_Read || type == ActionType.Action_ReadWrite) ? true : false;}
//	private static boolean need_write(ActionType type){return (type == ActionType.Action_Write || type == ActionType.Action_ReadWrite) ? true : false;}

//	private static short init_workday_calendar_table()
//	{
//		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
//		ret = finance_recorder_mgr.init_workday_calendar_table();
//		if (FinanceRecorderCmnDef.CheckFailure(ret))
//			show_error_and_exit(String.format("Fail to initialize the workday calendar table, due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret)));
//
//		return ret;
//	}
//
//	private static short init_database_time_range_table()
//	{
//		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
//		ret = finance_recorder_mgr.init_database_time_range_table();
//		if (FinanceRecorderCmnDef.CheckFailure(ret))
//			show_error_and_exit(String.format("Fail to initialize the database time range table, due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret)));
//
//		return ret;
//	}
//
//	private static short setup_time_range_table(String filename)
//	{clear_multi
//		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
//		ret = finance_recorder_mgr.setup_time_range_table_by_config_file(filename);
//		if (FinanceRecorderCmnDef.CheckFailure(ret))
//			show_error_and_exit(String.format("Fail to setup the parameters, due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret)));
//
//		return ret;
//	}
//
//	private static short setup_time_range_table(LinkedList<Integer> source_type_index_list, String time_month_begin, String time_month_end)
//	{
//		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
//		ret = finance_recorder_mgr.setup_time_range_table_by_parameter(source_type_index_list, time_month_begin, time_month_end);
//		if (FinanceRecorderCmnDef.CheckFailure(ret))
//			show_error_and_exit(String.format("Fail to setup the parameters, due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret)));
//
//		return ret;
//	}
//
//	private static short setup_backup_time_range_table(String filename)
//	{
//		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
//		ret = finance_recorder_mgr.setup_backup_time_range_table_by_config_file(filename);
//		if (FinanceRecorderCmnDef.CheckFailure(ret))
//			show_error_and_exit(String.format("Fail to setup the parameters, due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret)));
//
//		return ret;
//	}
//
//	private static short setup_backup_time_range_table(LinkedList<Integer> source_type_index_list, String time_month_begin, String time_month_end)
//	{
//		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
//		ret = finance_recorder_mgr.setup_backup_time_range_table_by_parameter(source_type_index_list, time_month_begin, time_month_end);
//		if (FinanceRecorderCmnDef.CheckFailure(ret))
//			show_error_and_exit(String.format("Fail to setup the parameters, due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret)));
//
//		return ret;
//	}
//
	private static void write_operation()
	{
		if(FinanceRecorderCmnDef.is_show_console())
			System.out.println("Write CSV data into MySQL......");

		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		ret = finance_recorder_mgr.transfrom_csv_to_sql();
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			show_error_and_exit(String.format("Fail to write CSV data into MySQL, due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret)));
	}

	private static void backup_operation()
	{
		if(FinanceRecorderCmnDef.is_show_console())
			System.out.println("Backup MySQL to CSV TAR......");

		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		ret = finance_recorder_mgr.transfrom_sql_to_csv(finance_time_range);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			show_error_and_exit(String.format("Fail to backup MySQL to CSV TAR, due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret)));
	}

	private static void cleanup_operation()
	{
		if(FinanceRecorderCmnDef.is_show_console())
			System.out.println("Cleanup old MySQL data......");

		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		ret = finance_recorder_mgr.cleanup_sql();
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			show_error_and_exit(String.format("Fail to cleanup the old MySQL, due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret)));
	}

	private static void delete_operation()
	{
		if(FinanceRecorderCmnDef.is_show_console())
			System.out.println("Delete old MySQL data......");

		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		ret = finance_recorder_mgr.delete_sql();
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			show_error_and_exit(String.format("Fail to delete the old MySQL, due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret)));
	}

	private static void restore_operation()
	{
		if(FinanceRecorderCmnDef.is_show_console())
			System.out.println("Restore MySQL data from CSV Tar......");

		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		ret = finance_recorder_mgr.transfrom_csv_to_sql();
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			show_error_and_exit(String.format("Fail to restore MySQL data from CSV Tar, due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret)));
	}

//	private static short backup_sql(boolean copy_backup_folder)
//	{
//		if(FinanceRecorderCmnDef.is_show_console())
//			System.out.println("Backup current MySQL data......");
//
//		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
//		ret = finance_recorder_mgr.backup_by_multithread(copy_backup_folder);
//		if (FinanceRecorderCmnDef.CheckFailure(ret))
//			show_error_and_exit(String.format("Fail to backup the MySQL, due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret)));
//
//		return ret;
//	}
//
//	private static short backup_sql_list()
//	{
//		if(!FinanceRecorderCmnDef.is_show_console())
//		{
//			FinanceRecorderCmnDef.warn("The STDOUT/STDERR is Disabled");
//			return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
//		}
//// Find the backup folder list
//		List<String> sorted_backup_list = new LinkedList<String>();
//		short ret = finance_recorder_mgr.get_sorted_backup_list(sorted_backup_list);
//		if (FinanceRecorderCmnDef.CheckFailure(ret))
//			return ret;
//// Print the result
//		if (sorted_backup_list.isEmpty())
//		{
//			System.out.println("No backup folders Found");
//		}
//		else
//		{
//			System.out.println("The backup folder list:");
//			for (String backup_foldername : sorted_backup_list)
//				System.out.println(backup_foldername);
//		}
//		return FinanceRecorderCmnDef.RET_SUCCESS;
//	}
//
//	private static short backup_sql_cleanup()
//	{
//		String filepath = String.format("%s/%s", FinanceRecorderCmnDef.get_current_path(), FinanceRecorderCmnDef.BACKUP_FOLDERNAME);
//		List<String> subfolder_list = new ArrayList<String>();
//		short ret = FinanceRecorderCmnDef.get_subfolder_list(filepath, subfolder_list);
//		if (FinanceRecorderCmnDef.CheckFailure(ret))
//			show_error_and_exit(String.format("Fail to show backup list of the MySQL, due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret)));
//		for (String subfolder : subfolder_list)
//		{
//			ret = FinanceRecorderCmnDef.delete_subfolder(String.format("%s/%s", filepath, subfolder));
//			if (FinanceRecorderCmnDef.CheckFailure(ret))
//				show_error_and_exit(String.format("Fail to delete backup folder: %s, due to: %s", subfolder, FinanceRecorderCmnDef.GetErrorDescription(ret)));
//		}
//		return FinanceRecorderCmnDef.RET_SUCCESS;
//	}
//
//	private static short write_sql(boolean use_multithread)
//	{
//// Write the financial data into MySQL
//		if(FinanceRecorderCmnDef.is_show_console())
//			System.out.println("Write financial data into MySQL......");
//
//		long time_start_millisecond = System.currentTimeMillis();
//		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
//		if (use_multithread)
//			ret = finance_recorder_mgr.write_by_multithread();
//		else
//			ret = finance_recorder_mgr.write();
//		if (FinanceRecorderCmnDef.CheckFailure(ret))
//			show_error_and_exit(String.format("Fail to write financial data into MySQL, due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret)));
//		long time_end_millisecond = System.currentTimeMillis();
//
//		if(FinanceRecorderCmnDef.is_show_console())
//			System.out.println("Write financial data into MySQL...... Done");
//
//		long time_lapse_millisecond = time_end_millisecond - time_start_millisecond;
//		String time_lapse_msg; 
//		if (time_lapse_millisecond >= 100 * 1000)
//			time_lapse_msg = String.format("######### Time Lapse: %d second(s) #########", (int)((time_end_millisecond - time_start_millisecond) / 1000));
//		else if (time_lapse_millisecond >= 10 * 1000)
//			time_lapse_msg = String.format("######### Time Lapse: %d second(s) #########", (int)((time_end_millisecond - time_start_millisecond) / 1000));
//		else
//			time_lapse_msg = String.format("######### Time Lapse: %d second(s) #########", (int)((time_end_millisecond - time_start_millisecond) / 1000));
//		FinanceRecorderCmnDef.info(time_lapse_msg);
//		if(FinanceRecorderCmnDef.is_show_console())
//			System.out.println(time_lapse_msg);
//
//		return ret;
//	}
//
//	private static short restore_sql(String restore_folderpath, String restore_foldername)
//	{
//// Write the financial data into MySQL
//		if(FinanceRecorderCmnDef.is_show_console())
//			System.out.println("Restore MySQL from financial data......");
//
//		long time_start_millisecond = System.currentTimeMillis();
//		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
//		if (restore_foldername == null)
//			ret = finance_recorder_mgr.restore_latest(restore_folderpath);
//		else
//			ret = finance_recorder_mgr.restore(restore_folderpath, restore_foldername);
//		if (FinanceRecorderCmnDef.CheckFailure(ret))
//			show_error_and_exit(String.format("Fail to backup MySQL from financial data, due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret)));
//		long time_end_millisecond = System.currentTimeMillis();
//
//		if(FinanceRecorderCmnDef.is_show_console())
//			System.out.println("Restore MySQL from financial data...... Done");
//
//		long time_lapse_millisecond = time_end_millisecond - time_start_millisecond;
//		String time_lapse_msg; 
//		if (time_lapse_millisecond >= 100 * 1000)
//			time_lapse_msg = String.format("######### Time Lapse: %d second(s) #########", (int)((time_end_millisecond - time_start_millisecond) / 1000));
//		else if (time_lapse_millisecond >= 10 * 1000)
//			time_lapse_msg = String.format("######### Time Lapse: %d second(s) #########", (int)((time_end_millisecond - time_start_millisecond) / 1000));
//		else
//			time_lapse_msg = String.format("######### Time Lapse: %d second(s) #########", (int)((time_end_millisecond - time_start_millisecond) / 1000));
//		FinanceRecorderCmnDef.info(time_lapse_msg);
//		if(FinanceRecorderCmnDef.is_show_console())
//			System.out.println(time_lapse_msg);
//
//		return ret;
//	}
//
//	private static short run_daily()
//	{
//		if(FinanceRecorderCmnDef.is_show_console())
//			System.out.println("Run daily data......");
//		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
//		ret = finance_recorder_mgr.run_daily();
//		if (FinanceRecorderCmnDef.CheckFailure(ret))
//			show_error_and_exit(String.format("Fail to run daily data, due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret)));
//
//		return ret;
//	}
//
////	private static short read_sql()
////	{
////		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
////		FinanceRecorderCmnClass.ResultSet result_set = new FinanceRecorderCmnClass.ResultSet();
////		ret = finance_recorder_mgr.read(result_set);
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
//////		List<List<String>> total_data_list = new ArrayList<List<String>>();
//////		ret = finance_recorder_mgr.read(total_data_list);
//////// Show the result statistics
//////		int index = 0;
//////		String msg;
//////		for (List<String> data_list : total_data_list)
//////		{
//////			msg = String.format("%d: %d", index, data_list.size());
//////			FinanceRecorderCmnDef.info(msg);
//////			if (FinanceRecorderCmnDef.is_show_console())
//////				System.out.println(msg);
//////			index++;
//////		}
////
////		return ret;
////	}
//
//	private static short check_sql(boolean check_error)
//	{
//		if (FinanceRecorderCmnDef.is_show_console() && check_error)
//			System.out.println("Let's check error......");
//		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
//		ret = finance_recorder_mgr.check(check_error);
//		if (FinanceRecorderCmnDef.CheckFailure(ret))
//			show_error_and_exit(String.format("Fail to check data in MySQL, due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret)));
//
//		return ret;
//	}
	public static void main(String args[])
	{
//		FinanceRecorderCmnClassCompanyProfile lookup = FinanceRecorderCmnClassCompanyProfile.get_instance();
//		for (ArrayList<String> data : lookup.entry())
//		{
//			System.out.printf("%s\n", data.get(FinanceRecorderCompanyProfileLookup.COMPANY_PROFILE_ENTRY_FIELD_INDEX_COMPANY_CODE_NUMBER));
//		}
//		for (int i = 0 ; i < lookup.get_company_group_size() ; i++)
//		{
//			System.out.printf("======================== Group: %d, %s ========================\n", i, lookup.get_company_group_description(i));
//			for (ArrayList<String> data : lookup.group_entry(i))
//			{
//				System.out.printf("%s\n", data.get(FinanceRecorderCompanyProfileLookup.COMPANY_PROFILE_ENTRY_FIELD_INDEX_COMPANY_CODE_NUMBER));
//			}
//		}
//		System.exit(0);

//		if (FinanceRecorderCmnDef.CheckFailure(init_param()))
//			show_error_and_exit("Fail to initialize the parameters ......");
// Transfer the command line to config and setup the parameters of the manager class
		if (FinanceRecorderCmnDef.CheckFailure(parse_param(args)))
			show_error_and_exit("Fail to parse the parameters ......");

// Determine the mode and initializ//		FinanceRecorderCmnClassCompanyProfile lookup = FinanceRecorderCmnClassCompanyProfile.get_instance();
//		for (ArrayList<String> data : lookup.entry())
//		{
//			System.out.printf("%s\n", data.get(FinanceRecorderCompanyProfileLookup.COMPANY_PROFILE_ENTRY_FIELD_INDEX_COMPANY_CODE_NUMBER));
//		}
//		for (int i = 0 ; i < lookup.get_company_group_size() ; i++)
//		{
//			System.out.printf("======================== Group: %d, %s ========================\n", i, lookup.get_company_group_description(i));
//			for (ArrayList<String> data : lookup.group_entry(i))
//			{
//				System.out.printf("%s\n", data.get(FinanceRecorderCompanyProfileLookup.COMPANY_PROFILE_ENTRY_FIELD_INDEX_COMPANY_CODE_NUMBER));
//			}
//		}

// Determine the finance mode
		if (FinanceRecorderCmnDef.FINANCE_MODE == null)
		{
			FinanceRecorderCmnDef.FINANCE_MODE = FinanceRecorderCmnDef.get_finance_analysis_mode();
			FinanceRecorderCmnDef.IS_FINANCE_MARKET_MODE = (FinanceRecorderCmnDef.FINANCE_MODE == FinanceRecorderCmnDef.FinanceAnalysisMode.FinanceAnalysis_Market);
			FinanceRecorderCmnDef.IS_FINANCE_STOCK_MODE = (FinanceRecorderCmnDef.FINANCE_MODE == FinanceRecorderCmnDef.FinanceAnalysisMode.FinanceAnalysis_Stock);
		}
		else if (FinanceRecorderCmnDef.FINANCE_MODE == FinanceRecorderCmnDef.FinanceAnalysisMode.FinanceAnalysis_Market)
		{
			FinanceRecorderCmnDef.IS_FINANCE_MARKET_MODE = true;
			FinanceRecorderCmnDef.IS_FINANCE_STOCK_MODE = false;
		}
		else if (FinanceRecorderCmnDef.FINANCE_MODE == FinanceRecorderCmnDef.FinanceAnalysisMode.FinanceAnalysis_Stock)
		{
			FinanceRecorderCmnDef.IS_FINANCE_MARKET_MODE = false;
			FinanceRecorderCmnDef.IS_FINANCE_STOCK_MODE = true;
		}
		else
			throw new RuntimeException("Unknown mode !!!");

// Create the instance of manager class
		if (FinanceRecorderCmnDef.IS_FINANCE_MARKET_MODE)
			finance_recorder_mgr = new FinanceRecorderMarketMgr();
		else if (FinanceRecorderCmnDef.IS_FINANCE_STOCK_MODE)
			finance_recorder_mgr = new FinanceRecorderStockMgr();
		else
			throw new IllegalStateException("Unknown finance mode");

		if (help_param)
			show_usage_and_exit();

		if (FinanceRecorderCmnDef.CheckFailure(check_param()))
			show_error_and_exit("Fail to check the parameters ......");
		if (FinanceRecorderCmnDef.CheckFailure(setup_param()))
			show_error_and_exit("Fail to setup the parameters ......");

		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
// cleanup/delete the old database
		if (is_cleanup_operation_enabled())
			cleanup_operation();
		else if (is_delete_operation_enabled())
			delete_operation();
		
// After Initialization is done, start to work.......
		if (is_write_operation_enabled())
			write_operation();
		else if (is_restore_operation_enabled())
			restore_operation();
		if (is_backup_operation_enabled())
			backup_operation();

		wait_to_exit();
	}

}
