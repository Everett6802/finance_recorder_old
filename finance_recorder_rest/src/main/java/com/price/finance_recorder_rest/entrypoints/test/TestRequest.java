package com.price.finance_recorder_rest.entrypoints.test;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement // Used to convert to JSON/XML
public class TestRequest
{
	private String first_name;
	private String last_name;
	
	public String get_first_name()
	{
		return first_name;
	}
	
	public void set_first_name(String first_name)
	{
		this.first_name = first_name;
	}
	
	public String get_last_name()
	{
		return last_name;
	}
	
	public void set_last_name(String last_name)
	{
		this.last_name = last_name;
	}
	
}
