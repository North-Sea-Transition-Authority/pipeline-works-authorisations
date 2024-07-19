package uk.co.ogauthority.pwa.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "pwa.api")
@Validated
public class ExternalApiConfiguration {

  @NotNull
  private String preSharedKey;

  public String getPreSharedKey() {
    return preSharedKey;
  }

  public void setPreSharedKey(String preSharedKey) {
    this.preSharedKey = preSharedKey;
  }
}
