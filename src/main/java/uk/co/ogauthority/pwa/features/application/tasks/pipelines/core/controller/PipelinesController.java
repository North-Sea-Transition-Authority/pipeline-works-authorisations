package uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
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
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineFlexibility;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineMaterial;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineType;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PipelineControllerRouteUtils;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PipelineHeaderForm;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PipelineHeaderFormValidator;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PipelineHeaderService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PipelineRemovalService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.tasklist.controller.PipelinesTaskListController;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.LongitudeDirection;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.enums.ScreenActionType;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.searchselector.SearchSelectorService;
import uk.co.ogauthority.pwa.util.FlashUtils;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/pipelines")
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.DECOMMISSIONING,
    PwaApplicationType.OPTIONS_VARIATION
})
@PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
@PwaApplicationPermissionCheck(permissions = PwaApplicationPermission.EDIT)
public class PipelinesController {

  private final PadPipelineService padPipelineService;
  private final PipelineRemovalService pipelineRemovalService;

  private final ApplicationBreadcrumbService breadcrumbService;
  private final PipelineHeaderFormValidator pipelineHeaderFormValidator;
  private final ControllerHelperService controllerHelperService;
  private final PipelineHeaderService pipelineHeaderService;

  @Autowired
  public PipelinesController(PadPipelineService padPipelineService,
                             PipelineRemovalService pipelineRemovalService,
                             ApplicationBreadcrumbService breadcrumbService,
                             PipelineHeaderFormValidator pipelineHeaderFormValidator,
                             ControllerHelperService controllerHelperService,
                             PipelineHeaderService pipelineHeaderService) {
    this.padPipelineService = padPipelineService;
    this.pipelineRemovalService = pipelineRemovalService;
    this.breadcrumbService = breadcrumbService;
    this.pipelineHeaderFormValidator = pipelineHeaderFormValidator;
    this.controllerHelperService = controllerHelperService;
    this.pipelineHeaderService = pipelineHeaderService;
  }

  private ModelAndView getRemovePipelineModelAndView(PwaApplicationDetail detail, PadPipeline padPipeline) {
    var modelAndView = new ModelAndView("pwaApplication/shared/pipelines/removePipeline")
        .addObject("pipeline", padPipelineService.getPipelineOverview(padPipeline))
        .addObject("backUrl", ReverseRouter.route(on(PipelinesTaskListController.class)
            .renderPipelinesOverview(detail.getMasterPwaApplicationId(), detail.getPwaApplicationType(), null, null)));
    breadcrumbService.fromPipelinesOverview(detail.getPwaApplication(), modelAndView, "Remove pipeline");
    return modelAndView;
  }

  private ModelAndView getAddEditPipelineModelAndView(PwaApplicationDetail detail, ScreenActionType type,
                                                      PadPipeline padPipeline) {

    var modelAndView = new ModelAndView("pwaApplication/shared/pipelines/addEditPipeline")
        .addObject("pipelineTypes", PipelineType.streamDisplayValues()
            .collect(StreamUtils.toLinkedHashMap(Enum::name, PipelineType::getDisplayName)))
        .addObject("longDirections", LongitudeDirection.stream()
            .collect(StreamUtils.toLinkedHashMap(Enum::name, LongitudeDirection::getDisplayText)))
        .addObject("cancelUrl", ReverseRouter.route(on(PipelinesTaskListController.class)
            .renderPipelinesOverview(detail.getMasterPwaApplicationId(), detail.getPwaApplicationType(), null, null)))
        .addObject("screenActionType", type)
        .addObject("pipelineFlexibilityTypes", PipelineFlexibility.asList())
        .addObject("pipelineMaterialTypes", PipelineMaterial.asList())
        .addObject("bundleNameRestUrl", SearchSelectorService.route(on(PipelineRestController.class)
            .searchBundleNames(detail.getMasterPwaApplicationId(), null, null)))
        .addObject("requiredQuestions",
            pipelineHeaderService.getRequiredQuestions(padPipeline, detail.getPwaApplicationType()));

    breadcrumbService.fromPipelinesOverview(detail.getPwaApplication(), modelAndView,
        type.getSubmitButtonText() + " pipeline");

    if (padPipeline != null) {
      modelAndView.addObject("pipelineNumber", padPipeline.getPipelineRef());
    }

    return modelAndView;
  }

  @GetMapping("/add-pipeline")
  public ModelAndView renderAddPipeline(@PathVariable("applicationId") Integer applicationId,
                                        @PathVariable("applicationType")
                                        @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                        PwaApplicationContext applicationContext,
                                        @ModelAttribute("form") PipelineHeaderForm form) {
    return getAddEditPipelineModelAndView(applicationContext.getApplicationDetail(), ScreenActionType.ADD, null);
  }

  @PostMapping("/add-pipeline")
  public ModelAndView postAddPipeline(@PathVariable("applicationId") Integer applicationId,
                                      @PathVariable("applicationType")
                                      @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                      PwaApplicationContext applicationContext,
                                      @ModelAttribute("form") PipelineHeaderForm form,
                                      BindingResult bindingResult) {

    var validationHints = pipelineHeaderService.getValidationHints(null, pwaApplicationType);
    pipelineHeaderFormValidator.validate(form, bindingResult, validationHints);

    return controllerHelperService.checkErrorsAndRedirect(bindingResult,
        getAddEditPipelineModelAndView(applicationContext.getApplicationDetail(), ScreenActionType.ADD, null), () -> {

          padPipelineService.addPipeline(applicationContext.getApplicationDetail(), form, validationHints.getRequiredQuestions());

          return ReverseRouter.redirect(
              on(PipelinesTaskListController.class).renderPipelinesOverview(applicationId, pwaApplicationType, null, null));

        });

  }

  @GetMapping("/pipeline/{padPipelineId}")
  public ModelAndView renderEditPipeline(@PathVariable("applicationId") Integer applicationId,
                                         @PathVariable("applicationType")
                                         @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                         @PathVariable("padPipelineId") Integer padPipelineId,
                                         PwaApplicationContext applicationContext,
                                         @ModelAttribute("form") PipelineHeaderForm form,
                                         RedirectAttributes redirectAttributes) {

    return PipelineControllerRouteUtils.ifAllowedFromOverviewOrRedirect(applicationContext, redirectAttributes,
        () -> {
          padPipelineService.mapEntityToForm(form, applicationContext.getPadPipeline());
          return getAddEditPipelineModelAndView(applicationContext.getApplicationDetail(), ScreenActionType.EDIT,
              applicationContext.getPadPipeline())
              .addObject("form", form);
        });

  }

  @PostMapping("/pipeline/{padPipelineId}")
  public ModelAndView postEditPipeline(@PathVariable("applicationId") Integer applicationId,
                                       @PathVariable("applicationType")
                                       @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                       @PathVariable("padPipelineId") Integer padPipelineId,
                                       PwaApplicationContext applicationContext,
                                       @ModelAttribute("form") PipelineHeaderForm form,
                                       BindingResult bindingResult) {

    return PipelineControllerRouteUtils.ifAllowedFromOverviewOrError(applicationContext, () -> {

      var padPipeline = applicationContext.getPadPipeline();

      var validationHints = pipelineHeaderService.getValidationHints(padPipeline, pwaApplicationType);
      pipelineHeaderFormValidator.validate(form, bindingResult, validationHints);

      return controllerHelperService.checkErrorsAndRedirect(bindingResult,
          getAddEditPipelineModelAndView(applicationContext.getApplicationDetail(), ScreenActionType.EDIT, padPipeline),
          () -> {

            padPipelineService.updatePipeline(padPipeline, form, validationHints.getRequiredQuestions());

            return ReverseRouter.redirect(on(PipelinesTaskListController.class)
                .renderPipelinesOverview(applicationId, pwaApplicationType, null, null));

          });

    });

  }

  @GetMapping("/pipeline/{padPipelineId}/remove")
  public ModelAndView renderRemovePipeline(@PathVariable("applicationId") Integer applicationId,
                                           @PathVariable("applicationType")
                                           @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                           @PathVariable("padPipelineId") Integer padPipelineId,
                                           PwaApplicationContext applicationContext) {
    return getRemovePipelineModelAndView(applicationContext.getApplicationDetail(),
        applicationContext.getPadPipeline());
  }

  @PostMapping("/pipeline/{padPipelineId}/remove")
  public ModelAndView postRemovePipeline(@PathVariable("applicationId") Integer applicationId,
                                         @PathVariable("applicationType")
                                         @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                         @PathVariable("padPipelineId") Integer padPipelineId,
                                         PwaApplicationContext applicationContext,
                                         RedirectAttributes redirectAttributes) {
    var padPipeline = applicationContext.getPadPipeline();
    FlashUtils.info(redirectAttributes,"Deleted pipeline " +  padPipelineService.getPipelineOverview(padPipeline).getPipelineName());
    pipelineRemovalService.removePipeline(padPipeline);
    return ReverseRouter.redirect(on(PipelinesTaskListController.class)
        .renderPipelinesOverview(applicationId, pwaApplicationType, null, null));
  }

}
