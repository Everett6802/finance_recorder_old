package com.price.finance_recorder_rest.persistence;

import java.util.List;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.persistence.Table;
//import javax.persistence.Query; // Java Persistence Query (JPQL)
//import org.hibernate.Query; // Hibernate Query (HQL), deprecated (since 5.2) use org.hibernate.query.Query instead
// Use the org.hibernate.query.NativeQuery and org.hibernate.query.Query instead
import org.hibernate.query.Query; // Hibernate Query (HQL)
//import org.hibernate.SQLQuery; // SQL Hibernate Query (HQL), deprecated (since 5.2) use org.hibernate.query.NativeQuery instead
import org.hibernate.query.NativeQuery; // SQL Hibernate Query (HQL)
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.BeanUtils;
//import org.hibernate.exception.SQLGrammarException;

import com.price.finance_recorder_rest.common.CmnLogger;
import com.price.finance_recorder_rest.common.CmnDBDef;
import com.price.finance_recorder_rest.common.CmnDef;
import com.price.finance_recorder_rest.common.CmnDef.FinanceMethod;
import com.price.finance_recorder_rest.common.CmnFunc;

// Import individual members. 
// Say import static some.package.DA.save instead of DA.*. 
// That will make it much easier to find where this imported method is coming from.
// https://stackoverflow.com/questions/420791/what-is-a-good-use-case-for-static-import-of-methods
import static com.price.finance_recorder_rest.common.CmnClass.Reversed.reversed;

import com.price.finance_recorder_rest.exceptions.ExecuteSQLCommandException;
import com.price.finance_recorder_rest.exceptions.ResourceNotFoundException;
import com.price.finance_recorder_rest.service.UserDTO;

//https://docs.jboss.org/hibernate/orm/3.5/reference/zh-CN/html/batch.html
// Access MySQL through Hibernate API
public class MySQLDAO
{
	private static final int EXECUTE_BATCH_SQL_CMD_COUNT = 20;
	private static final String TABLE_TIME_FIELD_NAME = "trade_date"; 

// Create Table command format
	private static final String FORMAT_CMD_TABLE_CREATE_HEAD_FORMAT = "CREATE TABLE %s (";
	private static final String FORMAT_CMD_TABLE_CREATE_TAIL = ")";
// Create Table command format
	private static final String FORMAT_CMD_TABLE_DROP_FORMAT = "DROP TABLE %s";
// Insert Data command format
	private static final String FORMAT_CMD_TABLE_INSERT_HEAD_FORMAT = "INSERT INTO %s VALUES(";
	private static final String FORMAT_CMD_TABLE_INSERT_TAIL = ")";
// Select Data command format
	private static final String FORMAT_CMD_TABLE_SELECT_FORMAT = "SELECT * FROM %s LIMIT %d, %d";
// Select First One Data command format
	private static final String FORMAT_CMD_TABLE_SELECT_FIRST_ONE_FORMAT = "SELECT * FROM %s ORDER BY %s ASC LIMIT 1";
// Select Last One Data command format
	private static final String FORMAT_CMD_TABLE_SELECT_LAST_ONE_FORMAT = "SELECT * FROM %s ORDER BY %s DESC LIMIT 1";
// Count data command format
	private static final String FORMAT_CMD_TABLE_SELECT_COUNT_FORMAT = "SELECT COUNT(*) FROM %s";

	public static UserDTO create_user(UserDTO dto) 
	{
		Session session = null;
		UserEntity entity = null;
		try
		{
			session = HibernateUtil.open_session();
			Transaction tx = session.beginTransaction();

	        entity = new UserEntity();
	        BeanUtils.copyProperties(dto, entity);

	        session.save(entity);
	        
	        tx.commit();			
		}
		finally
		{
			HibernateUtil.close_session(session);
		}

        UserDTO dto_rsp = new UserDTO();
        BeanUtils.copyProperties(entity, dto_rsp);
        
        return dto_rsp;
	}

	
	public static UserEntity read_user(String username) 
	{
		Session session = null;
		List<UserEntity> result_list = null;
		try
		{
			session = HibernateUtil.open_session();
//In the HQL , you should use the java class name and property name of the mapped @Entity instead of the actual table name and column name 
			String hql_cmd = String.format("FROM %s WHERE username=:username", UserEntity.class.getSimpleName());
// The SQL table is created if NOT exist
			Query<UserEntity> query = session.createQuery(hql_cmd);
			query.setParameter("username", username);
			result_list = query.getResultList();			
		}
		finally
		{
			HibernateUtil.close_session(session);
		}

		if (result_list.isEmpty())
			return null;
		return result_list.get(0);
	}

	public static List<UserEntity> read_users(int start, int limit) 
	{
		Session session = null;
		List<UserEntity> result_list = null;
		try
		{
			session = HibernateUtil.open_session();
//In the HQL , you should use the java class name and property name of the mapped @Entity instead of the actual table name and column name 
			String hql_cmd = String.format("FROM %s", UserEntity.class.getSimpleName());
// The SQL table is created if NOT exist
			Query<UserEntity> query = session.createQuery(hql_cmd);
			query.setFirstResult(start);
			query.setMaxResults(limit);
			result_list = query.getResultList();
		}
		finally
		{
			HibernateUtil.close_session(session);
		}
		return result_list;
	}

    public static void update_user(UserDTO dto) 
    {
	    UserEntity entity = new UserEntity();
	    BeanUtils.copyProperties(dto, entity);
	    
	    Session session = null;
	    try 
	    {
	    	session = HibernateUtil.open_session();
			Transaction tx = session.beginTransaction();
		    session.update(entity);
		    tx.commit();
	    }
		finally
		{
			HibernateUtil.close_session(session);
		}
	}

	public static void delete_user(UserDTO dto) 
	{
        UserEntity entity = new UserEntity();
        BeanUtils.copyProperties(dto, entity);
        
	    Session session = null;
	    try
	    {
	    	session = HibernateUtil.open_session();
			Transaction tx = session.beginTransaction();
	        session.delete(entity);
		    tx.commit();
	    }
		finally
		{
			HibernateUtil.close_session(session);
		}
	}

////////////////////////////////////////////////////////////////////////////////////////////////////

	private static Object create_entity_object_from_string(CmnDef.FinanceMethod finance_method, String data_line)
	{
		String[] data_split = data_line.split(",");
		switch (finance_method)
		{
		case FinanceMethod_TaiwanWeightedIndexAndVolume:
		{
			TaiwanWeightedIndexAndVolumeEntity entity = new TaiwanWeightedIndexAndVolumeEntity();
			entity.setTradeDate(java.sql.Date.valueOf(data_split[0]));
			entity.setTradeVolume(Long.parseLong(data_split[1]));
			entity.setTurnoverInValue(Long.parseLong(data_split[2]));
			entity.setNumberOfTransactions(Integer.parseInt(data_split[3]));
			entity.setWeightedStockIndex(Float.parseFloat(data_split[4]));
			entity.setNetChange(Float.parseFloat(data_split[5]));
			return entity;
		}
		case FinanceMethod_OptionPutCallRatio:
		{
			OptionPutCallRatioEntity entity = new OptionPutCallRatioEntity();
			entity.setTradeDate(java.sql.Date.valueOf(data_split[0]));
			entity.setPutTradeVolume(Long.parseLong(data_split[1]));
			entity.setCallTradeVolume(Long.parseLong(data_split[2]));
			entity.setPutCallRatio(Float.parseFloat(data_split[3]));
			entity.setPutOITradeVolume(Long.parseLong(data_split[4]));
			entity.setCallOITradeVolume(Long.parseLong(data_split[5]));
			entity.setPutCallOIRatio(Float.parseFloat(data_split[6]));
			return entity;
		}
		default :
		{
			String errmsg = String.format("Incorrect finance method[%d] for creating entity object from string", finance_method.ordinal());
			throw new IllegalArgumentException(errmsg);
		}
		}
	}

	private static Object get_entity_table_name(CmnDef.FinanceMethod finance_method)
	{
//If you're using the Table annotation you could do something like this:
		switch (finance_method)
		{
		case FinanceMethod_TaiwanWeightedIndexAndVolume:
		{
			Class<?> c = TaiwanWeightedIndexAndVolumeEntity.class;
			Table table = c.getAnnotation(Table.class);
			return table.name();
		}
		case FinanceMethod_OptionPutCallRatio:
		{
			Class<?> c = OptionPutCallRatioEntity.class;
			Table table = c.getAnnotation(Table.class);
			return table.name();
		}
		default :
		{
			String errmsg = String.format("Incorrect finance method[%d] for getting entity table name", finance_method.ordinal());
			throw new IllegalArgumentException(errmsg);
		}
		}
	}

	private static Object get_entity_class_name(CmnDef.FinanceMethod finance_method)
	{
//If you're using the Table annotation you could do something like this:
		switch (finance_method)
		{
		case FinanceMethod_TaiwanWeightedIndexAndVolume:
		{
			return TaiwanWeightedIndexAndVolumeEntity.class.getSimpleName();
		}
		case FinanceMethod_OptionPutCallRatio:
		{
			return OptionPutCallRatioEntity.class.getSimpleName();
		}
		default :
		{
			String errmsg = String.format("Incorrect finance method[%d] for getting entity class name", finance_method.ordinal());
			throw new IllegalArgumentException(errmsg);
		}
		}
	}

	private static String get_table_name_with_company_number(CmnDef.FinanceMethod finance_method, String company_number)
	{
		return String.format("%s_%s", company_number, CmnDef.FINANCE_DATA_NAME_LIST[finance_method.ordinal()]);
	}
	
	private static void create_table(Connection connection, CmnDef.FinanceMethod finance_method, String company_number)
	{
		String table_name = get_table_name_with_company_number(finance_method, company_number);
		String sql_cmd = null;
//Assemble the SQL command of creating table
		switch (finance_method)
		{
			case FinanceMethod_TaiwanWeightedIndexAndVolume:
			{
				sql_cmd = String.format(FORMAT_CMD_TABLE_CREATE_HEAD_FORMAT, table_name);
				sql_cmd += String.format("%s %s", CmnDBDef.STOCK_PRICE_AND_VOLUME_TABLE_FIELD_NAME_DEFINITION[0], CmnDBDef.STOCK_PRICE_AND_VOLUME_TABLE_FIELD_TYPE_DEFINITION[0]);
				for (int i = 1; i < CmnDBDef.STOCK_PRICE_AND_VOLUME_TABLE_FIELD_NAME_DEFINITION.length ; i++)
				{
					sql_cmd += String.format(",%s %s", CmnDBDef.STOCK_PRICE_AND_VOLUME_TABLE_FIELD_NAME_DEFINITION[i], CmnDBDef.STOCK_PRICE_AND_VOLUME_TABLE_FIELD_TYPE_DEFINITION[i]);
				}
				sql_cmd += FORMAT_CMD_TABLE_CREATE_TAIL;
			}
			break;
			default:
			{
				String errmsg = String.format("Incorrect finance method[%d] for getting create table SQL command", finance_method.ordinal());
				throw new IllegalArgumentException(errmsg);
			}
		}
		CmnLogger.format_debug("The command of creating table: %s", sql_cmd);
// Create MySQL table
		try 
		{
// Create table
			Statement s = connection.createStatement();
			s.executeUpdate(sql_cmd);			
		}
		catch(SQLException ex) //有可能會產生sql exception
		{
			String errmsg = null;
			if (ex.getErrorCode() == 1050) // ER_TABLE_EXISTS_ERROR
			{
// Handle the situation when the database has already existed
				errmsg = String.format("The table[%s] has already existed", table_name);
			}
			else
			{
				errmsg = String.format("Fails to create table[%s], due to: %s", table_name, ex.getMessage());
//				CmnLogger.error(errmsg);
////				if(CmnDef.is_show_console())
////					System.err.println(errmsg);
//				return CmnDef.RET_FAILURE_MYSQL_EXECUTE_COMMAND;
			}
			CmnLogger.format_error(errmsg);
			throw new ExecuteSQLCommandException(errmsg);
		}
//		return sql_cmd;
	}

	private static void insert_data_into_table(Connection connection, CmnDef.FinanceMethod finance_method, List<String> data_line_list, String company_number)
	{
		String table_name = CmnDef.FINANCE_DATA_NAME_LIST[8];
		String sql_cmd = null;
//Assemble the SQL command of creating table
		switch (finance_method)
		{
			case FinanceMethod_TaiwanWeightedIndexAndVolume:
			{
				sql_cmd = String.format(FORMAT_CMD_TABLE_INSERT_HEAD_FORMAT, table_name);
				sql_cmd += "?";
				for (int i = 1; i < CmnDBDef.STOCK_PRICE_AND_VOLUME_TABLE_FIELD_NAME_DEFINITION.length ; i++)
				{
					sql_cmd += ",?";
				}
				sql_cmd += FORMAT_CMD_TABLE_CREATE_TAIL;
			}
			break;
			default:
			{
				String errmsg = String.format("Incorrect finance method[%d] for getting create table SQL command", finance_method.ordinal());
				throw new IllegalArgumentException(errmsg);
			}
		}
		CmnLogger.format_debug("The command of inserting table: %s", sql_cmd);
// Disable Auto Commit
		try
		{
			connection.setAutoCommit(false);
		}
		catch (SQLException e)
		{
			String errmsg = String.format("Fail to enable MySQL auto-comomit, due to: %s", e.toString());
			CmnLogger.format_error(errmsg);
			throw new ExecuteSQLCommandException(errmsg);
		}		
// Insert data into table 
		
		int data_line_list_size = data_line_list.size();
		int cnt = 0, data_line_cnt = 0;
		for (String data_line : data_line_list)
		{
			PreparedStatement pstmt = null;
			try
			{
				pstmt = connection.prepareStatement(sql_cmd);
			}
			catch (SQLException e)
			{
				String errmsg = String.format("Fail to prepare MySQL batch command, due to: %s", e.toString());
				CmnLogger.format_error(errmsg);
				throw new ExecuteSQLCommandException(errmsg);
			}
			String[] data_split = data_line.split(",");
// Transform the date field into SQL Date format
			java.sql.Date sql_date = null;
			try
			{
				java.util.Date date_str = CmnFunc.get_date(data_split[0]);
				sql_date = new java.sql.Date(date_str.getTime());
			}
			catch (ParseException e)
			{
				String errmsg = String.format("Fail to transform the MySQL time format, due to: %s", e.toString());
				CmnLogger.format_error(errmsg);
				throw new ExecuteSQLCommandException(errmsg);
			}

// Generate the SQL batch command
			try
			{
				pstmt.setDate(1, sql_date);
				int data_split_length = data_split.length;
				for (int index = 1 ; index < data_split_length ; index++)
					pstmt.setString(index + 1, data_split[index]);
				pstmt.addBatch();
			}
			catch (SQLException e)
			{
				String errmsg = String.format("Fail to setup MySQL batch command, due to: %s", e.toString());
				CmnLogger.format_error(errmsg);
				throw new ExecuteSQLCommandException(errmsg);
			}
			cnt++;
			data_line_cnt++;
// Execute the command to write the batch messages into the MySQL
// Flush the data in buffer
			if (cnt == EXECUTE_BATCH_SQL_CMD_COUNT || data_line_cnt == data_line_list_size)
			{
				try
				{
//					CmnLogger.format_debug("Insert into data by command: %s", pstmt);
					pstmt.executeBatch();
					connection.commit();
				}
				catch(SQLException ex) //有可能會產生sql exception
				{
					String errmsg = String.format("Fail to insert into data by batch command[%s], due to: %s", pstmt, ex.getMessage());
					CmnLogger.format_error(errmsg);
					throw new ExecuteSQLCommandException(errmsg);
				}
			}
		}
// Enable Auto Commit
		try
		{
			connection.setAutoCommit(true);
		}
		catch (SQLException e)
		{
			String errmsg = String.format("Fail to enable MySQL auto-comomit, due to: %s", e.toString());
			CmnLogger.format_error(errmsg);
			throw new ExecuteSQLCommandException(errmsg);
		}
	}
	
	public static void create(FinanceMethod finance_method, List<String> data_line_list)
	{
		Session session = null;
		try 
		{
			session = HibernateUtil.open_session();
			Transaction tx = session.beginTransaction();

			int cnt = 0;
			for (String data_line : data_line_list)
			{
				Object entity = create_entity_object_from_string(finance_method, data_line);
				session.save(entity);
				if ((++cnt) == EXECUTE_BATCH_SQL_CMD_COUNT) // 20, same as the JDBC batch size
				{
// flush a batch of inserts and release memory:
					session.flush();
					session.clear();
					cnt = 0;
				}
			}

			tx.commit();
		}
		finally
		{
			HibernateUtil.close_session(session);
		}
	}

	public static void create(FinanceMethod finance_method, String company_number, List<String> data_line_list)
	{
// https://www.tutorialspoint.com/jdbc/jdbc-batch-processing.htm
		Connection connection = null;
		try 
		{
			connection = JDBCUtil.open_connection();
// Create table
			create_table(connection, finance_method, company_number);			
// Insert data into table
			insert_data_into_table(connection, finance_method, data_line_list, company_number);

		}
		finally
		{
			JDBCUtil.close_connection(connection);
		}
	}

	public static List<?> read(FinanceMethod finance_method, int start, int limit)
	{
		Session session = null; 
		List<?> result_list = null;
		try
		{
			session = HibernateUtil.open_session();
//In the HQL , you should use the java class name and property name of the mapped @Entity instead of the actual table name and column name 
			String hql_cmd = String.format("FROM %s", get_entity_class_name(finance_method));
// The SQL table is created if NOT exist
			Query<?> query = session.createQuery(hql_cmd);
			query.setFirstResult(start);
			query.setMaxResults(limit);
			result_list = query.getResultList();
		}
		finally
		{
			HibernateUtil.close_session(session);
		}
		return result_list;
	}

	public static List<?> read(FinanceMethod finance_method, String company_number, int start, int limit)
	{
		String table_name = get_table_name_with_company_number(finance_method, company_number);
		Connection connection = null;
		ArrayList<ArrayList<Object>> result_list = new ArrayList<ArrayList<Object>>();
		try 
		{
			connection = JDBCUtil.open_connection();
			String sql_cmd = String.format(FORMAT_CMD_TABLE_SELECT_FORMAT, table_name, start, limit);
			CmnLogger.format_debug("The command of reading table: %s", sql_cmd);
// Read table
			Statement s = connection.createStatement();
			ResultSet rs = s.executeQuery(sql_cmd);
// Parse the field
			String[] field_name_list = CmnDBDef.get_table_field_name_definition(finance_method);
			String[] field_type_list = CmnDBDef.get_table_field_type_definition(finance_method);
			int field_type_list_len = field_type_list.length;
			while(rs.next())
			{
//				Object[] field_value_list = new Object[field_type_list_len];
				ArrayList<Object> field_value_list = new ArrayList<Object>();
				for(int i = 0 ; i < field_type_list_len ; i++)
				{
					String field_name = field_name_list[i];
					String field_type = field_type_list[i];
					if (field_type.equals("INT"))
					{
						field_value_list.add(rs.getInt(field_name));
					}
					else if (field_type.equals("BIGINT"))
					{
						field_value_list.add(rs.getLong(field_name));
					}
					else if (field_type.equals("FLOAT"))
					{
						field_value_list.add(rs.getFloat(field_name));
					}
					else if (field_type.contains("DATE"))
					{
						field_value_list.add(rs.getString(field_name));
					}
					else
					{
						String errmsg = String.format("Unknown SQL field type: %s", field_type);
						CmnLogger.format_error(errmsg);
						throw new ExecuteSQLCommandException(errmsg);
					}
				}
				result_list.add(field_value_list);
			}
		}
		catch(SQLException ex) //有可能會產生sql exception
		{
			String errmsg = null;
			if (ex.getErrorCode() == 1146) // ER_NO_SUCH_TABLE
			{
// Handle the situation when the table does NOT exist
				errmsg = String.format("The table[%s] does NOT exist", table_name);
			}
			else
			{
				errmsg = String.format("Fails to reading table[%s], due to: %s", table_name, ex.getMessage());
			}

			CmnLogger.format_error(errmsg);
			throw new ExecuteSQLCommandException(errmsg);
		}
		finally
		{
			JDBCUtil.close_connection(connection);
		}
		return result_list;
	}

	private static Date[] find_sql_time_range(FinanceMethod finance_method, String company_number)
	{
		Date[] sql_date_time_list = new Date[2];
// Find the start/end time in database
		
		if (company_number == null)
		{
			Session session = null;
//			List<Date[]> update_date_time_list = null;
			try
			{
				session = HibernateUtil.open_session();
//				Date sql_start_date = null;
//				Date sql_end_date = null;
				
				String native_hql_cmd = null;
				List<java.sql.Timestamp> sql_data_list = null;
	// Find the time range in sql
	// Find the start time in the table
				native_hql_cmd = String.format("SELECT %s FROM %s ORDER BY %s ASC LIMIT 1", TABLE_TIME_FIELD_NAME, get_entity_table_name(finance_method), TABLE_TIME_FIELD_NAME);
				sql_data_list = session.createNativeQuery(native_hql_cmd).list();
				if(sql_data_list.isEmpty())
				{
					String errmsg = String.format("The table[%s] is empty while finding the start time", get_entity_table_name(finance_method));
					throw new ResourceNotFoundException(errmsg);
				}
				sql_date_time_list[0] = sql_data_list.get(0);
	// Find the end time in the table
				native_hql_cmd = String.format("SELECT %s FROM %s ORDER BY %s DESC LIMIT 1", TABLE_TIME_FIELD_NAME, get_entity_table_name(finance_method), TABLE_TIME_FIELD_NAME);
				sql_data_list = session.createNativeQuery(native_hql_cmd).list();
				if(sql_data_list.isEmpty())
				{
					String errmsg = String.format("The table[%s] is empty while finding the end time", get_entity_table_name(finance_method));
					throw new ResourceNotFoundException(errmsg);
				}
				sql_date_time_list[1] = sql_data_list.get(0);
			}
			finally
			{
				HibernateUtil.close_session(session);
			}
		}
		else
		{
// Find the start/end time in database
			Connection connection = null;
			String table_name = get_table_name_with_company_number(finance_method, company_number);
			try
			{
				connection = JDBCUtil.open_connection();
//				Date sql_start_date = null;
//				Date sql_end_date = null;
				
				String native_hql_cmd = null;
				List<java.sql.Timestamp> sql_data_list = null;
// Find the time range in sql
				String field_name = CmnDBDef.get_table_field_name_definition(finance_method)[0];
				String field_type = CmnDBDef.get_table_field_type_definition(finance_method)[0];
				if (!field_type.contains("DATE"))
				{
					String errmsg = String.format("The field type should be 'DATE', not %s", field_type);
					throw new RuntimeException(errmsg);
				}
// Find the start time in the table
				String sql_cmd = String.format(FORMAT_CMD_TABLE_SELECT_FIRST_ONE_FORMAT, table_name, TABLE_TIME_FIELD_NAME);
				CmnLogger.format_debug("The command of reading first data in table: %s", sql_cmd);
// Read table
				Statement s = connection.createStatement();
				ResultSet rs = s.executeQuery(sql_cmd);
			    if(!rs.next())
			    {
					String errmsg = String.format("The table[%s] is empty while finding the start time", table_name);
					throw new ResourceNotFoundException(errmsg);
			    }
			    else
			    {
					try 
					{
						String first_time_str = rs.getString(1);
						sql_date_time_list[0] = new SimpleDateFormat("yyyy-MM-dd").parse(first_time_str);
					} 
					catch (ParseException e) 
					{
						String errmsg = String.format("Fail to parse the start time in SQL[%s]", CmnDef.FINANCE_METHOD_DESCRIPTION_LIST[finance_method.value()]);
						throw new IllegalArgumentException(errmsg);
					}
			    }

// Find the end time in the table
			    sql_cmd = String.format(FORMAT_CMD_TABLE_SELECT_LAST_ONE_FORMAT, table_name, TABLE_TIME_FIELD_NAME);
				CmnLogger.format_debug("The command of reading last data in table: %s", sql_cmd);
// Read table
				s = connection.createStatement();
				rs = s.executeQuery(sql_cmd);
			    if(!rs.next())
			    {
					String errmsg = String.format("The table[%s] is empty while finding the end time", table_name);
					throw new ResourceNotFoundException(errmsg);
			    }
			    else
			    {
					try 
					{
						String first_time_str = rs.getString(1);
						sql_date_time_list[0] = new SimpleDateFormat("yyyy-MM-dd").parse(first_time_str);
					} 
					catch (ParseException e) 
					{
						String errmsg = String.format("Fail to parse the start time in SQL[%s]", CmnDef.FINANCE_METHOD_DESCRIPTION_LIST[finance_method.value()]);
						throw new IllegalArgumentException(errmsg);
					}
			    }
			}
			catch(SQLException ex) //有可能會產生sql exception
			{
				String errmsg = String.format("Fails to finding SQL time range in table[%s], due to: %s", table_name, ex.getMessage());
				CmnLogger.format_error(errmsg);
				throw new ExecuteSQLCommandException(errmsg);
			}
			finally
			{
				JDBCUtil.close_connection(connection);
			}
		}
		return sql_date_time_list;
	}

	private static List<Date[]> find_update_time_range(FinanceMethod finance_method, String company_number, List<String> csv_data_line_list)
	{
		if(csv_data_line_list.isEmpty())
		{
			String errmsg = String.format("The CSV data[%s] should NOT be empty", CmnDef.FINANCE_METHOD_DESCRIPTION_LIST[finance_method.value()]);
			throw new ResourceNotFoundException(errmsg);
		}
// Find the time range in SQL
		Date[] sql_date_time_list = find_sql_time_range(finance_method, company_number);
		Date sql_start_date = sql_date_time_list[0];
		Date sql_end_date = sql_date_time_list[1];
// Find the time range in CSV
		Date csv_start_date = null;
		Date csv_end_date = null;
		try 
		{
			String first_line = csv_data_line_list.get(0);
			csv_start_date = new SimpleDateFormat("yyyy-MM-dd").parse(first_line.split(",")[0]);
		} 
		catch (ParseException e) 
		{
			String errmsg = String.format("Fail to parse the start time in CSV[%s]", CmnDef.FINANCE_METHOD_DESCRIPTION_LIST[finance_method.value()]);
			throw new IllegalArgumentException(errmsg);
		}
		try 
		{
			String last_line = csv_data_line_list.get(csv_data_line_list.size() - 1);
			csv_end_date = new SimpleDateFormat("yyyy-MM-dd").parse(last_line.split(",")[0]);
		} 
		catch (ParseException e) 
		{
			String errmsg = String.format("Fail to parse the end time in CSV[%s]", CmnDef.FINANCE_METHOD_DESCRIPTION_LIST[finance_method.value()]);
			throw new IllegalArgumentException(errmsg);
		}
// Find the time range where data does NOT exist
		List<Date[]> update_date_time_list = null;
		long sql_start_date_intvalue = sql_start_date.getTime();
		long sql_end_date_intvalue = sql_end_date.getTime();
		long csv_start_date_intvalue = csv_start_date.getTime();
		long csv_end_date_intvalue = csv_end_date.getTime();
		if (csv_end_date_intvalue <= sql_start_date_intvalue || csv_start_date_intvalue >= sql_start_date_intvalue)
		{
			String errmsg = String.format("The time data does NOT overlap, SQL:[%s-%s], CSV data[%s-%s]", sql_start_date.toString(), sql_end_date.toString(), csv_start_date.toString(), csv_end_date.toString());
			throw new IllegalArgumentException(errmsg);			
		}
		else if (csv_start_date_intvalue >= sql_start_date_intvalue && csv_end_date_intvalue <= sql_end_date_intvalue)
		{
// The data already exist, no need to update	
		}
		else
		{
			update_date_time_list = new ArrayList<Date[]>();
			if(csv_start_date_intvalue < sql_start_date_intvalue && csv_end_date_intvalue > sql_end_date_intvalue)
			{
				update_date_time_list.add(new Date[]{csv_start_date, sql_start_date});
				update_date_time_list.add(new Date[]{sql_end_date, csv_end_date});
			}
			else if (csv_start_date_intvalue < sql_start_date_intvalue && csv_end_date_intvalue >= sql_start_date_intvalue)
			{
				update_date_time_list.add(new Date[]{csv_start_date, sql_start_date});
				update_date_time_list.add(new Date[]{});			
			}
			else if(csv_start_date_intvalue <= sql_end_date_intvalue && csv_end_date_intvalue > sql_end_date_intvalue)
			{
				update_date_time_list.add(new Date[]{});
				update_date_time_list.add(new Date[]{sql_end_date, csv_end_date});							
			}

			else
			{
				String errmsg = String.format("UnDefined overlapped condition, SQL:[%s-%s], CSV data[%s-%s]", sql_start_date.toString(), sql_end_date.toString(), csv_start_date.toString(), csv_end_date.toString());
				throw new IllegalArgumentException(errmsg);
			}
		}
		return update_date_time_list;
	}
	
//	private static List<Date[]> find_update_time_range(FinanceMethod finance_method, List<String> data_line_list)
//	{
//		List<Date[]> update_date_time_list = null;
//		Date sql_start_date = null;
//		Date sql_end_date = null;
//		Date csv_start_date = null;
//		Date csv_end_date = null;
//// Find the start/end time in database
//		Session session = null;
////		List<Date[]> update_date_time_list = null;
//		try
//		{
//			session = HibernateUtil.open_session();
//
//			String native_hql_cmd = null;
////			List<Object[]> sql_data_list = null;
//			List<java.sql.Timestamp> sql_data_list = null;
//// Find the time range in sql
//// Find the start time in the table
//			native_hql_cmd = String.format("SELECT %s FROM %s ORDER BY %s ASC LIMIT 1", TABLE_TIME_FIELD_NAME, get_entity_table_name(finance_method), TABLE_TIME_FIELD_NAME);
//			sql_data_list = session.createNativeQuery(native_hql_cmd).list();
//			if(sql_data_list.isEmpty())
//			{
//				String errmsg = String.format("The table[%s] is empty while finding the start time", get_entity_table_name(finance_method));
//				throw new ResourceNotFoundException(errmsg);
//			}
////			sql_start_date = (Date)sql_data_list.get(0)[0];
//			sql_start_date = sql_data_list.get(0);
//// Find the end time in the table
//			native_hql_cmd = String.format("SELECT %s FROM %s ORDER BY %s DESC LIMIT 1", TABLE_TIME_FIELD_NAME, get_entity_table_name(finance_method), TABLE_TIME_FIELD_NAME);
//			sql_data_list = session.createNativeQuery(native_hql_cmd).list();
//			if(sql_data_list.isEmpty())
//			{
//				String errmsg = String.format("The table[%s] is empty while finding the end time", get_entity_table_name(finance_method));
//				throw new ResourceNotFoundException(errmsg);
//			}
////			sql_end_date = (Date)sql_data_list.get(0)[0];
//			sql_end_date = sql_data_list.get(0);
//	
//			if(data_line_list.isEmpty())
//			{
//				String errmsg = String.format("The CSV data[%s] should NOT be empty", CmnDef.FINANCE_METHOD_DESCRIPTION_LIST[finance_method.value()]);
//				throw new ResourceNotFoundException(errmsg);
//			}
//			
//			try 
//			{
//				String first_line = data_line_list.get(0);
//				csv_start_date = new SimpleDateFormat("yyyy-MM-dd").parse(first_line.split(",")[0]);
//			} 
//			catch (ParseException e) 
//			{
//				String errmsg = String.format("Fail to parse the start time in CSV[%s]", CmnDef.FINANCE_METHOD_DESCRIPTION_LIST[finance_method.value()]);
//				throw new IllegalArgumentException(errmsg);
//			}
//			try 
//			{
//				String last_line = data_line_list.get(data_line_list.size() - 1);
//				csv_end_date = new SimpleDateFormat("yyyy-MM-dd").parse(last_line.split(",")[0]);
//			} 
//			catch (ParseException e) 
//			{
//				String errmsg = String.format("Fail to parse the end time in CSV[%s]", CmnDef.FINANCE_METHOD_DESCRIPTION_LIST[finance_method.value()]);
//				throw new IllegalArgumentException(errmsg);
//			}
//	// Find the time range where data does NOT exist
//			long sql_start_date_intvalue = sql_start_date.getTime();
//			long sql_end_date_intvalue = sql_end_date.getTime();
//			long csv_start_date_intvalue = csv_start_date.getTime();
//			long csv_end_date_intvalue = csv_end_date.getTime();
//			if (csv_end_date_intvalue <= sql_start_date_intvalue || csv_start_date_intvalue >= sql_start_date_intvalue)
//			{
//				String errmsg = String.format("The time data does NOT overlap, SQL:[%s-%s], CSV data[%s-%s]", sql_start_date.toString(), sql_end_date.toString(), csv_start_date.toString(), csv_end_date.toString());
//				throw new IllegalArgumentException(errmsg);			
//			}
//			else if (csv_start_date_intvalue >= sql_start_date_intvalue && csv_end_date_intvalue <= sql_end_date_intvalue)
//			{
//	// The data already exist, no need to update	
//			}
//			else
//			{
//				update_date_time_list = new ArrayList<Date[]>();
//				if(csv_start_date_intvalue < sql_start_date_intvalue && csv_end_date_intvalue > sql_end_date_intvalue)
//				{
//					update_date_time_list.add(new Date[]{csv_start_date, sql_start_date});
//					update_date_time_list.add(new Date[]{sql_end_date, csv_end_date});
//				}
//				else if (csv_start_date_intvalue < sql_start_date_intvalue && csv_end_date_intvalue >= sql_start_date_intvalue)
//				{
//					update_date_time_list.add(new Date[]{csv_start_date, sql_start_date});
//					update_date_time_list.add(new Date[]{});			
//				}
//				else if(csv_start_date_intvalue <= sql_end_date_intvalue && csv_end_date_intvalue > sql_end_date_intvalue)
//				{
//					update_date_time_list.add(new Date[]{});
//					update_date_time_list.add(new Date[]{sql_end_date, csv_end_date});							
//				}
//
//				else
//				{
//					String errmsg = String.format("UnDefined overlapped condition, SQL:[%s-%s], CSV data[%s-%s]", sql_start_date.toString(), sql_end_date.toString(), csv_start_date.toString(), csv_end_date.toString());
//					throw new IllegalArgumentException(errmsg);
//				}
//			}
//		}
//		finally
//		{
//			HibernateUtil.close_session(session);
//		}
////		return update_date_time_list;
//// Find the time range in csv
//		return update_date_time_list;
//	}
	
	private static List<int[]> find_update_csv_range(FinanceMethod finance_method, String company_number, List<String> data_line_list)
	{
		List<Date[]> update_date_time_list = find_update_time_range(finance_method, company_number, data_line_list);
		Date[] update_date_time_range_before = update_date_time_list.get(0);
		Date[] update_date_time_range_after = update_date_time_list.get(1);
		List<int[]> update_csv_range_list = new ArrayList<int[]>();
		if (update_date_time_range_before.length != 0)
		{
// Need to update the new data before the SQL start time
			long update_date_time_range_before_end_intvalue = update_date_time_range_before[1].getTime();
			int count = 0;
			for (String data_line : data_line_list)
			{
				String[] data_split = data_line.split(",");
				long time_intvalue;
				try 
				{
					time_intvalue = CmnFunc.get_date_integer(data_split[0]);
				} 
				catch (ParseException e) 
				{
					String errmsg = String.format("Fail to parse the current time[%s] in CSV[%s]", data_split[0], CmnDef.FINANCE_METHOD_DESCRIPTION_LIST[finance_method.value()]);
					throw new IllegalArgumentException(errmsg);
				}
				if (time_intvalue >= update_date_time_range_before_end_intvalue)
					break;
				count++;
			}
			update_csv_range_list.add(new int[]{0, count});
//			create(finance_method, data_line_list.subList(0, count));
		}
		if (update_date_time_range_after.length != 0)
		{
// Need to update the new data after the SQL end time
			long update_date_time_range_after_start_intvalue = update_date_time_range_after[0].getTime();
			int data_line_list_count = data_line_list.size();
			int count = data_line_list_count;
			for (String data_line : reversed(data_line_list))
			{
				String[] data_split = data_line.split(",");
//				long time_intvalue = CmnFunc.get_date_integer(data_split[0]);
				long time_intvalue;
				try 
				{
					time_intvalue = CmnFunc.get_date_integer(data_split[0]);
				} 
				catch (ParseException e) 
				{
					String errmsg = String.format("Fail to parse the current time[%s] in CSV[%s]", data_split[0], CmnDef.FINANCE_METHOD_DESCRIPTION_LIST[finance_method.value()]);
					throw new IllegalArgumentException(errmsg);
				}
				if (time_intvalue <= update_date_time_range_after_start_intvalue)
					break;
				count--;
			}
			update_csv_range_list.add(new int[]{count, data_line_list_count});
		}
		return update_csv_range_list;
	}

	public static void update(FinanceMethod finance_method, List<String> data_line_list)
	{
		List<int[]> update_csv_range_list = find_update_csv_range(finance_method, null, data_line_list);
		for (int[] update_csv_range : update_csv_range_list)
		{
			create(finance_method, data_line_list.subList(update_csv_range[0], update_csv_range[1]));
		}
	}

	public static void update(FinanceMethod finance_method, String company_number, List<String> data_line_list)
	{
		List<int[]> update_csv_range_list = find_update_csv_range(finance_method, company_number, data_line_list);
		for (int[] update_csv_range : update_csv_range_list)
		{
			create(finance_method, company_number, data_line_list.subList(update_csv_range[0], update_csv_range[1]));
		}
	}

	public static void delete(FinanceMethod finance_method)
	{
		Session session = null;
		try
		{
			session = HibernateUtil.open_session();
			Transaction tx = session.beginTransaction();
// Only remove the items in the table, the table still exists
			String hql_cmd = String.format("DELETE FROM %s", get_entity_class_name(finance_method));
//					try
//					{
			// If the table does NOT exist at first, the table is created and no exception occurs.
			// If the table is dropped manually, then exception occurs: org.hibernate.exception.SQLGrammarException: could not execute statement
			Query<?> query = session.createQuery(hql_cmd);
			query.executeUpdate();
//					}
//					catch (SQLGrammarException e)
//					{
//						String errmsg = String.format("Fail to delete the table[%s], due to: %s", CmnDef.FINANCE_DATA_NAME_LIST[finance_method.value()], e);
//						throw new FinanceRecorderResourceNotFoundException(errmsg);
//					}
					
// Executing an update/delete query, transaction is required
// Otherwise exception occurs: javax.persistence.TransactionRequiredException 
			tx.commit();
		}
		finally
		{
			HibernateUtil.close_session(session);
		}
	}

	public static void delete(FinanceMethod finance_method, String company_number)
	{
		String table_name = get_table_name_with_company_number(finance_method, company_number);
		Connection connection = null;
		try 
		{
			connection = JDBCUtil.open_connection();
			String sql_cmd = String.format(FORMAT_CMD_TABLE_DROP_FORMAT, table_name);
			CmnLogger.format_debug("The command of deleting table: %s", sql_cmd);
// Delete table
			Statement s = connection.createStatement();
			s.executeUpdate(sql_cmd);

		}
		catch(SQLException ex) //有可能會產生sql exception
		{
			String errmsg = null;
			if (ex.getErrorCode() == 1146) // ER_NO_SUCH_TABLE
			{
// Handle the situation when the table does NOT exist
				errmsg = String.format("The table[%s] does NOT exist", table_name);
			}
			else
			{
				errmsg = String.format("Fails to delete table[%s], due to: %s", table_name, ex.getMessage());
			}

			CmnLogger.format_error(errmsg);
			throw new ExecuteSQLCommandException(errmsg);
		}
		finally
		{
			JDBCUtil.close_connection(connection);
		}
	}
	
	public static int count(FinanceMethod finance_method)
	{
		Session session = null;
		int count = 0;
		try
		{
			session = HibernateUtil.open_session();
			String hql_cmd = String.format("SELECT COUNT(*) FROM %s", get_entity_class_name(finance_method));
	// The SQL table is created if NOT exist
			Query<?> query = session.createQuery(hql_cmd);
			count = ((Long)query.uniqueResult()).intValue();
		}
		finally
		{
			HibernateUtil.close_session(session);
		}
		return count;
	}

	public static int count(FinanceMethod finance_method, String company_number)
	{
		String table_name = get_table_name_with_company_number(finance_method, company_number);
		Connection connection = null;
		int count = 0;
		try 
		{
			connection = JDBCUtil.open_connection();
			String sql_cmd = String.format(FORMAT_CMD_TABLE_SELECT_COUNT_FORMAT, table_name);
			CmnLogger.format_debug("The command of counting entries in table: %s", sql_cmd);
// Count entries in table
			Statement s = connection.createStatement();
			ResultSet rs = s.executeQuery(sql_cmd);
		    while(rs.next())
		        count = rs.getInt(1);

		}
		catch(SQLException ex) //有可能會產生sql exception
		{
			String errmsg = null;
			if (ex.getErrorCode() == 1146) // ER_NO_SUCH_TABLE
			{
// Handle the situation when the table does NOT exist
				errmsg = String.format("The table[%s] does NOT exist", table_name);
			}
			else
			{
				errmsg = String.format("Fails to counting entries in table[%s], due to: %s", table_name, ex.getMessage());
			}

			CmnLogger.format_error(errmsg);
			throw new ExecuteSQLCommandException(errmsg);
		}
		finally
		{
			JDBCUtil.close_connection(connection);
		}
		return count;
	}
	
	public static void delete_if_exist(FinanceMethod finance_method)
	{
		if (count(finance_method) != 0)
			delete(finance_method);
	}
	
	public static boolean check_if_exist(FinanceMethod finance_method, String company_number)
	{
		String table_name = get_table_name_with_company_number(finance_method, company_number);
		Connection connection = null;
		boolean exist = false;
		try 
		{
			connection = JDBCUtil.open_connection();
			DatabaseMetaData database_metadata = connection.getMetaData();
			ResultSet res = database_metadata.getTables(null, null, table_name, null);
			exist = res.next(); 

		}
		catch(SQLException ex) //有可能會產生sql exception
		{
			String errmsg = String.format("Fails to check table[%s] exist, due to: %s", table_name, ex.getMessage());
			CmnLogger.format_error(errmsg);
			throw new ExecuteSQLCommandException(errmsg);
		}
		finally
		{
			JDBCUtil.close_connection(connection);
		}
		return exist;
	}
	
	public static void delete_if_exist(FinanceMethod finance_method, String company_number)
	{
		if (check_if_exist(finance_method, company_number))
			delete(finance_method, company_number);
	}
}
