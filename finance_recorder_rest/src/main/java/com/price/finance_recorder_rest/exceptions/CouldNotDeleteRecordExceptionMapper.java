package com.price.finance_recorder_rest.exceptions;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import com.price.finance_recorder_rest.common.CmnDef;
import com.price.finance_recorder_rest.common.ErrorMessage;

public class CouldNotDeleteRecordExceptionMapper implements ExceptionMapper<CouldNotDeleteRecordException> 
{
	@Override
	public Response toResponse(CouldNotDeleteRecordException exception)
	{
		ErrorMessage errorMessage = new ErrorMessage(exception.getMessage(), ExceptionType.COULD_NOT_DELETE_RECORD.name(), CmnDef.URL_REF);

		return Response.status(Response.Status.BAD_REQUEST).entity(errorMessage).build();
	}
}
