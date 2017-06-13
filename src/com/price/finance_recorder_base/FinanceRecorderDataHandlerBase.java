package com.price.finance_recorder_base;

//import java.util.HashMap;
import java.util.LinkedList;
import com.price.finance_recorder_cmn.FinanceRecorderClassBase;
import com.price.finance_recorder_cmn.FinanceRecorderCmnDef;


public abstract class FinanceRecorderDataHandlerBase extends FinanceRecorderClassBase implements FinanceRecorderDataHandlerInf
{
	protected LinkedList<Integer> source_type_index_list = null;
	protected String current_csv_working_folerpath = null; // FinanceRecorderCmnDef.CSV_ROOT_FOLDERPATH;
	protected FinanceRecorderCmnDef.OperationType operation_type = FinanceRecorderCmnDef.OperationType.Operation_Continue;

	public void set_current_csv_working_folerpath(String csv_working_folerpath){current_csv_working_folerpath = csv_working_folerpath;}
	public void enable_operation_continue(boolean enable){operation_type = (enable ? FinanceRecorderCmnDef.OperationType.Operation_Continue : FinanceRecorderCmnDef.OperationType.Operation_Stop);}
	public boolean is_operation_continue(){return (operation_type == FinanceRecorderCmnDef.OperationType.Operation_Continue ? true : false);}
	public boolean operation_can_continue(short ret){return (is_operation_continue() && FinanceRecorderCmnDef.CheckFailureWarnProcessContinue(ret) ? true : false);}
	public boolean connect_mysql_can_continue(short ret){return (is_operation_continue() && FinanceRecorderCmnDef.CheckMySQLFailureUnknownDatabase(ret) ? true : false);}
	protected abstract short create_finance_folder_hierarchy(String root_folderpath);
	protected abstract short parse_missing_csv();
}
