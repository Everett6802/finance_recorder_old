package com.price.finance_recorder_rest.entrypoints;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AuthenticationRsp 
{
	private String userId;
	private String username;
    private String token;

	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}

}
