package uk.co.ogauthority.pwa.component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.co.ogauthority.pwa.util.SecurityUtils;

@Component
@Profile("request-logging")
public class RequestLogger implements HandlerInterceptor {

  private static final String REQUEST_START_TIME_ATTR_NAME = "requestLoggerStartInstant";
  private static final Logger LOGGER = LoggerFactory.getLogger(RequestLogger.class);

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    request.setAttribute(REQUEST_START_TIME_ATTR_NAME, Instant.now().toEpochMilli());
    return true;
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                              Exception ex) throws Exception {

    try {

      var requestStart = (Long) request.getAttribute(REQUEST_START_TIME_ATTR_NAME);
      var requestDurationMs = Instant.now().toEpochMilli() - requestStart;

      String queryStringAppend = request.getQueryString() != null ? "?" + request.getQueryString() : "";
      var url = request.getRequestURI() + queryStringAppend;

      var userIdString = SecurityUtils.getAuthenticatedUserFromSecurityContext()
          .map(user -> {

            String proxyUserString = user.getProxyUserWuaId()
                    .map(proxyWuaId -> String.format(" | proxy wuaid = %s", proxyWuaId))
                    .orElse("");

            return String.format("user [wuaid = %s%s]",  user.getWuaId(), proxyUserString);

          })
          .orElse("unidentified user");

      LOGGER.info("{} '{}' returned {} in {} ms for {}", request.getMethod(), url, response.getStatus(), requestDurationMs, userIdString);

    } catch (Exception e) {
      LOGGER.warn("Error logging request.", e);
    }

  }
}
