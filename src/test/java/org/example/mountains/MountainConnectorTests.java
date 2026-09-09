package org.example.mountains;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

class MountainConnectorTests {
    @Test
    void retainsHttpErrorStatusAndBodyInsteadOfHidingItAsAnEmptyOptional() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/mountains", exchange -> {
            byte[] bytes = "{\"error\":\"Unavailable\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(503, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
        try {
            var result = new MountainConnector("http://127.0.0.1:" + server.getAddress().getPort()).getAll().orElseThrow();
            assertEquals(503, result.getResponse().statusCode());
            assertEquals("{\"error\":\"Unavailable\"}", result.getResponse().body());
            assertTrue(result.getMountains().isEmpty());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void invalidBaseUriIsRejectedEarly() {
        assertThrows(IllegalArgumentException.class, () -> new MountainConnector("not-a-url"));
        assertThrows(IllegalArgumentException.class, () -> new MountainConnector("http://localhost/?query=1"));
    }
}
