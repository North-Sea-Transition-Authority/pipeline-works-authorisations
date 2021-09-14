package uk.co.ogauthority.pwa.mvc.error;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.config.TechnicalSupportContactProperties;

@Service
public class ErrorService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ErrorService.class);

  private final TechnicalSupportContactProperties technicalSupportContactProperties;

  @Autowired
  public ErrorService(TechnicalSupportContactProperties technicalSupportContactProperties) {
    this.technicalSupportContactProperties = technicalSupportContactProperties;
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
    return modelAndView;
  }

}