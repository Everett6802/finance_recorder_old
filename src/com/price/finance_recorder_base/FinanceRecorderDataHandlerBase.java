package com.price.finance_recorder_base;

//import java.util.HashMap;
import java.util.LinkedList;
import com.price.finance_recorder_cmn.FinanceRecorderClassBase;
//import com.price.finance_recorder_cmn.FinanceRecorderCmnClass;
import com.price.finance_recorder_cmn.FinanceRecorderCmnDef;


public abstract class FinanceRecorderDataHandlerBase extends FinanceRecorderClassBase implements FinanceRecorderDataHandlerInf
{
	protected LinkedList<Integer> source_type_index_list = null;
	protected String finance_root_folerpath = FinanceRecorderCmnDef.CSV_ROOT_FOLDERPATH;
	protected String finance_root_backup_folerpath = FinanceRecorderCmnDef.BACKUP_CSV_ROOT_FOLDERPATH;

	public void set_finance_root_folerpath(String foldername){finance_root_folerpath = foldername;}
	public void set_finance_root_backup_folerpath(String backup_foldername){finance_root_backup_folerpath = backup_foldername;}
}
