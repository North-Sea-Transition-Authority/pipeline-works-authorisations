package uk.co.ogauthority.pwa.controller.pwaapplications.start;

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
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.util.ControllerUtils;
import uk.co.ogauthority.pwa.util.EnumUtils;
import uk.co.ogauthority.pwa.util.StreamUtils;

@Controller
@RequestMapping("/start-application")
public class StartPwaApplicationController {

  private final PwaApplicationRedirectService pwaApplicationRedirectService;

  @Autowired
  public StartPwaApplicationController(PwaApplicationRedirectService pwaApplicationRedirectService) {
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
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
    return new ModelAndView("pwaApplication/selectApplication")
      .addObject("workAreaUrl", ReverseRouter.route(on(WorkAreaController.class).renderWorkArea()))
      .addObject("applicationTypes", Arrays.stream(PwaApplicationType.values())
        .collect(StreamUtils.toLinkedHashMap(Enum::name, PwaApplicationType::getDisplayName)))
      .addObject("errorList", List.of());
  }

  /**
   * Handles posting selection of application type.
   * @return application type screen if validation errors, relevant application type start page if not
   */
  @PostMapping
  public ModelAndView startApplication(@Valid @ModelAttribute("form") StartPwaApplicationForm form,
                                       BindingResult bindingResult) {

    return ControllerUtils.validateAndRedirect(bindingResult, getStartAppModelAndView(), () ->
        pwaApplicationRedirectService.getStartApplicationRedirect(
            EnumUtils.getEnumValue(PwaApplicationType.class, form.getApplicationType())));

  }

}
