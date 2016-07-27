package com.price.finance_recorder;

public interface FinanceRecorderDataHandlerInf 
{
	public short read_from_csv(FinanceRecorderCSVHandlerMap csv_data_map);
	public short write_into_sql(final FinanceRecorderCSVHandlerMap csv_data_map);
	public short transfrom_csv_to_sql();
}
