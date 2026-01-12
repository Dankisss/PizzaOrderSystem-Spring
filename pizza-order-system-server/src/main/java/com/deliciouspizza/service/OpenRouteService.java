package com.deliciouspizza.service;

import com.deliciouspizza.dto.geocode.CalculatedDistance;
import com.deliciouspizza.dto.geocode.DirectionsResponseDto;
import com.deliciouspizza.dto.geocode.GeocodeSearchResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

@Service
public class OpenRouteService {

    private static final Logger logger = Logger.getLogger(OpenRouteService.class.getName());

    private static final double METERS_IN_KILOMETER = 1000.0;
    private static final double AVERAGE_CAR_SPEED = 50;

    private final WebClient webClient;
    private final String apiKey;
    private final String directionsPath;
    private final String geocodePath;

    public OpenRouteService(
            WebClient.Builder webClientBuilder,
            @Value("${openrouteservice.base-url}") String baseUrl,
            @Value("${openrouteservice.api-key}") String apiKey,
            @Value("${openrouteservice.directions-path}") String directionsPath,
            @Value("${openrouteservice.geocode-path}") String geocodePath) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
        this.directionsPath = directionsPath;
        this.geocodePath = geocodePath;
    }

    /**
     * Converts an address string into geographic coordinates (longitude, latitude).
     *
     * @param address The address string (e.g., "1600 Amphitheatre Parkway, Mountain View, CA").
     * @return A Mono emitting a List of Double representing [longitude, latitude], or empty if not found.
     */
    public Mono<List<Double>> getCoordinates(String address) {
        logger.info("Geocoding address: " + address);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(geocodePath)
                        .queryParam("api_key", apiKey)
                        .queryParam("text", address)
                        .build())
                .retrieve()
                .bodyToMono(GeocodeSearchResponseDto.class)
                .map(response -> {
                    List<Double> resultCoordinates = new ArrayList<>();
                    if (response != null && response.getFeatures() != null && !response.getFeatures().isEmpty()) {
                        GeocodeSearchResponseDto.Geometry geometry = response.getFeatures().getFirst().getGeometry();
                        if (geometry != null && geometry.getCoordinates() != null && geometry.getCoordinates().size() == 2) {
                            resultCoordinates = geometry.getCoordinates();
                        }
                    }
                    return resultCoordinates;
                })
                .doOnError(e -> {
                    if (e instanceof WebClientResponseException) {
                        WebClientResponseException ex = (WebClientResponseException) e;
                        logger.severe("Error geocoding address '" + address + "'. Status: " + ex.getStatusCode() + ". Body: " + ex.getResponseBodyAsString());
                    } else {
                        logger.severe("Error geocoding address '" + address + "': " + e.getMessage());
                    }
                })
                .defaultIfEmpty(Collections.emptyList());
    }

    /**
     * Calculates the distance between two sets of coordinates.
     * Coordinates should be in [longitude, latitude] format.
     *
     * @param startCoordinates [longitude, latitude] of the start point.
     * @param endCoordinates   [longitude, latitude] of the end point.
     * @return A Mono emitting the distance in meters, or 0.0 if calculation fails.
     */
    public Mono<CalculatedDistance> getDistance(List<Double> startCoordinates, List<Double> endCoordinates) {
        if (startCoordinates == null || startCoordinates.size() != 2 || endCoordinates == null || endCoordinates.size() != 2) {
            logger.warning("Invalid coordinates provided for distance calculation.");
            return Mono.just( new CalculatedDistance(0.0, 0.0));
        }

        String startString = String.format(java.util.Locale.US, "%.7f,%.7f", startCoordinates.get(0), startCoordinates.get(1));
        String endString = String.format(java.util.Locale.US, "%.7f,%.7f", endCoordinates.get(0), endCoordinates.get(1));

        logger.info("Calculating distance between: " + startString + " and " + endString);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(directionsPath)
                        .queryParam("api_key", apiKey)
                        .queryParam("start", startString)
                        .queryParam("end", endString)
                        .build())
                .retrieve()
                .bodyToMono(DirectionsResponseDto.class)
                .map(response -> {
                    if (response != null && response.getFeatures() != null && !response.getFeatures().isEmpty()) {
                        DirectionsResponseDto.Summary summary = response.getFeatures().getFirst().getProperties().getSummary();
                        if (summary != null && summary.getDistance() != null) {
                            return new CalculatedDistance(
                                    summary.getDistance() / METERS_IN_KILOMETER,
                                    summary.getDuration() / AVERAGE_CAR_SPEED
                            );
                        }
                    }
                    return new CalculatedDistance(0.0, 0.0);
                })
                .doOnError(e -> {
                    if (e instanceof WebClientResponseException) {
                        WebClientResponseException ex = (WebClientResponseException) e;
                        System.out.println("Error calculating distance. Status: " + ex.getStatusCode() + ". Body: " + ex.getResponseBodyAsString());
                    } else {
                        System.out.println("Error calculating distance: " + e.getMessage());
                    }
                })
                .defaultIfEmpty( new CalculatedDistance(0.0, 0.0));
    }
}