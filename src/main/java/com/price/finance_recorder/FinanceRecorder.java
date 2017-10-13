package com.price.finance_recorder;

//import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;


public class FinanceRecorder extends ClassCmnBase
{
	private static final String PARAM_SPLIT = ",";
	private static final String[] DEFAULT_CONST_ARRAY = new String[]
	{
		CmnDef.MARKET_STOCK_SWITCH_CONF_FILENAME,
		CmnDef.CSV_ROOT_FOLDERPATH,
		CmnDef.CSV_BACKUP_FOLDERPATH,
		CmnDef.CSV_RESTORE_FOLDERPATH,
		CmnDef.MARKET_ALL_CONFIG_FILENAME,
		CmnDef.STOCK_ALL_CONFIG_FILENAME,
		CmnDef.DEFAULT_COMPANY_PROFILE_CONF_FOLDERPATH,
		CmnDef.DEFAULT_STATEMENT_PROFILE_CONF_FOLDERPATH
	};

	private static CmnInf.MgrInf finance_recorder_mgr = null;
	private static CmnDef.FinanceAnalysisMode finance_analysis_mode = CmnDef.FinanceAnalysisMode.FinanceAnalysis_None;
	private static CmnClass.FinanceTimeRange finance_time_range = null;

	private static short init_finance_manager(CmnDef.FinanceAnalysisMode new_finance_analysis_mode)
	{
		if (new_finance_analysis_mode == CmnDef.FinanceAnalysisMode.FinanceAnalysis_None)
			new_finance_analysis_mode = CmnFunc.get_finance_analysis_mode_from_cfg();
		if (finance_analysis_mode == new_finance_analysis_mode)
		{
			CmnLogger.format_debug("No need to switch to the finance mode: %d", finance_analysis_mode);
			return CmnDef.RET_SUCCESS;
		}
		finance_recorder_mgr = null;
		finance_analysis_mode = new_finance_analysis_mode;
		CmnLogger.format_debug("Switch to the finance mode: %d", finance_analysis_mode);

// Create the instance of manager class
		if (finance_analysis_mode == CmnDef.FinanceAnalysisMode.FinanceAnalysis_Market)
		{
			finance_recorder_mgr = new MarketMgr();
		}
		else if (finance_analysis_mode == CmnDef.FinanceAnalysisMode.FinanceAnalysis_Stock)
		{
			finance_recorder_mgr = new StockMgr();
		}
		else
			throw new IllegalStateException(String.format("Unsupported finance mode: %d", finance_analysis_mode));
		CmnDef.FINANCE_MODE = finance_analysis_mode;
		CmnDef.IS_FINANCE_MARKET_MODE = (CmnDef.FINANCE_MODE == CmnDef.FinanceAnalysisMode.FinanceAnalysis_Market);
		CmnDef.IS_FINANCE_STOCK_MODE = (CmnDef.FINANCE_MODE == CmnDef.FinanceAnalysisMode.FinanceAnalysis_Stock);
		short ret = finance_recorder_mgr.initialize();
		if (CmnDef.CheckFailure(ret))
			return ret;
		return CmnDef.RET_SUCCESS;
	}

	private static short parse_method_index_from_param(LinkedList<Integer> method_index_list, String method_word_list_string) 
	{
		String[] method_word_array = method_word_list_string.split(PARAM_SPLIT);
		for (String method_index_str : method_word_array) 
		{
			Matcher matcher = CmnFunc.get_regex_matcher("([\\d]+)-([\\d]+)", method_index_str);
			if (matcher != null) 
			{
				int method_start_index = Integer.valueOf(matcher.group(1));
				int method_end_index = Integer.valueOf(matcher.group(2));
				for (int method_index = method_start_index; method_index <= method_end_index; method_index++)
					method_index_list.add(method_index);
			} 
			else 
			{
				if (CmnFunc.get_regex_matcher("([\\d]+)", method_index_str) == null)
				{
					CmnLogger.format_error("Incorrect method parameter: %s", method_index_str);
					return CmnDef.RET_FAILURE_INVALID_ARGUMENT;
				}
				int method_index = Integer.valueOf(method_index_str);
				method_index_list.add(method_index);
			}
		}
		return CmnDef.RET_SUCCESS;
	}

	private static short get_company_number_list_from_profile_config(LinkedList<String> company_number_list, final String config_folderpath) 
	{
		LinkedList<String> company_profile_list = new LinkedList<String>();
		short ret = CmnFunc.read_config_file_lines(CmnDef.COMPANY_PROFILE_CONF_FILENAME, config_folderpath, company_profile_list);
		if (CmnDef.CheckFailure(ret))
			return ret;
		for (String company_profile : company_profile_list) 
		{
			String[] company_profile_element_array = company_profile.split(CmnDef.COMMA_DATA_SPLIT);
			if (company_profile_element_array.length != StockMgr.COMPANY_PROFILE_ENTRY_FIELD_SIZE) 
			{
				CmnLogger.format_error("The Company Profile Entry Length should be %d, not: %d", StockMgr.COMPANY_PROFILE_ENTRY_FIELD_SIZE, company_profile_element_array.length);
				return CmnDef.RET_FAILURE_INCORRECT_CONFIG;
			}
			company_number_list.add(company_profile_element_array[StockMgr.COMPANY_PROFILE_ENTRY_FIELD_INDEX_COMPANY_CODE_NUMBER]);
		}
		return ret;
	}

	private static short compare_company_profile_change(final String new_company_profile_config_folderpath, ArrayList<String> lost_company_number_list, ArrayList<String> new_company_number_list) 
	{
		short ret = CmnDef.RET_SUCCESS;
		assert new_company_profile_config_folderpath != null : "company_profile_config_folderpath should NOT be NULL";
		StringBuilder timestamp_src_builder = new StringBuilder();
		StringBuilder timestamp_dst_builder = new StringBuilder();
		ret = CmnFunc.get_config_file_timestamp(timestamp_src_builder, CmnDef.COMPANY_PROFILE_CONF_FILENAME, new_company_profile_config_folderpath);
		if (CmnDef.CheckFailure(ret)) 
		{
			CmnLogger.format_error("Fails to get time stamp from source file[%s], due to: %s", CmnDef.COMPANY_PROFILE_CONF_FILENAME, CmnDef.GetErrorDescription(ret));
			return ret;
		}
		boolean need_renew = false;
		ret = CmnFunc.get_config_file_timestamp(timestamp_dst_builder, CmnDef.COMPANY_PROFILE_CONF_FILENAME, null);
		if (CmnDef.CheckFailure(ret)) 
		{
			if (ret == CmnDef.RET_FAILURE_NOT_FOUND) 
			{
				CmnLogger.format_warn("The company profile file[%s] does NOT exist", CmnDef.COMPANY_PROFILE_CONF_FILENAME);
				need_renew = true;
			} 
			else if (ret == CmnDef.RET_FAILURE_INCORRECT_CONFIG) 
			{
				CmnLogger.format_warn("Fails to get time stamp from destination file[%s]", CmnDef.COMPANY_PROFILE_CONF_FILENAME);
				need_renew = true;
			} 
			else 
			{
				CmnLogger.format_error("Error occurs while trying to get time stamp from the source company profile file[%s], due to: %s", CmnDef.COMPANY_PROFILE_CONF_FILENAME, CmnDef.GetErrorDescription(ret));
				return ret;
			}
		}
// Check if the time stamps are identical
		if (!need_renew)
			need_renew = (timestamp_src_builder.toString().equals(timestamp_dst_builder.toString()) ? false : true);
		if (need_renew)
		{
// Show time stamp
//			System.out.printf("The time stamp in company profile: %s -> %s\n", timestamp_src_builder.toString(), timestamp_dst_builder.toString());
			LinkedList<String> src_company_number_list = new LinkedList<String>();
			LinkedList<String> dst_company_number_list = new LinkedList<String>();
			ret = get_company_number_list_from_profile_config(src_company_number_list, new_company_profile_config_folderpath);
			if (CmnDef.CheckFailure(ret)) 
			{
				CmnLogger.format_error("Fails to get the company number from the source config file[%s] from: %s, due to: %s", CmnDef.COMPANY_PROFILE_CONF_FILENAME, new_company_profile_config_folderpath, CmnDef.GetErrorDescription(ret));
				return ret;
			}
			ret = get_company_number_list_from_profile_config(dst_company_number_list, null);
			if (CmnDef.CheckFailure(ret)) 
			{
				CmnLogger.format_error("Fails to get the company number from the destination config file[%s/%s] from: %s, due to: %s", CmnDef.COMPANY_PROFILE_CONF_FILENAME, CmnFunc.get_current_path(), CmnDef.CONF_FOLDERNAME, CmnDef.GetErrorDescription(ret));
				return ret;
			}
// Company company list between old and new company profile
			for (String src_company_number : src_company_number_list)
			{
				if (dst_company_number_list.indexOf(src_company_number) == -1)
					lost_company_number_list.add(src_company_number);
			}
			for (String dst_company_number : dst_company_number_list) 
			{
				if (src_company_number_list.indexOf(dst_company_number) == -1)
					new_company_number_list.add(dst_company_number);
			}
		}
		return ret;
	}

	private static short get_statement_field_list_from_profile_config(LinkedList<String> statement_field_list, final String config_filename, final String config_folderpath) 
	{
		LinkedList<String> statement_profile_list = new LinkedList<String>();
		short ret = CmnFunc.read_config_file_lines(config_filename, config_folderpath, statement_profile_list);
		if (CmnDef.CheckFailure(ret))
			return ret;
		for (String statement_profile : statement_profile_list) 
		{
			String[] statement_profile_element_array = statement_profile.split(CmnDef.COLON_DATA_SPLIT);
			if (statement_profile_element_array.length != 1) 
			{
				if (statement_profile.indexOf(CmnDef.ALIAS_DATA_SPLIT) != -1)
					continue;
			}
			statement_field_list.add(statement_profile_element_array[0]);
		}
		return ret;
	}

	private static short compare_statement_profile_change(final String new_statement_profile_config_folderpath, List<Integer> statement_method_index_list, HashMap<Integer, ArrayList<String>> lost_statement_field_map, HashMap<Integer, ArrayList<String>> new_statement_field_map) 
	{
		short ret = CmnDef.RET_SUCCESS;
		assert new_statement_profile_config_folderpath != null : "new_statement_profile_config_folderpath should NOT be NULL";
		// Compare the statement field
		for (Integer statement_method_index : statement_method_index_list) 
		{
			String statement_config_filename = CmnDef.STATEMENT_FIELD_NAME_CONF_FILENAME_ARRAY[statement_method_index - CmnDef.FINANCE_METHOD_STOCK_STATMENT_START];
			LinkedList<String> src_statement_field_list = new LinkedList<String>();
			LinkedList<String> dst_statement_field_list = new LinkedList<String>();
			ret = get_statement_field_list_from_profile_config(src_statement_field_list, statement_config_filename, new_statement_profile_config_folderpath);
			if (CmnDef.CheckFailure(ret)) 
			{
				CmnLogger.format_error("Fails to get the statement field from the source config file[%s] from: %s, due to: %s", statement_config_filename, new_statement_profile_config_folderpath, CmnDef.GetErrorDescription(ret));
				return ret;
			}
			ret = get_statement_field_list_from_profile_config(dst_statement_field_list, statement_config_filename, null);
			if (CmnDef.CheckFailure(ret)) 
			{
				CmnLogger.format_error("Fails to get the statement_field from the destination config file[%s/%s] from: %s, due to: %s", statement_config_filename, CmnFunc.get_current_path(), CmnDef.CONF_FOLDERNAME, CmnDef.GetErrorDescription(ret));
				return ret;
			}
			ArrayList<String> lost_statement_field_list = new ArrayList<String>();
			for (String src_statement_field : src_statement_field_list) 
			{
				if (dst_statement_field_list.indexOf(src_statement_field) == -1)
					lost_statement_field_list.add(src_statement_field);
			}
			ArrayList<String> new_statement_field_list = new ArrayList<String>();
			for (String dst_statement_field : dst_statement_field_list) 
			{
				if (src_statement_field_list.indexOf(dst_statement_field) == -1)
					new_statement_field_list.add(dst_statement_field);
			}
			// Check if the statement change occurs
			if (!lost_statement_field_list.isEmpty()) 
				lost_statement_field_map.put(statement_method_index, lost_statement_field_list);
			if (!new_statement_field_list.isEmpty())
				new_statement_field_map.put(statement_method_index, new_statement_field_list);
		}
		return ret;
	}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
// APIs
	public static short set_market_mode(){return init_finance_manager(CmnDef.FinanceAnalysisMode.FinanceAnalysis_Market);}
	public static short set_stock_mode(){return init_finance_manager(CmnDef.FinanceAnalysisMode.FinanceAnalysis_Stock);}
	public static short set_mode_from_cfg()
	{
		return init_finance_manager(CmnDef.FinanceAnalysisMode.FinanceAnalysis_None);
//		CmnDef.FinanceAnalysisMode finance_analysis_mode_from_cfg = null;
//		try
//		{
//			finance_analysis_mode_from_cfg = CmnFunc.get_finance_analysis_mode_from_cfg();
//		}
//		catch(RuntimeException e)
//		{
//			CmnLogger.error("Fail to find the finance mode from cfg");
//			return CmnDef.RET_FAILURE_INCORRECT_CONFIG;
//		}
//		short ret = CmnDef.RET_SUCCESS;
//		switch (finance_analysis_mode_from_cfg)
//		{
//		case FinanceAnalysis_Market:
//			ret = set_market_mode();
//			break;
//		case FinanceAnalysis_Stock:
//			ret = set_stock_mode();
//			break;
//		default:
//			CmnLogger.error(String.format("Unknown finance mode: %d", finance_analysis_mode));
//			ret = CmnDef.RET_FAILURE_UNEXPECTED_VALUE;
//		}
//		return ret;
	}
	public static FinanceRecorderCmnDef.FinanceAnalysisMode get_finance_mode(){return finance_analysis_mode;}

	public static String const_info(FinanceRecorderCmnDef.DefaultConstType default_const_type)
	{
		return DEFAULT_CONST_ARRAY[default_const_type.value()];
	}

	public static void get_all_method_index_list(List<Integer> method_index_list)
	{
		LinkedList<Integer> all_method_index_list = CmnFunc.get_all_method_index_list();
		for (Integer method_index : all_method_index_list)
			method_index_list.add(method_index);
	}

	public static void get_all_method_description_list(List<String> method_description_list)
	{
		LinkedList<Integer> all_method_index_list = CmnFunc.get_all_method_index_list();
		for (Integer method_index : all_method_index_list)
			method_description_list.add(CmnDef.FINANCE_METHOD_DESCRIPTION_LIST[method_index]);
	}

	public static void set_finance_folderpath(String finance_folderpath)
	{	
		finance_recorder_mgr.set_finance_folderpath(finance_folderpath);
	}

	public static String get_finance_folderpath()
	{	
		return finance_recorder_mgr.get_finance_folderpath();
	}

	public static void set_finance_backup_folderpath(String finance_backup_folderpath)
	{
		finance_recorder_mgr.set_finance_backup_folderpath(finance_backup_folderpath);
	}

	public static void set_finance_restore_folderpath(String finance_restore_folderpath)
	{
		finance_recorder_mgr.set_finance_restore_folderpath(finance_restore_folderpath);
	}

	public static void set_finance_backup_foldername(String finance_backup_foldername)
	{
		finance_recorder_mgr.set_finance_backup_foldername(finance_backup_foldername);
	}

	public static void set_finance_restore_foldername(String finance_restore_foldername)
	{
		finance_recorder_mgr.set_finance_restore_foldername(finance_restore_foldername);
	}

	public static void set_delete_sql_accurancy(CmnDef.DeleteSQLAccurancyType delete_sql_accurancy_type)
	{
		finance_recorder_mgr.set_delete_sql_accuracy(delete_sql_accurancy_type);
	}

	public static short get_backup_foldername_list(List<String> backup_foldername_list)
	{
		return finance_recorder_mgr.get_backup_foldername_list(backup_foldername_list);
	}

	public static short get_restore_foldername_list(List<String> restore_foldername_list)
	{
		return finance_recorder_mgr.get_restore_foldername_list(restore_foldername_list);
	}

	public static void set_operation_non_stop(boolean operation_non_stop)
	{
		finance_recorder_mgr.set_operation_non_stop(operation_non_stop);
	}

	public static short check_company_profile_change(final String new_company_profile_config_folderpath, ArrayList<String> lost_company_number_list, ArrayList<String> new_company_number_list)
	{
		assert lost_company_number_list.isEmpty() : "lost_company_number_list is NOT empty";
		assert new_company_number_list.isEmpty() : "new_company_number_list is NOT empty";
		return compare_company_profile_change(new_company_profile_config_folderpath, lost_company_number_list, new_company_number_list);
	}

	public static short check_statement_profile_change(String statement_method_word_list_string, final String new_statement_profile_config_folderpath, HashMap<Integer, ArrayList<String>> lost_company_number_map, HashMap<Integer, ArrayList<String>> new_company_number_map)
	{
		assert lost_company_number_map.isEmpty() : "lost_company_number_map is NOT empty";
		assert new_company_number_map.isEmpty() : "new_company_number_map is NOT empty";
		LinkedList<Integer> statement_method_index_list = new LinkedList<Integer>(); 
		short ret = parse_method_index_from_param(statement_method_index_list, statement_method_word_list_string);
		if (CmnDef.CheckFailure(ret))
			return ret;
		return compare_statement_profile_change(new_statement_profile_config_folderpath, statement_method_index_list, lost_company_number_map, new_company_number_map);
	}

	public static short operation_write()
	{
		finance_recorder_mgr.switch_current_csv_working_folerpath(CmnDef.CSVWorkingFolderType.CSVWorkingFolder_Write);
		return finance_recorder_mgr.transfrom_csv_to_sql();
	}

	public static short operation_write_multithread(int thread_count)
	{
		finance_recorder_mgr.switch_current_csv_working_folerpath(CmnDef.CSVWorkingFolderType.CSVWorkingFolder_Write);
		return finance_recorder_mgr.transfrom_csv_to_sql_multithread(thread_count);
	}

	public static short operation_backup()
	{
		finance_recorder_mgr.switch_current_csv_working_folerpath(CmnDef.CSVWorkingFolderType.CSVWorkingFolder_Backup);
		return finance_recorder_mgr.transfrom_sql_to_csv(finance_time_range);
	}

	public static short operation_delete()
	{
		return finance_recorder_mgr.delete_sql();
	}

	public static short operation_cleanup()
	{
		return finance_recorder_mgr.cleanup_sql();
	}

	public static short operation_restore()
	{
		finance_recorder_mgr.switch_current_csv_working_folerpath(CmnDef.CSVWorkingFolderType.CSVWorkingFolder_Restore);
		return finance_recorder_mgr.transfrom_csv_to_sql();
	}

	public static short operation_restore_multithread(int thread_count)
	{
		finance_recorder_mgr.switch_current_csv_working_folerpath(CmnDef.CSVWorkingFolderType.CSVWorkingFolder_Restore);
		return finance_recorder_mgr.transfrom_csv_to_sql_multithread(thread_count);
	}

	public static short check_exist(ArrayList<String> not_exist_list)
	{
		return finance_recorder_mgr.check_sql_exist(not_exist_list);
	}

	public static short set_method_from_file(String filepath)
	{
		return finance_recorder_mgr.set_method_from_file(filepath);
	}

	public static short set_method(List<Integer> method_index_list)
	{
		return finance_recorder_mgr.set_method(method_index_list);
	}

	public static short set_method(String method_word_list_string)
	{
		LinkedList<Integer> method_index_list = new LinkedList<Integer>(); 
		short ret = parse_method_index_from_param(method_index_list, method_word_list_string);
		if (CmnDef.CheckFailure(ret))
			return ret;
		return set_method(method_index_list);
	}

	public static short set_time_range(String time_range_string)
	{
		String[] time_range_range_array = time_range_string.split(PARAM_SPLIT);
		String time_range_start = null;
		String time_range_end = null;
		if (time_range_range_array.length == 2)
		{
			time_range_start = time_range_range_array[0];
			time_range_end = time_range_range_array[1];
		} 
		else if (time_range_range_array.length == 1) 
		{
			if (time_range_string.startsWith(PARAM_SPLIT))
				time_range_end = time_range_range_array[0];
			else
				time_range_start = time_range_range_array[0];
		} 
		else
		{
			CmnLogger.error(String.format("Incorrect time range parameter format: %s", time_range_string));
			return CmnDef.RET_FAILURE_INVALID_ARGUMENT;
		}
		finance_time_range = new CmnClass.FinanceTimeRange(time_range_start, time_range_end);
		return CmnDef.RET_SUCCESS;
	}

	public static short set_company_from_file(String filepath)
	{
		return finance_recorder_mgr.set_company_from_file(filepath);
	}

	public static short set_company(LinkedList<String> company_word_list)
	{
		return finance_recorder_mgr.set_company(company_word_list);
	}

	public static short set_company(String company_word_list_string)
	{
		LinkedList<String> company_word_list = new LinkedList<String>();
		String[] company_word_array = company_word_list_string.split(PARAM_SPLIT);
		for (String company_number : company_word_array)
			company_word_list.add(company_number);
		return finance_recorder_mgr.set_company(company_word_list);
	}
}
