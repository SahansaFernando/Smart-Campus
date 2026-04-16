package com.mycompany.smart.campus.app;

import javax.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("/api/v1")
public class ApiApplication extends ResourceConfig {
    public ApiApplication() {
        // Scan the resource, mapper, and filter packages.
        packages(
                "com.mycompany.smart.campus.resource",
                "com.mycompany.smart.campus.mapper",
                "com.mycompany.smart.campus.filter"
        );
    }
}
