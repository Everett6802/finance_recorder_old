package com.price.finance_recorder_rest.exceptions;

public class CouldNotCreateRecordException extends RuntimeException 
{
	private static final long serialVersionUID = 8940771678056367471L;

    public CouldNotCreateRecordException(String message)
    {
        super(message);
    }
}
