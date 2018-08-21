package com.price.finance_recorder_rest.entrypoints;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.BeanUtils;

import com.price.finance_recorder_rest.exceptions.FinanceRecorderMissingRequiredFieldException;
import com.price.finance_recorder_rest.service.StockExchangeAndVolumeDTO;
import com.price.finance_recorder_rest.service.StockExchangeAndVolumeService;

@Path("/stock_exchange_and_volume")
public class StockExchangeAndVolumeEntryPoint
{
	@POST
	@Consumes(MediaType.APPLICATION_JSON) // Input format
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML}) // Output
																								// format
	public StockExchangeAndVolumeRsp read_stock_price(StockExchangeAndVolumeReq req)
	{
		if (req == null)
			throw new FinanceRecorderMissingRequiredFieldException("Got null request");
		StockExchangeAndVolumeDTO dto = new StockExchangeAndVolumeDTO();
//Bean object, copy from requestObject to userDto
//Only firstName, lastName, email, password variables are copied;
		BeanUtils.copyProperties(req, dto);
		dto.validateRequiredFields();

		StockExchangeAndVolumeService service = new StockExchangeAndVolumeService();
		service.create(dto);

		StockExchangeAndVolumeRsp rsp = new StockExchangeAndVolumeRsp();

		return rsp;
	}
}
