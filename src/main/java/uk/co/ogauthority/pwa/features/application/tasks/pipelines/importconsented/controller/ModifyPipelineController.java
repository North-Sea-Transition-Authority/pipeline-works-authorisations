package uk.co.ogauthority.pwa.features.application.tasks.pipelines.importconsented.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.NamedPipeline;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.importconsented.ModifyPipelineForm;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.importconsented.ModifyPipelineService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.importconsented.ModifyPipelineValidator;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.tasklist.controller.PipelinesTaskListController;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.transfers.PadPipelineTransferService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.util.FlashUtils;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/pipelines/consented")
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.OPTIONS_VARIATION,
    PwaApplicationType.DECOMMISSIONING
})
@PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
@PwaApplicationPermissionCheck(permissions = PwaApplicationPermission.EDIT)
public class ModifyPipelineController {

  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final ModifyPipelineService modifyPipelineService;
  private final ModifyPipelineValidator modifyPipelineValidator;
  private final ControllerHelperService controllerHelperService;

  private final PadPipelineTransferService transferService;

  public ModifyPipelineController(
      ApplicationBreadcrumbService applicationBreadcrumbService,
      ModifyPipelineService modifyPipelineService,
      ModifyPipelineValidator modifyPipelineValidator,
      ControllerHelperService controllerHelperService, PadPipelineTransferService transferService) {
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.modifyPipelineService = modifyPipelineService;
    this.modifyPipelineValidator = modifyPipelineValidator;
    this.controllerHelperService = controllerHelperService;
    this.transferService = transferService;
  }

  private ModelAndView createImportConsentedPipelineModelAndView(PwaApplicationDetail detail) {
    var selectablePipelines = modifyPipelineService.getSelectableConsentedPipelines(detail);
    var pipelineSelection = selectablePipelines.stream()
        .collect(StreamUtils.toLinkedHashMap(named -> String.valueOf(named.getPipelineId()),
            NamedPipeline::getPipelineName));
    var modelAndView = new ModelAndView("pwaApplication/shared/pipelines/importConsented")
        .addObject("consentedPipelines", pipelineSelection)
        .addObject("cancelUrl", ReverseRouter.route(on(PipelinesTaskListController.class)
            .renderPipelinesOverview(detail.getMasterPwaApplicationId(), detail.getPwaApplicationType(), null, null)))
        .addObject("serviceStatuses", modifyPipelineService.getPipelineServiceStatusesForAppType(detail.getPwaApplicationType()));
    applicationBreadcrumbService.fromPipelinesOverview(detail.getPwaApplication(), modelAndView,
        "Modify consented pipeline");
    return modelAndView;
  }

  @GetMapping
  public ModelAndView renderImportConsentedPipeline(@PathVariable("applicationId") Integer applicationId,
                                                    @PathVariable("applicationType")
                                                    @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                    PwaApplicationContext applicationContext,
                                                    @ModelAttribute("form") ModifyPipelineForm form) {
    return createImportConsentedPipelineModelAndView(applicationContext.getApplicationDetail());
  }

  @PostMapping
  public ModelAndView postImportConsentedPipeline(@PathVariable("applicationId") Integer applicationId,
                                                  @PathVariable("applicationType")
                                                  @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                  PwaApplicationContext applicationContext,
                                                  @ModelAttribute("form") ModifyPipelineForm form,
                                                  BindingResult bindingResult,
                                                  RedirectAttributes redirectAttributes) {
    var detail = applicationContext.getApplicationDetail();
    modifyPipelineValidator.validate(form, bindingResult, detail);
    return controllerHelperService.checkErrorsAndRedirect(bindingResult,
        createImportConsentedPipelineModelAndView(detail),
        () -> {
          var importedPipeline = modifyPipelineService.importPipeline(detail, form);
          if (form.getPipelineStatus().equals(PipelineStatus.TRANSFERRED)) {
            transferService.transferOut(importedPipeline, importedPipeline.getPwaApplicationDetail());
          }
          FlashUtils.success(
              redirectAttributes, "Success", "The pipeline to be modified has been added to the pipelines listed below");
          return ReverseRouter.redirect(on(PipelinesTaskListController.class)
              .renderPipelinesOverview(applicationId, pwaApplicationType, null, null));
        });
  }
}
