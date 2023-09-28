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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.model.form.pwaapplications.PwaResourceTypeForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.PwaResourceTypeFormValidator;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;

@Controller
@RequestMapping("/start-application/resource")
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
  public ModelAndView renderResourceTypeForm(@ModelAttribute("form") PwaResourceTypeForm form,
                                             AuthenticatedUserAccount user) {
    var resourceOptions = Arrays.stream(PwaResourceType.values())
        .sorted(Comparator.comparingInt(PwaResourceType::getDisplayOrder))
        .collect(Collectors.toList());

    return new ModelAndView("pwaApplication/form/resourceType")
        .addObject("resourceOptions", resourceOptions)
        .addObject("workareaUrl", ReverseRouter.route(on(WorkAreaController.class).renderWorkArea(null, null, null)));
  }

  /**
   * Record resource type for PWA creation.
   * @param form the PwaResourceType form.
   * @return the next step in PWA creation.
   */
  @PostMapping
  public ModelAndView postResourceType(@ModelAttribute("form") PwaResourceTypeForm form,
                                       BindingResult bindingResult,
                                       AuthenticatedUserAccount user) {
    validator.validate(form, bindingResult);
    return controllerHelperService.checkErrorsAndRedirect(
        bindingResult,
        renderResourceTypeForm(form, user),
        () -> redirectService.getStartApplicationRedirect(form.getResourceType()));
  }

}
