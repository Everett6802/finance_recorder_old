package com.price.finance_recorder_rest.entrypoints;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AuthenticationRsp 
{
	private String id;
    private String token;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}

}
