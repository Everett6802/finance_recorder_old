package com.price.finance_recorder_rest.exceptions;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.price.finance_recorder_rest.common.CmnDef;
import com.price.finance_recorder_rest.common.ErrorMessage;

@Provider
public class ResourceNotFoundExceptionMapper implements ExceptionMapper<ResourceNotFoundException>
{
	@Override
	public Response toResponse(ResourceNotFoundException exception)
	{
		ErrorMessage errorMessage = new ErrorMessage(exception.getMessage(), ExceptionType.RESOURCE_NOT_FOUND.name(), CmnDef.URL_REF);

		return Response.status(Response.Status.NOT_ACCEPTABLE).entity(errorMessage).build();
	}
}
