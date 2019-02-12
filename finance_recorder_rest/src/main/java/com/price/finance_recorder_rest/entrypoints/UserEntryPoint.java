package com.price.finance_recorder_rest.entrypoints;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.BeanUtils;

import com.price.finance_recorder_rest.namebinding.Secured;
import com.price.finance_recorder_rest.service.UserDTO;
import com.price.finance_recorder_rest.service.UserService;


@Path("/user")
public class UserEntryPoint 
{
//    @Secured
	@POST
    @Consumes(MediaType.APPLICATION_JSON) // Input format
    @Produces({ MediaType.APPLICATION_JSON,  MediaType.APPLICATION_XML} ) // Output format
    public UserRsp create_user(UserReq req) 
	{
		UserRsp rsp = new UserRsp();
// Prepare UserDTO
        UserDTO dto = new UserDTO();
// Bean object, copy from req to dto
        BeanUtils.copyProperties(req, dto);        
// Pass into service layer
// Create new user 
        UserService service = new UserService();
// Return a user transfer object read by database
        UserDTO dto_rsp = service.create(dto);
//Prepare response. Should NOT contain any sensitive database data 
        BeanUtils.copyProperties(dto_rsp, rsp);
        rsp.setHref("/user/" + dto_rsp.getUserId());

		return rsp;
	}

	@Secured
	@GET
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public List<UserRsp> read_user(@DefaultValue("0") @QueryParam("start") int start, @DefaultValue("20") @QueryParam("limit") int limit)
	{
		UserService service = new UserService();
		List<UserDTO> dto_list = service.read(start, limit);
// Prepare response
		List<UserRsp> rsp_list = new ArrayList<UserRsp>();
		for (UserDTO dto : dto_list)
		{
			UserRsp rsp = new UserRsp();
			BeanUtils.copyProperties(dto, rsp);
			rsp.setHref("/user/" + dto.getUserId());
			rsp_list.add(rsp);
		}

		return rsp_list;
	}

	@Secured
	@GET
	@Path("/{name}")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public UserRsp read_user_by_name(@PathParam("name") String username)
	{
		UserService service = new UserService();
		UserDTO dto = service.read_by_username(username);
// Prepare response
		UserRsp rsp = new UserRsp();
		BeanUtils.copyProperties(dto, rsp);
		rsp.setHref("/user/" + dto.getUserId());

		return rsp;
	}

    @Secured
    @PUT
    @Path("/{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public UserRsp update_user(@PathParam("name") String username, UserUpdateReq req) 
    {    
        UserService service = new UserService();
        UserDTO dto = service.read_by_username(username);
// Bean object, copy from req to dto
        BeanUtils.copyProperties(req, dto);   
// Update User Details
        service.update(dto);
// Prepare return value 
        UserRsp rsp = new UserRsp();
        BeanUtils.copyProperties(dto, rsp);

        return rsp;
    }

    @Secured
    @DELETE
    @Path("/{name}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public UserRsp delete_user(@PathParam("name") String username) 
    {        
        UserService service = new UserService();
        UserDTO dto = service.read_by_username(username);
     
        service.delete(dto);
 
        UserRsp rsp = new UserRsp();
        return rsp;
    }
}
