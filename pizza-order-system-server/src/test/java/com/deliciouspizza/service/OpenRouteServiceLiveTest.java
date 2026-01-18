package com.deliciouspizza.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.util.List;

@SpringBootTest
@ActiveProfiles("test") // Tells Spring to use application-test.properties
public class OpenRouteServiceLiveTest {

    @Autowired
    private OpenRouteService openRouteService;

    @Test
    public void testLiveGeocoding() {
        String address = "1600 Amphitheatre Parkway, Mountain View, CA";

        StepVerifier.create(openRouteService.getCoordinates(address))
                .assertNext(coords -> {
                    System.out.println("Live API Response (Coordinates): " + coords);
                    // Check that we got back a valid Long/Lat pair
                    assert coords.size() == 2;
                    assert coords.get(0) != 0.0;
                })
                .verifyComplete();
    }

    @Test
    public void testLiveDistance() {
        // Sofia Center to Plovdiv Center (Bulgaria)
        List<Double> sofia = List.of(23.3219, 42.6977);
        List<Double> plovdiv = List.of(24.7453, 42.1354);

        StepVerifier.create(openRouteService.getDistance(sofia, plovdiv))
                .assertNext(distance -> {
                    System.out.println("Live API Response (Distance in meters): " + distance);
                    // Distance should be roughly 140km - 150km (140,000+ meters)
                    assert distance > 100000;
                })
                .verifyComplete();
    }
}