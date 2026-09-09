# Mountains Collector

A REST API and Java HTTP client for collecting mountain data. Add, browse, filter, update and delete mountains by name, altitude, range, country and hemisphere.

Originally developed for **CS253 – Web Service Development coursework**, this repository builds on the completed coursework with assisted improvements to the client, JSON mapping, automated tests and documentation. The imported coursework is preserved in the first Git commit, and standalone submission reference files are kept in `coursework/`.

## Technologies used

| Technology | Role in this project |
| --- | --- |
| **Java 17+** | Implements the server, mountain model, validation, storage and client. |
| **Spring Boot 3.5** | Starts and configures the application, including its embedded Tomcat web server. |
| **Spring Web / Spring MVC** | Maps HTTP routes to controller methods and converts requests and responses. |
| **Jackson** | Converts mountain objects to and from JSON, including the `isNorthern` hemisphere field. |
| **Java HttpClient** | Sends requests from `MountainConnector` to the REST API. The client handles URI encoding, timeouts and response parsing. |
| **Java collections and read/write locks** | Store mountains in memory and protect shared data during concurrent requests. |
| **Maven and Maven Wrapper** | Download dependencies, compile the application, run tests and package an executable JAR. A separate Maven installation is not required. |
| **JUnit 5 and Spring Boot Test** | Test service behaviour and exercise the real HTTP server and Java client together. |
| **Spring Boot Actuator** | Provides a health endpoint at `/actuator/health`. |
| **GitHub Actions** | Builds and tests the project on Java 17, 21 and 25 after pushes and pull requests. |

A request flows from the HTTP controller to `MountainService`, which validates and accesses the collection. Jackson handles JSON conversion. `MountainConnector` is a separate client for calling those endpoints from Java.

**Storage is in memory:** there is no SQL database. The collection starts empty and resets whenever the application restarts.

## Run locally

Install a JDK (Java 17 or later). Check it with `java -version`. The first build also needs internet access to download Maven and dependencies.

From the repository directory on macOS or Linux:

```bash
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

The API listens at <http://localhost:8080/mountains>. An empty collection returns `404` with `[]`, preserving the original coursework behaviour. Check that the server is ready at <http://localhost:8080/actuator/health>.

If port 8080 is in use, set the `PORT` environment variable before starting, for example:

```bash
PORT=8081 ./mvnw spring-boot:run
```

## Try it

Keep the server running and open another terminal in the repository directory. These examples use a fresh application with an empty collection.

### Add the sample catalogue

```bash
curl -i -X POST http://localhost:8080/mountains \
  -H 'Content-Type: application/json' \
  --data-binary @examples/mountains.json
```

A successful batch returns `201 Created` with no response body. The sample contains Yr Wyddfa, Makalu and Aconcagua. Adding the same sample twice returns `400`; it does not duplicate the records.

### Browse and filter

```bash
curl http://localhost:8080/mountains
curl http://localhost:8080/mountains/1
curl http://localhost:8080/mountains/country/Nepal
curl 'http://localhost:8080/mountains?isNorthern=false'
curl 'http://localhost:8080/mountains/country/Nepal?minAltitude=8400'
```

A single mountain looks like this:

```json
{
  "id": 1,
  "name": "Yr Wyddfa",
  "altitude": 1085,
  "range": "Eryri",
  "country": "Cymru",
  "isNorthern": true
}
```

### Update and delete

```bash
curl -i -X PUT http://localhost:8080/mountains/1 \
  -H 'Content-Type: application/json' \
  -d '{"name":"Yr Wyddfa","altitude":1085,"range":"Eryri","country":"Wales","isNorthern":true}'

curl -i -X DELETE http://localhost:8080/mountains/1
```

Both successful operations return `204 No Content`. IDs are assigned by the server; read the catalogue to find the ID when using an existing session.

## API reference

| Method | Path | Purpose |
| --- | --- | --- |
| POST | `/mountains` | Add a JSON array of mountains |
| GET | `/mountains` | List mountains; optionally filter by `isNorthern` and `minAltitude` |
| GET | `/mountains/{id}` | Retrieve one mountain object |
| GET | `/mountains/country/{country}` | Filter by country; also accepts `isNorthern` and `minAltitude` |
| GET | `/mountains/country/{country}/range/{range}` | Filter by country and range; also accepts `isNorthern` and `minAltitude` |
| GET | `/mountains/country/{country}/range/{range}/name/{name}` | Find a named mountain within a country and range |
| PUT | `/mountains/{id}` | Replace a mountain with a JSON object |
| DELETE | `/mountains/{id}` | Remove a mountain |
| GET | `/actuator/health` | Check application health |

Country, range and name matches are case-sensitive. URL-encode spaces and special characters in path segments; the Java client does this automatically. `minAltitude` means **strictly greater than** the supplied altitude, in metres. `isNorthern=true` selects the northern hemisphere; `false` selects the southern hemisphere.

Lists are sorted by ID. IDs are assigned on creation and retained on update. The server ignores IDs supplied in request bodies.

### Validation and status codes

Names, ranges and countries must contain non-whitespace text, and altitude must be greater than zero. A mountain's identity is its name, range and country; two records with the same identity are rejected even if their altitude or hemisphere differs. Batches are accepted or rejected as a whole, so an invalid entry cannot leave a partial import.

| Status | Meaning |
| --- | --- |
| `200` | Successful read |
| `201` | Batch created |
| `204` | Update or deletion completed |
| `400` | Invalid data, duplicate identity, malformed JSON or incorrectly typed parameters |
| `404` | Missing ID or no matching mountains |
| `405` | Unsupported HTTP method |

The original coursework contract is retained: an empty list query returns `404` and `[]`, while a missing individual mountain returns `404` without a body. Successful writes and service-validation failures also have empty bodies. Framework-generated errors may have JSON error bodies. Client code should inspect the HTTP status rather than expect one universal error-body format.

## Use the Java client

From Java code with this project's classes on the classpath:

```java
MountainConnector client = new MountainConnector("http://localhost:8080");
client.getByCountry("Nepal").ifPresent(result -> {
    System.out.println(result.getResponse().statusCode());
    result.getMountains().forEach(System.out::println);
});
```

The base URI accepts a trailing slash or no trailing slash. The client uses a 5-second connection timeout and a 10-second request timeout. HTTP error responses retain their status and raw body in `Response`, with an empty mountain list. `Optional.empty()` indicates a transport, interruption or parsing failure. Interrupted requests restore the thread's interrupted status.

## Build and test

```bash
./mvnw --batch-mode verify
java -jar target/mountains-collector-0.0.1-SNAPSHOT.jar
```

The tests cover CRUD operations, filters, JSON round trips, URL encoding, invalid requests, duplicate batches, defensive copies, concurrent writes and HTTP-error preservation in the client. Integration tests start an isolated server on a randomly assigned port; no separately running server is needed.

GitHub Actions runs the same build on Java 17, 21 and 25. Generated JARs, build reports, IDE files and local dependency caches are excluded from Git.

## Project structure

```text
src/main/java/org/example/mountains/
  Mountain.java              Mountain data and JSON mapping
  MountainController.java    HTTP endpoints and status codes
  MountainService.java       In-memory storage and validation
  MountainConnector.java     Java HTTP client
  MountainsApplication.java  Spring Boot entry point
  DemoTests.java             Original coursework demonstration checks
  Response.java              Parsed data and raw HTTP response wrapper
  SharedResources.java       Original coursework demo data and helpers
src/main/resources/          Server configuration
src/test/                    Automated tests
examples/                    Sample JSON catalogue
coursework/                  Original standalone submission reference files
```

## Scope

This is a coursework and portfolio project. It has no accounts, authentication, persistent storage, pagination or user interface. Anyone who can reach a running server can modify its collection. Publishing the source on GitHub does not deploy the API as a public service.

Possible extensions include persistent storage, pagination, authenticated writes and a browser interface. The current focus is a small, understandable API with a working Java client and automated checks.

## Coursework credit

Developed for **CS253 – Web Service Development**. The original demonstration tests and supporting helpers came from the course materials and are credited in their source comments. Later portfolio improvements are recorded in separate commits so they remain distinguishable from the submitted coursework.
