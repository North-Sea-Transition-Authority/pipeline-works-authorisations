package uk.co.ogauthority.pwa.integrations.energyportal.access;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "energy-portal.access-api")
@Validated
public record EnergyPortalAccessApiConfiguration(
    @NotNull String baseUrl,
    @NotNull String token,
    @NotNull String resourceType,
    @NotNull String privilegeName
) {
}
