package com.deliciouspizza.dto.geocode;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
public class CalculatedDistance {

    private double distance;
    private double duration;

    public CalculatedDistance() {
        this.distance = 0.0;
        this.duration = 0.0;
    }

    public CalculatedDistance(double distance, double duration) {
        this.distance = distance;
        this.duration = duration;
    }
}
