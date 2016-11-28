package com.price.finance_recorder_base;

import java.sql.*;
import java.text.*;
import java.util.*;

import com.price.finance_recorder_cmn.FinanceRecorderClassBase;
import com.price.finance_recorder_cmn.FinanceRecorderCmnClass;
import com.price.finance_recorder_cmn.FinanceRecorderCmnDef;
//import com.price.finance_recorder_cmn.FinanceRecorderCmnClass.FinanceDateRange;
//import com.price.finance_recorder_cmn.FinanceRecorderCmnClass.FinanceMonthRange;
//import com.price.finance_recorder_cmn.FinanceRecorderCmnClass.FinanceQuarterRange;


public class FinanceRecorderSQLClient extends FinanceRecorderClassBase
{
	private static final String DEF_SERVER = "localhost";
	private static final String DEF_USERNAME = "root";
	private static final String DEF_PASSWORD = "lab4man1";
// Create Database command format
	private static final String FORMAT_CMD_CREATE_DATABASE = "CREATE DATABASE %s";
// Delete Database command format
	private static final String FORMAT_CMD_DELETE_DATABASE = "DROP DATABASE IF EXISTS %s";
// Create Table command format
	private static final String FORMAT_CMD_CREATE_TABLE_HEAD_FORMAT = "CREATE TABLE IF NOT EXISTS %s (";
	private static final String FORMAT_CMD_CREATE_TABLE_TAIL = ")";
// Create Table command format
	private static final String FORMAT_CMD_DELETE_TABLE_FORMAT = "DROP TABLE IF EXISTS %s";
//	private static final String format_cmd_insert_into_table = "INSERT INTO sql%s VALUES(\"%s\", \"%s\", %d, \"%s\")";
// Insert Data command format
	private static final String FORMAT_CMD_INSERT_TABLE_HEAD_FORMAT = "INSERT INTO %s VALUES(";
	private static final String FORMAT_CMD_INSERT_TABLE_TAIL = ")";
// Select Data command format
	private static final String FORMAT_CMD_SELECT_HEAD = "SELECT ";
	private static final String FORMAT_CMD_SELECT_TAIL_FORMAT = " FROM %s";
// Date
	private static final String FORMAT_CMD_SELECT_DATE_RULE_EQUAL = " WHERE date = ?";
	private static final String FORMAT_CMD_SELECT_DATE_RULE_BETWEEN = " WHERE date BETWEEN ? AND ?";
	private static final String FORMAT_CMD_SELECT_DATE_RULE_GREATER_THAN = " WHERE date > ?";
	private static final String FORMAT_CMD_SELECT_DATE_RULE_LESS_THAN = " WHERE date < ?";
	private static final String FORMAT_CMD_SELECT_DATE_RULE_GREATER_EQUAL_THAN = " WHERE date >= ?";
	private static final String FORMAT_CMD_SELECT_DATE_RULE_LESS_EQUAL_THAN = " WHERE date <= ?";
// Fail to find the mysql command in different year
//// Month
//	private static final String FORMAT_CMD_SELECT_MONTH_RULE_BETWEEN_FORMAT = " WHERE year(date) = '%d' AND month(date) BETWEEN '%d' AND '%d'";
//	private static final String FORMAT_CMD_SELECT_MONTH_RULE_GREATER_THAN_FORMAT = " WHERE year(date) = '%d' AND month(date) > '%d'";
//	private static final String FORMAT_CMD_SELECT_MONTH_RULE_LESS_THAN_FORMAT = " WHERE year(date) = '%d' AND month(date) < '%d'";
//	private static final String FORMAT_CMD_SELECT_MONTH_RULE_GREATER_EQUAL_THAN_FORMAT = " WHERE year(date) = '%d' AND month(date) >= '%d'";
//	private static final String FORMAT_CMD_SELECT_MONTH_RULE_LESS_EQUAL_THAN_FORMAT = " WHERE year(date) = '%d' AND month(date) <= '%d'";
//// Quarter
//	private static final String FORMAT_CMD_SELECT_QUARTER_RULE_BETWEEN_FORMAT = " WHERE year(date) = '%d' AND quarter(date) BETWEEN '%d' AND '%d'";
//	private static final String FORMAT_CMD_SELECT_QUARTER_RULE_GREATER_THAN_FORMAT = " WHERE year(date) = '%d' AND quarter(date) > '%d'";
//	private static final String FORMAT_CMD_SELECT_QUARTER_RULE_LESS_THAN_FORMAT = " WHERE year(date) = '%d' AND quarter(date) < '%d'";
//	private static final String FORMAT_CMD_SELECT_QUARTER_RULE_GREATER_EQUAL_THAN_FORMAT = " WHERE year(date) = '%d' AND quarter(date) >= '%d'";
//	private static final String FORMAT_CMD_SELECT_QUARTER_RULE_LESS_EQUAL_THAN_FORMAT = " WHERE year(date) = '%d' AND quarter(date) <= '%d'";

	private static String field_array_to_string(String[] field_array)
	{
		String field_string = null;
		for (String field : field_array)
		{
			if (field_string == null)
				field_string = field;
			else
				field_string += (FinanceRecorderCmnDef.COMMA_DATA_SPLIT + field);
		}
		return field_string;
	}

	private static String[] field_string_to_array(String field_string)
	{
		return field_string.split(FinanceRecorderCmnDef.COMMA_DATA_SPLIT);
	}

	private static java.sql.Date get_sql_date(int[] date_value_list) throws ParseException
	{
		return get_sql_date(new FinanceRecorderCmnClass.FinanceDate(date_value_list));
	}
	private static java.sql.Date get_sql_date(String date_str) throws ParseException
	{
		return get_sql_date(new FinanceRecorderCmnClass.FinanceDate(date_str));
	}
	private static java.sql.Date get_sql_date(FinanceRecorderCmnClass.FinanceDate finance_date) throws ParseException
	{
		return new java.sql.Date(FinanceRecorderCmnClass.FinanceDate.get_java_date_object(finance_date).getTime());
	}
//	private static java.sql.Date get_sql_date(String date_str) throws ParseException
//	{
////		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); // your template here
////		java.util.Date dateStr = formatter.parse(date_str);
//		java.util.Date dateStr = FinanceRecorderCmnDef.get_date(date_str);
//		return new java.sql.Date(dateStr.getTime());
//	}

	public static short get_sql_field_command(int source_type_index, LinkedList<Integer> field_index_list, StringBuilder field_cmd_builder)
	{
		if (field_index_list.isEmpty())
			throw new IllegalArgumentException("The query should NOT be empty");
//		string field_cmd;
// Select all the fields in the table
		if ((int)field_index_list.size() == FinanceRecorderCmnDef.FINANCE_DATABASE_FIELD_AMOUNT_LIST[source_type_index] - 1) // Caution: Don't include the "date" field
			field_cmd_builder.append("*");
		else
		{
// Assemble the MySQL command of the designated field
// The "date" field is a must
			String field_cmd = String.format("%s", FinanceRecorderCmnDef.MYSQL_DATE_FILED_NAME);
//			int field_index_list_size = field_index_list.size();
//			for(int field_index = 0 ; field_index < field_index_list_size ; field_index++)
			ListIterator<Integer> iter = field_index_list.listIterator();
			while(iter.hasNext())
			{
//				snprintf(field_buf, 16, ",%s%d", MYSQL_FILED_NAME_BASE, field_index_list[field_index]);
//				field_cmd += string(field_buf);
				Integer index = iter.next();
				field_cmd += String.format(",%s%d", FinanceRecorderCmnDef.MYSQL_FILED_NAME_BASE, index);
			}
			field_cmd_builder.append(field_cmd);
		}
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	public static short get_sql_field_index_list(int source_type_index, String field_cmd, LinkedList<Integer> field_index_list)
	{
		if (field_cmd.equals(""))
			throw new IllegalArgumentException("The field_cmd should NOT be empty");
		String[] finance_data_sql_field_definition = FinanceRecorderCmnDef.FINANCE_DATA_SQL_FIELD_DEFINITION_LIST[source_type_index];
//		String[] finance_data_sql_field_type_definition = FinanceRecorderCmnDef.FINANCE_DATA_SQL_FIELD_TYPE_DEFINITION_LIST[source_type_index];

		if (field_cmd.equals("*"))
		{
			int finance_data_sql_field_definition_len = finance_data_sql_field_definition.length;
			for(int i = 0 ; i < finance_data_sql_field_definition_len ; i++)
				field_index_list.add(i);
		}
		else
		{
			String[] table_field_index_list = field_cmd.split(",");
			int table_field_index_list_len = table_field_index_list.length;
			int index;
			List<String> finance_data_sql_field_definition_list = Arrays.asList(finance_data_sql_field_definition);
			for(int i = 0 ; i < table_field_index_list_len ; i++)
			{
				index = finance_data_sql_field_definition_list.indexOf(table_field_index_list[i]);
				if (index == -1)
				{
					FinanceRecorderCmnDef.format_error("Unknown field: %s in %s", table_field_index_list[i], FinanceRecorderCmnDef.FINANCE_DATA_DESCRIPTION_LIST[source_type_index]);
					return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
				}
				field_index_list.add(index);
			}
		}
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	private static PreparedStatement prepare_query_sql_statement(Connection connection, String table_name, String cmd_table_field, FinanceRecorderCmnClass.FinanceTimeRange finance_time_range) throws SQLException, ParseException
	{
// Generate the SQL command for querying
		String cmd_select_data = FORMAT_CMD_SELECT_HEAD + cmd_table_field + String.format(FORMAT_CMD_SELECT_TAIL_FORMAT, table_name);
// Create the prepare statement
		PreparedStatement pstmt = null;
		if (finance_time_range != null)
		{
			FinanceRecorderCmnDef.FinanceTimeUnit finance_time_unit = finance_time_range.get_time_unit();
			FinanceRecorderCmnDef.FinanceTimeRangeType finance_time_range_type = finance_time_range.get_time_range_type();
			FinanceRecorderCmnDef.format_error("Time rangte, unit: %d, range type: %d", finance_time_unit.value(), finance_time_range_type.value());
			switch (finance_time_range.get_time_unit())
			{
			case FinanceTime_Date:
			{
// Try to create the prepared statement from date unit
				try
				{
					switch (finance_time_range_type)
					{
					case FinanceTimeRange_Between:
					{
						cmd_select_data += FORMAT_CMD_SELECT_DATE_RULE_BETWEEN;
						pstmt = connection.prepareStatement(cmd_select_data);
						pstmt.setDate(1, get_sql_date(finance_time_range.get_time_start_value_list()));
						pstmt.setDate(2, get_sql_date(finance_time_range.get_time_end_value_list()));
					}
					break;
					case FinanceTimeRange_LessEqual:
					{
						cmd_select_data += FORMAT_CMD_SELECT_DATE_RULE_LESS_EQUAL_THAN;
						pstmt = connection.prepareStatement(cmd_select_data);
						pstmt.setDate(1, get_sql_date(finance_time_range.get_time_end_value_list()));
					}
					break;
					case FinanceTimeRange_GreaterEqual:
					{
						cmd_select_data += FORMAT_CMD_SELECT_DATE_RULE_GREATER_EQUAL_THAN;
						pstmt = connection.prepareStatement(cmd_select_data);
						pstmt.setDate(1, get_sql_date(finance_time_range.get_time_start_value_list()));
					}
					break;
					default:
						throw new IllegalArgumentException(String.format("Unsupported time range: %d", finance_time_range_type.value()));
					}
				}
				catch (SQLException e)
				{
					FinanceRecorderCmnDef.format_error("Fail to prepare MySQL command for querying data in date unit, due to: %s", e.toString());
					throw e;
				}
			}
			break;
			case FinanceTime_Month:
			{
// Try to create the prepared statement from month unit
				try
				{
					switch (finance_time_range_type)
					{
					case FinanceTimeRange_Between:
					{
						cmd_select_data += FORMAT_CMD_SELECT_DATE_RULE_BETWEEN;
						pstmt = connection.prepareStatement(cmd_select_data);
						FinanceRecorderCmnClass.FinanceMonth finance_month_start = new FinanceRecorderCmnClass.FinanceMonth(finance_time_range.get_time_start_value_list());
						FinanceRecorderCmnClass.FinanceMonth finance_month_end = new FinanceRecorderCmnClass.FinanceMonth(finance_time_range.get_time_end_value_list());
						pstmt.setDate(1, get_sql_date(finance_month_start.get_date_start_object()));
						pstmt.setDate(2, get_sql_date(finance_month_end.get_date_end_object()));
					}
					break;
					case FinanceTimeRange_LessEqual:
					{
						cmd_select_data += FORMAT_CMD_SELECT_DATE_RULE_LESS_EQUAL_THAN;
						FinanceRecorderCmnClass.FinanceMonth finance_month_end = new FinanceRecorderCmnClass.FinanceMonth(finance_time_range.get_time_end_value_list());
						pstmt = connection.prepareStatement(cmd_select_data);
						pstmt.setDate(1, get_sql_date(finance_month_end.get_date_end_object()));
					}
					break;
					case FinanceTimeRange_GreaterEqual:
					{
						cmd_select_data += FORMAT_CMD_SELECT_DATE_RULE_GREATER_EQUAL_THAN;
						FinanceRecorderCmnClass.FinanceMonth finance_month_start = new FinanceRecorderCmnClass.FinanceMonth(finance_time_range.get_time_start_value_list());
						pstmt = connection.prepareStatement(cmd_select_data);
						pstmt.setDate(1, get_sql_date(finance_month_start.get_date_start_object()));
					}
					break;
					default:
						throw new IllegalArgumentException(String.format("Unsupported time range: %d", finance_time_range_type.value()));
					}
				}
				catch (SQLException e)
				{
					FinanceRecorderCmnDef.format_error("Fail to prepare MySQL command for querying data in month unit, due to: %s", e.toString());
					throw e;
				}
				catch (ParseException e)
				{
					FinanceRecorderCmnDef.format_error("Fail to parse mysql date in quarter unit, due to: %s", e.toString());
					throw e;
				}
			}
			break;
			case FinanceTime_Quarter:
			{
// Try to create the prepared statement from quartewr unit
				try
				{
					switch (finance_time_range_type)
					{
					case FinanceTimeRange_Between:
					{
						cmd_select_data += FORMAT_CMD_SELECT_DATE_RULE_BETWEEN;
						pstmt = connection.prepareStatement(cmd_select_data);
						FinanceRecorderCmnClass.FinanceQuarter finance_quarter_start = new FinanceRecorderCmnClass.FinanceQuarter(finance_time_range.get_time_start_value_list());
						FinanceRecorderCmnClass.FinanceQuarter finance_quarter_end = new FinanceRecorderCmnClass.FinanceQuarter(finance_time_range.get_time_end_value_list());
						pstmt.setDate(1, get_sql_date(finance_quarter_start.get_date_start_object()));
						pstmt.setDate(2, get_sql_date(finance_quarter_end.get_date_end_object()));
					}
					break;
					case FinanceTimeRange_LessEqual:
					{
						cmd_select_data += FORMAT_CMD_SELECT_DATE_RULE_LESS_EQUAL_THAN;
						FinanceRecorderCmnClass.FinanceQuarter finance_quarter_end = new FinanceRecorderCmnClass.FinanceQuarter(finance_time_range.get_time_end_value_list());
						pstmt = connection.prepareStatement(cmd_select_data);
						pstmt.setDate(1, get_sql_date(finance_quarter_end.get_date_end_object()));
					}
					break;
					case FinanceTimeRange_GreaterEqual:
					{
						cmd_select_data += FORMAT_CMD_SELECT_DATE_RULE_GREATER_EQUAL_THAN;
						FinanceRecorderCmnClass.FinanceQuarter finance_quarter_start = new FinanceRecorderCmnClass.FinanceQuarter(finance_time_range.get_time_start_value_list());
						pstmt = connection.prepareStatement(cmd_select_data);
						pstmt.setDate(1, get_sql_date(finance_quarter_start.get_date_start_object()));
					}
					break;
					default:
						throw new IllegalArgumentException(String.format("Unsupported time range: %d", finance_time_range_type.value()));
					}
				}
				catch (SQLException e)
				{
					FinanceRecorderCmnDef.format_error("Fail to prepare MySQL command for querying data in quarter unit, due to: %s", e.toString());
					throw e;
				}
				catch (ParseException e)
				{
					FinanceRecorderCmnDef.format_error("Fail to parse mysql date in quarter unit, due to: %s", e.toString());
					throw e;
				}
			}
			break;
			default:
				throw new IllegalStateException(String.format("Unknow time unit: %d", finance_time_unit.value()));
			}
		}
		else
		{
// Try to create the prepare statement
			try
			{
				pstmt = connection.prepareStatement(cmd_select_data);
			}
			catch (SQLException e)
			{
				FinanceRecorderCmnDef.format_error("Fail to prepare MySQL command for querying data, due to: %s", e.toString());
				throw e;
			}
		}
		return pstmt;
	}

	private Connection connection = null; //Database objects, 連接object
	private String server = null;
	private String username = null;
	private String password = null;
//	private String database_name = null;
//	private String table_name = null;
//	private FinanceRecorderCmnDef.FinanceObserverInf finance_observer = null;
//	private int source_type_index;
//	private FinanceRecorderCmnDef.DatabaseNotExistIngoreType database_not_exist_ignore_type = null; 
//	private FinanceRecorderCmnDef.DatabaseCreateThreadType database_create_thread_type = null;
	private FinanceRecorderCmnDef.DatabaseEnableBatchType batch_operation = FinanceRecorderCmnDef.DatabaseEnableBatchType.DatabaseEnableBatch_No;

	protected FinanceRecorderSQLClient()
	{
		// source_type_index = finance_data_type.ordinal();
		server = DEF_SERVER;
		username = DEF_USERNAME;
		password = DEF_PASSWORD;
//		finance_observer = observer;
	}

	public short try_connect_mysql(
			String database_name, 
			FinanceRecorderCmnDef.DatabaseNotExistIngoreType database_not_exist_ignore_type, 
			FinanceRecorderCmnDef.DatabaseCreateThreadType database_create_thread_type
		)
	{
//		database_name = database;
		FinanceRecorderCmnDef.format_debug("Try to connect to the MySQL database server[%s]......", database_name);
//		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
// Create the connection to the MySQL server and database
		String cmd_create_database = String.format(FORMAT_CMD_CREATE_DATABASE, database_name);
		FinanceRecorderCmnDef.format_debug("Create database by command: %s", cmd_create_database);

		FinanceRecorderCmnDef.debug("Try to connect to the MySQL database server...");
		try 
		{
// Java要連接資料庫時，需使用到JDBC-Driver，連接MySQL資料庫使用Connector/j，下載後解開壓縮，mysql-connector-java-5.1.15-bin.jar就是MySQL的JDBC-Driver.
// Go to http://blog.yslifes.com/archives/918 for more detailed info
// JDBC API is a Java API that can access any kind of tabular data, especially data stored in a Relational Database. 
// JDBC works with Java on a variety of platforms, such as Windows, Mac OS, and the various versions of UNIX.
// Go to http://www.tutorialspoint.com/jdbc/index.htm to see the example of MySQL command by JDBC
//註冊driver
			Class.forName("com.mysql.jdbc.Driver");
//jdbc:mysql://localhost/test?useUnicode=true&amp;characterEncoding=Big5: localhost是主機名, test是database名, useUnicode=true&amp;characterEncoding=Big5使用的編碼
			connection = DriverManager.getConnection(String.format("jdbc:mysql://localhost/%s?useUnicode=true&amp;characterEncoding=utf-8", database_name), username, password);
		}
		catch(ClassNotFoundException ex)
		{
			String errmsg = "DriverClassNotFound:" + ex.toString();
			FinanceRecorderCmnDef.error(errmsg);
			if(FinanceRecorderCmnDef.is_show_console())
				System.err.println(errmsg);
			return FinanceRecorderCmnDef.RET_FAILURE_MYSQL_NO_DRIVER;
		}
		catch(SQLException ex) //有可能會產生sql exception
		{
			if (database_not_exist_ignore_type == FinanceRecorderCmnDef.DatabaseNotExistIngoreType.DatabaseNotExistIngore_Yes)
			{
				FinanceRecorderCmnDef.format_debug("The %s database does NOT exist, create a NEW one", database_name);
				try
				{
					connection = DriverManager.getConnection(String.format("jdbc:mysql://localhost/?user=%s&password=%s", username, password)); 
					Statement s = connection.createStatement();
					FinanceRecorderCmnDef.format_debug("Try to create database by command: %s", cmd_create_database);
					try
					{
						s.executeUpdate(cmd_create_database);
					}
					catch(SQLException ex1) //有可能會產生sql exception
					{
						if (database_create_thread_type == FinanceRecorderCmnDef.DatabaseCreateThreadType.DatabaseCreateThread_Multiple && ex.getErrorCode() == 1049)
							FinanceRecorderCmnDef.format_warn("The database[%s] has already existed", database_name);
						else
							throw ex1;
					}
// Destroy the connection first
					disconnect_mysql();
// Reconnect to MySQL
//					database_name = database;
					connection = DriverManager.getConnection(String.format("jdbc:mysql://localhost/%s?useUnicode=true&amp;characterEncoding=utf-8", database_name), username, password);
				}
				catch(SQLException ex1) //有可能會產生sql exception
				{
					String errmsg = "Exception:" + ex1.toString();
					FinanceRecorderCmnDef.error(errmsg);
					if(FinanceRecorderCmnDef.is_show_console())
						System.err.println(errmsg);
					return FinanceRecorderCmnDef.RET_FAILURE_MYSQL;
				}
			}
			else
			{
				String errmsg = "Exception:" + ex.toString();
				FinanceRecorderCmnDef.error(errmsg);
				if(FinanceRecorderCmnDef.is_show_console())
					System.err.println(errmsg);
				if (ex.getErrorCode() == 1049)
					return FinanceRecorderCmnDef.RET_FAILURE_MYSQL_UNKNOWN_DATABASE;
				else
					return FinanceRecorderCmnDef.RET_FAILURE_MYSQL;
			}
		}

		FinanceRecorderCmnDef.format_debug("Try to connect to the MySQL database server[%s]...... Successfully", database_name);
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	public short disconnect_mysql()
	{
		FinanceRecorderCmnDef.format_debug("Disconnect from the MySQL database server...");
		if (connection != null)
		{
			try 
			{
				connection.close();
			} 
			catch (SQLException e) 
			{
				FinanceRecorderCmnDef.format_error("Fail to close the connection to MySQL, due to %s", e.toString());
				return FinanceRecorderCmnDef.RET_FAILURE_MYSQL; 
			}
			connection = null;
		}
//		database_name = null;

		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	protected short delete_database(String database_name)
	{
// Check if the connection is established
		if (connection == null)
		{
			FinanceRecorderCmnDef.error("The connection is NOT established");
			return  FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
		}

// Delete database
		String cmd_delete_database = String.format(FORMAT_CMD_DELETE_DATABASE, database_name);
// Create the SQL command list and then execute
		try
		{
			Statement s = connection.createStatement();
			FinanceRecorderCmnDef.format_debug("Delete database[%s] by command: %s", database_name, cmd_delete_database);
			s.executeUpdate(cmd_delete_database);
		}
		catch(SQLException ex) //有可能會產生sql exception
		{
			FinanceRecorderCmnDef.format_error("Fails to delete database[%s], due to: %s", database_name, ex.getMessage());
			return FinanceRecorderCmnDef.RET_FAILURE_MYSQL_EXECUTE_COMMAND;
		}
// Close the connection to MySQL
		disconnect_mysql();

		FinanceRecorderCmnDef.format_debug("Delete database[%s]...... Successfully", database_name);
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	protected short create_table(String table_name, String cmd_table_field)
	{
// Check if the connection is established
		if (connection == null)
		{
			FinanceRecorderCmnDef.error("The connection is NOT established");
			return  FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
		}
//		table_name = table;
		FinanceRecorderCmnDef.format_debug("Try to open the MySQL table[%s]...... ", table_name);
		String format_cmd_create_table_head = String.format(FORMAT_CMD_CREATE_TABLE_HEAD_FORMAT, table_name);
// Create table
		String cmd_create_table = format_cmd_create_table_head + cmd_table_field + FORMAT_CMD_CREATE_TABLE_TAIL;
		FinanceRecorderCmnDef.format_debug("Create table by command: %s", cmd_create_table);

// Create the SQL command list and then execute
		try
		{
			Statement s = connection.createStatement();
			FinanceRecorderCmnDef.format_debug("Try to create table[%s] by command: %s", table_name, cmd_create_table);
			s.executeUpdate(cmd_create_table);
		}
		catch(SQLException ex) //有可能會產生sql exception
		{
			if (ex.getErrorCode() == 1050)
				FinanceRecorderCmnDef.format_info("The table[%s] has already existed", table_name);
			else
			{
				FinanceRecorderCmnDef.format_error("Fails to create table[%s], due to: %s", table_name, ex.getMessage());
				return FinanceRecorderCmnDef.RET_FAILURE_MYSQL_EXECUTE_COMMAND;
			}
		}

		FinanceRecorderCmnDef.format_debug("Try to open the MySQL table[%s]...... Successfully", table_name);
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	protected short delete_table(String table_name)
	{
// Check if the connection is established
		if (connection == null)
		{
			FinanceRecorderCmnDef.error("The connection is NOT established");
			return  FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
		}
//		table_name = table;
		FinanceRecorderCmnDef.format_debug("Try to delete the MySQL table[%s]...... ", table_name);
		String cmd_delete_table = String.format(FORMAT_CMD_DELETE_TABLE_FORMAT, table_name);
// Create table
		FinanceRecorderCmnDef.format_debug("Delete table by command: %s", cmd_delete_table);

// Create the SQL command list and then execute
		try
		{
			Statement s = connection.createStatement();
			FinanceRecorderCmnDef.format_debug("Try to create table[%s] by command: %s", table_name, cmd_delete_table);
			s.executeUpdate(cmd_delete_table);
		}
		catch(SQLException ex) //有可能會產生sql exception
		{
			if (ex.getErrorCode() == 1050)
				FinanceRecorderCmnDef.format_info("The table[%s] has already existed", table_name);
			else
			{
				FinanceRecorderCmnDef.format_error("Fails to create table[%s], due to: %s", table_name, ex.getMessage());
				return FinanceRecorderCmnDef.RET_FAILURE_MYSQL_EXECUTE_COMMAND;
			}
		}

		FinanceRecorderCmnDef.format_debug("Try to open the MySQL table[%s]...... Successfully", table_name);
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

//	short get_table_name_list(List<String> table_name_list)
//	{
//// Check if the connection is established
//		if (connection == null)
//		{
//			FinanceRecorderCmnDef.error("The connection is NOT established");
//			return  FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
//		}
//
//// Create the SQL command list and then execute
//		try
//		{
//			DatabaseMetaData md = connection.getMetaData();
//			ResultSet rs = md.getTables(null, null, "%", null);
//			while (rs.next()) 
//			{
////				System.out.println(rs.getString(3));
//				table_name_list.add(rs.getString(3));
//			}
//		}
//		catch(SQLException ex) //有可能會產生sql exception
//		{
//			FinanceRecorderCmnDef.format_error("Fails to show tables[%s], due to: %s", ex.getMessage());
//			return FinanceRecorderCmnDef.RET_FAILURE_MYSQL_EXECUTE_COMMAND;
//		}
//
//		return FinanceRecorderCmnDef.RET_SUCCESS;
//	}

	protected short insert_data(String table_name, final FinanceRecorderCSVHandler csv_reader)
	{
// Check if the connection is established
		if (connection == null)
		{
			FinanceRecorderCmnDef.error("The connection is NOT established");
			return  FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
		}

		if (is_batch_operation())
		{
			// Disable Auto Commit
			try
			{
				connection.setAutoCommit(false);
			}
			catch (SQLException e)
			{
				FinanceRecorderCmnDef.format_debug("Fail to enable MySQL auto-comomit, due to: %s", e.toString());
				return FinanceRecorderCmnDef.RET_FAILURE_MYSQL;
			}
// Create the SQL command list and then execute
			PreparedStatement pstmt = null;
			short ret = FinanceRecorderCmnDef.RET_SUCCESS;
			int element_list_len = 0;
	OUT:
			for (String data : csv_reader)
			{
				String[] element_list = field_string_to_array(data);
				if (pstmt == null)
				{
					// Assemble all the element in the data
					element_list_len = element_list.length;
					String cmd_data = "?";
					for (int index = 1 ; index < element_list_len ; index++)
						cmd_data += ",?";
// Generate the SQL prepared batch command
					String format_cmd_insert_data_head = String.format(FORMAT_CMD_INSERT_TABLE_HEAD_FORMAT, table_name);
					String cmd_insert_data = format_cmd_insert_data_head + cmd_data + FORMAT_CMD_INSERT_TABLE_TAIL;
					try
					{
						pstmt = connection.prepareStatement(cmd_insert_data);
					}
					catch (SQLException e)
					{
						FinanceRecorderCmnDef.format_debug("Fail to prepare MySQL batch command, due to: %s", e.toString());
						ret = FinanceRecorderCmnDef.RET_FAILURE_MYSQL;
						break OUT;
					}
				}
				
// Transform the date field into SQL Date format
				java.sql.Date sql_date = null;
				try
				{
//					SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); // your template here
//					java.util.Date dateStr = formatter.parse(element_list[0]);
					java.util.Date dateStr = FinanceRecorderCmnDef.get_date(element_list[0]);
					sql_date = new java.sql.Date(dateStr.getTime());
				}
				catch (ParseException e)
				{
					FinanceRecorderCmnDef.format_debug("Fail to transform the MySQL time format, due to: %s", e.toString());
					ret = FinanceRecorderCmnDef.RET_FAILURE_MYSQL;
					break OUT;
				}
// Generate the SQL batch command
				try
				{
					pstmt.setDate(1, sql_date);
					for (int index = 1 ; index < element_list_len ; index++)
						pstmt.setString(index + 1, element_list[index]);
					pstmt.addBatch();
				}
				catch (SQLException e)
				{
					FinanceRecorderCmnDef.format_debug("Fail to prepare MySQL batch command, due to: %s", e.toString());
					ret = FinanceRecorderCmnDef.RET_FAILURE_MYSQL;
					break OUT;
				}
			}
			if (FinanceRecorderCmnDef.CheckSuccess(ret))
			{
// Execute the command to write the batch messages into the MySQL
				try
				{
//					FinanceRecorderCmnDef.format_debug("Insert into data by command: %s", pstmt);
					pstmt.executeBatch();
					connection.commit();
				}
				catch(SQLException ex) //有可能會產生sql exception
				{
					FinanceRecorderCmnDef.format_error("Fail to insert into data by batch command[%s], due to: %s", pstmt, ex.getMessage());
					return  FinanceRecorderCmnDef.RET_FAILURE_MYSQL_EXECUTE_COMMAND;
				}
			}
// Enable Auto Commit
			try
			{
				connection.setAutoCommit(true);
			}
			catch (SQLException e)
			{
				FinanceRecorderCmnDef.format_debug("Fail to enable MySQL auto-comomit, due to: %s", e.toString());
				return FinanceRecorderCmnDef.RET_FAILURE_MYSQL;
			}
		}
		else
		{
			String format_cmd_insert_data_head = String.format(FORMAT_CMD_INSERT_TABLE_HEAD_FORMAT, table_name);
// Create the SQL command list and then execute
			for (String data : csv_reader)
			{
				String[] element_list = field_string_to_array(data);
				String cmd_data = "?";
// Transform the date field into SQL Date format
				java.sql.Date sql_date = null;
				try
				{
//					SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); // your template here
//					java.util.Date dateStr = formatter.parse(element_list[0]);
					java.util.Date dateStr = FinanceRecorderCmnDef.get_date(element_list[0]);
					sql_date = new java.sql.Date(dateStr.getTime());
				}
				catch (ParseException e)
				{
					FinanceRecorderCmnDef.format_debug("Fail to transform the MySQL time format, due to: %s", e.toString());
					return FinanceRecorderCmnDef.RET_FAILURE_MYSQL;
				}
// Assemble all the element in the data
				int element_list_len = element_list.length;
				for (int index = 1 ; index < element_list_len ; index++)
				{
					cmd_data += String.format(",%s", element_list[index]);
				}
// Generate the SQL command
				String cmd_insert_data = format_cmd_insert_data_head + cmd_data + FORMAT_CMD_INSERT_TABLE_TAIL;
				PreparedStatement pstmt = null;
				try
				{
					pstmt = connection.prepareStatement(cmd_insert_data);
					pstmt.setDate(1, sql_date);
				}
				catch (SQLException e)
				{
					FinanceRecorderCmnDef.format_debug("Fail to prepare MySQL command, due to: %s", e.toString());
					return FinanceRecorderCmnDef.RET_FAILURE_MYSQL;
				}
// Write the message into the MySQL
				try
				{
//					FinanceRecorderCmnDef.format_debug("Insert into data by command: %s", pstmt);
					pstmt.executeUpdate();
				}
				catch(SQLException ex) //有可能會產生sql exception
				{
					FinanceRecorderCmnDef.format_error("Fail to insert into data by command[%s], due to: %s", pstmt, ex.getMessage());
					return  FinanceRecorderCmnDef.RET_FAILURE_MYSQL_EXECUTE_COMMAND;
				}
			}
		}
		return  FinanceRecorderCmnDef.RET_SUCCESS;
	}

	protected short select_data(String table_name, int source_type_index, LinkedList<Integer> field_index_list, FinanceRecorderCmnClass.FinanceTimeRange finance_time_range, FinanceRecorderCmnClass.ResultSet result_set)
	{
// Check if the connection is established
		if (connection == null)
		{
			FinanceRecorderCmnDef.error("The connection is NOT established");
			return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
		}
// Check if the SQL field should NOT be empty
		if (field_index_list.isEmpty())
		{
			FinanceRecorderCmnDef.format_error("No fields are selected in %s", table_name);
			return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
		}
		int field_index_list_len = field_index_list.size();

		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
// Generate the SQL command for querying
		StringBuilder field_cmd_builder = new StringBuilder();
		ret = FinanceRecorderSQLClient.get_sql_field_command(source_type_index, field_index_list, field_cmd_builder);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;
		String field_cmd = field_cmd_builder.toString();
// Generate the prepared statement
		PreparedStatement pstmt = null;
		try
		{
			pstmt = FinanceRecorderSQLClient.prepare_query_sql_statement(connection, table_name, field_cmd, finance_time_range);
		}
		catch (IllegalArgumentException e)
		{
			return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
		}
		catch (SQLException e)
		{
			return FinanceRecorderCmnDef.RET_FAILURE_MYSQL;
		}
		catch (ParseException e)
		{
			return FinanceRecorderCmnDef.RET_FAILURE_MYSQL;
		}

		String[] finance_data_sql_field_definition = FinanceRecorderCmnDef.FINANCE_DATA_SQL_FIELD_DEFINITION_LIST[source_type_index];
		ResultSet rs = null;
		try
		{
			FinanceRecorderCmnDef.format_debug("Select from data by command: %s", pstmt);
			rs = pstmt.executeQuery();
			OUT:
			while(rs.next())
			{
// Read the result from MySQL
				for(int i = 0 ; i < field_index_list_len ; i++)
				{
					Integer field_index = field_index_list.get(i);
					String field_type = finance_data_sql_field_definition[field_index];
					if (field_type.equals("INT"))
						result_set.set_data(source_type_index, field_index, rs.getInt(finance_data_sql_field_definition[field_index]));
					else if (field_type.equals("BIGINT"))
						result_set.set_data(source_type_index, field_index, rs.getLong(finance_data_sql_field_definition[field_index]));
					else if (field_type.equals("FLOAT"))
						result_set.set_data(source_type_index, field_index, rs.getFloat(finance_data_sql_field_definition[field_index]));
					else if (field_type.contains("DATE"))
					{
						String field_date_type = "DATE"; //finance_data_sql_field_definition[field_index_list.get(i)];
						String date_field_data = rs.getString(field_date_type);
						result_set.set_date(date_field_data);
					}
					else
					{
						FinanceRecorderCmnDef.format_debug("Unknown SQL field type: %s", field_type);
						ret = FinanceRecorderCmnDef.RET_FAILURE_MYSQL;
						break OUT;
					}
				}

//				String result_str = "";
//				for(int i = 0 ; i < field_index_list_len ; i++)
//				{
//					String field_type = finance_data_sql_field_type_definition[field_index_list.get(i)];
//					if (field_type.equals("INT"))
//						result_str += String.format("%d,", rs.getInt(finance_data_sql_field_definition[field_index_list.get(i)]));
//					else if (field_type.equals("BIGINT"))
//						result_str += String.format("%d,", rs.getLong(finance_data_sql_field_definition[field_index_list.get(i)]));
//					else if (field_type.equals("FLOAT"))
//						result_str += String.format("%f,", rs.getFloat(finance_data_sql_field_definition[field_index_list.get(i)]));
//					else if (field_type.contains("DATE"))
//						result_str += String.format("%s,", rs.getString(finance_data_sql_field_definition[field_index_list.get(i)]));
//					else
//					{
//						FinanceRecorderCmnDef.format_debug("Unknown SQL field type: %s", field_type);
//						return FinanceRecorderCmnDef.RET_FAILURE_MYSQL;
//					}
//				}
////				FinanceRecorderCmnDef.format_debug("Query Data: %s", result_str);
//				data_list.add(result_str.substring(0, result_str.length() - 1));
			}
		}
		catch(SQLException ex) //有可能會產生sql exception
		{
			FinanceRecorderCmnDef.format_error("Fail to select from data by command[%s], due to: %s", pstmt, ex.getMessage());
			ret = FinanceRecorderCmnDef.RET_FAILURE_MYSQL_EXECUTE_COMMAND;
		}
		catch(Exception ex)
		{
			FinanceRecorderCmnDef.format_error("Error occur due to SQL command[%s], due to: %s", pstmt, ex.getMessage());
			ret = FinanceRecorderCmnDef.RET_FAILURE_UNKNOWN;
		}
		finally
		{
			if (rs != null)
			{
				try {rs.close();}
				catch(SQLException ex){}
				rs = null;
			}
		}
		return ret;
	}
	protected short select_data(String table_name, int source_type_index, String field_cmd, FinanceRecorderCmnClass.FinanceTimeRange finance_time_range, FinanceRecorderCmnClass.ResultSet result_set)
	{
// Transform the command string to related field
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
		LinkedList<Integer> field_index_list = new LinkedList<Integer>();
		ret = FinanceRecorderSQLClient.get_sql_field_index_list(source_type_index, field_cmd, field_index_list);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;
		return select_data(table_name, source_type_index, field_index_list, finance_time_range, result_set);
	}
	protected short select_data(String table_name, int source_type_index, FinanceRecorderCmnClass.FinanceTimeRange finance_time_range, FinanceRecorderCmnClass.ResultSet result_set){return select_data(table_name, source_type_index, "*", finance_time_range, result_set);}
	protected short select_data(String table_name, int source_type_index, String cmd_table_field, FinanceRecorderCmnClass.ResultSet result_set){return select_data(table_name, source_type_index, cmd_table_field, null, result_set);}
	protected short select_data(String table_name, int source_type_index, FinanceRecorderCmnClass.ResultSet result_set){return select_data(table_name, source_type_index, "*", null, result_set);}

	void enable_batch_operation(boolean enable)
	{
		if (enable)
			batch_operation = FinanceRecorderCmnDef.DatabaseEnableBatchType.DatabaseEnableBatch_Yes;
		else
			batch_operation = FinanceRecorderCmnDef.DatabaseEnableBatchType.DatabaseEnableBatch_No;
	}
	boolean is_batch_operation(){return (batch_operation == FinanceRecorderCmnDef.DatabaseEnableBatchType.DatabaseEnableBatch_Yes ? true : false);}
}
