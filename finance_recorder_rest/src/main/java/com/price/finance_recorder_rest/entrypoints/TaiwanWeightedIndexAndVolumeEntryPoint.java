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

import com.price.finance_recorder_rest.exceptions.MissingRequiredFieldException;
import com.price.finance_recorder_rest.namebinding.Secured;
import com.price.finance_recorder_rest.service.TaiwanWeightedIndexAndVolumeDTO;
import com.price.finance_recorder_rest.service.TaiwanWeightedIndexAndVolumeService;

@Path("/taiwan_weighted_index_and_volume")
public class TaiwanWeightedIndexAndVolumeEntryPoint
{
	@Secured
	@POST
	@Consumes(MediaType.APPLICATION_JSON) // Input format
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML}) // Output format
	public TaiwanWeightedIndexAndVolumeRsp create_stock_exchange_and_volume(TaiwanWeightedIndexAndVolumeReq req)
	{
		if (req == null)
			throw new MissingRequiredFieldException("Got null request");
		TaiwanWeightedIndexAndVolumeDTO dto = new TaiwanWeightedIndexAndVolumeDTO();
//Bean object, copy from requestObject to userDto
//Only firstName, lastName, email, password variables are copied;
		BeanUtils.copyProperties(req, dto);
		dto.validateRequiredFields();

		TaiwanWeightedIndexAndVolumeService service = new TaiwanWeightedIndexAndVolumeService();
		service.create(req.getDatasetFolderpath());

		TaiwanWeightedIndexAndVolumeRsp rsp = new TaiwanWeightedIndexAndVolumeRsp();

		return rsp;
	}

	@GET
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public List<TaiwanWeightedIndexAndVolumeGetRsp> read_stock_exchange_and_volume(@DefaultValue("0") @QueryParam("start") int start, @DefaultValue("20") @QueryParam("limit") int limit)
	{
//		StockExchangeAndVolumeDTO dto = new StockExchangeAndVolumeDTO();
//		dto.setStart(start);
//		dto.setLimit(limit);
//		dto.validateRequiredFields();
		TaiwanWeightedIndexAndVolumeService service = new TaiwanWeightedIndexAndVolumeService();
		List<TaiwanWeightedIndexAndVolumeDTO> dto_list = service.read(start, limit);

// Prepare return value
		List<TaiwanWeightedIndexAndVolumeGetRsp> rsp_list = new ArrayList<TaiwanWeightedIndexAndVolumeGetRsp>();
		for (TaiwanWeightedIndexAndVolumeDTO dto : dto_list)
		{
			TaiwanWeightedIndexAndVolumeGetRsp rsp = new TaiwanWeightedIndexAndVolumeGetRsp();
			BeanUtils.copyProperties(dto, rsp);
//			rsp.setHref("/users/" + dto.getUserId());
			rsp_list.add(rsp);
		}

		return rsp_list;
	}

	@Secured
	@PUT
	@Consumes(MediaType.APPLICATION_JSON) // Input format
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML}) // Output format
	public TaiwanWeightedIndexAndVolumeRsp update_stock_exchange_and_volume(TaiwanWeightedIndexAndVolumeReq req)
	{
		if (req == null)
			throw new MissingRequiredFieldException("Got null request");
		TaiwanWeightedIndexAndVolumeDTO dto = new TaiwanWeightedIndexAndVolumeDTO();
//Bean object, copy from requestObject to userDto
//Only firstName, lastName, email, password variables are copied;
		BeanUtils.copyProperties(req, dto);
		dto.validateRequiredFields();

		TaiwanWeightedIndexAndVolumeService service = new TaiwanWeightedIndexAndVolumeService();
		service.create(req.getDatasetFolderpath());

		TaiwanWeightedIndexAndVolumeRsp rsp = new TaiwanWeightedIndexAndVolumeRsp();

		return rsp;
	}

	@Secured
	@DELETE
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public TaiwanWeightedIndexAndVolumeRsp delete_stock_exchange_and_volume(/*StockExchangeAndVolumeReq req*/)
	{
//		if (req == null)
//			throw new FinanceRecorderMissingRequiredFieldException("Got null request");
//		StockExchangeAndVolumeDTO dto = new StockExchangeAndVolumeDTO();

		TaiwanWeightedIndexAndVolumeService service = new TaiwanWeightedIndexAndVolumeService();
		service.delete();

		TaiwanWeightedIndexAndVolumeRsp rsp = new TaiwanWeightedIndexAndVolumeRsp();

		return rsp;
	}
}
