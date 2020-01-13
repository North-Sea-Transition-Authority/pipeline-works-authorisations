package uk.co.ogauthority.pwa.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FoxUrlService {

  private final String foxLoginUrl;
  private final String foxLogoutUrl;

  public FoxUrlService(@Value("${app.fox.login-url}") String foxLoginUrl, @Value("${app.fox.logout-url}") String foxLogoutUrl) {
    this.foxLoginUrl = foxLoginUrl;
    this.foxLogoutUrl = foxLogoutUrl;
  }

  public String getFoxLoginUrl() {
    return foxLoginUrl;
  }

  public String getFoxLogoutUrl() {
    return foxLogoutUrl;
  }
}
