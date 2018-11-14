package com.price.finance_recorder_rest.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.price.finance_recorder_rest.common.CmnDef;
import com.price.finance_recorder_rest.persistence.MySQLDAO;

public class StockExchangeAndVolumeService
{
	public void create(String dataset_folderpath)
	{
//		short ret = CmnDef.RET_SUCCESS;
//// Read data from the dataset
////		String filepath = String.format("%s/%s.csv", CmnFunc.get_dataset_market_data_path(), CmnDef.FINANCE_DATA_NAME_LIST[method_index]);
//		String filepath = CmnFunc.get_dataset_market_csv_filepath(CmnDef.FinanceMethod.FinanceMethod_StockExchangeAndVolume.value(), dto.getDatasetFolderpath());
//		LinkedList<String> data_line_list = new LinkedList<String>();
//		ret = CmnFunc.read_file_lines(filepath, data_line_list);
//		if (CmnDef.CheckFailure(ret))
//		{
//			String errmsg = String.format("Fail to read market[%d] dataset", CmnDef.FinanceMethod.FinanceMethod_StockExchangeAndVolume.value());
//			throw new FinanceRecorderResourceNotFoundException(errmsg);
//		}
//		MySQLDAO dao = new MySQLDAO();
//		dao.create(CmnDef.FinanceMethod.FinanceMethod_StockExchangeAndVolume, data_line_list);
		FinanceServiceAdapter.create_table(CmnDef.FinanceMethod.FinanceMethod_StockExchangeAndVolume, dataset_folderpath/*, new MySQLDAO()*/);
	}

	public List<StockExchangeAndVolumeDTO> read(int start, int limit)
	{
		List<?> entity_list = FinanceServiceAdapter.read_table(CmnDef.FinanceMethod.FinanceMethod_StockExchangeAndVolume, start, limit/*, new MySQLDAO()*/);
		List<StockExchangeAndVolumeDTO> dto_list = new ArrayList<StockExchangeAndVolumeDTO>();
		for (Object entity : entity_list)
		{
			StockExchangeAndVolumeDTO dto = new StockExchangeAndVolumeDTO();
			BeanUtils.copyProperties(entity, dto);
			dto_list.add(dto);
		}
		return dto_list;
	}

	public void update(String dataset_folderpath)
	{
		FinanceServiceAdapter.update_table(CmnDef.FinanceMethod.FinanceMethod_StockExchangeAndVolume, dataset_folderpath/*, new MySQLDAO()*/);
	}

	public void delete()
	{
		FinanceServiceAdapter.delete_table(CmnDef.FinanceMethod.FinanceMethod_StockExchangeAndVolume/*, new MySQLDAO()*/);
	}
}
