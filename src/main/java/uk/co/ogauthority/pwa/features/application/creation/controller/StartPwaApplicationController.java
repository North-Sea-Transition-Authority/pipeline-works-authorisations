package uk.co.ogauthority.pwa.features.application.creation.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.model.form.pwaapplications.start.StartPwaApplicationForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.util.EnumUtils;
import uk.co.ogauthority.pwa.util.converters.ResourceTypeUrl;

@Controller
@RequestMapping("/start-application/{resourceType}")
public class StartPwaApplicationController {

  private final PwaApplicationRedirectService pwaApplicationRedirectService;

  private final String contactEmail;
  private final ControllerHelperService controllerHelperService;

  @Autowired
  public StartPwaApplicationController(Environment environment,
                                       PwaApplicationRedirectService pwaApplicationRedirectService,
                                       ControllerHelperService controllerHelperService) {
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.contactEmail = environment.getProperty("app.support.email");
    this.controllerHelperService = controllerHelperService;
  }

  /**
   * Second page in PWA application journey, following resource type.
   * @return screen to select application type
   */
  @GetMapping
  public ModelAndView renderStartApplication(@ModelAttribute("form") StartPwaApplicationForm form,
                                             @PathVariable @ResourceTypeUrl PwaResourceType resourceType) {
    return getStartAppModelAndView(resourceType);
  }

  private ModelAndView getStartAppModelAndView(PwaResourceType pwaResourceType) {
    var applicationTypes = pwaResourceType.getPermittedApplicationTypes().stream()
        .sorted(Comparator.comparing(PwaApplicationType::getDisplayOrder))
        .collect(Collectors.toList());

    var initialGuideText = pwaResourceType == PwaResourceType.HYDROGEN
        ? "All new applications irrespective of pipeline lengths. "
        : "All new fields irrespective of pipeline lengths. ";

    return new ModelAndView("pwaApplication/selectApplication")
      .addObject("contactEmail", contactEmail)
      .addObject("workAreaUrl", ReverseRouter.route(on(WorkAreaController.class).renderWorkArea(null, null, null)))
      .addObject("applicationTypes", applicationTypes)
      .addObject("errorList", List.of())
      .addObject("initialGuideText", initialGuideText);
  }

  /**
   * Handles posting selection of application type.
   * @return application type screen if validation errors, relevant application type start page if not
   */
  @PostMapping
  public ModelAndView startApplication(@Valid @ModelAttribute("form") StartPwaApplicationForm form,
                                       BindingResult bindingResult,
                                       @PathVariable @ResourceTypeUrl PwaResourceType resourceType) {

    return controllerHelperService.checkErrorsAndRedirect(bindingResult, getStartAppModelAndView(resourceType), () ->
        pwaApplicationRedirectService.getStartApplicationRedirect(
            EnumUtils.getEnumValue(PwaApplicationType.class, form.getApplicationType()), resourceType));
  }

}
