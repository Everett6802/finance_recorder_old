package com.price.finance_recorder_rest.exceptions;

public class FinanceRecorderResourceNotFoundException extends RuntimeException
{
	private static final long serialVersionUID = -2016700172525831165L;
	
	public FinanceRecorderResourceNotFoundException(String message)
	{
		super(message);
	}
}
