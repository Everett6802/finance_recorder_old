package com.price.finance_recorder_rest.exceptions;

public class FinanceRecorderMissingRequiredFieldException extends RuntimeException
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2149502034205818865L;

	public FinanceRecorderMissingRequiredFieldException(String message)
	{
		super(message);
	}
}
