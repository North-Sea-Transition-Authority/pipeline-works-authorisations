package uk.co.ogauthority.pwa.mvc;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.co.ogauthority.pwa.features.analytics.AnalyticsService;
import uk.co.ogauthority.pwa.features.analytics.AnalyticsUtils;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;

@Component
public class PwaApplicationRouteInterceptor implements HandlerInterceptor {

  private static final Logger LOGGER = LoggerFactory.getLogger(PwaApplicationRouteInterceptor.class);

  private final AnalyticsService analyticsService;

  public PwaApplicationRouteInterceptor(AnalyticsService analyticsService) {
    this.analyticsService = analyticsService;
  }

  @Override
  @Async
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                              Exception ex) throws Exception {

    try {

      // if we're POSTing and triggering some validation, log this to analytics (i.e. full or partial)
      if (Objects.equals(request.getMethod(), "POST")) {

        var resolvedValidationTypes = Stream.of(ValidationType.values())
            .filter(validationType -> request.getParameter(validationType.getButtonText()) != null)
            .collect(Collectors.toList());

        if (resolvedValidationTypes.isEmpty()) {
          return;
        }

        var analyticsClientIdOpt = Arrays.stream(request.getCookies())
            .filter(cookie -> Objects.equals(cookie.getName(), AnalyticsUtils.GA_CLIENT_ID_COOKIE_NAME))
            .map(Cookie::getValue)
            .findFirst();

        // transforms "/engedudev1/cw/pwa-application/initial/261/project-information/" into "project-information"
        var endpointIdString = StringUtils.substringBefore(StringUtils.reverseDelimited(request.getRequestURI(), '/'), "/");

        resolvedValidationTypes.forEach(validationType -> analyticsService
            .sendAnalyticsEvent(
                analyticsClientIdOpt,
                validationType.getAnalyticsEventCategory(),
                Map.of("endpoint", endpointIdString)));

      }

    } catch (Exception e) {
      LOGGER.error("Error trying to intercept PWA application route: {}", request.getRequestURI(), e);
    }

  }

}