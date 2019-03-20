package com.price.finance_recorder_rest.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
//import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CmnFunc
{
//	private static Logger logger = Logger.getLogger(CmnFunc.class);
	private static Logger logger = LoggerFactory.getLogger(CmnFunc.class);
//	private static String current_path = null;
//	private static String parent_path = null;
	private static String user_home_path = null;
//	private static String dataset_path = null;
//	private static String dataset_conf_path = null;
//	private static String dataset_data_path = null;
//	private static String dataset_market_data_path = null;
//	private static String dataset_stock_data_path_format = null;
//	private static String dataset_stock_company_data_path_format = null;

// Can NOT get current path in the WebApp project
// Run the user home folder. Ex: /home/super
//	public static String get_current_path()
//	{
//		if (current_path == null)
//		{
//			try
//			{
//				File cur_dir = new File(".");
//				current_path = cur_dir.getCanonicalPath();
//			}
//			catch (Exception e)
//			{
//				logger.error(String.format("Fail to get the current path: %s", e.toString()));
//				return null;
//			}
//		}
//
//		return current_path;
//	}
//
//	public static String get_parent_path()
//	{
//		if (parent_path == null)
//		{
//			parent_path = get_current_path().substring(0, get_current_path().lastIndexOf("/"));
//		}
//		return parent_path;
//	}

	public static String get_user_home_path()
	{
		if (user_home_path == null)
		{
			try
			{
				File cur_dir = new File(".");
				user_home_path = cur_dir.getCanonicalPath();
			}
			catch (Exception e)
			{
				logger.error(String.format("Fail to get the user home path: %s", e.toString()));
				return null;
			}
		}

		return user_home_path;
	}

//	public static String get_current_path()
//	{
//		String cur_path = null;
//		try
//		{
//			File cur_dir = new File(".");
//			cur_path = cur_dir.getCanonicalPath();
//		}
//		catch (Exception e)
//		{
//			CmnLogger.error(String.format("Fail to get the current path: %s", e.toString()));
//			return null;
//		}
//		return cur_path;
//	}
	
	public static String get_dataset_absolute_path(String dataset_path)
	{
//		String dataset_absolute_path = null;
		if (dataset_path == null)
		{
//			return String.format("%s/%s", get_user_home_path(), CmnDef.FINANCE_DATASET_FOLDER_PATH);
			return CmnDef.FINANCE_DATASET_FOLDER_PATH;
		}
		else
		{
			if (dataset_path.startsWith("/"))
			{
// absolute path
				return dataset_path;
			}
			else
			{
// relative path
				return String.format("%s/%s", get_user_home_path(), dataset_path);
			}
		}
	}

	public static String get_dataset_conf_path(String dataset_path)
	{
		return String.format("%s/%s", get_dataset_absolute_path(dataset_path), CmnDef.CONF_FOLDERNAME);
	}

	public static String get_dataset_data_path(String dataset_path)
	{
		return String.format("%s/%s", get_dataset_absolute_path(dataset_path), CmnDef.DATA_FOLDERNAME);
	}

	public static String get_dataset_market_data_path(String dataset_path)
	{
		return String.format("%s/%s", get_dataset_data_path(dataset_path), CmnDef.DATASET_MARKET_FOLDERNAME);
	}

	public static String get_dataset_stock_data_path_format(String dataset_path)
	{
		return String.format("%s/%s", get_dataset_data_path(dataset_path), CmnDef.DATASET_STOCK_FOLDERNAME) + "%02d";
	}

	public static String get_dataset_stock_company_data_path_format(String dataset_path)
	{
		return get_dataset_stock_data_path_format(dataset_path) + "/%s";
	}

	public static String get_dataset_market_csv_filepath(int method_index, String dataset_path)
	{
		return String.format("%s/%s.csv", get_dataset_market_data_path(dataset_path), CmnDef.FINANCE_DATA_NAME_LIST[method_index]);
	}

	public static String get_dataset_stock_csv_filepath(int method_index, String company_number, int company_group_number, String dataset_path)
	{
		return String.format(get_dataset_stock_company_data_path_format(dataset_path), company_group_number, company_number) + String.format("/%s.csv", CmnDef.FINANCE_DATA_NAME_LIST[method_index]);
	}

	public static short execute_shell_command(String command, StringBuilder result_str_builder)
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
			logger.error(String.format("Interrupted exception occurs while running command: %s", command));
			return CmnDef.RET_FAILURE_IO_OPERATION;
		}
		catch (IOException e)
		{
			logger.error(String.format("Exception occurs while running command: %s, due to: %s", command, e.toString()));
			return CmnDef.RET_FAILURE_IO_OPERATION;
		}

		result_str_builder.append(output.toString());
		return CmnDef.RET_SUCCESS;
	}

	public static boolean check_file_exist(final String filepath)
	{
		logger.debug(String.format("Check the file exist: %s", filepath));
		File f = new File(filepath);
		boolean exist = f.exists();
		return exist;
	}

	public static short read_file_lines(BufferedReader br, List<String> line_list)
	{
		short ret = CmnDef.RET_SUCCESS;
		// Read the conf file
		try
		{
			String line = null;
			while ((line = br.readLine()) != null)
			{
				if (line.startsWith("#"))
					continue;
				String line_strip = line.replace("\n", "").replace("\r", "");
				if (line_strip.isEmpty())
					continue;
				line_list.add(line_strip);
			}
		}
		catch (IOException e)
		{
			logger.error(String.format("IO Error occur while parsing the config file, due to: %s", e.toString()));
			ret = CmnDef.RET_FAILURE_IO_OPERATION;
		}
		catch (Exception e)
		{
			logger.error(String.format("Error occur while parsing the config file, due to: %s", e.toString()));
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

	public static short read_file_line_count(BufferedReader br, int[] line_count)
	{
		short ret = CmnDef.RET_SUCCESS;
		int count = 0;
		// Read the conf file
		try
		{
			String line = null;
			while ((line = br.readLine()) != null)
			{
				if (line.startsWith("#"))
					continue;
				String line_strip = line.replace("\n", "").replace("\r", "");
				if (line_strip.isEmpty())
					continue;
				count++;
			}
		}
		catch (IOException e)
		{
			logger.error(String.format("IO Error occur while parsing the config file, due to: %s", e.toString()));
			ret = CmnDef.RET_FAILURE_IO_OPERATION;
		}
		catch (Exception e)
		{
			logger.error(String.format("Error occur while parsing the config file, due to: %s", e.toString()));
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
		line_count[0] = count;
		return ret;
	}

// line_no, start from 0, ignore the line start from "#" and empty line
	public static short read_file_line(BufferedReader br, int line_no, StringBuilder line_cur)
	{
		short ret = CmnDef.RET_SUCCESS;
		int line_cnt = 0;

//		if (line_no < 0)
//		{
//			int[] line_count = new int[1];
//			ret = read_file_line_count(br, line_count);
//			if (CmnDef.CheckFailure(ret))
//				return ret;
//			int new_line_no = line_count[0] + line_no;
//			if (new_line_no < 0)
//			{
//				logger.error(String.format("Incorrect line number[%d]. The line count: %d", line_no, line_count[0]));
//				return CmnDef.RET_FAILURE_INVALID_ARGUMENT;	
//			}
//		}
// Read the conf file
		boolean line_found = false;
		try
		{
			String line = null;
			while ((line = br.readLine()) != null)
			{
				if (line.startsWith("#"))
					continue;
				String line_strip = line.replace("\n", "").replace("\r", "");
				if (line_strip.isEmpty())
					continue;
				if (line_cnt == line_no)
				{
					line_cur.append(line);
					line_found = true;
					break;
				}
				line_cnt++;
			}
		}
		catch (IOException e)
		{
			logger.error(String.format("IO Error occur while parsing the config file, due to: %s", e.toString()));
			ret = CmnDef.RET_FAILURE_IO_OPERATION;
		}
		catch (Exception e)
		{
			logger.error(String.format("Error occur while parsing the config file, due to: %s", e.toString()));
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
		if (!line_found)
		{
			String.format("Fail to find the line: %d ", line_no);
			return CmnDef.RET_FAILURE_UNKNOWN;
		}
		return ret;
	}

	public static short read_file_lines(String filepath, List<String> line_list)
	{
		File f = new File(filepath);
		if (!f.exists())
		{
			logger.error(String.format("The configration file[%s] does NOT exist", filepath));
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
			logger.error(String.format("Fails to open %s file, due to: %s", filepath, e.toString()));
			return CmnDef.RET_FAILURE_IO_OPERATION;
		}

		return read_file_lines(br, line_list);
	}

	public static short read_file_line_count(String filepath, int[] line_count)
	{
		File f = new File(filepath);
		if (!f.exists())
		{
			logger.error(String.format("The configration file[%s] does NOT exist", filepath));
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
			logger.error(String.format("Fails to open %s file, due to: %s", filepath, e.toString()));
			return CmnDef.RET_FAILURE_IO_OPERATION;
		}

		return read_file_line_count(br, line_count);
	}

// line_no, start from 0, ignore the line start from "#" and empty line
	public static short read_file_line(String filepath, int line_no, StringBuilder line_cur)
	{
		File f = new File(filepath);
		if (!f.exists())
		{
			logger.error(String.format("The configration file[%s] does NOT exist", filepath));
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
			logger.error(String.format("Fails to open %s file, due to: %s", filepath, e.toString()));
			return CmnDef.RET_FAILURE_IO_OPERATION;
		}

		return read_file_line(br, line_no, line_cur);
	}

	public static short read_file_lines(String filename, String folderpath, List<String> line_list)
	{
		return read_file_lines(String.format("%s/%s", folderpath, filename), line_list);
	}

	public static short read_file_line_count(String filename, String folderpath, int[] line_count)
	{
		return read_file_line_count(String.format("%s/%s", folderpath, filename), line_count);
	}

	public static short read_file_line(String filename, String folderpath, int line_no, StringBuilder line_cur)
	{
		return read_file_line(String.format("%s/%s", folderpath, filename), line_no, line_cur);
	}

	public static short read_config_file_lines(String filename, String folderpath, List<String> config_line_list)
	{
//		if (folderpath == null)
//			folderpath =  get_current_path();
		String config_folderpath = String.format("%s/%s", folderpath, CmnDef.CONF_FOLDERNAME);
		return read_file_lines(filename, config_folderpath, config_line_list);
	}

//	public static short read_config_file_lines(String filename, LinkedList<String> config_line_list)
//	{
//		return read_config_file_lines(filename, null, config_line_list);
//	}

	public static short read_dataset_config_file_lines(String filename, String folderpath, List<String> config_line_list)
	{
		if (folderpath == null)
			folderpath =  get_dataset_absolute_path(null);
		String config_folderpath = String.format("%s/%s", folderpath, CmnDef.CONF_FOLDERNAME);
		return read_file_lines(filename, config_folderpath, config_line_list);
	}
	public static short read_dataset_config_file_lines(String filename, List<String> config_line_list)
	{
		return read_dataset_config_file_lines(filename, null, config_line_list);
	}
	
	public static short create_folder(final String path)
	{
		short ret = CmnDef.RET_SUCCESS;
		File file = new File(path);
		if (!file.mkdirs())
		{
			logger.error(String.format("Fail to create the folder: %s", path));
			ret = CmnDef.RET_FAILURE_IO_OPERATION;
		}
		return ret;
	}

	public static short create_folder_if_not_exist(final String path)
	{
		if (!check_file_exist(path))
			return create_folder(path);
		return CmnDef.RET_SUCCESS;
	}

	public static java.util.Date get_date(String date_str) throws ParseException
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); // your template here
		java.util.Date date = formatter.parse(date_str);
		return date;
	}

	public static java.util.Date get_month_date(String date_str) throws ParseException
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM"); // your template here
		java.util.Date date = formatter.parse(date_str);
		return date;
	}

	public static long get_date_integer(String date_str) throws ParseException
	{
		return get_date(date_str).getTime();
	}
}
