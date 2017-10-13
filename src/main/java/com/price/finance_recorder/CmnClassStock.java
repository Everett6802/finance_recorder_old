package com.price.finance_recorder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.price.finance_recorder.CmnClass.QuerySet;


public class CmnClassStock 
{
	static class CompanyGroupSet implements Iterable<Map.Entry<Integer, ArrayList<String>>>
	{
		private static CompanyProfile company_profile = null;
		private static HashMap<Integer, ArrayList<String>> whole_company_number_in_group_map;
		private static ArrayList<String> whole_company_number_list = null;
		private HashMap<Integer, ArrayList<String>> company_number_in_group_map = null;
		private HashMap<Integer, ArrayList<String>> altered_company_number_in_group_map = null;
		private boolean is_add_done = false;
		private int company_amount = -1;

		private static CompanyProfile get_company_profile()
		{
			if (company_profile == null)
				company_profile = CompanyProfile.get_instance();
			return company_profile;
		}

		private static void init_whole_company_number_in_group_map()
		{
			assert whole_company_number_in_group_map == null : "whole_company_number_in_group_map is NOT null";

			whole_company_number_in_group_map = new HashMap<Integer, ArrayList<String>>();
			int company_group_size = get_company_profile().get_company_group_size();
			for (int i = 0 ; i < company_group_size ; i++)
			{
				CompanyProfile.TraverseEntry traverse_entry = company_profile.group_entry(i);
				ArrayList<String> company_number_array = new ArrayList<String>();
				for (ArrayList<String> entry : traverse_entry)
					company_number_array.add(entry.get(CompanyProfile.COMPANY_PROFILE_ENTRY_FIELD_INDEX_COMPANY_CODE_NUMBER));
				whole_company_number_in_group_map.put(i, company_number_array);
			}
		}

		private static void init_whole_company_number_list()
		{
			assert whole_company_number_list == null : "whole_company_number_list is NOT null";

			whole_company_number_list = new ArrayList<String>();
			CompanyProfile.TraverseEntry traverse_entry = get_company_profile().entry();
			for (ArrayList<String> entry : traverse_entry)
			{
				whole_company_number_list.add(entry.get(CompanyProfile.COMPANY_PROFILE_ENTRY_FIELD_INDEX_COMPANY_CODE_NUMBER));
			}
		}

		public static final HashMap<Integer, ArrayList<String>> get_whole_company_number_in_group_map()
		{
			if (whole_company_number_in_group_map == null)
				init_whole_company_number_in_group_map();
			return whole_company_number_in_group_map;
		}

		public static final ArrayList<String> get_whole_company_number_list()
		{
			if (whole_company_number_list == null)
				init_whole_company_number_list();
			return whole_company_number_list;
		}

		public static CompanyGroupSet get_whole_company_group_set()
		{
			if (whole_company_number_in_group_map == null)
				init_whole_company_number_in_group_map();
			CompanyGroupSet company_group_set = new CompanyGroupSet();
			company_group_set.add_done();
			return company_group_set;
		}

		public CompanyGroupSet(){}

		@Override
		public Iterator<Map.Entry<Integer, ArrayList<String>>> iterator()
		{
			if (!is_add_done)
			{
				String errmsg = "The add_done flag is NOT set to True";
				CmnLogger.format_error(errmsg);
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

		public short add_company_in_group_list(int company_group_number, List<String> company_code_number_in_group_array)
		{
			if (is_add_done)
			{
				CmnLogger.format_error("The add_done flag has already been set to True");
				return CmnDef.RET_FAILURE_INCORRECT_OPERATION;
			}
			if (company_number_in_group_map == null)
				company_number_in_group_map = new HashMap<Integer, ArrayList<String>>();
			if (!company_number_in_group_map.containsKey(company_group_number))
			{
				ArrayList<String> company_number_deque = new ArrayList<String>();
				company_number_in_group_map.put(company_group_number, company_number_deque);
			}
			else
			{
				if (company_number_in_group_map.get(company_group_number) == null)
				{
					CmnLogger.format_error("The company group[%d] has already been set to NULL", company_group_number);
					return CmnDef.RET_FAILURE_INCORRECT_OPERATION;
				}
			}
			for (String company_code_number : company_code_number_in_group_array)
			{
				if (company_number_in_group_map.get(company_group_number).indexOf(company_code_number) != -1)
				{
					CmnLogger.format_warn("The company code number[%s] has already been added to the group[%d]", company_code_number, company_group_number);
					continue;
				}
				company_number_in_group_map.get(company_group_number).add(company_code_number);
			}
			return CmnDef.RET_SUCCESS;
		}

		public short add_company_list(List<String> company_code_number_list)
		{
			if (is_add_done)
			{
				CmnLogger.format_error("The add_done flag has already been set to True");
				return CmnDef.RET_FAILURE_INCORRECT_OPERATION;
			}
	// Categorize the company code number in the list into correct group
			HashMap<Integer, ArrayList<String>> company_number_in_group_map_tmp = new HashMap<Integer, ArrayList<String>>();
			for (String company_code_number : company_code_number_list)
			{
				int company_group_number = get_company_profile().lookup_company_group_number(company_code_number);
				if (!company_number_in_group_map_tmp.containsKey(company_group_number))
					company_number_in_group_map_tmp.put(company_group_number, new ArrayList<String>());
				company_number_in_group_map_tmp.get(company_group_number).add(company_code_number);
			}
			short ret = CmnDef.RET_SUCCESS;
	// Add data by group
			for (Map.Entry<Integer, ArrayList<String>> entry : company_number_in_group_map_tmp.entrySet())
			{
				int company_group_number = entry.getKey();
				ArrayList<String> company_code_number_in_group_list = entry.getValue();
				ret = add_company_in_group_list(company_group_number, company_code_number_in_group_list);
				if (CmnDef.CheckFailure(ret))
					return ret;
			}
			return ret;
		}

		public short add_company(int company_group_number, String company_code_number)
		{
			ArrayList<String> company_code_number_in_group_array = new ArrayList<String>();
			company_code_number_in_group_array.add(company_code_number);
			return add_company_in_group_list(company_group_number, company_code_number_in_group_array);
		}

		public short add_company(String company_code_number)
		{
			int company_group_number = get_company_profile().lookup_company_group_number(company_code_number);
			return add_company(company_group_number, company_code_number);
		}

		public short add_company_group(int company_group_number)
		{
			if (is_add_done)
			{
				CmnLogger.format_error("The add_done flag has already been set to True");
				return CmnDef.RET_FAILURE_INCORRECT_OPERATION;
			}

			if (company_number_in_group_map == null)
				company_number_in_group_map = new HashMap<Integer, ArrayList<String>>();

			if (company_number_in_group_map.containsKey(company_group_number))
			{
				if (company_number_in_group_map.get(company_group_number) != null)
					CmnLogger.format_warn("Select all company group[%d], ignore the original settings......", company_group_number);
			}
			company_number_in_group_map.put(company_group_number, null);
			return CmnDef.RET_SUCCESS;
		}

		public short add_done()
		{
			if (is_add_done)
			{
				CmnLogger.format_error("The add_done flag has already been set to True");
				return CmnDef.RET_FAILURE_INCORRECT_OPERATION;
			}
			setup_for_traverse();
			is_add_done = true;
			return CmnDef.RET_SUCCESS;
		}

		final ArrayList<String> get_company_number_in_group_list(int company_group_index)
		{
			if (!is_add_done)
			{
				String errmsg = "The add_done flag is NOT set to True";
				CmnLogger.error(errmsg);
				throw new IllegalStateException(errmsg);
			}
			if (!altered_company_number_in_group_map.containsKey(company_group_index))
			{
				String errmsg = String.format("The company group index[%d] is NOT found in data structure", company_group_index);
				CmnLogger.error(errmsg);
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
				altered_company_number_in_group_map = new HashMap<Integer, ArrayList<String>>();
				for (Map.Entry<Integer, ArrayList<String>> entry : company_number_in_group_map.entrySet())
					altered_company_number_in_group_map.put(entry.getKey(), (entry.getValue() != null) ? entry.getValue() : whole_company_number_in_group_map.get(entry.getKey()));
			}
		}

		public int get_company_amount()
		{
			if (!is_add_done)
			{
				String errmsg = "The add_done flag is NOT set to True";
				CmnLogger.format_error(errmsg);
				throw new IllegalStateException(errmsg);
			}
			if (company_amount == -1)
			{
				company_amount = 0;
				for (Map.Entry<Integer, ArrayList<String>> entry : altered_company_number_in_group_map.entrySet())
				{
					ArrayList<String> company_number_list = entry.getValue();
					company_amount += company_number_list.size();
				}
			}
			return company_amount;
		}

		public int get_cur_company_amount()
		{
			int cur_company_amount = 0;
			if (altered_company_number_in_group_map != null)
			{
				for (Map.Entry<Integer, ArrayList<String>> entry : altered_company_number_in_group_map.entrySet())
				{
					ArrayList<String> company_number_list = entry.getValue();
					company_amount += company_number_list.size();
				}
			}
			return cur_company_amount;
		}

		public ArrayList<CompanyGroupSet> get_sub_company_group_set_list(int sub_company_group_set_amount)
		{
			if (!is_add_done)
			{
				String errmsg = "The add_done flag is NOT set to True";
				CmnLogger.error(errmsg);
				throw new IllegalStateException(errmsg);
			}
			if (sub_company_group_set_amount <= 0)
				throw new IllegalArgumentException("sub_company_group_set_amount should be larger than 0");
			ArrayList<CompanyGroupSet> sub_company_group_set_list = new ArrayList<CompanyGroupSet>();
			ArrayList<Integer> sub_company_group_set_amount_list = new ArrayList<Integer>();
			int sub_company_amount = 0;
			int rest_company_amount = 0;
			if (get_company_amount() <= sub_company_group_set_amount)
			{
				sub_company_amount = 1;
				rest_company_amount = 0;
				sub_company_group_set_amount = get_company_amount();
				CmnLogger.format_debug("The company amount is less than sub company group set amount. Set the sub company group set amount to %d", get_company_amount());
			}
			else
			{
				sub_company_amount = get_company_amount() / sub_company_group_set_amount;
				rest_company_amount = get_company_amount() % sub_company_group_set_amount;
			}
			CompanyGroupSet sub_company_group_set = null;
			int sub_company_group_cnt = 0;
			int sub_company_amount_in_group_threshold = 0;
			int sub_company_cnt = 0;
			for (Map.Entry<Integer, ArrayList<String>> entry : altered_company_number_in_group_map.entrySet())
			{
				Integer company_group_number = entry.getKey();
				ArrayList<String> company_code_number_list = entry.getValue();
				for (String company_code_number : company_code_number_list)
				{
					if (sub_company_cnt == 0)
					{
						sub_company_group_set = new CompanyGroupSet();
						sub_company_group_set_list.add(sub_company_group_set);
						sub_company_amount_in_group_threshold = (sub_company_group_cnt < rest_company_amount ? (sub_company_amount + 1) : sub_company_amount);
						sub_company_group_cnt += 1;
					}
					sub_company_group_set.add_company(company_group_number, company_code_number);
					sub_company_cnt += 1;
	// Add to another group
					if (sub_company_cnt == sub_company_amount_in_group_threshold)
						sub_company_cnt = 0;
				}
			}
			String company_amount_list_str = "Company Amount list for each sub group: ";
			for (int group_index = 0 ; group_index < sub_company_group_set_amount ; group_index++)
			{
				sub_company_group_set = sub_company_group_set_list.get(group_index);
				sub_company_group_set.add_done();
				sub_company_group_set_amount_list.add(sub_company_group_set.get_company_amount());
				company_amount_list_str += String.format("%d ", sub_company_group_set.get_company_amount());
			}
			CmnLogger.debug(company_amount_list_str);
			return sub_company_group_set_list;
		}
	}


	public class StockQuerySet extends QuerySet
	{
		protected CompanyGroupSet company_group_set;

		public StockQuerySet()
		{
			company_group_set = new CompanyGroupSet();
		}

		public short add_company_list(int company_group_number, final ArrayList<String> company_code_number_in_group_array)
		{
			return company_group_set.add_company_in_group_list(company_group_number, company_code_number_in_group_array);
		}

		public short add_company(int company_group_number, String company_code_number)
		{
			return company_group_set.add_company(company_group_number, company_code_number);
		}

		public short add_company_group(int company_group_number)
		{
			return company_group_set.add_company_group(company_group_number);
		}

		public final CompanyGroupSet get_company_group_set()
		{
			return company_group_set;
		}
	}
}
