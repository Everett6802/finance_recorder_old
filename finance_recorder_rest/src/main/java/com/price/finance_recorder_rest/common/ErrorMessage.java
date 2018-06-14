package com.price.finance_recorder_rest.common;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class ErrorMessage
{
	
	private String errorMessage;
	private String errorMessageKey;
	private String href; // The links to the documentation
	
	// Can add the constructor from Eclipse:
	// Source -> Generate Constructor from Superclass
	public ErrorMessage()
	{
	}
	
	// Can add the constructor from Eclipse:
	// Source -> Generate Constructor from Field
	public ErrorMessage(String errorMessage, String errorMessageKey, String href)
	{
		this.errorMessage = errorMessage;
		this.errorMessageKey = errorMessageKey;
		this.href = href;
	}
	
	public String getErrorMessage()
	{
		return errorMessage;
	}
	
	public void setErrorMessage(String errorMessage)
	{
		this.errorMessage = errorMessage;
	}
	
	public String getErrorMessageKey()
	{
		return errorMessageKey;
	}
	
	public void setErrorMessageKey(String errorMessageKey)
	{
		this.errorMessageKey = errorMessageKey;
	}
	
	public String getHref()
	{
		return href;
	}
	
	public void setHref(String href)
	{
		this.href = href;
	}
}
