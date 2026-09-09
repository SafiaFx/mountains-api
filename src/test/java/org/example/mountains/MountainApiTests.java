package org.example.mountains;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MountainApiTests {
    @LocalServerPort private int port;
    @Autowired private MountainService service;
    @Autowired private ObjectMapper mapper;
    private MountainConnector connector;
    private final HttpClient http = HttpClient.newHttpClient();

    @BeforeEach
    void clearCollection() {
        service.getAll().forEach(m -> service.deleteMountain(m.getId()));
        // Deliberately omit the trailing slash to exercise URI normalization.
        connector = new MountainConnector("http://127.0.0.1:" + port);
    }

    private HttpResponse<String> raw(String method, String path, String body) throws Exception {
        return http.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
                .header("Content-Type", "application/json")
                .method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body))
                .build(), HttpResponse.BodyHandlers.ofString());
    }

    private int add(String name, int altitude, String range, String country, boolean north) {
        Response response = connector.addMountains(List.of(new Mountain(name, altitude, range, country, north))).orElseThrow();
        assertEquals(201, response.getResponse().statusCode());
        assertTrue(response.getResponse().body().isEmpty());
        return connector.getByName(country, range, name).orElseThrow().getMountains().get(0).getId();
    }

    @Test
    void supportsCreateReadUpdateAndDelete() {
        int id = add("Snowdon", 1085, "Snowdonia", "Wales", true);
        Response detail = connector.getById(id).orElseThrow();
        assertEquals(200, detail.getResponse().statusCode());
        assertEquals("Snowdon", detail.getMountains().get(0).getName());
        assertTrue(detail.getResponse().headers().firstValue("Content-Type").orElseThrow().contains("application/json"));
        Mountain replacement = new Mountain("Yr Wyddfa", 1085, "Eryri", "Cymru", true);
        assertEquals(204, connector.updateMountain(id, replacement).orElseThrow().getResponse().statusCode());
        assertEquals("Yr Wyddfa", connector.getById(id).orElseThrow().getMountains().get(0).getName());
        assertEquals(204, connector.deleteMountain(id).orElseThrow().getResponse().statusCode());
        assertEquals(404, connector.getById(id).orElseThrow().getResponse().statusCode());
        assertEquals(404, connector.deleteMountain(id).orElseThrow().getResponse().statusCode());
    }

    @Test
    void filtersCountryRangeNameHemisphereAndStrictAltitude() {
        add("Makalu", 8485, "Himalayas", "Nepal", true);
        add("Annapurna", 8091, "Himalayas", "Nepal", true);
        add("Aconcagua", 6961, "Andes", "Argentina", false);
        assertEquals(3, connector.getAll().orElseThrow().getMountains().size());
        assertEquals(2, connector.getByCountry("Nepal").orElseThrow().getMountains().size());
        assertEquals(2, connector.getByCountryAndRange("Nepal", "Himalayas").orElseThrow().getMountains().size());
        assertEquals("Makalu", connector.getByCountryAltitude("Nepal", 8091).orElseThrow().getMountains().get(0).getName());
        assertEquals(1, connector.getByHemisphere(false).orElseThrow().getMountains().size());
        assertEquals(2, connector.getByHemisphere(true).orElseThrow().getMountains().size());
    }

    @Test
    void encodesSpacesAccentsAndPlusSignsInPaths() {
        int id = add("Pic + Étoile", 1200, "Example Range", "New Zealand", false);
        assertEquals(id, connector.getByName("New Zealand", "Example Range", "Pic + Étoile")
                .orElseThrow().getMountains().get(0).getId());
    }

    @Test
    void preservesCoursework404ForAnEmptyCollection() {
        Response response = connector.getAll().orElseThrow();
        assertEquals(404, response.getResponse().statusCode());
        assertEquals("[]", response.getResponse().body());
        assertTrue(response.getMountains().isEmpty());
    }

    @Test
    void rejectsDuplicateAndInvalidBatchesWithoutPartialWrites() {
        add("One", 1000, "Range", "Wales", true);
        assertEquals(400, connector.addMountains(List.of(new Mountain("One", 2000, "Range", "Wales", false)))
                .orElseThrow().getResponse().statusCode());
        assertEquals(400, connector.addMountains(List.of(new Mountain("Valid", 500, "Range", "Wales", true),
                new Mountain("Invalid", -1, "Range", "Wales", true))).orElseThrow().getResponse().statusCode());
        assertEquals(1, connector.getAll().orElseThrow().getMountains().size());
    }

    @Test
    void badJsonAndInvalidQueryValuesReturn400() throws Exception {
        assertEquals(400, raw("POST", "/mountains", "{broken").statusCode());
        assertEquals(400, raw("POST", "/mountains", "[null]").statusCode());
        assertEquals(400, raw("GET", "/mountains?minAltitude=high", null).statusCode());
        assertEquals(400, raw("GET", "/mountains?isNorthern=invalid", null).statusCode());
        assertEquals(400, raw("GET", "/mountains/not-an-id", null).statusCode());
    }

    @Test
    void unknownRouteAndUnsupportedMethodHaveExpectedStatus() throws Exception {
        assertEquals(404, raw("GET", "/unknown", null).statusCode());
        var response = raw("PATCH", "/mountains/1", "{}");
        assertEquals(405, response.statusCode());
        assertTrue(response.headers().firstValue("Allow").orElseThrow().contains("PUT"));
    }

    @Test
    void jsonRoundTripKeepsHemisphereAndAcceptsLegacyAlias() throws Exception {
        String body = "[{\"name\":\"North\",\"altitude\":1000,\"range\":\"Range\",\"country\":\"Wales\",\"isNorthern\":true}]";
        assertEquals(201, raw("POST", "/mountains", body).statusCode());
        var json = mapper.readTree(raw("GET", "/mountains", null).body()).get(0);
        assertTrue(json.get("isNorthern").booleanValue());
        assertFalse(json.has("northern"));
        Mountain legacy = mapper.readValue("{\"northern\":true}", Mountain.class);
        assertTrue(legacy.getIsNorthern());
    }

    @Test
    void healthEndpointIsAvailable() throws Exception {
        var response = raw("GET", "/actuator/health", null);
        assertEquals(200, response.statusCode());
        assertEquals("UP", mapper.readTree(response.body()).get("status").textValue());
    }

    @Test
    void missingUpdateReturns404() {
        assertEquals(404, connector.updateMountain(Integer.MAX_VALUE, new Mountain("New", 10, "Range", "Wales", true))
                .orElseThrow().getResponse().statusCode());
    }
}
