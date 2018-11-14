package com.price.finance_recorder_lib;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
//import java.util.ArrayList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
//import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
//import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;

class CmnFunc
{
	// private static RecorderLogger recorder_logger =
	// RecorderLogger.get_instance();

	static CmnDef.FinanceAnalysisMode get_finance_analysis_mode_from_cfg() throws RuntimeException
	{
		CmnDef.FinanceAnalysisMode mode = null;
		String errmsg;
		// Read the data from the config file
		LinkedList<String> config_line_list = new LinkedList<String>();
		short res = read_config_file_lines(CmnDef.MARKET_STOCK_SWITCH_CONF_FILENAME, config_line_list);
		if (CmnDef.CheckFailure(res))
		{
			errmsg = String.format("Fail to parse the config file: %s, due to: %s", CmnDef.MARKET_STOCK_SWITCH_CONF_FILENAME, CmnDef.GetErrorDescription(res));
			throw new RuntimeException(errmsg);
		}
		if (config_line_list.size() != 1)
		{
			errmsg = String.format("Incorrect content in the config file: %s", CmnDef.MARKET_STOCK_SWITCH_CONF_FILENAME);
			throw new RuntimeException(errmsg);
		}

		mode = CmnDef.FinanceAnalysisMode.valueOf(Integer.parseInt(config_line_list.get(0)));
		if (mode == null)
		{
			errmsg = String.format("Unknown finance analysis mode: %s", config_line_list.get(0));
			throw new RuntimeException(errmsg);
		}
		return mode;
	}
	//
	// static boolean is_market_mode()
	// {
	// return (FINANCE_MODE != null ? IS_FINANCE_MARKET_MODE :
	// get_finance_analysis_mode() ==
	// FinanceAnalysisMode.FinanceAnalysis_Market);
	// }
	//
	// static boolean is_stock_mode()
	// {
	// return (FINANCE_MODE != null ? IS_FINANCE_STOCK_MODE :
	// get_finance_analysis_mode() ==
	// FinanceAnalysisMode.FinanceAnalysis_Stock);
	// }

	static String get_finance_mode_description()
	{
		if (CmnDef.IS_FINANCE_MARKET_MODE)
			return CmnDef.FINANCE_MARKET_MODE_DESCRIPTION;
		else
			if (CmnDef.IS_FINANCE_STOCK_MODE)
				return CmnDef.FINANCE_STOCK_MODE_DESCRIPTION;
			else
				throw new IllegalStateException("Unknown finance mode");
	}

	static boolean check_method_index_in_range(int method_index)
	{
		if (CmnDef.IS_FINANCE_MARKET_MODE)
		{
			if ((method_index >= CmnDef.FINANCE_METHOD_MARKET_START) && (method_index < CmnDef.FINANCE_METHOD_MARKET_END))
				return true;
			else
				return false;
		}
		else
			if (CmnDef.IS_FINANCE_STOCK_MODE)
			{
				if ((method_index >= CmnDef.FINANCE_METHOD_STOCK_START) && (method_index < CmnDef.FINANCE_METHOD_STOCK_END))
					return true;
				else
					return false;
			}
		throw new IllegalStateException("Unknown finance mode");
	}

	static boolean check_statement_method_index_in_range(int method_index)
	{
		if (CmnDef.IS_FINANCE_MARKET_MODE)
		{
			return false;
		}
		else
			if (CmnDef.IS_FINANCE_STOCK_MODE)
			{
				if ((method_index >= CmnDef.FINANCE_METHOD_STOCK_STATMENT_START) && (method_index < CmnDef.FINANCE_METHOD_STOCK_STATMENT_END))
					return true;
				else
					return false;
			}
		throw new IllegalStateException("Unknown finance mode");
	}

	static int[] get_method_index_range()
	{
		int[] method_index_list = new int[2];
		if (CmnDef.IS_FINANCE_MARKET_MODE)
		{
			method_index_list[0] = CmnDef.FINANCE_METHOD_MARKET_START;
			method_index_list[1] = CmnDef.FINANCE_METHOD_MARKET_END;
		}
		else
			if (CmnDef.IS_FINANCE_STOCK_MODE)
			{
				method_index_list[0] = CmnDef.FINANCE_METHOD_STOCK_START;
				method_index_list[1] = CmnDef.FINANCE_METHOD_STOCK_END;
			}
			else
				throw new IllegalStateException("Unknown finance mode");
		return method_index_list;
	}

	static int get_method_size()
	{
		if (CmnDef.IS_FINANCE_MARKET_MODE)
			return CmnDef.FinanceMethod.get_market_method_amount();
		else
			if (CmnDef.IS_FINANCE_STOCK_MODE)
				return CmnDef.FinanceMethod.get_stock_method_amount();
			else
				throw new IllegalStateException("Unknown finance mode");
	}

	static int get_method_index_from_description(String method_description)
	{
		int method_index = Arrays.asList(CmnDef.FINANCE_METHOD_DESCRIPTION_LIST).indexOf(method_description);
		if (method_index == -1)
			CmnLogger.format_warn("Unknown source type description: %s", method_description);
		return method_index;
	}

	static LinkedList<Integer> get_all_method_index_list()
	{
		int[] method_index_range = get_method_index_range();
		LinkedList<Integer> method_index_list = new LinkedList<Integer>();
		for (int index = method_index_range[0]; index < method_index_range[1]; index++)
			method_index_list.add(index);
		return method_index_list;
	}

	static LinkedList<Integer> get_all_stock_statement_method_index_list()
	{
		LinkedList<Integer> stock_statement_method_index_list = new LinkedList<Integer>();
		for (int index = CmnDef.FINANCE_METHOD_STOCK_STATMENT_START; index < CmnDef.FINANCE_METHOD_STOCK_STATMENT_END; index++)
			stock_statement_method_index_list.add(index);
		return stock_statement_method_index_list;
	}

	static int get_source_key(int method_index)
	{
		if (!CmnDef.IS_FINANCE_MARKET_MODE)
		{
			String errmsg = "It's NOT Market mode";
			throw new IllegalStateException(errmsg);
		}
		return method_index;
	}

	static int get_source_key(int method_index, int company_group_number, String company_code_number)
	{
		if (!CmnDef.IS_FINANCE_STOCK_MODE)
		{
			String errmsg = "It's NOT Stock mode";
			throw new IllegalStateException(errmsg);
		}
		int company_code_number_int = Integer.valueOf(company_code_number);
		return ((company_group_number << CmnDef.SOURCE_KEY_COMPANY_GROUP_NUMBER_BIT_OFFSET) | (company_code_number_int << CmnDef.SOURCE_KEY_COMPANY_CODE_NUMBER_BIT_OFFSET) | (method_index << CmnDef.SOURCE_KEY_SOURCE_TYPE_INDEX_BIT_OFFSET));
	}

	static int get_method(int source_key)
	{
		return ((source_key & CmnDef.SOURCE_KEY_SOURCE_TYPE_INDEX_MASK) >> CmnDef.SOURCE_KEY_SOURCE_TYPE_INDEX_BIT_OFFSET);
	}

	static String get_company_code_number(int source_key)
	{
		if (!CmnDef.IS_FINANCE_STOCK_MODE)
		{
			String errmsg = "It's NOT Stock mode";
			throw new IllegalStateException(errmsg);
		}
		return String.format("%04d", (source_key & CmnDef.SOURCE_KEY_COMPANY_GROUP_NUMBER_MASK) >> CmnDef.SOURCE_KEY_COMPANY_CODE_NUMBER_BIT_OFFSET);
	}

	static int get_company_group_number(int source_key)
	{
		if (!CmnDef.IS_FINANCE_STOCK_MODE)
		{
			String errmsg = "It's NOT Stock mode";
			throw new IllegalStateException(errmsg);
		}
		return ((source_key & CmnDef.SOURCE_KEY_COMPANY_CODE_NUMBER_MASK) >> CmnDef.SOURCE_KEY_COMPANY_GROUP_NUMBER_BIT_OFFSET);
	}

	static String transform_date_str(int year_value, int month_value, int day_value)
	{
		return String.format("%d-%02d-%02d", year_value, month_value, day_value);
	}

	static String transform_month_str(int year_value, int month_value)
	{
		return String.format("%d-%02d", year_value, month_value);
	}

	static String transform_quarter_str(int year_value, int quarter_value)
	{
		return String.format("%dq%d", year_value, quarter_value);
	}

	static Matcher get_regex_matcher(String search_pattern, String data)
	{
		Pattern pattern = Pattern.compile(search_pattern);
		Matcher matcher = pattern.matcher(data);
		if (!matcher.find())
			return null;
		return matcher;
	}

	static Matcher get_finance_time_regex_matcher(CmnDef.FinanceTimeUnit finance_time_unit, String time_string)
	{
		return get_regex_matcher(CmnDef.FINANCE_TIME_REGEX_STRING_FORMAT_ARRAY[finance_time_unit.value()], time_string);
	}

	static Matcher check_date_str_format(String date_string)
	{
		// Time Format: yyyy-MM-dd; Ex: 2015-09-04
		Matcher matcher = get_finance_time_regex_matcher(CmnDef.FinanceTimeUnit.FinanceTime_Date, date_string);
		if (matcher == null)
		{
			String errmsg = String.format("The string[%s] is NOT date format", date_string);
			CmnLogger.error(errmsg);
			throw new IllegalArgumentException(errmsg);
		}
		int date_string_len = date_string.length();
		if ((date_string_len < CmnDef.DEF_MIN_DATE_STRING_LENGTH) || (date_string_len > CmnDef.DEF_MAX_DATE_STRING_LENGTH))
		{
			String errmsg = String.format("The date stirng[%s] length is NOT in the range [%d, %d]", date_string_len, CmnDef.DEF_MIN_DATE_STRING_LENGTH, CmnDef.DEF_MAX_DATE_STRING_LENGTH);
			CmnLogger.error(errmsg);
			throw new IllegalArgumentException(errmsg);
		}
		return matcher;
	}

	static Matcher check_month_str_format(String month_string)
	{
		// Time Format: yyyy-mm; Ex: 2015-09
		Matcher matcher = get_finance_time_regex_matcher(CmnDef.FinanceTimeUnit.FinanceTime_Month, month_string);
		if (matcher == null)
		{
			String errmsg = String.format("The string[%s] is NOT month format", month_string);
			CmnLogger.error(errmsg);
			throw new IllegalArgumentException(errmsg);
		}
		int month_string_len = month_string.length();
		if ((month_string_len < CmnDef.DEF_MIN_MONTH_STRING_LENGTH) || (month_string_len > CmnDef.DEF_MAX_MONTH_STRING_LENGTH))
		{
			String errmsg = String.format("The month stirng[%s] length is NOT in the range [%d, %d]", month_string_len, CmnDef.DEF_MIN_MONTH_STRING_LENGTH, CmnDef.DEF_MAX_MONTH_STRING_LENGTH);
			CmnLogger.error(errmsg);
			throw new IllegalArgumentException(errmsg);
		}
		return matcher;
	}

	static Matcher check_quarter_str_format(String quarter_string)
	{
		// Time Format: yyyy[Qq]q; Ex: 2015q1
		Matcher matcher = get_finance_time_regex_matcher(CmnDef.FinanceTimeUnit.FinanceTime_Quarter, quarter_string);
		if (matcher == null)
		{
			String errmsg = String.format("The string[%s] is NOT quarter format", quarter_string);
			CmnLogger.error(errmsg);
			throw new IllegalArgumentException(errmsg);
		}
		int quarter_string_len = quarter_string.length();
		if ((quarter_string_len < CmnDef.DEF_MIN_QUARTER_STRING_LENGTH) || (quarter_string_len > CmnDef.DEF_MAX_QUARTER_STRING_LENGTH))
		{
			String errmsg = String.format("The quarter stirng[%s] length is NOT in the range [%d, %d]", quarter_string_len, CmnDef.DEF_MIN_QUARTER_STRING_LENGTH, CmnDef.DEF_MAX_QUARTER_STRING_LENGTH);
			CmnLogger.error(errmsg);
			throw new IllegalArgumentException(errmsg);
		}
		return matcher;
	}

	static CmnDef.FinanceTimeUnit get_time_unit_from_string(String data)
	{
		// Find the time unit of the string
		for (int i = 0; i < CmnDef.FINANCE_TIME_REGEX_STRING_FORMAT_ARRAY.length; i++)
		{
			CmnDef.FinanceTimeUnit finance_time_unit = CmnDef.FinanceTimeUnit.values()[i];
			if (CmnFunc.get_finance_time_regex_matcher(finance_time_unit, data) != null)
				return finance_time_unit;
		}
		return CmnDef.FinanceTimeUnit.FinanceTime_Undefined;
	}

	static int[] get_date_value_list_from_str(String date_string)
	{
		Matcher matcher = check_date_str_format(date_string);
		return new int[]{Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher.group(3))};
	}

	static int[] get_month_value_list_from_str(String month_string)
	{
		Matcher matcher = check_month_str_format(month_string);
		return new int[]{Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2))};
	}

	static int[] get_quarter_value_list_from_str(String quarter_string)
	{
		Matcher matcher = check_quarter_str_format(quarter_string);
		return new int[]{Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2))};
	}

	static void check_year_range(int year_value)
	{
		if ((year_value < CmnDef.DEF_START_YEAR) || (year_value > CmnDef.DEF_END_YEAR))
			throw new IndexOutOfBoundsException(String.format("Year[%d] is NOT in range [%d, %d]", year_value, CmnDef.DEF_START_YEAR, CmnDef.DEF_END_YEAR));
	}

	static void check_month_range(int month_value)
	{
		if ((month_value < CmnDef.DEF_START_MONTH) || (month_value > CmnDef.DEF_END_MONTH))
			throw new IndexOutOfBoundsException(String.format("Month[%d] is NOT in range [%d, %d]", month_value, CmnDef.DEF_START_MONTH, CmnDef.DEF_END_MONTH));
	}

	static void check_quarter_range(int quarter_value)
	{
		if ((quarter_value < CmnDef.DEF_START_QUARTER) || (quarter_value > CmnDef.DEF_END_QUARTER))
			throw new IndexOutOfBoundsException(String.format("Quarter[%d] is NOT in range [%d, %d]", quarter_value, CmnDef.DEF_START_QUARTER, CmnDef.DEF_END_QUARTER));
	}

	static void check_day_range(int day_value, int year_value, int month_value)
	{
		int end_day_in_month = get_month_last_day(year_value, month_value);
		if ((day_value < CmnDef.DEF_START_DAY) || (day_value > end_day_in_month))
			throw new IndexOutOfBoundsException(String.format("Day[%d] is NOT in range [%d, %d]", day_value, CmnDef.DEF_START_DAY, end_day_in_month));
	}

	static int get_quarter_from_month(int month)
	{
		check_month_range(month);
		switch (month)
		{
			case 1 :
			case 2 :
			case 3 :
				return 1;
			case 4 :
			case 5 :
			case 6 :
				return 2;
			case 7 :
			case 8 :
			case 9 :
				return 3;
			case 10 :
			case 11 :
			case 12 :
				return 4;
		}
		// Impossible to reach
		return 0;
	}

	// private static final String get_code_position()
	// {
	// return String.format("%s:%d", ClassCmnBase.__FILE__(),
	// ClassCmnBase.__LINE__());
	// }

	// static String field_array_to_string(String[] field_array)
	// {
	// String field_string = null;
	// for (String field : field_array)
	// {
	// if (field_string == null)
	// field_string = field;
	// else
	// field_string += (COMMA_DATA_SPLIT + field);
	// }
	//
	// return field_string;
	// }

	// static String[] field_string_to_array(String field_string)
	// {
	// return field_string.split(DATA_SPLIT);
	// }

	static String get_current_path()
	{
		String cur_path = null;
		try
		{
			File cur_dir = new File(".");
			cur_path = cur_dir.getCanonicalPath();
		}
		catch (Exception e)
		{
			CmnLogger.error(String.format("Fail to get the current path: %s", e.toString()));
			return null;
		}
		return cur_path;
	}

	static int get_month_last_day(int year, int month)
	{
		check_year_range(year);
		check_month_range(month);
		Calendar calendar = Calendar.getInstance();
		// passing month-1 because 0-->jan, 1-->feb... 11-->dec
		calendar.set(year, month - 1, 1);
		calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));
		return calendar.get(Calendar.DATE);
	}

	static int get_quarter_first_month(int quarter)
	{
		check_quarter_range(quarter);
		return ((quarter * 3) - 2);
	}

	static int get_quarter_last_month(int quarter)
	{
		check_quarter_range(quarter);
		return (quarter * 3);
	}

	static int get_quarter_last_day(int quarter)
	{
		check_quarter_range(quarter);
		switch (quarter)
		{
			case 1 :
			case 4 :
				return 31;
			case 2 :
			case 3 :
				return 30;
		}
		// Should not reach
		return 0;
	}

	static java.util.Date get_date(String date_str) throws ParseException
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); // your
		// template
		// here
		java.util.Date date = formatter.parse(date_str);
		return date;
	}

	static java.util.Date get_month_date(String date_str) throws ParseException
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM"); // your
		// template
		// here
		java.util.Date date = formatter.parse(date_str);
		return date;
	}

	static final String get_time_folder_name()
	{
		Calendar cal = Calendar.getInstance();
		String time_string = String.format("%04d%02d%02d%02d%02d%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE), cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE), cal
				.get(Calendar.SECOND));
		return time_string;
	}

	// static final String get_month_str(java.util.Date date)
	// {
	// Calendar cal = Calendar.getInstance();
	// cal.setTime(date);
	// String time_month = String.format("%04d-%02d", cal.get(Calendar.YEAR),
	// cal.get(Calendar.MONTH) + 1);
	// return time_month;
	// }
	//
	// static final String get_date_str(java.util.Date date)
	// {
	// Calendar cal = Calendar.getInstance();
	// cal.setTime(date);
	// String time_date = String.format("%04d-%02d-%02d",
	// cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1,
	// cal.get(Calendar.DATE));
	// return time_date;
	// }

	// static final String get_time_month_today()
	// {
	// return get_month_str(new java.util.Date());
	// }
	//
	// static final String get_time_date_today()
	// {
	// return get_date_str(new java.util.Date());
	// }

	static short copy_file(String src_filepath, String dst_filepath)
	{
		Path src_filepath_obj = Paths.get(src_filepath);
		Path dst_filepath_obj = Paths.get(dst_filepath);
		try
		{
			CopyOption[] options = new CopyOption[]{StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES};
			CmnLogger.format_debug("Copy File; From: %s, To: %s", src_filepath, dst_filepath);
			Files.copy(src_filepath_obj, dst_filepath_obj, options);
		}
		catch (IOException e)
		{
			CmnLogger.format_error("Fail to copy file, due to: %s", e.toString());
			return CmnDef.RET_FAILURE_IO_OPERATION;
		}
		return CmnDef.RET_SUCCESS;
	}

	static short copy_config_file(String config_filename, String src_config_folderpath)
	{
		String src_filepath = String.format("%s/%s", src_config_folderpath, config_filename);
		String dst_filepath = String.format("%s/%s/%s", get_current_path(), CmnDef.CONF_FOLDERNAME, config_filename);
		return copy_file(src_filepath, dst_filepath);
	}

	static short create_symbolic_link(String newlink_filepath, String target_filepath)
	{
		if (check_file_exist(target_filepath))
		{
			CmnLogger.format_error("The file[%s] does NOT exist", target_filepath);
			return CmnDef.RET_FAILURE_NOT_FOUND;
		}
		try
		{
			Files.createSymbolicLink(Paths.get(newlink_filepath), Paths.get(target_filepath));
		}
		catch (IOException x)
		{
			CmnLogger.format_error("Fail to create the symbolic link of the file%s: %s", target_filepath);
			return CmnDef.RET_FAILURE_IO_OPERATION;
		}
		return CmnDef.RET_SUCCESS;
	}

	static short direct_string_to_output_stream(String data, String filepath)
	{
		// Open the config file for writing
		OutputStreamWriter osw = null;
		BufferedWriter bw = null;
		try
		{
			if (filepath != null) // To file
			{
				File f = new File(filepath);
				FileOutputStream fos = new FileOutputStream(f);
				osw = new OutputStreamWriter(fos);
			}
			else // To Standard Output
			{
				osw = new OutputStreamWriter(System.out);
			}
			bw = new BufferedWriter(osw);
		}
		catch (IOException e)
		{
			CmnLogger.format_error("Error occur while directing to output stream, due to: %s", e.toString());
			return CmnDef.RET_FAILURE_IO_OPERATION;
		}

		short ret = CmnDef.RET_SUCCESS;
		// Read the conf file
		try
		{
			bw.write(data);
			bw.flush();
		}
		catch (IOException e)
		{
			CmnLogger.format_error("Error occur while parsing the config file, due to: %s", e.toString());
			ret = CmnDef.RET_FAILURE_IO_OPERATION;
		}
		finally
		{
			if (bw != null)
			{
				try
				{
					bw.close();
				}
				catch (Exception e)
				{
				}
				bw = null;
			}
		}
		return ret;
	}

	static short direct_string_to_output_stream(String data)
	{
		return direct_string_to_output_stream(data, null);
	}

	static short execute_shell_command(String command, StringBuilder result_str_builder)
	{
		StringBuffer output = new StringBuffer();
		Process p;
		try
		{
			// In order to run the pipe command successfully
			String[] command_list = new String[]{"bash", "-c", command};
			p = Runtime.getRuntime().exec(command_list);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = "";
			while ((line = reader.readLine()) != null)
				output.append(line + "\n");
		}
		catch (InterruptedException e)
		{
			CmnLogger.format_error("Interrupted exception occurs while running command: %s", command);
			return CmnDef.RET_FAILURE_IO_OPERATION;
		}
		catch (IOException e)
		{
			CmnLogger.format_error("Exception occurs while running command: %s, due to: %s", command, e.toString());
			return CmnDef.RET_FAILURE_IO_OPERATION;
		}

		result_str_builder.append(output.toString());
		return CmnDef.RET_SUCCESS;
	}

	static boolean check_file_exist(final String filepath)
	{
		CmnLogger.format_debug("Check the file exist: %s", filepath);
		File f = new File(filepath);
		boolean exist = f.exists();
		return exist;
	}

	static boolean check_config_file_exist(final String config_filename, String config_folderpath)
	{
		String config_filepath = null;
		if (config_folderpath == null)
			config_filepath = String.format("%s/%s/%s", get_current_path(), CmnDef.CONF_FOLDERNAME, config_filename);
		else
			config_filepath = String.format("%s/%s", config_folderpath, config_filename);
		return check_file_exist(config_filepath);
	}

	static boolean check_config_file_exist(final String config_filename)
	{
		String config_filepath = String.format("%s/%s/%s", get_current_path(), CmnDef.CONF_FOLDERNAME, config_filename);
		return check_file_exist(config_filepath);
	}

	static short read_config_file_lines(String filename, String config_folderpath, LinkedList<String> config_line_list)
	{
		String config_filepath = null;
		if (config_folderpath == null)
			config_filepath = String.format("%s/%s/%s", get_current_path(), CmnDef.CONF_FOLDERNAME, filename);
		else
			config_filepath = String.format("%s/%s", config_folderpath, filename);
		// debug(String.format("Check the config file[%s] exist",
		// config_filepath));
		File f = new File(config_filepath);
		if (!f.exists())
		{
			CmnLogger.format_error("The configration file[%s] does NOT exist", config_filepath);
			return CmnDef.RET_FAILURE_NOT_FOUND;
		}

		// Open the config file for reading
		BufferedReader br = null;
		try
		{
			FileInputStream fis = new FileInputStream(f);
			InputStreamReader isr = new InputStreamReader(fis);
			br = new BufferedReader(isr);
		}
		catch (IOException e)
		{
			CmnLogger.format_error("Fails to open %s file, due to: %s", config_filepath, e.toString());
			return CmnDef.RET_FAILURE_IO_OPERATION;
		}

		short ret = CmnDef.RET_SUCCESS;
		// Read the conf file
		try
		{
			String line = null;
			while ((line = br.readLine()) != null)
			{
				if (line.startsWith("#"))
					continue;
				String line_strip = line.replace("\n", "");
				if (line_strip.isEmpty())
					continue;
				config_line_list.add(line_strip);
			}
		}
		catch (IOException e)
		{
			CmnLogger.format_error("IO Error occur while parsing the config file, due to: %s", e.toString());
			ret = CmnDef.RET_FAILURE_IO_OPERATION;
		}
		catch (Exception e)
		{
			CmnLogger.format_error("Error occur while parsing the config file, due to: %s", e.toString());
			ret = CmnDef.RET_FAILURE_UNKNOWN;
		}
		finally
		{
			if (br != null)
			{
				try
				{
					br.close();
				}
				catch (Exception e)
				{
				}
				br = null;
			}
		}
		return ret;
	}

	static short read_config_file_lines(String filename, LinkedList<String> config_line_list)
	{
		return read_config_file_lines(filename, null, config_line_list);
	}

	static short read_company_config_file(String conf_filename, LinkedList<String> company_config_list)
	{
		LinkedList<String> config_line_list = new LinkedList<String>();
		short ret = CmnDef.RET_SUCCESS;
		ret = read_config_file_lines(conf_filename, config_line_list);
		if (CmnDef.CheckFailure(ret))
			return ret;
		for (String line : config_line_list)
		{
			String[] param_list = line.split(CmnDef.SPACE_DATA_SPLIT);
			for (String param : param_list)
				company_config_list.addLast(param);
		}
		return CmnDef.RET_SUCCESS;
	}

	static short create_folder(final String path)
	{
		short ret = CmnDef.RET_SUCCESS;
		File file = new File(path);
		if (!file.mkdirs())
		{
			CmnLogger.format_error("Fail to create the folder: %s", path);
			ret = CmnDef.RET_FAILURE_IO_OPERATION;
		}
		return ret;
	}

	static short create_folder_if_not_exist(final String path)
	{
		if (!check_file_exist(path))
			return create_folder(path);
		return CmnDef.RET_SUCCESS;
	}

	static short create_folder_in_project_if_not_exist(final String foldername_in_project)
	{
		String folder_path = String.format("%s/%s", get_current_path(), foldername_in_project);
		return create_folder_if_not_exist(folder_path);
	}

	static short send_email(String title, String address, String content)
	{
		short ret = CmnDef.RET_SUCCESS;
		String cmd = String.format("echo \"%s\" | mail -s \"%s\" %s", content, title, address);
		try
		{
			StringBuilder result_str_builder = new StringBuilder();
			ret = execute_shell_command(cmd, result_str_builder);
			if (CmnDef.CheckFailure(ret))
				return ret;
			// Process p = Runtime.getRuntime().exec(cmd);
			// int exit_val = p.waitFor();
			// format_debug("The process of the command[%s] exit value: %d",
			// cmd, exit_val);
		}
		catch (Exception ex)
		{
			CmnLogger.format_error("Fail to run command: %s, due to: %s", cmd, ex.toString());
			return CmnDef.RET_FAILURE_UNKNOWN;
		}

		return ret;
	}

	// Index range: Positive: [0, data_size-1]; Negative: [-1, -data_size]
	static int get_index_ex(int index, int data_size)
	{
		return ((index < 0) ? index = data_size + index : index);
	}

	// Start index range: Positive: [0, data_size-1]; Negative: [-1, -data_size]
	static int get_start_index_ex(int index, int data_size)
	{
		return get_index_ex(index, data_size);
	}

	// End index range: Positive: [1, data_size]; Negative: [-1, -data_size]
	static int get_end_index_ex(int end_index, int data_size)
	{
		return ((end_index < 0) ? end_index = data_size + end_index + 1 : end_index);
	}

	static boolean check_start_index_in_range(int start_index, int range_start, int range_end)
	{
		assert range_start >= 0 : "range_start should be larger than 0";
		assert range_end >= 1 : "range_end should be larger than 1";
		return (((start_index >= range_start) && (start_index < range_end)) ? true : false);
	}

	static boolean check_end_index_in_range(int end_index, int range_start, int range_end)
	{
		assert range_start >= 0 : "range_start should be larger than 0";
		assert range_end >= 1 : "range_end should be larger than 1";
		return (((end_index > range_start) && (end_index <= range_end)) ? true : false);
	}

	static short get_subfolder_list(String folderpath, List<String> subfolder_list)
	{
		File dir = new File(folderpath);
		if (!dir.exists())
		{
			CmnLogger.format_error("The folder does Not exist", folderpath);
			return CmnDef.RET_FAILURE_NOT_FOUND;
		}
		String[] children = dir.list();
		if (children != null)
		{
			for (int i = 0; i < children.length; i++)
				subfolder_list.add(children[i]);
		}
		return CmnDef.RET_SUCCESS;
	}

	private static boolean delete_dir(File dir)
	{
		if (dir.isDirectory())
		{
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++)
			{
				boolean success = delete_dir(new File(dir, children[i]));
				if (!success)
					return false;
			}
		}
		// The directory is now empty or this is a file so delete it
		return dir.delete();
	}

	static short delete_subfolder(String folderpath)
	{
		File dir = new File(folderpath);
		if (!dir.exists())
		{
			// format_error("The folder does Not exist", folderpath);
			return CmnDef.RET_FAILURE_NOT_FOUND;
		}
		return delete_dir(dir) ? CmnDef.RET_SUCCESS : CmnDef.RET_FAILURE_UNKNOWN;
	}

	static short copy_dir(File src, File dest)
	{
		if (src.isDirectory())
		{
			// if directory not exists, create it
			if (!dest.exists())
			{
				dest.mkdir();
				CmnLogger.format_debug("The destination directory[%s] does NOT exist, create one", dest);
			}
			// list all the directory contents
			String files[] = src.list();
			for (String file : files)
			{
				// construct the src and dest file structure
				File srcFile = new File(src, file);
				File destFile = new File(dest, file);
				// recursive copy
				copy_dir(srcFile, destFile);
			}
		}
		else
		{
			// if file, then copy it. Use bytes stream to support all file types
			try
			{
				InputStream in = new FileInputStream(src);
				OutputStream out = new FileOutputStream(dest);
				byte[] buffer = new byte[1024];
				int length;
				// copy the file content in bytes
				while ((length = in.read(buffer)) > 0)
					out.write(buffer, 0, length);
				in.close();
				out.close();
				// System.out.println("File copied from " + src + " to " +
				// dest);
			}
			catch (FileNotFoundException e)
			{
				CmnLogger.format_error("File Not Found, due to: %s", e.toString());
				return CmnDef.RET_FAILURE_NOT_FOUND;
			}
			catch (IOException e)
			{
				CmnLogger.format_error("Error occurs while Operating IO, due to: %s", e.toString());
				return CmnDef.RET_FAILURE_IO_OPERATION;
			}
		}
		return CmnDef.RET_SUCCESS;
	}

	static short copy_folder(String src_folderpath, String dest_folderpath)
	{
		File src = new File(src_folderpath);
		if (!src.exists())
		{
			CmnLogger.format_error("Source Folder[%s] Not Found", src_folderpath);
			return CmnDef.RET_FAILURE_NOT_FOUND;
		}
		File dest = new File(dest_folderpath);
		return copy_dir(src, dest);
	}

	/**
	 * Compress (tar.gz) the input files to the output file
	 * 
	 * @param files
	 *           The files to compress
	 * @param output
	 *           The resulting output file (should end in .tar.gz)
	 * @throws IOException
	 */
	static void compress_files(Collection<File> files, File output) throws IOException
	{
		CmnLogger.debug("Compressing " + files.size() + " to " + output.getAbsoluteFile());
		// Create the output stream for the output file
		FileOutputStream fos = new FileOutputStream(output);
		// Wrap the output file stream in streams that will tar and gzip
		// everything
		TarArchiveOutputStream taos = new TarArchiveOutputStream(new GZIPOutputStream(new BufferedOutputStream(fos)));
		// TAR has an 8 gig file limit by default, this gets around that
		taos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR); // to get
		// past
		// the 8
		// gig
		// limit
		// TAR originally didn't support long file names, so enable the support
		// for it
		taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
		// Get to putting all the files in the compressed output file
		for (File f : files)
			add_files_to_compression(taos, f, ".");
		// Close everything up
		taos.close();
		fos.close();
	}

	static void compress_file(File file, File output) throws IOException
	{
		ArrayList<File> list = new ArrayList<File>(1);
		list.add(file);
		compress_files(list, output);
	}

	/**
	 * Does the work of compression and going recursive for nested directories
	 * Borrowed heavily from http://www.thoughtspark.org/node/53
	 * 
	 * @param taos
	 *           The archive
	 * @param file
	 *           The file to add to the archive
	 * @param dir
	 *           The directory that should serve as the parent directory in the
	 *           archive
	 * @throws IOException
	 */
	private static void add_files_to_compression(TarArchiveOutputStream taos, File file, String dir) throws IOException
	{
		// Create an entry for the file
		taos.putArchiveEntry(new TarArchiveEntry(file, dir + File.separator + file.getName()));
		if (file.isFile())
		{
			// Add the file to the archive
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
			IOUtils.copy(bis, taos);
			taos.closeArchiveEntry();
			bis.close();
		}
		else
			if (file.isDirectory())
			{
				// close the archive entry
				taos.closeArchiveEntry();
				// go through all the files in the directory and using recursion,
				// add them to the archive
				for (File childFile : file.listFiles())
				{
					add_files_to_compression(taos, childFile, file.getName());
				}
			}
	}

	static void decompress_file(File tar_file, File output) throws IOException
	{
		output.mkdir();
		TarArchiveInputStream tain = new TarArchiveInputStream(new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(tar_file))));

		TarArchiveEntry tarEntry = tain.getNextTarEntry();
		// create a file with the same name as the tarEntry
		while (tarEntry != null)
		{
			File output_path = new File(output, tarEntry.getName());
			// System.out.println("working: " + output_path.getCanonicalPath());
			if (tarEntry.isDirectory())
				output_path.mkdirs();
			else
			{
				output_path.createNewFile();
				// byte [] bto_read = new byte[(int)tarEntry.getSize()];
				byte[] bto_read = new byte[1024];
				// FileInputStream fin = new
				// FileInputStream(output_path.getCanonicalPath());
				BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(output_path));
				int len = 0;
				while ((len = tain.read(bto_read)) != -1)
				{
					bout.write(bto_read, 0, len);
				}
				bout.close();
				bto_read = null;
			}
			tarEntry = tain.getNextTarEntry();
		}
		tain.close();
	}

	static short get_config_file_timestamp(StringBuilder timestamp_string_builder, final String config_filename, final String config_folderpath)
	{
		String config_filepath = null;
		if (config_folderpath != null)
		{
			config_filepath = String.format("%s/%s", config_folderpath, config_filename);
		}
		else
		{
			config_filepath = String.format("%s/%s/%s", get_current_path(), CmnDef.CONF_FOLDERNAME, config_filename);
		}
		File f = new File(config_filepath);
		if (!f.exists())
		{
			CmnLogger.format_error("The configration file[%s] does NOT exist", config_filepath);
			return CmnDef.RET_FAILURE_NOT_FOUND;
		}

		// Open the config file for reading
		BufferedReader br = null;
		try
		{
			FileInputStream fis = new FileInputStream(f);
			InputStreamReader isr = new InputStreamReader(fis);
			br = new BufferedReader(isr);
		}
		catch (IOException e)
		{
			CmnLogger.format_error("Fails to open %s file, due to: %s", config_filepath, e.toString());
			return CmnDef.RET_FAILURE_IO_OPERATION;
		}

		short ret = CmnDef.RET_SUCCESS;
		// Read the conf file
		String line = null;
		try
		{
			if ((line = br.readLine()) == null)
			{
				throw new IOException(String.format("Fail to read the config file[%s]", config_filename));
			}
			String[] timestamp_element_array = line.split(" ");
			if (timestamp_element_array.length != 3)
			{
				throw new IllegalStateException(String.format("Incorrect format in the first line of the config[%s]: %s", config_filename, line));
			}
			if (!timestamp_element_array[0].equals(CmnDef.CONFIG_TIMESTAMP_STRING_PREFIX))
			{
				throw new IllegalStateException(String.format("Incorrect time stamp prefix in config[%s]: %s", config_filename, timestamp_element_array[0]));
			}
			timestamp_string_builder.append(String.format("%s %s", timestamp_element_array[1], timestamp_element_array[2]));
		}
		catch (IllegalStateException e)
		{
			CmnLogger.format_error("Incorrect time stamp format, due to: %s", e.toString());
			ret = CmnDef.RET_FAILURE_INCORRECT_CONFIG;
		}
		catch (IOException e)
		{
			CmnLogger.format_error("IO Error occur while parsing the config file, due to: %s", e.toString());
			ret = CmnDef.RET_FAILURE_IO_OPERATION;
		}
		catch (Exception e)
		{
			CmnLogger.format_error("Error occur while parsing the config file, due to: %s", e.toString());
			ret = CmnDef.RET_FAILURE_UNKNOWN;
		}
		finally
		{
			if (br != null)
			{
				try
				{
					br.close();
				}
				catch (Exception e)
				{
				}
				br = null;
			}
		}
		return ret;
	}

	public static <T> String assemble_list_to_string(List<T> list, String splitter)
	{
		StringBuilder list_string_builder = null;
		for (T elem : list)
		{
			if (list_string_builder == null)
				list_string_builder = new StringBuilder();
			else
				list_string_builder.append(splitter);
			list_string_builder.append(elem.toString());
		}
		return list_string_builder.toString();
	}

	public static String execute_shell_ommand(String command)
	{
		StringBuffer output = new StringBuffer();
		Process p;
		try
		{
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line = "";
			while ((line = reader.readLine()) != null)
			{
				output.append(line + "\n");
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return output.toString();

	}
}
