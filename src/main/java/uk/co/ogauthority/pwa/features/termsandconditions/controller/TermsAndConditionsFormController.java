package uk.co.ogauthority.pwa.features.termsandconditions.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.HasAnyRole;
import uk.co.ogauthority.pwa.features.termsandconditions.model.TermsAndConditionsForm;
import uk.co.ogauthority.pwa.features.termsandconditions.service.TermsAndConditionsService;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaService;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamType;
import uk.co.ogauthority.pwa.util.FlashUtils;

@Controller
@RequestMapping("/terms-and-conditions")
@HasAnyRole(teamType = TeamType.REGULATOR, roles = {Role.PWA_MANAGER})
public class TermsAndConditionsFormController {

  private final TermsAndConditionsService termsAndConditionsService;
  private final ControllerHelperService controllerHelperService;
  private final MasterPwaService masterPwaService;

  public TermsAndConditionsFormController(
      TermsAndConditionsService termsAndConditionsService,
      ControllerHelperService controllerHelperService, MasterPwaService masterPwaService) {
    this.termsAndConditionsService = termsAndConditionsService;
    this.controllerHelperService = controllerHelperService;
    this.masterPwaService = masterPwaService;
  }

  @GetMapping("/new")
  public ModelAndView renderNewTermsAndConditionsForm(@ModelAttribute("form") TermsAndConditionsForm form,
                                                   AuthenticatedUserAccount user) {

    return getTermsAndConditionsVariationModelAndView(null);
  }

  @GetMapping("/edit/{pwaId}")
  public ModelAndView renderEditTermsAndConditionsForm(@ModelAttribute("form") TermsAndConditionsForm form,
                                                   @PathVariable(required = false) Integer pwaId,
                                                   AuthenticatedUserAccount user) {

    return getTermsAndConditionsVariationModelAndView(pwaId)
        .addObject("form", termsAndConditionsService.getTermsAndConditionsForm(pwaId));
  }

  @PostMapping({"/new", "/edit/{pwaId}"})
  public ModelAndView submitTermsAndConditionsForm(@ModelAttribute("form") TermsAndConditionsForm form,
                                                   BindingResult bindingResult,
                                                   @PathVariable(required = false) Integer pwaId,
                                                   AuthenticatedUserAccount user,
                                                   RedirectAttributes redirectAttributes) {

    var validatedBindingResult = termsAndConditionsService.validateForm(form, bindingResult);

    String successFlashMessage = pwaId != null ? "Updated terms and conditions record"
                                               : "Added new terms and conditions record";

    return controllerHelperService.checkErrorsAndRedirect(validatedBindingResult,
        getTermsAndConditionsVariationModelAndView(pwaId), () -> {
          termsAndConditionsService.saveForm(form, user.getLinkedPerson());
          FlashUtils.success(
              redirectAttributes,
              successFlashMessage
          );
          return ReverseRouter.redirect(on(TermsAndConditionsManagementController.class)
              .renderTermsAndConditionsManagement(null, null, null));
        });
  }

  private ModelAndView getTermsAndConditionsVariationModelAndView(Integer pwaId) {
    String pageTitle = "Submit terms and conditions for a PWA";
    boolean existingRecord = false;

    if (pwaId != null) {
      String reference = masterPwaService.getCurrentDetailOrThrow(masterPwaService.getMasterPwaById(pwaId)).getReference();
      pageTitle = String.format("Update terms and conditions for PWA %s", reference);
      existingRecord = true;
    }

    return new ModelAndView("termsandconditions/termsAndConditionsForm")
        .addObject("cancelUrl", ReverseRouter.route(on(TermsAndConditionsManagementController.class)
            .renderTermsAndConditionsManagement(null, null, null)))
        .addObject("pwaSelectorOptions", termsAndConditionsService.getPwasForSelector())
        .addObject("pageTitle", pageTitle)
        .addObject("existingRecord", existingRecord);
  }
}
