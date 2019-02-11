package com.price.finance_recorder_rest.exceptions;

public enum ExceptionType
{
	API_TEST_ERROR("API Test Error"),
	AUTHENTICATION_FAILED("Authentication failed"),
	RESOURCE_NOT_FOUND("Missing Resource File"),
	MISSING_REQUIRED_FIELD("Missing required field"),
    RECORD_ALREADY_EXISTS("Record already exists"),
    NO_RECORD_FOUND("Record with provided username is NOT found"),
    COULD_NOT_UPDATE_RECORD("Could not update record"),
    COULD_NOT_DELETE_RECORD("Could not delete record"),
    SQL_EXECUTION_FAILED("MySQL command execution failed"),
    INCORRECT_OPERATION("Incorrect operation"),
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
