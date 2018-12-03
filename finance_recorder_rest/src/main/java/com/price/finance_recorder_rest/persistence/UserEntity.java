package com.price.finance_recorder_rest.persistence;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.beans.BeanUtils;


@Entity
@Table(name = "user")
public class UserEntity implements Serializable
{
	private static final long serialVersionUID = -436883682529328450L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private long id;

	@Column(name = "user_id", nullable = false)
    private String userId;
	
	@Column(name = "username", nullable = false)
    private String username;

	@Column(name = "salt", nullable = false)
	private String salt;

	@Column(name = "encrypted_password", nullable = false)
    private String encryptedPassword;

	// token is set only for authentication, can be null
	@Column(name = "token"/*, nullable = false*/)
    private String token;

// The setter/getter of id is a MUST, otherwise BeanUtils::copyProperties() does NOT work
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
    public String getUserId() {
		return userId;
	}
    public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getSalt() {
		return salt;
	}
	public void setSalt(String salt) {
		this.salt = salt;
	}
	public String getEncryptedPassword() {
		return encryptedPassword;
	}
	public void setEncryptedPassword(String encryptedPassword) {
		this.encryptedPassword = encryptedPassword;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
}
