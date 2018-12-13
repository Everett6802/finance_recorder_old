package com.price.finance_recorder_rest.entrypoints;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class UserRsp 
{
	private String userId;
	private String username;
	private String href;

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
	public String getHref() {
		return href;
	}
	public void setHref(String href) {
		this.href = href;
	}
}
