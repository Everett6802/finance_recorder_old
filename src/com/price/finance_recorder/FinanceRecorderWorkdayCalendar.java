package com.price.finance_recorder;

import java.io.*;


public class FinanceRecorderWorkdayCalendar
{
	private FinanceRecorderWorkdayCalendar(){}
	public Object clone() throws CloneNotSupportedException {throw new CloneNotSupportedException();}

	private static FinanceRecorderWorkdayCalendar workday_calendar = null;
	public static FinanceRecorderWorkdayCalendar get_instance()
	{
		if (workday_calendar == null)
			allocate();

		return workday_calendar;
	}

	private FinanceRecorderCmnDef.TimeRangeCfg time_range_cfg = null;

	private static synchronized void allocate() // For thread-safe
	{
		if (workday_calendar == null)
		{
			workday_calendar = new FinanceRecorderWorkdayCalendar();
			workday_calendar.initialize();
		} 
	}

	private void initialize()
	{
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		
	}

	protected short parse_config()
	{
// Open the file
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		BufferedReader reader = null;
		String conf_filepath = String.format("%s/%s/%s", FinanceRecorderCmnDef.get_current_path(), FinanceRecorderCmnDef.CONF_FOLDERNAME);
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
			boolean param_start = false;
			while ((buf = reader.readLine()) != null)
			{
				if (!param_start)
				{
					if (buf.startsWith(start_flag))
						param_start = true;
					continue;
				}
				else
				{
					if (buf.startsWith(stop_flag))
						break;
				}
				FinanceRecorderCmnDef.WriteDebugFormatSyslog(__FILE__(), __LINE__(), "Param content: %s", buf);
// Since the String::readLine read the data before the '\n' character, the data does NOT involve the '\n' character inside
//				int new_line_pos = buf.indexOf('\n');
				int split_pos = buf.indexOf('=');

// Get the config for each line
				if (buf.isEmpty())
					break;
				else if (split_pos < 0)
				{
// Incorrect config! Fail to find the 'new line' character in a specific line
					FinanceRecorderCmnDef.WriteErrorFormatSyslog(__FILE__(), __LINE__(), "Incorrect config: %s", buf);
					ret = FinanceRecorderCmnDef.RET_FAILURE_UNKNOWN;
					break;
				}
// Update the parameter value
				ret = parse_config_param(buf.substring(0, split_pos), buf.substring(split_pos + 1));
				if (FinanceRecorderCmnDef.CheckMsgDumperFailure(ret))
					break;
			}
		}
		catch (FileNotFoundException ex)
		{
			FinanceRecorderCmnDef.WriteDebugFormatSyslog(__FILE__(), __LINE__(), "The config file[%s] does NOT exist, use the default value", conf_filepath);
			return FinanceRecorderCmnDef.RET_FAILURE_OPEN_FILE;
		}
		catch (IOException ex)
		{
			FinanceRecorderCmnDef.WriteErrorFormatSyslog(__FILE__(), __LINE__(), "Error occur due to %s", ex.toString());
			ret = FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
		}
		finally 
		{
// Close the file
			if (reader != null)
			{
				try 
				{
					reader.close();
				}
				catch (IOException e){}// nothing to do here except log the exception
			}
		}
		
		return ret;
	}

}
