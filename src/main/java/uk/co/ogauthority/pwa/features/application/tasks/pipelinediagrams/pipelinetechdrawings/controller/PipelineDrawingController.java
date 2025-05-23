package uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
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
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.overview.controller.TechnicalDrawingsController;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.PadTechnicalDrawing;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.PadTechnicalDrawingLinkService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.PadTechnicalDrawingService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.PipelineDrawingForm;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.PipelineDrawingUrlFactory;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.enums.ScreenActionType;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/technical-drawings/pipeline-drawings")
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.DECOMMISSIONING,
    PwaApplicationType.OPTIONS_VARIATION
})
public class PipelineDrawingController {

  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final PadTechnicalDrawingService padTechnicalDrawingService;
  private final PadTechnicalDrawingLinkService padTechnicalDrawingLinkService;
  private final ControllerHelperService controllerHelperService;
  private final PadFileManagementService padFileManagementService;

  @Autowired
  public PipelineDrawingController(
      ApplicationBreadcrumbService applicationBreadcrumbService,
      PadTechnicalDrawingService padTechnicalDrawingService,
      PadTechnicalDrawingLinkService padTechnicalDrawingLinkService,
      ControllerHelperService controllerHelperService,
      PadFileManagementService padFileManagementService
  ) {
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.padTechnicalDrawingService = padTechnicalDrawingService;
    this.padTechnicalDrawingLinkService = padTechnicalDrawingLinkService;
    this.controllerHelperService = controllerHelperService;
    this.padFileManagementService = padFileManagementService;
  }

  private ModelAndView getDrawingModelAndView(PwaApplicationDetail detail, PipelineDrawingForm form) {
    var fileUploadAttributes = padFileManagementService.getFileUploadComponentAttributesForLegacyPadFile(
        form.getUploadedFiles(),
        detail,
        FileDocumentType.PIPELINE_DRAWINGS,
        ApplicationDetailFilePurpose.PIPELINE_DRAWINGS
    );

    var modelAndView = new ModelAndView("pwaApplication/shared/techdrawings/addPipelineDrawing")
        .addObject("pipelineViews", padTechnicalDrawingService.getUnlinkedApplicationPipelineOverviews(detail))
        .addObject("backUrl", ReverseRouter.route(on(TechnicalDrawingsController.class)
            .renderOverview(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null)))
        .addObject("actionType", ScreenActionType.ADD)
        .addObject("fileUploadAttributes", fileUploadAttributes);

    applicationBreadcrumbService.fromTechnicalDrawings(detail.getPwaApplication(), modelAndView,
        ScreenActionType.ADD.getActionText() + " pipeline schematic");

    return modelAndView;
  }

  private ModelAndView getEditDrawingModelAndView(PwaApplicationDetail detail, PadTechnicalDrawing drawing,
                                                  PipelineDrawingForm form) {
    List<Integer> linkedDrawingPipelineIds = padTechnicalDrawingLinkService.getLinksFromDrawing(drawing)
        .stream()
        .map(drawingLink -> drawingLink.getPipeline().getId())
        .collect(Collectors.toUnmodifiableList());
    var overviews = padTechnicalDrawingService.getUnlinkedAndSpecificApplicationPipelineOverviews(detail,
        linkedDrawingPipelineIds);

    var fileUploadAttributes = padFileManagementService.getFileUploadComponentAttributesForLegacyPadFile(
        form.getUploadedFiles(),
        detail,
        FileDocumentType.PIPELINE_DRAWINGS,
        ApplicationDetailFilePurpose.PIPELINE_DRAWINGS
    );

    var modelAndView = new ModelAndView("pwaApplication/shared/techdrawings/addPipelineDrawing")
        .addObject("pipelineViews", overviews)
        .addObject("backUrl", ReverseRouter.route(on(TechnicalDrawingsController.class)
            .renderOverview(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null)))
        .addObject("actionType", ScreenActionType.EDIT)
        .addObject("fileUploadAttributes", fileUploadAttributes);

    applicationBreadcrumbService.fromTechnicalDrawings(detail.getPwaApplication(), modelAndView,
        ScreenActionType.EDIT.getActionText() + " pipeline schematic");
    return modelAndView;
  }

  private ModelAndView getRemoveDrawingModelAndView(PwaApplicationDetail detail, Integer drawingId) {
    var modelAndView = new ModelAndView("pwaApplication/shared/techdrawings/removePipelineDrawing")
        .addObject("summary", padTechnicalDrawingService.getPipelineSummaryView(detail, drawingId))
        .addObject("urlFactory", new PipelineDrawingUrlFactory(detail))
        .addObject("backUrl", ReverseRouter.route(on(TechnicalDrawingsController.class)
            .renderOverview(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null)));
    applicationBreadcrumbService.fromTechnicalDrawings(detail.getPwaApplication(), modelAndView,
        "Remove pipeline schematic");
    return modelAndView;
  }

  @GetMapping("/new")
  @PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
  public ModelAndView renderAddDrawing(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @ModelAttribute("form") PipelineDrawingForm form,
      PwaApplicationContext applicationContext) {

    return getDrawingModelAndView(applicationContext.getApplicationDetail(), form);
  }

  @PostMapping("/new")
  @PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
  public ModelAndView postAddDrawing(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @ModelAttribute("form") PipelineDrawingForm form,
      BindingResult bindingResult,
      PwaApplicationContext applicationContext) {

    bindingResult = padTechnicalDrawingService.validateDrawing(form, bindingResult, ValidationType.FULL,
        applicationContext.getApplicationDetail());
    var modelAndView = getDrawingModelAndView(applicationContext.getApplicationDetail(), form);
    return controllerHelperService.checkErrorsAndRedirect(bindingResult, modelAndView, () -> {
      padFileManagementService.saveFiles(form, applicationContext.getApplicationDetail(), FileDocumentType.PIPELINE_DRAWINGS);
      padTechnicalDrawingService.addDrawing(applicationContext.getApplicationDetail(), form);
      return ReverseRouter.redirect(on(TechnicalDrawingsController.class)
          .renderOverview(applicationType, applicationId, null, null));
    });
  }

  @GetMapping("/{drawingId}/remove")
  @PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
  public ModelAndView renderRemoveDrawing(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @PathVariable("drawingId") Integer drawingId,
      PwaApplicationContext applicationContext) {

    return getRemoveDrawingModelAndView(applicationContext.getApplicationDetail(), drawingId);
  }

  @PostMapping("/{drawingId}/remove")
  @PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
  public ModelAndView postRemoveDrawing(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @PathVariable("drawingId") Integer drawingId,
      PwaApplicationContext applicationContext,
      AuthenticatedUserAccount user) {

    padTechnicalDrawingService.removeDrawing(applicationContext.getApplicationDetail(), drawingId, user);

    return ReverseRouter.redirect(on(TechnicalDrawingsController.class)
        .renderOverview(applicationType, applicationId, null, null));
  }

  @GetMapping("/{drawingId}/edit")
  @PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
  public ModelAndView renderEditDrawing(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @PathVariable("drawingId") Integer drawingId,
      @ModelAttribute("form") PipelineDrawingForm form,
      PwaApplicationContext applicationContext) {

    var drawing = padTechnicalDrawingService.getDrawing(applicationContext.getApplicationDetail(), drawingId);
    padTechnicalDrawingService.mapDrawingToForm(applicationContext.getApplicationDetail(), drawing, form);
    return getEditDrawingModelAndView(applicationContext.getApplicationDetail(), drawing, form);
  }

  @PostMapping("/{drawingId}/edit")
  @PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
  public ModelAndView postEditDrawing(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @PathVariable("drawingId") Integer drawingId,
      @ModelAttribute("form") PipelineDrawingForm form,
      BindingResult bindingResult,
      PwaApplicationContext applicationContext,
      AuthenticatedUserAccount user) {

    bindingResult = padTechnicalDrawingService.validateEdit(form, bindingResult, ValidationType.FULL,
        applicationContext.getApplicationDetail(), drawingId);
    var drawing = padTechnicalDrawingService.getDrawing(applicationContext.getApplicationDetail(), drawingId);
    var modelAndView = getEditDrawingModelAndView(applicationContext.getApplicationDetail(), drawing, form);
    return controllerHelperService.checkErrorsAndRedirect(bindingResult, modelAndView, () -> {
      padFileManagementService.saveFiles(form, applicationContext.getApplicationDetail(), FileDocumentType.PIPELINE_DRAWINGS);
      padTechnicalDrawingService.updateDrawing(applicationContext.getApplicationDetail(), drawingId, user, form);
      return ReverseRouter.redirect(on(TechnicalDrawingsController.class)
          .renderOverview(applicationType, applicationId, null, null));
    });

  }
}
