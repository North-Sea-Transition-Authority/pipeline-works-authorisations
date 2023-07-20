package uk.co.ogauthority.pwa.features.termsandconditions.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

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
import uk.co.ogauthority.pwa.features.termsandconditions.model.TermsAndConditionsForm;
import uk.co.ogauthority.pwa.features.termsandconditions.service.TermsAndConditionsService;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.util.FlashUtils;
import uk.co.ogauthority.pwa.util.RouteUtils;

@Controller
@RequestMapping("/terms-and-conditions/new")
public class TermsAndConditionsFormController {

  private final TermsAndConditionsService termsAndConditionsService;
  private final ControllerHelperService controllerHelperService;

  public TermsAndConditionsFormController(
      TermsAndConditionsService termsAndConditionsService,
      ControllerHelperService controllerHelperService) {
    this.termsAndConditionsService = termsAndConditionsService;
    this.controllerHelperService = controllerHelperService;
  }

  @GetMapping
  public ModelAndView renderTermsAndConditionsVariationForm(@ModelAttribute("form") TermsAndConditionsForm form,
                                                            AuthenticatedUserAccount user) {
    checkUserPrivilege(user);
    return getTermsAndConditionsVariationModelAndView();
  }

  @PostMapping
  public ModelAndView submitTermsAndConditionsVariationForm(@ModelAttribute("form") TermsAndConditionsForm form,
                                                            BindingResult bindingResult,
                                                            AuthenticatedUserAccount user,
                                                            RedirectAttributes redirectAttributes) {
    checkUserPrivilege(user);
    var validatedBindingResult = termsAndConditionsService.validateForm(form, bindingResult);

    return controllerHelperService.checkErrorsAndRedirect(validatedBindingResult,
        getTermsAndConditionsVariationModelAndView(), () -> {
          termsAndConditionsService.saveForm(form, user.getLinkedPerson());
          FlashUtils.success(
              redirectAttributes,
              "Added new record for terms and conditions"
          );
          return RouteUtils.redirectWorkArea();
        });
  }

  private ModelAndView getTermsAndConditionsVariationModelAndView() {
    return new ModelAndView("termsandconditions/termsAndConditionsForm")
        .addObject("cancelUrl", ReverseRouter.route(on(TermsAndConditionsManagementController.class)
            .renderTermsAndConditionsManagement(null, null, null, null)))
        .addObject("pwaSelectorOptions", termsAndConditionsService.getPwasForSelector());
  }

  private void checkUserPrivilege(AuthenticatedUserAccount authenticatedUser) {
    if (!authenticatedUser.hasPrivilege(PwaUserPrivilege.PWA_MANAGER)) {
      throw new AccessDeniedException("Access to terms and conditions denied");
    }
  }
}
