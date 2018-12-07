package com.price.finance_recorder_rest.exceptions;

public class NoRecordFoundException extends RuntimeException
{
	private static final long serialVersionUID = -6025318935285128607L;
	
	public NoRecordFoundException(String message)
	{
		super(message);
	}
}
