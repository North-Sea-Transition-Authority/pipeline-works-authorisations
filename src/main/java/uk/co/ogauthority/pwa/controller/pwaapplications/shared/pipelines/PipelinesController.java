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
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.form.enums.ScreenActionType;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineHeaderForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelinesService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineHeaderFormValidator;
import uk.co.ogauthority.pwa.util.ControllerUtils;
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

  private final PadPipelinesService padPipelinesService;
  private final ApplicationBreadcrumbService breadcrumbService;
  private final PipelineHeaderFormValidator validator;

  @Autowired
  public PipelinesController(PadPipelinesService padPipelinesService,
                             ApplicationBreadcrumbService breadcrumbService,
                             PipelineHeaderFormValidator validator) {
    this.padPipelinesService = padPipelinesService;
    this.breadcrumbService = breadcrumbService;
    this.validator = validator;
  }

  private ModelAndView getOverviewModelAndView(PwaApplicationDetail detail) {
    return new ModelAndView("pwaApplication/shared/pipelines/overview")
        .addObject("pipelineOverviews", padPipelinesService.getPipelineOverviews(detail).stream()
            .sorted(Comparator.comparing(PipelineOverview::getPipelineNumber))
            .collect(Collectors.toList()))
        .addObject("addPipelineUrl", ReverseRouter.route(on(PipelinesController.class)
            .renderAddPipeline(detail.getMasterPwaApplicationId(), detail.getPwaApplicationType(), null, null)));
  }

  @GetMapping
  public ModelAndView renderPipelinesOverview(@PathVariable("applicationId") Integer applicationId,
                                              @PathVariable("applicationType")
                                              @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                              PwaApplicationContext applicationContext) {
    return getOverviewModelAndView(applicationContext.getApplicationDetail());
  }

  private ModelAndView getAddEditPipelineMav(PwaApplicationDetail detail, ScreenActionType type, PadPipeline pipeline) {

    var modelAndView = new ModelAndView("pwaApplication/shared/pipelines/addEditPipeline")
        .addObject("pipelineTypes", PipelineType.stream()
            .sorted(Comparator.comparing(PipelineType::getDisplayOrder))
            .collect(StreamUtils.toLinkedHashMap(Enum::name, PipelineType::getDisplayName)))
        .addObject("longDirections", LongitudeDirection.stream()
            .collect(StreamUtils.toLinkedHashMap(Enum::name, LongitudeDirection::getDisplayText)))
        .addObject("cancelUrl", ReverseRouter.route(on(PipelinesController.class)
            .renderPipelinesOverview(detail.getMasterPwaApplicationId(), detail.getPwaApplicationType(), null)))
        .addObject("screenActionType", type);

    breadcrumbService.fromPipelinesOverview(detail.getPwaApplication(), modelAndView, type.getSubmitButtonText() + " pipeline");

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
    return getAddEditPipelineMav(applicationContext.getApplicationDetail(), ScreenActionType.ADD, null);
  }

  @PostMapping("/add-pipeline")
  public ModelAndView postAddPipeline(@PathVariable("applicationId") Integer applicationId,
                                      @PathVariable("applicationType")
                                      @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                      PwaApplicationContext applicationContext,
                                      @ModelAttribute("form") PipelineHeaderForm form,
                                      BindingResult bindingResult) {

    validator.validate(form, bindingResult, applicationContext);

    return ControllerUtils.checkErrorsAndRedirect(bindingResult,
        getAddEditPipelineMav(applicationContext.getApplicationDetail(), ScreenActionType.ADD, null), () -> {

          padPipelinesService.addPipeline(applicationContext.getApplicationDetail(), form);

          return ReverseRouter.redirect(on(PipelinesController.class).renderPipelinesOverview(applicationId, pwaApplicationType, null));

        });

  }

  @GetMapping("/pipeline/{padPipelineId}")
  public ModelAndView renderEditPipeline(@PathVariable("applicationId") Integer applicationId,
                                         @PathVariable("applicationType")
                                         @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                         PwaApplicationContext applicationContext,
                                         @PathVariable("padPipelineId") Integer padPipelineId,
                                         @ModelAttribute("form") PipelineHeaderForm form) {

    var pipeline = padPipelinesService.getPipeline(padPipelineId);
    padPipelinesService.mapEntityToForm(form, pipeline);

    return getAddEditPipelineMav(applicationContext.getApplicationDetail(), ScreenActionType.EDIT, pipeline)
        .addObject("form", form);

  }

  @PostMapping("/pipeline/{padPipelineId}")
  public ModelAndView postEditPipeline(@PathVariable("applicationId") Integer applicationId,
                                       @PathVariable("applicationType")
                                       @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                       PwaApplicationContext applicationContext,
                                       @PathVariable("padPipelineId") Integer padPipelineId,
                                       @ModelAttribute("form") PipelineHeaderForm form,
                                       BindingResult bindingResult) {

    var pipeline = padPipelinesService.getPipeline(padPipelineId);

    validator.validate(form, bindingResult, applicationContext);

    return ControllerUtils.checkErrorsAndRedirect(bindingResult,
        getAddEditPipelineMav(applicationContext.getApplicationDetail(), ScreenActionType.EDIT, pipeline), () -> {

          padPipelinesService.updatePipeline(pipeline, form);

          return ReverseRouter.redirect(on(PipelinesController.class).renderPipelinesOverview(applicationId, pwaApplicationType, null));

        });

  }


}
