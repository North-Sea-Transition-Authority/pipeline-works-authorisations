package uk.co.ogauthority.pwa.mvc;

import static jakarta.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

import org.apache.catalina.connector.ClientAbortException;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;
import uk.co.ogauthority.pwa.mvc.error.ErrorService;
import uk.co.ogauthority.pwa.service.footer.FooterService;

@Component
public class DefaultExceptionResolver extends SimpleMappingExceptionResolver {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExceptionResolver.class);
  private final FooterService footerService;
  private final ErrorService errorService;

  @Autowired
  public DefaultExceptionResolver(FooterService footerService,
                                  ErrorService errorService) {
    this.footerService = footerService;
    this.errorService = errorService;
    setDefaultErrorView("error");
    setDefaultStatusCode(SC_INTERNAL_SERVER_ERROR);
  }

  @Override
  protected ModelAndView getModelAndView(String viewName, Exception ex) {
    if (ex instanceof ClientAbortException) {

      //See https://mtyurt.net/post/spring-how-to-handle-ioexception-broken-pipe.html
      //ClientAbortException indicates a broken pipe/network error. Return null so it can be handled by the servlet,
      //otherwise Spring attempts to write to the broken response.
      LOGGER.trace("Suppressed ClientAbortException");
      return null;

    } else {

      ModelAndView modelAndView = super.getModelAndView(viewName, ex);

      String errorRef = RandomStringUtils.randomNumeric(5);

      LOGGER.error("Caught unhandled exception (ref {})", errorRef, ex);

      modelAndView.addObject("errorRef", errorRef);

      footerService.addFooterUrlsToModelAndView(modelAndView);
      errorService.addErrorAttributesToModel(modelAndView, ex);

      return modelAndView;

    }
  }
}
