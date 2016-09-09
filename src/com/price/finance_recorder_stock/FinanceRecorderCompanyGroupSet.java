package com.price.finance_recorder_stock;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import com.price.finance_recorder_cmn.FinanceRecorderCmnDef;


public class FinanceRecorderCompanyGroupSet implements Iterable<Map.Entry<Integer, ArrayList<String>>>
{
	private static TreeMap<Integer, ArrayList<String>> whole_company_number_in_group_map;
	private TreeMap<Integer, ArrayList<String>> company_number_in_group_map = null;
	private TreeMap<Integer, ArrayList<String>> altered_company_number_in_group_map = null;
	private boolean is_add_done = false;

	static void init_whole_company_number_in_group_map()
	{
		assert whole_company_number_in_group_map == null : "whole_company_number_in_group_map is NOT null";

		FinanceRecorderCmnClassCompanyProfile company_profile = FinanceRecorderCmnClassCompanyProfile.get_instance();
		whole_company_number_in_group_map = new TreeMap<Integer, ArrayList<String>>();
		int company_group_size = company_profile.get_company_group_size();
		for (int i = 0 ; i < company_group_size ; i++)
		{
			FinanceRecorderCmnClassCompanyProfile.TraverseEntry traverse_entry = company_profile.group_entry(i);
			ArrayList<String> company_number_array = new ArrayList<String>();
			for (ArrayList<String> entry : traverse_entry)
				company_number_array.add(entry.get(FinanceRecorderCmnClassCompanyProfile.COMPANY_PROFILE_ENTRY_FIELD_INDEX_COMPANY_CODE_NUMBER));
			whole_company_number_in_group_map.put(i, company_number_array);
		}
	}

	static public FinanceRecorderCompanyGroupSet get_whole_company_group_set()
	{
		FinanceRecorderCompanyGroupSet company_group_set = new FinanceRecorderCompanyGroupSet();
		company_group_set.add_done();
		return company_group_set;
	}

	public FinanceRecorderCompanyGroupSet(){}

	@Override
	public Iterator<Map.Entry<Integer, ArrayList<String>>> iterator()
	{
		if (!is_add_done)
		{
			String errmsg = "The add_done flag is NOT set to True";
			FinanceRecorderCmnDef.format_error(errmsg);
			throw new IllegalStateException(errmsg);
		}
		Iterator<Map.Entry<Integer, ArrayList<String>>> it = new Iterator<Map.Entry<Integer, ArrayList<String>>>()
		{
			private Iterator<Map.Entry<Integer, ArrayList<String>>> iter = altered_company_number_in_group_map.entrySet().iterator();
			@Override
			public boolean hasNext()
			{
				return iter.hasNext();
			}
			@Override
			public Map.Entry<Integer, ArrayList<String>> next()
			{
				return (Map.Entry<Integer, ArrayList<String>>)iter.next();
			}
			@Override
			public void remove() {throw new UnsupportedOperationException();}
		};
		return it;
	}

	public short add_company_list(int company_group_number, ArrayList<String> company_code_number_in_group_array)
	{
		if (is_add_done)
		{
			FinanceRecorderCmnDef.format_error("The add_done flag has already been set to True");
			return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
		}
		if (company_number_in_group_map == null)
			company_number_in_group_map = new TreeMap<Integer, ArrayList<String>>();
		if (!company_number_in_group_map.containsKey(company_group_number))
		{
			ArrayList<String> company_number_deque = new ArrayList<String>();
			company_number_in_group_map.put(company_group_number, company_number_deque);
		}
		else
		{
			if (company_number_in_group_map.get(company_group_number) == null)
			{
				FinanceRecorderCmnDef.format_error("The company group[%d] has already been set to NULL", company_group_number);
				return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
			}
		}
		for (String company_code_number : company_code_number_in_group_array)
		{
			if (company_number_in_group_map.get(company_group_number).indexOf(company_code_number) != -1)
			{
				FinanceRecorderCmnDef.format_warn("The company code number[%s] has already been added to the group[%d]", company_code_number, company_group_number);
				continue;
			}
			company_number_in_group_map.get(company_group_number).add(company_code_number);
		}
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	public short add_company(int company_group_number, String company_code_number)
	{
		ArrayList<String> company_code_number_in_group_array = new ArrayList<String>();
		company_code_number_in_group_array.add(company_code_number);
		return add_company_list(company_group_number, company_code_number_in_group_array);
	}

	public short add_company_group(int company_group_number)
	{
		if (is_add_done)
		{
			FinanceRecorderCmnDef.format_error("The add_done flag has already been set to True");
			return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
		}

		if (company_number_in_group_map == null)
			company_number_in_group_map = new TreeMap<Integer, ArrayList<String>>();

		if (company_number_in_group_map.containsKey(company_group_number))
		{
			if (company_number_in_group_map.get(company_group_number) != null)
				FinanceRecorderCmnDef.format_warn("Select all company group[%d], ignore the original settings......", company_group_number);
		}
		company_number_in_group_map.put(company_group_number, null);
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	public short add_done()
	{
		if (is_add_done)
		{
			FinanceRecorderCmnDef.format_error("The add_done flag has already been set to True");
			return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
		}
		setup_for_traverse();
		is_add_done = true;
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	final ArrayList<String> get_company_number_in_group_list(int company_group_index)
	{
		if (!is_add_done)
		{
			String errmsg = "The add_done flag is NOT set to True";
			FinanceRecorderCmnDef.format_error(errmsg);
			throw new IllegalStateException(errmsg);
		}
		if (!altered_company_number_in_group_map.containsKey(company_group_index))
		{
			String errmsg = String.format("The company group index[%d] is NOT found in data structure", company_group_index);
			FinanceRecorderCmnDef.format_error(errmsg);
			throw new IllegalArgumentException(errmsg);
		}
		return altered_company_number_in_group_map.get(company_group_index);
	}

	private void setup_for_traverse()
	{
		if (whole_company_number_in_group_map == null)
			init_whole_company_number_in_group_map();
		if (company_number_in_group_map == null)
			altered_company_number_in_group_map = whole_company_number_in_group_map;
		else
		{
			altered_company_number_in_group_map = new TreeMap<Integer, ArrayList<String>>();
			for (Map.Entry<Integer, ArrayList<String>> entry : company_number_in_group_map.entrySet())
				altered_company_number_in_group_map.put(entry.getKey(), (entry.getValue() != null) ? entry.getValue() : whole_company_number_in_group_map.get(entry.getKey()));
		}
	}
}
