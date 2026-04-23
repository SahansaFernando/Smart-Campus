# Smart-Campus

Part 1: Service Architecture & Setup 

Question: In your report, explain the default lifecycle of a JAX-RS Resource class. Is a
new instance instantiated for every incoming request, or does the runtime treat it as a
singleton? Elaborate on how this architectural decision impacts the way you manage and
synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions.

JAX-RS resource lifecycle is implementation-dependent, so resource classes should not be relied upon as long-lived state holders. In practice, resource classes should be treated as request handlers rather than containers for persistent data. Therefore, application state such as rooms, sensors, and readings should be stored in a shared in-memory structure such as a dedicated CampusStore class rather than in instance fields. This approach ensures that data remains available across requests and prevents accidental data loss when the runtime creates or manages resource instances differently. It also raises concurrency concerns, since multiple requests may access the same collections simultaneously. For that reason, thread-safe structures such as ConcurrentHashMap are appropriate for shared maps, while mutable lists should be handled carefully to avoid race conditions during updates.

Part 2: Room Management

Question: When returning a list of rooms, what are the implications of returning only
IDs versus returning the full room objects? Consider network bandwidth and client side
processing.

Returning only room IDs reduces the size of the response payload and uses less network bandwidth, but it gives the client limited information and often requires additional requests to obtain complete details. Returning full room objects increases the payload size, but it provides all relevant information in a single response, including the room name, capacity, and linked sensor IDs. This reduces client-side processing and simplifies API consumption. In this coursework, returning full objects is generally the more practical choice because it is easier to test, easier to demonstrate, and more useful for the client. ID-only responses may be more efficient in larger systems, but they trade bandwidth savings for increased request overhead.

Question: Is the DELETE operation idempotent in your implementation? Provide a detailed
justification by describing what happens if a client mistakenly sends the exact same DELETE
request for a room multiple times.

Yes, the DELETE operation is idempotent because repeating the same request does not change the final state of the system after the resource has already been removed. When a room is deleted successfully, it is removed from the in-memory store. If the client sends the same DELETE request again, the room no longer exists, so the API returns 404 Not Found. Although the response status changes on repeated attempts, the overall system state remains unchanged after the first successful deletion. This is the defining property of idempotent behaviour and is one of the reasons DELETE is considered a safe REST operation to repeat.

Part 3: Sensor Operations & Linking

Question: We explicitly use the @Consumes (MediaType.APPLICATION_JSON) annotation on
the POST method. Explain the technical consequences if a client attempts to send data in
a different format, such as text/plain or application/xml. How does JAX-RS handle this
Mismatch?

When a POST method is annotated with @Consumes(MediaType.APPLICATION_JSON), it declares that the endpoint accepts only JSON input. If a client sends text/plain, application/xml, or another unsupported media type, JAX-RS will reject the request because the payload does not match the expected representation. In most cases, the runtime returns 415 Unsupported Media Type, and in some situations it may also fail during message-body conversion. This behaviour is important because it enforces a strict contract between client and server, ensuring that only valid JSON data is processed by the application logic. As a result, the API becomes more predictable, robust, and easier to debug.

Question: You implemented this filtering using @QueryParam. Contrast this with an alternative
design where the type is part of the URL path (e.g., /api/vl/sensors/type/CO2). Why
is the query parameter approach generally considered superior for filtering and searching
collections?

Using @QueryParam is generally superior because query parameters are intended for optional filtering and searching of collection resources. A request such as /api/v1/sensors?type=CO2 clearly indicates that the client wants a filtered view of the sensor collection while keeping the main resource path unchanged. If the filter value is placed in the path, the endpoint begins to look like a separate resource rather than a search criterion, which reduces flexibility. Query parameters are also easier to extend because additional filters can be added later without changing the resource structure. For this reason, query parameters are the more REST-friendly and maintainable choice for collection filtering.


Part 4: Deep Nesting with Sub - Resources

Question: Discuss the architectural benefits of the Sub-Resource Locator pattern. How
does delegating logic to separate classes help manage complexity in large APIs compared
to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one massive controller
Class?

The Sub-Resource Locator pattern improves architecture by separating nested logic into dedicated classes rather than placing everything inside one large controller. In this coursework, the /sensors/{sensorId}/readings path is handled by a separate SensorReadingResource class, which makes the code easier to understand and maintain. This approach supports separation of concerns, since each class is responsible for a smaller and more focused part of the API. It also improves scalability, because changes to readings do not require editing a large controller that also handles sensors and other routes. Compared with a single massive controller, the sub-resource pattern is cleaner, more modular, and more aligned with the domain structure of the application.

Part 5: Advanced Error Handling, Exception Mapping & Logging 

Question: Why is HTTP 422 often considered more semantically accurate than a standard
404 when the issue is a missing reference inside a valid JSON payload?

HTTP 422 Unprocessable Entity is more semantically accurate because the request is syntactically valid, but one of the values in the JSON body refers to a resource that does not exist. In this situation, the endpoint is present, the payload can be parsed, and the server understands the request structure, but the operation cannot be completed because the linked resource is missing. This differs from 404 Not Found, which is normally used when the endpoint or resource itself is absent. As a result, 422 communicates a validation or business-rule failure more precisely than 404, making it a better choice for this type of request error.


Question: From a cybersecurity standpoint, explain the risks associated with exposing
internal Java stack traces to external API consumers. What specific information could an
attacker gather from such a trace?

Exposing internal Java stack traces is risky because it reveals implementation details that should remain hidden from external clients. A stack trace may disclose package names, class names, method names, file paths, line numbers, and framework or library versions. It may also reveal how requests are processed internally and where failures occur. This information can help an attacker understand the structure of the application and identify possible weaknesses or exploit paths. For this reason, production APIs should use a global exception mapper to return a generic error response rather than exposing raw stack traces.

Question: Why is it advantageous to use JAX-RS filters for cross-cutting concerns like
logging, rather than manually inserting Logger.info() statements inside every single resource
method?

JAX-RS filters are advantageous because logging is a cross-cutting concern that should apply consistently across the entire API. If logging statements are placed manually inside each resource method, the code becomes repetitive, harder to maintain, and easier to forget when new endpoints are added. A filter centralises the logging logic so that every request and response is handled in one place, improving consistency and reducing duplication. It also keeps the resource classes focused on business logic rather than infrastructure concerns. In a well-structured REST API, this separation makes the codebase cleaner, easier to extend, and easier to debug.



## Sample Curl Commands

1.Discovery

curl http://localhost:8080/api/v1

2.Create a room

curl -X POST http://localhost:8080/api/v1/rooms ^
  -H "Content-Type: application/json" ^
  -d "{\"id\":\"R101\",\"name\":\"Lecture Hall 1\",\"capacity\":40,\"sensorIds\":[]}"

3.Create a sensor

curl -X POST http://localhost:8080/api/v1/sensors ^
  -H "Content-Type: application/json" ^
  -d "{\"id\":\"S1\",\"type\":\"CO2\",\"status\":\"ACTIVE\",\"currentValue\":0,\"roomId\":\"R101\"}"

4.Filter sensors by type

curl "http://localhost:8080/api/v1/sensors?type=CO2"

5.Add a sensor reading

curl -X POST http://localhost:8080/api/v1/sensors/S1/readings ^
  -H "Content-Type: application/json" ^
  -d "{\"value\":450.0}"
