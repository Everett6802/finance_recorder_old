package com.price.finance_recorder_rest.entrypoints;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class UserRsp 
{
	private String user_id;
	private String username;
	private String href;
	public String getUser_id() {
		return user_id;
	}
	public void setUser_id(String user_id) {
		this.user_id = user_id;
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
