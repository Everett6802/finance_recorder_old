package com.price.finance_recorder_cmdline_test;

import java.util.ArrayList;
import java.util.LinkedList;

import com.price.finance_recorder.FinanceRecorder;
import com.price.finance_recorder.FinanceRecorderCmnDef;
import com.price.finance_recorder.FinanceRecorderCmnDef.FinanceAnalysisMode;
import com.price.finance_recorder.FinanceRecorderCmnDef.DeleteSQLAccurancyType;
import com.price.finance_recorder.FinanceRecorderCmnDef.DefaultConstType;

public class FinanceRecorderCmdlineTest 
{
//	private static final String PARAM_SPLIT = ",";
	private static final byte DATABASE_OPERATION_WRITE_MASK = 0x1;
	private static final byte DATABASE_OPERATION_BACKUP_MASK = 0x1 << 1;
	private static final byte DATABASE_OPERATION_DELETE_MASK = 0x1 << 2;
	private static final byte DATABASE_OPERATION_CLEANUP_MASK = 0x1 << 3;
	private static final byte DATABASE_OPERATION_RESTORE_MASK = 0x1 << 4;
	private static final byte DATABASE_OPERATION_CHECK_EXIST_MASK = 0x1 << 5;

	private static boolean market_mode_param = false;
	private static boolean stock_mode_param = false;

	private static boolean help_param = false;
	private static String company_profile_filepath_param = null;
	private static boolean compare_company_param = false;
	private static boolean renew_company_param = false;
	private static String statement_profile_filepath_param = null;
	private static boolean compare_statement_param = false;
	private static boolean renew_statement_param = false;
	private static String statement_method_param = null;
	private static String finance_folderpath_param = null;
	private static String finance_backup_folderpath_param = null;
	private static String finance_restore_folderpath_param = null;
	private static String finance_backup_foldername_param = null;
	private static String finance_restore_foldername_param = null;
	private static boolean show_finance_backup_list_param = false;
	private static boolean show_finance_restore_list_param = false;
	private static String delete_sql_accurancy_param = null;
	private static String multi_thread_param = null;
	private static String database_operation_param = null;
	private static String set_operation_non_stop_param = null;
	private static String method_from_file_param = null;
	private static String method_param = null;
	private static String time_range_param = null;
	private static String company_from_file_param = null;
	private static String company_param = null;
	// private static boolean compress_file_param = false;

	// private static FinanceRecorderMgrInf FinanceRecorder = null;
	private static boolean show_console = true;
	private static byte database_operation = 0x0;
	private static boolean operation_non_stop = true;
	private static DeleteSQLAccurancyType delete_sql_accurancy_type = null;

	private static boolean is_market_mode()
	{
		return FinanceRecorder.get_finance_mode() == FinanceAnalysisMode.FinanceAnalysis_Market ? true : false;
	}
	private static boolean is_stock_mode()
	{
		return FinanceRecorder.get_finance_mode() == FinanceAnalysisMode.FinanceAnalysis_Stock ? true : false;
	}

	private static String lib_const(DefaultConstType default_const_type)
	{
		return lib_const(default_const_type);
	}

	private static boolean is_write_operation_enabled() 
	{
		return (database_operation & DATABASE_OPERATION_WRITE_MASK) != 0;
	}

	private static boolean is_backup_operation_enabled() 
	{
		return (database_operation & DATABASE_OPERATION_BACKUP_MASK) != 0;
	}

	private static boolean is_delete_operation_enabled() 
	{
		return (database_operation & DATABASE_OPERATION_DELETE_MASK) != 0;
	}

	private static boolean is_cleanup_operation_enabled() 
	{
		return (database_operation & DATABASE_OPERATION_CLEANUP_MASK) != 0;
	}

	private static boolean is_restore_operation_enabled() 
	{
		return (database_operation & DATABASE_OPERATION_RESTORE_MASK) != 0;
	}

	private static boolean is_check_exist_operation_enabled() 
	{
		return (database_operation & DATABASE_OPERATION_CHECK_EXIST_MASK) != 0;
	}

	private static void PRINT_STDOUT(String msg) 
	{
		if (show_console)
			System.out.printf(msg);
	}

	private static void PRINT_STDERR(String msg) 
	{
		if (show_console)
			System.err.printf(msg);
	}

	private static void show_error_and_exit(String err_msg) 
	{
		PRINT_STDERR(err_msg);
		System.exit(1);
	}

	private static String get_time_elapse_string(long time_start_millisecond, long time_end_millisecond)
	{
		long time_lapse_millisecond = time_end_millisecond - time_start_millisecond;
		String time_lapse_msg;
		if (time_lapse_millisecond >= 100 * 1000)
			time_lapse_msg = String.format("######### Time Lapse: %d second(s) #########", (int) ((time_end_millisecond - time_start_millisecond) / 1000));
		else if (time_lapse_millisecond >= 10 * 1000)
			time_lapse_msg = String.format("######### Time Lapse: %d second(s) #########", (int) ((time_end_millisecond - time_start_millisecond) / 1000));
		else
			time_lapse_msg = String.format("######### Time Lapse: %d second(s) #########", (int) ((time_end_millisecond - time_start_millisecond) / 1000));
		return time_lapse_msg;
	}

	private static void show_error_and_exit(short ret) 
	{
		String err_msg = String.format("Error occur due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret));
		PRINT_STDERR(err_msg);
		System.exit(1);
	}


	private static void show_usage_and_exit() 
	{
		System.out.println("====================== Usage ======================");
		System.out.printf("--market_mode --stock_mode\nDescription: Switch the market/stock mode\nCaution: Read parameters from %s when NOT set\n", lib_const(DefaultConstType.DefaultConstType_MARKET_STOCK_SWITCH_CONF_FILENAME));
		System.out.println("--silent\nDescription: True for disabling STDOUR/STDERR");
		System.out.println("-h|--help\nDescription: The usage");
		System.out.printf("check_sql_exist--finance_folderpath\nDescription: The finance folder path\nDefault: %s\n", lib_const(DefaultConstType.DefaultConstType_CSV_ROOT_FOLDERPATH));
		System.out.printf("--finance_backup_folderpath\nDescription: The finance backup folder path\nDefault: %s\n", lib_const(DefaultConstType.DefaultConstType_CSV_BACKUP_FOLDERPATH));
		System.out.printf("--finance_restore_folderpath\nDescription: The finance restore folder path\nDefault: %s\n", lib_const(DefaultConstType.DefaultConstType_CSV_RESTORE_FOLDERPATH));
		System.out.println("--finance_backup_foldername\nDescription: Select an folder under the finance backup root folder\nDefault: Auto-generate the folder from the current time\nCaution: Only take effect for Database Operation: B(backup)");
		System.out.println("--finance_restore_foldername\nDescription: Select an folder under the finance restore root folder\nDefault: The latest operation folder\nCaution: Only take effect for Database Operation: R(restore)");
		System.out.println("  Format: 160313060053");
		// System.out.println("--operation_latest_folder\nDescription: Select the latest operation folder from the specfic finance root folder\nCaution: Only take effect for Database Operation: B(backup),R(restore)");
		System.out.printf("--show_finance_backup_list\nDescription: Show backup subfolder name list in a specific folder\nDefault folder: %s\n", lib_const(DefaultConstType.DefaultConstType_CSV_BACKUP_FOLDERPATH));
		System.out.printf("--show_finance_restore_list\nDescription: Show restore subfolder name list in a specific folder\nDefault folder: %s\n", lib_const(DefaultConstType.DefaultConstType_CSV_RESTORE_FOLDERPATH));
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
		System.out.println("--set_operation_non_stop\nDescription: Keep running or stop while accessing data and error occurs\nDefault: True");
		System.out.println("  Type: TRUE/True/true FALSE/False/false");
		System.out.println("  TRUE/True/true: Keep running while accessing data and error occurs");
		System.out.println("  FALSE/False/false: Stop while accessing data and error occurs");
		System.out.println("--backup_list\nDescription: List database backup folder");
		System.out.println("--restore_list\nDescription: List database restore folder");
		System.out.printf(String.format("--method_from_all_default_file\nDescription: The all finance method in full time range from file: %s\nCaution: method is ignored when set\n", (is_market_mode() ? lib_const(DefaultConstType.DefaultConstType_MARKET_ALL_CONFIG_FILENAME) : lib_const(DefaultConstType.DefaultConstType_STOCK_ALL_CONFIG_FILENAME))));
		System.out.printf(String.format("--method_from_file\nDescription: The finance method from file\nCaution: method is ignored when set\n", (is_market_mode() ? lib_const(DefaultConstType.DefaultConstType_MARKET_ALL_CONFIG_FILENAME) : lib_const(DefaultConstType.DefaultConstType_STOCK_ALL_CONFIG_FILENAME))));
		System.out.println("-m|--method\nDescription: Data method\nCaution: Ignored when --method_from_file/--method_from_all_default_file set");
		System.out.println("  Format 1: Method (ex. 1,3,5)");
		System.out.println("  Format 2: Method range (ex. 2-6)");
		System.out.println("  Format 3: Method/Method range hybrid (ex. 1,3-4,6)");
		ArrayList<Integer> all_method_index_list = new ArrayList<Integer>();
		ArrayList<String> all_method_description_list = new ArrayList<String>();
		FinanceRecorder.get_all_method_index_list(all_method_index_list);
		FinanceRecorder.get_all_method_description_list(all_method_description_list);
		assert all_method_index_list.size() == all_method_description_list.size() : "The dimensions of all method index/description are NOT identical";
		for (int i = 0 ; i < all_method_index_list.size() ; i++)
			System.out.printf("  %d: %s\n", all_method_index_list.get(i), all_method_description_list.get(i));
		System.out.println("-t|--time_range\nDescription: The time range of the SQL\nDefault: full range in SQL\nCaution: Only take effect for Database Operation: B(backup)");
		System.out.println("  Format 1 (start_time): 2015-09");
		System.out.println("  Format 1 (,end_time): ,2015-09");
		System.out.println("  Format 2 (start_time,end_time): 2015-01,2015-09");
		if (is_stock_mode()) 
		{
			System.out.println("--delete_sql_accurancy\nDescription: The accurancy of delete SQL\nDefault: Method only\nCaution: Only take effect for Delete operation");
			System.out.println("  Format 1 Method Only: 0");
			System.out.println("  Format 2 Company Only: 1");
			System.out.println("  Format 3 Method and Company: 2");
			System.out.println("--multi_thread\nDescription: Execute actions by using multiple threads\nCaution: Only take effect for Write action");
			System.out.println("--company_from_file\nDescription: The company code number from file\nDefault: All company code nubmers\nCaution: company is ignored when set");
			System.out.println("-c|--company\nDescription: The list of the company code number\nDefault: All company code nubmers\nCaution: Only work when company_from_file is NOT set");
			System.out.println("  Format 1 Company code number: 2347");
			System.out.println("  Format 2 Company code number range: 2100-2200");
			System.out.println("  Format 3 Company group number: [Gg]12");
			System.out.println("  Format 4 Company code number/number range/group hybrid: 2347,2100-2200,G12,2362,g2,1500-1510");
			System.out.println("--compare_company\nDescription: Compare the table of the company profile\nCaution: Exit after comparing the company profile");
			System.out.println("--renew_company\nDescription: Compare the table of the company profile and Renew it if necessary\nCaution: The old companies are removed and the new ones are added after renewing. Exit after renewing the company profile");
			System.out.printf("--company_profile_filepath\nDescription: The company profile filepath\nDefault: %s\n", lib_const(DefaultConstType.DefaultConstType_DEFAULT_COMPANY_PROFILE_CONF_FOLDERPATH));
			System.out.println("--compare_statement\nDescription: Compare the table of the statement profile\nCaution: Exit after comparing the statement");
			System.out.println("--renew_statement\nDescription: Compare the table of the statement profile and Renew it if necessary\nCaution: All data in the database are cleaned-up after renewing. Exit after renewing the company profile");
//			System.out.println("--statement_source\nDescription: Statement Data source type");
//			System.out.println("  Format 1: Statement type (ex. 0,2)");
//			System.out.println("  Format 2: Statement type range (ex. 1-3)");
//			System.out.println("  Format 3: Statement type/type range hybrid (ex. 0,2-3)");
//			LinkedList<Integer> whole_stock_statement_method_index_list = FinanceRecorderCmnDef.get_all_stock_statement_method_index_list();
//			for (Integer index : whole_stock_statement_method_index_list)
//				System.out.printf("  %d: %s\n", index + FinanceRecorderCmnDef.FINANCE_DATA_SOURCE_STOCK_STATMENT_START, FinanceRecorderCmnDef.FINANCE_DATA_DESCRIPTION_LIST[index]);
			System.out.printf("--statement_profile_filepath\nDescription: The statement profile filepath\nDefault: %s\n", lib_const(DefaultConstType.DefaultConstType_DEFAULT_STATEMENT_PROFILE_CONF_FOLDERPATH));
		}
		System.out.println("===================================================");
		System.exit(0);
	}

	private static void determine_finance_mode()
	{
// Determine the mode and initialize the manager class
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		if (market_mode_param && stock_mode_param)
		{
			show_error_and_exit("Fail to determine the finance mode !!! Both Market/Stock mode are set");
		}
		else if (market_mode_param)
		{
			ret = FinanceRecorder.set_market_mode();
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				show_error_and_exit(String.format("Fail to set the MARKET mode !!! Due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret)));
		}
		else if (stock_mode_param)
		{
			ret = FinanceRecorder.set_stock_mode();
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				show_error_and_exit(String.format("Fail to set the STOCK mode !!! Due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret)));
		}
		else
		{
			ret = FinanceRecorder.set_mode_from_cfg();
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				show_error_and_exit(String.format("Fail to set the finance mode from config file !!! Due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret)));
		}
	}

	private static short parse_param(String args[], boolean early_parse_param) 
	{
		int index = 0;
		int index_offset = 0;
		int args_len = args.length;
// Parse the argument
		while (index < args_len) 
		{
			String option = args[index];
			if (option.equals("--market_mode")) // Need to be parsed first
			{
				if (early_parse_param) 
				{
					market_mode_param = true;
				}
				index_offset = 1;
			}
			else if (option.equals("--stock_mode")) // Need to be parsed first
			{
				if (early_parse_param) 
				{
					stock_mode_param = true;
				}
				index_offset = 1;
			}
			else if (option.equals("--silent"))
			{
				if (early_parse_param) 
				{
					show_console = false;
				}
				index_offset = 1;
			}
			else if (option.equals("-h") || option.equals("--help"))
			{
				if (!early_parse_param) 
				{
					help_param = true;
				}
				index_offset = 1;
			}
			else if (option.equals("--company_profile_filepath"))
			{
				if (!early_parse_param) 
				{
					company_profile_filepath_param = args[index + 1];
				}
				index_offset = 2;
			}
			else if (option.equals("--compare_company"))
			{
				if (!early_parse_param) 
				{
					compare_company_param = true;
				}
				index_offset = 1;
			}
			else if (option.equals("--renew_company"))
			{
				if (!early_parse_param) 
				{
					renew_company_param = true;
				}
				index_offset = 1;
			}
			else if (option.equals("--statement_profile_filepath"))
			{
				if (!early_parse_param) 
				{
					statement_profile_filepath_param = args[index + 1];
				}
				index_offset = 2;
			}
			else if (option.equals("--compare_statement"))
			{
				if (!early_parse_param) 
				{
					compare_statement_param = true;
				}
				index_offset = 1;
			}
			else if (option.equals("--renew_statement"))
			{
				if (!early_parse_param) 
				{
					renew_statement_param = true;
				}
				index_offset = 1;
			}
			else if (option.equals("--finance_folderpath"))
			{
				if (!early_parse_param) 
				{
					if (index + 1 >= args_len)
						show_error_and_exit(String.format("The option[%s] does NOT contain value", option));
					finance_folderpath_param = args[index + 1];
				}
				index_offset = 2;
			}
			else if (option.equals("--finance_backup_folderpath"))
			{
				if (!early_parse_param) 
				{
					if (index + 1 >= args_len)
						show_error_and_exit(String.format("The option[%s] does NOT contain value", option));
					finance_backup_folderpath_param = args[index + 1];
				}
				index_offset = 2;
			}
			else if (option.equals("--finance_restore_folderpath"))
			{
				if (!early_parse_param) 
				{
					if (index + 1 >= args_len)
						show_error_and_exit(String.format("The option[%s] does NOT contain value", option));
					finance_restore_folderpath_param = args[index + 1];
				}
				index_offset = 2;
			}
			else if (option.equals("--finance_backup_foldername"))
			{
				if (!early_parse_param) 
				{
					if (index + 1 >= args_len)
						show_error_and_exit(String.format("The option[%s] does NOT contain value", option));
					finance_backup_foldername_param = args[index + 1];
				}
				index_offset = 2;
			}
			else if (option.equals("--finance_restore_foldername"))
			{
				if (!early_parse_param) 
				{
					if (index + 1 >= args_len)
						show_error_and_exit(String.format("The option[%s] does NOT contain value", option));
					finance_restore_foldername_param = args[index + 1];
				}
				index_offset = 2;
			}
			else if (option.equals("--delete_sql_accurancy"))
			{
				if (!early_parse_param) 
				{
					if (index + 1 >= args_len)
						show_error_and_exit(String.format("The option[%s] does NOT contain value", option));
					if (is_market_mode())
						PRINT_STDOUT("WARNING: The delete_sql_accurancy arguemnt is ignored in the Market mode");
					else
						delete_sql_accurancy_param = args[index + 1];
				}
				index_offset = 2;
			}
			else if (option.equals("--multi_thread"))
			{
				if (!early_parse_param) 
				{
					if (index + 1 >= args_len)
						show_error_and_exit(String.format("The option[%s] does NOT contain value", option));
					multi_thread_param = args[index + 1];
				}
				index_offset = 2;
			}
			else if (option.equals("--show_finance_backup_list"))
			{
				if (!early_parse_param) 
				{
					show_finance_backup_list_param = true;
				}
				index_offset = 1;
			}
			else if (option.equals("--show_finance_restore_list"))
			{
				if (!early_parse_param) 
				{
					show_finance_restore_list_param = true;
				}
				index_offset = 1;
			}
			else if (option.equals("--set_operation_non_stop"))
			{
				if (!early_parse_param) 
				{
					if (index + 1 >= args_len)
						show_error_and_exit(String.format("The option[%s] does NOT contain value", option));
					set_operation_non_stop_param = args[index + 1];
				}
				index_offset = 2;
			}
			else if (option.equals("-o") || option.equals("--operation"))
			{
				if (!early_parse_param) 
				{
					if (index + 1 >= args_len)
						show_error_and_exit(String.format("The option[%s] does NOT contain value", option));
					database_operation_param = args[index + 1];
				}
				index_offset = 2;
			}
			else if (option.equals("--method_from_file"))
			{
				if (!early_parse_param) 
				{
					if (index + 1 >= args_len)
						show_error_and_exit(String.format("The option[%s] does NOT contain value", option));
					method_from_file_param = args[index + 1];
				}
				index_offset = 2;
			}
			else if (option.equals("-m") || option.equals("--method"))
			{
				if (!early_parse_param) 
				{
					if (index + 1 >= args_len)
						show_error_and_exit(String.format("The option[%s] does NOT contain value", option));
					method_param = args[index + 1];
				}
				index_offset = 2;
			}
			else if (option.equals("-t") || option.equals("--time_range"))
			{
				if (!early_parse_param) 
				{
					if (index + 1 >= args_len)
						show_error_and_exit(String.format("The option[%s] does NOT contain value", option));
					time_range_param = args[index + 1];
				}
				index_offset = 2;
			}
			else if (option.equals("--company_from_file"))
			{
				if (!early_parse_param) 
				{
					if (index + 1 >= args_len)
						show_error_and_exit(String.format("The option[%s] does NOT contain value", option));
					if (is_market_mode())
						PRINT_STDOUT("WARNING: The company_from_file arguemnt is ignored in the Market mode");
					else
						company_from_file_param = args[index + 1];
				}
				index_offset = 2;
			}
			else if (option.equals("-c") || option.equals("--company"))
			{
				if (!early_parse_param) 
				{
					if (index + 1 >= args_len)
						show_error_and_exit(String.format("The option[%s] does NOT contain value", option));
					if (is_market_mode())
						PRINT_STDOUT("WARNING: The company_from_file arguemnt is ignored in the Market mode");
					else
						company_param = args[index + 1];
				}
				index_offset = 2;
			}
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
				PRINT_STDOUT("WARNING: The 'write' and 'resotre' operation can NOT be enabled simultaneously, ignore the 'restore' operation");
				database_operation &= ~DATABASE_OPERATION_RESTORE_MASK;
			}
			if ((database_operation & DATABASE_OPERATION_RESTORE_MASK) != 0 && (database_operation & DATABASE_OPERATION_CLEANUP_MASK) == 0) 
			{
				PRINT_STDOUT("WARNING: The 'cleanup' operation should be enabled, when the 'restore' operation is set");
				database_operation |= DATABASE_OPERATION_CLEANUP_MASK;
			}
			if ((database_operation & DATABASE_OPERATION_DELETE_MASK) != 0 && (database_operation & DATABASE_OPERATION_CLEANUP_MASK) != 0) 
			{
				PRINT_STDOUT("WARNING: The 'delete' operation is ignored since the 'cleanup' operation is set");
				database_operation &= ~DATABASE_OPERATION_DELETE_MASK;
			}
		}
		if (set_operation_non_stop_param != null) 
		{
			if (set_operation_non_stop_param.equals("TRUE") | set_operation_non_stop_param.equals("True") || set_operation_non_stop_param.equals("true")) 
			{
				operation_non_stop = true;
			} 
			else if (set_operation_non_stop_param.equals("FALSE") || set_operation_non_stop_param.equals("False") || set_operation_non_stop_param.equals("false")) 
			{
				operation_non_stop = false;
			} 
			else 
			{
				show_error_and_exit(String.format("Unknown value of the set_operation_non_stop attribute: %d", set_operation_non_stop_param));
			}
		}

		if (finance_backup_folderpath_param != null) 
		{
			if (!is_backup_operation_enabled()) 
			{
				finance_backup_folderpath_param = null;
				PRINT_STDOUT("WARNING: The 'finance_backup_folderpath_param' argument is ignored since Backup action is NOT set");
			}
		}
		if (finance_restore_folderpath_param != null) 
		{
			if (!is_restore_operation_enabled()) 
			{
				finance_restore_folderpath_param = null;
				PRINT_STDOUT("WARNING: The 'finance_restore_folderpath_param' argument is ignored since Restore action is NOT set");
			}
		}
		if (finance_backup_foldername_param != null) 
		{
			if (!is_backup_operation_enabled()) 
			{
				finance_backup_foldername_param = null;
				PRINT_STDOUT("WARNING: The 'finance_backup_foldername_param' argument is ignored since Backup action is NOT set");
			}
		}
		if (finance_restore_foldername_param != null) 
		{
			if (!is_restore_operation_enabled()) 
			{
				if (finance_restore_foldername_param != null) 
				{
					finance_restore_foldername_param = null;
					PRINT_STDOUT("WARNING: The 'finance_restore_foldername_param' argument is ignored since Restore action is NOT set");
				}
			}
		}
		if (delete_sql_accurancy_param != null) 
		{
			if (is_delete_operation_enabled()) 
			{
				try 
				{
					delete_sql_accurancy_type = DeleteSQLAccurancyType.valueOf(Integer.valueOf(delete_sql_accurancy_param));
				} 
				catch (Exception e) {}
				if (delete_sql_accurancy_type == null)
					throw new IllegalStateException(String.format("Unknown delete sql accurancy type: %s", delete_sql_accurancy_param));
			} 
			else 
			{
				delete_sql_accurancy_param = null;
				PRINT_STDOUT("WARNING: The 'delete_sql_accurancy' argument is ignored since Delete action is NOT set");
			}
		}

		if (multi_thread_param != null) 
		{
			if (!is_write_operation_enabled())
			{
				multi_thread_param = null;
				PRINT_STDOUT("WARNING: The 'multi_thread_param' argument is ignored since Write action is NOT set");
			}
		}
		if (method_from_file_param != null) 
		{
			if (method_param != null) 
			{
				method_param = null;
				PRINT_STDOUT("WARNING: The 'method' argument is ignored since 'method_from_file' is set");
			}
			if (time_range_param != null) 
			{
				time_range_param = null;
				PRINT_STDOUT("WARNING: The 'time_range' argument is ignored since 'method_from_file' is set");
			}
		}
		if (is_market_mode()) 
		{
			if (company_param != null) 
			{
				company_param = null;
				PRINT_STDOUT("WARNING: The 'company' argument is ignored since it's Market mode");
			}
			if (compare_company_param) 
			{
				compare_company_param = false;
				PRINT_STDOUT("WARNING: The 'compare_company' argument is ignored since it's Market mode");
			}
			if (renew_company_param) 
			{
				renew_company_param = false;
				PRINT_STDOUT("WARNING: The 'renew_company' argument is ignored since it's Market mode");
			}
			if (company_profile_filepath_param != null) 
			{
				company_profile_filepath_param = null;
				PRINT_STDOUT("WARNING: The 'company_profile_filepath' argument is ignored since it's Market mode");
			}
			if (compare_statement_param) 
			{
				compare_statement_param = false;
				PRINT_STDOUT("WARNING: The 'no_compare_statement' argument is ignored since it's Market mode");
			}
			if (renew_statement_param) 
			{
				renew_statement_param = false;
				PRINT_STDOUT("WARNING: The 'renew_statement' argument is ignored since it's Market mode");
			}
			if (statement_profile_filepath_param != null) 
			{
				statement_profile_filepath_param = null;
				PRINT_STDOUT("WARNING: The 'statement_profile_filepath' argument is ignored since it's Market mode");
			}
			if (company_from_file_param != null) 
			{
				company_from_file_param = null;
				PRINT_STDOUT("WARNING: The 'company_from_file' argument is ignored since it's Market mode");
			}
			if (delete_sql_accurancy_param != null) 
			{
				delete_sql_accurancy_param = null;
				PRINT_STDOUT("WARNING: The 'delete_sql_accurancy' argument is ignored since it's Market mode");
			}
		} 
		else 
		{
			if (renew_company_param && !compare_company_param) 
			{
				compare_company_param = true;
				PRINT_STDOUT("WARNING: The 'compare_company' argument is enabled since 'renew_company' is set");
			}
			if (!compare_company_param && is_write_operation_enabled()) 
			{
				PRINT_STDOUT("WARNING: The 'compare_company' argument should be enabled when the 'write' operation is set");
				compare_company_param = true;
			}
			if (compare_company_param) 
			{
//				if (company_profile_filepath_param == null)
//					company_profile_filepath_param = FinanceRecorderCmnDef.DEFAULT_COMPANY_PROFILE_CONF_FOLDERPATH;
			} 
			else 
			{
				if (company_profile_filepath_param != null) 
				{
					company_profile_filepath_param = null;
					PRINT_STDOUT("WARNING: The 'company_profile_filepath' argument is ignored since 'renew_company' is NOT set");
				}
			}
			if (renew_statement_param && !compare_statement_param) 
			{
				compare_statement_param = true;
				PRINT_STDOUT("WARNING: The 'compare_statement' argument is enabled since 'renew_statement' is set");
			}
			if (!compare_statement_param && is_write_operation_enabled()) 
			{
				PRINT_STDOUT("WARNING: The 'compare_statement' argument should be enabled when the 'write' operation is set");
				compare_statement_param = true;
			}
			if (compare_statement_param) 
			{
//				if (statement_profile_filepath_param == null)
//					statement_profile_filepath_param = FinanceRecorderCmnDef.DEFAULT_STATEMENT_PROFILE_CONF_FOLDERPATH;
			} 
			else 
			{
				if (statement_profile_filepath_param != null) 
				{
					statement_profile_filepath_param = null;
					PRINT_STDOUT("WARNING: The 'statement_profile_filepath' argument is ignored since 'compare_statement_param' is NOT set");
				}
			}
			if (company_from_file_param != null) 
			{
				if (company_param != null) 
				{
					company_param = null;
					PRINT_STDOUT("WARNING: The 'company' argument is ignored since 'company_from_file' is set");
				}
			}
		}
		return ret;
	}

	private static short setup_param() 
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
//		String errmsg = null;
// Setup the finance root folder path
		if (finance_folderpath_param != null)
			FinanceRecorder.set_finance_folderpath(finance_folderpath_param);
// Switch the flag to keep or stop accessing data while error occurs
		if (set_operation_non_stop_param != null)
			FinanceRecorder.set_operation_non_stop(operation_non_stop);
// Setup the finance backup root folder path
		if (is_backup_operation_enabled()) 
		{
			if (finance_backup_folderpath_param != null) 
				FinanceRecorder.set_finance_backup_folderpath(finance_backup_folderpath_param);
			if (finance_backup_foldername_param != null)
				FinanceRecorder.set_finance_backup_foldername(finance_backup_foldername_param);
		}
// Setup the finance restore root folder path
		if (is_restore_operation_enabled()) 
		{
			if (finance_restore_folderpath_param != null) 
				FinanceRecorder.set_finance_restore_folderpath(finance_restore_folderpath_param);
			if (finance_restore_foldername_param != null)
				FinanceRecorder.set_finance_restore_foldername(finance_restore_foldername_param);
		}
// Set the delete SQL accurancy
		if (delete_sql_accurancy_param != null)
			FinanceRecorder.set_delete_sql_accurancy(delete_sql_accurancy_type);
// Set method
		if (method_from_file_param != null)
			ret = FinanceRecorder.set_method_from_file(method_from_file_param);
		else 
		{
			ret = FinanceRecorder.set_method(method_param);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				show_error_and_exit(String.format("Fail to set method, due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret)));
// Set the time range
			if (time_range_param != null)
			{
				ret = FinanceRecorder.set_time_range(time_range_param);
				if (FinanceRecorderCmnDef.CheckFailure(ret))
					show_error_and_exit(String.format("Fail to set time range, due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret)));
			}
		}
// Set company list. For stock mode only
		if (is_stock_mode()) 
		{
			if (company_from_file_param != null) 
			{
				ret = FinanceRecorder.set_company_from_file(company_from_file_param);
				if (FinanceRecorderCmnDef.CheckFailure(ret))
					show_error_and_exit(String.format("Fail to set company from file, due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret)));
			} 
			else if (company_param != null) 
			{
				ret = FinanceRecorder.set_company(company_param);
				if (FinanceRecorderCmnDef.CheckFailure(ret))
					show_error_and_exit(String.format("Fail to set company, due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret)));
			}
		}
		return ret;
	}

	private static void write_operation() 
	{
		PRINT_STDOUT(String.format("Write CSV[%s] data into SQL......\n", FinanceRecorder.get_finance_folderpath()));
//		FinanceRecorder.switch_current_csv_working_folerpath(CmnDef.CSVWorkingFolderType.CSVWorkingFolder_Write);
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		long time_start_millisecond = System.currentTimeMillis();
		if (multi_thread_param != null)
			ret = FinanceRecorder.operation_write_multithread(Integer.valueOf(multi_thread_param));
		else
			ret = FinanceRecorder.operation_write();
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			show_error_and_exit(String.format("Fail to write CSV data into MySQL, due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret)));
		long time_end_millisecond = System.currentTimeMillis();
		PRINT_STDOUT("Write CSV data to SQL...... Done");
// Calculate the time elapse
		PRINT_STDOUT(get_time_elapse_string(time_start_millisecond, time_end_millisecond));
	}
//
//	private static void backup_operation() 
//	{
//		if (show_console)
//			System.out.printf("Backup SQL to CSV[%s]......\n",
//					FinanceRecorder.get_finance_backup_folderpath());
//		FinanceRecorder
//				.switch_current_csv_working_folerpath(CmnDef.CSVWorkingFolderType.CSVWorkingFolder_Backup);
//		short ret = CmnDef.RET_SUCCESS;
//		long time_start_millisecond = System.currentTimeMillis();
//		// Create the finance backup folder
//		ret = CmnDef.create_folder_if_not_exist(finance_backup_folderpath_param);
//		if (CmnDef.CheckFailure(ret))
//			CmnDef.format_error(
//					"Fail to create finance backup root folder[%s], due to: %s",
//					finance_backup_folderpath_param,
//					CmnDef.GetErrorDescription(ret));
//		ret = FinanceRecorder.transfrom_sql_to_csv(finance_time_range);
//		if (CmnDef.CheckFailure(ret))
//			show_error_and_exit(String.format(
//					"Fail to backup MySQL to CSV, due to: %s",
//					CmnDef.GetErrorDescription(ret)));
//		long time_end_millisecond = System.currentTimeMillis();
//
//		if (show_console)
//			System.out.println("Backup SQL to CSV...... Done");
//
//		long time_lapse_millisecond = time_end_millisecond
//				- time_start_millisecond;
//		String time_lapse_msg;
//		if (time_lapse_millisecond >= 100 * 1000)
//			time_lapse_msg = String
//					.format("######### Time Lapse: %d second(s) #########",
//							(int) ((time_end_millisecond - time_start_millisecond) / 1000));
//		else if (time_lapse_millisecond >= 10 * 1000)
//			time_lapse_msg = String
//					.format("######### Time Lapse: %d second(s) #########",
//							(int) ((time_end_millisecond - time_start_millisecond) / 1000));
//		else
//			time_lapse_msg = String
//					.format("######### Time Lapse: %d second(s) #########",
//							(int) ((time_end_millisecond - time_start_millisecond) / 1000));
//		CmnDef.info(time_lapse_msg);
//		if (show_console)
//			System.out.println(time_lapse_msg);
//	}
//
	private static void cleanup_operation() 
	{
		PRINT_STDOUT("Cleanup old MySQL data......");

		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		ret = FinanceRecorder.operation_cleanup();
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			show_error_and_exit(String.format("Fail to cleanup the old MySQL, due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret)));
	}

//	private static void delete_operation() {
//		if (show_console)
//			System.out.println("Delete old MySQL data......");
//
//		short ret = CmnDef.RET_SUCCESS;
//		ret = FinanceRecorder.operation_delete();
//		if (CmnDef.CheckFailure(ret))
//			show_error_and_exit(String.format(
//					"Fail to delete the old MySQL, due to: %s",
//					CmnDef.GetErrorDescription(ret)));
//	}
//
//	private static void restore_operation() {
//		if (show_console)
//			System.out.printf("Restore SQL data from CSV[%s]......\n",
//					FinanceRecorder.get_finance_restore_folderpath());
//		FinanceRecorder
//				.switch_current_csv_working_folerpath(CmnDef.CSVWorkingFolderType.CSVWorkingFolder_Restore);
//		short ret = CmnDef.RET_SUCCESS;
//		// Check the finance restore folder exist
//		if (!CmnDef.check_file_exist(finance_restore_folderpath_param))
//			show_error_and_exit(String.format(
//					"The finance restore root folder[%s] does NOT exist",
//					finance_restore_folderpath_param));
//		long time_start_millisecond = System.currentTimeMillis();
//		if (multi_thread_param != null)
//			ret = FinanceRecorder.transfrom_csv_to_sql_multithread(Integer
//					.valueOf(multi_thread_param));
//		else
//			ret = FinanceRecorder.transfrom_csv_to_sql();
//		if (CmnDef.CheckFailure(ret))
//			show_error_and_exit(String.format(
//					"Fail to restore MySQL data from CSV, due to: %s",
//					CmnDef.GetErrorDescription(ret)));
//		long time_end_millisecond = System.currentTimeMillis();
//		if (show_console)
//			System.out.println("Restore SQL data from CSV...... Done");
//		long time_lapse_millisecond = time_end_millisecond
//				- time_start_millisecond;
//		String time_lapse_msg;
//		if (time_lapse_millisecond >= 100 * 1000)
//			time_lapse_msg = String
//					.format("######### Time Lapse: %d second(s) #########",
//							(int) ((time_end_millisecond - time_start_millisecond) / 1000));
//		else if (time_lapse_millisecond >= 10 * 1000)
//			time_lapse_msg = String
//					.format("######### Time Lapse: %d second(s) #########",
//							(int) ((time_end_millisecond - time_start_millisecond) / 1000));
//		else
//			time_lapse_msg = String
//					.format("######### Time Lapse: %d second(s) #########",
//							(int) ((time_end_millisecond - time_start_millisecond) / 1000));
//		CmnDef.info(time_lapse_msg);
//		if (show_console)
//			System.out.println(time_lapse_msg);
//	}
//
//	private static void check_exist_operation() {
//		if (show_console)
//			System.out.println("Check MySQL exist......");
//
//		ArrayList<String> not_exist_list = new ArrayList<String>();
//		short ret = CmnDef.RET_SUCCESS;
//		ret = FinanceRecorder.check_sql_exist(not_exist_list);
//		if (CmnDef.CheckFailure(ret)) {
//			show_error_and_exit(String.format(
//					"Fail to check the MySQL exist, due to: %s",
//					CmnDef.GetErrorDescription(ret)));
//		} else {
//			if (!not_exist_list.isEmpty()) {
//				String not_found_size_string = String.format(
//						"There are totally %d tables missing",
//						not_exist_list.size());
//				CmnDef.warn(not_found_size_string);
//				if (show_console)
//					System.out.println(not_found_size_string);
//				for (String not_found : not_exist_list) {
//					CmnDef.warn(not_found);
//					if (show_console)
//						System.out.println(not_found);
//				}
//			} else {
//				String no_not_found_string = "There are NO tables missing";
//				CmnDef.info(no_not_found_string);
//				if (show_console)
//					System.out.println(no_not_found_string);
//			}
//		}
//	}

	public static void main(String args[]) 
	{
		// if (CmnDef.CheckFailure(init_param()))
		// show_error_and_exit("Fail to initialize the parameters ......");
// Early-parse the parameters
		if (FinanceRecorderCmnDef.CheckFailure(parse_param(args, true)))
			show_error_and_exit("Fail to early parse the parameters ......");
		determine_finance_mode();
// Parse the parameters
		if (FinanceRecorderCmnDef.CheckFailure(parse_param(args, false)))
			show_error_and_exit("Fail to parse the parameters ......");
// Check the parameters
		if (FinanceRecorderCmnDef.CheckFailure(check_param()))
			show_error_and_exit("Fail to check the parameters ......");
// Setup the parameters
		if (FinanceRecorderCmnDef.CheckFailure(setup_param()))
			show_error_and_exit("Fail to setup the parameters ......");
// Start to work.......
//		if (is_check_exist_operation_enabled())
//			check_exist_operation();
		if (is_cleanup_operation_enabled())
			cleanup_operation();
//		else if (is_delete_operation_enabled())
//			delete_operation();
		if (is_write_operation_enabled())
			write_operation();
//		else if (is_restore_operation_enabled())
//			restore_operation();
//		if (is_backup_operation_enabled())
//			backup_operation();

//		wait_to_exit();
	}
}
