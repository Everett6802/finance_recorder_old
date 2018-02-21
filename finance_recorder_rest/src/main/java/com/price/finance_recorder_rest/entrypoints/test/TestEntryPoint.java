package com.price.finance_recorder_rest.entrypoints.test;

import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/test")
public class TestEntryPoint
{
	@GET
	@Produces("text/html")
	public Response test_get()
	{
		String output = "<h1>Finance Recorder API Test<h1>" + "<p>GET Request is working ... <br>Ping @ "
				+ new Date().toString() + "</p<br>";
		return Response.status(200).entity(output).build();
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON) // Input format
	@Produces("text/html") // Output format
	public Response test_put(TestRequest req)
	{
		String output = "<h1>Finance Recorder API Test<h1>"
				+ String.format("<p>POST Request is working, FirstName: %s, LastName: %s ... <br>Ping @ %s",
						req.get_first_name(), req.get_last_name(), new Date().toString())
				+ "</p<br>";
		return Response.status(200).entity(output).build();
	}
}