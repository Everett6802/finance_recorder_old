package com.price.finance_recorder_stock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.HashMap;

import com.price.finance_recorder_cmn.FinanceRecorderCmnClass;
import com.price.finance_recorder_cmn.FinanceRecorderCmnDef;


public class FinanceRecorderDatabaseTimeRange 
{
	private static FinanceRecorderDatabaseTimeRange instance = null;
	public static FinanceRecorderDatabaseTimeRange get_instance()
	{
		if (instance == null)
			allocate();
		return instance;
	}
	private static synchronized void allocate() // For thread-safe
	{
		if (instance == null)
		{
			instance = new FinanceRecorderDatabaseTimeRange();
			short ret = instance.initialize();
			if (FinanceRecorderCmnDef.CheckFailure(ret))
			{
				String errmsg = String.format("Fail to initialize the FinanceRecorderDatabaseTimeRange object, due to: %s", FinanceRecorderCmnDef.GetErrorDescription(ret));
				throw new RuntimeException(errmsg);
			}
		} 
	}
	private static synchronized void release() // For thread-safe
	{
		if (instance != null)
			instance = null;
	}

	private FinanceRecorderDatabaseTimeRange(){}
	public Object clone() throws CloneNotSupportedException {throw new CloneNotSupportedException();}

	private HashMap<String, ArrayList<FinanceRecorderCmnClass.FinanceTimeRange>> database_time_range_map = null;

	private short parse_database_time_range_config()
	{
		int stock_source_type_amount = FinanceRecorderCmnDef.FinanceSourceType.get_stock_source_type_amount();
		database_time_range_map = new HashMap<String, ArrayList<FinanceRecorderCmnClass.FinanceTimeRange>>();
// Check if the config folder exist
		if (FinanceRecorderCmnDef.check_config_file_exist(FinanceRecorderCmnDef.STOCK_DATABASE_TIME_RANGE_CONF_FOLDERNAME))
		{
			FinanceRecorderCmnDef.format_warn("The database time range config folder[%s] does NOT exist", FinanceRecorderCmnDef.STOCK_DATABASE_TIME_RANGE_CONF_FOLDERNAME);
			return FinanceRecorderCmnDef.RET_FAILURE_NOT_FOUND;
		}
// Check if the config folder exist
		database_time_range_map = new HashMap<String, ArrayList<FinanceRecorderCmnClass.FinanceTimeRange>>();
		ArrayList<String> whole_company_number_list = FinanceRecorderCompanyGroupSet.get_whole_company_number_list();
// Assemble the config folder path of stock time range config files
		String conf_folderpath = String.format("%s/%s/%s", FinanceRecorderCmnDef.get_current_path(), FinanceRecorderCmnDef.CONF_FOLDERNAME, FinanceRecorderCmnDef.STOCK_DATABASE_TIME_RANGE_CONF_FOLDERNAME);
		FinanceRecorderCmnDef.format_error("Find the stock time range configurtion in the folder: %s", conf_folderpath);
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		OUT:
		for (String company_number : whole_company_number_list)
		{
// Check if the config file exist
			String conf_filename = String.format(FinanceRecorderCmnDef.STOCK_DATABASE_TIME_RANGE_CONF_FILENAME_FORMAT, company_number);
			if (FinanceRecorderCmnDef.check_config_file_exist(conf_filename, conf_folderpath))
			{
				FinanceRecorderCmnDef.format_warn("The database time range config file[%s] does NOT exist", FinanceRecorderCmnDef.MARKET_DATABASE_TIME_RANGE_CONF_FILENAME);
				database_time_range_map.put(company_number, null);
				continue OUT;
			}
			LinkedList<String> config_line_list = new LinkedList<String>();
			ret = FinanceRecorderCmnDef.get_config_file_lines(conf_filename, conf_folderpath, config_line_list);
			if (FinanceRecorderCmnDef.CheckFailure(ret))
				break OUT;// Open the file
			ArrayList<FinanceRecorderCmnClass.FinanceTimeRange> database_time_range_list = new ArrayList<FinanceRecorderCmnClass.FinanceTimeRange>(stock_source_type_amount);
			Collections.fill(database_time_range_list, null);
// Try to parse the content of the config file
			for (String config_line : config_line_list)
			{
// Check if the source type in the config file is in order
				String data_array[] = config_line.split(" ");
				if (data_array.length != 3)
				{
					FinanceRecorderCmnDef.format_error("Incorrect config format: %s", config_line);
					ret = FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
					break OUT;
				}
// Get source type index
				int source_type_index = FinanceRecorderCmnDef.get_source_type_index_from_description(data_array[0]);
				if (source_type_index == -1)
				{
					FinanceRecorderCmnDef.format_error("Unknown source type description[%s] in config file", data_array[0]);
					ret = FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
					break OUT;
				}
// Check the source type is in correct finance mode
				if (!FinanceRecorderCmnDef.check_source_type_index_in_range(source_type_index))
				{
					FinanceRecorderCmnDef.format_error("The source type[%s] does NOT belong to %s mode", FinanceRecorderCmnDef.FINANCE_DATA_DESCRIPTION_LIST[source_type_index], FinanceRecorderCmnDef.get_finance_mode_description());
					ret = FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
					break OUT;
				}
// Setup the start/end time string 
				if (database_time_range_list.get(source_type_index) != null)
				{
					FinanceRecorderCmnDef.format_error("The time range of source type[%s] has already been set", FinanceRecorderCmnDef.FINANCE_DATA_DESCRIPTION_LIST[source_type_index]);
					ret = FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
					break OUT;
				}
				database_time_range_list.set(source_type_index, new FinanceRecorderCmnClass.FinanceTimeRange(data_array[0], data_array[1]));
			}
			database_time_range_map.put(company_number, database_time_range_list);
		}

		return ret;
	}

	private short initialize()
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		// Read the data from the config file
		ret = parse_database_time_range_config();
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}
}
