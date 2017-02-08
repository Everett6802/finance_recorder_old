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
	protected String finance_root_backup_folerpath = FinanceRecorderCmnDef.CSV_BACKUP_ROOT_FOLDERPATH;

	public void set_finance_root_folerpath(String folderpath){finance_root_folerpath = folderpath;}
	public void set_finance_root_backup_folerpath(String backup_folderpath){finance_root_backup_folerpath = backup_folderpath;}
	protected abstract short create_finance_folder_hierarchy(String root_folderpath);
	protected abstract short parse_missing_csv();
}
