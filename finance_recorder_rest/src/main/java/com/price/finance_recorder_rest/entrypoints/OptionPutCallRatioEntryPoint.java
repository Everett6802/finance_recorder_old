package com.price.finance_recorder_rest.entrypoints;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.BeanUtils;

import com.price.finance_recorder_rest.exceptions.FinanceRecorderMissingRequiredFieldException;
import com.price.finance_recorder_rest.service.OptionPutCallRatioDTO;
import com.price.finance_recorder_rest.service.OptionPutCallRatioService;

@Path("/option_put_call_ratio")
public class OptionPutCallRatioEntryPoint
{
	@POST
	@Consumes(MediaType.APPLICATION_JSON) // Input format
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML}) // Output
																								// format
	public OptionPutCallRatioRsp create_option_put_call_ratio(OptionPutCallRatioReq req)
	{
		if (req == null)
			throw new FinanceRecorderMissingRequiredFieldException("Got null request");
		OptionPutCallRatioDTO dto = new OptionPutCallRatioDTO();
//Bean object, copy from requestObject to userDto
//Only firstName, lastName, email, password variables are copied;
		BeanUtils.copyProperties(req, dto);
		dto.validateRequiredFields();

		OptionPutCallRatioService service = new OptionPutCallRatioService();
		service.create(dto);

		OptionPutCallRatioRsp rsp = new OptionPutCallRatioRsp();

		return rsp;
	}
}
