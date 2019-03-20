package com.price.finance_recorder_rest.libs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import com.price.finance_recorder_rest.common.CmnDef;
import com.price.finance_recorder_rest.common.CmnDef.FinanceMethod;
//import com.price.finance_recorder_rest.common.CmnDef.FinanceMethod;
import com.price.finance_recorder_rest.common.CmnFunc;
import com.price.finance_recorder_rest.common.CmnLogger;
import com.price.finance_recorder_rest.libs.CompanyProfile;

public class CSVHandler implements Iterable<String> 
{
	public enum HandlerMode{HandlerMode_Read, HandlerMode_Write};

	private static CompanyProfile company_profile = null;
	
	private static synchronized CompanyProfile get_company_profile()
	{
		if (company_profile == null)
		{
			company_profile = CompanyProfile.get_instance();
		}
		return company_profile;
	}
	
	public static CSVHandler get_csv_reader(String csv_resource_path, boolean no_not_found_exception)
	{
		CSVHandler csv_reader = new CSVHandler();
		csv_reader.handler_mode = HandlerMode.HandlerMode_Read;
		CmnLogger.debug(String.format("Ready to read CSV from: %s", csv_resource_path));
		csv_reader.csv_resource_path = csv_resource_path;
		short ret = csv_reader.initialize_read_stream();
		if (CmnDef.CheckFailure(ret))
		{
			if (no_not_found_exception)
			{
				if (CmnDef.CheckFailureNotFound(ret))
					return null;
			}
			else
				throw new RuntimeException(String.format("Fail to initialize the CSV reader, due to: %s", CmnDef.GetErrorDescription(ret)));
		}
		return csv_reader;
	}
	public static CSVHandler get_csv_reader(String csv_resource_path){return get_csv_reader(csv_resource_path, true);}

	public static CSVHandler get_csv_writer(String csv_resource_path, boolean no_not_found_exception)
	{
		CSVHandler csv_writer = new CSVHandler();
		csv_writer.handler_mode = HandlerMode.HandlerMode_Write;
		csv_writer.csv_resource_path = csv_resource_path;
		short ret = csv_writer.initialize_write_stream();
		if (CmnDef.CheckFailure(ret))
		{
			if (no_not_found_exception)
			{
				if (CmnDef.CheckFailureNotFound(ret))
					return null;
			}
			else
				throw new RuntimeException(String.format("Fail to initialize the CSV writer, due to: %s", CmnDef.GetErrorDescription(ret)));
		}
		return csv_writer;
	}
	public static CSVHandler get_csv_writer(String csv_resource_path){return get_csv_writer(csv_resource_path, false);}

	public static String read_statistics(FinanceMethod finance_method, String company_number, String dataset_folderpath)
	{
		String csv_filepath = null;
		if (company_number == null)
		{
			csv_filepath = CmnFunc.get_dataset_market_csv_filepath(finance_method.value(), dataset_folderpath);
		}
		else
		{
			if (company_profile == null)
				company_profile = get_company_profile();
			int company_group_number = company_profile.lookup_company_group_number(company_number);
			csv_filepath = CmnFunc.get_dataset_stock_csv_filepath(finance_method.value(), company_number, company_group_number, dataset_folderpath);
		}
		String lookup_statistics = "";
		CSVHandler csv_handler = get_csv_reader(csv_filepath);
		if (csv_handler == null)
			lookup_statistics = ((company_number != null) ? String.format("%s\n", CmnDef.FINANCE_DATA_NAME_LIST[finance_method.value()]) : String.format("%s:%s\n", CmnDef.FINANCE_DATA_NAME_LIST[finance_method.value()], company_number));
		else
		{
			short ret = CmnDef.RET_SUCCESS;
			StringBuilder line_first_builder = new StringBuilder();
			ret = csv_handler.read_line(0, line_first_builder);
			if (CmnDef.CheckFailure(ret))
			{
				String errmsg = ((company_number != null) ? String.format("Fail to read first line of csv[%s], due to: %s", CmnDef.FINANCE_DATA_NAME_LIST[finance_method.value()], CmnDef.GetErrorDescription(ret)) : String.format("Fail to read first line of csv[%s:%s], due to: %s", CmnDef.FINANCE_DATA_NAME_LIST[finance_method.value()], company_number, CmnDef.GetErrorDescription(ret)));
				throw new RuntimeException(errmsg);				
			}
			StringBuilder line_last_builder = new StringBuilder();
			ret = csv_handler.read_line(-1, line_last_builder);
			if (CmnDef.CheckFailure(ret))
			{
				String errmsg = ((company_number != null) ? String.format("Fail to read last line of csv[%s], due to: %s", CmnDef.FINANCE_DATA_NAME_LIST[finance_method.value()], CmnDef.GetErrorDescription(ret)) : String.format("Fail to read last line of csv[%s:%s], due to: %s", CmnDef.FINANCE_DATA_NAME_LIST[finance_method.value()], company_number, CmnDef.GetErrorDescription(ret)));
				throw new RuntimeException(errmsg);				
			}
			String[] line_first_split = line_first_builder.toString().split(",");
			String[] line_last_split = line_last_builder.toString().split(",");
			lookup_statistics += String.format("Time Range: %s - %s\n", line_first_split[0], line_last_split[0]);
			int[] line_count = new int[1];
			ret = csv_handler.read_line_count(line_count);
			if (CmnDef.CheckFailure(ret))
			{
				String errmsg = ((company_number != null) ? String.format("Fail to read line count of csv[%s], due to: %s", CmnDef.FINANCE_DATA_NAME_LIST[finance_method.value()], CmnDef.GetErrorDescription(ret)) : String.format("Fail to read line count of csv[%s:%s], due to: %s", CmnDef.FINANCE_DATA_NAME_LIST[finance_method.value()], company_number, CmnDef.GetErrorDescription(ret)));
				throw new RuntimeException(errmsg);				
			}
			lookup_statistics += String.format("Amount: %d\n", line_count[0]);
		}

		return lookup_statistics;
	}
	
	private final String NEW_LINE = "\n";
	private String csv_resource_path;
	private BufferedReader br = null;
	private BufferedWriter bw = null;
	private HandlerMode handler_mode;
	private ArrayList<String> data_list = null;
	private boolean csv_from_remote = false;

	private CSVHandler(boolean csv_from_remote)
	{
		this.csv_from_remote = csv_from_remote;
	}

	private CSVHandler()
	{
		this(false);
	}
	
	private short initialize_read_stream() 
	{
		if (!csv_from_remote)
		{
			try
			{
				File file = new File(csv_resource_path);
				br = new BufferedReader(new FileReader(file));
			}
			catch (FileNotFoundException e)
			{
				CmnLogger.format_error("The csv data file path[%s] is NOT found", csv_resource_path);
				return CmnDef.RET_FAILURE_NOT_FOUND;
			}
		}
		else
		{
			try
			{
				URL url = new URL(csv_resource_path);
				URLConnection conn = url.openConnection();
				br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			}
			catch (IOException e)
			{
				CmnLogger.format_error("The csv data URL path[%s] is NOT found", csv_resource_path);
				return CmnDef.RET_FAILURE_NOT_FOUND;
			}
		}
		return CmnDef.RET_SUCCESS;
	}

	private short initialize_write_stream()
	{
		File file = new File(csv_resource_path);
		try
		{
			file.createNewFile();
			bw = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
		}
		catch (FileNotFoundException e)
		{
			CmnLogger.format_error("The data file[%s] is NOT found", csv_resource_path);
			return CmnDef.RET_FAILURE_NOT_FOUND;
		}
		catch (IOException e)
		{
			CmnLogger.format_error("Error occur, due to: %s", e.toString());
			return CmnDef.RET_FAILURE_IO_OPERATION;
		} 

		return CmnDef.RET_SUCCESS;
	}

//	public short initialize(String data_filepath, HandlerMode mode)
//	{
//		csv_resource_path = data_filepath;
//		handler_mode = mode;
////		CmnLogger.format_debug("Open the CSV file: %s", csv_resource_path);
//		return ((mode == HandlerMode.HandlerMode_Read) ? initialize_read_stream() : initialize_write_stream());
//	}

	public short deinitialize()
	{
		if (br != null)
		{
			try 
			{
				br.close();
			}
			catch (IOException e){}
		}
		if (bw != null)
		{
			try 
			{
				bw.flush();
				bw.close();
			}
			catch (IOException e){}
		}

		csv_resource_path = null;
		return CmnDef.RET_SUCCESS;
	}

	public short read()
	{
		if (handler_mode != HandlerMode.HandlerMode_Read)
		{
			CmnLogger.error("CSV Handler is NOT in READ mode");
			return CmnDef.RET_FAILURE_INCORRECT_OPERATION;
		}
		if (data_list != null)
		{
			CmnLogger.error("CSV data already exist in the list");
			return CmnDef.RET_FAILURE_INCORRECT_OPERATION;
		}
		data_list = new ArrayList<String>();

		short ret = CmnDef.RET_SUCCESS;
		String line = null;
		int count = 0;
//		String field_string = null;
		try
		{
			while ((line = br.readLine()) != null) 
			{
//				String[] field_array = line.split(CmnDef.DATA_SPLIT);
//				data_list.add(format_data_to_string(field_array));
				data_list.add(line);
//				CmnLogger.format_debug("New Data: %s", ((LinkedList<String>)data_list).peekLast());
				count++;
			}
			CmnLogger.format_debug("Read %d data in: %s", count, csv_resource_path);
		}
		catch (IOException e) 
		{
			CmnLogger.format_error("Error occur while reading the data, due to: %s", e.toString());
			ret = CmnDef.RET_FAILURE_IO_OPERATION;
		}
		return ret;
	}

	public short read_line(int line_no, StringBuilder line_cur)
	{
		if (handler_mode != HandlerMode.HandlerMode_Read)
		{
			CmnLogger.error("CSV Handler is NOT in READ mode");
			return CmnDef.RET_FAILURE_INCORRECT_OPERATION;
		}

		short ret = CmnDef.RET_SUCCESS;
		if (data_list != null)
		{
			int data_list_len = data_list.size();
			if (line_no < 0)
				line_no = data_list_len + line_no;
			if (line_no < 0 || line_no >= data_list_len)
			{
				CmnLogger.format_error("The line no[%d] is out of the range[0, %d)", line_no, data_list_len);
				return CmnDef.RET_FAILURE_INVALID_ARGUMENT;
			}
			line_cur.append(data_list.get(line_no));
			return CmnDef.RET_SUCCESS;
		}
		else
		{
			if (line_no < 0)
			{
				int[] line_count = new int[1];
				ret = CmnFunc.read_file_line_count(csv_resource_path, line_count);
				if (CmnDef.CheckFailure(ret))
					return ret;
				line_no = line_count[0] + line_no;
				if (line_no < 0)
				{
					CmnLogger.format_error("The line number[%d] is out of range [0, %d)", line_no, line_count[0]);
					return CmnDef.RET_FAILURE_INVALID_ARGUMENT;
				}
			}
			ret = CmnFunc.read_file_line(csv_resource_path, line_no, line_cur);
		}
//		String line = null;
//		int count = 0;
//		boolean found = false;
//		try
//		{
//			while ((line = br.readLine()) != null) 
//			{
//				if (line_no == count)
//				{
//					line_cur.append(line);
//					found = true;
//					break;
//				}
//				count++;
//			}
//		}
//		catch (IOException e) 
//		{
//			CmnLogger.format_error("Error occur while reading the data, due to: %s", e.toString());
//			ret = CmnDef.RET_FAILURE_IO_OPERATION;
//		}
//		CmnLogger.format_debug("Read line data[%d] in: %s", line_no, csv_resource_path);
		return ret;
	}

	public short read_line_count(int[] line_count)
	{
		if (handler_mode != HandlerMode.HandlerMode_Read)
		{
			CmnLogger.error("CSV Handler is NOT in READ mode");
			return CmnDef.RET_FAILURE_INCORRECT_OPERATION;
		}

		short ret = CmnDef.RET_SUCCESS;
		if (data_list != null)
		{
			line_count[0] = data_list.size();
		}
		else
		{
			ret = CmnFunc.read_file_line_count(csv_resource_path, line_count);
		}

//		int count = 0;
//		try
//		{
//			while (br.readLine() != null) count++;
//		}
//		catch (IOException e) 
//		{
//			CmnLogger.format_error("Error occur while reading the data, due to: %s", e.toString());
//			ret = CmnDef.RET_FAILURE_IO_OPERATION;
//		}
//		line_count[0] = count;
//		CmnLogger.format_debug("Read line count[%d] in: %s", line_count[0], csv_resource_path);
		return ret;
	}
	
	public final ArrayList<String> get_read_data()
	{
		if (handler_mode != HandlerMode.HandlerMode_Read)
		{
			CmnLogger.error("CSV Handler is NOT in READ mode");
			throw new IllegalStateException("CSV Handler is NOT in READ mode");
		}
		if (data_list == null)
		{
			CmnLogger.error("No CSV data to read");
			throw new IllegalStateException("No CSV data to read");
		}
		return data_list;
	}

	public short write()
	{
		if (handler_mode != HandlerMode.HandlerMode_Write)
		{
			CmnLogger.error("CSV Handler is NOT in WRITE mode");
			return CmnDef.RET_FAILURE_INCORRECT_OPERATION;
		}

		short ret = CmnDef.RET_SUCCESS;
//		String line = null;
		int count = 0;
//		String field_string = null;
		try
		{
			for (String data : data_list)
			{ 
				bw.write(data);
				bw.write(NEW_LINE);
			}
			bw.flush();
		}
		catch (IOException e) 
		{
			CmnLogger.format_error("Error occur while writing the data, due to: %s", e.toString());
			ret = CmnDef.RET_FAILURE_IO_OPERATION;
		}
		CmnLogger.format_debug("Write %d data in: %s", count, csv_resource_path);

		return ret;
	}

	public void set_write_data(final ArrayList<String> intput_data_list)
	{
		if (handler_mode != HandlerMode.HandlerMode_Write)
		{
			CmnLogger.error("CSV Handler is NOT in WRITE mode");
			throw new IllegalStateException("CSV Handler is NOT in WRITE mode");
		}
		if (data_list != null)
		{
			CmnLogger.error("CSV data has already been set");
			throw new IllegalStateException("CSV data has already been set");
		}
		data_list = intput_data_list;
	}

	@Override
	public Iterator<String> iterator()
	{
		if (handler_mode != HandlerMode.HandlerMode_Read)
		{
			CmnLogger.error("CSV Handler is NOT in READ mode");
			throw new IllegalStateException("CSV Handler is NOT in READ mode");
		}
		Iterator<String> it = new Iterator<String>()
		{
			int data_list_len = data_list.size();
			int cur_index = 0;
			@Override
			public boolean hasNext()
			{
				return cur_index < data_list_len;
			}
			@Override
			public String next()
			{
				return data_list.get(cur_index++);
			}
			@Override
			public void remove() {throw new UnsupportedOperationException();}
		};
		return it;
	}
}
