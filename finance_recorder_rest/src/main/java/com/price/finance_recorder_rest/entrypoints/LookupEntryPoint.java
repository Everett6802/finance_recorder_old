package com.price.finance_recorder_rest.entrypoints;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.BeanUtils;

import com.price.finance_recorder_rest.service.StockPriceAndVolumeDTO;
import com.price.finance_recorder_rest.service.StockPriceAndVolumeService;

@Path("/lookup")
public class LookupEntryPoint 
{
//	@GET
//    @Path("/time_range/sql/{company_number}")
//	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
//	public LookupTimeRangeRsp read_sql_company_time_range(@PathParam("company_number") String company_number)
//	{
//		StockPriceAndVolumeDTO dto = new StockPriceAndVolumeDTO();
//		dto.setStart(start);
//		dto.setLimit(limit);
//
//		dto.validateRequiredFields();
//
//		StockPriceAndVolumeService service = new StockPriceAndVolumeService();
//		List<StockPriceAndVolumeDTO> dto_get_list = service.read(company_number, start, limit);
//
//// Prepare return value
//		List<StockPriceAndVolumeGetRsp> rsp_list = new ArrayList<StockPriceAndVolumeGetRsp>();
//		for (StockPriceAndVolumeDTO dto_get : dto_get_list)
//		{
//			StockPriceAndVolumeGetRsp rsp = new StockPriceAndVolumeGetRsp();
//			BeanUtils.copyProperties(dto_get, rsp);
////			rsp.setHref("/users/" + dto.getUserId());
//			rsp_list.add(rsp);
//		}
//
//		return rsp_list;
//	}
}
