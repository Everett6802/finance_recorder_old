package com.price.finance_recorder_stock;

import java.util.ArrayList;
import com.price.finance_recorder_cmn.FinanceRecorderCmnClass.QuerySet;


public class FinanceRecorderStockQuerySet extends QuerySet
{
	protected FinanceRecorderCompanyGroupSet company_group_set;

	public FinanceRecorderStockQuerySet()
	{
		company_group_set = new FinanceRecorderCompanyGroupSet();
	}

	public short add_company_list(int company_group_number, final ArrayList<String> company_code_number_in_group_array)
	{
		return company_group_set.add_company_in_group_list(company_group_number, company_code_number_in_group_array);
	}

	public short add_company(int company_group_number, String company_code_number)
	{
		return company_group_set.add_company(company_group_number, company_code_number);
	}

	public short add_company_group(int company_group_number)
	{
		return company_group_set.add_company_group(company_group_number);
	}

	public final FinanceRecorderCompanyGroupSet get_company_group_set()
	{
		return company_group_set;
	}
}
