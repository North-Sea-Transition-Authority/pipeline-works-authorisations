package uk.co.ogauthority.pwa.mvc;

import static net.logstash.logback.argument.StructuredArguments.value;

import com.google.common.base.Stopwatch;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;
import uk.co.ogauthority.pwa.hibernate.HibernateQueryCounter;

@Component
public class RequestLogFilter extends OncePerRequestFilter {

  static final String MDC_WUA_ID = RequestLogFilter.class.getName() + ".WUA_ID";
  static final String MDC_REQUEST_TYPE = RequestLogFilter.class.getName() + ".REQUEST_TYPE";
  private static final Logger LOGGER = LoggerFactory.getLogger(RequestLogFilter.class);
  private static final String UNKNOWN = "unknown";

  private final HibernateQueryCounter hibernateQueryCounter;

  @Autowired
  public RequestLogFilter(HibernateQueryCounter hibernateQueryCounter) {
    this.hibernateQueryCounter = hibernateQueryCounter;
  }

  @Override
  protected void doFilterInternal(@NotNull HttpServletRequest request,
                                  @NotNull HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
    var stopwatch = Stopwatch.createStarted();

    var nonMvcPath = request.getRequestURI().contains("/actuator/health")
        || request.getRequestURI().contains("/assets/");

    if (nonMvcPath) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      filterChain.doFilter(request, response);
    } finally {
      String queryString = StringUtils.defaultString(request.getQueryString());
      if (!queryString.isEmpty()) {
        queryString = "?" + queryString;
      }

      Long overallQueryCount = hibernateQueryCounter.getQueryCount();

      Object patternAttribute = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
      String mvcPattern = StringUtils.firstNonBlank(patternAttribute != null ? patternAttribute.toString() : "",
          UNKNOWN);

      String requestType = StringUtils.firstNonBlank(MDC.get(MDC_REQUEST_TYPE), UNKNOWN);
      String userId = StringUtils.firstNonBlank(MDC.get(MDC_WUA_ID), UNKNOWN);
      LOGGER.info(
          "{} request: {} {}{} ({}), time: {}, status: {}, user id: {}, overall hibernate count: {}, ",
          value("request_type", requestType),
          value("request_method", request.getMethod()),
          value("request_uri", request.getRequestURI()),
          value("request_query_string", queryString),
          value("request_uri_pattern", mvcPattern),
          value("time_ms", stopwatch.elapsed(TimeUnit.MILLISECONDS)),
          value("response_status", response.getStatus()),
          value("wua_id", userId),
          value("query_count_overall", overallQueryCount));

      hibernateQueryCounter.clearQueryCount();
    }
  }
}
