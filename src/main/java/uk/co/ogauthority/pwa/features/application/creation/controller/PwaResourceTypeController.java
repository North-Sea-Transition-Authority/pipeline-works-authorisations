package uk.co.ogauthority.pwa.features.application.creation.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.model.form.pwaapplications.PwaResourceTypeForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.PwaResourceTypeFormValidator;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/new/resource")
public class PwaResourceTypeController {

  private final PwaResourceTypeFormValidator validator;

  private final ControllerHelperService controllerHelperService;

  private final PwaApplicationRedirectService redirectService;

  @Autowired
  public PwaResourceTypeController(PwaResourceTypeFormValidator validator,
                                   ControllerHelperService controllerHelperService,
                                   PwaApplicationRedirectService redirectService) {
    this.validator = validator;
    this.controllerHelperService = controllerHelperService;
    this.redirectService = redirectService;
  }

  /**
   * Render Resource Type form page.
   * @param form the PwaResourceType form.
   * @return the form page.
   */
  @GetMapping
  public ModelAndView renderResourceTypeForm(@PathVariable @ApplicationTypeUrl PwaApplicationType applicationType,
                                             @ModelAttribute("form") PwaResourceTypeForm form,
                                             AuthenticatedUserAccount user) {
    var resourceOptions = Arrays.stream(PwaResourceType.values())
        .sorted(Comparator.comparingInt(PwaResourceType::getDisplayOrder))
        .collect(Collectors.toList());

    return new ModelAndView("pwaApplication/form/resourceType")
        .addObject("resourceOptions", resourceOptions)
        .addObject("workareaUrl", ReverseRouter.route(on(WorkAreaController.class).renderWorkArea(null, null, null)));
  }

  /**
   * Record application type and resource type for PWA creation.
   * @param applicationType the pwa application type.
   * @param form the PwaResourceType form.
   * @return the next step in PWA creation.
   */
  @PostMapping
  public ModelAndView postResourceType(@PathVariable @ApplicationTypeUrl PwaApplicationType applicationType,
                                       @ModelAttribute("form") PwaResourceTypeForm form,
                                       BindingResult bindingResult,
                                       AuthenticatedUserAccount user) {
    validator.validate(form, bindingResult);
    return controllerHelperService.checkErrorsAndRedirect(
        bindingResult,
        renderResourceTypeForm(applicationType, form, user),
        () -> redirectService.getStartApplicationRedirect(applicationType, form.getResourceType()));
  }

}
