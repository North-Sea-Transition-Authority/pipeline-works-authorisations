package uk.co.ogauthority.pwa.features.analytics;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class AnalyticsController {

  private final AnalyticsService analyticsService;

  @Autowired
  public AnalyticsController(AnalyticsService analyticsService) {
    this.analyticsService = analyticsService;
  }

  @PostMapping(value = "/analytics/collect", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ModelAndView collectAnalyticsEvent(
      @RequestBody AnalyticsEventForm analyticsEventForm,
      @CookieValue(name = AnalyticsUtils.GA_CLIENT_ID_COOKIE_NAME, required = false) Optional<String> googleAnalyticsClientId
  ) {

    analyticsService.sendAnalyticsEvent(
        googleAnalyticsClientId,
        analyticsEventForm.getEventCategory(),
        analyticsEventForm.getParamMap()
    );

    return new ModelAndView("blank");

  }

}
