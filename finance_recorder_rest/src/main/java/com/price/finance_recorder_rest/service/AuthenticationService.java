package com.price.finance_recorder_rest.service;

import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import org.springframework.beans.BeanUtils;

import com.price.finance_recorder_rest.exceptions.AuthenticationException;
import com.price.finance_recorder_rest.exceptions.ExceptionType;
import com.price.finance_recorder_rest.persistence.MySQLDAO;
import com.price.finance_recorder_rest.persistence.UserEntity;

public class AuthenticationService 
{
	public UserDTO authenticate(String username, String password) throws AuthenticationException 
	{
// Username must exist in the system
        UserEntity entity = MySQLDAO.read_user(username); 
        if (entity == null)
            throw new AuthenticationException(String.format("%s: %s", ExceptionType.AUTHENTICATION_FAILED.name(), "User does NOT exist"));

        String encrypted_password = SecurityUtil.generateSecurePassword(password, entity.getSalt());
// Check the password
        boolean authenticated = false;
        if (encrypted_password != null)
        	authenticated = encrypted_password.equalsIgnoreCase(entity.getEncryptedPassword());
        if (!authenticated)
            throw new AuthenticationException(String.format("%s: %s", ExceptionType.AUTHENTICATION_FAILED.name(), "Incorrect password"));

		UserDTO dto = new UserDTO();
		BeanUtils.copyProperties(entity, dto);
		return dto;
	}

    public String issue_access_token(UserDTO dto) throws AuthenticationException 
    {
//        String returnValue = null;
        String new_salt_as_postfix = dto.getSalt();
        String access_token_material = dto.getUserId() + new_salt_as_postfix;

        byte[] encrypted_access_token = null;
        try 
        {
            encrypted_access_token = SecurityUtil.encrypt(dto.getEncryptedPassword(), access_token_material);
        } 
        catch (InvalidKeySpecException ex) 
        {
//            Logger.getLogger(AuthenticationServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new AuthenticationException(String.format("%s:%s", ExceptionType.AUTHENTICATION_FAILED.name(), "Faled to issue secure access token"));
        }

        String encrypted_access_token_base64_encoded = Base64.getEncoder().encodeToString(encrypted_access_token);
// Split token into equal parts
        int tokenLength = encrypted_access_token_base64_encoded.length();
        String token_to_save_to_database = encrypted_access_token_base64_encoded.substring(0, tokenLength / 2);
// Update the token into database
        dto.setToken(token_to_save_to_database);
        MySQLDAO.update_user(dto);

        String access_token = encrypted_access_token_base64_encoded.substring(tokenLength / 2, tokenLength);
        return access_token;
    }

    public void reset_security_cridentials(String password, UserDTO dto) 
    {
// Generate a new salt
        String salt = SecurityUtil.getSalt(30);
// Generate a new encrypted password 
        String securePassword = SecurityUtil.generateSecurePassword(password, salt);
        dto.setSalt(salt);
        dto.setEncryptedPassword(securePassword);
// Update user profile 
        MySQLDAO.update_user(dto);
    }
}
