package com.deliciouspizza.dto.geocode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class GeocodeSearchResponseDto {

    private List<Feature> features;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    @AllArgsConstructor
    public static class Feature {

        private Geometry geometry;

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    @AllArgsConstructor
    public static class Geometry {

        private List<Double> coordinates; // [longitude, latitude]

    }
}