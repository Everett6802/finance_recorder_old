package com.price.finance_recorder_lib;

import java.util.ArrayList;
//import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


abstract class MgrBase implements CmnInf.MgrInf
{
	// private static WorkdayCalendar workday_calendar = null;//
	// WorkdayCalendar.get_instance();
	// private static DatabaseTimeRange database_time_range = null;
	// //DatabaseTimeRange.get_instance();
	// private static String generate_foldername_from_time()
	// {
	// Calendar cal = Calendar.getInstance();
	// cal.setTime(new java.util.Date());
	// return String.format("%02d%02d%02d%02d%02d", cal.get(Calendar.YEAR),
	// cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE),
	// cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE));
	// }
	public static short sort_sub_foldername_list(String root_folderpath, List<String> sorted_sub_foldername_list)
	{
		class FoldernameComparable implements Comparable<Object>
		{
			public String foldername;
			
			public FoldernameComparable(String name)
			{
				foldername = name;
			}
			
			@Override
			public int compareTo(Object other)
			{
				Long value = Long.parseLong(foldername);
				Long another_value = Long.parseLong(((FoldernameComparable) other).foldername);
				if (value > another_value)
					return 1;
				else if (value < another_value)
					return -1;
				else
					return 0;
			}
			
			@Override
			public String toString()
			{
				return foldername;
			}
		}
		;
		
		// String filepath = String.format("%s/%s", CmnDef.get_current_path(),
		// CmnDef.BACKUP_FOLDERNAME);
		ArrayList<String> sub_foldername_list = new ArrayList<String>();
		short ret = CmnFunc.get_subfolder_list(root_folderpath, sub_foldername_list);
		if (CmnDef.CheckFailure(ret))
		{
			if (CmnDef.CheckFailureNotFound(ret))
				return CmnDef.RET_SUCCESS;
			CmnLogger.format_error("Fail to get sub folder list from the folder: %s, due to: %s", root_folderpath,
					CmnDef.GetErrorDescription(ret));
			return ret;
		}
		List<FoldernameComparable> sorted_subfolder_list = new LinkedList<FoldernameComparable>();
		for (String sub_foldername : sub_foldername_list)
			sorted_subfolder_list.add(new FoldernameComparable(sub_foldername));
		// Sort the data by number
		Collections.sort(sorted_subfolder_list);
		for (FoldernameComparable sorted_subfolder : sorted_subfolder_list)
			sorted_sub_foldername_list.add(sorted_subfolder.toString());
		return CmnDef.RET_SUCCESS;
	}
	// public static short get_sorted_sub_foldername_list(String
	// time_folderpath)
	// {
	// int pos = time_folderpath.lastIndexOf("/");
	// if (pos == -1)
	// {
	// CmnLogger.format_error("Incorrect time folder path: %s",
	// time_folderpath);
	// return CmnDef.RET_FAILURE_INCORRECT_PATH;
	// }
	// String time_foldername = time_folderpath.substring(pos + 1);
	// if (CmnDef.get_regex_matcher("[\\d]{14}", time_foldername) == null)
	// {
	// CmnLogger.format_error("Incorrect time folder name: %s",
	// time_foldername);
	// return CmnDef.RET_FAILURE_INCORRECT_PATH;
	// }
	// String time_parent_folderpath = time_foldername.substring(0, pos);
	// return sort_sub_foldername_list(time_parent_folderpath,
	// sorted_sub_foldername_list);
	// }
	
	// private HashMap<Integer, CmnClass.TimeRangeCfg>
	// finance_source_time_range_table = null;
	// private HashMap<Integer, CmnClass.TimeRangeCfg>
	// finance_backup_source_time_range_table = null;
	// private HashMap<Integer, LinkedList<Integer>>
	// finance_backup_source_field_table = null;
	// private LinkedList<String> email_address_list = new LinkedList<String>();
	protected LinkedList<Integer> method_index_list = null;
	protected CmnClassStock.CompanyGroupSet company_group_set = null;
	// protected boolean setup_data_source_rule_done = false;
	protected String finance_root_folderpath = CmnDef.CSV_ROOT_FOLDERPATH;
	protected String finance_backup_folderpath = CmnDef.CSV_BACKUP_FOLDERPATH;
	// protected String finance_backup_foldername = null;
	protected String finance_restore_folderpath = CmnDef.CSV_RESTORE_FOLDERPATH;
	// protected String finance_restore_foldername = null;
	protected String[] finance_root_folderpath_array = new String[]
	{ CmnDef.CSV_ROOT_FOLDERPATH, CmnDef.CSV_BACKUP_FOLDERPATH, CmnDef.CSV_RESTORE_FOLDERPATH };
	// protected String[] finance_remote_root_folderpath_array = new
	// String[]{CmnDef.CSV_ROOT_FOLDERPATH, CmnDef.CSV_BACKUP_FOLDERPATH,
	// CmnDef.CSV_RESTORE_FOLDERPATH};
	protected CmnDef.CSVWorkingFolderType csv_working_folder_type = CmnDef.CSVWorkingFolderType.CSVWorkingFolder_Unknown;
	protected String current_csv_working_folerpath = null;
	protected CmnDef.DeleteSQLAccurancyType delete_sql_accurancy_type = CmnDef.DeleteSQLAccurancyType.DeleteSQLAccurancyType_METHOD_ONLY;
	protected boolean operation_non_stop = true;
	protected String remote_csv_server_ip = null;
	
	protected abstract CmnInf.DataHandlerInf get_data_handler();
	
	@Override
	public void set_finance_folderpath(String finance_folderpath)
	{
		assert finance_folderpath != null : "finance_folderpath should NOT be null";
		finance_root_folderpath_array[CmnDef.CSVWorkingFolderType.CSVWorkingFolder_Write
				.value()] = finance_root_folderpath = finance_folderpath;
	}
	
	@Override
	public void set_finance_backup_folderpath(String finance_folderpath)
	{
		assert finance_folderpath != null : "finance_backup_folderpath should NOT be null";
		finance_root_folderpath_array[CmnDef.CSVWorkingFolderType.CSVWorkingFolder_Backup
				.value()] = finance_backup_folderpath = finance_folderpath;
	}
	
	// public void set_finance_backup_foldername(String finance_foldername)
	// {
	// finance_backup_foldername = finance_foldername;
	// }
	@Override
	public void set_finance_restore_folderpath(String finance_folderpath)
	{
		assert finance_folderpath != null : "finance_restore_folderpath should NOT be null";
		finance_root_folderpath_array[CmnDef.CSVWorkingFolderType.CSVWorkingFolder_Restore
				.value()] = finance_restore_folderpath = finance_folderpath;
	}
	
	// public void set_finance_restore_foldername(String finance_foldername)
	// {
	// finance_restore_foldername = finance_foldername;
	// }
	@Override
	public String get_finance_folderpath()
	{
		return finance_root_folderpath;
	}
	
	@Override
	public String get_finance_backup_folderpath()
	{
		return finance_backup_folderpath;
	}
	
	// public String get_finance_backup_foldername(){return
	// finance_backup_foldername;}
	@Override
	public String get_finance_restore_folderpath()
	{
		return finance_restore_folderpath;
	}
	
	// public String get_finance_restore_foldername(){return
	// finance_restore_foldername;}
	@Override
	public void switch_current_csv_working_folerpath(CmnDef.CSVWorkingFolderType working_folder_type)
	{
		csv_working_folder_type = working_folder_type;
		current_csv_working_folerpath = finance_root_folderpath_array[csv_working_folder_type.value()];
	}
	
	@Override
	public void set_operation_non_stop(boolean enable)
	{
		operation_non_stop = enable;
	}
	
	@Override
	public boolean is_operation_non_stop()
	{
		return operation_non_stop;
	}
	
	public void set_csv_remote_source(String server_ip)
	{
		remote_csv_server_ip = server_ip;
	}// disable remote source while server_ip = null
	
	public boolean is_csv_remote_source()
	{
		return remote_csv_server_ip != null ? true : false;
	}
	
	@Override
	public short get_backup_foldername_list(List<String> sorted_sub_foldername_list)
	{
		// int pos = finance_backup_folderpath.lastIndexOf("/");
		// if (pos == -1)
		// {
		// CmnLogger.format_error("Incorrect finance root backup folder path:
		// %s", finance_backup_folderpath);
		// return CmnDef.RET_FAILURE_INCORRECT_PATH;
		// }
		// String backup_foldername = finance_backup_folderpath.substring(pos +
		// 1);
		// if (CmnDef.get_regex_matcher("[\\d]{14}", backup_foldername) == null)
		// {
		// CmnLogger.format_error("Incorrect finance root backup folder name:
		// %s", backup_foldername);
		// return CmnDef.RET_FAILURE_INCORRECT_PATH;
		// }
		// String finance_parent_backup_folderpath =
		// finance_backup_folderpath.substring(0, pos);
		return sort_sub_foldername_list(finance_backup_folderpath, sorted_sub_foldername_list);
	}
	
	@Override
	public short get_restore_foldername_list(List<String> sorted_sub_foldername_list)
	{
		// int pos = finance_restore_folderpath.lastIndexOf("/");
		// if (pos == -1)
		// {
		// CmnLogger.format_error("Incorrect finance root restore folder path:
		// %s", finance_restore_folderpath);
		// return CmnDef.RET_FAILURE_INCORRECT_PATH;
		// }
		// String restore_foldername = finance_restore_folderpath.substring(pos
		// + 1);
		// if (CmnDef.get_regex_matcher("[\\d]{14}", restore_foldername) ==
		// null)
		// {
		// CmnLogger.format_error("Incorrect finance root restore folder name:
		// %s", restore_foldername);
		// return CmnDef.RET_FAILURE_INCORRECT_PATH;
		// }
		// String finance_parent_restore_folderpath =
		// finance_restore_folderpath.substring(0, pos);
		return sort_sub_foldername_list(finance_restore_folderpath, sorted_sub_foldername_list);
	}
	
	@Override
	public short initialize()
	{
		/////////////////////////////////////////////////////////////
		// The source type, time range, company group should be setup before
		///////////////////////////////////////////////////////////// initialize
		///////////////////////////////////////////////////////////// the
		///////////////////////////////////////////////////////////// manager
		///////////////////////////////////////////////////////////// class
		// if (setup_data_source_rule_done)
		// {
		// CmnLogger.error("The manager class has been initialized....");
		// return CmnDef.RET_FAILURE_INCORRECT_OPERATION;
		// }
		short ret = set_method(null);
		return ret;
	}
	
	// public short reset_data_source_rule()
	// {
	// if (!setup_data_source_rule_done)
	// return CmnDef.RET_FAILURE_INCORRECT_OPERATION;
	// if (method_index_list != null)
	// method_index_list = null;
	// setup_data_source_rule_done = false;
	// return CmnDef.RET_SUCCESS;
	// }
	//
	// public short set_data_source_rule_done()
	// {
	// if (setup_data_source_rule_done)
	// return CmnDef.RET_FAILURE_INCORRECT_OPERATION;
	// if (method_index_list == null)
	// {
	// for (Integer method_index : CmnFunc.get_all_method_index_list())
	// method_index_list.add(method_index);
	// }
	// setup_data_source_rule_done = true;
	// return CmnDef.RET_SUCCESS;
	// }
	
	// @Override
	// public short set_method_from_file(String filename)
	// {
	// // if (setup_data_source_rule_done)
	// // {
	// // CmnLogger.error("The data source rule has been initialized....");
	// // return CmnDef.RET_FAILURE_INCORRECT_OPERATION;
	// // }
	// short ret = CmnDef.RET_SUCCESS;
	// LinkedList<String> config_line_list = new LinkedList<String>();
	// // Read the content from the config file
	// ret = CmnFunc.read_config_file_lines(filename, config_line_list);
	// if (CmnDef.CheckFailure(ret))
	// return ret;
	// if (!method_index_list.isEmpty())
	// method_index_list.clear();
	// OUT: for (String line : config_line_list)
	// {
	// String[] entry_arr = line.split(" ");
	// if (entry_arr.length < 1)
	// {
	// CmnLogger.format_error("Incorrect format", line);
	// return CmnDef.RET_FAILURE_INCORRECT_CONFIG;
	// }
	// // Get the type of data source
	// int method_index =
	// CmnFunc.get_method_index_from_description(entry_arr[0]);
	// if (method_index == -1)
	// {
	// CmnLogger.format_error("Unknown data source type[%s] in config file",
	// entry_arr[0]);
	// ret = CmnDef.RET_FAILURE_INCORRECT_CONFIG;
	// break OUT;
	// }
	// // Check the source type is in correct finance mode
	// if (!CmnFunc.check_method_index_in_range(method_index))
	// {
	// CmnLogger.format_error("The source type[%s] does NOT belong to %s mode",
	// method_index,
	// CmnFunc.get_finance_mode_description());
	// ret = CmnDef.RET_FAILURE_INCORRECT_CONFIG;
	// break OUT;
	// }
	// method_index_list.add(method_index);
	// }
	// return ret;
	// }
	
	@Override
	public short set_method(List<Integer> in_method_index_list)
	{
		// if (setup_data_source_rule_done)
		// {
		// CmnLogger.error("The data source rule has been initialized....");
		// return CmnDef.RET_FAILURE_INCORRECT_OPERATION;
		// }
		if (in_method_index_list == null)
			in_method_index_list = CmnFunc.get_all_method_index_list();
		method_index_list = new LinkedList<Integer>();
		for (Integer method_index : in_method_index_list)
			method_index_list.add(method_index);
		return CmnDef.RET_SUCCESS;
	}
	
	// @Override
	// public short set_company_from_file(String filename)
	// {
	// // This method can only be called in Stock mode
	// throw new RuntimeException("Unsupported method !!!");
	// }
	
	@Override
	public short set_company(List<String> company_number_list)
	{
		// This method can only be called in Stock mode
		throw new RuntimeException("Unsupported method !!!");
	}
	
	@Override
	public short transfrom_csv_to_sql()
	{
		// if (!setup_data_source_rule_done)
		// {
		// CmnLogger.error("The data source rule is NOT initialized....");
		// return CmnDef.RET_FAILURE_INCORRECT_OPERATION;
		// }
		if (current_csv_working_folerpath == null)
		{
			CmnLogger.debug("current_csv_working_folerpath should NOT be NULL");
			return CmnDef.RET_FAILURE_INCORRECT_OPERATION;
		}
		CmnInf.DataHandlerInf finance_recorder_data_handler = get_data_handler();
		finance_recorder_data_handler.set_current_csv_working_folerpath(current_csv_working_folerpath);
		finance_recorder_data_handler.set_operation_non_stop(operation_non_stop);
		finance_recorder_data_handler.set_csv_remote_source(remote_csv_server_ip);
		short ret = finance_recorder_data_handler.transfrom_csv_to_sql();
		return ret;
	}
	
	@Override
	public short transfrom_csv_to_sql_multithread(int sub_company_group_set_amount)
	{
		throw new RuntimeException("Not support multh-thread !!!");
	}
	
	@Override
	public short transfrom_sql_to_csv(CmnClass.QuerySet query_set, CmnClass.FinanceTimeRange finance_time_range)
	{
		// if (!setup_data_source_rule_done)
		// {
		// CmnLogger.error("The data source rule is NOT initialized....");
		// return CmnDef.RET_FAILURE_INCORRECT_OPERATION;
		// }
		if (current_csv_working_folerpath == null)
		{
			CmnLogger.debug("current_csv_working_folerpath should NOT be NULL");
			return CmnDef.RET_FAILURE_INCORRECT_OPERATION;
		}
		// Create the folder for writing CSV if not exist
		short ret = CmnDef.RET_SUCCESS;
		CmnInf.DataHandlerInf finance_recorder_data_handler = get_data_handler();
		finance_recorder_data_handler.set_current_csv_working_folerpath(current_csv_working_folerpath);
		finance_recorder_data_handler.set_operation_non_stop(operation_non_stop);
		ret = finance_recorder_data_handler.transfrom_sql_to_csv(query_set, finance_time_range);
		return ret;
	}
	
	@Override
	public short transfrom_sql_to_csv(CmnClass.FinanceTimeRange finance_time_range)
	{
		// if (!setup_data_source_rule_done)
		// {
		// CmnLogger.error("The data source rule is NOT initialized....");
		// return CmnDef.RET_FAILURE_INCORRECT_OPERATION;
		// }
		CmnClass.QuerySet query_set = new CmnClass.QuerySet();
		for (Integer method_index : method_index_list)
			query_set.add_query(method_index);
		short ret = query_set.add_query_done();
		if (CmnDef.CheckFailure(ret))
		{
			CmnLogger.error("Fail to set add-done flag in query_set to true");
			return CmnDef.RET_FAILURE_INVALID_ARGUMENT;
		}
		return transfrom_sql_to_csv(query_set, finance_time_range);
	}
	
	@Override
	public short delete_sql()
	{
		// if (!setup_data_source_rule_done)
		// {
		// CmnLogger.error("The data source rule is NOT initialized....");
		// return CmnDef.RET_FAILURE_INCORRECT_OPERATION;
		// }
		CmnInf.DataHandlerInf finance_recorder_data_handler = get_data_handler();
		finance_recorder_data_handler.set_operation_non_stop(operation_non_stop);
		short ret = CmnDef.RET_SUCCESS;
		switch (delete_sql_accurancy_type)
		{
			case DeleteSQLAccurancyType_METHOD_ONLY:
				ret = finance_recorder_data_handler.delete_sql_by_method();
				break;
			case DeleteSQLAccurancyType_COMPANY_ONLY:
				ret = finance_recorder_data_handler.delete_sql_by_company();
				break;
			case DeleteSQLAccurancyType_METHOD_AND_COMPANY:
				ret = finance_recorder_data_handler.delete_sql_by_method_and_company();
				break;
			default:
				throw new IllegalStateException(
						String.format("Unknown delete sql accurancy type: %d", delete_sql_accurancy_type.value()));
		}
		return ret;
	}
	
	@Override
	public short cleanup_sql()
	{
		// if (!setup_data_source_rule_done)
		// {
		// CmnLogger.error("The data source rule is NOT initialized....");
		// return CmnDef.RET_FAILURE_INCORRECT_OPERATION;
		// }
		CmnInf.DataHandlerInf finance_recorder_data_handler = get_data_handler();
		finance_recorder_data_handler.set_operation_non_stop(operation_non_stop);
		short ret = finance_recorder_data_handler.cleanup_sql();
		return ret;
	}
	
	@Override
	public short check_sql_exist(ArrayList<String> not_exist_list)
	{
		// if (!setup_data_source_rule_done)
		// {
		// CmnLogger.error("The data source rule is NOT initialized....");
		// return CmnDef.RET_FAILURE_INCORRECT_OPERATION;
		// }
		CmnInf.DataHandlerInf finance_recorder_data_handler = get_data_handler();
		finance_recorder_data_handler.set_operation_non_stop(operation_non_stop);
		short ret = finance_recorder_data_handler.check_sql_exist(not_exist_list);
		return ret;
	}
	
	// private short update_backup_by_config_file(String filename,
	// HashMap<Integer,CmnClass.TimeRangeCfg> time_range_table, HashMap<Integer,
	// LinkedList<Integer>> source_field_table)
	// {
	// short ret = CmnDef.RET_SUCCESS;
	// LinkedList<String> config_line_list = new LinkedList<String>();
	//// Read the content from the config file
	// ret = get_config_file_lines(filename, config_line_list);
	// if (CmnDef.CheckFailure(ret))
	// return ret;
	//
	// String data_source;
	// String field_list_str;
	// String time_month_start;
	// String time_month_end;
	// String time_month_today = null;
	// OUT:
	// for (String line : config_line_list)
	// {
	// if (line.startsWith(CmnDef.CONF_ENTRY_IGNORE_FLAG) || line.length() == 0)
	// {
	// CmnLogger.format_debug("Ignore the entry: %s", line);
	// continue;
	// }
	// String[] entry_arr = line.split(" ");
	// if (entry_arr.length < 2)
	// {
	// CmnLogger.format_error("Incorrect format", line);
	// return CmnDef.RET_FAILURE_INCORRECT_CONFIG;
	// }
	//// Get the type of data source
	// data_source = entry_arr[0];
	// int method_index =
	// Arrays.asList(CmnDef.FINANCE_DATA_DESCRIPTION_LIST).indexOf(data_source);
	// if (method_index == -1)
	// {
	// CmnLogger.format_error("Unknown data source type[%s] in config file",
	// data_source);
	// ret = CmnDef.RET_FAILURE_INCORRECT_CONFIG;
	// break OUT;
	// }
	//// Parse the field
	// LinkedList<Integer> field_list = new LinkedList<Integer>();
	// field_list_str = entry_arr[1];
	// if (field_list_str.equals(CONFIG_ALL_FIELD_STRING))
	// {
	//// Start from 1; the date field should be ignored
	// for (int i = 1 ; i <
	// CmnDef.FINANCE_DATABASE_FIELD_AMOUNT_LIST[method_index] ; i++)
	// field_list.add(i);
	// }
	// else
	// {
	// String[] field_array = field_list_str.split(":");
	// for (String field : field_array)
	// {
	//// Ignore the date field
	// if (field.equals("0"))
	// {
	// CmnLogger.format_warn("Ignore the date filed in data source[%s]",
	// data_source);
	// continue;
	// }
	// field_list.add(Integer.getInteger(field));
	// }
	// }
	//// Get the time of start time
	// if (entry_arr.length >= 3)
	// time_month_start = entry_arr[2];
	// else
	// {
	// if (time_month_today == null)
	// time_month_today = CmnDef.get_time_month_today();
	// time_month_start = time_month_today;
	// }
	// if (CmnClass.TimeCfg.get_month_value_matcher(time_month_start) == null)
	// {
	// CmnLogger.format_error("Incorrect start time format[%s] in config file",
	// time_month_start);
	// ret = CmnDef.RET_FAILURE_INCORRECT_CONFIG;
	// break OUT;
	// }
	//// Get the time of end time
	// if (entry_arr.length >= 4)
	// time_month_end = entry_arr[3];
	// else
	// {
	// if (time_month_today == null)
	// time_month_today = CmnDef.get_time_month_today();
	// time_month_end = time_month_today;
	// }
	// if (CmnClass.TimeCfg.get_month_value_matcher(time_month_end) == null)
	// {
	// CmnLogger.format_error("Incorrect end time format[%s] in config file",
	// time_month_end);
	// ret = CmnDef.RET_FAILURE_INCORRECT_CONFIG;
	// break OUT;
	// }
	//
	// CmnLogger.format_debug("New entry for backup in config [%s %s %s:%s]",
	// data_source, field_list_str, time_month_start, time_month_end);
	// source_field_table.put(method_index, field_list);
	//// CmnClass.TimeRangeCfg time_range_cfg = new
	// CmnClass.TimeRangeCfg(time_month_start, time_month_end);
	////// Adjust the time range
	//// CmnClass.TimeRangeCfg time_range_cfg =
	// get_time_range_cfg_object_after_adjustment(method_index,
	// time_month_start, time_month_end);
	//// if (time_range_cfg == null)
	//// {
	//// ret = CmnDef.RET_FAILURE_UNEXPECTED_VALUE;
	//// break OUT;
	//// }
	// time_range_table.put(method_index, new
	// CmnClass.TimeRangeCfg(time_month_start, time_month_end));
	//
	// }
	// return ret;
	// }
	//
	// private short update_time_range_table_by_parameter(LinkedList<Integer>
	// method_index_list, String time_month_start, String time_month_end,
	// HashMap<Integer,CmnClass.TimeRangeCfg> time_range_table)
	// {
	//// Check the time of start time
	// if (CmnClass.TimeCfg.get_month_value_matcher(time_month_start) == null)
	// {
	// CmnLogger.format_error("Incorrect start month format[%s] in config file",
	// time_month_start);
	// return CmnDef.RET_FAILURE_INCORRECT_CONFIG;
	// }
	//// Check the time of start time
	// if (CmnClass.TimeCfg.get_month_value_matcher(time_month_end) == null)
	// {
	// CmnLogger.format_error("Incorrect end month format[%s] in config file",
	// time_month_end);
	// return CmnDef.RET_FAILURE_INCORRECT_CONFIG;
	// }
	//
	// for (Integer method_index : method_index_list)
	// {
	// CmnLogger.format_debug("New entry in config [%s %s:%s]", method_index,
	// time_month_start, time_month_end);
	//// CmnClass.TimeRangeCfg time_range_cfg = new
	// CmnClass.TimeRangeCfg(time_month_start, time_month_end);
	// CmnClass.TimeRangeCfg time_range_cfg =
	// get_time_range_cfg_object_after_adjustment(method_index,
	// time_month_start, time_month_end);
	// if (time_range_cfg == null)
	// return CmnDef.RET_FAILURE_UNEXPECTED_VALUE;
	// time_range_table.put(method_index, time_range_cfg);
	// }
	// return CmnDef.RET_SUCCESS;
	// }
	//
	// private short
	// update_full_time_range_table(HashMap<Integer,CmnClass.TimeRangeCfg>
	// time_range_table)
	// {
	// if (database_time_range == null)
	// {
	// CmnLogger.error("The database time range object is NOT initialized");
	// return CmnDef.RET_FAILURE_INCORRECT_OPERATION;
	// }
	// return database_time_range.get_all_method_time_range(time_range_table);
	// }
	//
	// public short setup_time_range_table_by_config_file(String filename)
	// {
	// if (finance_source_time_range_table != null)
	// {
	// CmnLogger.error("The time range table has already been updated");
	// return CmnDef.RET_FAILURE_INCORRECT_OPERATION;
	// }
	// finance_source_time_range_table = new HashMap<Integer,
	// CmnClass.TimeRangeCfg>();
	// return update_time_range_table_by_config_file(filename,
	// finance_source_time_range_table);
	// }
	//
	// public short setup_time_range_table_by_parameter(LinkedList<Integer>
	// method_index_list, String time_month_start, String time_month_end)
	// {
	// if (finance_source_time_range_table != null)
	// {
	// CmnLogger.error("The time range table has already been updated");
	// return CmnDef.RET_FAILURE_INCORRECT_OPERATION;
	// }
	// finance_source_time_range_table = new HashMap<Integer,
	// CmnClass.TimeRangeCfg>();
	// return update_time_range_table_by_parameter(method_index_list,
	// time_month_start, time_month_end, finance_source_time_range_table);
	// }
	//
	// public short setup_backup_time_range_table_by_config_file(String
	// filename)
	// {
	// if (finance_backup_source_time_range_table != null)
	// {
	// CmnLogger.error("The time range table for backup has already been
	// updated");
	// return CmnDef.RET_FAILURE_INCORRECT_OPERATION;
	// }
	// finance_backup_source_time_range_table = new HashMap<Integer,
	// CmnClass.TimeRangeCfg>();
	// return update_time_range_table_by_config_file(filename,
	// finance_backup_source_time_range_table);
	// }
	//
	// public short
	// setup_backup_time_range_table_by_parameter(LinkedList<Integer>
	// method_index_list, String time_month_start, String time_month_end)
	// {
	// if (finance_backup_source_time_range_table != null)
	// {
	// CmnLogger.error("The time range table for backup has already been
	// updated");
	// return CmnDef.RET_FAILURE_INCORRECT_OPERATION;
	// }
	// finance_backup_source_time_range_table = new HashMap<Integer,
	// CmnClass.TimeRangeCfg>();
	// return update_time_range_table_by_parameter(method_index_list,
	// time_month_start, time_month_end,
	// finance_backup_source_time_range_table);
	// }
	//
	// private short parse_config()
	// {
	// final String config_field[] = {"Unknown", "[email_address]"};
	//
	// BufferedReader reader = null;
	//// Open the config file
	// String filepath = String.format("%s/%s", CmnDef.CONF_FOLDERNAME,
	// CmnDef.FINANCE_RECORDER_CONF_FILENAME);
	// File file = new File(filepath);
	// if (!file.exists())
	// {
	// CmnLogger.format_error("The config file[%s] does NOT exist",
	// CmnDef.FINANCE_RECORDER_CONF_FILENAME);
	// return CmnDef.RET_FAILURE_NOT_FOUND;
	// }
	// short ret = CmnDef.RET_SUCCESS;
	//// Try to parse the content of the config file
	// try
	// {
	// reader = new BufferedReader(new FileReader(file));
	// String buf;
	//// boolean param_start = false;
	// ConfigFieldType config_field_type = ConfigFieldType.ConfigField_Unknown;
	// OUT:
	// while ((buf = reader.readLine()) != null)
	// {
	//// if (buf.length() == 0)
	//// continue;
	// if (buf.startsWith("["))
	// {
	// config_field_type = ConfigFieldType.ConfigField_Unknown;
	// for (int i = 1 ; i < ConfigFieldType.values().length ; i++)
	// {
	// if (buf.indexOf(config_field[i]) != -1)
	// {
	// CmnLogger.format_debug("Parse the parameter in the file: %s",
	// config_field[i]);
	// config_field_type = ConfigFieldType.valueOf(i);
	// break;
	// }
	// }
	// if (config_field_type == ConfigFieldType.ConfigField_Unknown)
	// {
	// CmnLogger.format_error("Unknown field title in config[%s]: %s", filepath,
	// buf);
	// return CmnDef.RET_FAILURE_INCORRECT_CONFIG;
	// }
	// }
	// else
	// {
	//// if (strlen(buf) == 0)
	//// continue;
	// switch(config_field_type)
	// {
	// case ConfigField_EmailAddress:
	// {
	// email_address_list.add(buf);
	// }
	// break;
	// default:
	// {
	// CmnLogger.format_error("Unsupported field type: %d", config_field_type);
	// return CmnDef.RET_FAILURE_INCORRECT_CONFIG;
	// }
	// }
	// }
	// }
	// }
	// catch (IOException ex)
	// {
	// CmnLogger.format_error("IO Error occur due to %s", ex.toString());
	// ret = CmnDef.RET_FAILURE_INVALID_ARGUMENT;
	// }
	// catch (Exception ex)
	// {
	// CmnLogger.format_error("Error occur due to %s", ex.toString());
	// ret = CmnDef.RET_FAILURE_INVALID_ARGUMENT;
	// }
	// finally
	// {
	//// Close the file
	// if (reader != null)
	// {
	// try {reader.close();}
	// catch (IOException e){}// nothing to do here except log the exception
	// }
	// }
	// return CmnDef.RET_SUCCESS;
	// }
	//
	//// private short check_time_month_range_from_csv(int method_index,
	// StringBuilder csv_time_month_start_str_builder, StringBuilder
	// csv_time_month_end_str_builder)
	//// {
	//// String command = String.format("ls %s | grep %s",
	// CmnDef.DATA_ROOT_FOLDERPATH,
	// CmnDef.FINANCE_DATA_NAME_LIST[method_index]);
	//// CmnLogger.format_debug("Find CSV time range by command: %s",
	// method_index, command);
	//// StringBuilder result_str_builder = new StringBuilder();
	//// short ret = CmnDef.execute_shell_command(command, result_str_builder);
	//// if (CmnDef.CheckFailure(ret))
	//// return ret;
	////// Check the search result
	//// String result_list_str = result_str_builder.toString();
	//// if (result_list_str.isEmpty())
	//// {
	//// CmnLogger.format_error("No %s CSV files are found",
	// CmnDef.FINANCE_DATA_DESCRIPTION_LIST[method_index]);
	//// return CmnDef.RET_FAILURE_NOT_FOUND;
	//// }
	////// Find the csv time range from the result of shell command
	//// String[] result_list = result_list_str.split("\n");
	//// int result_list_len = result_list.length;
	////// Pattern csv_filename_pattern =
	// Pattern.compile(String.format("%s([\\d]{6}).csv",
	// CmnDef.FINANCE_DATA_NAME_LIST[method_index]));
	//// Pattern csv_filename_pattern = Pattern.compile("([\\d]{6})");
	//// Matcher csv_filename_start_matcher =
	// csv_filename_pattern.matcher(result_list[0]);
	//// if (!csv_filename_start_matcher.find())
	//// {
	//// CmnLogger.format_error("Incorrect start time filename format: %s",
	// result_list[0]);
	//// return CmnDef.RET_FAILURE_UNEXPECTED_VALUE;
	//// }
	//// Matcher csv_filename_end_matcher =
	// csv_filename_pattern.matcher(result_list[result_list_len - 1]);
	//// if (!csv_filename_end_matcher.find())
	//// {
	//// CmnLogger.format_error("Incorrect end time filename format: %s",
	// result_list[result_list_len - 1]);
	//// return CmnDef.RET_FAILURE_UNEXPECTED_VALUE;
	//// }
	//// String start_time_str = csv_filename_start_matcher.group(1);
	//// String end_time_str = csv_filename_end_matcher.group(1);
	//// int start_year = Integer.parseInt(start_time_str.substring(0, 4));
	//// int start_month = Integer.parseInt(start_time_str.substring(4));
	//// int end_year = Integer.parseInt(end_time_str.substring(0, 4));
	//// int end_month = Integer.parseInt(end_time_str.substring(4));
	////
	//// csv_time_month_start_str_builder.append(String.format("%04d-%02d",
	// start_year, start_month));
	//// csv_time_month_end_str_builder.append(String.format("%04d-%02d",
	// end_year, end_month));
	//// CmnLogger.format_debug("Time range from CSV: %s %s",
	// csv_time_month_start_str_builder.toString(),
	// csv_time_month_end_str_builder.toString());
	////
	//// return CmnDef.RET_SUCCESS;
	//// }
	////
	//// private CmnClass.TimeRangeCfg
	// get_time_range_cfg_object_after_adjustment(int method_index, String
	// time_month_start, String time_month_end)
	//// {
	//// StringBuilder csv_time_month_start_str_builder = new StringBuilder();
	//// StringBuilder csv_time_month_end_str_builder = new StringBuilder();
	//// short ret = check_time_month_range_from_csv(method_index,
	// csv_time_month_start_str_builder, csv_time_month_end_str_builder);
	//// if (CmnDef.CheckFailure(ret))
	//// return null;
	//// String month_start = time_month_start;
	//// String month_end = time_month_end;
	//// CmnClass.TimeRangeCfg time_range_cfg = null;
	//// try
	//// {
	//// java.util.Date csv_start_month_date =
	// CmnDef.get_month_date(csv_time_month_start_str_builder.toString());
	//// java.util.Date csv_end_month_date =
	// CmnDef.get_month_date(csv_time_month_end_str_builder.toString());
	//// java.util.Date start_month_date =
	// CmnDef.get_month_date(time_month_start);
	//// java.util.Date end_month_date = CmnDef.get_month_date(time_month_end);
	//// if (start_month_date.before(csv_start_month_date))
	//// {
	//// CmnLogger.format_warn("Out of range in %s! Change start month from %s
	// to %s", CmnDef.FINANCE_DATA_DESCRIPTION_LIST[method_index],
	// time_month_start, csv_time_month_start_str_builder.toString());
	//// month_start = csv_time_month_start_str_builder.toString();
	//// }
	//// if (end_month_date.after(csv_end_month_date))
	//// {
	//// CmnLogger.format_warn("Out of range in %s! Change end month from %s to
	// %s", CmnDef.FINANCE_DATA_DESCRIPTION_LIST[method_index], time_month_end,
	// csv_time_month_end_str_builder.toString());
	//// month_end = csv_time_month_end_str_builder.toString();
	//// }
	//// time_range_cfg = new CmnClass.TimeRangeCfg(month_start, month_end);
	//// }
	//// catch (ParseException e)
	//// {
	//// CmnLogger.format_error("Incorrect time format, due to: %s",
	// e.toString());
	//// }
	//// catch (Exception e)
	//// {
	//// CmnLogger.format_error("Error occur due to: %s", e.toString());
	//// }
	////
	//// return time_range_cfg;
	//// }
	//
	// public short write()
	// {
	//// Write the data into MySQL one by one
	// short ret;
	// for (Map.Entry<Integer, CmnClass.TimeRangeCfg> entry :
	// finance_source_time_range_table.entrySet())
	// {
	// int method_index = entry.getKey();
	// MarketDataHandler finance_recorder_data_handler = new
	// MarketDataHandler(CmnDef.FinanceSourceType.valueOf(method_index));
	// CmnClass.TimeRangeCfg time_range_cfg = entry.getValue();
	//
	// CmnLogger.format_debug("Try to write data [%s %s] into MySQL......",
	// finance_recorder_data_handler.get_description(),
	// time_range_cfg.toString());
	//// Write the data into MySQL
	// ret = finance_recorder_data_handler.write_to_sql(time_range_cfg,
	// CmnDef.DatabaseCreateThreadType.DatabaseCreateThread_Single,
	// CmnDef.DatabaseEnableBatchType.DatabaseEnableBatch_No);
	// if (CmnDef.CheckFailure(ret))
	// return ret;
	// }
	// return CmnDef.RET_SUCCESS;
	// }
	//
	// public short restore(String restore_folderpath, String
	// restore_foldername)
	// {
	//// Backup the workday calendar/database time range config files
	// short ret = CmnDef.RET_SUCCESS;
	// String current_path = CmnDef.get_current_path();
	// String src_folderpath = String.format("%s/%s/%s", (restore_folderpath !=
	// null ? restore_folderpath : current_path), CmnDef.BACKUP_FOLDERNAME,
	// restore_foldername);
	// String workday_canlendar_src_filepath = String.format("%s/%s",
	// src_folderpath, CmnDef.WORKDAY_CANLENDAR_CONF_FILENAME);
	// String database_time_range_src_filepath = String.format("%s/%s",
	// src_folderpath,CmnDef.DATABASE_TIME_RANGE_CONF_FILENAME);
	// String dst_folderpath = String.format("%s/%s", current_path,
	// CmnDef.CONF_FOLDERNAME);
	//// Copy the workday calendar config file
	// CmnLogger.format_debug("Copy workday calendar config file[%s] from bakcup
	// folder[%s]", CmnDef.WORKDAY_CANLENDAR_CONF_FILENAME, restore_foldername);
	// File workday_calendar_file_handle = new
	// File(workday_canlendar_src_filepath);
	// if (!workday_calendar_file_handle.exists())
	// {
	// CmnLogger.format_error("The workday calendar config file[%s] does NOT
	// exist", CmnDef.WORKDAY_CANLENDAR_CONF_FILENAME);
	// return CmnDef.RET_FAILURE_NOT_FOUND;
	// }
	// ret = CmnDef.copy_file(workday_canlendar_src_filepath,
	// String.format("%s/%s", dst_folderpath,
	// CmnDef.WORKDAY_CANLENDAR_CONF_FILENAME));
	// if (CmnDef.CheckFailure(ret))
	// return ret;
	//// Copy the database time range config file
	// CmnLogger.format_debug("Copy database time range config file[%s] from
	// bakcup folder[%s]", CmnDef.DATABASE_TIME_RANGE_CONF_FILENAME,
	// restore_foldername);
	// File database_time_range_file_handle = new
	// File(database_time_range_src_filepath);
	// if (!database_time_range_file_handle.exists())
	// {
	// CmnLogger.format_error("The database time range config file[%s] does NOT
	// exist", CmnDef.DATABASE_TIME_RANGE_CONF_FILENAME);
	// return CmnDef.RET_FAILURE_NOT_FOUND;
	// }
	// ret = CmnDef.copy_file(database_time_range_src_filepath,
	// String.format("%s/%s", dst_folderpath,
	// CmnDef.DATABASE_TIME_RANGE_CONF_FILENAME));
	// if (CmnDef.CheckFailure(ret))
	// return ret;
	//// We assume the process stops after restore so that the databse time
	// range singleton can be initialized here !!!
	// ret = init_database_time_range_table();
	//// Write the data into MySQL one by one
	// int data_name_list_length = CmnDef.FINANCE_DATA_NAME_LIST.length;
	// for (int method_index = 0 ; method_index < data_name_list_length ;
	// method_index++)
	// {
	// CmnClass.TimeRangeCfg time_range_cfg = null;
	// try
	// {
	// time_range_cfg = database_time_range.get_method_time_range(method_index);
	// MarketDataHandler finance_recorder_data_handler = new
	// MarketDataHandler(CmnDef.FinanceSourceType.valueOf(method_index),
	// src_folderpath);
	// CmnLogger.format_debug("Try to restore data [%s %s] into MySQL......",
	// finance_recorder_data_handler.get_description(),
	// time_range_cfg.toString());
	// // Write the data into MySQL
	// ret = finance_recorder_data_handler.write_to_sql(time_range_cfg,
	// CmnDef.DatabaseCreateThreadType.DatabaseCreateThread_Single,
	// CmnDef.DatabaseEnableBatchType.DatabaseEnableBatch_No);
	// if (CmnDef.CheckFailure(ret))
	// return ret;
	// }
	// catch (IllegalArgumentException e)
	// {
	// CmnLogger.format_debug("No data[%d] to restore, skipped...",
	// method_index);
	// }
	// }
	// return CmnDef.RET_SUCCESS;
	// }
	//
	// public short restore_latest(String restore_folderpath)
	// {
	//// Write the data into MySQL one by one
	// List<String> sorted_restore_list = new LinkedList<String>();
	// short ret = get_sorted_backup_list(sorted_restore_list);
	// if (CmnDef.CheckFailure(ret))
	// return ret;
	// if (sorted_restore_list.isEmpty())
	// {
	// CmnLogger.error("The restore files are NOT found");
	// return CmnDef.RET_FAILURE_NOT_FOUND;
	// }
	// String restore_foldername =
	// sorted_restore_list.get(sorted_restore_list.size() - 1);
	//
	// return restore(restore_folderpath, restore_foldername);
	// }
	//
	// public short write_by_multithread()
	// {
	// ThreadPoolExecutor executor =
	// (ThreadPoolExecutor)Executors.newFixedThreadPool(CmnDef.MAX_CONCURRENT_THREAD);
	// LinkedList<Future<Integer>> res_list = new LinkedList<Future<Integer>>();
	// short ret = CmnDef.RET_SUCCESS;
	// OUT:
	// do
	// {
	// for (Map.Entry<Integer, CmnClass.TimeRangeCfg> entry :
	// finance_source_time_range_table.entrySet())
	// {
	// int method_index = entry.getKey();
	//// Writer finance_recorder_data_handler = new
	// Writer(CmnDef.FinanceSourceType.valueOf(method_index));
	// CmnClass.TimeRangeCfg time_range_cfg = entry.getValue();
	//// Split the time range into several smaller time range slice
	// ArrayList<CmnClass.TimeRangeCfg> time_range_slice_cfg_list = new
	// ArrayList<CmnClass.TimeRangeCfg>();
	// ret = get_time_range_slice(time_range_cfg,
	// CmnDef.WRITE_SQL_MAX_MONTH_RANGE_IN_THREAD, time_range_slice_cfg_list);
	// if (CmnDef.CheckFailure(ret))
	// break OUT;
	// for (CmnClass.TimeRangeCfg time_range_slice_cfg :
	// time_range_slice_cfg_list)
	// {
	//// Setup the time range
	//// Activate the task of writing data into SQL
	// MarketDataHandler finance_recorder_data_handler = new
	// MarketDataHandler(CmnDef.FinanceSourceType.valueOf(method_index));
	//// CmnLogger.format_debug("Try to write data [%s %04d%02d:%04d%02d] into
	// MySQL......", finance_recorder_data_handler.get_description(),
	// time_range_slice_cfg.get_start_time().get_year(),
	// time_range_slice_cfg.get_start_time().get_month(),
	// time_range_slice_cfg.get_end_time().get_year(),
	// time_range_slice_cfg.get_end_time().get_month());
	// StockWriteSQLTask task = new StockWriteSQLTask(new
	// MarketDataHandler(CmnDef.FinanceSourceType.valueOf(method_index)),
	// time_range_slice_cfg);
	// Future<Integer> res = executor.submit(task);
	//// try{Thread.sleep(5);}
	//// catch (InterruptedException e){}
	// res_list.add(res);
	// }
	// }
	//// Check the result
	// for (Future<Integer> res : res_list)
	// {
	// try
	// {
	// ret = res.get().shortValue();
	// }
	// catch (ExecutionException e)
	// {
	// CmnLogger.format_error("Fail to get return value, due to: %s",
	// e.toString());
	// ret = CmnDef.RET_FAILURE_UNKNOWN;
	// }
	// catch (InterruptedException e)
	// {
	// CmnLogger.format_error("Fail to get return value, due to: %s",
	// e.toString());
	// ret = CmnDef.RET_FAILURE_INCORRECT_OPERATION;
	// }
	// if (CmnDef.CheckFailure(ret))
	// break OUT;
	// }
	// }while(false);
	//// Shut down the executor
	// if(CmnDef.CheckSuccess(ret))
	// executor.shutdown();
	// else
	// executor.shutdownNow();
	//
	// return ret;
	// }
	//
	// public short init_workday_calendar_table()
	// {
	// if (workday_calendar != null)
	// {
	// CmnLogger.error("The workday calendar object has already been
	// initialized");
	// return CmnDef.RET_FAILURE_INCORRECT_OPERATION;
	// }
	// workday_calendar = WorkdayCalendar.get_instance();
	// return CmnDef.RET_SUCCESS;
	// }
	//
	// public short init_database_time_range_table()
	// {
	// if (database_time_range != null)
	// {
	// CmnLogger.error("The database time range object has already been
	// initialized");
	// return CmnDef.RET_FAILURE_INCORRECT_OPERATION;
	// }
	// database_time_range = DatabaseTimeRange.get_instance();
	// return CmnDef.RET_SUCCESS;
	// }
	//
	// public short query(CmnClass.TimeRangeCfg time_range_cfg, final
	// CmnClass.QuerySet query_set, CmnClass.ResultSet result_set)
	// {
	// if (database_time_range == null)
	// {
	// CmnLogger.error("The database time range object is NOT initialized");
	// return CmnDef.RET_FAILURE_INCORRECT_OPERATION;
	// }
	//
	// if (time_range_cfg.get_start_time() == null ||
	// time_range_cfg.get_end_time() == null)
	// {
	// CmnLogger.error("The start/end time in time_range_cfg should NOT be
	// NULL");
	// return CmnDef.RET_FAILURE_INVALID_ARGUMENT;
	// }
	// if (time_range_cfg.is_month_type())
	// {
	// CmnLogger.error("The time format of time_range_cfg should be Day type");
	// return CmnDef.RET_FAILURE_INVALID_ARGUMENT;
	// }
	// if (!query_set.is_add_query_done())
	// {
	// CmnLogger.error("The setting of query data is NOT complete");
	// return CmnDef.RET_FAILURE_INCORRECT_OPERATION;
	// }
	//// Collect the information that what kind of the data source will be
	// queried
	// HashSet<Integer> method_index_set = new HashSet<Integer>();
	// for (int i = 0 ; i < CmnDef.FINANCE_SOURCE_SIZE ; i++)
	// {
	// if (!query_set.get_index(i).isEmpty())
	// method_index_set.add(i);
	// }
	//// Restrict the search time range
	// CmnClass.TimeRangeCfg restrict_time_range_cfg = new
	// CmnClass.TimeRangeCfg(time_range_cfg.get_start_time_str(),
	// time_range_cfg.get_end_time_str());
	// database_time_range.restrict_time_range(method_index_set,
	// restrict_time_range_cfg);
	//
	// short ret = CmnDef.RET_SUCCESS;
	//// CmnClass.TimeRangeCfg time_range_cfg = null;
	// for (int method_index = 0 ; method_index < CmnDef.FINANCE_SOURCE_SIZE ;
	// method_index++)
	// {
	// LinkedList<Integer> query_field = query_set.get_index(method_index);
	// if (query_field.isEmpty())
	// continue;
	//// Add to the result set
	// ret = result_set.add_set(method_index, query_field);
	// if (CmnDef.CheckFailure(ret))
	// return ret;
	//
	// MarketDataHandler finance_recorder_data_handler = new
	// MarketDataHandler(CmnDef.FinanceSourceType.valueOf(method_index));
	// CmnLogger.format_debug("Try to read data [%s %s] from MySQL......",
	// finance_recorder_data_handler.get_description(),
	// restrict_time_range_cfg.toString());
	//// Format the SQL query command
	// StringBuilder field_cmd_builder = new StringBuilder();
	// ret = SQLClient.get_sql_field_command(method_index, query_field,
	// field_cmd_builder);
	// if (CmnDef.CheckFailure(ret))
	// return ret;
	//// Read the data from MySQL
	// ret =
	// finance_recorder_data_handler.read_from_sql(restrict_time_range_cfg,
	// field_cmd_builder.toString(), result_set);
	// if (CmnDef.CheckFailure(ret))
	// {
	//// If the database does NOT exist, just ignore the warning
	// if (!CmnDef.CheckMySQLFailureUnknownDatabase(ret))
	// return ret;
	// }
	// result_set.switch_to_check_date_mode();
	// }
	//// Check the result
	// ret = result_set.check_data();
	//
	// return ret;
	// }
	//
	// public short read_all(CmnClass.ResultSet result_set)
	// {
	//// Read all the data from MySQL one by one
	// short ret = CmnDef.RET_SUCCESS;
	// for (Map.Entry<Integer, CmnClass.TimeRangeCfg> entry :
	// finance_source_time_range_table.entrySet())
	// {
	//// LinkedList<String> data_list = new LinkedList<String>();
	// int method_index = entry.getKey();
	// MarketDataHandler finance_recorder_data_handler = new
	// MarketDataHandler(CmnDef.FinanceSourceType.valueOf(method_index));
	// CmnClass.TimeRangeCfg time_range_cfg = entry.getValue();
	//
	// CmnLogger.format_debug("Try to read data [%s %s] from MySQL......",
	// finance_recorder_data_handler.get_description(),
	// time_range_cfg.toString());
	//// Read the data from MySQL
	// ret = finance_recorder_data_handler.read_from_sql(time_range_cfg, "*",
	// result_set);
	// if (CmnDef.CheckFailure(ret))
	//// If the database does NOT exist, just ignore the warning
	// if (!CmnDef.CheckMySQLFailureUnknownDatabase(ret))
	// return ret;
	// }
	// return ret;
	// }
	//
	// public short clear(int method_index)
	// {
	// MarketDataHandler finance_recorder_data_handler = new
	// MarketDataHandler(CmnDef.FinanceSourceType.valueOf(method_index));
	// CmnLogger.format_debug("Try to delete database [%s]......",
	// finance_recorder_data_handler.get_description());
	// return finance_recorder_data_handler.delete_sql();
	// }
	//
	// public short clear_multi(LinkedList<Integer> method_index_list)
	// {
	// short ret = CmnDef.RET_SUCCESS;
	// for (Integer method_index : method_index_list)
	// {
	// ret = clear(method_index);
	// if (CmnDef.CheckFailure(ret))
	// return ret;
	// }
	//
	// return ret;
	// }
	//
	// public short clear_all()
	// {
	// short ret = CmnDef.RET_SUCCESS;
	// for (int method_index = 0 ; method_index <
	// CmnDef.FinanceSourceType.values().length ; method_index++)
	// {
	// ret = clear(method_index);
	// if (CmnDef.CheckFailure(ret))
	// return ret;
	// }
	//
	// return ret;
	// }
	//
	// public short check(boolean check_error)
	// {
	//// The class of keeping track of the relation between data type index and
	// the database start date
	// class DatabaseStartDateCfg implements Comparable
	// {
	// public java.util.Date database_start_date;
	// public int method_index;
	//// private LinkedList<String> date_list;
	//
	// public DatabaseStartDateCfg(java.util.Date date, int index)
	// {
	// database_start_date = date;
	// method_index = index;
	//// date_list = new LinkedList<String>();
	// }
	// public final java.util.Date get_start_date(){return database_start_date;}
	// @Override
	// public int compareTo(Object other)
	// {
	// return
	// database_start_date.compareTo(((DatabaseStartDateCfg)other).get_start_date());
	// }
	// @Override
	// public String toString()
	// {
	// return String.format("%s: %s",
	// CmnDef.FINANCE_DATA_DESCRIPTION_LIST[method_index],
	// CmnDef.get_date_str(database_start_date));
	// }
	// };
	//
	// short ret = CmnDef.RET_SUCCESS;
	// ArrayList<DatabaseStartDateCfg> database_start_date_cfg_list = new
	// ArrayList<DatabaseStartDateCfg>();
	//// Get the time range for each database
	// String total_time_range_str = "";
	// for (int method_index = 0 ; method_index <
	// CmnDef.FinanceSourceType.values().length ; method_index++)
	// {
	// MarketDataHandler finance_recorder_data_handler = new
	// MarketDataHandler(CmnDef.FinanceSourceType.valueOf(method_index));
	// ret = finance_recorder_data_handler.find_database_time_range();
	// if (CmnDef.CheckFailure(ret))
	// return ret;
	// StringBuilder database_start_date_builder = new StringBuilder();
	// finance_recorder_data_handler.get_database_start_date(database_start_date_builder);
	// try
	// {
	// java.util.Date date_str =
	// CmnDef.get_date(database_start_date_builder.toString());
	// database_start_date_cfg_list.add(new DatabaseStartDateCfg(date_str,
	// method_index));
	// }
	// catch (ParseException e)
	// {
	// CmnLogger.format_debug("Fail to transform the MySQL time format, due to:
	// %s", e.toString());
	// ret = CmnDef.RET_FAILURE_MYSQL;
	// }
	// StringBuilder time_range_str_builder = new StringBuilder();
	// finance_recorder_data_handler.get_database_time_range_str(time_range_str_builder);
	// total_time_range_str += String.format("%s\n",
	// time_range_str_builder.toString());
	// CmnLogger.format_debug("Database[%s] start date: %s",
	// finance_recorder_data_handler.get_description(),
	// database_start_date_builder.toString());
	// }
	//// Write the time range into the config file
	// String conf_filepath = String.format("%s/%s/%s",
	// CmnDef.get_current_path(), CmnDef.CONF_FOLDERNAME,
	// CmnDef.DATABASE_TIME_RANGE_CONF_FILENAME);
	// ret = CmnDef.direct_string_to_output_stream(total_time_range_str,
	// conf_filepath);
	// if (CmnDef.CheckFailure(ret))
	// return ret;
	// if (CmnDef.is_show_console())
	// CmnDef.direct_string_to_output_stream(total_time_range_str);
	//
	//// Copy the database time range config file to the FinanceAnalyzer project
	// String current_path = CmnDef.get_current_path();
	// String working_folder = current_path.substring(0,
	// current_path.lastIndexOf("/"));
	// String dst_folderpath = String.format("%s/%s/%s", working_folder,
	// CmnDef.DATABASE_TIME_RANGE_FILE_DST_PROJECT_NAME,
	// CmnDef.CONF_FOLDERNAME);
	// File f = new File(dst_folderpath);
	// if (f.exists())
	// {
	// String src_filepath = String.format("%s/%s/%s", current_path,
	// CmnDef.CONF_FOLDERNAME, CmnDef.DATABASE_TIME_RANGE_CONF_FILENAME);
	// String dst_filepath = String.format("%s/%s", dst_folderpath,
	// CmnDef.DATABASE_TIME_RANGE_CONF_FILENAME);
	// ret = CmnDef.copy_file(src_filepath, dst_filepath);
	// if (CmnDef.CheckFailure(ret))
	// return ret;
	// }
	//
	// if (!check_error)
	// return ret;
	//// Start to check error in the database
	//// Sort the data by date
	// Collections.sort(database_start_date_cfg_list);
	//// Compare the date list in each database
	//// LinkedList<String> data_base_list = null;
	// String[] data_base_array = null;
	// int data_base_array_size = 0;
	// int data_base_method_index = -1;
	//// database_start_date_cfg_list.remove(0);
	// String base_database_print_str = null;
	// for (DatabaseStartDateCfg database_start_date_cfg :
	// database_start_date_cfg_list)
	// {
	// MarketDataHandler finance_recorder_data_handler = new
	// MarketDataHandler(CmnDef.FinanceSourceType.valueOf(database_start_date_cfg.method_index));
	// if (data_base_array == null)
	// {
	//// data_base_list = new LinkedList<String>();
	// CmnClass.ResultSet base_result_set = new CmnClass.ResultSet();
	//// ret = finance_recorder_data_handler.find_database_time_range();
	// ret = finance_recorder_data_handler.read_from_sql(
	// new
	// CmnClass.TimeRangeCfg(CmnDef.get_date_str(database_start_date_cfg.database_start_date),
	// CmnDef.get_time_date_today()),
	// CmnDef.FINANCE_DATA_SQL_FIELD_DEFINITION_LIST[database_start_date_cfg.method_index][0],
	// base_result_set
	// );
	// if (CmnDef.CheckFailure(ret))
	// return ret;
	//// Get the data from the ResultSet data structure
	// ArrayList<String> date_data_array =
	// base_result_set.get_date_array().get_data_array();
	// data_base_array = new String[date_data_array.size()];
	// date_data_array.toArray(data_base_array);
	// base_database_print_str = String.format("The base database: %s\n",
	// finance_recorder_data_handler.get_description());
	// data_base_array_size = data_base_array.length;
	// data_base_method_index = database_start_date_cfg.method_index;
	// }
	// else
	// {
	// CmnClass.ResultSet compare_result_set = new CmnClass.ResultSet();
	// ret = finance_recorder_data_handler.read_from_sql(
	// new
	// CmnClass.TimeRangeCfg(CmnDef.get_date_str(database_start_date_cfg.database_start_date),
	// CmnDef.get_time_date_today()),
	// CmnDef.FINANCE_DATA_SQL_FIELD_DEFINITION_LIST[database_start_date_cfg.method_index][0],
	// compare_result_set
	// );
	// if (CmnDef.CheckFailure(ret))
	// return ret;
	//// Get the data from the ResultSet data structure
	// ArrayList<String> date_data_array =
	// compare_result_set.get_date_array().get_data_array();
	// String[] data_compare_array = new String[date_data_array.size()];
	// date_data_array.toArray(data_compare_array);
	// int data_compare_array_size = data_compare_array.length;
	// String start_compare_date = data_compare_array[0];
	//// int start_index = data_base_array.indexOf(start_compare_date);
	// int start_index = -1;
	//// Find the start index in the base array
	// for (int i = 0 ; i < data_base_array_size ; i++)
	// {
	// if (data_base_array[i].equals(start_compare_date))
	// {
	// start_index = i;
	// break;
	// }
	// }
	// if (start_index == -1)
	// {
	// CmnLogger.format_error("Fail to find Database[%s] start date",
	// finance_recorder_data_handler.get_description());
	// return CmnDef.RET_FAILURE_MYSQL_DATA_NOT_CONSISTENT;
	// }
	//// ArrayList<String> sub_data_base_array =
	// data_base_array.subList(start_index, data_base_array_size);
	//// Check if the sizes are identical
	// int sub_data_base_array_size = data_base_array_size - start_index;
	// if (sub_data_base_array_size != data_compare_array_size)
	// {
	// if (base_database_print_str != null)
	// {
	// CmnLogger.error(base_database_print_str);
	// if(CmnDef.is_show_console())
	// System.err.println(base_database_print_str);
	// base_database_print_str = null;
	// }
	// String err_result = String.format("The size in NOT identical, %s: %d, %s:
	// %d",
	// CmnDef.FINANCE_DATA_DESCRIPTION_LIST[database_start_date_cfg.method_index],
	// data_compare_array_size,
	// CmnDef.FINANCE_DATA_DESCRIPTION_LIST[data_base_method_index],
	// sub_data_base_array_size
	// );
	// CmnLogger.format_error(err_result);
	// if(CmnDef.is_show_console())
	// System.err.println(err_result);
	//// return CmnDef.RET_FAILURE_MYSQL_DATA_NOT_CONSISTENT;
	//// Show the error detail
	// String base_data, compare_data;
	// for (int i = 0 ; i < sub_data_base_array_size ; i++)
	// {
	// base_data = data_base_array[i + start_index];
	// compare_data = data_compare_array[i];
	// while (compare_data.compareTo(base_data) != 0)
	// {
	// String err_result_detail = String.format("Date NOT Found %s: %s",
	// CmnDef.FINANCE_DATA_DESCRIPTION_LIST[database_start_date_cfg.method_index],
	// base_data
	// );
	// CmnLogger.format_error(err_result_detail);
	// if(CmnDef.is_show_console())
	// System.err.println(err_result_detail);
	//// return CmnDef.RET_FAILURE_MYSQL_DATA_NOT_CONSISTENT;
	// }
	// }
	// }
	//// if (sub_data_base_list.size() != compare_result_set.size())
	//// {
	//// if (base_database_print_str != null)
	//// {
	//// CmnLogger.error(base_database_print_str);
	//// if(CmnDef.is_show_console())
	//// System.err.println(base_database_print_str);
	//// base_database_print_str = null;
	//// }
	//// String err_result = String.format("The size in NOT identical, %s: %d,
	// %s: %d",
	//// CmnDef.FINANCE_DATA_DESCRIPTION_LIST[database_start_date_cfg.method_index],
	//// compare_result_set.size(),
	//// CmnDef.FINANCE_DATA_DESCRIPTION_LIST[data_base_method_index],
	//// sub_data_base_list.size()
	//// );
	//// CmnLogger.format_error(err_result);
	//// if(CmnDef.is_show_console())
	//// System.err.println(err_result);
	////// return CmnDef.RET_FAILURE_MYSQL_DATA_NOT_CONSISTENT;
	////// Show the error detail
	//// ListIterator<String> base_iter = sub_data_base_list.listIterator();
	//// ListIterator<String> compare_iter = compare_result_set.listIterator();
	//// String base_data, compare_data;
	//// while (base_iter.hasNext())
	//// {
	//// base_data = base_iter.next();
	//// compare_data = compare_iter.next();
	//// while (compare_data.compareTo(base_data) != 0)
	//// {
	//// String err_result_detail = String.format("Date NOT Found %s: %s",
	//// CmnDef.FINANCE_DATA_DESCRIPTION_LIST[database_start_date_cfg.method_index],
	//// base_data
	//// );
	//// CmnLogger.format_error(err_result_detail);
	//// if(CmnDef.is_show_console())
	//// System.err.println(err_result_detail);
	//// base_data = base_iter.next();
	////// return CmnDef.RET_FAILURE_MYSQL_DATA_NOT_CONSISTENT;
	//// }
	//// }
	//// }
	//
	//// if (!sub_data_base_list.equals(compare_result_set))
	//// {
	//// CmnLogger.format_error(
	//// "Date in Database(%s, %s) are NOT identical",
	//// CmnDef.FINANCE_DATA_SQL_FIELD_DEFINITION_LIST[data_base_method_index],
	//// CmnDef.FINANCE_DATA_SQL_FIELD_DEFINITION_LIST[database_start_date_cfg.method_index]
	//// );
	//// return CmnDef.RET_FAILURE_MYSQL_DATA_NOT_CONSISTENT;
	//// }
	// }
	//// System.out.println(database_start_date);
	// }
	// return ret;
	// }
	//
	// public short run_daily()
	// {
	// if (workday_calendar == null)
	// {
	// CmnLogger.error("The workday calendar object is NOT initialized");
	// return CmnDef.RET_FAILURE_INCORRECT_OPERATION;
	// }
	//
	//// Find the latest workday
	// short ret = CmnDef.RET_SUCCESS;
	// int[] date_list = new int[3];
	// ret = workday_calendar.get_last_workday(date_list);
	// if (CmnDef.CheckFailure(ret))
	// return ret;
	// int year = date_list[0], month = date_list[1], day = date_list[2];
	// CmnLogger.format_debug("The workday: %04d-%02d-%02d", year, month, day);
	// int[] prev_date_list = new int[3];
	// ret = workday_calendar.get_prev_workday(year, month, day,
	// prev_date_list);
	// if (CmnDef.CheckFailure(ret))
	// return ret;
	// int prev_year = prev_date_list[0], prev_month = prev_date_list[1],
	// prev_day = prev_date_list[2];
	// CmnLogger.format_debug("The previous workday: %04d-%02d-%02d", prev_year,
	// prev_month, prev_day);
	/// *
	// * 臺股指數及成交量
	// 成交金額(2), 發行量加權股價指數(4), 漲跌點數(5)
	// * 三大法人現貨買賣超
	// 自營商(自行買賣)_買賣差額(3), 自營商(避險)_買賣差額(6), 投信_買賣差額(9), 外資及陸資_買賣差額(12)
	// * 現貨融資融券餘額
	// 融資金額(仟元)_前日餘額(14), 融資金額(仟元)_今日餘額(15)
	// * 三大法人期貨和選擇權留倉淨額
	// 自營商_多空淨額_契約金額(6), 投信_多空淨額_契約金額(12), 外資_多空淨額_契約金額(18)
	// * 三大法人選擇權賣權買權比
	// 買賣權未平倉量比率%(6)
	// * 三大法人選擇權買賣權留倉淨額
	// 買權_自營商_買方_口數(1), 買權_自營商_賣方_口數(3), 買權_外資_買方_口數(13), 買權_外資_賣方_口數(15),
	// 賣權_自營商_買方_口數(19), 賣權_自營商_賣方_口數(21), 賣權_外資_買方_口數(31), 賣權_外資_賣方_口數(33)
	// * 十大交易人及特定法人期貨資訊
	// 臺股期貨_到期月份_買方_前十大交易人合計_部位數(3), 臺股期貨_到期月份_賣方_前十大交易人合計_部位數(7),
	// 臺股期貨_所有契約_買方_前十大交易人合計_部位數(12), 臺股期貨_所有契約_賣方_前十大交易人合計_部位數(16)
	// */
	// CmnClass.QuerySet query_set = new CmnClass.QuerySet();
	// CmnClass.TimeRangeCfg time_range_cfg = new
	// CmnClass.TimeRangeCfg(prev_year, prev_month, prev_day, year, month, day);
	// CmnClass.ResultSet result_set = new CmnClass.ResultSet();
	//// Add the database field we are interested in
	// CmnClass.add_query(query_set,
	// CmnDef.FinanceSourceType.FinanceSource_StockExchangeAndVolume, 2);
	// CmnClass.add_query(query_set,
	// CmnDef.FinanceSourceType.FinanceSource_StockExchangeAndVolume, 4);
	// CmnClass.add_query(query_set,
	// CmnDef.FinanceSourceType.FinanceSource_StockExchangeAndVolume, 5);
	// CmnClass.add_query(query_set,
	// CmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell,
	// 3);
	// CmnClass.add_query(query_set,
	// CmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell,
	// 6);
	// CmnClass.add_query(query_set,
	// CmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell,
	// 9);
	// CmnClass.add_query(query_set,
	// CmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell,
	// 12);
	// CmnClass.add_query(query_set,
	// CmnDef.FinanceSourceType.FinanceSource_StockMarginTradingAndShortSelling,
	// 14);
	// CmnClass.add_query(query_set,
	// CmnDef.FinanceSourceType.FinanceSource_StockMarginTradingAndShortSelling,
	// 15);
	// CmnClass.add_query(query_set,
	// CmnDef.FinanceSourceType.FinanceSource_FutureAndOptionTop3LegalPersonsOpenInterest,
	// 6);
	// CmnClass.add_query(query_set,
	// CmnDef.FinanceSourceType.FinanceSource_FutureAndOptionTop3LegalPersonsOpenInterest,
	// 12);
	// CmnClass.add_query(query_set,
	// CmnDef.FinanceSourceType.FinanceSource_FutureAndOptionTop3LegalPersonsOpenInterest,
	// 18);
	// CmnClass.add_query(query_set,
	// CmnDef.FinanceSourceType.FinanceSource_OptionPutCallRatio, 6);
	// CmnClass.add_query(query_set,
	// CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest,
	// 1);
	// CmnClass.add_query(query_set,
	// CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest,
	// 3);
	// CmnClass.add_query(query_set,
	// CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest,
	// 13);
	// CmnClass.add_query(query_set,
	// CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest,
	// 15);
	// CmnClass.add_query(query_set,
	// CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest,
	// 19);
	// CmnClass.add_query(query_set,
	// CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest,
	// 21);
	// CmnClass.add_query(query_set,
	// CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest,
	// 31);
	// CmnClass.add_query(query_set,
	// CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest,
	// 33);
	// CmnClass.add_query(query_set,
	// CmnDef.FinanceSourceType.FinanceSource_FutureTop10DealersAndLegalPersons,
	// 3);
	// CmnClass.add_query(query_set,
	// CmnDef.FinanceSourceType.FinanceSource_FutureTop10DealersAndLegalPersons,
	// 7);
	// CmnClass.add_query(query_set,
	// CmnDef.FinanceSourceType.FinanceSource_FutureTop10DealersAndLegalPersons,
	// 12);
	// CmnClass.add_query(query_set,
	// CmnDef.FinanceSourceType.FinanceSource_FutureTop10DealersAndLegalPersons,
	// 16);
	// query_set.add_query_done();
	//
	//// Query the data from MySQL
	// ret = query(time_range_cfg, query_set, result_set);
	// if (CmnDef.CheckFailure(ret))
	// return ret;
	//// Show the result
	// ret = result_set.show_data();
	// if (CmnDef.CheckFailure(ret))
	// return ret;
	//
	//// Write into file
	// CmnClass.TimeCfg time_cfg = new CmnClass.TimeCfg(year, month, day);
	// ret = show_daily(time_cfg, result_set, SHOW_RESULT_FILE |
	// SHOW_RESULT_EMAIL);
	// if (CmnDef.CheckFailure(ret))
	// return ret;
	// return ret;
	// }
	//
	// public short show_daily(CmnClass.TimeCfg time_cfg, final
	// CmnClass.ResultSet result_set, int show_result_type)
	// {
	// short ret = CmnDef.RET_SUCCESS;
	// // Check the folder of keeping track of the result exist
	// ret =
	// CmnDef.create_folder_in_project_if_not_exist(CmnDef.RESULT_FOLDERNAME);
	// if (CmnDef.CheckFailure(ret))
	// return ret;
	//
	// String buf_string = "";
	//// Assemble the data
	// buf_string += String.format("日期: %04d-%02d-%02d\n", time_cfg.get_year(),
	// time_cfg.get_month(), time_cfg.get_day());
	// buf_string += String.format("發行量加權股價指數: %.2f, 漲跌: %.2f, 成交金額(億): %.2f,
	// 變化(億): %.2f\n\n",
	// result_set.get_float_array_element(CmnDef.FinanceSourceType.FinanceSource_StockExchangeAndVolume.value(),
	// 4, 1),
	// result_set.get_float_array_element(CmnDef.FinanceSourceType.FinanceSource_StockExchangeAndVolume.value(),
	// 5, 1),
	// result_set.get_long_array_element(CmnDef.FinanceSourceType.FinanceSource_StockExchangeAndVolume.value(),
	// 2, 1) / 100000000.0,
	// (result_set.get_long_array_element(CmnDef.FinanceSourceType.FinanceSource_StockExchangeAndVolume.value(),
	// 2, 1) -
	// result_set.get_long_array_element(CmnDef.FinanceSourceType.FinanceSource_StockExchangeAndVolume.value(),
	// 2, 0)) / 100000000.0
	// );
	// buf_string += String.format("三大法人買賣超(億)\n外資及陸資: %.2f, 變化: %.2f\n投信: %.2f,
	// 變化: %.2f\n自營商: %.2f, 變化: %.2f\n三大法人: %.2f, 變化: %.2f\n\n",
	// result_set.get_long_array_element(CmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell.value(),
	// 12, 1) / 100000000.0,
	// (result_set.get_long_array_element(CmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell.value(),
	// 12, 1) -
	// result_set.get_long_array_element(CmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell.value(),
	// 12, 0)) / 100000000.0,
	// result_set.get_long_array_element(CmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell.value(),
	// 9, 1) / 100000000.0,
	// (result_set.get_long_array_element(CmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell.value(),
	// 9, 1) -
	// result_set.get_long_array_element(CmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell.value(),
	// 9, 0)) / 100000000.0,
	// (result_set.get_long_array_element(CmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell.value(),
	// 3, 1) +
	// result_set.get_long_array_element(CmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell.value(),
	// 6, 1)) / 100000000.0,
	// (result_set.get_long_array_element(CmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell.value(),
	// 3, 1) +
	// result_set.get_long_array_element(CmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell.value(),
	// 6, 1) -
	// result_set.get_long_array_element(CmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell.value(),
	// 3, 0) -
	// result_set.get_long_array_element(CmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell.value(),
	// 6, 0)) / 100000000.0,
	// (result_set.get_long_array_element(CmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell.value(),
	// 3, 1) +
	// result_set.get_long_array_element(CmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell.value(),
	// 6, 1) +
	// result_set.get_long_array_element(CmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell.value(),
	// 9, 1) +
	// result_set.get_long_array_element(CmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell.value(),
	// 12, 1)) / 100000000.0,
	// (result_set.get_long_array_element(CmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell.value(),
	// 3, 1) +
	// result_set.get_long_array_element(CmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell.value(),
	// 6, 1) +
	// result_set.get_long_array_element(CmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell.value(),
	// 9, 1) +
	// result_set.get_long_array_element(CmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell.value(),
	// 12, 1) -
	// result_set.get_long_array_element(CmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell.value(),
	// 3, 0) -
	// result_set.get_long_array_element(CmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell.value(),
	// 6, 0) -
	// result_set.get_long_array_element(CmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell.value(),
	// 9, 0) -
	// result_set.get_long_array_element(CmnDef.FinanceSourceType.FinanceSource_StockTop3LegalPersonsNetBuyOrSell.value(),
	// 12, 0)) / 100000000.0
	// );
	// buf_string += String.format("融資餘額(億): %.2f, 變化: %.2f\n\n",
	// result_set.get_long_array_element(CmnDef.FinanceSourceType.FinanceSource_StockMarginTradingAndShortSelling.value(),
	// 15, 1) / 100000.0,
	// (result_set.get_long_array_element(CmnDef.FinanceSourceType.FinanceSource_StockMarginTradingAndShortSelling.value(),
	// 15, 1) -
	// result_set.get_long_array_element(CmnDef.FinanceSourceType.FinanceSource_StockMarginTradingAndShortSelling.value(),
	// 14, 1)) / 100000.0
	// );
	// buf_string += String.format("三大法人期權留倉淨額(億)\n外資: %d, 變化: %d\n投信: %d, 變化:
	// %d\n自營商: %d, 變化: %d\n三大法人: %d, 變化: %d\n\n",
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_FutureAndOptionTop3LegalPersonsOpenInterest.value(),
	// 18, 1) / 100.0,
	// (result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_FutureAndOptionTop3LegalPersonsOpenInterest.value(),
	// 18, 1) -
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_FutureAndOptionTop3LegalPersonsOpenInterest.value(),
	// 18, 0)) / 100.0,
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_FutureAndOptionTop3LegalPersonsOpenInterest.value(),
	// 12, 1) / 100.0,
	// (result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_FutureAndOptionTop3LegalPersonsOpenInterest.value(),
	// 12, 1) -
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_FutureAndOptionTop3LegalPersonsOpenInterest.value(),
	// 12, 0)) / 100.0,
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_FutureAndOptionTop3LegalPersonsOpenInterest.value(),
	// 6, 1) / 100.0,
	// (result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_FutureAndOptionTop3LegalPersonsOpenInterest.value(),
	// 6, 1) -
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_FutureAndOptionTop3LegalPersonsOpenInterest.value(),
	// 6, 0)) / 100.0,
	// (result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_FutureAndOptionTop3LegalPersonsOpenInterest.value(),
	// 6, 1) +
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_FutureAndOptionTop3LegalPersonsOpenInterest.value(),
	// 12, 1) +
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_FutureAndOptionTop3LegalPersonsOpenInterest.value(),
	// 18, 1)) / 100.0,
	// (result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_FutureAndOptionTop3LegalPersonsOpenInterest.value(),
	// 6, 1) +
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_FutureAndOptionTop3LegalPersonsOpenInterest.value(),
	// 12, 1) +
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_FutureAndOptionTop3LegalPersonsOpenInterest.value(),
	// 18, 1) -
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_FutureAndOptionTop3LegalPersonsOpenInterest.value(),
	// 6, 0) -
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_FutureAndOptionTop3LegalPersonsOpenInterest.value(),
	// 12, 0) -
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_FutureAndOptionTop3LegalPersonsOpenInterest.value(),
	// 18, 0)) / 100.0
	// );
	// buf_string += String.format("未平倉Put/Call Ratio: %.2f, 變化: %.2f\n\n",
	// result_set.get_float_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionPutCallRatio.value(),
	// 6, 1),
	// result_set.get_float_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionPutCallRatio.value(),
	// 6, 1) -
	// result_set.get_float_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionPutCallRatio.value(),
	// 6, 0)
	// );
	// buf_string += String.format("選擇權買賣權留倉口數\n外資\n Buy Call: %d, 變化: %d\n Buy
	// Put: %d, 變化: %d\n Sell Call: %d, 變化: %d\n Sell Put: %d, 變化: %d\n 多方: %d,
	// 變化: %d\n 空方: %d, 變化: %d\n自營商\n Buy Call: %d, 變化: %d\n Buy Put: %d, 變化:
	// %d\n Sell Call: %d, 變化: %d\n Sell Put: %d, 變化: %d\n 多方: %d, 變化: %d\n 空方:
	// %d, 變化: %d\n\n",
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(),
	// 13, 1),
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(),
	// 13, 1) -
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(),
	// 13, 0),
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(),
	// 15, 1),
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(),
	// 15, 1) -
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(),
	// 15, 0),
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(),
	// 31, 1),
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(),
	// 31, 1) -
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(),
	// 31, 0),
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(),
	// 33, 1),
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(),
	// 33, 1) -
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(),
	// 33, 0),
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(),
	// 13, 1) +
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(),
	// 33, 1),
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(),
	// 13, 1) +
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(),
	// 33, 1) -
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(),
	// 13, 0) -
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(),
	// 33, 0),
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(),
	// 15, 1) +
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(),
	// 31, 1),
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(),
	// 15, 1) +
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(),
	// 31, 1) -
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(),
	// 15, 0) -
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(),
	// 31, 0),
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(),
	// 1, 1),
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(),
	// 1, 1) -
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(),
	// 1, 0),
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(),
	// 3, 1),
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(),
	// 3, 1) -
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(),
	// 3, 0),
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(),
	// 19, 1),
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(),
	// 19, 1) -
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(),
	// 19, 0),
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(),
	// 21, 1),
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(),
	// 21, 1) -
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(),
	// 21, 0),
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(),
	// 1, 1) +
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(),
	// 21, 1),
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(),
	// 1, 1) +
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(),
	// 21, 1) -
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(),
	// 1, 0) -
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(),
	// 21, 0),
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(),
	// 3, 1) +
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(),
	// 19, 1),
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(),
	// 3, 1) +
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(),
	// 19, 1) -
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(),
	// 3, 0) -
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_OptionTop3LegalPersonsBuyAndSellOptionOpenInterest.value(),
	// 19, 0)
	// );
	// buf_string += String.format("十大交易人及特法留倉淨口數\n近月: %d, 變化: %d\n全月: %d, 變化:
	// %d\n\n",
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_FutureTop10DealersAndLegalPersons.value(),
	// 3, 1) -
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_FutureTop10DealersAndLegalPersons.value(),
	// 7, 1),
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_FutureTop10DealersAndLegalPersons.value(),
	// 3, 1) -
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_FutureTop10DealersAndLegalPersons.value(),
	// 7, 1) -
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_FutureTop10DealersAndLegalPersons.value(),
	// 3, 0) +
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_FutureTop10DealersAndLegalPersons.value(),
	// 7, 0),
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_FutureTop10DealersAndLegalPersons.value(),
	// 12, 1) -
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_FutureTop10DealersAndLegalPersons.value(),
	// 16, 1),
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_FutureTop10DealersAndLegalPersons.value(),
	// 12, 1) -
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_FutureTop10DealersAndLegalPersons.value(),
	// 16, 1) -
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_FutureTop10DealersAndLegalPersons.value(),
	// 12, 0) +
	// result_set.get_int_array_element(CmnDef.FinanceSourceType.FinanceSource_FutureTop10DealersAndLegalPersons.value(),
	// 16, 0)
	// );
	//
	//// SHow result on the screen
	// if ((show_result_type & SHOW_RESULT_STDOUT) != 0)
	// {
	//// Write the data into STDOUT
	// ret = CmnDef.direct_string_to_output_stream(buf_string);
	// if (CmnDef.CheckFailure(ret))
	// return ret;
	// }
	//// Write the data into file
	// if ((show_result_type & SHOW_RESULT_FILE) != 0)
	// {
	// String filename = String.format(CmnDef.DAILY_FINANCE_FILENAME_FORMAT,
	// time_cfg.get_year(), time_cfg.get_month(), time_cfg.get_day());
	// String filepath = String.format("%s/%s/%s", CmnDef.get_current_path(),
	// CmnDef.RESULT_FOLDERNAME, filename);
	// CmnLogger.format_debug("Write daily data to file[%s]", filepath);
	// ret = CmnDef.direct_string_to_output_stream(buf_string, filepath);
	// if (CmnDef.CheckFailure(ret))
	// return ret;
	//// System.out.printf("Check the result in file: %s\n", filepath);
	// }
	//// Send the result by email
	// if ((show_result_type & SHOW_RESULT_EMAIL) != 0)
	// {
	// String title = String.format(CmnDef.DAILY_FINANCE_EMAIL_TITLE_FORMAT,
	// time_cfg.get_year(), time_cfg.get_month(), time_cfg.get_day());
	// for (String email_address : email_address_list)
	// {
	// if (email_address.isEmpty())
	// continue;
	// CmnLogger.format_debug("Write daily data by email[%s] to %s", title,
	// email_address);
	// ret = CmnDef.send_email(title, email_address, buf_string);
	// if (CmnDef.CheckFailure(ret))
	// return ret;
	// }
	// }
	// return CmnDef.RET_SUCCESS;
	// }
	//
	// public short backup_by_multithread(boolean copy_backup_folder)
	// {
	// short ret = CmnDef.RET_SUCCESS;
	// String backup_folderpath = String.format("%s/%s",
	// CmnDef.get_current_path(), CmnDef.BACKUP_FOLDERNAME);
	//// Create a folder for MySQL backup if not exist
	// ret = CmnDef.create_folder_if_not_exist(backup_folderpath);
	// if (CmnDef.CheckFailure(ret))
	// return ret;
	//// Create a folder for storing CSV files
	//// Define the folder name due to current time
	// java.util.Date date_now = new java.util.Date();
	// Calendar cal = Calendar.getInstance();
	// cal.setTime(date_now);
	// String csv_backup_foldername = String.format("%02d%02d%02d%02d%02d%02d",
	// cal.get(Calendar.YEAR) % 100, cal.get(Calendar.MONTH) + 1,
	// cal.get(Calendar.DATE), cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE),
	// cal.get(Calendar.SECOND));
	// CmnLogger.format_debug("Create the folder for CSV backup file: %s",
	// csv_backup_foldername);
	// String csv_backup_folderpath = String.format("%s/%s", backup_folderpath,
	// csv_backup_foldername);
	// ret = CmnDef.create_folder(csv_backup_folderpath);
	// if (CmnDef.CheckFailure(ret))
	// return ret;
	//// Get the time range table/source field for backup
	// finance_backup_source_time_range_table = new HashMap<Integer,
	// CmnClass.TimeRangeCfg>();
	// finance_backup_source_field_table = new HashMap<Integer,
	// LinkedList<Integer>>();
	//// ret =
	// update_full_time_range_table(finance_backup_source_time_range_table);
	// ret = update_backup_by_config_file(CmnDef.BACKUP_FILENAME,
	// finance_backup_source_time_range_table,
	// finance_backup_source_field_table);
	// if (CmnDef.CheckFailure(ret))
	// return ret;
	//
	//// Create the worker threads for backup
	// ThreadPoolExecutor executor =
	// (ThreadPoolExecutor)Executors.newFixedThreadPool(CmnDef.MAX_CONCURRENT_THREAD);
	// LinkedList<Future<Integer>> res_list = new LinkedList<Future<Integer>>();
	// OUT:
	// do
	// {
	// for (Map.Entry<Integer, CmnClass.TimeRangeCfg> entry :
	// finance_backup_source_time_range_table.entrySet())
	// {
	// int method_index = entry.getKey();
	// CmnClass.TimeRangeCfg time_range_cfg = entry.getValue();
	//// Restrict the search time range
	// CmnClass.TimeRangeCfg restrict_time_range_cfg = new
	// CmnClass.TimeRangeCfg(time_range_cfg.get_start_time_str(),
	// time_range_cfg.get_end_time_str());
	// database_time_range.restrict_time_range(method_index,
	// restrict_time_range_cfg);
	//
	//// Split the time range into several smaller time range slice
	// ArrayList<CmnClass.TimeRangeCfg> time_range_slice_cfg_list = new
	// ArrayList<CmnClass.TimeRangeCfg>();
	// ret = get_time_range_slice(restrict_time_range_cfg,
	// CmnDef.BACKUP_SQL_MAX_MONTH_RANGE_IN_THREAD, time_range_slice_cfg_list);
	// if (CmnDef.CheckFailure(ret))
	// break OUT;
	//
	// for (CmnClass.TimeRangeCfg time_range_slice_cfg :
	// time_range_slice_cfg_list)
	// {
	//// Backup the data from MySQL
	// MarketDataHandler finance_recorder_data_handler = new
	// MarketDataHandler(CmnDef.FinanceSourceType.valueOf(method_index));
	// CmnLogger.format_debug("Try to backup data [%s %04d%02d:%04d%02d] from
	// MySQL......", finance_recorder_data_handler.get_description(),
	// time_range_slice_cfg.get_start_time().get_year(),
	// time_range_slice_cfg.get_start_time().get_month(),
	// time_range_slice_cfg.get_end_time().get_year(),
	// time_range_slice_cfg.get_end_time().get_month());
	// StockBackupSQLTask task = new StockBackupSQLTask(new
	// MarketDataHandler(CmnDef.FinanceSourceType.valueOf(method_index)),
	// time_range_slice_cfg, csv_backup_foldername,
	// finance_backup_source_field_table.get(method_index));
	// Future<Integer> res = executor.submit(task);
	// res_list.add(res);
	// }
	// }
	//// Check the result
	// for (Future<Integer> res : res_list)
	// {
	// try
	// {
	// ret = res.get().shortValue();
	// }
	// catch (ExecutionException e)
	// {
	// CmnLogger.format_error("Fail to get return value, due to: %s",
	// e.toString());
	// ret = CmnDef.RET_FAILURE_UNKNOWN;
	// }
	// catch (InterruptedException e)
	// {
	// CmnLogger.format_error("Fail to get return value, due to: %s",
	// e.toString());
	// ret = CmnDef.RET_FAILURE_INCORRECT_OPERATION;
	// }
	// if (CmnDef.CheckFailure(ret))
	// break OUT;
	// }
	// }while(false);
	//// Shut down the executor
	// if(CmnDef.CheckSuccess(ret))
	// executor.shutdown();
	// else
	// executor.shutdownNow();
	//
	//// Backup the workday calendar/database time range config files
	// String current_path = CmnDef.get_current_path();
	// String workday_canlendar_src_filepath = String.format("%s/%s/%s",
	// current_path, CmnDef.CONF_FOLDERNAME,
	// CmnDef.WORKDAY_CANLENDAR_CONF_FILENAME);
	// String database_time_range_src_filepath = String.format("%s/%s/%s",
	// current_path, CmnDef.CONF_FOLDERNAME,
	// CmnDef.DATABASE_TIME_RANGE_CONF_FILENAME);
	//
	// String dst_folderpath = String.format("%s/%s/%s", current_path,
	// CmnDef.BACKUP_FOLDERNAME, csv_backup_foldername);
	//// Copy the workday calendar config file
	// CmnLogger.format_debug("Copy workday calendar config file[%s] to %s",
	// CmnDef.WORKDAY_CANLENDAR_CONF_FILENAME, csv_backup_foldername);
	// File workday_calendar_file_handle = new
	// File(workday_canlendar_src_filepath);
	// if (!workday_calendar_file_handle.exists())
	// {
	// CmnLogger.format_error("The workday calendar config file[%s] does NOT
	// exist", CmnDef.WORKDAY_CANLENDAR_CONF_FILENAME);
	// return CmnDef.RET_FAILURE_NOT_FOUND;
	// }
	// ret = CmnDef.copy_file(workday_canlendar_src_filepath,
	// String.format("%s/%s", dst_folderpath,
	// CmnDef.WORKDAY_CANLENDAR_CONF_FILENAME));
	// if (CmnDef.CheckFailure(ret))
	// return ret;
	//// Copy the database time range config file
	// CmnLogger.format_debug("Copy database time range config file[%s] to %s",
	// CmnDef.DATABASE_TIME_RANGE_CONF_FILENAME, csv_backup_foldername);
	// File database_time_range_file_handle = new
	// File(database_time_range_src_filepath);
	// if (!database_time_range_file_handle.exists())
	// {
	// CmnLogger.format_error("The database time range config file[%s] does NOT
	// exist", CmnDef.DATABASE_TIME_RANGE_CONF_FILENAME);
	// return CmnDef.RET_FAILURE_NOT_FOUND;
	// }
	// ret = CmnDef.copy_file(database_time_range_src_filepath,
	// String.format("%s/%s", dst_folderpath,
	// CmnDef.DATABASE_TIME_RANGE_CONF_FILENAME));
	// if (CmnDef.CheckFailure(ret))
	// return ret;
	//
	// if (copy_backup_folder)
	// {
	// CmnDef.delete_subfolder(CmnDef.COPY_BACKUP_FOLDERPATH);
	// CmnLogger.format_debug("Copy backup folder[%s] to %s",
	// csv_backup_foldername, CmnDef.COPY_BACKUP_FOLDERPATH);
	// ret = CmnDef.copy_folder(dst_folderpath, CmnDef.COPY_BACKUP_FOLDERPATH);
	// if (CmnDef.CheckFailure(ret))
	// return ret;
	// }
	//
	// return ret;
	// }
	//
}
