package com.price.finance_recorder_rest.entrypoints;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.price.finance_recorder_rest.common.CmnDef;
import com.price.finance_recorder_rest.common.CmnFunc;
import com.price.finance_recorder_rest.exceptions.ResourceNotFoundException;


@Path("/help")
public class HelpEntryPoint
{
	@GET
	@Produces("text/html")
	public Response get_help()
	{
//		System.out.println(String.format("Counter: %d", count++));
		String output = "";
		InputStream is = getClass().getClassLoader().getResourceAsStream("help.html");
		if (is == null)
			throw new ResourceNotFoundException("The help.html is NOT found");

//		try
//		{
//			BufferedReader br = new BufferedReader(new InputStreamReader(is));
//			String line = null;
//			while ((line = br.readLine()) != null)
//			{
//				output += line;
//			}
//		}
//		catch (IOException e)
//		{
//
//			String err = String.format("Error occur while reading data from help.html, due to: %s", e.toString());
//			throw new RuntimeException(err);
//		}
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		LinkedList<String> line_list = new LinkedList<String>();
		short ret = CmnFunc.read_file_lines(br, line_list);
		if (CmnDef.CheckFailure(ret))
		{
			String err = String.format("Error occur while reading data from help.html, due to: %s", CmnDef.GetErrorDescription(ret));
			throw new RuntimeException(err);
		}
		for (String line : line_list)
			output += line;
		return Response.status(200).entity(output).build();
	}


/*
If you were to send: <b><i>test</i></b>
Content-Type: text/html would output: test
Content-Type: text/plain would output: <b><i>test</i></b>
 */
	@GET
	@Path("/method_index")
	@Produces("text/plain")
	public Response get_help_method_index()
	{
		String output = "";
		int finance_data_name_list_len = CmnDef.FINANCE_DATA_NAME_LIST.length;
		for (int i = 0 ; i < finance_data_name_list_len ; i++)
		{
			output += String.format("%d: %s  [%s]\n", i, CmnDef.FINANCE_DATA_NAME_LIST[i], CmnDef.FINANCE_METHOD_DESCRIPTION_LIST[i]);
		}
		return Response.status(200).entity(output).build();
	}
}
