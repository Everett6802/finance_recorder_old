package com.price.stock_recorder;

import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


public abstract class StockWriterBase implements StockRecorderCmnDef.StockWriterInf
{
	private static final String DEF_SERVER = "localhost";
	private static final String DEF_USERNAME = "root";
	private static final String DEF_PASSWORD = "lab4man1";
	private static final String DEF_DATABASE = "finance";

	private static final String format_cmd_create_database = "CREATE DATABASE %s";

//	protected String format_cmd_create_table = "CREATE TABLE sql%s (date VARCHAR(16), time VARCHAR(16), severity INT, data VARCHAR(512))";
	protected static final String format_cmd_create_table_head = "CREATE TABLE %s (";
	protected static final String format_cmd_create_table_tail = ")";
//	private static final String format_cmd_insert_into_table = "INSERT INTO sql%s VALUES(\"%s\", \"%s\", %d, \"%s\")";
	protected static final String format_cmd_insert_into_table_head = "INSERT INTO %s (";
	protected static final String format_cmd_insert_into_table_mid = ")VALUES(";
	protected static final String format_cmd_insert_into_table_tail = ") ON DUPLICATE KEY UPDATE";

	protected Map<Integer, Integer> sql_file_field_mapping_table = new HashMap<Integer, Integer>();

	protected String format_cmd_insert_into_table_head_with_name = null;
	protected String cmd_create_table = null;
	protected PreparedStatement[] cmd_insert_data_list = new PreparedStatement[StockRecorderCmnDef.EACH_UPDATE_DATA_AMOUNT];

	protected Connection connection = null; //Database objects, 連接object
	private boolean table_created = false;
	private String server = null;
	private String username = null;
	private String password = null;
	private String database = null;
	protected String table = null;

	StockRecorderCmnDef.StockObserverInf parent_observer = null;

	public StockWriterBase()
	{
		server = DEF_SERVER;
		username = DEF_USERNAME;
		password = DEF_PASSWORD;
		database = DEF_DATABASE;
	}

	private short try_connect_mysql()
	{
		StockRecorderCmnDef.format_debug("Try to connect to the MySQL database server...");
		try 
		{
// Java要連接資料庫時，需使用到JDBC-Driver，連接MySQL資料庫使用Connector/j，下載後解開壓縮，mysql-connector-java-5.1.15-bin.jar就是MySQL的JDBC-Driver了.
// Go to http://blog.yslifes.com/archives/918 for more detailed info
// JDBC API is a Java API that can access any kind of tabular data, especially data stored in a Relational Database. 
// JDBC works with Java on a variety of platforms, such as Windows, Mac OS, and the various versions of UNIX.
// Go to http://www.tutorialspoint.com/jdbc/index.htm to see the example of MySQL command by JDBC
//註冊driver
			Class.forName("com.mysql.jdbc.Driver");
//jdbc:mysql://localhost/test?useUnicode=true&amp;characterEncoding=Big5: localhost是主機名, test是database名, useUnicode=true&amp;characterEncoding=Big5使用的編碼
			connection = DriverManager.getConnection(String.format("jdbc:mysql://localhost/%s?useUnicode=true&amp;characterEncoding=Big5", database), username, password);
		}
		catch(ClassNotFoundException ex)
		{
			System.out.println("DriverClassNotFound:" + ex.toString());
			return StockRecorderCmnDef.RET_FAILURE_MYSQL;
		}
		catch(SQLException ex) //有可能會產生sql exception
		{
			StockRecorderCmnDef.format_debug("The %s database does NOT exist, create a NEW one", database);
			try
			{
				connection = DriverManager.getConnection(String.format("jdbc:mysql://localhost/?user=%s&password=%s", username, password)); 
				Statement s = connection.createStatement();
				String cmd_create_database = String.format(format_cmd_create_database, database);
				StockRecorderCmnDef.format_debug("Try to create database[%s] by command: %s", database, cmd_create_database);
				s.executeUpdate(cmd_create_database);
			}
			catch(SQLException ex1) //有可能會產生sql exception
			{
				System.out.println("Exception:" + ex1.toString());
				return StockRecorderCmnDef.RET_FAILURE_MYSQL;
			}
			StockRecorderCmnDef.format_debug("Try to connect to the MySQL database server...... Successfully");
		}

		return StockRecorderCmnDef.RET_SUCCESS;
	}

	public short open_table()
	{
// Check if the connection is established
		if (connection == null)
		{
			StockRecorderCmnDef.error("The connection is NOT established");
			return  StockRecorderCmnDef.RET_FAILURE_MYSQL;
		}

		if (!table_created)
		{
// Create the table in the database...
			try
			{
				Statement s = connection.createStatement();
//				cmd_buf = String.format(cmd_create_table, current_time_string);
				StockRecorderCmnDef.format_debug("Try to create table[%s] by command: %s", table, cmd_create_table);

				s.executeUpdate(cmd_create_table);
			}
			catch(SQLException ex) //有可能會產生sql exception
			{
				if (ex.getErrorCode() == 1050)
					StockRecorderCmnDef.format_debug("The table[%s] has already existed", table);
				else
				{
					StockRecorderCmnDef.format_error("Fails to create table[%s], due to: %d, %s", table, ex.getMessage());
					return StockRecorderCmnDef.RET_FAILURE_MYSQL;
				}
			}			
			table_created = true;
		}

		return StockRecorderCmnDef.RET_SUCCESS;
	}

	@Override
	public short initialize(StockRecorderCmnDef.StockObserverInf observer, String table_name, List<String> sql_file_field_mapping)
	{
		parent_observer = observer;
		table = table_name;
		format_cmd_insert_into_table_head_with_name = String.format(format_cmd_insert_into_table_head, table);
//		StockRecorderCmnDef.debug("Initialize the MsgDumperSql object......");

		short ret = StockRecorderCmnDef.RET_SUCCESS;
// Create the connection to the MySQL server
		ret = try_connect_mysql();
		if (StockRecorderCmnDef.CheckFailure(ret))
			return ret;

// Generate the SQL command to create table
		ret = format_field_cmd();
		if (StockRecorderCmnDef.CheckFailure(ret))
			return ret;

// Create table
		ret = open_table();
		if (StockRecorderCmnDef.CheckFailure(ret))
			return ret;

// Generate the mapping table of sql and file field position
		int split = -1;
		Integer sql_pos;
		Integer file_pos;
		for (String mapping : sql_file_field_mapping)
		{
			split = mapping.indexOf(':');
			if (split == -1)
			{
				StockRecorderCmnDef.format_debug("Incorrect format for mapping: %s", mapping);
				return StockRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
			}
			sql_pos = Integer.valueOf(mapping.substring(0, split));
			file_pos = Integer.valueOf(mapping.substring(split + 1));
			StockRecorderCmnDef.format_debug("SQL File Mapping Item: (%d: %d)", sql_pos, file_pos);
			sql_file_field_mapping_table.put(sql_pos, file_pos);
		}

// Check if the date index in the mapping table
		if (!sql_file_field_mapping_table.containsKey(get_date_index()))
		{
			StockRecorderCmnDef.error("The DATE index is NOT found in the mapping table");
			return StockRecorderCmnDef.RET_FAILURE_INCORRECT_CONFIG;
		}

		return StockRecorderCmnDef.RET_SUCCESS;
	}

	@Override
	public short write(List<String> data_list)
	{
// Format the SQL command of inserting data
		short ret = format_data_cmd(data_list);
		if (StockRecorderCmnDef.CheckFailure(ret))
			return ret;

// Write the message into the log file
		try
		{
			for (PreparedStatement cmd_insert_data : cmd_insert_data_list)
			{
				StockRecorderCmnDef.format_debug("Insert data by command: %s", cmd_insert_data);
				cmd_insert_data.executeUpdate();
			}
		}
		catch(SQLException ex) //有可能會產生sql exception
		{
			StockRecorderCmnDef.format_error("Fails to insert data into table[%s], due to: %s", table, ex.getMessage());
			return  StockRecorderCmnDef.RET_FAILURE_MYSQL;
		}

		return  StockRecorderCmnDef.RET_SUCCESS;
	}

	@Override
	public short deinitialize()
	{
		StockRecorderCmnDef.format_debug("DeInitialize the MsgDumperSql object......");
		StockRecorderCmnDef.format_debug("Release the parameters connected to the MySQL database server");
		if (connection != null)
		{
			try 
			{
				connection.close();
			} 
			catch (SQLException e) 
			{
				// TODO Auto-generated catch block
				StockRecorderCmnDef.format_error("Fail to close the connection to MySQL, due to %s", e.toString());
				return StockRecorderCmnDef.RET_FAILURE_MYSQL; 
			}
			connection = null;
		}

		return StockRecorderCmnDef.RET_SUCCESS;
	}

	protected abstract int get_date_index();
	protected abstract short format_field_cmd();
	protected abstract short format_data_cmd(List<String> data_list);
}
