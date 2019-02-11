package com.price.finance_recorder_rest.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.price.finance_recorder_rest.common.CmnDef;
import com.price.finance_recorder_rest.persistence.MySQLDAO;

public class OptionPutCallRatioService
{
	public void create(OptionPutCallRatioDTO dto)
	{
		FinanceServiceDelegator.create_table(CmnDef.FinanceMethod.FinanceMethod_OptionPutCallRatio, dto.getDatasetFolderpath()/*, new MySQLDAO()*/);
	}

	public List<OptionPutCallRatioDTO> read(int start, int limit)
	{
		List<?> entity_list = FinanceServiceDelegator.read_table(CmnDef.FinanceMethod.FinanceMethod_OptionPutCallRatio, start, limit/*, new MySQLDAO()*/);
		List<OptionPutCallRatioDTO> dto_list = new ArrayList<OptionPutCallRatioDTO>();
		for (Object entity : entity_list)
		{
			OptionPutCallRatioDTO dto = new OptionPutCallRatioDTO();
			BeanUtils.copyProperties(entity, dto);
			dto_list.add(dto);
		}
		return dto_list;
	}

	public void update(OptionPutCallRatioDTO dto)
	{
		FinanceServiceDelegator.update_table(CmnDef.FinanceMethod.FinanceMethod_OptionPutCallRatio, dto.getDatasetFolderpath()/*, new MySQLDAO()*/);
	}

	public void delete()
	{
		FinanceServiceDelegator.delete_table(CmnDef.FinanceMethod.FinanceMethod_OptionPutCallRatio/*, new MySQLDAO()*/);
	}
}
