package com.price.stock_recorder;

import java.io.*;


public abstract class StockReaderBase implements StockRecorderCmnDef.StockReaderInf
{
	protected static final String FILE_FOLDER_NAME = "data";

	protected String format_data_to_string(String[] field_array)
	{
		String field_string = null;
		for (String field : field_array)
		{
			if (field_string == null)
				field_string = field;
			else
				field_string += (StockRecorderCmnDef.DATA_SPLIT + field);
		}

		return field_string;
	}
}
