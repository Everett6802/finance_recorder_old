package com.price.finance_recorder_rest.persistence;

import java.util.List;
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
//import org.hibernate.exception.SQLGrammarException;

import com.price.finance_recorder_rest.common.CmnDef;
import com.price.finance_recorder_rest.common.CmnDef.FinanceMethod;
import com.price.finance_recorder_rest.common.CmnFunc;

// Import individual members. 
// Say import static some.package.DA.save instead of DA.*. 
// That will make it much easier to find where this imported method is coming from.
// https://stackoverflow.com/questions/420791/what-is-a-good-use-case-for-static-import-of-methods
import static com.price.finance_recorder_rest.common.CmnClass.Reversed.reversed;
import com.price.finance_recorder_rest.exceptions.FinanceRecorderResourceNotFoundException;

//https://docs.jboss.org/hibernate/orm/3.5/reference/zh-CN/html/batch.html
// Access MySQL through Hibernate API
public class MySQLDAO
{
	private static final String TABLE_TIME_FIELD_NAME = "trade_date"; 

	private static Object create_entity_object_from_string(CmnDef.FinanceMethod finance_method, String data_line)
	{
		String[] data_split = data_line.split(",");
		switch (finance_method)
		{
		case FinanceMethod_StockExchangeAndVolume :
		{
			StockExchangeAndVolumeEntity entity = new StockExchangeAndVolumeEntity();
			entity.setTradeDate(java.sql.Date.valueOf(data_split[0]));
			entity.setTradeVolume(Long.parseLong(data_split[1]));
			entity.setTurnoverInValue(Long.parseLong(data_split[2]));
			entity.setNumberOfTransactions(Integer.parseInt(data_split[3]));
			entity.setWeightedStockIndex(Float.parseFloat(data_split[4]));
			entity.setNetChange(Float.parseFloat(data_split[5]));
			return entity;
		}
		case FinanceMethod_OptionPutCallRatio :
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
			String errmsg = String.format("Unknown finance method: %d", finance_method.ordinal());
			throw new IllegalArgumentException(errmsg);
		}
		}
	}

	private static Object get_entity_table_name(CmnDef.FinanceMethod finance_method)
	{
//If you're using the Table annotation you could do something like this:
		switch (finance_method)
		{
		case FinanceMethod_StockExchangeAndVolume :
		{
			Class<?> c = StockExchangeAndVolumeEntity.class;
			Table table = c.getAnnotation(Table.class);
			return table.name();
		}
		case FinanceMethod_OptionPutCallRatio :
		{
			Class<?> c = OptionPutCallRatioEntity.class;
			Table table = c.getAnnotation(Table.class);
			return table.name();
		}
		default :
		{
			String errmsg = String.format("Unknown finance method: %d", finance_method.ordinal());
			throw new IllegalArgumentException(errmsg);
		}
		}
	}

	private static Object get_entity_class_name(CmnDef.FinanceMethod finance_method)
	{
//If you're using the Table annotation you could do something like this:
		switch (finance_method)
		{
		case FinanceMethod_StockExchangeAndVolume :
		{
			return StockExchangeAndVolumeEntity.class.getSimpleName();
		}
		case FinanceMethod_OptionPutCallRatio :
		{
			return OptionPutCallRatioEntity.class.getSimpleName();
		}
		default :
		{
			String errmsg = String.format("Unknown finance method: %d", finance_method.ordinal());
			throw new IllegalArgumentException(errmsg);
		}
		}
	}

	public static void create(FinanceMethod finance_method, List<String> data_line_list)
	{
		Session session = HibernateUtil.openConnection();
		Transaction tx = session.beginTransaction();

		int cnt = 0;
		for (String data_line : data_line_list)
		{
			Object entity = create_entity_object_from_string(finance_method, data_line);
			session.save(entity);
			if ((++cnt) == 20) // 20, same as the JDBC batch size
			{
// flush a batch of inserts and release memory:
				session.flush();
				session.clear();
				cnt = 0;
			}
		}

		tx.commit();
		HibernateUtil.closeConnection(session);
	}

	public static List<?> read(FinanceMethod finance_method, int start, int limit)
	{
		Session session = HibernateUtil.openConnection();
//		Transaction tx = session.beginTransaction();

//		CriteriaBuilder cb = session.getCriteriaBuilder();
//
//		// Create Criteria against a particular persistent class
//		CriteriaQuery<UserEntity> criteria = cb.createQuery(UserEntity.class);
//		// Query roots always reference entities
//		Root<UserEntity> userRoot = criteria.from(UserEntity.class);
//		criteria.select(userRoot);
//
//		// Fetch results from start to a number of "limit"
//		List<UserEntity> searchResults = session.createQuery(criteria).setFirstResult(start).setMaxResults(limit).getResultList();
//
//		List<UserDTO> returnValue = new ArrayList<UserDTO>();
//		for (UserEntity userEntity : searchResults)
//		{
//			UserDTO userDto = new UserDTO();
//			BeanUtils.copyProperties(userEntity, userDto);
//			returnValue.add(userDto);
//		}
//		String hql_cmd = String.format("FROM %s", get_entity_table_name(finance_method));

//In the HQL , you should use the java class name and property name of the mapped @Entity instead of the actual table name and column name 
		String hql_cmd = String.format("FROM %s", get_entity_class_name(finance_method));
// The SQL table is created if NOT exist
		Query<?> query = session.createQuery(hql_cmd);
		query.setFirstResult(start);
		query.setMaxResults(limit);
		List<?> result_list = query.getResultList();
		HibernateUtil.closeConnection(session);
		return result_list;
	}

	private static List<Date[]> find_update_time_range(FinanceMethod finance_method, List<String> data_line_list)
	{
		Session session = HibernateUtil.openConnection();
		Transaction tx = session.beginTransaction();

		Date sql_start_date = null;
		Date sql_end_date = null;
		Date csv_start_date = null;
		Date csv_end_date = null;

		String native_hql_cmd = null;
//		List<Object[]> sql_data_list = null;
		List<java.sql.Timestamp> sql_data_list = null;
// Find the time range in sql
// Find the start time in the table
		native_hql_cmd = String.format("SELECT %s FROM %s ORDER BY %s ASC LIMIT 1", TABLE_TIME_FIELD_NAME, get_entity_table_name(finance_method), TABLE_TIME_FIELD_NAME);
		sql_data_list = session.createNativeQuery(native_hql_cmd).list();
		if(sql_data_list.isEmpty())
		{
			String errmsg = String.format("The table[%s] is empty while finding the start time", get_entity_table_name(finance_method));
			throw new FinanceRecorderResourceNotFoundException(errmsg);
		}
//		sql_start_date = (Date)sql_data_list.get(0)[0];
		sql_start_date = sql_data_list.get(0);
// Find the end time in the table
		native_hql_cmd = String.format("SELECT %s FROM %s ORDER BY %s DESC LIMIT 1", TABLE_TIME_FIELD_NAME, get_entity_table_name(finance_method), TABLE_TIME_FIELD_NAME);
		sql_data_list = session.createNativeQuery(native_hql_cmd).list();
		if(sql_data_list.isEmpty())
		{
			String errmsg = String.format("The table[%s] is empty while finding the end time", get_entity_table_name(finance_method));
			throw new FinanceRecorderResourceNotFoundException(errmsg);
		}
//		sql_end_date = (Date)sql_data_list.get(0)[0];
		sql_end_date = sql_data_list.get(0);
// Find the time range in csv
		if(data_line_list.isEmpty())
		{
			String errmsg = String.format("The CSV data[%s] should NOT be empty", CmnDef.FINANCE_METHOD_DESCRIPTION_LIST[finance_method.value()]);
			throw new FinanceRecorderResourceNotFoundException(errmsg);
		}
		
		try 
		{
			String first_line = data_line_list.get(0);
			csv_start_date = new SimpleDateFormat("yyyy-MM-dd").parse(first_line.split(",")[0]);
		} 
		catch (ParseException e) 
		{
			String errmsg = String.format("Fail to parse the start time in CSV[%s]", CmnDef.FINANCE_METHOD_DESCRIPTION_LIST[finance_method.value()]);
			throw new IllegalArgumentException(errmsg);
		}
		try 
		{
			String last_line = data_line_list.get(data_line_list.size() - 1);
			csv_end_date = new SimpleDateFormat("yyyy-MM-dd").parse(last_line.split(",")[0]);
		} 
		catch (ParseException e) 
		{
			String errmsg = String.format("Fail to parse the end time in CSV[%s]", CmnDef.FINANCE_METHOD_DESCRIPTION_LIST[finance_method.value()]);
			throw new IllegalArgumentException(errmsg);
		}
// Find the time range where data does NOT exist
		long sql_start_date_intvalue = sql_start_date.getTime();
		long sql_end_date_intvalue = sql_end_date.getTime();
		long csv_start_date_intvalue = csv_start_date.getTime();
		long csv_end_date_intvalue = csv_end_date.getTime();
		List<Date[]> update_date_time_range_list = null;
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
			update_date_time_range_list = new ArrayList<Date[]>();
			if(csv_start_date_intvalue < sql_start_date_intvalue && csv_end_date_intvalue > sql_end_date_intvalue)
			{
				update_date_time_range_list.add(new Date[]{csv_start_date, sql_start_date});
				update_date_time_range_list.add(new Date[]{sql_end_date, csv_end_date});
			}
			else if (csv_start_date_intvalue < sql_start_date_intvalue && csv_end_date_intvalue >= sql_start_date_intvalue)
			{
				update_date_time_range_list.add(new Date[]{csv_start_date, sql_start_date});
				update_date_time_range_list.add(new Date[]{});			
			}
			else if(csv_start_date_intvalue <= sql_end_date_intvalue && csv_end_date_intvalue > sql_end_date_intvalue)
			{
				update_date_time_range_list.add(new Date[]{});
				update_date_time_range_list.add(new Date[]{sql_end_date, csv_end_date});							
			}

			else
			{
				String errmsg = String.format("UnDefined overlapped condition, SQL:[%s-%s], CSV data[%s-%s]", sql_start_date.toString(), sql_end_date.toString(), csv_start_date.toString(), csv_end_date.toString());
				throw new IllegalArgumentException(errmsg);
			}
		}
		
		HibernateUtil.closeConnection(session);
		return update_date_time_range_list;
	}
	
	private static List<int[]> find_update_csv_range(FinanceMethod finance_method, List<String> data_line_list)
	{
		List<Date[]> update_date_time_range_list = find_update_time_range(finance_method, data_line_list);
		Date[] update_date_time_range_before = update_date_time_range_list.get(0);
		Date[] update_date_time_range_after = update_date_time_range_list.get(1);
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
//			create(finance_method, data_line_list.subList(count, data_line_list_count));
		}
		return update_csv_range_list;
	}

	public static void update(FinanceMethod finance_method, List<String> data_line_list)
	{
		List<int[]> update_csv_range_list = find_update_csv_range(finance_method, data_line_list);
		for (int[] update_csv_range : update_csv_range_list)
		{
			create(finance_method, data_line_list.subList(update_csv_range[0], update_csv_range[1]));
		}
	}
	
	public static void delete(FinanceMethod finance_method)
	{
		Session session = HibernateUtil.openConnection();
		Transaction tx = session.beginTransaction();
// Only remove the items in the table, the table still exists
		String hql_cmd = String.format("DELETE FROM %s", get_entity_class_name(finance_method));
//		try
//		{
// If the table does NOT exist at first, the table is created and no exception occurs.
// If the table is dropped manually, then exception occurs: org.hibernate.exception.SQLGrammarException: could not execute statement
		Query<?> query = session.createQuery(hql_cmd);
		query.executeUpdate();
//		}
//		catch (SQLGrammarException e)
//		{
//			String errmsg = String.format("Fail to delete the table[%s], due to: %s", CmnDef.FINANCE_DATA_NAME_LIST[finance_method.value()], e);
//			throw new FinanceRecorderResourceNotFoundException(errmsg);
//		}
		
// Executing an update/delete query, transaction is required
// Otherwise exception occurs: javax.persistence.TransactionRequiredException 
		tx.commit();
		HibernateUtil.closeConnection(session);
	}

	public static int count(FinanceMethod finance_method)
	{
		Session session = HibernateUtil.openConnection();
//		Transaction tx = session.beginTransaction();
		String hql_cmd = String.format("SELECT COUNT(*) FROM %s", get_entity_class_name(finance_method));
//		int count = ((Long)session.createQuery(hql_cmd).uniqueResult()).intValue();
// The SQL table is created if NOT exist
		Query<?> query = session.createQuery(hql_cmd);
		int count = ((Long)query.uniqueResult()).intValue();

//		tx.commit();
		HibernateUtil.closeConnection(session);	
		return count;
	}
	
	public static void delete_if_exist(FinanceMethod finance_method)
	{
		if (count(finance_method) != 0)
			delete(finance_method);
	}
}
