package uk.co.ogauthority.pwa.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ServiceProperties {

  private final String serviceName;
  private final String customerMnemonic;
  private final String customerName;

  @Autowired
  public ServiceProperties(@Value("${service.name}") String serviceName,
                           @Value("${service.customer.mnemonic}") String customerMnemonic,
                           @Value("${service.customer.name}") String customerName) {
    this.serviceName = serviceName;
    this.customerMnemonic = customerMnemonic;
    this.customerName = customerName;
  }

  public String getServiceName() {
    return serviceName;
  }

  public String getCustomerMnemonic() {
    return customerMnemonic;
  }

  public String getCustomerName() {
    return customerName;
  }
}

