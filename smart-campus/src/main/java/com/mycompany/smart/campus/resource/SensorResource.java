package com.mycompany.smart.campus.resource;

import com.mycompany.smart.campus.exception.LinkedResourceNotFoundException;
import com.mycompany.smart.campus.model.Room;
import com.mycompany.smart.campus.model.Sensor;
import com.mycompany.smart.campus.store.CampusStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.stream.Collectors;

@Path("sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    // GET /api/v1/sensors
    // Optional ?type=CO2 filter is handled here.
    @GET
    public Collection<Sensor> getSensors(@QueryParam("type") String type) {
        if (type == null || type.isBlank()) {
            return CampusStore.SENSORS.values();
        }

        return CampusStore.SENSORS.values().stream()
                .filter(sensor -> type.equalsIgnoreCase(sensor.getType()))
                .collect(Collectors.toList());
    }

    // POST /api/v1/sensors
    // Ensures the linked room exists before creating the sensor.
    @POST
    public Response addSensor(Sensor sensor) {
        if (sensor == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Request body is missing")
                    .build();
        }

        if (sensor.getId() == null || sensor.getId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Sensor id is required")
                    .build();
        }

        if (CampusStore.SENSORS.containsKey(sensor.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Sensor with id " + sensor.getId() + " already exists")
                    .build();
        }

        if (sensor.getRoomId() == null || sensor.getRoomId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("roomId is required")
                    .build();
        }

        Room room = CampusStore.ROOMS.get(sensor.getRoomId());
        if (room == null) {
            // The JSON is valid, but the referenced room does not exist.
            throw new LinkedResourceNotFoundException("Room " + sensor.getRoomId() + " does not exist");
        }

        if (room.getSensorIds() == null) {
            room.setSensorIds(new java.util.ArrayList<>());
        }

        if (sensor.getStatus() == null || sensor.getStatus().isBlank()) {
            sensor.setStatus("ACTIVE");
        }

        CampusStore.SENSORS.put(sensor.getId(), sensor);
        room.getSensorIds().add(sensor.getId());

        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }

    // GET /api/v1/sensors/{sensorId}
    @GET
    @Path("/{sensorId}")
    public Response getSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = CampusStore.SENSORS.get(sensorId);

        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Sensor not found")
                    .build();
        }

        return Response.ok(sensor).build();
    }

    // DELETE /api/v1/sensors/{sensorId}
    // Also removes the sensor id from the owning room.
    @DELETE
    @Path("/{sensorId}")
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = CampusStore.SENSORS.remove(sensorId);

        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Sensor not found")
                    .build();
        }

        Room room = CampusStore.ROOMS.get(sensor.getRoomId());
        if (room != null && room.getSensorIds() != null) {
            room.getSensorIds().remove(sensorId);
        }

        CampusStore.READINGS.remove(sensorId);

        return Response.noContent().build();
    }

    // Sub-resource locator for nested reading operations.
    @Path("/{sensorId}/readings")
    public SensorReadingResource readings(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}
