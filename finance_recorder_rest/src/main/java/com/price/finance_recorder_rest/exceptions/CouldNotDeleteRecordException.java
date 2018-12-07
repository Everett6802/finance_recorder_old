package com.price.finance_recorder_rest.exceptions;

public class CouldNotDeleteRecordException extends RuntimeException 
{
	private static final long serialVersionUID = 7514875241363381777L;
    
	public CouldNotDeleteRecordException(String message)
    {
        super(message);
    }
}
