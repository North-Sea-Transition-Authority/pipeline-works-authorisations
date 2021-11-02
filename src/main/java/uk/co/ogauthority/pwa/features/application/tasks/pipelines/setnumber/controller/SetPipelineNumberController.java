package uk.co.ogauthority.pwa.features.application.tasks.pipelines.setnumber.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.function.Supplier;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.setnumber.RegulatorPipelineNumberTaskService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.setnumber.SetPipelineNumberForm;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.tasklist.controller.PipelinesTaskListController;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/pipelines/{padPipelineId}/set-number")
@PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
@PwaApplicationPermissionCheck(permissions = PwaApplicationPermission.SET_PIPELINE_REFERENCE)
public class SetPipelineNumberController {

  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final ControllerHelperService controllerHelperService;
  private final RegulatorPipelineNumberTaskService regulatorPipelineNumberTaskService;
  private final PadPipelineService padPipelineService;

  public SetPipelineNumberController(
      ApplicationBreadcrumbService applicationBreadcrumbService,
      ControllerHelperService controllerHelperService,
      RegulatorPipelineNumberTaskService regulatorPipelineNumberTaskService,
      PadPipelineService padPipelineService) {
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.controllerHelperService = controllerHelperService;
    this.regulatorPipelineNumberTaskService = regulatorPipelineNumberTaskService;
    this.padPipelineService = padPipelineService;
  }

  @GetMapping
  public ModelAndView renderSetPipelineNumber(@PathVariable("applicationType")
                                              @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                              @PathVariable("applicationId") Integer applicationId,
                                              @PathVariable("padPipelineId") Integer padPipelineId,
                                              PwaApplicationContext applicationContext,
                                              @ModelAttribute("form") SetPipelineNumberForm form) {

    return whenPipelineCanHaveReferenceSet(applicationContext, () -> getSetReferenceModelAndView(applicationContext));

  }

  private ModelAndView getSetReferenceModelAndView(PwaApplicationContext applicationContext) {
    var pipelineOverview = padPipelineService.getPipelineOverview(applicationContext.getPadPipeline());

    var pipelineNumberRange = regulatorPipelineNumberTaskService.getPermittedPipelineNumberRange();
    var modelAndView = new ModelAndView("pwaApplication/shared/pipelines/setPipelineNumber")
        .addObject("pipelineOverview", pipelineOverview)
        .addObject("minNumber", pipelineNumberRange.getMinimum())
        .addObject("maxNumber", pipelineNumberRange.getMaximum());

    applicationBreadcrumbService.fromPipelinesOverview(
        applicationContext.getPwaApplication(), modelAndView, "Set pipeline number");

    return modelAndView;

  }

  @PostMapping
  public ModelAndView setPipelineReference(@PathVariable("applicationType")
                                           @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                           @PathVariable("applicationId") Integer applicationId,
                                           @PathVariable("padPipelineId") Integer padPipelineId,
                                           PwaApplicationContext applicationContext,
                                           @ModelAttribute("form") SetPipelineNumberForm form,
                                           BindingResult bindingResult) {

    return whenPipelineCanHaveReferenceSet(applicationContext, () -> {
      regulatorPipelineNumberTaskService.validateForm(applicationContext.getPadPipeline(), form, bindingResult);

      return controllerHelperService.checkErrorsAndRedirect(
          bindingResult,
          getSetReferenceModelAndView(applicationContext),
          () -> {
            regulatorPipelineNumberTaskService.setPipelineNumber(applicationContext.getPadPipeline(), form.getPipelineNumber());
            return ReverseRouter.redirect(on(PipelinesTaskListController.class)
                .renderPipelinesOverview(applicationId, pwaApplicationType, null));
          });
    });

  }

  private ModelAndView whenPipelineCanHaveReferenceSet(PwaApplicationContext applicationContext,
                                                       Supplier<ModelAndView> modelAndViewSupplier) {

    if (regulatorPipelineNumberTaskService.pipelineTaskAccessible(
        applicationContext.getPermissions(), applicationContext.getPadPipeline())) {
      return modelAndViewSupplier.get();
    }
    throw new AccessDeniedException(
        "Not allowed to access set pipeline reference task. " + applicationContext.toString());

  }

}
