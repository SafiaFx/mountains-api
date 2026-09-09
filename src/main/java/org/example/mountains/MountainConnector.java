package org.example.mountains;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Java HTTP client for the mountain API. HTTP errors retain their status and raw body;
 * an empty Optional means a transport or response-parsing failure.
 */
public class MountainConnector {
    private static final TypeReference<List<Mountain>> MOUNTAIN_LIST_TYPE = new TypeReference<>() {};
    private final String serviceUri;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MountainConnector(final String serviceUri) {
        URI base = URI.create(serviceUri);
        if (!("http".equalsIgnoreCase(base.getScheme()) || "https".equalsIgnoreCase(base.getScheme()))
                || base.getHost() == null || base.getRawQuery() != null || base.getRawFragment() != null) {
            throw new IllegalArgumentException("Use an HTTP(S) base URI without a query or fragment.");
        }
        this.serviceUri = serviceUri.endsWith("/") ? serviceUri : serviceUri + "/";
    }

    public Optional<Response> addMountains(final List<Mountain> mountains) {
        return send("POST", "mountains", mountains);
    }

    public Optional<Response> getAll() {
        return send("GET", "mountains", null);
    }

    public Optional<Response> getByCountry(final String country) {
        return send("GET", "mountains/country/" + urlEncode(country), null);
    }

    public Optional<Response> getByCountryAndRange(final String country, final String range) {
        return send("GET", "mountains/country/" + urlEncode(country) + "/range/" + urlEncode(range), null);
    }

    public Optional<Response> getByHemisphere(final boolean isNorthern) {
        return send("GET", "mountains?isNorthern=" + isNorthern, null);
    }

    public Optional<Response> getByCountryAltitude(final String country, final int altitude) {
        return send("GET", "mountains/country/" + urlEncode(country) + "?minAltitude=" + altitude, null);
    }

    public Optional<Response> getByName(final String country, final String range, final String name) {
        return send("GET", "mountains/country/" + urlEncode(country) + "/range/" + urlEncode(range)
                + "/name/" + urlEncode(name), null);
    }

    public Optional<Response> getById(final int id) {
        return send("GET", "mountains/" + id, null);
    }

    public Optional<Response> updateMountain(final int id, final Mountain mountain) {
        return send("PUT", "mountains/" + id, mountain);
    }

    public Optional<Response> deleteMountain(final int id) {
        return send("DELETE", "mountains/" + id, null);
    }

    private Optional<Response> send(final String method, final String path, final Object body) {
        try {
            HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(serviceUri + path))
                    .timeout(Duration.ofSeconds(10)).header("Accept", "application/json");
            if (body == null) {
                request.method(method, HttpRequest.BodyPublishers.noBody());
            } else {
                request.header("Content-Type", "application/json")
                        .method(method, HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)));
            }
            HttpResponse<String> response = client.send(request.build(), HttpResponse.BodyHandlers.ofString());
            // Error bodies describe a failure, not a Mountain. Keep them available through Response.
            List<Mountain> mountains = response.statusCode() >= 400 ? List.of() : parseResponse(response.body());
            return Optional.of(new Response(mountains, response));
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (IOException | IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private List<Mountain> parseResponse(final String body) throws IOException {
        if (body == null || body.isBlank()) {
            return List.of();
        }
        String json = body.strip();
        return json.startsWith("[")
                ? objectMapper.readValue(json, MOUNTAIN_LIST_TYPE)
                : List.of(objectMapper.readValue(json, Mountain.class));
    }

    private String urlEncode(final String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
