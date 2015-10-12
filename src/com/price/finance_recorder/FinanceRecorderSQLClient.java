package com.price.finance_recorder;

import java.sql.*;
import java.text.*;
import java.util.*;


public class FinanceRecorderSQLClient extends FinanceRecorderCmnBase
{
	private static final String DEF_SERVER = "localhost";
	private static final String DEF_USERNAME = "root";
	private static final String DEF_PASSWORD = "lab4man1";

	private Connection connection = null; //Database objects, 連接object
	private boolean table_created = false;
	private String server = null;
	private String username = null;
	private String password = null;
	private String database_name = null;
	private String table_name = null;
	private FinanceRecorderCmnDef.FinanceObserverInf finance_observer = null;

// Create Database command format
	private static final String format_cmd_create_database = "CREATE DATABASE %s CHARACTER SET utf8 COLLATE utf8_bin";
// Create Table command format
	protected static final String format_cmd_create_table_head_format = "CREATE TABLE IF NOT EXISTS %s (";
	protected static final String format_cmd_create_table_tail = ")";
//		private static final String format_cmd_insert_into_table = "INSERT INTO sql%s VALUES(\"%s\", \"%s\", %d, \"%s\")";
// Insert Data command format
	protected static final String format_cmd_insert_data_head_format = "INSERT INTO %s VALUES(";
	protected static final String format_cmd_insert_data_tail = ")";

	protected String format_cmd_create_table_head = null;
	protected String format_cmd_insert_data_head = null;

	protected List<String> table_field_list = new LinkedList<String>();
	protected List<PreparedStatement> sql_cmd_data_list = new LinkedList<PreparedStatement>();

	FinanceRecorderCmnDef.FinanceObserverInf parent_observer = null;

	public FinanceRecorderSQLClient(FinanceRecorderCmnDef.FinanceObserverInf observer)
	{
		server = DEF_SERVER;
		username = DEF_USERNAME;
		password = DEF_PASSWORD;
		finance_observer = observer;
	}

	private short try_connect_mysql(String cmd_create_database)
	{
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
			System.out.println("DriverClassNotFound:" + ex.toString());
			return FinanceRecorderCmnDef.RET_FAILURE_MYSQL;
		}
		catch(SQLException ex) //有可能會產生sql exception
		{
			FinanceRecorderCmnDef.format_debug("The %s database does NOT exist, create a NEW one", database_name);
			try
			{
				connection = DriverManager.getConnection(String.format("jdbc:mysql://localhost/?user=%s&password=%s", username, password)); 
				Statement s = connection.createStatement();
				FinanceRecorderCmnDef.format_debug("Try to create database by command: %s", cmd_create_database);
				s.executeUpdate(cmd_create_database);
			}
			catch(SQLException ex1) //有可能會產生sql exception
			{
				System.out.println("Exception:" + ex1.toString());
				return FinanceRecorderCmnDef.RET_FAILURE_MYSQL;
			}
			FinanceRecorderCmnDef.format_debug("Try to connect to the MySQL database server...... Successfully");
		}

		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	private short open_table(String cmd_create_table)
	{
// Check if the connection is established
		if (connection == null)
		{
			FinanceRecorderCmnDef.error("The connection is NOT established");
			return  FinanceRecorderCmnDef.RET_FAILURE_MYSQL;
		}

		if (!table_created)
		{
// Create the table in the database...
			try
			{
				Statement s = connection.createStatement();
//				cmd_buf = String.format(cmd_create_table, current_time_string);
				FinanceRecorderCmnDef.format_debug("Try to create table[%s] by command: %s", table_name, cmd_create_table);

				s.executeUpdate(cmd_create_table);
			}
			catch(SQLException ex) //有可能會產生sql exception
			{
				if (ex.getErrorCode() == 1050)
					FinanceRecorderCmnDef.format_debug("The table has already existed");
				else
				{
					FinanceRecorderCmnDef.format_error("Fails to create table[%s], due to: %d, %s", table_name, ex.getMessage());
					return FinanceRecorderCmnDef.RET_FAILURE_MYSQL;
				}
			}			
			table_created = true;
		}

		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	public short initialize(String database, String table, String cmd_table_field)
	{
		database_name = database;
		table_name = table;

		format_cmd_create_table_head = String.format(format_cmd_create_table_head_format, table_name);
		format_cmd_insert_data_head = String.format(format_cmd_insert_data_head_format, table_name);

//		FinanceRecorderCmnDef.debug("Initialize the MsgDumperSql object......");
		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
// Create the connection to the MySQL server and database
		String cmd_create_database = String.format(format_cmd_create_database, database_name);
		FinanceRecorderCmnDef.format_debug("Create database by command: %s", cmd_create_database);
		ret = try_connect_mysql(cmd_create_database);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;
//		if (table_field_list.isEmpty())
//		{
//			FinanceRecorderCmnDef.error("table_field_list should NOT be empty");
//			return FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
//		}
//		int table_field_list_len = table_field_list.size();
//		String cmd_create_table_field = table_field_list.get(0);
//		for (int i = 1 ; i < table_field_list_len ; i ++)
//			cmd_create_table_field += String.format(",%s", table_field_list.get(i));
// Create table
		String cmd_create_table = format_cmd_create_table_head + cmd_table_field + format_cmd_create_table_tail;
		FinanceRecorderCmnDef.format_debug("Create table by command: %s", cmd_create_table);
		ret = open_table(cmd_create_table);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;

		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	public short deinitialize()
	{
		FinanceRecorderCmnDef.format_debug("Release the parameters connected to the MySQL database server");
		if (connection != null)
		{
			try 
			{
				connection.close();
			} 
			catch (SQLException e) 
			{
				// TODO Auto-generated catch block
				FinanceRecorderCmnDef.format_error("Fail to close the connection to MySQL, due to %s", e.toString());
				return FinanceRecorderCmnDef.RET_FAILURE_MYSQL; 
			}
			connection = null;
		}

		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	public static java.sql.Date transform_java_sql_date_format(String date_str) throws ParseException
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); // your template here
		java.util.Date dateStr = formatter.parse(date_str);
		return  new java.sql.Date(dateStr.getTime());
	}

	private short transform_sql_command(List<String> data_list)
	{
		for (String data : data_list)
		{
			String[] element_list = FinanceRecorderCmnDef.field_string_to_array(data);
//			String cmd_field = field_list[0];
			String cmd_data = "?";
// Transform the date field into SQL Date format
			java.sql.Date sql_date = null;
			try
			{
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); // your template here
				java.util.Date dateStr = formatter.parse(element_list[0]);
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
			String cmd_insert_data = format_cmd_insert_data_head + cmd_data + format_cmd_insert_data_tail;
			try
			{
				PreparedStatement pstmt = connection.prepareStatement(cmd_insert_data);
				pstmt.setDate(1, sql_date);
//				StockRecorderCmnDef.format_debug("Create the command of inserting data: %s", pstmt);
				sql_cmd_data_list.add(pstmt);
			}
			catch (SQLException e)
			{
				FinanceRecorderCmnDef.format_debug("Fail to prepare MySQL command, due to: %s", e.toString());
				return FinanceRecorderCmnDef.RET_FAILURE_MYSQL;
			}
		}
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	private short execute_sql_command(List<PreparedStatement> sql_cmd_list)
	{
// Write the message into the MySQL
		try
		{
			for (PreparedStatement sql_cmd : sql_cmd_list)
			{
				FinanceRecorderCmnDef.format_debug("Insert data by command: %s", sql_cmd);
				sql_cmd.executeUpdate();
//				Statement s = connection.createStatement();
//				s.executeUpdate(cmd_insert_data);
			}
		}
		catch(SQLException ex) //有可能會產生sql exception
		{
			FinanceRecorderCmnDef.format_error("Fails to insert data into table[%s], due to: %s", table_name, ex.getMessage());
			return  FinanceRecorderCmnDef.RET_FAILURE_MYSQL;
		}
		return  FinanceRecorderCmnDef.RET_SUCCESS;
	}

//	public short write(List<PreparedStatement> cmd_insert_data_list)
	public short write(List<String> data_list)
	{
// Create the SQL command list
		short ret = transform_sql_command(data_list);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;
// Execute the SQL command list
		ret = execute_sql_command(sql_cmd_data_list);
		if (FinanceRecorderCmnDef.CheckFailure(ret))
			return ret;
// Clear the SQL command list
		sql_cmd_data_list.clear();
		return  FinanceRecorderCmnDef.RET_SUCCESS;
	}
}
