package com.price.finance_recorder;

import java.sql.*;
import java.text.*;
import java.util.*;

import com.price.finance_recorder.FinanceRecorderCmnDef.FinanceFieldType;


public class FinanceRecorderSQLClient extends FinanceRecorderCmnBase
{
	private static final String DEF_SERVER = "localhost";
	private static final String DEF_USERNAME = "root";
	private static final String DEF_PASSWORD = "lab4man1";
// Create Database command format
	private static final String FORMAT_CMD_CREATE_DATABASE = "CREATE DATABASE %s";
// Create Table command format
	private static final String FORMAT_CMD_CREATE_TABLE_HEAD_FORMAT = "CREATE TABLE IF NOT EXISTS %s (";
	private static final String FORMAT_CMD_CREATE_TABLE_TAIL = ")";
//	private static final String format_cmd_insert_into_table = "INSERT INTO sql%s VALUES(\"%s\", \"%s\", %d, \"%s\")";
// Insert Data command format
	private static final String FORMAT_CMD_INSERT_DATA_HEAD_FORMAT = "INSERT INTO %s VALUES(";
	private static final String FORMAT_CMD_INSERT_DATA_TAIL = ")";
// Select Data command format
	private static final String FORMAT_CMD_SELECT_DATA_HEAD = "SELECT ";
	private static final String FORMAT_CMD_SELECT_DATA_TAIL_FORMAT = " FROM %s";
	private static final String FORMAT_CMD_SELECT_DATE_RULE_BETWEEN = " WHERE date BETWEEN ? AND ?";
	private static final String FORMAT_CMD_SELECT_DATE_RULE_GREATER_THAN = " WHERE date > ?";
	private static final String FORMAT_CMD_SELECT_DATE_RULE_LESS_THAN = " WHERE date < ?";
	private static final String FORMAT_CMD_SELECT_DATE_RULE_GREATER_EQUAL_THAN = " WHERE date >= ?";
	private static final String FORMAT_CMD_SELECT_DATE_RULE_LESS_EQUAL_THAN = " WHERE date <= ?";
	private static final String FORMAT_CMD_SELECT_MONTH_RULE_BETWEEN_FORMAT = " WHERE month(date) BETWEEN '%d' AND '%d'";
	private static final String FORMAT_CMD_SELECT_MONTH_RULE_GREATER_THAN_FORMAT = " WHERE month(date) > '%d'";
	private static final String FORMAT_CMD_SELECT_MONTH_RULE_LESS_THAN_FORMAT = " WHERE month(date) < '%d'";
	private static final String FORMAT_CMD_SELECT_MONTH_RULE_GREATER_EQUAL_THAN_FORMAT = " WHERE month(date) >= '%d'";
	private static final String FORMAT_CMD_SELECT_MONTH_RULE_LESS_EQUAL_THAN_FORMAT = " WHERE month(date) <= '%d'";

// Delete Database command format
	private static final String FORMAT_CMD_DELETE_DATABASE = "DROP DATABASE IF EXISTS %s";

	private Connection connection = null; //Database objects, 連接object
	private String server = null;
	private String username = null;
	private String password = null;
	private String database_name = null;
//	private String table_name = null;
	private FinanceRecorderCmnDef.FinanceObserverInf finance_observer = null;
	private int finace_data_type_index;

	FinanceRecorderSQLClient(FinanceRecorderCmnDef.FinanceSourceType finance_data_type, FinanceRecorderCmnDef.FinanceObserverInf observer)
	{
		finace_data_type_index = finance_data_type.ordinal();
		server = DEF_SERVER;
		username = DEF_USERNAME;
		password = DEF_PASSWORD;
		finance_observer = observer;
	}

	private static java.sql.Date transform_java_sql_date_format(String date_str) throws ParseException
	{
//		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); // your template here
//		java.util.Date dateStr = formatter.parse(date_str);
		java.util.Date dateStr = FinanceRecorderCmnDef.get_date(date_str);
		return new java.sql.Date(dateStr.getTime());
	}

	public static short get_sql_field_command(int source_index, LinkedList<Integer> query_field, StringBuilder field_cmd_builder)
	{
		if (query_field.isEmpty())
			throw new IllegalArgumentException("The query should NOT be empty");
//		string field_cmd;
// Select all the fields in the table
		if ((int)query_field.size() == FinanceRecorderCmnDef.FINANCE_DATABASE_FIELD_AMOUNT_LIST[source_index] - 1) // Caution: Don't include the "date" field
			field_cmd_builder.append("*");
		else
		{
// Assemble the MySQL command of the designated field
// The "date" field is a must
			String field_cmd = String.format("%s", FinanceRecorderCmnDef.MYSQL_DATE_FILED_NAME);
//			int query_field_size = query_field.size();
//			for(int field_index = 0 ; field_index < query_field_size ; field_index++)
			ListIterator<Integer> iter = query_field.listIterator();
			while(iter.hasNext())
			{
//				snprintf(field_buf, 16, ",%s%d", MYSQL_FILED_NAME_BASE, query_field[field_index]);
//				field_cmd += string(field_buf);
				Integer index = iter.next();
				field_cmd += String.format(",%s%d", FinanceRecorderCmnDef.MYSQL_FILED_NAME_BASE, index);
			}
			field_cmd_builder.append(field_cmd);
		}

		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	short try_connect_mysql(
			String database, 
			FinanceRecorderCmnDef.DatabaseNotExistIngoreType database_not_exist_ignore_type, 
			FinanceRecorderCmnDef.DatabaseCreateThreadType database_create_thread_type
		)
	{
		database_name = database;
		FinanceRecorderCmnDef.format_debug("Try to connect to the MySQL database server[%s]......", database_name);

		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
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
					database_name = database;
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

	short disconnect_mysql()
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
		database_name = null;

		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	short create_table(String table_name, String cmd_table_field)
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
				FinanceRecorderCmnDef.format_debug("The table[%s] has already existed", table_name);
			else
			{
				FinanceRecorderCmnDef.format_error("Fails to create table[%s], due to: %s", table_name, ex.getMessage());
				return FinanceRecorderCmnDef.RET_FAILURE_MYSQL_EXECUTE_COMMAND;
			}
		}

		FinanceRecorderCmnDef.format_debug("Try to open the MySQL table[%s]...... Successfully", table_name);
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	short delete_database()
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

	short get_table_name_list(List<String> table_name_list)
	{
// Check if the connection is established
		if (connection == null)
		{
			FinanceRecorderCmnDef.error("The connection is NOT established");
			return  FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
		}

// Create the SQL command list and then execute
		try
		{
			DatabaseMetaData md = connection.getMetaData();
			ResultSet rs = md.getTables(null, null, "%", null);
			while (rs.next()) 
			{
//				System.out.println(rs.getString(3));
				table_name_list.add(rs.getString(3));
			}
		}
		catch(SQLException ex) //有可能會產生sql exception
		{
			FinanceRecorderCmnDef.format_error("Fails to show tables[%s], due to: %s", ex.getMessage());
			return FinanceRecorderCmnDef.RET_FAILURE_MYSQL_EXECUTE_COMMAND;
		}

		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	short insert_data(String table_name, List<String> data_list)
	{
// Check if the connection is established
		if (connection == null)
		{
			FinanceRecorderCmnDef.error("The connection is NOT established");
			return  FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
		}

		String format_cmd_insert_data_head = String.format(FORMAT_CMD_INSERT_DATA_HEAD_FORMAT, table_name);

// Create the SQL command list and then execute
		for (String data : data_list)
		{
			String[] element_list = FinanceRecorderCmnDef.field_string_to_array(data);
			String cmd_data = "?";
// Transform the date field into SQL Date format
			java.sql.Date sql_date = null;
			try
			{
//				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); // your template here
//				java.util.Date dateStr = formatter.parse(element_list[0]);
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
			String cmd_insert_data = format_cmd_insert_data_head + cmd_data + FORMAT_CMD_INSERT_DATA_TAIL;
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
//				FinanceRecorderCmnDef.format_debug("Insert into data by command: %s", pstmt);
				pstmt.executeUpdate();
			}
			catch(SQLException ex) //有可能會產生sql exception
			{
				FinanceRecorderCmnDef.format_error("Fail to insert into data by command[%s], due to: %s", pstmt, ex.getMessage());
				return  FinanceRecorderCmnDef.RET_FAILURE_MYSQL_EXECUTE_COMMAND;
			}
		}

		return  FinanceRecorderCmnDef.RET_SUCCESS;
	}

	short insert_data_batch(String table_name, List<String> data_list)
	{
// Check if the connection is established
		if (connection == null)
		{
			FinanceRecorderCmnDef.error("The connection is NOT established");
			return  FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
		}
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
		for (String data : data_list)
		{
			String[] element_list = FinanceRecorderCmnDef.field_string_to_array(data);
			if (pstmt == null)
			{
				// Assemble all the element in the data
				element_list_len = element_list.length;
				String cmd_data = "?";
				for (int index = 1 ; index < element_list_len ; index++)
					cmd_data += ",?";
// Generate the SQL prepared batch command
				String format_cmd_insert_data_head = String.format(FORMAT_CMD_INSERT_DATA_HEAD_FORMAT, table_name);
				String cmd_insert_data = format_cmd_insert_data_head + cmd_data + FORMAT_CMD_INSERT_DATA_TAIL;
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
//				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); // your template here
//				java.util.Date dateStr = formatter.parse(element_list[0]);
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
//				FinanceRecorderCmnDef.format_debug("Insert into data by command: %s", pstmt);
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
		return  FinanceRecorderCmnDef.RET_SUCCESS;
	}

//	short select_data(String table_name, String cmd_table_field, FinanceRecorderCmnClass.TimeRangeCfg time_range_cfg, List<String> data_list)
	short select_data(String table_name, String cmd_table_field, FinanceRecorderCmnClass.TimeRangeCfg time_range_cfg, FinanceRecorderCmnClass.ResultSet result_set)
	{
// Check if the connection is established
		if (connection == null)
		{
			FinanceRecorderCmnDef.error("The connection is NOT established");
			return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
		}

// Generate the SQL command for querying
		String format_cmd_select_data_tail = String.format(FORMAT_CMD_SELECT_DATA_TAIL_FORMAT, table_name);
		String cmd_select_data = FORMAT_CMD_SELECT_DATA_HEAD + cmd_table_field + format_cmd_select_data_tail;
		PreparedStatement pstmt = null;
// Create the SQL command list and then execute
		if (time_range_cfg == null)
		{
			try
			{
				pstmt = connection.prepareStatement(cmd_select_data);
			}
			catch (SQLException e)
			{
				FinanceRecorderCmnDef.format_error("Fail to prepare MySQL command for querying data, due to: %s", e.toString());
				return FinanceRecorderCmnDef.RET_FAILURE_MYSQL;
			}
		}
		else if (!time_range_cfg.is_month_type())
		{
// Transform the date field into SQL Date format
			java.sql.Date sql_date_start = null;
			java.sql.Date sql_date_end = null;
			try
			{
				if (time_range_cfg.get_start_time_str() != null)
					sql_date_start = transform_java_sql_date_format(time_range_cfg.get_start_time_str());
				if (time_range_cfg.get_end_time_str() != null)
					sql_date_end = transform_java_sql_date_format(time_range_cfg.get_end_time_str());
			}
			catch (ParseException e)
			{
				FinanceRecorderCmnDef.format_error("Fail to transform the MySQL time format, due to: %s", e.toString());
				return FinanceRecorderCmnDef.RET_FAILURE_MYSQL;
			}
			try
			{
				if (time_range_cfg.get_start_time_str() != null && time_range_cfg.get_end_time_str() != null)
				{
					cmd_select_data += FORMAT_CMD_SELECT_DATE_RULE_BETWEEN;
					pstmt = connection.prepareStatement(cmd_select_data);
					pstmt.setDate(1, sql_date_start);
					pstmt.setDate(2, sql_date_end);
				}
				else if (time_range_cfg.get_start_time_str() != null)
				{
					cmd_select_data += FORMAT_CMD_SELECT_DATE_RULE_GREATER_EQUAL_THAN;
					pstmt = connection.prepareStatement(cmd_select_data);
					pstmt.setDate(1, sql_date_start);
				}
				else if (time_range_cfg.get_start_time_str() != null)
				{
					cmd_select_data += FORMAT_CMD_SELECT_DATE_RULE_LESS_EQUAL_THAN;
					pstmt = connection.prepareStatement(cmd_select_data);
					pstmt.setDate(1, sql_date_end);
				}
				else
				{
					pstmt = connection.prepareStatement(cmd_select_data);
				}
			}
			catch (SQLException e)
			{
				FinanceRecorderCmnDef.format_error("Fail to prepare MySQL command for querying data by date, due to: %s", e.toString());
				return FinanceRecorderCmnDef.RET_FAILURE_MYSQL;
			}
		}
		else
		{
			if (time_range_cfg.get_start_time_str() != null && time_range_cfg.get_end_time_str() != null)
			{
				int[] time_start_list = FinanceRecorderCmnClass.TimeCfg.get_month_value(time_range_cfg.get_start_time_str());
				int[] time_end_list = FinanceRecorderCmnClass.TimeCfg.get_month_value(time_range_cfg.get_end_time_str());
				if (time_start_list == null || time_end_list == null)
					return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
				int month_start = time_start_list[1];
				int month_end = time_end_list[1];
				cmd_select_data += String.format(FORMAT_CMD_SELECT_MONTH_RULE_BETWEEN_FORMAT, month_start, month_end);
			}
			else if (time_range_cfg.get_start_time_str() != null)
			{
				int[] time_start_list = FinanceRecorderCmnClass.TimeCfg.get_month_value(time_range_cfg.get_start_time_str());
				if (time_start_list == null)
					return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
				int month_start = time_start_list[1];
				cmd_select_data += String.format(FORMAT_CMD_SELECT_MONTH_RULE_GREATER_EQUAL_THAN_FORMAT, month_start);
			}
			else if (time_range_cfg.get_end_time_str() != null)
			{
				int[] time_end_list = FinanceRecorderCmnClass.TimeCfg.get_month_value(time_range_cfg.get_end_time_str());
				if (time_end_list == null)
					return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
				int month_end = time_end_list[1];
				cmd_select_data += String.format(FORMAT_CMD_SELECT_MONTH_RULE_LESS_EQUAL_THAN_FORMAT, month_end);
			}
			try
			{
				pstmt = connection.prepareStatement(cmd_select_data);
			}
			catch (SQLException e)
			{
				FinanceRecorderCmnDef.format_error("Fail to prepare MySQL command for querying data by month, due to: %s", e.toString());
				return FinanceRecorderCmnDef.RET_FAILURE_MYSQL;
			}
		}
// Get the MySQL database field and its type
		String[] finance_data_sql_field_definition = FinanceRecorderCmnDef.FINANCE_DATA_SQL_FIELD_DEFINITION_LIST[finace_data_type_index];
		String[] finance_data_sql_field_type_definition = FinanceRecorderCmnDef.FINANCE_DATA_SQL_FIELD_TYPE_DEFINITION_LIST[finace_data_type_index];

		ArrayList<Integer> field_index_list = new ArrayList<Integer>();
		if (cmd_table_field.equals("*"))
		{
			int finance_data_sql_field_definition_len = finance_data_sql_field_definition.length;
			for(int i = 0 ; i < finance_data_sql_field_definition_len ; i++)
				field_index_list.add(i);
		}
		else
		{
			String[] table_field_list = cmd_table_field.split(",");
			int table_field_list_len = table_field_list.length;
			int index;
			for(int i = 0 ; i < table_field_list_len ; i++)
			{
				index = Arrays.asList(finance_data_sql_field_definition).indexOf(table_field_list[i]);
				if (index == -1)
				{
					FinanceRecorderCmnDef.format_error("Unknown field: %s in %s", table_field_list[i], FinanceRecorderCmnDef.FINANCE_DATABASE_DESCRIPTION_LIST[finace_data_type_index]);
					return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
				}
				field_index_list.add(index);
			}
		}
		if (field_index_list.isEmpty())
		{
			FinanceRecorderCmnDef.format_error("No fields are selected in %s", FinanceRecorderCmnDef.FINANCE_DATABASE_DESCRIPTION_LIST[finace_data_type_index]);
			return FinanceRecorderCmnDef.RET_FAILURE_INVALID_ARGUMENT;
		}
		int field_index_list_len = field_index_list.size();

		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
// Read the result from MySQL
		ResultSet rs = null;
		try
		{
			FinanceRecorderCmnDef.format_debug("Select from data by command: %s", pstmt);
			rs = pstmt.executeQuery();
			OUT:
			while(rs.next())
			{
				for(int i = 0 ; i < field_index_list_len ; i++)
				{
					Integer field_index = field_index_list.get(i);
					String field_type = finance_data_sql_field_type_definition[field_index];
					if (field_type.equals("INT"))
						result_set.set_data(finace_data_type_index, field_index, rs.getInt(finance_data_sql_field_definition[field_index]));
					else if (field_type.equals("BIGINT"))
						result_set.set_data(finace_data_type_index, field_index, rs.getLong(finance_data_sql_field_definition[field_index]));
					else if (field_type.equals("FLOAT"))
						result_set.set_data(finace_data_type_index, field_index, rs.getFloat(finance_data_sql_field_definition[field_index]));
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

//	short select_data(String table_name, FinanceRecorderCmnClass.TimeRangeCfg time_range_cfg, List<String> data_list){return select_data(table_name, "*", time_range_cfg, data_list);}
//	short select_data(String table_name, String cmd_table_field, List<String> data_list){return select_data(table_name, cmd_table_field, null, data_list);}
//	short select_data(String table_name, List<String> data_list){return select_data(table_name, "*", null, data_list);}
	short select_data(String table_name, FinanceRecorderCmnClass.TimeRangeCfg time_range_cfg, FinanceRecorderCmnClass.ResultSet result_set){return select_data(table_name, "*", time_range_cfg, result_set);}
	short select_data(String table_name, String cmd_table_field, FinanceRecorderCmnClass.ResultSet result_set){return select_data(table_name, cmd_table_field, null, result_set);}
	short select_data(String table_name, FinanceRecorderCmnClass.ResultSet result_set){return select_data(table_name, "*", null, result_set);}

}
