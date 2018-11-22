package com.price.finance_recorder_rest.exceptions;

public enum ExceptionType
{
	AUTHENTICATION_FAILED("Authentication failed"),
	API_TEST_ERROR("API Test Error"),
	RESOURCE_NOT_FOUND("Missing Resource File"),
	MISSING_REQUIRED_FIELD("Missing required field"),
	INTERNAL_SERVER_ERROR("Internal Server Error");

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
