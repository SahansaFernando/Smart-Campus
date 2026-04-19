package com.mycompany.smart.campus.mapper;

import com.mycompany.smart.campus.exception.LinkedResourceNotFoundException;
import com.mycompany.smart.campus.model.ErrorResponse;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {
    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {
        // 422 fits a valid JSON payload that references a missing room.
        int status = 422;
        ErrorResponse body = new ErrorResponse(
                System.currentTimeMillis(),
                status,
                "Unprocessable Entity",
                exception.getMessage(),
                uriInfo == null ? null : uriInfo.getPath()
        );

        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}
