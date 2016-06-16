package com.price.finance_recorder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;

public class FinanceRecorderCompanyProfileLookup
{
// Some constants related to the company profile entry
	static final private int COMPANY_PROFILE_ENTRY_FIELD_INDEX_COMPANY_CODE_NUMBER = 0;
	static final private int COMPANY_PROFILE_ENTRY_FIELD_INDEX_COMPANY_NAME = 1;
	static final private int COMPANY_PROFILE_ENTRY_FIELD_INDEX_LISTING_DATE = 3;
	static final private int COMPANY_PROFILE_ENTRY_FIELD_INDEX_MARKET = 4;
	static final private int COMPANY_PROFILE_ENTRY_FIELD_INDEX_INDUSTRY = 5;
	static final private int COMPANY_PROFILE_ENTRY_FIELD_INDEX_GROUP_NAME = 7;
	static final private int COMPANY_PROFILE_ENTRY_FIELD_INDEX_GROUP_NUMBER = 8;
	static final private int COMPANY_PROFILE_ENTRY_FIELD_SIZE = 9;

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

	
	private FinanceRecorderCompanyProfileLookup(){}
	public Object clone() throws CloneNotSupportedException {throw new CloneNotSupportedException();}

	private static FinanceRecorderCompanyProfileLookup instance = null;
	public static FinanceRecorderCompanyProfileLookup get_instance()
	{
		if (instance == null)
			allocate();
		return instance;
	}
	private static synchronized void allocate() // For thread-safe
	{
		if (instance == null)
		{
			instance = new FinanceRecorderCompanyProfileLookup();
			short ret = instance.initialize();
			if (FinanceRecorderCmnDef.CheckFailure(ret))
			{
				String errmsg = String.format("Fail to initialize the FinanceRecorderCompanyProfileLookup object , due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret));
				throw new RuntimeException(errmsg);
			}
		} 
	}
	private static synchronized void release() // For thread-safe
	{
		if (instance != null)
			instance = null;
	}

	TreeMap<String, CompanyProfileEntry> company_profile_map = null;
	ArrayList<CompanyProfileEntry> company_profile_sorted_array = null;

	private short initialize()
	{
		company_profile_map = new TreeMap<String, CompanyProfileEntry>();

// Open the file
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		BufferedReader reader = null;
		String conf_filepath = String.format("%s/%s/%s", FinanceRecorderCmnDef.get_current_path(), FinanceRecorderCmnDef.CONF_FOLDERNAME, FinanceRecorderCmnDef.COMPANY_PROFILE_FILENAME);
		FinanceRecorderCmnDef.format_debug("Try to parse the configuration in %s", conf_filepath);
// Check the file exists or not
		File fp = new File(conf_filepath);
		if (!fp.exists())
		{
			FinanceRecorderCmnDef.format_error("The configration file[%s] does NOT exist", conf_filepath);
			return FinanceRecorderCmnDef.RET_FAILURE_NOT_FOUND;
		}
// Try to parse the content of the config file
		try
		{
			reader = new BufferedReader(new FileReader(fp));
			String buf;
			OUT:
			while ((buf = reader.readLine()) != null)
			{
				if (buf.length() == 0)
					continue;
// Check if the source type in the config file is in order
				String data_array[] = buf.split(FinanceRecorderCmnDef.DATA_SPLIT);
				if (data_array.length != COMPANY_PROFILE_ENTRY_FIELD_SIZE)
				{
					FinanceRecorderCmnDef.format_error("Incorrect config format: %s", buf);
					ret = FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
					break OUT;
				}
				CompanyProfileEntry company_profile_entry = new CompanyProfileEntry();
				for (String data : data_array)
					company_profile_entry.add(data);
				company_profile_map.put(company_profile_entry.get(COMPANY_PROFILE_ENTRY_FIELD_INDEX_COMPANY_CODE_NUMBER), company_profile_entry);
			}
		}
		catch (IOException ex)
		{
			FinanceRecorderCmnDef.format_error("Error occur due to %s", ex.toString());
			ret = FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
		}
		finally 
		{
// Close the file
			if (reader != null)
			{
				try {reader.close();}
				catch (IOException e){}// nothing to do here except log the exception
			}
		}
		return ret;
	}

	public CompanyProfileEntry lookup_company_profile(String company_number)
	{
		CompanyProfileEntry company_profile = company_profile_map.get(company_number);
		if (company_profile == null)
			throw new IllegalArgumentException(String.format("Fail to find the company profile of company number: %s", company_number));
		return company_profile;
	}

	public String lookup_company_listing_date(String company_number)
	{
		return lookup_company_profile(company_number).get(COMPANY_PROFILE_ENTRY_FIELD_INDEX_LISTING_DATE);
	}

	public String lookup_company_group_name(String company_number)
	{
		return lookup_company_profile(company_number).get(COMPANY_PROFILE_ENTRY_FIELD_INDEX_GROUP_NAME);
	}

	public String lookup_company_group_number(String company_number)
	{
		return lookup_company_profile(company_number).get(COMPANY_PROFILE_ENTRY_FIELD_INDEX_GROUP_NUMBER);
	}

	private short generate_company_profile_sorted_deque()
	{
		if (company_profile_sorted_array != null)
			return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
		company_profile_sorted_array = new ArrayList<CompanyProfileEntry>();

		for (Map.Entry<String, CompanyProfileEntry> entry : company_profile_map.entrySet())
		{
			CompanyProfileEntry company_profile_entry = entry.getValue();
			company_profile_sorted_array.add(company_profile_entry);
		}

		System.out.printf("size: %d\n", company_profile_sorted_array.size());
		Collections.sort(company_profile_sorted_array);
		for (CompanyProfileEntry company_profile_entry : company_profile_sorted_array)
		{
			System.out.println(company_profile_entry.toString());
		}

		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

}
