package uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelines;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.pwaapplications.rest.PipelineRestController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineFlexibility;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineMaterial;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.form.enums.ScreenActionType;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineHeaderForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineHeaderFormValidator;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineUrlFactory;
import uk.co.ogauthority.pwa.service.search.SearchSelectorService;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/pipelines")
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION
})
@PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
@PwaApplicationPermissionCheck(permissions = PwaApplicationPermission.EDIT)
public class PipelinesController {

  private final PadPipelineService padPipelineService;
  private final ApplicationBreadcrumbService breadcrumbService;
  private final PipelineHeaderFormValidator pipelineHeaderFormValidator;
  private final PwaApplicationRedirectService applicationRedirectService;
  private final ControllerHelperService controllerHelperService;

  @Autowired
  public PipelinesController(PadPipelineService padPipelineService,
                             ApplicationBreadcrumbService breadcrumbService,
                             PipelineHeaderFormValidator pipelineHeaderFormValidator,
                             PwaApplicationRedirectService applicationRedirectService,
                             ControllerHelperService controllerHelperService) {
    this.padPipelineService = padPipelineService;
    this.breadcrumbService = breadcrumbService;
    this.pipelineHeaderFormValidator = pipelineHeaderFormValidator;
    this.applicationRedirectService = applicationRedirectService;
    this.controllerHelperService = controllerHelperService;
  }

  private ModelAndView getOverviewModelAndView(PwaApplicationDetail detail, PadPipeline pipeline) {

    var modelAndView = new ModelAndView("pwaApplication/shared/pipelines/overview")
        .addObject("pipelineTaskListItems", padPipelineService.getPipelineTaskListItems(detail).stream()
            .sorted(Comparator.comparing(PipelineOverview::getPipelineNumber))
            .collect(Collectors.toList()))
        .addObject("pipelineUrlFactory", new PipelineUrlFactory(detail))
        .addObject("canImportConsentedPipeline", padPipelineService.canImportConsentedPipelines(detail))
        .addObject("taskListUrl", applicationRedirectService.getTaskListRoute(detail.getPwaApplication()));

    breadcrumbService.fromTaskList(detail.getPwaApplication(), modelAndView, "Pipelines");

    return modelAndView;

  }

  @GetMapping
  public ModelAndView renderPipelinesOverview(@PathVariable("applicationId") Integer applicationId,
                                              @PathVariable("applicationType")
                                              @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                              PwaApplicationContext applicationContext) {
    return getOverviewModelAndView(applicationContext.getApplicationDetail(), applicationContext.getPadPipeline());
  }

  @PostMapping
  public ModelAndView postPipelinesOverview(@PathVariable("applicationId") Integer applicationId,
                                            @PathVariable("applicationType")
                                            @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                            PwaApplicationContext applicationContext) {

    var detail = applicationContext.getApplicationDetail();
    // otherwise, make sure that all pipelines have header info and idents
    var pipelinesComplete = padPipelineService.isComplete(detail);

    if (!pipelinesComplete) {
      return getOverviewModelAndView(applicationContext.getApplicationDetail(), applicationContext.getPadPipeline())
          .addObject("errorMessage",
              "At least one pipeline must be added. Each pipeline must have at least one ident.");
    }

    return applicationRedirectService.getTaskListRedirect(applicationContext.getPwaApplication());

  }

  private ModelAndView getAddEditPipelineModelAndView(PwaApplicationDetail detail, ScreenActionType type,
                                                      PadPipeline pipeline) {

    var modelAndView = new ModelAndView("pwaApplication/shared/pipelines/addEditPipeline")
        .addObject("pipelineTypes", PipelineType.streamDisplayValues()
            .collect(StreamUtils.toLinkedHashMap(Enum::name, PipelineType::getDisplayName)))
        .addObject("longDirections", LongitudeDirection.stream()
            .collect(StreamUtils.toLinkedHashMap(Enum::name, LongitudeDirection::getDisplayText)))
        .addObject("cancelUrl", ReverseRouter.route(on(PipelinesController.class)
            .renderPipelinesOverview(detail.getMasterPwaApplicationId(), detail.getPwaApplicationType(), null)))
        .addObject("screenActionType", type)
        .addObject("pipelineFlexibilityTypes", PipelineFlexibility.asList())
        .addObject("pipelineMaterialTypes", PipelineMaterial.asList())
        .addObject("bundleNameRestUrl", SearchSelectorService.route(on(PipelineRestController.class)
            .searchBundleNames(detail.getMasterPwaApplicationId(), null, null)));

    breadcrumbService.fromPipelinesOverview(detail.getPwaApplication(), modelAndView,
        type.getSubmitButtonText() + " pipeline");

    if (pipeline != null) {
      modelAndView.addObject("pipelineNumber", pipeline.getPipelineRef());
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

    pipelineHeaderFormValidator.validate(form, bindingResult, applicationContext);

    return controllerHelperService.checkErrorsAndRedirect(bindingResult,
        getAddEditPipelineModelAndView(applicationContext.getApplicationDetail(), ScreenActionType.ADD, null), () -> {

          padPipelineService.addPipeline(applicationContext.getApplicationDetail(), form);

          return ReverseRouter.redirect(
              on(PipelinesController.class).renderPipelinesOverview(applicationId, pwaApplicationType, null));

        });

  }

  @GetMapping("/pipeline/{padPipelineId}")
  public ModelAndView renderEditPipeline(@PathVariable("applicationId") Integer applicationId,
                                         @PathVariable("applicationType")
                                         @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                         @PathVariable("padPipelineId") Integer padPipelineId,
                                         PwaApplicationContext applicationContext,
                                         @ModelAttribute("form") PipelineHeaderForm form) {

    padPipelineService.mapEntityToForm(form, applicationContext.getPadPipeline());

    return getAddEditPipelineModelAndView(applicationContext.getApplicationDetail(), ScreenActionType.EDIT,
        applicationContext.getPadPipeline())
        .addObject("form", form);

  }

  @PostMapping("/pipeline/{padPipelineId}")
  public ModelAndView postEditPipeline(@PathVariable("applicationId") Integer applicationId,
                                       @PathVariable("applicationType")
                                       @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                       @PathVariable("padPipelineId") Integer padPipelineId,
                                       PwaApplicationContext applicationContext,
                                       @ModelAttribute("form") PipelineHeaderForm form,
                                       BindingResult bindingResult) {

    pipelineHeaderFormValidator.validate(form, bindingResult, applicationContext);

    var pipeline = applicationContext.getPadPipeline();

    return controllerHelperService.checkErrorsAndRedirect(bindingResult,
        getAddEditPipelineModelAndView(applicationContext.getApplicationDetail(), ScreenActionType.EDIT, pipeline),
        () -> {

          padPipelineService.updatePipeline(pipeline, form);

          return ReverseRouter.redirect(
              on(PipelinesController.class).renderPipelinesOverview(applicationId, pwaApplicationType, null));

        });

  }

}
