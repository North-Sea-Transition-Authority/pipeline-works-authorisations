package uk.co.ogauthority.pwa.controller;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import uk.co.ogauthority.pwa.auth.EnergyPortalConfiguration;

@EnableConfigurationProperties(value = {
    EnergyPortalConfiguration.class
})
@Retention(RetentionPolicy.RUNTIME)
public @interface IncludeEnergyPortalConfigurationProperties {
}
