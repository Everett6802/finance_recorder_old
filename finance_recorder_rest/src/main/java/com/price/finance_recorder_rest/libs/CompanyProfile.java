package com.price.finance_recorder_rest.libs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import com.price.finance_recorder_rest.common.CmnDef;
import com.price.finance_recorder_rest.common.CmnFunc;
import com.price.finance_recorder_rest.common.CmnLogger;


public class CompanyProfile 
{
	// Some constants related to the company profile entry
		static final public int COMPANY_PROFILE_ENTRY_FIELD_INDEX_COMPANY_CODE_NUMBER = 0;
		static final public int COMPANY_PROFILE_ENTRY_FIELD_INDEX_COMPANY_NAME = 1;
		static final public int COMPANY_PROFILE_ENTRY_FIELD_INDEX_LISTING_DATE = 3;
		static final public int COMPANY_PROFILE_ENTRY_FIELD_INDEX_MARKET = 4;
		static final public int COMPANY_PROFILE_ENTRY_FIELD_INDEX_INDUSTRY = 5;
		static final public int COMPANY_PROFILE_ENTRY_FIELD_INDEX_GROUP_NAME = 7;
		static final public int COMPANY_PROFILE_ENTRY_FIELD_INDEX_GROUP_NUMBER = 8;
		static final public int COMPANY_PROFILE_ENTRY_FIELD_SIZE = 9;

		static final private int COMPANY_GROUP_ENTRY_FIELD_SIZE = 2;

		private static class CompanyProfileEntry implements Comparable<CompanyProfileEntry>
		{
			public ArrayList<String> profile_element_array = null;
			public CompanyProfileEntry()
			{
				profile_element_array = new ArrayList<String>();
			}

			public void add(final String new_element){profile_element_array.add(new_element);}

			public String get(int index)
			{
				return profile_element_array.get(index);
			}

			@Override
			public int compareTo(CompanyProfileEntry another) 
			{
				return profile_element_array.get(COMPANY_PROFILE_ENTRY_FIELD_INDEX_COMPANY_CODE_NUMBER).compareTo(another.profile_element_array.get(COMPANY_PROFILE_ENTRY_FIELD_INDEX_COMPANY_CODE_NUMBER));
			}

			@Override
			public String toString()
			{
				Iterator<String> iter = profile_element_array.iterator();
				String res = "";
				while(iter.hasNext())
				{
					res += String.format("%s ", iter.next());
				}
				return res;
			}
		};

		public static class TraverseEntry implements Iterable<ArrayList<String>>
		{
			private ArrayList<CompanyProfileEntry> company_profile_sorted_list = null;

			public TraverseEntry(ArrayList<CompanyProfileEntry> new_array)
			{
				company_profile_sorted_list = new_array;
			}
			@Override
			public Iterator<ArrayList<String>> iterator()
			{
				Iterator<ArrayList<String>> it = new Iterator<ArrayList<String>>()
				{
					private int array_size = company_profile_sorted_list.size();
					private int cur_index = 0;
					@Override
					public boolean hasNext()
					{
						return cur_index < array_size;
					}
					@Override
					public ArrayList<String> next()
					{
						return company_profile_sorted_list.get(cur_index++).profile_element_array;
					}
					@Override
					public void remove() {throw new UnsupportedOperationException();}
				};
				return it;
			}
		};

		private CompanyProfile(){}
		public Object clone() throws CloneNotSupportedException {throw new CloneNotSupportedException();}

		private static CompanyProfile instance = null;
		public static CompanyProfile get_instance()
		{
			if (instance == null)
				allocate();
			return instance;
		}
		private static synchronized void allocate() // For thread-safe
		{
			if (instance == null)
			{
				instance = new CompanyProfile();
				short ret = instance.initialize();
				if (CmnDef.CheckFailure(ret))
				{
					String errmsg = String.format("Fail to initialize the CompanyProfile object , due to: %s", CmnDef.GetErrorDescription(ret));
					throw new RuntimeException(errmsg);
				}
			} 
		}
//		private static synchronized void release() // For thread-safe
//		{
//			if (instance != null)
//				instance = null;
//		}

		TreeMap<String, CompanyProfileEntry> company_profile_map = null;
		ArrayList<String> company_group_description_list = null;
		ArrayList<CompanyProfileEntry> company_profile_sorted_list = null;
		ArrayList<ArrayList<CompanyProfileEntry>> company_group_profile_sorted_list = null;
		int company_group_size = 0;

		private short initialize()
		{
			short ret = CmnDef.RET_SUCCESS;
			ret = parse_company_profile_conf();
			if (CmnDef.CheckFailure(ret))
				return ret;
			ret = parse_company_group_conf();
			if (CmnDef.CheckFailure(ret))
				return ret;
//			ret = generate_company_profile_sorted_list();
//			if (CmnDef.CheckFailure(ret))
//				return ret;
//			ret = generate_company_group_profile_sorted_list();
//			if (CmnDef.CheckFailure(ret))
//				return ret;
			return CmnDef.RET_SUCCESS;
		}

		private short parse_company_profile_conf()
		{
			company_profile_map = new TreeMap<String, CompanyProfileEntry>();
	// Open the file
			short ret = CmnDef.RET_SUCCESS;
			LinkedList<String> config_line_list = new LinkedList<String>();
			ret = CmnFunc.read_dataset_config_file_lines(CmnDef.COMPANY_PROFILE_CONF_FILENAME, config_line_list);
			if (CmnDef.CheckFailure(ret))
				return ret;
	OUT:
			for (String line : config_line_list)
			{
			// Check if the source type in the config file is in order
				String data_array[] = line.split(CmnDef.COMMA_DATA_SPLIT);
				if (data_array.length != COMPANY_PROFILE_ENTRY_FIELD_SIZE)
				{
					CmnLogger.format_error("Incorrect config format: %s", line);
					ret = CmnDef.RET_FAILURE_INCORRECT_CONFIG;
					break OUT;
				}
				CompanyProfileEntry company_profile_entry = new CompanyProfileEntry();
				for (String data : data_array)
					company_profile_entry.add(data);
				company_profile_map.put(company_profile_entry.get(COMPANY_PROFILE_ENTRY_FIELD_INDEX_COMPANY_CODE_NUMBER), company_profile_entry);
			}
			return ret;
		}

		private short parse_company_group_conf()
		{
			company_group_description_list = new ArrayList<String>();
// Open the file
			short ret = CmnDef.RET_SUCCESS;
			LinkedList<String> config_line_list = new LinkedList<String>();
			ret = CmnFunc.read_dataset_config_file_lines(CmnDef.COMPANY_GROUP_CONF_FILENAME, config_line_list);
			if (CmnDef.CheckFailure(ret))
				return ret;
			int line_cnt = 0;
			String group_number = null;
			String group_description = null;
	OUT:
			for (String line : config_line_list)
			{
	// Check if the source type in the config file is in order
				String data_array[] = line.split(CmnDef.SPACE_DATA_SPLIT);
				if (data_array.length != COMPANY_GROUP_ENTRY_FIELD_SIZE)
				{
					CmnLogger.format_error("Incorrect config format: %s", line);
					ret = CmnDef.RET_FAILURE_INCORRECT_CONFIG;
					break OUT;
				}
				group_number = data_array[0];
				group_description = data_array[1];
				if (Integer.valueOf(group_number) != line_cnt)
				{
					CmnLogger.format_error("Incorrect company group number, expected: %d, actual: %d", line_cnt, Integer.valueOf(group_number));
					ret = CmnDef.RET_FAILURE_INVALID_ARGUMENT;
					break OUT;		
				}
				company_group_description_list.add(group_description);
				line_cnt++;
			}
			assert(company_group_description_list.size() == line_cnt) : "The company_group_description_list size is NOT correct";
			company_group_size = line_cnt;
			return ret;
		}

		private short generate_company_profile_sorted_list()
		{
			if (company_profile_sorted_list != null)
				return CmnDef.RET_FAILURE_INCORRECT_OPERATION;
			company_profile_sorted_list = new ArrayList<CompanyProfileEntry>();

			for (Map.Entry<String, CompanyProfileEntry> entry : company_profile_map.entrySet())
			{
				CompanyProfileEntry company_profile_entry = entry.getValue();
				company_profile_sorted_list.add(company_profile_entry);
			}

//			System.out.printf("size: %d\n", company_profile_sorted_list.size());
//			Collections.sort(company_profile_sorted_list);
//			for (CompanyProfileEntry company_profile_entry : company_profile_sorted_list)
//				System.out.println(company_profile_entry.toString());

			return CmnDef.RET_SUCCESS;
		}

		private short generate_company_group_profile_sorted_list()
		{
			if (company_group_profile_sorted_list != null)
				return CmnDef.RET_FAILURE_INCORRECT_OPERATION;
			company_group_profile_sorted_list = new ArrayList<ArrayList<CompanyProfileEntry>>();

			for (int i = 0 ; i < company_group_size ; i++)
			{
				ArrayList<CompanyProfileEntry> company_profile_array = new ArrayList<CompanyProfileEntry>();
				company_group_profile_sorted_list.add(company_profile_array);
			}
			for (Map.Entry<String, CompanyProfileEntry> entry : company_profile_map.entrySet())
			{
				CompanyProfileEntry company_profile_entry = entry.getValue();
				String company_group_number = company_profile_entry.get(COMPANY_PROFILE_ENTRY_FIELD_INDEX_GROUP_NUMBER);
				company_group_profile_sorted_list.get(Integer.valueOf(company_group_number)).add(company_profile_entry);
			}
	// Sort the data by company code number in each group 
			for (int i = 0 ; i < company_group_size ; i++)
			{
				ArrayList<CompanyProfileEntry> company_profile_array = company_group_profile_sorted_list.get(i);
				Collections.sort(company_profile_array);
//				System.out.printf("++++++++++++++++ group [%d] member count: %d\n", i, company_profile_array.size());
//				for (CompanyProfileEntry company_profile_entry : company_profile_array)
//					System.out.println(company_profile_entry.toString());
			}

			return CmnDef.RET_SUCCESS;
		}

		private static synchronized void init_company_profile_sorted_deque()
		{
			if (instance.company_profile_sorted_list == null)
			{
				short ret = instance.generate_company_profile_sorted_list();
				if (CmnDef.CheckFailure(ret))
					throw new RuntimeException(String.format("%s", CmnDef.GetErrorDescription(ret)));
			}
		}

		private static synchronized void init_company_group_profile_sorted_deque()
		{
			if (instance.company_group_profile_sorted_list == null)
			{
				short ret = instance.generate_company_group_profile_sorted_list();
				if (CmnDef.CheckFailure(ret))
					throw new RuntimeException(String.format("%s", CmnDef.GetErrorDescription(ret)));
			}
		}

//		@Override
//		public Iterator<ArrayList<String>> iterator()
//		{
//			Iterator<ArrayList<String>> it = new Iterator<ArrayList<String>>()
//			{
//				private int array_size = company_profile_sorted_list.size();
//				private int cur_index = 0;
//				@Override
//				public boolean hasNext()
//				{
//					return cur_index < array_size;
//				}
//				@Override
//				public ArrayList<String> next()
//				{
//					return company_profile_sorted_list.get(cur_index++).profile_element_array;
//				}
//				@Override
//				public void remove() {throw new UnsupportedOperationException();}
//			};
//			return it;
//		}

		public TraverseEntry entry()
		{
			if (company_profile_sorted_list == null) init_company_profile_sorted_deque();
			return new TraverseEntry(company_profile_sorted_list);
		}

		public TraverseEntry group_entry(int index)
		{
			if (company_group_profile_sorted_list == null) init_company_group_profile_sorted_deque();
			return new TraverseEntry(company_group_profile_sorted_list.get(index));
		}

		public int get_company_group_size(){return company_group_size;}

		public String get_company_group_description(int index) throws IllegalArgumentException
		{
			if (index < 0 || index >= company_group_size)
				throw new IllegalArgumentException("index is Out Of Range");
			return company_group_description_list.get(index);
		}

		public CompanyProfileEntry lookup_company_profile(String company_number)
		{
			CompanyProfileEntry company_profile = company_profile_map.get(company_number);
			if (company_profile == null)
				throw new IllegalArgumentException(String.format("Fail to find the company profile of company number: %s", company_number));
			return company_profile;
		}

		public String lookup_company_listing_date(String company_number){return lookup_company_profile(company_number).get(COMPANY_PROFILE_ENTRY_FIELD_INDEX_LISTING_DATE);}
		public String lookup_company_group_name(String company_number){return lookup_company_profile(company_number).get(COMPANY_PROFILE_ENTRY_FIELD_INDEX_GROUP_NAME);}
		public Integer lookup_company_group_number(String company_number){return Integer.valueOf(lookup_company_profile(company_number).get(COMPANY_PROFILE_ENTRY_FIELD_INDEX_GROUP_NUMBER));}

}
