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
	private String server = null;
	private String username = null;
	private String password = null;
	private String database_name = null;
	private String table_name = null;
	private FinanceRecorderCmnDef.FinanceObserverInf finance_observer = null;

// Create Database command format
	private static final String format_cmd_create_database = "CREATE DATABASE %s";
// Create Table command format
	protected static final String format_cmd_create_table_head_format = "CREATE TABLE IF NOT EXISTS %s (";
	protected static final String format_cmd_create_table_tail = ")";
//		private static final String format_cmd_insert_into_table = "INSERT INTO sql%s VALUES(\"%s\", \"%s\", %d, \"%s\")";
// Insert Data command format
	protected static final String format_cmd_insert_data_head_format = "INSERT INTO %s VALUES(";
	protected static final String format_cmd_insert_data_tail = ")";
// Delete Database command format
	private static final String format_cmd_delete_database = "DROP DATABASE IF EXISTS %s";


	FinanceRecorderSQLClient(FinanceRecorderCmnDef.FinanceObserverInf observer)
	{
		server = DEF_SERVER;
		username = DEF_USERNAME;
		password = DEF_PASSWORD;
		finance_observer = observer;
	}

	private static java.sql.Date transform_java_sql_date_format(String date_str) throws ParseException
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); // your template here
		java.util.Date dateStr = formatter.parse(date_str);
		return  new java.sql.Date(dateStr.getTime());
	}

	short try_connect_mysql(
			String database, 
			FinanceRecorderCmnDef.DatabaseNotExistIngoreType database_not_exist_ignore_type, 
			FinanceRecorderCmnDef.DatabaseCreateThreadType database_create_thread_type
		)
	{
		database_name = database;
		FinanceRecorderCmnDef.format_debug("Try to connect to the MySQL database server[%s]...... Successfully", database_name);

		short ret = FinanceRecorderCmnDef.RET_SUCCESS;
// Create the connection to the MySQL server and database
		String cmd_create_database = String.format(format_cmd_create_database, database_name);
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
			System.out.println("DriverClassNotFound:" + ex.toString());
			return FinanceRecorderCmnDef.RET_FAILURE_MYSQL;
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
					System.out.println("Exception:" + ex1.toString());
					return FinanceRecorderCmnDef.RET_FAILURE_MYSQL;
				}
			}
			else
			{
				System.out.println("Exception:" + ex.toString());
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
				// TODO Auto-generated catch block
				FinanceRecorderCmnDef.format_error("Fail to close the connection to MySQL, due to %s", e.toString());
				return FinanceRecorderCmnDef.RET_FAILURE_MYSQL; 
			}
			connection = null;
		}
		database_name = null;

		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	short open_table(String table, String cmd_table_field)
	{
// Check if the connection is established
		if (connection == null)
		{
			FinanceRecorderCmnDef.error("The connection is NOT established");
			return  FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
		}

		table_name = table;

		FinanceRecorderCmnDef.format_debug("Try to open the MySQL table[%s]...... ", table_name);
		String format_cmd_create_table_head = String.format(format_cmd_create_table_head_format, table_name);
// Create table
		String cmd_create_table = format_cmd_create_table_head + cmd_table_field + format_cmd_create_table_tail;
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
				return FinanceRecorderCmnDef.RET_FAILURE_MYSQL;
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
		String cmd_delete_database = String.format(format_cmd_delete_database, database_name);
// Create the SQL command list and then execute
		try
		{
			Statement s = connection.createStatement();
			FinanceRecorderCmnDef.format_debug("Delete database[%s] by command: %s", database_name, cmd_delete_database);
			s.executeUpdate(cmd_delete_database);
		}
		catch(SQLException ex) //有可能會產生sql exception
		{
			FinanceRecorderCmnDef.format_error("Fails to delete database[%s], due to: %d, %s", database_name, ex.getMessage());
			return FinanceRecorderCmnDef.RET_FAILURE_MYSQL;
		}
// Close the connection to MySQL
		disconnect_mysql();

		FinanceRecorderCmnDef.format_debug("Delete database[%s]...... Successfully", database_name);
		return FinanceRecorderCmnDef.RET_SUCCESS;
	}

	short insert_data(List<String> data_list)
	{
// Check if the connection is established
		if (connection == null)
		{
			FinanceRecorderCmnDef.error("The connection is NOT established");
			return  FinanceRecorderCmnDef.RET_FAILURE_INCORRECT_OPERATION;
		}

		String format_cmd_insert_data_head = String.format(format_cmd_insert_data_head_format, table_name);

// Create the SQL command list and then execute
		for (String data : data_list)
		{
			String[] element_list = FinanceRecorderCmnDef.field_string_to_array(data);
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
				FinanceRecorderCmnDef.format_debug("Insert into data by command: %s", pstmt);
				pstmt.executeUpdate();
			}
			catch(SQLException ex) //有可能會產生sql exception
			{
				FinanceRecorderCmnDef.format_error("Fail to insert into data by command[%s], due to: %s", pstmt, ex.getMessage());
				return  FinanceRecorderCmnDef.RET_FAILURE_MYSQL;
			}
		}

		return  FinanceRecorderCmnDef.RET_SUCCESS;
	}
}
