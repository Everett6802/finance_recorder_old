package com.price.finance_recorder_rest.service;

import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.BeanUtils;

import com.price.finance_recorder_rest.exceptions.AuthenticationException;
import com.price.finance_recorder_rest.exceptions.ExceptionType;
import com.price.finance_recorder_rest.namebinding.AuthenticationFilter;
import com.price.finance_recorder_rest.persistence.MySQLDAO;
import com.price.finance_recorder_rest.persistence.UserEntity;


public class AuthenticationService 
{
	public UserDTO authenticate(String username, String password) throws AuthenticationException 
	{
		System.err.println("Auth: Check1");
// Username must exist in the system
        UserEntity entity = MySQLDAO.read_user(username); 
        if (entity == null)
            throw new AuthenticationException(String.format("%s: %s", ExceptionType.AUTHENTICATION_FAILED.name(), "User does NOT exist"));
        System.err.println("Auth: Check2");
        String encrypted_password = SecurityUtil.generate_secure_password(password, entity.getSalt());
        System.out.println("Auth: Check3");
// Check the password
        boolean authenticated = false;
        if (encrypted_password != null)
        	authenticated = encrypted_password.equalsIgnoreCase(entity.getEncryptedPassword());
        if (!authenticated)
            throw new AuthenticationException(String.format("%s: %s", ExceptionType.AUTHENTICATION_FAILED.name(), "Incorrect password"));

        System.out.println("Auth: Check4");
		UserDTO dto = new UserDTO();
		BeanUtils.copyProperties(entity, dto);
		System.out.println("Auth: Check5");
		return dto;
	}

    public String issue_access_token(UserDTO dto) throws AuthenticationException 
    {
// Get the complete token
// Create Access token material out of the useId received and salt kept database
//        String new_salt_as_postfix = dto.getSalt();
//        String access_token_material = dto.getUserId() + new_salt_as_postfix;
//        byte[] encrypted_access_token = null;
//        try 
//        {
//            encrypted_access_token = SecurityUtil.encrypt(dto.getEncryptedPassword(), access_token_material);
//        } 
//        catch (InvalidKeySpecException ex) 
//        {
//            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
//            throw new AuthenticationException(String.format("%s:%s", ExceptionType.AUTHENTICATION_FAILED.name(), "Faled to issue secure access token"));
//        }
//        String encrypted_access_token_base64_encoded = Base64.getEncoder().encodeToString(encrypted_access_token);
    	String encrypted_access_token_base64_encoded = null;
	    try 
	    {
	    	encrypted_access_token_base64_encoded = SecurityUtil.get_encrypted_access_token_base64_encoded(dto.getSalt(), dto.getUserId(), dto.getEncryptedPassword());
	    }
	    catch (InvalidKeySpecException ex) 
	    {
	        Logger.getLogger(AuthenticationService.class.getName()).log(Level.SEVERE, null, ex);
	        throw new AuthenticationException(String.format("%s:%s", ExceptionType.AUTHENTICATION_FAILED.name(), "Faled to issue secure access token"));
	    }
// Split token into equal parts
        int token_length = encrypted_access_token_base64_encoded.length();
        String token_to_save_to_database = encrypted_access_token_base64_encoded.substring(0, token_length / 2);
// Update the token into database
        dto.setToken(token_to_save_to_database);
        MySQLDAO.update_user(dto);

        String access_token = encrypted_access_token_base64_encoded.substring(token_length / 2, token_length);
        return access_token;
    }

    public void reset_security_cridentials(String password, UserDTO dto) 
    {
// Generate a new salt
        String salt = SecurityUtil.get_salt(30);
// Generate a new encrypted password 
        String secure_password = SecurityUtil.generate_secure_password(password, salt);
        dto.setSalt(salt);
        dto.setEncryptedPassword(secure_password);
// Update user profile 
        MySQLDAO.update_user(dto);
    }
}
