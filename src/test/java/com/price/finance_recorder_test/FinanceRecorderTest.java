package com.price.finance_recorder_test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;
import com.price.finance_recorder.FinanceRecorder;
import com.price.finance_recorder.FinanceRecorderCmnDef;
import com.price.finance_recorder.FinanceRecorderCmnDef.FinanceAnalysisMode;
import com.price.finance_recorder.FinanceRecorderCmnDef.DeleteSQLAccurancyType;
import com.price.finance_recorder.FinanceRecorderCmnDef.DefaultConstType;


public class FinanceRecorderTest 
{
	@Test
	public void test_set_market_mode()
	{
		short ret = FinanceRecorder.set_market_mode();
		assertEquals(ret, FinanceRecorderCmnDef.RET_SUCCESS);
	}

	@Test
	public void test_set_stock_mode()
	{
		short ret = FinanceRecorder.set_stock_mode();
		assertEquals(ret, FinanceRecorderCmnDef.RET_SUCCESS);
	}

	@Test
	public void test_set_mode_from_cfg()
	{
		short ret = FinanceRecorder.set_mode_from_cfg();
		assertEquals(ret, FinanceRecorderCmnDef.RET_SUCCESS);
	}

	@Test
	public void test_get_backup_foldername_list()
	{
		List<String> backup_foldername_list = new ArrayList<String>();
		short ret = FinanceRecorder.get_backup_foldername_list(backup_foldername_list);
		assertEquals(ret, FinanceRecorderCmnDef.RET_SUCCESS);
	}

	@Test
	public void test_get_restore_foldername_list()
	{
		List<String> restore_foldername_list = new ArrayList<String>();
		short ret = FinanceRecorder.get_restore_foldername_list(restore_foldername_list);
		assertEquals(ret, FinanceRecorderCmnDef.RET_SUCCESS);
	}

	@Test
	public void test_check_company_profile_change()
	{
		String new_company_profile_config_folderpath = "/home/super/Projects/finance_scrapy/conf";
		ArrayList<String> lost_company_number_list = new ArrayList<String>();
		ArrayList<String> new_company_number_list = new ArrayList<String>();
		short ret = FinanceRecorder.check_company_profile_change(new_company_profile_config_folderpath, lost_company_number_list, new_company_number_list);
		if (FinanceRecorderCmnDef.CheckFailure(ret) && FinanceRecorderCmnDef.CheckFailureNotFound(ret))
			System.err.println("The company profile is missing......");
		assertEquals(ret, FinanceRecorderCmnDef.RET_SUCCESS);
	}

	@Test
	public void test_check_statement_profile_change()
	{
		String statement_method_word_list_string = "9-12"; 
		String new_statement_profile_config_folderpath = "/home/super/Projects/finance_scrapy/conf"; 
		HashMap<Integer, ArrayList<String>> lost_statement_profile_map = new HashMap<Integer, ArrayList<String>>();
		HashMap<Integer, ArrayList<String>> new_statement_profile_map = new HashMap<Integer, ArrayList<String>>();
		short ret = FinanceRecorder.check_statement_profile_change(statement_method_word_list_string, new_statement_profile_config_folderpath, lost_statement_profile_map, new_statement_profile_map);
		if (FinanceRecorderCmnDef.CheckFailure(ret) && FinanceRecorderCmnDef.CheckFailureNotFound(ret))
			System.err.println("Some statement profiles are missing......");
		assertEquals(ret, FinanceRecorderCmnDef.RET_SUCCESS);
	}
}
