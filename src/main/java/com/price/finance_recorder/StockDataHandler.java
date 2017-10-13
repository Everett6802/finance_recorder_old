package com.price.finance_recorder;

import java.util.*;


class StockDataHandler extends DataHandlerBase
{
//	private static CmnClassCompanyProfile company_profile = CmnClassCompanyProfile.get_instance();
	private static CmnClass.QuerySet whole_field_query_set = null;
	private static CompanyProfile company_profile = null;
	private static String get_csv_filepath(String csv_folderpath, int method_index, int company_group_number, String company_code_number)
	{
		return String.format("%s/%s%02d/%s/%s.csv", csv_folderpath, CmnDef.CSV_STOCK_FOLDERNAME, company_group_number, company_code_number, CmnDef.FINANCE_DATA_NAME_LIST[method_index]);
	}
	private static String get_database_name(int company_group_number)
	{
		return String.format("%s%02d", CmnDef.SQL_STOCK_DATABASE_NAME, company_group_number);
	}
	
//	private static String get_sql_database_name(int company_group_number)
//	{
//		return String.format("%s%02d", CmnDef.SQL_STOCK_DATABASE_NAME, company_group_number);
//	}
//
//	private static String get_sql_table_name(int method_index, String company_code_number)
//	{
//		return String.format("%s%s", company_code_number, CmnDef.FINANCE_DATA_NAME_LIST[method_index]);
//	}

	private static CompanyProfile get_company_profile()
	{
		if (company_profile == null)
			company_profile = CompanyProfile.get_instance();
		return company_profile;
	}

	public static CmnInf.DataHandlerInf get_data_handler(final LinkedList<Integer> method_index_list, final CmnClassStock.CompanyGroupSet company_group_set)
	{
		if (!CmnDef.IS_FINANCE_STOCK_MODE)
		{
			String errmsg = "It's NOT Stock mode";
			throw new IllegalStateException(errmsg);
		}
		for (Integer method_index : method_index_list)
		{
			if (!CmnDef.FinanceMethod.is_stock_method(method_index))
			{
				String errmsg = String.format("The data source[%d] is NOT Stock source type", method_index);
				throw new IllegalArgumentException(errmsg);
			}
		}
		StockDataHandler data_handler_obj = new StockDataHandler();
		data_handler_obj.method_index_list = method_index_list;
		if (data_handler_obj.method_index_list == null)
		{
			data_handler_obj.method_index_list = new LinkedList<Integer>();
			int start_index = CmnDef.FinanceMethod.FinanceMethod_StockStart.value();
			int end_index = CmnDef.FinanceMethod.FinanceMethod_StockEnd.value();
			for (int method_index = start_index ; method_index < end_index ; method_index++)
				method_index_list.add(method_index);
		}
		data_handler_obj.company_group_set = company_group_set;
		if (data_handler_obj.company_group_set == null)
			data_handler_obj.company_group_set = CmnClassStock.CompanyGroupSet.get_whole_company_group_set();
		return data_handler_obj;
	}
	public static CmnInf.DataHandlerInf get_data_handler_whole()
	{
		return get_data_handler(null, null);
	}

	private CmnClassStock.CompanyGroupSet company_group_set = null;
	private CmnDef.CreateThreadType database_create_thread_type = CmnDef.CreateThreadType.CreateThread_Single;
	private HashMap<String, ArrayList<Integer>> missing_csv_map = null;
	private StockSQLClient sql_client = null;
//	private LinkedList<CmnClass.MethodTimeRange> method_time_range_list = null;
//	private String csv_backup_foldername = CmnDef.COPY_BACKUP_FOLDERPATH;

	private StockDataHandler()
	{
		if (whole_field_query_set == null)
		{
			whole_field_query_set = new CmnClass.QuerySet();
			int method_start_index = CmnDef.FinanceMethod.FinanceMethod_StockStart.value();
			int method_end_index = CmnDef.FinanceMethod.FinanceMethod_StockEnd.value();
			for (int method_index = method_start_index ; method_index < method_end_index ; method_index++)
				whole_field_query_set.add_query(method_index);
			whole_field_query_set.add_query_done();
		}
		if (sql_client == null)
			sql_client = new StockSQLClient();
	}

	protected short create_finance_folder_hierarchy(String root_folderpath)
	{
		short ret = CmnFunc.create_folder_if_not_exist(root_folderpath);
		if (CmnDef.CheckFailure(ret))
			CmnLogger.format_error("Fail to create the root folder[%s], due to: %s", root_folderpath, CmnDef.GetErrorDescription(ret));
		for (Map.Entry<Integer, ArrayList<String>> entry : CmnClassStock.CompanyGroupSet.get_whole_company_number_in_group_map().entrySet())
		{
// Create the company group sub-folder
			String company_group_folderpath = String.format("%s/stock%02d", root_folderpath, entry.getKey());
			ret = CmnFunc.create_folder_if_not_exist(company_group_folderpath);
			if (CmnDef.CheckFailure(ret))
				CmnLogger.format_error("Fail to create the company group sub folder[%s], due to: %s", company_group_folderpath, CmnDef.GetErrorDescription(ret));
// Create the company number sub-folder
			ArrayList<String> company_number_list = entry.getValue();
			for (String company_number : company_number_list)
			{
				String company_number_folderpath = String.format("%s/%s", company_group_folderpath, company_number);
				ret = CmnFunc.create_folder_if_not_exist(company_number_folderpath);
				if (CmnDef.CheckFailure(ret))
					CmnLogger.format_error("Fail to create the company number sub folder[%s], due to: %s", company_number_folderpath, CmnDef.GetErrorDescription(ret));
			}
		}
		return CmnDef.RET_SUCCESS;
	}

	protected short parse_missing_csv()
	{
//		String missing_csv_filepath = String.format("%s/%s", current_csv_working_folerpath, CmnDef.MISSING_CSV_STOCK_FILENAME);
		LinkedList<String> config_line_list = new LinkedList<String>();
		short ret = CmnFunc.read_config_file_lines(CmnDef.MISSING_CSV_STOCK_FILENAME, current_csv_working_folerpath, config_line_list);
		if (CmnDef.CheckFailure(ret))
		{
			if (!CmnDef.CheckFailureNotFound(ret))
				return ret;
			else
				CmnLogger.format_debug("The missing CSV file[%s] does NOT exist", CmnDef.MISSING_CSV_STOCK_FILENAME);
		}
		else
		{
// Parse the config
//			missing_csv_map = new HashMap<String, ArrayList<Integer>>();
			String missing_csv_title = config_line_list.pop();
			if (missing_csv_title.indexOf("[FileNotFound]") != -1)
				missing_csv_map = new HashMap<String, ArrayList<Integer>>();
			else
			{
				config_line_list.pop();
				missing_csv_title = config_line_list.pop();
				if (missing_csv_title.indexOf("[FileNotFound]") != -1)
					missing_csv_map = new HashMap<String, ArrayList<Integer>>();
			}
			if (missing_csv_map != null)
			{
				String missing_csv_string = config_line_list.pop();
				String[] missing_csv_array = missing_csv_string.split(";");
				for (String missing_csv : missing_csv_array)
				{
					String[] missing_csv_element_array = missing_csv.split(":");
					String company_number = missing_csv_element_array[0];
					Integer method_index = Integer.valueOf(missing_csv_element_array[1]);
					if (!missing_csv_map.containsKey(company_number))
						missing_csv_map.put(company_number, new ArrayList<Integer>());
					missing_csv_map.get(company_number).add(method_index);
				}
			}
		}
		return CmnDef.RET_SUCCESS;
	}

	public short read_from_csv(CSVHandlerMap csv_data_map)
	{
		assert method_index_list != null : "method_index_list == NULL";
		assert company_group_set != null : "company_group_set == NULL";

		short ret = CmnDef.RET_SUCCESS;
// Ignore the CSV file which is already in the Not Found list
		ret = parse_missing_csv();
		if (CmnDef.CheckFailure(ret))
			return ret;

		for (Map.Entry<Integer, ArrayList<String>> company_code_entry : company_group_set)
		{
			int company_group_number = company_code_entry.getKey();
			for(String company_code_number : company_code_entry.getValue())
			{
				for (Integer method_index : method_index_list)
				{
					CSVHandler csv_reader = CSVHandler.get_csv_reader(StockDataHandler.get_csv_filepath(current_csv_working_folerpath, method_index, company_group_number, company_code_number));
					if (csv_reader == null)
					{
						CmnLogger.error(String.format("CSV NOT Found [%s:%d]", company_code_number, method_index));
						if (!is_operation_non_stop())
						{
// Check this missing CSV exist in the Not Found list
							if (missing_csv_map != null)
							{
								if (missing_csv_map.containsKey(company_code_number))
								{
									ArrayList<Integer> method_index_list = missing_csv_map.get(company_code_number);
									if (method_index_list.indexOf(method_index) != -1)
									{
										CmnLogger.format_debug("CSV[%s:%d] already in the Not-Found list", company_code_number, method_index);
										continue;
									}
								}
							}
							return CmnDef.RET_FAILURE_NOT_FOUND;
						}
						else
							continue;
					}
					ret = csv_reader.read();
					if (CmnDef.CheckFailure(ret))
						return ret;
					csv_data_map.put(CmnFunc.get_source_key(method_index, company_group_number, company_code_number), csv_reader);
				}
			}
		}
		return CmnDef.RET_SUCCESS;
	}

	public short write_into_sql(final CSVHandlerMap csv_data_map)
	{
		assert method_index_list != null : "method_index_list == NULL";
		assert company_group_set != null : "company_group_set == NULL";

		short ret = CmnDef.RET_SUCCESS;
		OUT:
		for (Map.Entry<Integer, ArrayList<String>> company_code_entry : company_group_set)
		{
			int company_group_number = company_code_entry.getKey();
// Establish the connection to the MySQL and create the database if not exist
//			StockSQLClient sql_client = new StockSQLClient();
			ret = sql_client.try_connect_mysql(company_group_number);
			if (CmnDef.CheckFailure(ret))
			{
				if (connect_mysql_can_continue(ret))
				{
					CmnLogger.format_warn("Try to create the database: %s", get_database_name(company_group_number));
					ret = sql_client.create_database(company_group_number);
					if (CmnDef.CheckFailure(ret))
						return ret;
// It's required to re-connect the database after creating it
					ret = sql_client.try_connect_mysql(company_group_number);
					if (CmnDef.CheckFailure(ret))
						return ret;
				}
				else
					return ret;
			}
			for(String company_code_number : company_code_entry.getValue())
			{
				for (Integer method_index : method_index_list)
				{
// Check data exist
					Integer source_key = CmnFunc.get_source_key(method_index, company_group_number, company_code_number);
					if (!csv_data_map.containsKey(source_key))
					{
						CmnLogger.format_error("The CSV data of source key[%d] (method: %d, company_code_number: %s)", source_key, method_index, company_code_number);
						ret = CmnDef.RET_FAILURE_INVALID_ARGUMENT;
						break OUT;
					}
// Create MySQL table
					ret = sql_client.create_table(method_index, company_code_number);
					if (CmnDef.CheckFailure(ret))
					{
						if (operation_can_continue(ret))
							continue;
						else
							break OUT;
					}
// Write the data into MySQL database
					CSVHandler csv_reader = csv_data_map.get(source_key);
					ret = sql_client.insert_data(method_index, company_code_number, csv_reader);
					if (CmnDef.CheckFailure(ret))
						break OUT;
				}
			}
// Destroy the connection to the MySQL
			sql_client.disconnect_mysql();
		}
		return ret;
	}

	public short transfrom_csv_to_sql()
	{
		assert method_index_list != null : "method_index_list == NULL";
		assert company_group_set != null : "company_group_set == NULL";

		short ret = CmnDef.RET_SUCCESS;
		if (is_operation_non_stop())
		{
// Ignore the CSV file which is already in the Not Found list
			ret = parse_missing_csv();
			if (CmnDef.CheckFailure(ret))
				return ret;
		}
OUT:
		for (Map.Entry<Integer, ArrayList<String>> company_code_entry : company_group_set)
		{
			int company_group_number = company_code_entry.getKey();
// Establish the connection to the MySQL and create the database if not exist
//			StockSQLClient sql_client = new StockSQLClient();
//			String database_name = String.format("%s%02d", CmnDef.SQL_STOCK_DATABASE_NAME, company_group_number);
			ret = sql_client.try_connect_mysql(company_group_number);
			if (CmnDef.CheckFailure(ret))
			{
				if (connect_mysql_can_continue(ret))
				{
					CmnLogger.format_warn("Try to create the database: %s", CmnDef.SQL_MARKET_DATABASE_NAME);
					ret = sql_client.create_database(company_group_number);
// Should NOT fail in this condition......
					if (CmnDef.CheckFailure(ret))
						return ret;
// It's required to re-connect the database after creating it
					ret = sql_client.try_connect_mysql(company_group_number);
					if (CmnDef.CheckFailure(ret))
						return ret;
				}
				else
					return ret;
			}
// For each company
			for(String company_code_number : company_code_entry.getValue())
			{
// For each source type
				for (Integer method_index : method_index_list)
				{
// Read data from CSV
					CSVHandler csv_reader = CSVHandler.get_csv_reader(StockDataHandler.get_csv_filepath(current_csv_working_folerpath, method_index, company_group_number, company_code_number));
					if (csv_reader == null)
					{
						CmnLogger.error(String.format("CSV NOT Found [%s:%d]", company_code_number, method_index));
						if (!is_operation_non_stop())
						{
// Check this missing CSV exist in the Not Found list
							if (missing_csv_map != null)
							{
								if (missing_csv_map.containsKey(company_code_number))
								{
									ArrayList<Integer> method_index_list = missing_csv_map.get(company_code_number);
									if (method_index_list.indexOf(method_index) != -1)
									{
										CmnLogger.format_debug("CSV[%s:%d] already in the Not-Found list", company_code_number, method_index);
										continue;
									}
								}
							}
							return CmnDef.RET_FAILURE_NOT_FOUND;
						}
						else
							continue;
					}
					ret = csv_reader.read();
					if (CmnDef.CheckFailure(ret))
						return ret;
// Create MySQL table
					ret = sql_client.create_table(method_index, company_code_number);
					if (CmnDef.CheckFailure(ret))
					{
						if (operation_can_continue(ret))
							continue;
						else
							break OUT;
					}
// Write the data into MySQL database
					ret = sql_client.insert_data(method_index, company_code_number, csv_reader);
					if (CmnDef.CheckFailure(ret))
						break OUT;
				}
			}
// Destroy the connection to the MySQL
			sql_client.disconnect_mysql();
		}
		return ret;
	}

	public short read_from_sql(CmnClass.QuerySet query_set, CmnClass.FinanceTimeRange finance_time_range, CmnClass.ResultSetMap result_set_map)
	{
		assert method_index_list != null : "method_index_list == NULL";
		assert company_group_set != null : "company_group_set == NULL";

// CAUTION: The data set in the reslt_set variable should be added before calling this function 
// Set the mapping table of reading the specific CSV files and writing correct SQL database
		short ret = CmnDef.RET_SUCCESS;
		CmnDef.ResultSetDataUnit data_unit = result_set_map.get_data_unit();
//		StockSQLClient sql_client = new StockSQLClient();
		for (Map.Entry<Integer, ArrayList<String>> company_code_entry : company_group_set)
		{
			int company_group_number = company_code_entry.getKey();
// Establish the connection to the MySQL
			ret = sql_client.try_connect_mysql(company_group_number);
			if (CmnDef.CheckFailure(ret))
			{
				if (connect_mysql_can_continue(ret))
				{
	// No data to read
						CmnLogger.warn(String.format("No data to read from %s", get_database_name(company_group_number)));
						return CmnDef.RET_SUCCESS;
				}
				else
					return ret;
			}
			CmnClass.ResultSet result_set = null;
OUT:
			for(String company_code_number : company_code_entry.getValue())
			{
				switch (data_unit)
				{
				case ResultSetDataUnit_NoMethod:
				{
					result_set = new CmnClass.ResultSet();
// Add query set
					for (Integer method_index : method_index_list)
					{
						ret = result_set.add_set(method_index, query_set.get_field_index_list(method_index));
						if (CmnDef.CheckFailure(ret))
							break OUT;
					}
// Query data from each source type
					for (Integer method_index : method_index_list)
					{
						ret = sql_client.select_data(method_index, company_code_number, finance_time_range, result_set);
						if (CmnDef.CheckFailure(ret))
						{
							if (operation_can_continue(ret))
								continue;
							else
								break OUT;
						}
					}
// Keep track of the data in the designated data structure
					ret = result_set_map.register_result_set(Integer.valueOf(company_code_number), result_set);
					if (CmnDef.CheckFailure(ret))
						break OUT;
				}
				break;
				case ResultSetDataUnit_Method:
				{
					for (Integer method_index : method_index_list)
					{
						result_set = new CmnClass.ResultSet();
// Add query set
						ret = result_set.add_set(method_index, query_set.get_field_index_list(method_index));
						if (CmnDef.CheckFailure(ret))
							break OUT;
// Query data from each source type
						ret = sql_client.select_data(method_index, company_code_number, finance_time_range, result_set);
						if (CmnDef.CheckFailure(ret))
						{
							if (operation_can_continue(ret))
								continue;
							else
								break OUT;
						}
// Keep track of the data in the designated data structure
						ret = result_set_map.register_result_set(CmnFunc.get_source_key(method_index, company_group_number, company_code_number), result_set);
						if (CmnDef.CheckFailure(ret))
							break OUT;
					}
				}
				break;
				default:
				{
					String errmsg = String.format("Unsupported data unit: %d", data_unit.ordinal());
					throw new IllegalArgumentException(errmsg);
				}
				}
			}
	// Destroy the connection to the MySQL
			sql_client.disconnect_mysql();
		}

		return ret;
	}
	public short read_from_sql(CmnClass.FinanceTimeRange finance_time_range, CmnClass.ResultSetMap result_set_map)
	{
		return read_from_sql(whole_field_query_set, finance_time_range, result_set_map);
	}

	public short write_into_csv(CmnClass.ResultSetMap result_set_map)
	{
		assert method_index_list != null : "method_index_list == NULL";
		assert company_group_set != null : "company_group_set == NULL";

		short ret = CmnDef.RET_SUCCESS;
// Create the finance folder hierarchy for writing CSV
		ret = create_finance_folder_hierarchy(current_csv_working_folerpath);
		if (CmnDef.CheckFailure(ret))
			return ret;

		CmnDef.ResultSetDataUnit data_unit = result_set_map.get_data_unit();
		CmnClass.ResultSet result_set = null;
OUT:
		switch (data_unit)
		{
		case ResultSetDataUnit_NoMethod:
		{
			for (Map.Entry<Integer, CmnClass.ResultSet> entry : result_set_map)
			{
				int source_key = entry.getKey();
				String company_code_number = CmnFunc.get_company_code_number(source_key);
				int company_group_number = CmnFunc.get_company_group_number(source_key);
				result_set = entry.getValue();
				for (Integer method_index : method_index_list)
				{
					CSVHandler csv_writer = CSVHandler.get_csv_writer(StockDataHandler.get_csv_filepath(current_csv_working_folerpath, method_index, company_group_number, company_code_number));
	//Assemble the data and write into CSV
					ArrayList<String> csv_data_list = result_set.to_string_array(method_index);
					csv_writer.set_write_data(csv_data_list);
					ret = csv_writer.write();
					if (CmnDef.CheckFailure(ret))
						break OUT;
				}
			}
		}
		break;
		case ResultSetDataUnit_Method:
		{
			for (Map.Entry<Integer, CmnClass.ResultSet> entry : result_set_map)
			{
				int source_key = entry.getKey();
				int method_index = CmnFunc.get_method(source_key);
				String company_code_number = CmnFunc.get_company_code_number(source_key);
				int company_group_number = CmnFunc.get_company_group_number(source_key);
				result_set = entry.getValue();
// Ignore the data which is NOT in the list
				CSVHandler csv_writer = CSVHandler.get_csv_writer(StockDataHandler.get_csv_filepath(current_csv_working_folerpath, method_index, company_group_number, company_code_number));
// Assemble the data and write into CSV
				ArrayList<String> csv_data_list = result_set.to_string_array(method_index);
				csv_writer.set_write_data(csv_data_list);
				ret = csv_writer.write();
				if (CmnDef.CheckFailure(ret))
					break OUT;
			}
		}
		break;
		default:
		{
			String errmsg = String.format("Unsupported data unit: %d", data_unit.ordinal());
			throw new IllegalArgumentException(errmsg);
		}
		}
		return ret;
	}

	public short transfrom_sql_to_csv(CmnClass.QuerySet query_set, CmnClass.FinanceTimeRange finance_time_range)
	{
		assert method_index_list != null : "method_index_list == NULL";
		assert company_group_set != null : "company_group_set == NULL";

		if (!query_set.is_add_query_done())
		{
			CmnLogger.error("The add-done flag in query_set is NOT true");
			return CmnDef.RET_FAILURE_INVALID_ARGUMENT;
		}
		short ret = CmnDef.RET_SUCCESS;
// Create the finance folder hierarchy for writing CSV
		ret = create_finance_folder_hierarchy(current_csv_working_folerpath);
		if (CmnDef.CheckFailure(ret))
			return ret;

//		StockSQLClient sql_client = new StockSQLClient();
		CmnClass.ResultSet result_set = new CmnClass.ResultSet();
// Add query set
		for (Integer method_index : method_index_list)
		{
			ret = result_set.add_set(method_index, query_set.get_field_index_list(method_index));
			if (CmnDef.CheckFailure(ret))
				return ret;
		}
		for (Map.Entry<Integer, ArrayList<String>> company_code_entry : company_group_set)
		{
			int company_group_number = company_code_entry.getKey();
// Establish the connection to the MySQL
			ret = sql_client.try_connect_mysql(company_group_number);
			if (CmnDef.CheckFailure(ret))
			{
				if (connect_mysql_can_continue(ret))
				{
// No data to read
					CmnLogger.warn(String.format("No data to read from %s", get_database_name(company_group_number)));
					continue;
				}
				else
					return ret;
			}
OUT:
			for(String company_code_number : company_code_entry.getValue())
			{
//				System.out.printf("company_code_number: %s\n", company_code_number);
// Query data from each source type
				for (Integer method_index : method_index_list)
				{
					ret = sql_client.select_data(method_index, company_code_number, finance_time_range, result_set);
					if (CmnDef.CheckFailure(ret))
					{
						if (operation_can_continue(ret))
							continue OUT;
						else
							break OUT;
					}
					CSVHandler csv_writer = CSVHandler.get_csv_writer(StockDataHandler.get_csv_filepath(current_csv_working_folerpath, method_index, company_group_number, company_code_number));
//Assemble the data and write into CSV
					ArrayList<String> csv_data_list = result_set.to_string_array(method_index);
					csv_writer.set_write_data(csv_data_list);
					ret = csv_writer.write();
					if (CmnDef.CheckFailure(ret))
						break OUT;
				}
// Cleanup the result data
				result_set.clear_array_data();
			}
// Destroy the connection to the MySQL
			sql_client.disconnect_mysql();
		}
		return ret;
	}
	public short transfrom_whole_sql_to_csv(CmnClass.FinanceTimeRange finance_time_range)
	{
		return transfrom_sql_to_csv(whole_field_query_set, finance_time_range);
	}

	private short delete_sql_table_by_method_and_company(List<Integer> in_method_index_list, CmnClassStock.CompanyGroupSet in_company_group_set)
	{
		short ret = CmnDef.RET_SUCCESS;
OUT:
		for (Map.Entry<Integer, ArrayList<String>> company_code_entry : in_company_group_set)
		{
//			StockSQLClient sql_client = new StockSQLClient();
			int company_group_number = company_code_entry.getKey();
// Establish the connection to the MySQL
			ret = sql_client.try_connect_mysql(company_group_number);
			if (CmnDef.CheckFailure(ret))
			{
				if (connect_mysql_can_continue(ret))
				{
					CmnLogger.warn(String.format("Database[%s] does NOT exist", get_database_name(company_group_number)));
					continue;
				}
				else
					return ret;
			}
			for(String company_code_number : company_code_entry.getValue())
			{
// Delete each table
				for (Integer method_index : in_method_index_list)
				{
					ret = sql_client.delete_table(method_index, company_code_number);
					if (CmnDef.CheckFailure(ret))
					{
						if (operation_can_continue(ret))
							continue;
						else
							break OUT;
					}
				}
			}
// Destroy the connection to the MySQL
			sql_client.disconnect_mysql();
		}
		return CmnDef.RET_SUCCESS;
	}

	public short delete_sql_by_method()
	{
		assert method_index_list != null : "method_index_list == NULL";

		return delete_sql_table_by_method_and_company(method_index_list, CmnClassStock.CompanyGroupSet.get_whole_company_group_set());
	}

	public short delete_sql_by_company() // Only useful in stock mode
	{
		assert company_group_set != null : "company_group_set == NULL";

		return delete_sql_table_by_method_and_company(CmnFunc.get_all_method_index_list(), company_group_set);
	} 

	public short delete_sql_by_method_and_company() // Only useful in stock mode
	{
		assert method_index_list != null : "method_index_list == NULL";
		assert company_group_set != null : "company_group_set == NULL";

		return delete_sql_table_by_method_and_company(method_index_list, company_group_set);
	} 

	public short cleanup_sql()
	{
		short ret = CmnDef.RET_SUCCESS;
// Establish the connection to the MySQL
		int company_group_size = get_company_profile().get_company_group_size();
OUT:
		for (int i = 0 ; i < company_group_size ; i++)
		{
//			String database_name = StockSQLClient.get_database_name(i);
//			StockSQLClient sql_client = new StockSQLClient();
			ret = sql_client.try_connect_mysql(i);
			if (CmnDef.CheckFailure(ret))
			{
				if (connect_mysql_can_continue(ret))
				{
					CmnLogger.warn(String.format("Database[%s] does NOT exist", get_database_name(i)));
					continue;
				}
				else
					return ret;
			}
// Delete the database
			ret = sql_client.delete_database(i);
			if (CmnDef.CheckFailure(ret))
			{
// Should NOT fail !!!
				if (operation_can_continue(ret))
				{
					ret = CmnDef.RET_FAILURE_NOT_FOUND;
					String errmsg = String.format("Fails to delete database[%s], due to: %s", get_database_name(i), CmnDef.GetErrorDescription(ret));
					CmnLogger.error(errmsg);
				}
			}
// Destroy the connection to the MySQL
			sql_client.disconnect_mysql();
			if (CmnDef.CheckFailure(ret))
				break OUT;
		}
		return ret;
	}

	public short check_sql_exist(ArrayList<String> not_exist_list)
	{
		assert method_index_list != null : "method_index_list == NULL";
		assert company_group_set != null : "company_group_set == NULL";

		short ret = CmnDef.RET_SUCCESS;
// Establish the connection to the MySQL
OUT:
		for (Map.Entry<Integer, ArrayList<String>> company_code_entry : company_group_set)
		{
//			StockSQLClient sql_client = new StockSQLClient();
			int company_group_number = company_code_entry.getKey();
// Establish the connection to the MySQL
			ret = sql_client.try_connect_mysql(company_group_number);
			if (CmnDef.CheckFailure(ret))
			{
				if (CmnDef.CheckMySQLFailureUnknownDatabase(ret))
				{
					CmnLogger.format_warn("The database[%s] does NOT exist, add all tables in the Not-Found List......", get_database_name(company_group_number));
					LinkedList<Integer> all_method_index_list = CmnFunc.get_all_method_index_list();
					for(String company_code_number : company_code_entry.getValue())
					{
						for (Integer method_index : all_method_index_list)
							not_exist_list.add(String.format("%s:%d", company_code_number, method_index));
					}
					return CmnDef.RET_SUCCESS;
				}
				else
					return ret;
			}
			if (CmnDef.CheckFailure(ret))
			{
				String database_name = String.format("%s%02d", CmnDef.SQL_STOCK_DATABASE_NAME, company_group_number);
				if (CmnDef.CheckMySQLFailureUnknownDatabase(ret))
				{
					CmnLogger.format_warn("The database[%s] does NOT exist, add all tables in the Not-Found List......", database_name);
					LinkedList<Integer> all_method_index_list = CmnFunc.get_all_method_index_list();
					for(String company_code_number : company_code_entry.getValue())
					{
						for (Integer method_index : all_method_index_list)
							not_exist_list.add(String.format("%s:%d", company_code_number, method_index));
					}
					continue OUT;
				}
				else
				{
					CmnLogger.format_error("Error occurs while checking database[%s] exist, due to: %s", database_name, CmnDef.GetErrorDescription(ret));
					return ret;
				}
			}
// Check MySQL table exist
			boolean fail_and_exit = false;
OUT1:
			for(String company_code_number : company_code_entry.getValue())
			{
				for (Integer method_index : method_index_list)
				{
					ret = sql_client.check_table_exist(method_index, company_code_number);
					if (CmnDef.CheckFailure(ret))
					{
						String table_name = String.format("%s:%d", company_code_number, method_index);
						if (CmnDef.CheckFailureNotFound(ret))
							not_exist_list.add(table_name);
						else
						{
							CmnLogger.format_error("Error occurs while checking table[%s] exist, due to: %s", table_name, CmnDef.GetErrorDescription(ret));
							fail_and_exit = true;
							break OUT1;
						}
					}
				}
			}
// Destroy the connection to the MySQL
			sql_client.disconnect_mysql();
			if (fail_and_exit)
				return ret;
		}
		return CmnDef.RET_SUCCESS;
	}

	public void enable_multi_thread_type(boolean enable)
	{
		database_create_thread_type = enable ? CmnDef.CreateThreadType.CreateThread_Multiple : CmnDef.CreateThreadType.CreateThread_Single;
	}
	public boolean is_multi_thread_type(){return database_create_thread_type == CmnDef.CreateThreadType.CreateThread_Multiple;}
}
