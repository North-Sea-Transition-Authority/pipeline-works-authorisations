package uk.co.ogauthority.pwa.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.CurrentUserView;
import uk.co.ogauthority.pwa.service.FoxUrlService;

@ControllerAdvice
public class DefaultPageControllerAdvice {

  private final FoxUrlService foxUrlService;

  @Autowired
  public DefaultPageControllerAdvice(FoxUrlService foxUrlService) {
    this.foxUrlService = foxUrlService;
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
  }

  private void addCurrentUserView(Model model) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUserAccount) {
      AuthenticatedUserAccount user = (AuthenticatedUserAccount) authentication.getPrincipal();
      model.addAttribute("currentUserView", CurrentUserView.authenticated(user));
    } else {
      model.addAttribute("currentUserView", CurrentUserView.unauthenticated());
    }
  }

  private void addLogoutUrl(Model model) {
    model.addAttribute("foxLogoutUrl", foxUrlService.getFoxLogoutUrl());
  }

}
