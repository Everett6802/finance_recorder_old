package com.price.finance_recorder_rest.exceptions;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import com.price.finance_recorder_rest.common.CmnDef;
import com.price.finance_recorder_rest.common.ErrorMessage;

public class ExecuteSQLCommandExceptionMapper implements ExceptionMapper<ExecuteSQLCommandException> 
{
	@Override
	public Response toResponse(ExecuteSQLCommandException exception)
	{
		ErrorMessage errorMessage = new ErrorMessage(exception.getMessage(), ExceptionType.SQL_EXECUTION_FAILED.name(), CmnDef.URL_REF);

		return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
	}
}
