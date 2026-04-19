package com.mycompany.smart.campus.mapper;

import com.mycompany.smart.campus.model.ErrorResponse;
import com.mycompany.smart.campus.exception.RoomNotEmptyException;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {
    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(RoomNotEmptyException exception) {
        // Convert the domain rule violation into a clean 409 response.
        ErrorResponse body = new ErrorResponse(
                System.currentTimeMillis(),
                Response.Status.CONFLICT.getStatusCode(),
                "Conflict",
                exception.getMessage(),
                uriInfo == null ? null : uriInfo.getPath()
        );

        return Response.status(Response.Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}
