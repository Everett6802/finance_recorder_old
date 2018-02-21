package com.price.finance_recorder_lib;

import java.util.LinkedList;


abstract class DataHandlerBase extends ClassCmnBase implements CmnInf.DataHandlerInf
{
	protected LinkedList<Integer> method_index_list = null;
	protected String current_csv_working_folerpath = null; // FinanceRecorderCmnDef.CSV_ROOT_FOLDERPATH;
	protected CmnDef.OperationType operation_type = CmnDef.OperationType.Operation_NonStop;
	protected CmnDef.CSVSourceLocationType csv_source_location_type = CmnDef.CSVSourceLocationType.CSVSourceLocation_Local;
	protected String remote_csv_server_ip = null;

	public void set_current_csv_working_folerpath(String csv_working_folerpath){current_csv_working_folerpath = csv_working_folerpath;}
	public void set_operation_non_stop(boolean enable){operation_type = (enable ? CmnDef.OperationType.Operation_NonStop : CmnDef.OperationType.Operation_Stop);}
	public boolean is_operation_non_stop(){return (operation_type == CmnDef.OperationType.Operation_NonStop ? true : false);}
	public void set_csv_remote_source(String server_ip) // disable remote source while server_ip = null 
	{
		if (server_ip == null)
		{
			csv_source_location_type = CmnDef.CSVSourceLocationType.CSVSourceLocation_Local;
			remote_csv_server_ip = null;
		}
		else
		{
			csv_source_location_type = CmnDef.CSVSourceLocationType.CSVSourceLocation_Remote;
			remote_csv_server_ip = server_ip;
		}
	}
	public boolean is_csv_remote_source(){return csv_source_location_type == CmnDef.CSVSourceLocationType.CSVSourceLocation_Remote ? true : false;}
	public boolean operation_can_continue(short ret){return (is_operation_non_stop() && CmnDef.CheckFailureWarnProcessContinue(ret) ? true : false);}
	public boolean connect_mysql_can_continue(short ret){return (is_operation_non_stop() && CmnDef.CheckMySQLFailureUnknownDatabase(ret) ? true : false);}
	protected abstract short create_finance_folder_hierarchy(String root_folderpath);
	protected abstract short parse_missing_csv();
}
