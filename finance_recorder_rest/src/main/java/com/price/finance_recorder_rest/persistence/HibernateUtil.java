package com.price.finance_recorder_rest.persistence;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil
{
	private static SessionFactory sessionFactory = null;

	private static SessionFactory getSessionFactory()
	{
		if (sessionFactory == null)
		{
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
		}
		return sessionFactory;
	}

	public static Session openConnection()
	{
		SessionFactory sessionFactory = getSessionFactory();
		return sessionFactory.openSession();
	}

	public static void closeConnection(Session session)
	{
		if (session != null)
		{
			session.close();
		}
	}

}
