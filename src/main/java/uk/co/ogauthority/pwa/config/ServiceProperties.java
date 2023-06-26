package uk.co.ogauthority.pwa.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ServiceProperties {

  private final String serviceName;
  private final String fullServiceName;
  private final String customerMnemonic;
  private final String customerName;

  private final String emtMnemonic;

  @Autowired
  public ServiceProperties(@Value("${service.name}") String serviceName,
                           @Value("${service.full-name}") String fullServiceName,
                           @Value("${service.customer.mnemonic}") String customerMnemonic,
                           @Value("${service.customer.name}") String customerName,
                           @Value("${service.emt.authority.mnemonic}") String emtMnemonic) {
    this.serviceName = serviceName;
    this.fullServiceName = fullServiceName;
    this.customerMnemonic = customerMnemonic;
    this.customerName = customerName;
    this.emtMnemonic = emtMnemonic;
  }

  public String getServiceName() {
    return serviceName;
  }

  public String getFullServiceName() {
    return fullServiceName;
  }

  public String getCustomerMnemonic() {
    return customerMnemonic;
  }

  public String getCustomerName() {
    return customerName;
  }

  public String getEmtMnemonic() {
    return emtMnemonic;
  }
}