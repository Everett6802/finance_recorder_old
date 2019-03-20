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

import com.price.finance_recorder_rest.common.CmnDef.FinanceMethod;
import com.price.finance_recorder_rest.exceptions.MissingRequiredFieldException;
import com.price.finance_recorder_rest.namebinding.Secured;
import com.price.finance_recorder_rest.service.FinanceServiceDelegator;
import com.price.finance_recorder_rest.service.StockPriceAndVolumeDTO;
import com.price.finance_recorder_rest.service.StockPriceAndVolumeService;


@Path("/stock_exchange_and_volume")
public class StockPriceAndVolumeEntryPoint 
{
    @Secured
    @POST
    @Path("/{company_number}")
    @Consumes(MediaType.APPLICATION_JSON) // Input format
    @Produces({ MediaType.APPLICATION_JSON,  MediaType.APPLICATION_XML} ) // Output format
    public StockPriceAndVolumeRsp create_stock_price_and_volume(@PathParam("company_number") String company_number, StockPriceAndVolumeReq req) 
	{
		if (req == null)
			throw new MissingRequiredFieldException("Got null request");
		StockPriceAndVolumeDTO dto = new StockPriceAndVolumeDTO();
//Bean object, copy from requestObject to userDto
//Only firstName, lastName, email, password variables are copied;
		BeanUtils.copyProperties(req, dto);
		dto.validateRequiredFields();

		StockPriceAndVolumeService service = new StockPriceAndVolumeService();
		service.create(company_number, dto);

		StockPriceAndVolumeRsp rsp = new StockPriceAndVolumeRsp();

		return rsp;
	}

//	@Secured
	@GET
    @Path("/{company_number}")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public List<StockPriceAndVolumeGetRsp> read_stock_price_and_volume(@PathParam("company_number") String company_number, @DefaultValue("0") @QueryParam("start") int start, @DefaultValue("20") @QueryParam("limit") int limit)
	{
		StockPriceAndVolumeDTO dto = new StockPriceAndVolumeDTO();
		dto.setStart(start);
		dto.setLimit(limit);

		dto.validateRequiredFields();

		StockPriceAndVolumeService service = new StockPriceAndVolumeService();
		List<StockPriceAndVolumeDTO> dto_get_list = service.read(company_number, start, limit);

// Prepare return value
		List<StockPriceAndVolumeGetRsp> rsp_list = new ArrayList<StockPriceAndVolumeGetRsp>();
		for (StockPriceAndVolumeDTO dto_get : dto_get_list)
		{
			StockPriceAndVolumeGetRsp rsp = new StockPriceAndVolumeGetRsp();
			BeanUtils.copyProperties(dto_get, rsp);
//			rsp.setHref("/users/" + dto.getUserId());
			rsp_list.add(rsp);
		}

		return rsp_list;
	}

    @Secured
    @PUT
    @Path("/{company_number}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public StockPriceAndVolumeRsp update_stock_price_and_volume(@PathParam("company_number") String company_number, StockPriceAndVolumeReq req) 
    {    
		if (req == null)
			throw new MissingRequiredFieldException("Got null request");
		StockPriceAndVolumeDTO dto = new StockPriceAndVolumeDTO();
//Bean object, copy from requestObject to userDto
//Only firstName, lastName, email, password variables are copied;
		BeanUtils.copyProperties(req, dto);
		dto.validateRequiredFields();

		StockPriceAndVolumeService service = new StockPriceAndVolumeService();
		service.update(company_number, dto);

		StockPriceAndVolumeRsp rsp = new StockPriceAndVolumeRsp();

		return rsp;
    }

    @Secured
    @DELETE
    @Path("/{company_number}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public StockPriceAndVolumeRsp delete_stock_price_and_volume(@PathParam("company_number") String company_number) 
    {        
		StockPriceAndVolumeService service = new StockPriceAndVolumeService();
		service.delete(company_number);

		StockPriceAndVolumeRsp rsp = new StockPriceAndVolumeRsp();

		return rsp;
    }

	@GET
    @Path("/{company_number}/sql/metadata")
	@Produces({MediaType.TEXT_PLAIN})
	public String read_stock_price_and_volume_sql_metadata(@PathParam("company_number") String company_number)
	{
		StockPriceAndVolumeService service = new StockPriceAndVolumeService();
		String metadata = service.read_sql_metadata(company_number);
		return metadata;
	}

	@GET
    @Path("/{company_number}/csv/metadata")
	@Produces({MediaType.TEXT_PLAIN})
	public String read_stock_price_and_volume_csv_metadata(@PathParam("company_number") String company_number, @DefaultValue("") @QueryParam("dataset_folderpath") String dataset_folderpath)
	{
		StockPriceAndVolumeDTO dto = new StockPriceAndVolumeDTO();
		if (!dataset_folderpath.isEmpty())
			dto.setDatasetFolderpath(dataset_folderpath);
//Bean object, copy from requestObject to userDto
//Only firstName, lastName, email, password variables are copied;
		dto.validateRequiredFields();

		StockPriceAndVolumeService service = new StockPriceAndVolumeService();
		String metadata = service.read_csv_metadata(company_number, dto);
		return metadata;
	}
}
