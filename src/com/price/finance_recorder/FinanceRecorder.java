package com.price.finance_recorder;

//import java.io.*;
//import java.util.*;
//import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
	private static final byte DATABASE_OPERATION_CHECK_EXIST_MASK = 0x1 << 5;

	private static boolean help_param = false;
	private static String finance_folderpath_param = null;
	private static String finance_backup_folderpath_param = null;
	private static String finance_restore_folderpath_param = null;
	private static String finance_backup_foldername_param = null;
	private static String finance_restore_foldername_param = null;
	private static boolean show_finance_backup_foldername_param = false;
	private static boolean show_finance_restore_foldername_param = false;
	private static String delete_sql_accurancy_param = null;
	private static String multi_thread_param = null;
	private static String database_operation_param = null;
	private static boolean continue_when_csv_not_foud_param = false;
	private static boolean continue_when_sql_not_foud_param = false;
	private static String source_from_file_param = null;
	private static String source_param = null;
	private static String time_range_param = null;
	private static String company_from_file_param = null;
	private static String company_param = null;
//	private static boolean compress_file_param = false;

	private static FinanceRecorderMgrInf finance_recorder_mgr = null;
	private static byte database_operation = 0x0;
	private static FinanceRecorderCmnClass.FinanceTimeRange finance_time_range = null;
	private static FinanceRecorderCmnDef.DeleteSQLAccurancyType delete_sql_accurancy_type = null;

	private static boolean is_write_operation_enabled(){return (database_operation & DATABASE_OPERATION_WRITE_MASK) != 0;}
	private static boolean is_backup_operation_enabled(){return (database_operation & DATABASE_OPERATION_BACKUP_MASK) != 0;}
	private static boolean is_delete_operation_enabled(){return (database_operation & DATABASE_OPERATION_DELETE_MASK) != 0;}
	private static boolean is_cleanup_operation_enabled(){return (database_operation & DATABASE_OPERATION_CLEANUP_MASK) != 0;}
	private static boolean is_restore_operation_enabled(){return (database_operation & DATABASE_OPERATION_RESTORE_MASK) != 0;}
	private static boolean is_check_exist_operation_enabled(){return (database_operation & DATABASE_OPERATION_CHECK_EXIST_MASK) != 0;}
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
			else if (option.equals("--finance_restore_folderpath"))
			{
				if (index + 1 >= args_len)
					show_error_and_exit(String.format("The option[%s] does NOT contain value", option));
				finance_restore_folderpath_param = args[index + 1];
				index_offset = 2;
			}
			else if (option.equals("--finance_backup_foldername"))
			{
				if (index + 1 >= args_len)
					show_error_and_exit(String.format("The option[%s] does NOT contain value", option));
				finance_backup_foldername_param = args[index + 1];
				index_offset = 2;
			}
			else if (option.equals("--finance_restore_foldername"))
			{
				if (index + 1 >= args_len)
					show_error_and_exit(String.format("The option[%s] does NOT contain value", option));
				finance_restore_foldername_param = args[index + 1];
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
			else if (option.equals("--multi_thread"))
			{
				if (index + 1 >= args_len)
					show_error_and_exit(String.format("The option[%s] does NOT contain value", option));
				multi_thread_param = args[index + 1];
				index_offset = 2;
			}
			else if (option.equals("--show_finance_backup_foldername"))
			{
				show_finance_backup_foldername_param = true;
				index_offset = 1;
			}
			else if (option.equals("--show_finance_restore_foldername"))
			{
				show_finance_restore_foldername_param = true;
				index_offset = 1;
			}
			else if (option.equals("-o") || option.equals("--operation"))
			{
				if (index + 1 >= args_len)
					show_error_and_exit(String.format("The option[%s] does NOT contain value", option));
				database_operation_param = args[index + 1];
				index_offset = 2;
			}
			else if (option.equals("--continue_when_csv_not_foud"))
			{
				continue_when_csv_not_foud_param = true;
				index_offset = 1;
			}
			else if (option.equals("--continue_when_sql_not_foud"))
			{
				continue_when_sql_not_foud_param = true;
				index_offset = 1;
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
//			else if (option.equals("--compress_file"))
//			{
//				compress_file_param = true;
//				index_offset = 1;
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
//			else if (option.equals("--check_error"))
//			{
//				check_error = true;
//				index_offset = 1;
//			}
//			else if (option.equals("--show_console"))
//			{
//				FinanceRecorderCmnDef.enable_show_console(true);
//				index_offset = 1;
//			}
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
			if (database_operation_param.indexOf('E') != -1 || database_operation_param.indexOf('e') != -1)
				database_operation |= DATABASE_OPERATION_CHECK_EXIST_MASK;
			if ((database_operation & DATABASE_OPERATION_WRITE_MASK) != 0 && (database_operation & DATABASE_OPERATION_RESTORE_MASK) != 0)
			{
				FinanceRecorderCmnDef.warn("The 'write' and 'resotre' operation can NOT be enabled simultaneously, ignore the 'restore' operation");
				database_operation &= ~DATABASE_OPERATION_RESTORE_MASK;
			}
			if ((database_operation & DATABASE_OPERATION_RESTORE_MASK) != 0 && (database_operation & DATABASE_OPERATION_CLEANUP_MASK) == 0)
			{
				FinanceRecorderCmnDef.warn("The 'cleanup' operation should be enabled, when 'restore' is set");
				database_operation &= ~DATABASE_OPERATION_RESTORE_MASK;
			}
			if ((database_operation & DATABASE_OPERATION_DELETE_MASK) != 0 && (database_operation & DATABASE_OPERATION_CLEANUP_MASK) != 0)
			{
				FinanceRecorderCmnDef.warn("The 'delete' operation is ignored since 'cleanup' is set");
				database_operation &= ~DATABASE_OPERATION_RESTORE_MASK;
			}
		}

		if (finance_backup_folderpath_param != null)
		{
			if (!is_backup_operation_enabled())
			{
				finance_backup_folderpath_param = null;
				FinanceRecorderCmnDef.warn("The 'finance_backup_folderpath_param' argument is ignored since Backup action is NOT set");
			}
		}
		if (finance_restore_folderpath_param != null)
		{
			if (!is_restore_operation_enabled())
			{
				finance_restore_folderpath_param = null;
				FinanceRecorderCmnDef.warn("The 'finance_restore_folderpath_param' argument is ignored since Restore action is NOT set");
			}
		}
		if (finance_backup_foldername_param != null)
		{
			if (!is_backup_operation_enabled())
			{
				finance_backup_foldername_param = null;
				FinanceRecorderCmnDef.warn("The 'finance_backup_foldername_param' argument is ignored since Backup action is NOT set");
			}
		}
		if (finance_restore_foldername_param != null)
		{
			if (!is_restore_operation_enabled())
			{
				finance_restore_foldername_param = null;
				FinanceRecorderCmnDef.warn("The 'finance_restore_foldername_param' argument is ignored since Restore action is NOT set");
			}
		}
		if (continue_when_csv_not_foud_param)
		{
			if (!is_write_operation_enabled() && !is_restore_operation_enabled())
			{
				continue_when_csv_not_foud_param = false;
				FinanceRecorderCmnDef.warn("The 'continue_when_csv_not_foud_param' argument is ignored since Write/Restore action is NOT set");
			}
		}
		if (continue_when_sql_not_foud_param)
		{
			if (!is_backup_operation_enabled())
			{
				continue_when_sql_not_foud_param = false;
				FinanceRecorderCmnDef.warn("The 'continue_when_sql_not_foud_param' argument is ignored since Backup action is NOT set");
			}
		}
		if (delete_sql_accurancy_param != null)
		{
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
				delete_sql_accurancy_param = null;
				FinanceRecorderCmnDef.warn("The 'delete_sql_accurancy' argument is ignored since Delete action is NOT set");
			}
		}

		if (multi_thread_param != null)
		{
			if (!is_write_operation_enabled())
			{
				multi_thread_param = null;
				FinanceRecorderCmnDef.warn("The 'multi_thread_param' argument is ignored since Write action is NOT set");
			}
		}
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
		if (is_backup_operation_enabled())
		{
			if (finance_backup_folderpath_param == null)
			{
				finance_backup_folderpath_param = FinanceRecorderCmnDef.CSV_BACKUP_ROOT_FOLDERPATH;
			}
			if (finance_backup_foldername_param == null)
			{
				finance_backup_foldername_param = FinanceRecorderCmnDef.get_time_folder_name();
			}
			String finance_backup_filepath = String.format("%s/%s", finance_backup_folderpath_param, finance_backup_foldername_param);
			finance_recorder_mgr.set_finance_backup_folderpath(finance_backup_filepath);
		}
// Setup the finance restore root folder path
		if (is_restore_operation_enabled())
		{
			if (finance_restore_folderpath_param == null)
			{
				finance_restore_folderpath_param = FinanceRecorderCmnDef.CSV_RESTORE_ROOT_FOLDERPATH;
			}
			if (finance_restore_foldername_param == null)
			{
				finance_restore_foldername_param = FinanceRecorderCmnDef.get_time_folder_name();
			}
			String finance_restore_filepath = String.format("%s/%s", finance_restore_folderpath_param, finance_restore_foldername_param);
			finance_recorder_mgr.set_finance_restore_folderpath(finance_restore_filepath);
		}
// Set the delete SQL accurancy 
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
		System.out.printf("--market_mode --stock_mode\nDescription: Switch the market/stock mode\nCaution: Read parameters from %s when NOT set\n", FinanceRecorderCmnDef.MARKET_STOCK_SWITCH_CONF_FILENAME);
		System.out.println("-h|--help\nDescription: The usage");
		System.out.println("--silent\nDescription: True for disabling STDOUR/STDERR");
		System.out.printf("check_sql_exist--finance_folderpath\nDescription: The finance folder path\nDefault: %s\n", FinanceRecorderCmnDef.CSV_ROOT_FOLDERPATH);
		System.out.printf("--finance_backup_folderpath\nDescription: The finance backup folder path\nDefault: %s\n", FinanceRecorderCmnDef.CSV_BACKUP_ROOT_FOLDERPATH);
		System.out.printf("--finance_restore_folderpath\nDescription: The finance restore folder path\nDefault: %s\n", FinanceRecorderCmnDef.CSV_RESTORE_ROOT_FOLDERPATH);
		System.out.println("--finance_backup_foldername\nDescription: Select an folder under the finance backup root folder\nDefault: Auto-generate the folder from the current time\nCaution: Only take effect for Database Operation: B(backup)");
		System.out.println("--finance_restore_foldername\nDescription: Select an folder under the finance restore root folder\nDefault: The latest operation folder\nCaution: Only take effect for Database Operation: R(restore)");
		System.out.println("  Format: 160313060053");
//		System.out.println("--operation_latest_folder\nDescription: Select the latest operation folder from the specfic finance root folder\nCaution: Only take effect for Database Operation: B(backup),R(restore)");
		System.out.printf("--show_finance_backup_foldername\nDescription: Show backup subfolder name list in a specific folder\nDefault folder: %s\n", FinanceRecorderCmnDef.CSV_BACKUP_ROOT_FOLDERPATH);
		System.out.printf("--show_finance_restore_foldername\nDescription: Show restore subfolder name list in a specific folder\nDefault folder: %s\n", FinanceRecorderCmnDef.CSV_RESTORE_ROOT_FOLDERPATH);
		System.out.println("-o|--database_operation\nDescription: Operate the MySQL");
		System.out.println("  Type: {W(w), B(b), D(d), C(c), R(r), E(e)");
		System.out.println("  W(w): Write into SQL from CSV files");
		System.out.println("  B(b): Backup SQL to CSV files");
		System.out.println("  D(d): delete existing SQL");
		System.out.println("  C(c): Clean-up all existing SQL");
		System.out.println("  R(r): Restore SQL from CSV file");
		System.out.println("  E(e): check SQL Exist");
		System.out.println("Caution:");
		System.out.println("  The R(r) attribute is ignored if W(w) set");
		System.out.println("  The D(d) attribute is ignored if C(c) set");
		System.out.println("  The C(c) attribute is enabled if R(r) set");
		System.out.println("--continue_when_csv_not_foud\nDescription: Continue running when CSV file Not Found\nCaution: Only take effect for Write/Restore action");
		System.out.println("--continue_when_sql_not_foud\nDescription: Continue running when SQL table Not Found\nCaution: Only take effect for Backup action");
		if (FinanceRecorderCmnDef.IS_FINANCE_STOCK_MODE)
		{
			System.out.println("--delete_sql_accurancy\nDescription: The accurancy of delete SQL\nDefault: Source type only\nCaution: Only take effect for Delete action");
			System.out.println("  Format 1 Source Type Only: 0");
			System.out.println("  Format 2 Company Only: 1");
			System.out.println("  Format 3 Source Type and Company: 2");
			System.out.println("--multi_thread\nDescription: Execute actions by using multiple threads\nCaution: Only take effect for Write action");
		}
		System.out.println("--backup_list\nDescription: List database backup folder");
		System.out.println("--restore_list\nDescription: List database restore folder");
		System.out.printf(String.format("--source_from_all_default_file\nDescription: The all finance data source in full time range from file: %s\nCaution: source is ignored when set\n", (FinanceRecorderCmnDef.IS_FINANCE_MARKET_MODE ? FinanceRecorderCmnDef.MARKET_ALL_CONFIG_FILENAME : FinanceRecorderCmnDef.STOCK_ALL_CONFIG_FILENAME)));
		System.out.printf(String.format("--source_from_file\nDescription: The finance data source from file\nCaution: source is ignored when set\n", (FinanceRecorderCmnDef.IS_FINANCE_MARKET_MODE ? FinanceRecorderCmnDef.MARKET_ALL_CONFIG_FILENAME : FinanceRecorderCmnDef.STOCK_ALL_CONFIG_FILENAME)));
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
//		System.out.println("--compress_file\nDescription: Access the compressed file for the backup/restore operation\nDefault: true\nCaution: Only take effect for Database Operation: B(backup) R(restore)");
//		System.out.println("--delete_old\nDescription: delete the old MySQL databases\nCaution: Ignored if --restore set");
//		System.out.println("--backup\nDescription: Backup the current databases");
//		System.out.println("--backup_list\nDescription: List database backup folder");
//		System.out.println("--backup_cleanup\nDescription: CleanUp all database backup sub-folders");
//		System.out.println("--copy_backup\nDescription: Copy the backup folder to the designated path\nCaution: Enabled if --backup set");
//		System.out.println("--check_error\nDescription: Check if the data in the MySQL database is correct");
//		System.out.println("--run_daily\nDescription: Run daily data\nCaution: Executed after writing MySQL data if set");
		System.out.println("===================================================");
		System.exit(0);
	}

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
	private static short show_backup_folername_list()
	{
		if(!FinanceRecorderCmnDef.is_show_console())
		{
			FinanceRecorderCmnDef.warn("The STDOUT/STDERR is Disabled");
			show_error_and_exit(FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG);
		}
// Find the backup folder list
		LinkedList<String> sorted_foldername_list = new LinkedList<String>();
		short ret = finance_recorder_mgr.get_backup_foldername_list(sorted_foldername_list);
		if (FinanceRecorderCmnDef.CheckSuccess(ret))
		{
// Print the result
			if (sorted_foldername_list.isEmpty())
				System.out.println("No backup folders Found");
			else
			{
				System.out.println("The backup folder list:");
				for (String foldername : sorted_foldername_list)
					System.out.println(foldername);
			}
		}
		else
			FinanceRecorderCmnDef.warn(String.format("Fail to get finance backup foldername list, due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret)));
		return ret;
	}

	private static short show_restore_folername_list()
	{
		if(!FinanceRecorderCmnDef.is_show_console())
		{
			FinanceRecorderCmnDef.warn("The STDOUT/STDERR is Disabled");
			show_error_and_exit(FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG);
		}
// Find the backup folder list
		LinkedList<String> sorted_foldername_list = new LinkedList<String>();
		short ret = finance_recorder_mgr.get_restore_foldername_list(sorted_foldername_list);
		if (FinanceRecorderCmnDef.CheckSuccess(ret))
		{
// Print the result
			if (sorted_foldername_list.isEmpty())
				System.out.println("No restore folders Found");
			else
			{
				System.out.println("The restore folder list:");
				for (String foldername : sorted_foldername_list)
					System.out.println(foldername);
			}
		}
		else
			FinanceRecorderCmnDef.warn(String.format("Fail to get finance restore foldername list, due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret)));
		return ret;
	}

	private static void show_backup_and_restore_foldername_list_and_exit()
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		if (show_finance_backup_foldername_param)
		{
			ret = show_backup_folername_list();
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				show_error_and_exit(String.format("Fail to show bakcup foldername list, due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret)));
		}
		if (show_finance_restore_foldername_param)
		{
			show_restore_folername_list();
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				show_error_and_exit(String.format("Fail to show bakcup foldername list, due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret)));
		}
		System.exit(0);
	}

	private static void write_operation()
	{
		if(FinanceRecorderCmnDef.is_show_console())
			System.out.printf("Write CSV[%s] data into MySQL......\n", finance_recorder_mgr.get_finance_folderpath());

		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		long time_start_millisecond = System.currentTimeMillis();
		if (multi_thread_param != null)
			ret = finance_recorder_mgr.transfrom_csv_to_sql_multithread(Integer.valueOf(multi_thread_param), !continue_when_csv_not_foud_param);
		else
			ret = finance_recorder_mgr.transfrom_csv_to_sql(!continue_when_csv_not_foud_param);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			show_error_and_exit(String.format("Fail to write CSV data into MySQL, due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret)));
		long time_end_millisecond = System.currentTimeMillis();

		if(FinanceRecorderCmnDef.is_show_console())
			System.out.println("Write CSV data into MySQL...... Done");

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
	}

	private static void backup_operation()
	{
		if(FinanceRecorderCmnDef.is_show_console())
			System.out.printf("Backup MySQL to CSV[%s]......\n", finance_recorder_mgr.get_finance_backup_folderpath());
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
// Create the finance backup folder
		ret = FinanceRecorderCmnDef.create_folder_if_not_exist(finance_backup_folderpath_param);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			FinanceRecorderCmnDef.format_error("Fail to create finance backup root folder[%s], due to: %s", finance_backup_folderpath_param, FinanceRecorderCmnDef.GetErrorDescription(ret));
		ret = finance_recorder_mgr.transfrom_sql_to_csv(finance_time_range, !continue_when_sql_not_foud_param);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			show_error_and_exit(String.format("Fail to backup MySQL to CSV, due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret)));
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
			System.out.printf("Restore MySQL data from CSV[%s]......\n", finance_recorder_mgr.get_finance_restore_folderpath());
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
// Check the finance restore folder exist
		if (!FinanceRecorderCmnDef.check_file_exist(finance_restore_folderpath_param))
			show_error_and_exit(String.format("The finance restore root folder[%s] does NOT exist", finance_restore_folderpath_param));
		long time_start_millisecond = System.currentTimeMillis();
		ret = finance_recorder_mgr.transfrom_csv_to_sql(!continue_when_csv_not_foud_param);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			show_error_and_exit(String.format("Fail to restore MySQL data from CSV, due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret)));
		long time_end_millisecond = System.currentTimeMillis();
		if(FinanceRecorderCmnDef.is_show_console())
			System.out.println("Restore MySQL data from CSV Tar...... Done");
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
	}

	private static void check_exist_operation()
	{
		if(FinanceRecorderCmnDef.is_show_console())
			System.out.println("Check MySQL exist......");

		ArrayList<String> not_exist_list = new ArrayList<String>();
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		ret = finance_recorder_mgr.check_sql_exist(not_exist_list);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
		{
			show_error_and_exit(String.format("Fail to check the MySQL exist, due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret)));
		}
		else
		{
			if (!not_exist_list.isEmpty())
			{
				String not_found_size_string = String.format("There are totally %d tables missing", not_exist_list.size());
				FinanceRecorderCmnDef.warn(not_found_size_string);
				if(FinanceRecorderCmnDef.is_show_console())
					System.out.println(not_found_size_string);
				for (String not_found : not_exist_list)
				{
					FinanceRecorderCmnDef.warn(not_found);
					if(FinanceRecorderCmnDef.is_show_console())
						System.out.println(not_found);
				}
			}
			else
			{
				String no_not_found_string = "There are NO tables missing";
				FinanceRecorderCmnDef.info(no_not_found_string);
				if(FinanceRecorderCmnDef.is_show_console())
					System.out.println(no_not_found_string);
			}
		}
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
//		File src_folder = new File("/home/super/Projects/finance_recorder_java/.backup/160604045541");
//		LinkedList<File> src_folder_list = new LinkedList();
//		src_folder_list.add(src_folder);
//		File dst_folder = new File("/home/super/Projects/finance_recorder_java/.backup/160604045541.tar.gz");
//		try
//		{
//			FinanceRecorderCmnDef.compress_files(src_folder_list, dst_folder);
//		}
//		catch (IOException ex)
//		{
//			System.err.printf("Error, due to: %s", ex.toString());
//		}
//		File dst_folder2 = new File("/home/super");
//		try
//		{
//			FinanceRecorderCmnDef.decompress_file(dst_folder, dst_folder2);
//		}
//		catch (IOException ex)
//		{
//			System.err.printf("Error, due to: %s", ex.toString());
//		}
//		System.exit(0);
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

// Determine the mode and initialize
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

// Show finance backup/restore foldername
		if (show_finance_backup_foldername_param || show_finance_restore_foldername_param)
			show_backup_and_restore_foldername_list_and_exit();
// After Initialization is done, start to work.......		
		if (is_check_exist_operation_enabled())
			check_exist_operation();

		if (is_cleanup_operation_enabled())
			cleanup_operation();
		else if (is_delete_operation_enabled())
			delete_operation();

		if (is_write_operation_enabled())
			write_operation();
		else if (is_restore_operation_enabled())
			restore_operation();

		if (is_backup_operation_enabled())
			backup_operation();

		wait_to_exit();
	}

}
