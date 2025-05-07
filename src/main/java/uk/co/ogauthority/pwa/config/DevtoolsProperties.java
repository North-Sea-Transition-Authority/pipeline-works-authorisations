package uk.co.ogauthority.pwa.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;

@Profile("devtools")
@ConfigurationProperties(prefix = "devtools")
public record DevtoolsProperties(
    String migrationS3Bucket,
    String migrationCsvFileKey
) {}
