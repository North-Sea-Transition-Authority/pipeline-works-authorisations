package uk.co.ogauthority.pwa.mvc;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

import java.util.Optional;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;
import uk.co.ogauthority.pwa.mvc.error.ErrorService;

@Controller
public class DefaultErrorController implements ErrorController {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultErrorController.class);

  private final ErrorService errorService;

  @Autowired
  public DefaultErrorController(ErrorService errorService) {
    this.errorService = errorService;
  }

  /**
   * Handles framework-level errors (404s, authorisation failures, filter exceptions) for browser clients. Errors thrown
   * by app code (controller methods and below) are handled in {@link DefaultExceptionResolver}.
   */
  @RequestMapping("/error")
  public ModelAndView handleError(HttpServletRequest request) {
    Optional<Integer> statusCode = Optional.ofNullable(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE))
        .map(e -> (Integer) e);

    String viewName = statusCode.map(this::getViewName).orElse("error");
    ModelAndView modelAndView = new ModelAndView(viewName);

    //Look for the Spring specific exception first, fall back to the Servlet exception if not available
    Object dispatcherException = request.getAttribute(DispatcherServlet.EXCEPTION_ATTRIBUTE);
    Object servletException = request.getAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE);
    Throwable throwable = (Throwable) ObjectUtils.defaultIfNull(dispatcherException, servletException);

    errorService.addErrorAttributesToModel(modelAndView, throwable);

    return modelAndView;
  }

  private String getViewName(int statusCode) {
    switch (statusCode) {
      case SC_NOT_FOUND:
      case SC_METHOD_NOT_ALLOWED:
        return "error/404";
      case SC_FORBIDDEN:
      case SC_UNAUTHORIZED:
        return "error/403";
      default:
        return "error";
    }
  }

  @Override
  public String getErrorPath() {
    return "/error";
  }

}