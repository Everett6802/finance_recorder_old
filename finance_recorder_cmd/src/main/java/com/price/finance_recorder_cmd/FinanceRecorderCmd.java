package com.price.finance_recorder_cmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.price.finance_recorder_lib.FinanceRecorder;
import com.price.finance_recorder_lib.FinanceRecorderCmnDef;
import com.price.finance_recorder_lib.FinanceRecorderCmnDef.DefaultConstType;
import com.price.finance_recorder_lib.FinanceRecorderCmnDef.DeleteSQLAccurancyType;
import com.price.finance_recorder_lib.FinanceRecorderCmnDef.FinanceAnalysisMode;


public class FinanceRecorderCmd
{
	// private static final String PARAM_SPLIT = ",";
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
	// private static String statement_method_param = null;
	private static String finance_folderpath_param = null;
	private static String finance_backup_folderpath_param = null;
	private static String finance_restore_folderpath_param = null;
	// private static String finance_backup_foldername_param = null;
	// private static String finance_restore_foldername_param = null;
	// private static boolean show_finance_backup_list_param = false;
	// private static boolean show_finance_restore_list_param = false;
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
		return FinanceRecorder.const_info(default_const_type);
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
		if (time_lapse_millisecond >= (100 * 1000))
			time_lapse_msg = String.format("######### Time Lapse: %d second(s) #########",
					(int) ((time_end_millisecond - time_start_millisecond) / 1000));
		else if (time_lapse_millisecond >= (10 * 1000))
			time_lapse_msg = String.format("######### Time Lapse: %d second(s) #########",
					(int) ((time_end_millisecond - time_start_millisecond) / 1000));
		else
			time_lapse_msg = String.format("######### Time Lapse: %d second(s) #########",
					(int) ((time_end_millisecond - time_start_millisecond) / 1000));
		return time_lapse_msg;
	}
	
	private static void show_usage_and_exit()
	{
		try
		{
			BufferedReader br = new BufferedReader(
					new InputStreamReader(FinanceRecorder.class.getClassLoader().getResourceAsStream("help.txt")));
			String line = null;
			while ((line = br.readLine()) != null)
			{
				PRINT_STDOUT(line);
				PRINT_STDOUT("\n");
			}
		} catch (IOException e)
		{
			String err = String.format("Error occur while reading the data, due to: %s", e.toString());
			PRINT_STDERR(err);
		}
		
		System.exit(0);
	}
	
	private static void determine_finance_mode()
	{
		// Determine the mode and initialize the manager class
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		if (market_mode_param && stock_mode_param)
		{
			show_error_and_exit("Fail to determine the finance mode !!! Both Market/Stock mode are set");
		} else if (market_mode_param)
		{
			ret = FinanceRecorder.set_market_mode();
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				show_error_and_exit(String.format("Fail to set the MARKET mode !!! Due to: %s",
						FinanceRecorderCmnDef.GetErrorDescription(ret)));
		} else if (stock_mode_param)
		{
			ret = FinanceRecorder.set_stock_mode();
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				show_error_and_exit(String.format("Fail to set the STOCK mode !!! Due to: %s",
						FinanceRecorderCmnDef.GetErrorDescription(ret)));
		} else
		{
			ret = FinanceRecorder.set_mode_from_cfg();
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				show_error_and_exit(String.format("Fail to set the finance mode from config file !!! Due to: %s",
						FinanceRecorderCmnDef.GetErrorDescription(ret)));
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
			} else if (option.equals("--stock_mode")) // Need to be parsed first
			{
				if (early_parse_param)
				{
					stock_mode_param = true;
				}
				index_offset = 1;
			} else if (option.equals("--silent"))
			{
				if (early_parse_param)
				{
					show_console = false;
				}
				index_offset = 1;
			} else if (option.equals("-h") || option.equals("--help"))
			{
				if (!early_parse_param)
				{
					help_param = true;
				}
				index_offset = 1;
			} else if (option.equals("--company_profile_filepath"))
			{
				if (!early_parse_param)
				{
					company_profile_filepath_param = args[index + 1];
				}
				index_offset = 2;
			} else if (option.equals("--compare_company"))
			{
				if (!early_parse_param)
				{
					compare_company_param = true;
				}
				index_offset = 1;
			} else if (option.equals("--renew_company"))
			{
				if (!early_parse_param)
				{
					renew_company_param = true;
				}
				index_offset = 1;
			} else if (option.equals("--statement_profile_filepath"))
			{
				if (!early_parse_param)
				{
					statement_profile_filepath_param = args[index + 1];
				}
				index_offset = 2;
			} else if (option.equals("--compare_statement"))
			{
				if (!early_parse_param)
				{
					compare_statement_param = true;
				}
				index_offset = 1;
			} else if (option.equals("--renew_statement"))
			{
				if (!early_parse_param)
				{
					renew_statement_param = true;
				}
				index_offset = 1;
			} else if (option.equals("--finance_folderpath"))
			{
				if (!early_parse_param)
				{
					if ((index + 1) >= args_len)
						show_error_and_exit(String.format("The option[%s] does NOT contain value", option));
					finance_folderpath_param = args[index + 1];
				}
				index_offset = 2;
			} else if (option.equals("--finance_backup_folderpath"))
			{
				if (!early_parse_param)
				{
					if ((index + 1) >= args_len)
						show_error_and_exit(String.format("The option[%s] does NOT contain value", option));
					finance_backup_folderpath_param = args[index + 1];
				}
				index_offset = 2;
			} else if (option.equals("--finance_restore_folderpath"))
			{
				if (!early_parse_param)
				{
					if ((index + 1) >= args_len)
						show_error_and_exit(String.format("The option[%s] does NOT contain value", option));
					finance_restore_folderpath_param = args[index + 1];
				}
				index_offset = 2;
			}
			// else if (option.equals("--finance_backup_foldername"))
			// {
			// if (!early_parse_param)
			// {
			// if (index + 1 >= args_len)
			// show_error_and_exit(String.format("The option[%s] does NOT
			// contain value", option));
			// finance_backup_foldername_param = args[index + 1];
			// }
			// index_offset = 2;
			// }
			// else if (option.equals("--finance_restore_foldername"))
			// {
			// if (!early_parse_param)
			// {
			// if (index + 1 >= args_len)
			// show_error_and_exit(String.format("The option[%s] does NOT
			// contain value", option));
			// finance_restore_foldername_param = args[index + 1];
			// }
			// index_offset = 2;
			// }
			else if (option.equals("--delete_sql_accurancy"))
			{
				if (!early_parse_param)
				{
					if ((index + 1) >= args_len)
						show_error_and_exit(String.format("The option[%s] does NOT contain value", option));
					if (is_market_mode())
						PRINT_STDOUT("WARNING: The delete_sql_accurancy arguemnt is ignored in the Market mode");
					else
						delete_sql_accurancy_param = args[index + 1];
				}
				index_offset = 2;
			} else if (option.equals("--multi_thread"))
			{
				if (!early_parse_param)
				{
					if ((index + 1) >= args_len)
						show_error_and_exit(String.format("The option[%s] does NOT contain value", option));
					multi_thread_param = args[index + 1];
				}
				index_offset = 2;
			}
			// else if (option.equals("--show_finance_backup_list"))
			// {
			// if (!early_parse_param)
			// {
			// show_finance_backup_list_param = true;
			// }
			// index_offset = 1;
			// }
			// else if (option.equals("--show_finance_restore_list"))
			// {
			// if (!early_parse_param)
			// {
			// show_finance_restore_list_param = true;
			// }
			// index_offset = 1;
			// }
			else if (option.equals("--set_operation_non_stop"))
			{
				if (!early_parse_param)
				{
					if ((index + 1) >= args_len)
						show_error_and_exit(String.format("The option[%s] does NOT contain value", option));
					set_operation_non_stop_param = args[index + 1];
				}
				index_offset = 2;
			} else if (option.equals("-o") || option.equals("--operation"))
			{
				if (!early_parse_param)
				{
					if ((index + 1) >= args_len)
						show_error_and_exit(String.format("The option[%s] does NOT contain value", option));
					database_operation_param = args[index + 1];
				}
				index_offset = 2;
			} else if (option.equals("--method_from_file"))
			{
				if (!early_parse_param)
				{
					if ((index + 1) >= args_len)
						show_error_and_exit(String.format("The option[%s] does NOT contain value", option));
					method_from_file_param = args[index + 1];
				}
				index_offset = 2;
			} else if (option.equals("-m") || option.equals("--method"))
			{
				if (!early_parse_param)
				{
					if ((index + 1) >= args_len)
						show_error_and_exit(String.format("The option[%s] does NOT contain value", option));
					method_param = args[index + 1];
				}
				index_offset = 2;
			} else if (option.equals("-t") || option.equals("--time_range"))
			{
				if (!early_parse_param)
				{
					if ((index + 1) >= args_len)
						show_error_and_exit(String.format("The option[%s] does NOT contain value", option));
					time_range_param = args[index + 1];
				}
				index_offset = 2;
			} else if (option.equals("--company_from_file"))
			{
				if (!early_parse_param)
				{
					if ((index + 1) >= args_len)
						show_error_and_exit(String.format("The option[%s] does NOT contain value", option));
					if (is_market_mode())
						PRINT_STDOUT("WARNING: The company_from_file arguemnt is ignored in the Market mode");
					else
						company_from_file_param = args[index + 1];
				}
				index_offset = 2;
			} else if (option.equals("-c") || option.equals("--company"))
			{
				if (!early_parse_param)
				{
					if ((index + 1) >= args_len)
						show_error_and_exit(String.format("The option[%s] does NOT contain value", option));
					if (is_market_mode())
						PRINT_STDOUT("WARNING: The company_from_file arguemnt is ignored in the Market mode");
					else
						company_param = args[index + 1];
				}
				index_offset = 2;
			} else
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
			if ((database_operation_param.indexOf('W') != -1) || (database_operation_param.indexOf('w') != -1))
				database_operation |= DATABASE_OPERATION_WRITE_MASK;
			if ((database_operation_param.indexOf('B') != -1) || (database_operation_param.indexOf('b') != -1))
				database_operation |= DATABASE_OPERATION_BACKUP_MASK;
			if ((database_operation_param.indexOf('M') != -1) || (database_operation_param.indexOf('m') != -1))
				database_operation |= DATABASE_OPERATION_DELETE_MASK;
			if ((database_operation_param.indexOf('C') != -1) || (database_operation_param.indexOf('c') != -1))
				database_operation |= DATABASE_OPERATION_CLEANUP_MASK;
			if ((database_operation_param.indexOf('R') != -1) || (database_operation_param.indexOf('r') != -1))
				database_operation |= DATABASE_OPERATION_RESTORE_MASK;
			if ((database_operation_param.indexOf('E') != -1) || (database_operation_param.indexOf('e') != -1))
				database_operation |= DATABASE_OPERATION_CHECK_EXIST_MASK;
			if (((database_operation & DATABASE_OPERATION_WRITE_MASK) != 0)
					&& ((database_operation & DATABASE_OPERATION_RESTORE_MASK) != 0))
			{
				PRINT_STDOUT(
						"WARNING: The 'write' and 'resotre' operation can NOT be enabled simultaneously, ignore the 'restore' operation");
				database_operation &= ~DATABASE_OPERATION_RESTORE_MASK;
			}
			if (((database_operation & DATABASE_OPERATION_RESTORE_MASK) != 0)
					&& ((database_operation & DATABASE_OPERATION_CLEANUP_MASK) == 0))
			{
				PRINT_STDOUT("WARNING: The 'cleanup' operation should be enabled, when the 'restore' operation is set");
				database_operation |= DATABASE_OPERATION_CLEANUP_MASK;
			}
			if (((database_operation & DATABASE_OPERATION_DELETE_MASK) != 0)
					&& ((database_operation & DATABASE_OPERATION_CLEANUP_MASK) != 0))
			{
				PRINT_STDOUT("WARNING: The 'delete' operation is ignored since the 'cleanup' operation is set");
				database_operation &= ~DATABASE_OPERATION_DELETE_MASK;
			}
		}
		if (set_operation_non_stop_param != null)
		{
			if ((set_operation_non_stop_param.equals("TRUE") | set_operation_non_stop_param.equals("True"))
					|| set_operation_non_stop_param.equals("true"))
			{
				operation_non_stop = true;
			} else if (set_operation_non_stop_param.equals("FALSE") || set_operation_non_stop_param.equals("False")
					|| set_operation_non_stop_param.equals("false"))
			{
				operation_non_stop = false;
			} else
			{
				show_error_and_exit(String.format("Unknown value of the set_operation_non_stop attribute: %d",
						set_operation_non_stop_param));
			}
		}
		if (finance_backup_folderpath_param != null)
		{
			if (!is_backup_operation_enabled())
			{
				finance_backup_folderpath_param = null;
				PRINT_STDOUT(
						"WARNING: The 'finance_backup_folderpath_param' argument is ignored since Backup operation is NOT set");
			}
		}
		if (finance_restore_folderpath_param != null)
		{
			if (!is_restore_operation_enabled())
			{
				finance_restore_folderpath_param = null;
				PRINT_STDOUT(
						"WARNING: The 'finance_restore_folderpath_param' argument is ignored since Restore operation is NOT set");
			}
		}
		// if (finance_backup_foldername_param != null)
		// {
		// if (!is_backup_operation_enabled())
		// {
		// finance_backup_foldername_param = null;
		// PRINT_STDOUT("WARNING: The 'finance_backup_foldername_param' argument
		// is ignored since Backup operation is NOT set");
		// }
		// }
		// if (finance_restore_foldername_param != null)
		// {
		// if (!is_restore_operation_enabled())
		// {
		// if (finance_restore_foldername_param != null)
		// {
		// finance_restore_foldername_param = null;
		// PRINT_STDOUT("WARNING: The 'finance_restore_foldername_param'
		// argument is ignored since Restore operation is NOT set");
		// }
		// }
		// }
		if (delete_sql_accurancy_param != null)
		{
			if (is_delete_operation_enabled())
			{
				try
				{
					delete_sql_accurancy_type = DeleteSQLAccurancyType
							.valueOf(Integer.valueOf(delete_sql_accurancy_param));
				} catch (Exception e)
				{
				}
				if (delete_sql_accurancy_type == null)
					throw new IllegalStateException(
							String.format("Unknown delete sql accurancy type: %s", delete_sql_accurancy_param));
			} else
			{
				delete_sql_accurancy_param = null;
				PRINT_STDOUT(
						"WARNING: The 'delete_sql_accurancy' argument is ignored since Delete operation is NOT set");
			}
		}
		
		if (multi_thread_param != null)
		{
			if (!is_write_operation_enabled())
			{
				multi_thread_param = null;
				PRINT_STDOUT("WARNING: The 'multi_thread_param' argument is ignored since Write operation is NOT set");
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
		} else
		{
			if (renew_company_param && !compare_company_param)
			{
				compare_company_param = true;
				PRINT_STDOUT("WARNING: The 'compare_company' argument is enabled since 'renew_company' is set");
			}
			if (!compare_company_param && is_write_operation_enabled())
			{
				PRINT_STDOUT(
						"WARNING: The 'compare_company' argument should be enabled when the 'write' operation is set");
				compare_company_param = true;
			}
			if (compare_company_param)
			{
				// if (company_profile_filepath_param == null)
				// company_profile_filepath_param =
				// FinanceRecorderCmnDef.DEFAULT_COMPANY_PROFILE_CONF_FOLDERPATH;
			} else
			{
				if (company_profile_filepath_param != null)
				{
					company_profile_filepath_param = null;
					PRINT_STDOUT(
							"WARNING: The 'company_profile_filepath' argument is ignored since 'renew_company' is NOT set");
				}
			}
			if (renew_statement_param && !compare_statement_param)
			{
				compare_statement_param = true;
				PRINT_STDOUT("WARNING: The 'compare_statement' argument is enabled since 'renew_statement' is set");
			}
			if (!compare_statement_param && is_write_operation_enabled())
			{
				PRINT_STDOUT(
						"WARNING: The 'compare_statement' argument should be enabled when the 'write' operation is set");
				compare_statement_param = true;
			}
			if (compare_statement_param)
			{
				// if (statement_profile_filepath_param == null)
				// statement_profile_filepath_param =
				// FinanceRecorderCmnDef.DEFAULT_STATEMENT_PROFILE_CONF_FOLDERPATH;
			} else
			{
				if (statement_profile_filepath_param != null)
				{
					statement_profile_filepath_param = null;
					PRINT_STDOUT(
							"WARNING: The 'statement_profile_filepath' argument is ignored since 'compare_statement_param' is NOT set");
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
		// String errmsg = null;
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
			// if (finance_backup_foldername_param != null)
			// FinanceRecorder.set_finance_backup_foldername(finance_backup_foldername_param);
		}
		// Setup the finance restore root folder path
		if (is_restore_operation_enabled())
		{
			if (finance_restore_folderpath_param != null)
				FinanceRecorder.set_finance_restore_folderpath(finance_restore_folderpath_param);
			// if (finance_restore_foldername_param != null)
			// FinanceRecorder.set_finance_restore_foldername(finance_restore_foldername_param);
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
				show_error_and_exit(String.format("Fail to set method, due to: %s",
						FinanceRecorderCmnDef.GetErrorDescription(ret)));
			// Set the time range
			if (time_range_param != null)
			{
				ret = FinanceRecorder.set_time_range(time_range_param);
				if (FinanceRecorderCmnDef.CheckFailure(ret))
					show_error_and_exit(String.format("Fail to set time range, due to: %s",
							FinanceRecorderCmnDef.GetErrorDescription(ret)));
			}
		}
		// Set company list. For stock mode only
		if (is_stock_mode())
		{
			if (company_from_file_param != null)
			{
				ret = FinanceRecorder.set_company_from_file(company_from_file_param);
				if (FinanceRecorderCmnDef.CheckFailure(ret))
					show_error_and_exit(String.format("Fail to set company from file, due to: %s",
							FinanceRecorderCmnDef.GetErrorDescription(ret)));
			} else if (company_param != null)
			{
				ret = FinanceRecorder.set_company(company_param);
				if (FinanceRecorderCmnDef.CheckFailure(ret))
					show_error_and_exit(String.format("Fail to set company, due to: %s",
							FinanceRecorderCmnDef.GetErrorDescription(ret)));
			}
		}
		return ret;
	}
	
	private static void write_operation()
	{
		PRINT_STDOUT(String.format("Write CSV[%s] data into SQL......\n", FinanceRecorder.get_finance_folderpath()));
		// FinanceRecorder.switch_current_csv_working_folerpath(CmnDef.CSVWorkingFolderType.CSVWorkingFolder_Write);
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		long time_start_millisecond = System.currentTimeMillis();
		if (multi_thread_param != null)
			ret = FinanceRecorder.operation_write_multithread(Integer.valueOf(multi_thread_param));
		else
			ret = FinanceRecorder.operation_write();
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			show_error_and_exit(String.format("Fail to write CSV data into MySQL, due to: %s",
					FinanceRecorderCmnDef.GetErrorDescription(ret)));
		long time_end_millisecond = System.currentTimeMillis();
		PRINT_STDOUT("Write CSV data to SQL...... Done\n");
		// Calculate the time elapse
		PRINT_STDOUT(get_time_elapse_string(time_start_millisecond, time_end_millisecond));
		PRINT_STDOUT("\n");
	}
	
	private static void backup_operation()
	{
		PRINT_STDOUT(
				String.format("Backup CSV[%s] data from SQL......\n", FinanceRecorder.get_finance_backup_folderpath()));
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		long time_start_millisecond = System.currentTimeMillis();
		ret = FinanceRecorder.operation_backup();
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			show_error_and_exit(String.format("Fail to backup CSV data from MySQL, due to: %s",
					FinanceRecorderCmnDef.GetErrorDescription(ret)));
		long time_end_millisecond = System.currentTimeMillis();
		PRINT_STDOUT("Backup CSV data from SQL...... Done\n");
		// Calculate the time elapse
		PRINT_STDOUT(get_time_elapse_string(time_start_millisecond, time_end_millisecond));
		PRINT_STDOUT("\n");
	}
	
	private static void cleanup_operation()
	{
		PRINT_STDOUT("Cleanup old MySQL data......\n");
		
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		ret = FinanceRecorder.operation_cleanup();
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			show_error_and_exit(String.format("Fail to cleanup the old MySQL, due to: %s",
					FinanceRecorderCmnDef.GetErrorDescription(ret)));
	}
	
	private static void delete_operation()
	{
		PRINT_STDOUT("Delete old MySQL data......\n");
		
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		ret = FinanceRecorder.operation_delete();
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			show_error_and_exit(String.format("Fail to cleanup the old MySQL, due to: %s",
					FinanceRecorderCmnDef.GetErrorDescription(ret)));
	}
	
	private static void restore_operation()
	{
		PRINT_STDOUT(String.format("Restore SQL data from CSV[%s]......\n",
				FinanceRecorder.get_finance_restore_folderpath()));
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		long time_start_millisecond = System.currentTimeMillis();
		// Cleanup the old database
		ret = FinanceRecorder.operation_cleanup();
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			show_error_and_exit(String.format("Fail to cleanup old SQL data, due to: %s",
					FinanceRecorderCmnDef.GetErrorDescription(ret)));
		ret = FinanceRecorder.operation_restore();
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			show_error_and_exit(String.format("Fail to restore SQL data from CSV, due to: %s",
					FinanceRecorderCmnDef.GetErrorDescription(ret)));
		long time_end_millisecond = System.currentTimeMillis();
		PRINT_STDOUT("Restore SQL data from CSV...... Done\n");
		// Calculate the time elapse
		PRINT_STDOUT(get_time_elapse_string(time_start_millisecond, time_end_millisecond));
		PRINT_STDOUT("\n");
	}
	
	private static void check_exist_operation()
	{
		PRINT_STDOUT(String.format("Check MySQL exist......\n"));
		
		ArrayList<String> not_exist_list = new ArrayList<String>();
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		ret = FinanceRecorder.operation_check_exist(not_exist_list);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			show_error_and_exit(String.format("Fail to check the MySQL exist, due to: %s",
					FinanceRecorderCmnDef.GetErrorDescription(ret)));
		else
		{
			if (!not_exist_list.isEmpty())
			{
				PRINT_STDOUT(String.format("=== There are totally %d tables missing ===\n", not_exist_list.size()));
				for (String not_found : not_exist_list)
					PRINT_STDOUT(String.format("%s\n", not_found));
			} else
			{
				PRINT_STDOUT("There are NO tables missing");
			}
		}
	}
	
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
		// FinanceRecorder.test();
		// System.exit(0);
		// Execute some parameters before checking and exit
		// Run the parameters and then exit......
		if (help_param)
			show_usage_and_exit();
		// Check the parameters
		if (FinanceRecorderCmnDef.CheckFailure(check_param()))
			show_error_and_exit("Fail to check the parameters ......");
		// Setup the parameters
		if (FinanceRecorderCmnDef.CheckFailure(setup_param()))
			show_error_and_exit("Fail to setup the parameters ......");
		
		// Start to work.......
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
		
		// wait_to_exit();
	}
}
