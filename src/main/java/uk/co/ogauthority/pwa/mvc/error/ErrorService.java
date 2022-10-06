package uk.co.ogauthority.pwa.mvc.error;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Optional;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.config.ServiceProperties;
import uk.co.ogauthority.pwa.config.TechnicalSupportContactProperties;
import uk.co.ogauthority.pwa.features.analytics.AnalyticsConfigurationProperties;
import uk.co.ogauthority.pwa.features.analytics.AnalyticsController;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.util.ControllerUtils;

@Service
public class ErrorService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ErrorService.class);

  private final TechnicalSupportContactProperties technicalSupportContactProperties;
  private final ServiceProperties serviceProperties;
  private final AnalyticsConfigurationProperties analyticsConfigurationProperties;
  private final String analyticsMeasurementUrl;

  @Autowired
  public ErrorService(TechnicalSupportContactProperties technicalSupportContactProperties,
                      ServiceProperties serviceProperties,
                      AnalyticsConfigurationProperties analyticsConfigurationProperties) {
    this.technicalSupportContactProperties = technicalSupportContactProperties;
    this.serviceProperties = serviceProperties;
    this.analyticsConfigurationProperties = analyticsConfigurationProperties;
    this.analyticsMeasurementUrl = ReverseRouter.route(on(AnalyticsController.class)
        .collectAnalyticsEvent(null, Optional.empty()));
  }

  private String getErrorReference() {
    return RandomStringUtils.randomNumeric(10);
  }

  private void addErrorReference(ModelAndView modelAndView, Throwable throwable) {
    var errorReference = getErrorReference();
    modelAndView.addObject("errorRef", errorReference);
    LOGGER.error("Caught unhandled exception (errorRef {})", errorReference, throwable);
  }

  private void addTechnicalSupportContactDetails(ModelAndView modelAndView) {
    modelAndView.addObject("technicalSupportContact", technicalSupportContactProperties);
  }

  public ModelAndView addErrorAttributesToModel(ModelAndView modelAndView, Throwable throwable) {
    if (throwable != null) {
      addErrorReference(modelAndView, throwable);
    }
    addTechnicalSupportContactDetails(modelAndView);
    modelAndView.addObject("feedbackUrl", ControllerUtils.getFeedbackUrl());
    modelAndView.addObject("service", serviceProperties);
    modelAndView.addObject("analytics", analyticsConfigurationProperties.getProperties());
    modelAndView.addObject("analyticsMeasurementUrl", analyticsMeasurementUrl);
    modelAndView.addObject("cookiePrefsUrl", ControllerUtils.getCookiesUrl());

    return modelAndView;
  }

}