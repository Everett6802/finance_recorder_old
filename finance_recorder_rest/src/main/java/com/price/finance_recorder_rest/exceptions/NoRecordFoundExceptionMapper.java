package com.price.finance_recorder_rest.exceptions;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import com.price.finance_recorder_rest.common.CmnDef;
import com.price.finance_recorder_rest.common.ErrorMessage;

public class NoRecordFoundExceptionMapper implements ExceptionMapper<NoRecordFoundException> 
{

	@Override
	public Response toResponse(NoRecordFoundException exception)
	{
		ErrorMessage errorMessage = new ErrorMessage(exception.getMessage(), ExceptionType.MISSING_REQUIRED_FIELD.name(), CmnDef.URL_REF);

		return Response.status(Response.Status.BAD_REQUEST).entity(errorMessage).build();
	}
}
