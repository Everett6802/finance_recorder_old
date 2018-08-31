package com.price.finance_recorder_rest.persistence;

import java.util.List;

import javax.persistence.Query;
import javax.persistence.Table;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.price.finance_recorder_rest.common.CmnDef;
import com.price.finance_recorder_rest.common.CmnDef.FinanceMethod;

//https://docs.jboss.org/hibernate/orm/3.5/reference/zh-CN/html/batch.html
public class MySQLDAO
{
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

	public void create(FinanceMethod finance_method, List<String> data_line_list)
	{
		Session session = HibernateUtil.openConnection();
		Transaction tx = session.beginTransaction();

		int cnt = 0;
		for (String data_line : data_line_list)
		{
			Object entity = create_entity_object_from_string(finance_method, data_line);
//			String[] data_split = data_line.split(",");
//			StockExchangeAndVolumeEntity entity = new StockExchangeAndVolumeEntity();
//			entity.setTradeDate(java.sql.Date.valueOf(data_split[0]));
//			entity.setTradeVolume(Long.parseLong(data_split[1]));
//			entity.setTurnoverInValue(Long.parseLong(data_split[2]));
//			entity.setNumberOfTransactions(Integer.parseInt(data_split[3]));
//			entity.setWeightedStockIndex(Float.parseFloat(data_split[4]));
//			entity.setNetChange(Float.parseFloat(data_split[5]));
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

	public List<?> read(FinanceMethod finance_method, int start, int limit)
	{
		Session session = HibernateUtil.openConnection();
		Transaction tx = session.beginTransaction();

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
		String hql_cmd = String.format("FROM %s", get_entity_table_name(finance_method));
		Query query = session.createQuery(hql_cmd);
		query.setFirstResult(start);
		query.setMaxResults(limit);
		List<?> result_list = query.getResultList();
		HibernateUtil.closeConnection(session);
		return result_list;
	}
}
