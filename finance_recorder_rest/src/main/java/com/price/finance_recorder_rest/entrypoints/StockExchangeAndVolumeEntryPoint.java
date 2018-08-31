package com.price.finance_recorder_rest.entrypoints;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
	public StockExchangeAndVolumeRsp create_stock_exchange_and_volume(StockExchangeAndVolumeReq req)
	{
		if (req == null)
			throw new FinanceRecorderMissingRequiredFieldException("Got null request");
		StockExchangeAndVolumeDTO dto = new StockExchangeAndVolumeDTO();
//Bean object, copy from requestObject to userDto
//Only firstName, lastName, email, password variables are copied;
		BeanUtils.copyProperties(req, dto);
		dto.validateRequiredFields();

		StockExchangeAndVolumeService service = new StockExchangeAndVolumeService();
		service.create(req.getDatasetFolderpath());

		StockExchangeAndVolumeRsp rsp = new StockExchangeAndVolumeRsp();

		return rsp;
	}

	@GET
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public List<StockExchangeAndVolumeRsp> read_stock_exchange_and_volume(@DefaultValue("0") @QueryParam("start") int start, @DefaultValue("50") @QueryParam("limit") int limit)
	{
		StockExchangeAndVolumeDTO dto = new StockExchangeAndVolumeDTO();
		dto.setStart(start);
		dto.setLimit(limit);

		dto.validateRequiredFields();

		StockExchangeAndVolumeService service = new StockExchangeAndVolumeService();
		List<StockExchangeAndVolumeDTO> dto_list = service.read(start, limit);

		// Prepare return value
		List<StockExchangeAndVolumeRsp> returnValue = new ArrayList<StockExchangeAndVolumeRsp>();
		for (StockExchangeAndVolumeDTO dto : dto_list)
		{
			StockExchangeAndVolumeRsp rsp = new StockExchangeAndVolumeRsp();
			BeanUtils.copyProperties(dto, rsp);
			userModel.setHref("/users/" + userDto.getUserId());
			returnValue.add(rsp);
		}

		return returnValue;
	}
}
