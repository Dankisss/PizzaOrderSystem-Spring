package com.deliciouspizza.service;

import com.deliciouspizza.dto.geocode.DirectionsResponseDto;
import com.deliciouspizza.dto.geocode.GeocodeSearchResponseDto;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.List;

class OpenRouteServiceTest {

    private static MockWebServer mockWebServer;
    private OpenRouteService openRouteService;

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void initialize() {
        String baseUrl = String.format("http://localhost:%s", mockWebServer.getPort());

        // Manual instantiation since we aren't loading the full Spring Context
        openRouteService = new OpenRouteService(
                WebClient.builder(),
                baseUrl,
                "test-api-key",
                "/v2/directions/driving-car",
                "/geocode/search"
        );
    }

    @Test
    void getCoordinates_ShouldReturnLongitudeAndLatitude() {
        // Mocking the JSON response from OpenRouteService
        String mockJsonResponse = """
            {
              "features": [{
                "geometry": {
                  "coordinates": [23.3219, 42.6977]
                }
              }]
            }
            """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(mockJsonResponse)
                .addHeader("Content-Type", "application/json"));

        StepVerifier.create(openRouteService.getCoordinates("Sofia, Bulgaria"))
                .expectNextMatches(coords ->
                        coords.get(0) == 23.3219 && coords.get(1) == 42.6977)
                .verifyComplete();
    }

    @Test
    void getDistance_ShouldReturnMeters() {
        // Mocking the directions response
        String mockJsonResponse = """
            {
              "routes": [{
                "summary": {
                  "distance": 1500.5
                }
              }]
            }
            """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(mockJsonResponse)
                .addHeader("Content-Type", "application/json"));

        List<Double> start = List.of(23.3219, 42.6977);
        List<Double> end = List.of(23.3300, 42.7000);

        StepVerifier.create(openRouteService.getDistance(start, end))
                .expectNext(1500.5)
                .verifyComplete();
    }
}