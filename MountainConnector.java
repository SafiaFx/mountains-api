import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Version to submit to Autograder - outside of package
 */

/**
 * HTTP client - sends requests to the server and converts JSON responses into Java objects
 * REST API methods build a HTTP request, send it, and process the response in a try/catch
 * All public methods return an Optional, or empty Optional if parsing fails
 */
public class MountainConnector {

    private static final TypeReference<List<Mountain>> MOUNTAIN_LIST_TYPE = new TypeReference<>() {
    };

    private final String serviceUri;

    private final HttpClient client = HttpClient.newHttpClient();

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Constructor
     *
     * @param serviceUri base URI of the server
     */
    public MountainConnector(final String serviceUri) {
        this.serviceUri = serviceUri;
    }


    /**
     * Add mountains
     *
     * @param mountains list of mountains
     */
    public Optional<Response> addMountains(final List<Mountain> mountains) {

        try {
            String mountainsAsJson = objectMapper.writeValueAsString(mountains);

            HttpRequest request = HttpRequest.newBuilder().uri(new URI(serviceUri + "mountains"))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(mountainsAsJson))
                    .build();

            HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString());
            return Optional.of(new Response(parseResponse(response.body()), response));
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    /**
     * Get mountains
     */
    public Optional<Response> getAll() {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(new URI(serviceUri + "mountains"))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString());
            return Optional.of(new Response(parseResponse(response.body()), response));
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    /**
     * Get mountains by country
     *
     * @param country country to search for
     */
    public Optional<Response> getByCountry(final String country) {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(new URI(serviceUri + "mountains/country/" + urlEncode(country)))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString());
            return Optional.of(new Response(parseResponse(response.body()), response));
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    /**
     * Get mountains by country and range
     *
     * @param country country to search for
     * @param range   mountain range to search for
     */
    public Optional<Response> getByCountryAndRange(final String country, final String range) {
        try {
            String requestUri = serviceUri + "mountains/country/" + urlEncode(country)
                    + "/range/" + urlEncode(range);

            HttpRequest request = HttpRequest.newBuilder().uri(new URI(requestUri))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString());
            return Optional.of(new Response(parseResponse(response.body()), response));
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    /**
     * Get mountains by hemisphere
     *
     * @param isNorthern true for northern hemisphere
     */
    public Optional<Response> getByHemisphere(final boolean isNorthern) {
        try {
            String requestUri = serviceUri + "mountains?isNorthern="
                    + urlEncode(String.valueOf(isNorthern));

            HttpRequest request = HttpRequest.newBuilder().uri(new URI(requestUri))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString());
            return Optional.of(new Response(parseResponse(response.body()), response));
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    /**
     * Get mountains by country and altitude
     *
     * @param country  country to search for
     * @param altitude minimum altitude
     */
    public Optional<Response> getByCountryAltitude(final String country, final int altitude) {
        try {
            String requestUri = serviceUri + "mountains/country/" + urlEncode(country)
                    + "?minAltitude=" + urlEncode(String.valueOf(altitude));

            HttpRequest request = HttpRequest.newBuilder().uri(new URI(requestUri))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString());
            return Optional.of(new Response(parseResponse(response.body()), response));
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    /**
     * Get mountains by country, range, and name
     *
     * @param country country to search for
     * @param range   mountain range to search for
     * @param name    mountain name to search for
     */
    public Optional<Response> getByName(final String country, final String range,
                                        final String name) {
        try {
            String requestUri = serviceUri + "mountains/country/" + urlEncode(country)
                    + "/range/" + urlEncode(range)
                    + "/name/" + urlEncode(name);

            HttpRequest request = HttpRequest.newBuilder().uri(new URI(requestUri))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString());
            return Optional.of(new Response(parseResponse(response.body()), response));
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    /**
     * Get mountains by ID
     *
     * @param id mountain ID
     */
    public Optional<Response> getById(final int id) {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(new URI(serviceUri + "mountains/" + id))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString());
            return Optional.of(new Response(parseResponse(response.body()), response));
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    /**
     * Update mountain
     *
     * @param id       mountain ID
     * @param mountain mountain data
     */
    public Optional<Response> updateMountain(final int id, final Mountain mountain) {
        try {
            String mountainAsJson = objectMapper.writeValueAsString(mountain);

            HttpRequest request = HttpRequest.newBuilder().uri(new URI(serviceUri + "mountains/" + id))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(mountainAsJson))
                    .build();

            HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString());
            return Optional.of(new Response(parseResponse(response.body()), response));
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    /**
     * Delete a mountain
     *
     * @param id mountain ID
     */
    public Optional<Response> deleteMountain(final int id) {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(new URI(serviceUri + "mountains/" + id))
                    .header("Accept", "application/json")
                    .DELETE()
                    .build();

            HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString());
            return Optional.of(new Response(parseResponse(response.body()), response));
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    /**
     * Helper method to convert the response body into a list of mountains
     *
     * @param responseBody response body returned by the server
     * @return list of mountains, or an empty list if the body is empty
     * @throws Exception if JSON cannot be parsed
     */
    private List<Mountain> parseResponse(final String responseBody) throws Exception {
        // No response body means there is no mountain data to return
        if (responseBody == null) {
            return new ArrayList<>();
        }

        // An empty response body means there is no mountain data to return
        if (responseBody.isEmpty()) {
            return new ArrayList<>();
        }

        if (responseBody.startsWith("[")) {
            return objectMapper.readValue(responseBody, MOUNTAIN_LIST_TYPE);
        }

        Mountain mountain = objectMapper.readValue(responseBody, Mountain.class);

        List<Mountain> mountains = new ArrayList<>();
        mountains.add(mountain);
        return mountains;
    }

    /**
     * Helper method to encode a value for use in URI
     *
     * @param arg value to encode
     * @return encoded value
     */
    private String urlEncode(final String arg) {
        return URLEncoder.encode(arg, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
    }
}