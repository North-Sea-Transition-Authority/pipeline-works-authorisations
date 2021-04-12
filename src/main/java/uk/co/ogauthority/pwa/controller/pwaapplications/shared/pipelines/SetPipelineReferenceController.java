package uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelines;

import java.util.function.Supplier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.tasklist.RegulatorPipelineReferenceTaskService;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/pipelines/{padPipelineId}/set-reference")
@PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
@PwaApplicationPermissionCheck(permissions = PwaApplicationPermission.SET_PIPELINE_REFERENCE)
public class SetPipelineReferenceController {

  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final ControllerHelperService controllerHelperService;
  private final RegulatorPipelineReferenceTaskService regulatorPipelineReferenceTaskService;
  private final PadPipelineService padPipelineService;

  public SetPipelineReferenceController(
      ApplicationBreadcrumbService applicationBreadcrumbService,
      ControllerHelperService controllerHelperService,
      RegulatorPipelineReferenceTaskService regulatorPipelineReferenceTaskService,
      PadPipelineService padPipelineService) {
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.controllerHelperService = controllerHelperService;
    this.regulatorPipelineReferenceTaskService = regulatorPipelineReferenceTaskService;
    this.padPipelineService = padPipelineService;
  }

  @GetMapping
  public ModelAndView renderSetPipelineReference(@PathVariable("applicationType")
                                                 @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                 @PathVariable("applicationId") Integer applicationId,
                                                 @PathVariable("padPipelineId") Integer padPipelineId,
                                                 PwaApplicationContext applicationContext) {

    return whenPipelineCanHaveReferenceSet(applicationContext, () -> {
      var pipelineOverview = padPipelineService.getPipelineOverview(applicationContext.getPadPipeline());

      var modelAndView = new ModelAndView("pwaApplication/shared/pipelines/setPipelineReference")
          .addObject("pipelineOverview", pipelineOverview);

      applicationBreadcrumbService.fromPipelinesOverview(
          applicationContext.getPwaApplication(), modelAndView, "Set pipeline reference");

      return modelAndView;
    });

  }

  private ModelAndView whenPipelineCanHaveReferenceSet(PwaApplicationContext applicationContext, Supplier<ModelAndView> modelAndViewSupplier) {

    if (regulatorPipelineReferenceTaskService.pipelineTaskAccessible(
        applicationContext.getPermissions(), applicationContext.getPadPipeline())) {
      return modelAndViewSupplier.get();
    }
     throw new AccessDeniedException("Not allowed to access set pipeline reference task. " + applicationContext.toString());

  }

}
