package uk.co.ogauthority.pwa.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TechnicalSupportContactProperties {

  private final String serviceName;

  private final String emailAddress;

  private final String phoneNumber;

  @Autowired
  public TechnicalSupportContactProperties(@Value("${pwa.tech-support.service-name}") String serviceName,
                                           @Value("${pwa.tech-support.email}") String emailAddress,
                                           @Value("${pwa.tech-support.number}") String phoneNumber) {
    this.serviceName = serviceName;
    this.emailAddress = emailAddress;
    this.phoneNumber = phoneNumber;
  }

  public String getServiceName() {
    return serviceName;
  }

  public String getEmailAddress() {
    return emailAddress;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

}