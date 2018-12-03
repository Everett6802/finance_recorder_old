package com.price.finance_recorder_rest.exceptions;

public class ResourceNotFoundException extends RuntimeException
{
	private static final long serialVersionUID = -2016700172525831165L;
	
	public ResourceNotFoundException(String message)
	{
		super(message);
	}
}
