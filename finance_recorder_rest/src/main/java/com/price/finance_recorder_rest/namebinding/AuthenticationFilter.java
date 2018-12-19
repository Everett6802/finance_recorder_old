package com.price.finance_recorder_rest.namebinding;

import java.io.IOException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

import com.price.finance_recorder_rest.exceptions.AuthenticationException;
import com.price.finance_recorder_rest.exceptions.ExceptionType;
import com.price.finance_recorder_rest.persistence.MySQLDAO;
import com.price.finance_recorder_rest.persistence.UserEntity;
import com.price.finance_recorder_rest.service.AuthenticationService;
import com.price.finance_recorder_rest.service.SecurityUtil;


@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter 
{
	@Override
    public void filter(ContainerRequestContext requestContext) throws IOException 
	{
// Extract Authorization header details
        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer")) 
        	throw new AuthenticationException(String.format("%s: %s", ExceptionType.AUTHENTICATION_FAILED.name(), "Authorization header must be provided"));
// Extract the token
        String token = authorizationHeader.substring("Bearer".length()).trim();
// Extract user name
//        String userId = requestContext.getUriInfo().getPathParameters().getFirst("id");
        UriInfo uri_info = requestContext.getUriInfo();
        MultivaluedMap<String, String> path_pathParameters = uri_info.getPathParameters();
        String username = path_pathParameters.getFirst("name");
        
        validate_token(token, username);
    }
    
    private void validate_token(String token, String username) throws AuthenticationException 
    {
    	List<UserEntity> entity_list = null;
    	if (username == null)
    	{
    		entity_list = MySQLDAO.read_users(0, 8);
    	}
    	else
    	{
// Username must exist in the system
            UserEntity entity = MySQLDAO.read_user(username); 
            if (entity == null)
                throw new AuthenticationException(String.format("%s: %s", ExceptionType.AUTHENTICATION_FAILED.name(), "User does NOT exist"));    		
            entity_list = new LinkedList<UserEntity>();
            entity_list.add(entity);
    	}
    	AuthenticationException auth_ex = null;
// Create Access token material out of the useId received and salt kept database
    	for (UserEntity entity : entity_list)
    	{
    		auth_ex = null;
        	String encrypted_access_token_base64_encoded = null;
    	    try 
    	    {
    	    	encrypted_access_token_base64_encoded = SecurityUtil.get_encrypted_access_token_base64_encoded(entity.getSalt(), entity.getUserId(), entity.getEncryptedPassword());
    	    }
    	    catch (InvalidKeySpecException ex) 
    	    {
    	        Logger.getLogger(AuthenticationService.class.getName()).log(Level.SEVERE, null, ex);
//    	        throw new AuthenticationException(String.format("%s:%s", ExceptionType.AUTHENTICATION_FAILED.name(), "Faled to issue secure access token"));
    	        auth_ex = new AuthenticationException(String.format("%s:%s", ExceptionType.AUTHENTICATION_FAILED.name(), "Faled to issue secure access token"));
    	        continue;
    	    }
// Compare two access tokens 
// Assemble Access token using two parts. One from DB and one from http request.
            String complete_token = entity.getToken() + token;
            if (!encrypted_access_token_base64_encoded.equalsIgnoreCase(complete_token))
            {
//                throw new AuthenticationException(String.format("%s:%s", ExceptionType.AUTHENTICATION_FAILED.name(), "Authorization token did not match"));    		
    	        auth_ex = new AuthenticationException(String.format("%s:%s", ExceptionType.AUTHENTICATION_FAILED.name(), "Authorization token did not match")); 
    	        continue;    	
            }
            if (auth_ex == null)
            	break;
    	}
    	if (auth_ex != null)
    		throw auth_ex;
    }
}
