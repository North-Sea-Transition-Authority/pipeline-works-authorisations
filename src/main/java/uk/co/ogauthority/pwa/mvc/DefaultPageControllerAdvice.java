package uk.co.ogauthority.pwa.mvc;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import uk.co.ogauthority.pwa.auth.CurrentUserView;
import uk.co.ogauthority.pwa.config.ServiceProperties;
import uk.co.ogauthority.pwa.controller.MarkdownController;
import uk.co.ogauthority.pwa.features.analytics.AnalyticsConfigurationProperties;
import uk.co.ogauthority.pwa.features.analytics.AnalyticsController;
import uk.co.ogauthority.pwa.features.analytics.AnalyticsUtils;
import uk.co.ogauthority.pwa.features.webapp.TopMenuService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.footer.FooterService;
import uk.co.ogauthority.pwa.util.ControllerUtils;
import uk.co.ogauthority.pwa.util.SecurityUtils;
import uk.co.ogauthority.pwa.util.ValidatorUtils;

/**
 * Provides common model objects for the default page view.
 */
@ControllerAdvice(annotations = Controller.class)
public class DefaultPageControllerAdvice {

  private final TopMenuService topMenuService;
  private final HttpServletRequest request;
  private final ServiceProperties serviceProperties;
  private final FooterService footerService;
  private final AnalyticsConfigurationProperties analyticsConfigurationProperties;
  private final String analyticsMeasurementUrl;

  @Autowired
  public DefaultPageControllerAdvice(TopMenuService topMenuService,
                                     HttpServletRequest request,
                                     ServiceProperties serviceProperties,
                                     FooterService footerService,
                                     AnalyticsConfigurationProperties analyticsConfigurationProperties) {
    this.topMenuService = topMenuService;
    this.request = request;
    this.serviceProperties = serviceProperties;
    this.footerService = footerService;
    this.analyticsConfigurationProperties = analyticsConfigurationProperties;
    this.analyticsMeasurementUrl = ReverseRouter.route(on(AnalyticsController.class)
        .collectAnalyticsEvent(null, Optional.empty()));
  }

  @InitBinder
  public void initBinder(WebDataBinder binder) {
    // Trim whitespace from form fields
    binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
  }

  @ModelAttribute
  public void addCommonModelAttributes(Model model) {
    addCurrentUserView(model);
    addTopMenuItems(model, request);
    addSubmitButtonText(model);
    addMarkdownGuidanceUrl(model);
    addGovNotifyMarkdownGuidanceUrl(model);
    footerService.addFooterUrlsToModel(model);
    model.addAttribute("service", serviceProperties);
    model.addAttribute("maxCharacterLength", ValidatorUtils.MAX_DEFAULT_STRING_LENGTH);
    model.addAttribute("feedbackUrl", ControllerUtils.getFeedbackUrl());
    model.addAttribute("analytics", analyticsConfigurationProperties.getProperties());
    model.addAttribute("analyticsMeasurementUrl", analyticsMeasurementUrl);
    model.addAttribute("cookiePrefsUrl", ControllerUtils.getCookiesUrl());
    model.addAttribute("analyticsClientIdCookieName", AnalyticsUtils.GA_CLIENT_ID_COOKIE_NAME);
  }

  private void addCurrentUserView(Model model) {
    SecurityUtils.getAuthenticatedUserFromSecurityContext()
        .ifPresent(user -> model.addAttribute("currentUserView", CurrentUserView.authenticated(user)));
  }

  private void addTopMenuItems(Model model, HttpServletRequest request) {
    SecurityUtils.getAuthenticatedUserFromSecurityContext()
        .ifPresent(user -> {
          model.addAttribute("navigationItems", topMenuService.getTopMenuItems(user));
          model.addAttribute("currentEndPoint", request.getRequestURI());
        });
  }

  private void addSubmitButtonText(Model model) {
    model.addAttribute("submitPrimaryButtonText", ValidationType.FULL.getButtonText());
    model.addAttribute("submitSecondaryButtonText", ValidationType.PARTIAL.getButtonText());
  }

  private void addMarkdownGuidanceUrl(Model model) {
    model.addAttribute("markdownGuidanceUrl",
        ReverseRouter.route(on(MarkdownController.class).renderMarkdownGuidance(null)));
  }

  private void addGovNotifyMarkdownGuidanceUrl(Model model) {
    model.addAttribute("govNotifyMarkdownGuidanceUrl",
        ReverseRouter.route(on(MarkdownController.class).renderEmailMarkdownGuidance(null)));
  }

}
