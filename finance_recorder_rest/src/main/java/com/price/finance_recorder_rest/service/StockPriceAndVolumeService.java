package com.price.finance_recorder_rest.service;

import java.util.ArrayList;
import java.util.List;

//import org.springframework.beans.BeanUtils;

import com.price.finance_recorder_rest.common.CmnDef;


public class StockPriceAndVolumeService 
{
	public void create(String company_number, StockPriceAndVolumeDTO dto)
	{
		FinanceServiceDelegator.create_table(CmnDef.FinanceMethod.FinanceMethod_StockPriceAndVolume, company_number, dto.getDatasetFolderpath()/*, new MySQLDAO()*/);
	}

	public List<StockPriceAndVolumeDTO> read(String company_number, int start, int limit)
	{
		List<?> field_array_list = FinanceServiceDelegator.read_table(CmnDef.FinanceMethod.FinanceMethod_StockPriceAndVolume, company_number, start, limit/*, new MySQLDAO()*/);
		List<StockPriceAndVolumeDTO> dto_list = new ArrayList<StockPriceAndVolumeDTO>();
		for (Object field_array : field_array_list)
		{
			StockPriceAndVolumeDTO dto = new StockPriceAndVolumeDTO();
			dto.fillin_field((ArrayList<Object>)field_array);
			dto_list.add(dto);
		}
		return dto_list;
	}

	public void update(String company_number, StockPriceAndVolumeDTO dto)
	{
		FinanceServiceDelegator.update_table(CmnDef.FinanceMethod.FinanceMethod_StockPriceAndVolume, company_number, dto.getDatasetFolderpath()/*, new MySQLDAO()*/);
	}

	public void delete(String company_number)
	{
		FinanceServiceDelegator.delete_table(CmnDef.FinanceMethod.FinanceMethod_StockPriceAndVolume, company_number/*, new MySQLDAO()*/);
	}

	public String read_sql_metadata(String company_number)
	{
		return FinanceServiceDelegator.read_table_metadata(CmnDef.FinanceMethod.FinanceMethod_StockPriceAndVolume, company_number);
	}

	public String read_csv_metadata(String company_number, StockPriceAndVolumeDTO dto)
	{
		return FinanceServiceDelegator.read_file_metadata(CmnDef.FinanceMethod.FinanceMethod_StockPriceAndVolume, company_number, dto.getDatasetFolderpath());
	}
}
