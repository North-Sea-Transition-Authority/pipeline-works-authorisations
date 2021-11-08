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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.model.form.pwaapplications.start.StartPwaApplicationForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.util.EnumUtils;

@Controller
@RequestMapping("/start-application")
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
   * First page in PWA application journey.
   * @return screen to select application type
   */
  @GetMapping
  public ModelAndView renderStartApplication(@ModelAttribute("form") StartPwaApplicationForm form) {
    return getStartAppModelAndView();
  }

  private ModelAndView getStartAppModelAndView() {
    var applicationTypes = PwaApplicationType.stream()
        .sorted(Comparator.comparing(PwaApplicationType::getDisplayOrder))
        .collect(Collectors.toList());

    return new ModelAndView("pwaApplication/selectApplication")
      .addObject("contactEmail", contactEmail)
      .addObject("workAreaUrl", ReverseRouter.route(on(WorkAreaController.class).renderWorkArea(null, null, null)))
      .addObject("applicationTypes", applicationTypes)
      .addObject("errorList", List.of());
  }

  /**
   * Handles posting selection of application type.
   * @return application type screen if validation errors, relevant application type start page if not
   */
  @PostMapping
  public ModelAndView startApplication(@Valid @ModelAttribute("form") StartPwaApplicationForm form,
                                       BindingResult bindingResult) {

    return controllerHelperService.checkErrorsAndRedirect(bindingResult, getStartAppModelAndView(), () ->
        pwaApplicationRedirectService.getStartApplicationRedirect(
            EnumUtils.getEnumValue(PwaApplicationType.class, form.getApplicationType())));

  }

}
