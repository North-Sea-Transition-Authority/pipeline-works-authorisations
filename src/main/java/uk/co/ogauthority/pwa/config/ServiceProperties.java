package uk.co.ogauthority.pwa.config;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

@Component
public class ServiceProperties {

  private final String serviceName;
  private final String fullServiceName;
  private final String serviceAcronym;
  private final String customerMnemonic;
  private final String customerName;
  private final String serviceUrl;
  private final String emtMnemonic;

  @Autowired
  public ServiceProperties(@Value("${service.name}") String serviceName,
                           @Value("${service.full-name}") String fullServiceName,
                           @Value("${service.name.acronym}") String serviceAcronym,
                           @Value("${service.customer.mnemonic}") String customerMnemonic,
                           @Value("${service.customer.name}") String customerName,
                           @Value("${service.emt.authority.mnemonic}") String emtMnemonic) {
    this.serviceName = serviceName;
    this.fullServiceName = fullServiceName;
    this.serviceAcronym = serviceAcronym;
    this.customerMnemonic = customerMnemonic;
    this.customerName = customerName;
    this.emtMnemonic = emtMnemonic;
    this.serviceUrl = ReverseRouter.route(on(WorkAreaController.class).renderWorkArea(null, null, null));
  }

  public String getServiceName() {
    return serviceName;
  }

  public String getFullServiceName() {
    return fullServiceName;
  }

  public String getServiceAcronym() {
    return serviceAcronym;
  }

  public String getCustomerMnemonic() {
    return customerMnemonic;
  }

  public String getCustomerName() {
    return customerName;
  }

  public String getServiceUrl() {
    return serviceUrl;
  }

  public String getEmtMnemonic() {
    return emtMnemonic;
  }
}