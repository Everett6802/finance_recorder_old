package com.price.finance_recorder_rest.exceptions;

public class ExecuteSQLCommandException extends RuntimeException 
{
	private static final long serialVersionUID = -4708718222478303820L;

    public ExecuteSQLCommandException(String message)
    {
        super(message);
    }
}
