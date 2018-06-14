package com.price.finance_recorder_rest.exceptions;

public enum ExceptionType
{
	API_TEST_ERROR("API Test Error"), INTERNAL_SERVER_ERROR("Internal Server Error");
	
	private String exception_message;
	
	ExceptionType(String exception_message)
	{
		this.exception_message = exception_message;
	}
	
	public String get_exception_message()
	{
		return exception_message;
	}
	
	public void set_exception_message(String exception_message)
	{
		this.exception_message = exception_message;
	}
	
}
