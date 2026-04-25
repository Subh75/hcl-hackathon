package com.favouritepayee.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "scoring")
public record ScoringProperties(
        double frequencyWeight,
        double recencyWeight,
        double timeWeight
) {
}
