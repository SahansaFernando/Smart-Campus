package com.mycompany.smart.campus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.LinkedHashMap;
import java.util.Map;

@Path("")
@Produces(MediaType.APPLICATION_JSON)
public class RootResource {

    @GET
    public Response test() {
        // Return basic discovery metadata and top-level links.
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("api", "Smart Campus API");
        info.put("version", "v1");
        info.put("contact", "Hamed Hamzeh / Westminster coursework");

        Map<String, String> links = new LinkedHashMap<>();
        links.put("rooms", "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");

        info.put("links", links);

        return Response.ok(info).build();
    }
}
