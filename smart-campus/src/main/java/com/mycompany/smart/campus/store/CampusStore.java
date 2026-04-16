package com.mycompany.smart.campus.store;

import com.mycompany.smart.campus.model.Room;
import com.mycompany.smart.campus.model.Sensor;
import com.mycompany.smart.campus.model.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class CampusStore {
    // Shared in-memory state for the whole API.
    public static final Map<String, Room> ROOMS = new ConcurrentHashMap<>();
    public static final Map<String, Sensor> SENSORS = new ConcurrentHashMap<>();
    public static final Map<String, List<SensorReading>> READINGS = new ConcurrentHashMap<>();

    private CampusStore() {
        // Utility class
    }

    public static List<SensorReading> readingsFor(String sensorId) {
        // Create the reading list on demand for each sensor.
        return READINGS.computeIfAbsent(sensorId, id -> new ArrayList<>());
    }
}
