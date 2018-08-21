package com.price.finance_recorder_rest.persistence;

import java.util.List;

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
//			entity.setDate(java.sql.Date.valueOf(data_split[0]));
//			entity.setTradeVolume(Long.parseLong(data_split[1]));
//			entity.setTurnoverInValue(Long.parseLong(data_split[2]));
//			entity.setNumberOfTransactions(Integer.parseInt(data_split[3]));
//			entity.setIndex(Float.parseFloat(data_split[4]));
//			entity.setChange(Float.parseFloat(data_split[5]));
			return entity;
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
//			Object entity = create_entity_object_from_string(finance_method, data_line);
			String[] data_split = data_line.split(",");
			StockExchangeAndVolumeEntity entity = new StockExchangeAndVolumeEntity();
			entity.setTradeDate(java.sql.Date.valueOf(data_split[0]));
			entity.setTradeVolume(Long.parseLong(data_split[1]));
			entity.setTurnoverInValue(Long.parseLong(data_split[2]));
			entity.setNumberOfTransactions(Integer.parseInt(data_split[3]));
			entity.setWeightedStockIndex(Float.parseFloat(data_split[4]));
			entity.setNetChange(Float.parseFloat(data_split[5]));
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
}
