package com.price.finance_recorder_rest.entrypoints;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.price.finance_recorder_rest.service.AuthenticationService;
import com.price.finance_recorder_rest.service.UserDTO;


@Path("/authentication")
public class AuthenticationEntryPoint 
{
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public AuthenticationRsp login(AuthenticationReq req)
    {
        AuthenticationRsp rsp = new AuthenticationRsp();        
        AuthenticationService authenticationService = new AuthenticationService();
// Authentication
        UserDTO dto = authenticationService.authenticate(req.getUsername(), req.getPassword());
// Update the new encrypted password
        authenticationService.reset_security_cridentials(req.getPassword(), dto);
// Get Access Token
        String access_token = authenticationService.issue_access_token(dto);
        
        rsp.setUserId(dto.getUserId());
        rsp.setUsername(dto.getUsername());
        rsp.setToken(access_token);
        
        return rsp;
    }
}
