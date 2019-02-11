package com.price.finance_recorder_rest.persistence;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
//import org.hibernate.cfg.DefaultNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class StockNamingStrategy extends PhysicalNamingStrategyStandardImpl//DefaultNamingStrategy 
{
	private static final long serialVersionUID = 1L;
	public static final StockNamingStrategy INSTANCE = new StockNamingStrategy();

	private String company_number;
	
//	public StockNamingStrategy(String company_number)
	public void set_company_number(String company_number)
	{
		this.company_number = company_number;
	}
	
	@Override
	public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context)
//	public String tableName(String tableName) 
	{
		return new Identifier(name.getText() + company_number, name.isQuoted());
//	    return tableName + company_number;
	}
}
