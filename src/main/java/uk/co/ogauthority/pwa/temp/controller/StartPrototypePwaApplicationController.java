package uk.co.ogauthority.pwa.temp.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Arrays;
import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.model.form.pwaapplications.start.StartPwaApplicationForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.util.EnumUtils;
import uk.co.ogauthority.pwa.util.StreamUtils;

@Controller
@RequestMapping("/prototype/start-application")
public class StartPrototypePwaApplicationController {

  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final ControllerHelperService controllerHelperService;

  @Autowired
  public StartPrototypePwaApplicationController(PwaApplicationRedirectService pwaApplicationRedirectService,
                                                ControllerHelperService controllerHelperService) {
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
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
    return new ModelAndView("pwaApplication/temporary/selectApplication")
      .addObject("workAreaUrl", ReverseRouter.route(on(WorkAreaController.class).renderWorkArea(null, null, null)))
      .addObject("applicationTypes", Arrays.stream(PwaApplicationType.values())
        .collect(StreamUtils.toLinkedHashMap(Enum::name, PwaApplicationType::getDisplayName)))
      .addObject("errorList", List.of());
  }

  /**
   * Handles posting selection of application type.
   *
   * @return application type screen if validation errors, relevant application type start page if not
   */
  @PostMapping
  public ModelAndView startApplication(@Valid @ModelAttribute("form") StartPwaApplicationForm form,
                                       BindingResult bindingResult) {

    return controllerHelperService.checkErrorsAndRedirect(bindingResult, getStartAppModelAndView(), () -> {
          var applicationType = EnumUtils.getEnumValue(PrototypeApplicationType.class, form.getApplicationType());
          switch (applicationType) {
            case INITIAL:
              return ReverseRouter.redirect(on(StartPrototypeInitialPwaController.class).renderStartPage());
            case CAT_1_VARIATION:
            case CAT_2_VARIATION:
            case DECOMMISSIONING:
            case DEPOSIT_CONSENT:
            case HUOO_VARIATION:
            case OPTIONS_VARIATION:
            default:
              return null;
          }
        }
    );
  }

}
