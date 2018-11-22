package com.price.finance_recorder_rest.entrypoints;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.BeanUtils;

import com.price.finance_recorder_rest.service.UserDTO;
import com.price.finance_recorder_rest.service.UserService;


@Path("/user")
public class UserEntryPoint 
{
	@POST
    @Consumes(MediaType.APPLICATION_JSON) // Input format
    @Produces({ MediaType.APPLICATION_JSON,  MediaType.APPLICATION_XML} ) // Output format
    public UserRsp createUser(UserReq req) 
	{
		UserRsp rsp = new UserRsp();

// Prepare UserDTO
        UserDTO dto = new UserDTO();
// Bean object, copy from req to dto
        BeanUtils.copyProperties(req, dto);        
// Pass into service layer
        // Create new user 
        UserService userService = new UserService();
// Return a user transfer object read by database
        UserDTO dto_rsp = userService.create(dto);
 
        //Prepare response
// Only firstName, lastName, email, password, href variables are copied;
// Should NOT contain any sensitive database data 
        BeanUtils.copyProperties(dto_rsp, rsp);
        rsp.setHref("/user/" + dto_rsp.getUserId());

		return rsp;
	}

}
