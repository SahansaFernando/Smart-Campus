package com.mycompany.smart.campus.resource;

import com.mycompany.smart.campus.exception.RoomNotEmptyException;
import com.mycompany.smart.campus.model.Room;
import com.mycompany.smart.campus.store.CampusStore;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.*;

@Path("rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    // GET /api/v1/rooms
    @GET
    public Collection<Room> getRooms() {
        return CampusStore.ROOMS.values();
    }

    // POST /api/v1/rooms
    // Validates the payload and stores the room in memory.
    @POST
    public Response addRoom(Room room) {
        if (room == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Request body is missing")
                    .build();
        }

        if (room.getId() == null || room.getId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Room id is required")
                    .build();
        }

        if (CampusStore.ROOMS.containsKey(room.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Room with id " + room.getId() + " already exists")
                    .build();
        }

        if (room.getSensorIds() == null) {
            room.setSensorIds(new ArrayList<>());
        }

        CampusStore.ROOMS.put(room.getId(), room);

        return Response.status(Response.Status.CREATED)
                .entity(room)
                .build();
    }

    // GET /api/v1/rooms/{id}
    @GET
    @Path("/{id}")
    public Response getRoom(@PathParam("id") String id) {
        Room room = CampusStore.ROOMS.get(id);

        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Room not found")
                    .build();
        }

        return Response.ok(room).build();
    }

    // PUT /api/v1/rooms/{id}
    // Keeps the same room id and preserves the sensor links.
    @PUT
    @Path("/{id}")
    public Response updateRoom(@PathParam("id") String id, Room updatedRoom) {
        Room existingRoom = CampusStore.ROOMS.get(id);
        if (existingRoom == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Room not found")
                    .build();
        }

        if (updatedRoom == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Request body is missing")
                    .build();
        }

        // Preserve the sensor links already assigned to this room.
        updatedRoom.setId(id);
        updatedRoom.setSensorIds(existingRoom.getSensorIds());
        CampusStore.ROOMS.put(id, updatedRoom);

        return Response.ok(updatedRoom).build();
    }

    // DELETE /api/v1/rooms/{id}
    // Prevent deletion while sensors still reference this room.
    @DELETE
    @Path("/{id}")
    public Response deleteRoom(@PathParam("id") String id) {
        Room room = CampusStore.ROOMS.get(id);

        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Room not found")
                    .build();
        }

        boolean hasLinkedSensors = CampusStore.SENSORS.values().stream()
                .anyMatch(sensor -> id.equals(sensor.getRoomId()));

        if (hasLinkedSensors) {
            throw new RoomNotEmptyException("Room " + id + " still has assigned sensors");
        }

        CampusStore.ROOMS.remove(id);
        return Response.noContent().build();
    }
}
