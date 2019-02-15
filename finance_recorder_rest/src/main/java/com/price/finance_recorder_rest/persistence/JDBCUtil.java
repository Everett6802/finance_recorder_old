package com.price.finance_recorder_rest.persistence;

import java.sql.*;
//import java.text.*;
//import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
//import java.util.regex.Matcher;

import com.mysql.jdbc.MysqlErrorNumbers;
import com.price.finance_recorder_rest.common.CmnDef;
import com.price.finance_recorder_rest.common.CmnLogger;
import com.price.finance_recorder_rest.exceptions.ExecuteSQLCommandException;


public class JDBCUtil 
{
	private static AtomicBoolean need_check_driver_exist = new AtomicBoolean(true);
	private static AtomicBoolean need_check_database_exist = new AtomicBoolean(true);
	private static final String DEF_SERVER = "localhost";
	private static final String DEF_DATABASE_NAME = "finance_dataset";
	private static final String DEF_USERNAME = "root";
	private static final String DEF_PASSWORD = "lab4man1";
// Create Database command format
	private static final String FORMAT_CMD_CREATE_DATABASE = "CREATE DATABASE %s";
// Delete Database command format
	private static final String FORMAT_CMD_DELETE_DATABASE = "DROP DATABASE IF EXISTS %s";

	private static synchronized void check_driver_exist()
	{
		try 
		{
// Java要連接資料庫時，需使用到JDBC-Driver，連接MySQL資料庫使用Connector/j，下載後解開壓縮，mysql-connector-java-5.1.15-bin.jar就是MySQL的JDBC-Driver.
// Go to http://blog.yslifes.com/archives/918 for more detailed info
// JDBC API is a Java API that can access any kind of tabular data, especially data stored in a Relational Database. 
// JDBC works with Java on a variety of platforms, such as Windows, Mac OS, and the various versions of UNIX.
// Go to http://www.tutorialspoint.com/jdbc/index.htm to see the example of MySQL command by JDBC
//註冊driver
			Class.forName("com.mysql.jdbc.Driver");
			need_check_driver_exist.set(false);
		}
		catch(ClassNotFoundException ex)
		{
			String errmsg = "DriverClassNotFound:" + ex.toString();
			CmnLogger.error(errmsg);
//			if(CmnDef.is_show_console())
//				System.err.println(errmsg);
			throw new IllegalStateException(errmsg);
		}
	}

	private static synchronized void check_database_exist()
	{
		if (!need_check_database_exist.get())
			return;
		create_database_not_exist(DEF_DATABASE_NAME, DEF_USERNAME, DEF_PASSWORD);
		need_check_database_exist.set(false);
	}

	private static void create_database_not_exist(String database_name, String username, String password)
	{
		Connection connection = null;
		String cmd_create_database = String.format(FORMAT_CMD_CREATE_DATABASE, database_name);
		CmnLogger.format_debug("Create database by command: %s", cmd_create_database);
		boolean database_already_exist = false;
		try
		{
			connection = DriverManager.getConnection(String.format("jdbc:mysql://localhost/?user=%s&password=%s", username, password)); 
			Statement s = connection.createStatement();
			CmnLogger.format_debug("Try to create database by command: %s", cmd_create_database);
			s.executeUpdate(cmd_create_database);
		}
		catch(SQLException ex) //有可能會產生sql exception
		{
			String errmsg = null;
//			int error_code = ex.getErrorCode();
// the vendor's error code
			if (ex.getErrorCode() == MysqlErrorNumbers.ER_DB_CREATE_EXISTS)
			{
// Handle the situation when the database has already existed
				database_already_exist = true;
				errmsg = String.format("The database[%s] has already existed", database_name);
				CmnLogger.format_info(errmsg);
			}
			else
			{
				errmsg = String.format("Fails to create database[%s], due to: %s", database_name, ex.getMessage());
				CmnLogger.format_error(errmsg);
				throw new ExecuteSQLCommandException(errmsg); 
			}
		}
// Close the connection to MySQL
		try 
		{
			connection.close();
		} 
		catch (SQLException e) 
		{
			CmnLogger.format_error("Fail to close the connection to MySQL, due to %s", e.toString());
		}

		if (!database_already_exist)
			CmnLogger.format_debug("Create database[%s]...... Successfully", database_name);
	}

	private static void delete_database(Connection connection, String database_name)
	{
// Delete database
		String cmd_delete_database = String.format(FORMAT_CMD_DELETE_DATABASE, database_name);
// Create the SQL command list and then execute
		try
		{
			Statement s = connection.createStatement();
			CmnLogger.format_debug("Delete database[%s] by command: %s", database_name, cmd_delete_database);
			s.executeUpdate(cmd_delete_database);
		}
		catch(SQLException ex) //有可能會產生sql exception
		{
			String errmsg = null;
			if (ex.getErrorCode() == MysqlErrorNumbers.ER_DB_DROP_EXISTS)
			{
// Handle the situation when the database does NOT exist
				errmsg = String.format("The database[%s] does NOT exist", database_name);
			}
			else
			{
				errmsg = String.format("Fails to delete database[%s], due to: %s", database_name, ex.getMessage());
			}
			CmnLogger.error(errmsg);
			throw new ExecuteSQLCommandException(errmsg); 
		}
		
// Close the connection to MySQL
		try 
		{
			connection.close();
		} 
		catch (SQLException e) 
		{
			String errmsg = String.format("Fail to close the connection to MySQL, due to %s", e.toString());
			CmnLogger.format_error(errmsg);
			throw new ExecuteSQLCommandException(errmsg); 
		}

		CmnLogger.format_debug("Delete database[%s]...... Successfully", database_name);
	}

	public static Connection open_connection()
	{
// Check the MySQL driver exist
		if (need_check_driver_exist.get())
			check_driver_exist();
// Check the MySQL database exist
		if (need_check_database_exist.get())
			check_database_exist();

//// Disconnect old mysql connection
//		if (connection != null)
//			close_connection();
		CmnLogger.format_debug("Try to connect to the MySQL database server[%s]......", DEF_DATABASE_NAME);
		Connection connection = null;
		try 
		{
//jdbc:mysql://localhost/test?useUnicode=true&amp;characterEncoding=Big5: localhost是主機名, test是database名, useUnicode=true&amp;characterEncoding=Big5使用的編碼
			connection = DriverManager.getConnection(String.format("jdbc:mysql://localhost/%s?useUnicode=true&amp;characterEncoding=utf-8", DEF_DATABASE_NAME), DEF_USERNAME, DEF_PASSWORD);
		}
		catch(SQLException ex) //有可能會產生sql exception
		{
			String errmsg = null;
			if (ex.getErrorCode() == MysqlErrorNumbers.ER_DB_CREATE_EXISTS)
			{
// Handle the situation when the database does NOT exist
				errmsg = String.format("The database[%s] does NOT exist", DEF_DATABASE_NAME);
//				CmnLogger.error(errmsg);
//				return CmnDef.RET_FAILURE_MYSQL_UNKNOWN_DATABASE;
			}
			else
			{
				errmsg = "Exception occurs while connection to MySQL, due to:" + ex.toString();
//				CmnLogger.error(errmsg);
////				if(CmnDef.is_show_console())
////					System.err.println(errmsg);
//				return CmnDef.RET_FAILURE_MYSQL;
			}
			CmnLogger.format_error(errmsg);
			throw new ExecuteSQLCommandException(errmsg); 
		}
		CmnLogger.format_debug("Try to connect to the MySQL database server[%s]...... Successfully", DEF_DATABASE_NAME);
		return connection;
	}

	public static short close_connection(Connection connection)
	{
		CmnLogger.debug("Disconnect from the MySQL database server...");
		if (connection != null)
		{
			try 
			{
				connection.close();
			} 
			catch (SQLException e) 
			{
				CmnLogger.format_error("Fail to close the connection to MySQL, due to %s", e.toString());
				return CmnDef.RET_FAILURE_MYSQL; 
			}
			connection = null;
		}
//		database_name = null;
		return CmnDef.RET_SUCCESS;
	}

}
