package com.price.finance_recorder_rest.exceptions;

public class MissingRequiredFieldException extends RuntimeException
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2149502034205818865L;

	public MissingRequiredFieldException(String message)
	{
		super(message);
	}
}
