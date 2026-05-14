package io.github.fhv5.finsight.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "application.security")
public record TokenProperties(
        String secretKey,
        Duration accessTokenExpiration,
        Duration refreshTokenExpiration
) {}
