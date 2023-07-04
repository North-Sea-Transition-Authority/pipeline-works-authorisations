package uk.co.ogauthority.pwa.features.termsandconditions.controller;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.features.termsandconditions.model.TermsAndConditionsVariationForm;
import uk.co.ogauthority.pwa.features.termsandconditions.service.TermsAndConditionsVariationService;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.util.FlashUtils;
import uk.co.ogauthority.pwa.util.RouteUtils;

@Controller
@RequestMapping("/terms-and-conditions")
public class TermsAndConditionsVariationFormController {

  private final TermsAndConditionsVariationService termsAndConditionsVariationService;
  private final ControllerHelperService controllerHelperService;

  public TermsAndConditionsVariationFormController(
      TermsAndConditionsVariationService termsAndConditionsVariationService,
      ControllerHelperService controllerHelperService) {
    this.termsAndConditionsVariationService = termsAndConditionsVariationService;
    this.controllerHelperService = controllerHelperService;
  }

  @GetMapping
  public ModelAndView renderTermsAndConditionsVariationForm(@ModelAttribute("form") TermsAndConditionsVariationForm form,
                                                            AuthenticatedUserAccount user) {
    checkUserPrivilege(user);
    return getTermsAndConditionsVariationModelAndView();
  }

  @PostMapping
  public ModelAndView submitTermsAndConditionsVariationForm(@ModelAttribute("form") TermsAndConditionsVariationForm form,
                                                            BindingResult bindingResult,
                                                            AuthenticatedUserAccount user,
                                                            RedirectAttributes redirectAttributes) {
    checkUserPrivilege(user);
    var validatedBindingResult = termsAndConditionsVariationService.validateForm(form, bindingResult);

    return controllerHelperService.checkErrorsAndRedirect(validatedBindingResult,
        getTermsAndConditionsVariationModelAndView(), () -> {
          termsAndConditionsVariationService.saveForm(form, user.getWuaId());
          FlashUtils.success(
              redirectAttributes,
              "Submitted terms and conditions variation"
          );
          return RouteUtils.redirectWorkArea();
        });
  }

  private ModelAndView getTermsAndConditionsVariationModelAndView() {
    return new ModelAndView("termsandconditions/termsAndConditionsVariationForm")
        .addObject("cancelUrl", "/work-area") // placeholder
        .addObject("pwaSelectorOptions", termsAndConditionsVariationService.getPwasForSelector());
  }

  private void checkUserPrivilege(AuthenticatedUserAccount authenticatedUser) {
    if (!authenticatedUser.hasPrivilege(PwaUserPrivilege.PWA_MANAGER)) {
      throw new AccessDeniedException("Access to terms and conditions denied");
    }
  }
}
