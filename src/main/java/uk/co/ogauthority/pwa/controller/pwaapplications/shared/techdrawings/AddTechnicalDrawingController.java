package uk.co.ogauthority.pwa.controller.pwaapplications.shared.techdrawings;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.techdetails.TechnicalDrawingForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/technical-drawings/pipeline-drawings")
@PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION
})
public class AddTechnicalDrawingController {

  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final PadPipelineService padPipelineService;

  @Autowired
  public AddTechnicalDrawingController(
      ApplicationBreadcrumbService applicationBreadcrumbService,
      PadPipelineService padPipelineService) {
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.padPipelineService = padPipelineService;
  }

  private ModelAndView getDrawingModelAndView(PwaApplicationDetail detail) {
    var modelAndView = new ModelAndView("pwaApplication/shared/techdrawings/addPipelineDrawing")
        .addObject("pipelineViews", padPipelineService.getPipelineOverviews(detail))
        .addObject("backUrl", "#");
    applicationBreadcrumbService.fromTechnicalDrawings(detail.getPwaApplication(), modelAndView,
        "Add pipeline drawing");
    return modelAndView;
  }

  @GetMapping("/new")
  public ModelAndView renderAddDrawing(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @ModelAttribute("form") TechnicalDrawingForm form,
      PwaApplicationContext applicationContext) {

    return getDrawingModelAndView(applicationContext.getApplicationDetail());
  }

  @PostMapping("/new")
  public ModelAndView postAddDrawing(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @ModelAttribute("form") TechnicalDrawingForm form,
      PwaApplicationContext applicationContext) {

    return getDrawingModelAndView(applicationContext.getApplicationDetail());
  }

}
