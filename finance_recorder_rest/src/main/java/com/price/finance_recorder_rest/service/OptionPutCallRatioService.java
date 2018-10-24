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
		FinanceServiceProxy.csv2sql(CmnDef.FinanceMethod.FinanceMethod_OptionPutCallRatio, dto.getDatasetFolderpath(), new MySQLDAO());
	}

	public List<OptionPutCallRatioDTO> read(int start, int limit)
	{
		List<?> entity_list = FinanceServiceProxy.from_sql(CmnDef.FinanceMethod.FinanceMethod_OptionPutCallRatio, start, limit, new MySQLDAO());
		List<OptionPutCallRatioDTO> dto_list = new ArrayList<OptionPutCallRatioDTO>();
		for (Object entity : entity_list)
		{
			OptionPutCallRatioDTO dto = new OptionPutCallRatioDTO();
			BeanUtils.copyProperties(entity, dto);
			dto_list.add(dto);
		}
		return dto_list;
	}
}
