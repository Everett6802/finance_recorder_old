package com.price.finance_recorder_rest.entrypoints;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.price.finance_recorder_rest.exceptions.FinanceRecorderResourceNotFoundException;


@Path("/help")
public class HelpEntryPoint
{
	static int count = 1;
	
	@GET
	@Produces("text/html")
	public Response get_help()
	{
		System.out.println(String.format("Counter: %d", count++));
		String output = "";
		try
		{
			InputStream is = getClass().getClassLoader().getResourceAsStream("help.html");
			if (is == null)
			{
				throw new FinanceRecorderResourceNotFoundException("The help.html is NOT found");
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = br.readLine()) != null)
			{
				output += line;
			}
		}
		catch (IOException e)
		{
			
			String err = String.format("Error occur while reading data from help.html, due to: %s", e.toString());
			throw new RuntimeException(err);
		}
		
		return Response.status(200).entity(output).build();
	}
}
