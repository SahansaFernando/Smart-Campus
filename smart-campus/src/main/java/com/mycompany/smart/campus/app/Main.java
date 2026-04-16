package com.mycompany.smart.campus.app;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;

import java.net.URI;

public class Main {
    private static final String BASE_URI = "http://localhost:8080/api/v1/";

    public static void main(String[] args) {
        URI uri = URI.create(BASE_URI);

        // Start the embedded Grizzly server with the versioned JAX-RS app.
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(uri, new ApiApplication());

        System.out.println("Server running at " + uri);

        // Make sure the server shuts down cleanly when the JVM exits.
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdownNow));
    }
}
