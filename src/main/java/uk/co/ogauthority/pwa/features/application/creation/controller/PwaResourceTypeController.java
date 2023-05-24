package uk.co.ogauthority.pwa.features.application.creation.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.model.form.pwaapplications.PwaResourceTypeForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

@Controller
@RequestMapping("/pwa-application/initial/new/resource")
public class PwaResourceTypeController {


  /**
   * Render of start page for initial PWA application.
   */
  @GetMapping
  public ModelAndView renderResourceTypeForm(@ModelAttribute("form") PwaResourceTypeForm form) {
    Map<String, String> resourceOptionsMap = Arrays.stream(PwaResourceType.values())
        .collect(Collectors.toMap(PwaResourceType::name, resourceType -> "PWA - " + resourceType.getDisplayName()));

    return new ModelAndView("pwaApplication/form/resourceType")
        .addObject("resourceOptionsMap", resourceOptionsMap)
        .addObject("workareaUrl", ReverseRouter.route(on(WorkAreaController.class).renderWorkArea(null, null, null)));
  }

  /**
   * Create initial PWA application and redirect to first task.
   */
  @PostMapping
  public ModelAndView postResourceType(@ModelAttribute("form") PwaResourceTypeForm form) {
    return ReverseRouter.redirect(on(StartInitialPwaController.class).renderStartPage(form.getResourceType()));
  }

}
