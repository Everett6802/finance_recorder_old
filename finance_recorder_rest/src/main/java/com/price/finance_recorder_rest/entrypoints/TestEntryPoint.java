package com.price.finance_recorder_rest.entrypoints;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.Date;
import java.util.LinkedList;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.hibernate.Session;
import org.hibernate.Transaction;
//import org.springframework.beans.BeanUtils;

//import com.price.finance_recorder_rest.common.CmnDef;
import com.price.finance_recorder_rest.common.CmnFunc;
//import com.price.finance_recorder_rest.exceptions.ResourceNotFoundException;
import com.price.finance_recorder_rest.persistence.HibernateUtil;


@Path("/test")
public class TestEntryPoint 
{
//	@POST
//	@Path("/naming_strategy/{company_number}")
//	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
//	public Response create_naming_strategy(@PathParam("company_number") String company_number)
//	{
//		Session session = null;
//		StockPriceAndVolumeEntity entity = null;
//		try
//		{
//			session = HibernateUtil.openConnection(company_number);
//			Transaction tx = session.beginTransaction();
//
//	        entity = new StockPriceAndVolumeEntity();
//	        Date TradeDate = CmnFunc.get_date("2019-01-08");
//	        entity.setTradeDate(TradeDate);
//
//	        session.save(entity);
//	        
//	        tx.commit();
//			HibernateUtil.closeConnection(session);
//		} 
//		catch (ParseException e) 
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

//		return Response.status(200).entity(entity).build();
//	}
}
