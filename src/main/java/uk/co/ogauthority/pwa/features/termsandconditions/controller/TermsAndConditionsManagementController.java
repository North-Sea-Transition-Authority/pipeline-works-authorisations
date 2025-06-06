package uk.co.ogauthority.pwa.features.termsandconditions.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.HasAnyRole;
import uk.co.ogauthority.pwa.features.termsandconditions.model.TermsAndConditionsFilterForm;
import uk.co.ogauthority.pwa.features.termsandconditions.service.TermsAndConditionsService;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.objects.FormObjectMapper;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamType;

@Controller
@RequestMapping("/terms-and-conditions")
@HasAnyRole(teamType = TeamType.REGULATOR, roles = {Role.PWA_MANAGER})
public class TermsAndConditionsManagementController {

  private final TermsAndConditionsService termsAndConditionsService;

  public TermsAndConditionsManagementController(TermsAndConditionsService termsAndConditionsService) {
    this.termsAndConditionsService = termsAndConditionsService;
  }

  @GetMapping
  public ModelAndView renderTermsAndConditionsManagement(@ModelAttribute("form") TermsAndConditionsFilterForm form,
                                                         @RequestParam(defaultValue = "0", name = "page") Integer page,
                                                         AuthenticatedUserAccount user) {
    return new ModelAndView("termsandconditions/termsAndConditionsManagement")
        .addObject("termsAndConditionsPageView",
            termsAndConditionsService.getPwaManagementScreenPageView(page, form.getPwaReference() != null ? form.getPwaReference() : ""))
        .addObject("termsAndConditionsFormUrl", ReverseRouter.route(on(TermsAndConditionsFormController.class)
            .renderNewTermsAndConditionsForm(null, user)))
        .addObject("clearFilterUrl", ReverseRouter.route(on(TermsAndConditionsManagementController.class)
            .renderTermsAndConditionsManagement(new TermsAndConditionsFilterForm(), null, null)))
        .addObject("form", form);
  }

  @PostMapping
  public ModelAndView filterTermsAndConditions(@ModelAttribute("form") TermsAndConditionsFilterForm form,
                                       BindingResult bindingResult,
                                       AuthenticatedUserAccount user) {

    var paramMap = new LinkedMultiValueMap<String, String>();
    paramMap.setAll(FormObjectMapper.toMap(form));

    return ReverseRouter.redirectWithQueryParamMap(on(TermsAndConditionsManagementController.class)
       .renderTermsAndConditionsManagement(form, null, null), paramMap);
  }

}
