package com.gsp.aiims.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.jwt")
public class JwtConfig {

    @NotBlank
    private String secret;

    @Positive
    private long expirationMs = 86400000L;

    @Positive
    private long refreshExpirationMs = 604800000L;
}
