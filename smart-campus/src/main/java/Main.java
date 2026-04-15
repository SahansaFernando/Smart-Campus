package com.mycompany.smart.campus;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;

import java.net.URI;

public class Main {
    public static void main(String[] args) {

        URI uri = URI.create("http://localhost:8080/");

        ResourceConfig config = new ResourceConfig()
                .packages("com.mycompany.smart.campus");

        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(uri, config);

        System.out.println("Server running at " + uri);
    }
}