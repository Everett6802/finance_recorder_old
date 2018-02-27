package com.price.finance_recorder_rest.entrypoints.help;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;


@Path("/help")
public class HelpEntryPoint
{
	@GET
	@Produces("text/html")
	public Response help_get()
	{
		// String output = "<h3>Finance Recorder Help<h3>" + "<p>GET Request is
		// working ... <br>Ping @ "
		// + new Date().toString() + "</p<br>";
		String output = "";
		try
		{
			BufferedReader br = new BufferedReader(
					new InputStreamReader(getClass().getClassLoader().getResourceAsStream("help.html")));
			String line = null;
			while ((line = br.readLine()) != null)
			{
				output += line;
			}
		} catch (IOException e)
		{
			// String err = String.format("Error occur while reading the data,
			// due to: %s", e.toString());
			// PRINT_STDERR(err);
		}
		
		return Response.status(200).entity(output).build();
	}
}
