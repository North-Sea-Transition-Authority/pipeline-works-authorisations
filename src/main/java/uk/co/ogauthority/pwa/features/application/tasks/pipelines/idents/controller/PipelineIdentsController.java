package uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.controller;

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
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PipelineControllerRouteUtils;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.IdentUrlFactory;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdentService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PipelineIdentForm;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PipelineIdentFormValidator;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.tasklist.controller.PipelinesTaskListController;
import uk.co.ogauthority.pwa.features.generalcase.pipelineview.IdentView;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.enums.ScreenActionType;
import uk.co.ogauthority.pwa.model.form.location.CoordinateForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.util.CoordinateUtils;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/pipelines/pipeline/{padPipelineId}/idents")
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.DECOMMISSIONING,
    PwaApplicationType.OPTIONS_VARIATION
})
@PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
@PwaApplicationPermissionCheck(permissions = PwaApplicationPermission.EDIT)
public class PipelineIdentsController {

  private final ApplicationBreadcrumbService breadcrumbService;
  private final PipelineIdentFormValidator validator;
  private final PadPipelineIdentService padIdentService;
  private final PadPipelineService padPipelineService;
  private final ControllerHelperService controllerHelperService;

  @Autowired
  public PipelineIdentsController(ApplicationBreadcrumbService breadcrumbService,
                                  PipelineIdentFormValidator validator,
                                  PadPipelineIdentService padIdentService,
                                  PadPipelineService padPipelineService,
                                  ControllerHelperService controllerHelperService) {
    this.breadcrumbService = breadcrumbService;
    this.validator = validator;
    this.padIdentService = padIdentService;
    this.padPipelineService = padPipelineService;
    this.controllerHelperService = controllerHelperService;
  }

  private ModelAndView getIdentOverviewModelAndView(PwaApplicationDetail detail, PadPipeline padPipeline) {
    var modelAndView = new ModelAndView("pwaApplication/shared/pipelines/identOverview")
        .addObject("pipelineOverview", padPipelineService.getPipelineOverview(padPipeline))
        .addObject("summaryView", padIdentService.getConnectedPipelineIdentSummaryView(padPipeline))
        .addObject("addIdentUrl", ReverseRouter.route(on(PipelineIdentsController.class)
            .renderAddIdent(detail.getMasterPwaApplicationId(), detail.getPwaApplicationType(), padPipeline.getId(),
                null, null, null)))
        .addObject("identUrlFactory",
            new IdentUrlFactory(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(),
                padPipeline.getId()))
        .addObject("backUrl", ReverseRouter.route(on(PipelinesTaskListController.class)
            .renderPipelinesOverview(detail.getMasterPwaApplicationId(), detail.getPwaApplicationType(), null)))
        .addObject("coreType", padPipeline.getCoreType());

    breadcrumbService.fromPipelinesOverview(detail.getPwaApplication(), modelAndView,
        padPipeline.getPipelineRef() + " idents");

    return modelAndView;
  }

  private ModelAndView getRemoveIdentModelAndView(PwaApplicationDetail detail, IdentView identView,
                                                  PadPipeline pipeline) {
    var modelAndView = new ModelAndView("pwaApplication/shared/pipelines/removeIdent")
        .addObject("identView", identView)
        .addObject("backUrl", ReverseRouter.route(on(PipelineIdentsController.class)
            .renderIdentOverview(detail.getMasterPwaApplicationId(), detail.getPwaApplicationType(), pipeline.getId(),
                null, null)))
        .addObject("coreType", pipeline.getCoreType());

    breadcrumbService.fromPipelineIdentOverview(detail.getPwaApplication(), pipeline, modelAndView, "Remove ident");
    return modelAndView;
  }

  @GetMapping
  public ModelAndView renderIdentOverview(@PathVariable("applicationId") Integer applicationId,
                                          @PathVariable("applicationType")
                                          @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                          @PathVariable("padPipelineId") Integer padPipelineId,
                                          PwaApplicationContext applicationContext,
                                          RedirectAttributes redirectAttributes) {
    return PipelineControllerRouteUtils.ifAllowedFromOverviewOrRedirect(applicationContext, redirectAttributes,
        () -> getIdentOverviewModelAndView(applicationContext.getApplicationDetail(),
            applicationContext.getPadPipeline()));
  }

  private ModelAndView getAddEditIdentModelAndView(PwaApplicationDetail detail, PipelineIdentForm identForm,
                                                   PadPipeline padPipeline, ScreenActionType screenActionType) {
    var modelAndView = new ModelAndView("pwaApplication/shared/pipelines/addEditIdent")
        .addObject("longDirections", LongitudeDirection.stream()
            .collect(StreamUtils.toLinkedHashMap(Enum::name, LongitudeDirection::getDisplayText)))
        .addObject("cancelUrl", ReverseRouter.route(on(PipelineIdentsController.class)
            .renderIdentOverview(detail.getMasterPwaApplicationId(), detail.getPwaApplicationType(),
                padPipeline.getId(), null, null)))
        .addObject("screenActionType", screenActionType)
        .addObject("form", identForm)
        .addObject("coreType", padPipeline.getCoreType());

    breadcrumbService.fromPipelineIdentOverview(detail.getPwaApplication(), padPipeline, modelAndView,
        screenActionType.getActionText() + " ident");
    return modelAndView;
  }

  @PostMapping
  public ModelAndView postIdentOverview(@PathVariable("applicationId") Integer applicationId,
                                        @PathVariable("applicationType")
                                        @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                        @PathVariable("padPipelineId") Integer padPipelineId,
                                        PwaApplicationContext applicationContext,
                                        RedirectAttributes redirectAttributes) {

    return PipelineControllerRouteUtils.ifAllowedFromOverviewOrError(applicationContext, () -> {
      var identSummaryValidationResult = padIdentService.getSummaryScreenValidationResult(
          applicationContext.getPadPipeline());
      if (identSummaryValidationResult.isSectionComplete()) {
        return ReverseRouter.redirect(on(PipelinesTaskListController.class)
            .renderPipelinesOverview(applicationId, pwaApplicationType, null));
      } else {
        var modelAndView = getIdentOverviewModelAndView(
            applicationContext.getApplicationDetail(),
            applicationContext.getPadPipeline()
        );
        return modelAndView.addObject("identSummaryValidationResult", identSummaryValidationResult);
      }
    });

  }

  @GetMapping("/add")
  public ModelAndView renderAddIdent(@PathVariable("applicationId") Integer applicationId,
                                     @PathVariable("applicationType")
                                     @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                     @PathVariable("padPipelineId") Integer padPipelineId,
                                     PwaApplicationContext applicationContext,
                                     @ModelAttribute("form") PipelineIdentForm form,
                                     RedirectAttributes redirectAttributes) {

    // set the fromLocation of our new ident to the toLocation of the previous ident if one exists
    return PipelineControllerRouteUtils.ifAllowedFromOverviewOrRedirect(applicationContext, redirectAttributes, () -> {
      padIdentService.getMaxIdent(applicationContext.getPadPipeline())
          .ifPresent(previousIdent -> {
            var fromCoordinateForm = new CoordinateForm();
            CoordinateUtils.mapCoordinatePairToForm(previousIdent.getToCoordinates(), fromCoordinateForm);
            form.setFromLocation(previousIdent.getToLocation());
            form.setFromCoordinateForm(fromCoordinateForm);
          });

      return getAddEditIdentModelAndView(applicationContext.getApplicationDetail(), form,
          applicationContext.getPadPipeline(), ScreenActionType.ADD);
    });

  }

  @GetMapping("/add/{insertAboveIdentId}")
  public ModelAndView renderInsertIdentAbove(@PathVariable("applicationId") Integer applicationId,
                                             @PathVariable("applicationType")
                                             @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                             @PathVariable("padPipelineId") Integer padPipelineId,
                                             @PathVariable("insertAboveIdentId") Integer insertAboveIdentId,
                                             PwaApplicationContext applicationContext,
                                             @ModelAttribute("form") PipelineIdentForm form,
                                             RedirectAttributes redirectAttributes) {

    // set the fromLocation of our new ident to the toLocation of the previous ident if one exists
    return PipelineControllerRouteUtils.ifAllowedFromOverviewOrRedirect(applicationContext, redirectAttributes, () -> {
      var nextIdent = padIdentService.getIdent(applicationContext.getPadPipeline(), insertAboveIdentId);
      padIdentService.getIdentByIdentNumber(applicationContext.getPadPipeline(), nextIdent.getIdentNo() - 1)
          .ifPresent(previousIdent -> {
            var fromCoordinateForm = new CoordinateForm();
            CoordinateUtils.mapCoordinatePairToForm(previousIdent.getToCoordinates(), fromCoordinateForm);
            form.setFromLocation(previousIdent.getToLocation());
            form.setFromCoordinateForm(fromCoordinateForm);
          });

      return getAddEditIdentModelAndView(applicationContext.getApplicationDetail(), form,
          applicationContext.getPadPipeline(), ScreenActionType.ADD);
    });

  }

  @PostMapping("/add/{insertAboveIdentId}")
  public ModelAndView postInsertIdentAbove(@PathVariable("applicationId") Integer applicationId,
                                           @PathVariable("applicationType")
                                           @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                           @PathVariable("padPipelineId") Integer padPipelineId,
                                           @PathVariable("insertAboveIdentId") Integer insertAboveIdentId,
                                           PwaApplicationContext applicationContext,
                                           @ModelAttribute("form") PipelineIdentForm form,
                                           BindingResult bindingResult,
                                           RedirectAttributes redirectAttributes) {

    return PipelineControllerRouteUtils.ifAllowedFromOverviewOrError(applicationContext, () -> {
      var coreType = applicationContext.getPadPipeline().getCoreType();
      var nextIdent = padIdentService.getIdent(applicationContext.getPadPipeline(), insertAboveIdentId);
      validator.validate(form, bindingResult, applicationContext, coreType);

      return controllerHelperService.checkErrorsAndRedirect(bindingResult,
          getAddEditIdentModelAndView(applicationContext.getApplicationDetail(), form,
              applicationContext.getPadPipeline(), ScreenActionType.ADD),
          () -> {
            padIdentService.addIdentAtPosition(applicationContext.getPadPipeline(), form, nextIdent.getIdentNo());
            return ReverseRouter.redirect(on(PipelineIdentsController.class).renderIdentOverview(
                applicationId, pwaApplicationType, padPipelineId, applicationContext, null));
          });
    });

  }

  @PostMapping("/add")
  public ModelAndView postAddIdent(@PathVariable("applicationId") Integer applicationId,
                                   @PathVariable("applicationType")
                                   @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                   @PathVariable("padPipelineId") Integer padPipelineId,
                                   PwaApplicationContext applicationContext,
                                   @ModelAttribute("form") PipelineIdentForm form,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes) {

    return PipelineControllerRouteUtils.ifAllowedFromOverviewOrError(applicationContext, () -> {

      validator.validate(form, bindingResult, applicationContext,
          applicationContext.getPadPipeline().getCoreType());

      return controllerHelperService.checkErrorsAndRedirect(bindingResult,
          getAddEditIdentModelAndView(applicationContext.getApplicationDetail(), form,
              applicationContext.getPadPipeline(),
              ScreenActionType.ADD),
          () -> {
            padIdentService.addIdent(applicationContext.getPadPipeline(), form);
            return ReverseRouter.redirect(on(PipelineIdentsController.class).renderIdentOverview(
                applicationId, pwaApplicationType, padPipelineId, applicationContext, null));
          });
    });

  }

  @GetMapping("/{identId}/remove")
  public ModelAndView renderRemoveIdent(@PathVariable("applicationId") Integer applicationId,
                                        @PathVariable("applicationType")
                                        @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                        @PathVariable("padPipelineId") Integer padPipelineId,
                                        PwaApplicationContext applicationContext,
                                        @PathVariable("identId") Integer identId,
                                        RedirectAttributes redirectAttributes) {
    var identView = padIdentService.getIdentView(applicationContext.getPadPipeline(), identId);
    return PipelineControllerRouteUtils.ifAllowedFromOverviewOrRedirect(applicationContext, redirectAttributes,
        () -> getRemoveIdentModelAndView(applicationContext.getApplicationDetail(), identView,
            applicationContext.getPadPipeline()));
  }

  @PostMapping("/{identId}/remove")
  public ModelAndView postRemoveIdent(@PathVariable("applicationId") Integer applicationId,
                                      @PathVariable("applicationType")
                                      @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                      @PathVariable("padPipelineId") Integer padPipelineId,
                                      PwaApplicationContext applicationContext,
                                      @PathVariable("identId") Integer identId,
                                      RedirectAttributes redirectAttributes) {
    var ident = padIdentService.getIdent(applicationContext.getPadPipeline(), identId);
    return PipelineControllerRouteUtils.ifAllowedFromOverviewOrError(applicationContext,
        () -> {
          padIdentService.removeIdent(ident);
          return ReverseRouter.redirect(on(PipelineIdentsController.class)
              .renderIdentOverview(applicationId, pwaApplicationType, padPipelineId, null, null));
        });
  }

  @GetMapping("/edit/{identId}")
  public ModelAndView renderEditIdent(@PathVariable("applicationId") Integer applicationId,
                                      @PathVariable("applicationType")
                                      @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                      @PathVariable("padPipelineId") Integer padPipelineId,
                                      @PathVariable("identId") Integer identId,
                                      PwaApplicationContext applicationContext,
                                      @ModelAttribute("form") PipelineIdentForm form,
                                      RedirectAttributes redirectAttributes) {

    var ident = padIdentService.getIdent(applicationContext.getPadPipeline(), identId);

    padIdentService.mapEntityToForm(ident, form);

    return PipelineControllerRouteUtils.ifAllowedFromOverviewOrRedirect(applicationContext, redirectAttributes,
        () -> getAddEditIdentModelAndView(applicationContext.getApplicationDetail(), form,
            applicationContext.getPadPipeline(), ScreenActionType.EDIT));

  }

  @PostMapping("/edit/{identId}")
  public ModelAndView postEditIdent(@PathVariable("applicationId") Integer applicationId,
                                    @PathVariable("applicationType")
                                    @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                    @PathVariable("padPipelineId") Integer padPipelineId,
                                    @PathVariable("identId") Integer identId,
                                    PwaApplicationContext applicationContext,
                                    @ModelAttribute("form") PipelineIdentForm form,
                                    BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes) {

    var ident = padIdentService.getIdent(applicationContext.getPadPipeline(), identId);

    return PipelineControllerRouteUtils.ifAllowedFromOverviewOrError(applicationContext,
        () -> {
          validator.validate(form, bindingResult, applicationContext,
              applicationContext.getPadPipeline().getCoreType());

          return controllerHelperService.checkErrorsAndRedirect(bindingResult,
              getAddEditIdentModelAndView(applicationContext.getApplicationDetail(), form,
                  applicationContext.getPadPipeline(), ScreenActionType.EDIT), () -> {

                padIdentService.updateIdent(ident, form);
                return ReverseRouter.redirect(on(PipelineIdentsController.class).renderIdentOverview(
                    applicationId, pwaApplicationType, padPipelineId, applicationContext, null));
              });
        });

  }

}
