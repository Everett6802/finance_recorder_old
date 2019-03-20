package com.price.finance_recorder_rest.persistence;

import org.hibernate.HibernateException;
import org.hibernate.Metamodel;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.metadata.ClassMetadata;

public class HibernateUtil
{
//	private static SessionFactory sessionFactory = null;
	private static StockNamingStrategy stock_name_strategy = null;

	private static SessionFactory getSessionFactory()
	{
		SessionFactory sessionFactory = null;
//		if (sessionFactory == null)
		Configuration conf = new Configuration();
		conf.configure();
		try
		{
//               sessionFactory = new Configuration().configure().buildSessionFactory();
			sessionFactory = conf.buildSessionFactory();
		}
		catch (HibernateException e)
		{
			System.err.println("Initial SessionFactory creation failed: " + e);
			throw new ExceptionInInitializerError(e);
		}
		catch (Exception e)
		{
			System.err.println("Exception occurs due to: " + e);
			throw e;
		}
		return sessionFactory;
	}

//	private static SessionFactory getSessionFactory(String company_number)
//	{
//		if (sessionFactory == null)
//		{
//			Configuration conf = new Configuration();
//			
//			try
//			{
//				if (company_number != null)
//				{
//					conf.configure("hibernate_company.cfg.xml");
//					if (stock_name_strategy == null)
//						stock_name_strategy = new StockNamingStrategy();
//					stock_name_strategy.set_company_number(company_number);
//					conf.setPhysicalNamingStrategy(stock_name_strategy);
//				}
//				else
//					conf.configure();
////               sessionFactory = new Configuration().configure().buildSessionFactory();
//				sessionFactory = conf.buildSessionFactory();
//			}
//			catch (HibernateException e)
//			{
//				System.err.println("Initial SessionFactory creation failed: " + e);
//				throw new ExceptionInInitializerError(e);
//			}
//			catch (Exception e)
//			{
//				System.err.println("Exception occurs due to: " + e);
//				throw e;
//			}
//		}
//		return sessionFactory;
//	}

//	public static Session open_session(boolean reset_session_factory)
//	{
////		SessionFactory sessionFactory = getSessionFactory();
////		return sessionFactory.openSession();
//		if (reset_session_factory) sessionFactory = null;
//		return getSessionFactory(null).openSession();
//	}
//	public static Session open_session(){return open_session(true);}
//
//	public static Session open_session(String company_number, boolean reset_session_factory)
//	{
////		SessionFactory sessionFactory = getSessionFactory();
////		return sessionFactory.openSession();
//		if (reset_session_factory) sessionFactory = null;
//		return getSessionFactory(company_number).openSession();
//	}
//	public static Session open_session(String company_number){return open_session(company_number, true);}

	@SuppressWarnings("deprecation")
	public static ClassMetadata get_metadata(Class<?> class_obj)
	{
		SessionFactory sessionFactory = getSessionFactory();
		return sessionFactory.getClassMetadata(class_obj);
	}

	public static Session open_session()
	{
		SessionFactory sessionFactory = getSessionFactory();
		return sessionFactory.openSession();
	}
	
	public static void close_session(Session session)
	{
		if (session != null)
		{
			session.close();
		}
	}

}
