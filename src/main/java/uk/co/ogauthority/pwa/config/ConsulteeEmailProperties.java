package uk.co.ogauthority.pwa.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConsulteeEmailProperties {

  private final String email;
  private final String name;

  public ConsulteeEmailProperties(@Value("${consultee.email}") String email,
                                  @Value("${consultee.name}") String name) {
    this.email = email;
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public String getName() {
    return name;
  }
}
