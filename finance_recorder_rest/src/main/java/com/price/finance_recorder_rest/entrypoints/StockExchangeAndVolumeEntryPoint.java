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
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML}) // Output format
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
	public List<StockExchangeAndVolumeGetRsp> read_stock_exchange_and_volume(@DefaultValue("0") @QueryParam("start") int start, @DefaultValue("50") @QueryParam("limit") int limit)
	{
		StockExchangeAndVolumeDTO dto = new StockExchangeAndVolumeDTO();
		dto.setStart(start);
		dto.setLimit(limit);

		dto.validateRequiredFields();

		StockExchangeAndVolumeService service = new StockExchangeAndVolumeService();
		List<StockExchangeAndVolumeDTO> dto_get_list = service.read(start, limit);

// Prepare return value
		List<StockExchangeAndVolumeGetRsp> returnValue = new ArrayList<StockExchangeAndVolumeGetRsp>();
		for (StockExchangeAndVolumeDTO dto_get : dto_get_list)
		{
			StockExchangeAndVolumeGetRsp rsp = new StockExchangeAndVolumeGetRsp();
			BeanUtils.copyProperties(dto_get, rsp);
//			rsp.setHref("/users/" + dto.getUserId());
			returnValue.add(rsp);
		}

		return returnValue;
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON) // Input format
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML}) // Output format
	public StockExchangeAndVolumeRsp update_stock_exchange_and_volume(StockExchangeAndVolumeReq req)
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

	@DELETE
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public StockExchangeAndVolumeRsp delete_stock_exchange_and_volume(/*StockExchangeAndVolumeReq req*/)
	{
//		if (req == null)
//			throw new FinanceRecorderMissingRequiredFieldException("Got null request");
//		StockExchangeAndVolumeDTO dto = new StockExchangeAndVolumeDTO();

		StockExchangeAndVolumeService service = new StockExchangeAndVolumeService();
		service.delete();

		StockExchangeAndVolumeRsp rsp = new StockExchangeAndVolumeRsp();

		return rsp;
	}
}
