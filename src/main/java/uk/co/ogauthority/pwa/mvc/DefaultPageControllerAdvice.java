package uk.co.ogauthority.pwa.mvc;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import javax.servlet.http.HttpServletRequest;
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
import uk.co.ogauthority.pwa.energyportal.service.TopMenuService;
import uk.co.ogauthority.pwa.service.FoxUrlService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.footer.FooterService;
import uk.co.ogauthority.pwa.util.SecurityUtils;
import uk.co.ogauthority.pwa.util.ValidatorUtils;

/**
 * Provides common model objects for the default page view.
 */
@ControllerAdvice(annotations = Controller.class)
public class DefaultPageControllerAdvice {

  private final FoxUrlService foxUrlService;
  private final TopMenuService topMenuService;
  private final HttpServletRequest request;
  private final ServiceProperties serviceProperties;
  private final FooterService footerService;

  @Autowired
  public DefaultPageControllerAdvice(FoxUrlService foxUrlService,
                                     TopMenuService topMenuService,
                                     HttpServletRequest request,
                                     ServiceProperties serviceProperties,
                                     FooterService footerService) {
    this.foxUrlService = foxUrlService;
    this.topMenuService = topMenuService;
    this.request = request;
    this.serviceProperties = serviceProperties;
    this.footerService = footerService;
  }

  @InitBinder
  public void initBinder(WebDataBinder binder) {
    // Trim whitespace from form fields
    binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
  }

  @ModelAttribute
  public void addCommonModelAttributes(Model model) {
    addCurrentUserView(model);
    addLogoutUrl(model);
    addTopMenuItems(model, request);
    addSubmitButtonText(model);
    addMarkdownGuidanceUrl(model);
    footerService.addFooterUrlsToModel(model);
    model.addAttribute("service", serviceProperties);
    model.addAttribute("maxCharacterLength", ValidatorUtils.MAX_DEFAULT_STRING_LENGTH);
  }

  private void addCurrentUserView(Model model) {
    SecurityUtils.getAuthenticatedUserFromSecurityContext()
        .ifPresent(user -> model.addAttribute("currentUserView", CurrentUserView.authenticated(user)));
  }

  private void addLogoutUrl(Model model) {
    model.addAttribute("foxLogoutUrl", foxUrlService.getFoxLogoutUrl());
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

}
