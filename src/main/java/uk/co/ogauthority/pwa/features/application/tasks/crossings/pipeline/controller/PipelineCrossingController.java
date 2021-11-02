package uk.co.ogauthority.pwa.features.application.tasks.crossings.pipeline.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.pwaapplications.rest.PortalOrganisationUnitRestController;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock.AddBlockCrossingForm;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.pipeline.PadPipelineCrossingOwnerService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.pipeline.PadPipelineCrossingService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.pipeline.PipelineCrossingForm;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.pipeline.PipelineCrossingFormValidator;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.pipeline.PipelineCrossingUrlFactory;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist.CrossingAgreementsTaskListService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist.CrossingOverview;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist.controller.CrossingAgreementsController;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.enums.ScreenActionType;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.crossings.CrossingAgreementTask;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.searchselector.SearchSelectorService;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/crossings/pipeline")
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.DECOMMISSIONING
})
@PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
public class PipelineCrossingController {

  private final PadPipelineCrossingService padPipelineCrossingService;
  private final PadPipelineCrossingOwnerService padPipelineCrossingOwnerService;
  private final PipelineCrossingFormValidator pipelineCrossingFormValidator;
  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final CrossingAgreementsTaskListService crossingAgreementsTaskListService;
  private final PadFileService padFileService;
  private final ControllerHelperService controllerHelperService;

  @Autowired
  public PipelineCrossingController(
      PadPipelineCrossingService padPipelineCrossingService,
      PadPipelineCrossingOwnerService padPipelineCrossingOwnerService,
      PipelineCrossingFormValidator pipelineCrossingFormValidator,
      ApplicationBreadcrumbService applicationBreadcrumbService,
      CrossingAgreementsTaskListService crossingAgreementsTaskListService,
      PadFileService padFileService,
      ControllerHelperService controllerHelperService) {
    this.padPipelineCrossingService = padPipelineCrossingService;
    this.padPipelineCrossingOwnerService = padPipelineCrossingOwnerService;
    this.pipelineCrossingFormValidator = pipelineCrossingFormValidator;
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.crossingAgreementsTaskListService = crossingAgreementsTaskListService;
    this.padFileService = padFileService;
    this.controllerHelperService = controllerHelperService;
  }

  private ModelAndView getCrossingModelAndView(PwaApplicationDetail pwaApplicationDetail,
                                               ScreenActionType screenActionType) {
    var modelAndView = new ModelAndView("pwaApplication/shared/crossings/pipeline/addPipelineCrossing")
        .addObject("screenActionType", screenActionType)
        .addObject("backUrl",
            crossingAgreementsTaskListService.getRoute(pwaApplicationDetail, CrossingAgreementTask.PIPELINE_CROSSINGS))
        .addObject("orgsRestUrl", SearchSelectorService.route(on(PortalOrganisationUnitRestController.class)
            .searchPortalOrgUnits(null)));
    applicationBreadcrumbService.fromCrossingSection(pwaApplicationDetail, modelAndView,
        CrossingAgreementTask.PIPELINE_CROSSINGS,
        screenActionType.getActionText() + " pipeline crossing");
    return modelAndView;
  }

  private ModelAndView createOverviewModelAndView(PwaApplicationDetail detail) {
    var modelAndView = new ModelAndView("pwaApplication/shared/crossings/overview")
        .addObject("overview", CrossingOverview.PIPELINE_CROSSINGS)
        .addObject("pipelineCrossings", padPipelineCrossingService.getPipelineCrossingViews(detail))
        .addObject("pipelineCrossingUrlFactory", new PipelineCrossingUrlFactory(detail))
        .addObject("pipelineCrossingFiles",
            padFileService.getUploadedFileViews(detail, ApplicationDetailFilePurpose.PIPELINE_CROSSINGS,
                ApplicationFileLinkStatus.FULL))
        .addObject("backUrl", ReverseRouter.route(on(CrossingAgreementsController.class)
            .renderCrossingAgreementsOverview(detail.getPwaApplicationType(),
                detail.getMasterPwaApplicationId(), null,
                null)));
    applicationBreadcrumbService.fromCrossings(detail.getPwaApplication(), modelAndView,
        "Pipeline crossings");
    return modelAndView;
  }

  private ModelAndView repopulateOnError(PwaApplicationDetail pwaApplicationDetail, ScreenActionType screenActionType,
                                         PipelineCrossingForm pipelineCrossingForm) {
    var owners = padPipelineCrossingService.getPrepopulatedSearchSelectorItems(
        pipelineCrossingForm.getPipelineOwners());
    return getCrossingModelAndView(pwaApplicationDetail, screenActionType)
        .addObject("preselectedOwners", owners);

  }

  @GetMapping
  public ModelAndView renderOverview(@PathVariable("applicationType")
                                     @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                     @PathVariable("applicationId") Integer applicationId,
                                     @ModelAttribute("form") PipelineCrossingForm form,
                                     PwaApplicationContext applicationContext) {
    return createOverviewModelAndView(applicationContext.getApplicationDetail());
  }

  @PostMapping
  public ModelAndView postOverview(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @ModelAttribute("form") AddBlockCrossingForm form,
      PwaApplicationContext applicationContext) {

    var detail = applicationContext.getApplicationDetail();
    if (!padPipelineCrossingService.isComplete(detail)) {
      int pipelineCrossingCount = padPipelineCrossingService.getPipelineCrossingCount(detail);
      if (pipelineCrossingCount > 0) {
        return createOverviewModelAndView(detail)
            .addObject("errorMessage", "At least one document must be uploaded");
      } else {
        return createOverviewModelAndView(detail)
            .addObject("errorMessage", "Add at least one pipeline crossing");
      }
    }
    return ReverseRouter.redirect(on(CrossingAgreementsController.class)
        .renderCrossingAgreementsOverview(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null,
            null));
  }

  @GetMapping("/new")
  public ModelAndView renderAddCrossing(@PathVariable("applicationType")
                                        @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                        @PathVariable("applicationId") Integer applicationId,
                                        @ModelAttribute("form") PipelineCrossingForm form,
                                        PwaApplicationContext applicationContext) {
    var detail = applicationContext.getApplicationDetail();
    return getCrossingModelAndView(detail, ScreenActionType.ADD);
  }

  @PostMapping("/new")
  public ModelAndView postAddCrossings(@PathVariable("applicationType")
                                       @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                       @PathVariable("applicationId") Integer applicationId,
                                       @Valid @ModelAttribute("form") PipelineCrossingForm form,
                                       BindingResult bindingResult,
                                       PwaApplicationContext applicationContext) {
    var detail = applicationContext.getApplicationDetail();
    pipelineCrossingFormValidator.validate(form, bindingResult);
    return controllerHelperService.checkErrorsAndRedirect(bindingResult, repopulateOnError(detail, ScreenActionType.ADD, form),
        () -> {
          padPipelineCrossingService.createPipelineCrossings(detail, form);
          return crossingAgreementsTaskListService.getOverviewRedirect(detail,
              CrossingAgreementTask.PIPELINE_CROSSINGS);
        });
  }

  @GetMapping("/{crossingId}/edit")
  public ModelAndView renderEditCrossing(@PathVariable("applicationType")
                                         @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                         @PathVariable("applicationId") Integer applicationId,
                                         @PathVariable("crossingId") Integer crossingId,
                                         @ModelAttribute("form") PipelineCrossingForm form,
                                         PwaApplicationContext applicationContext) {
    var detail = applicationContext.getApplicationDetail();
    var crossing = padPipelineCrossingService.getPipelineCrossing(detail, crossingId);
    form.setPipelineCrossed(crossing.getPipelineCrossed());
    form.setPipelineFullyOwnedByOrganisation(crossing.getPipelineFullyOwnedByOrganisation());
    return getCrossingModelAndView(detail, ScreenActionType.EDIT)
        .addObject("preselectedOwners", padPipelineCrossingOwnerService.getOwnerPrepopulationFormAttribute(crossing));
  }

  @PostMapping("/{crossingId}/edit")
  public ModelAndView postEditCrossing(@PathVariable("applicationType")
                                       @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                       @PathVariable("applicationId") Integer applicationId,
                                       @PathVariable("crossingId") Integer crossingId,
                                       @Valid @ModelAttribute("form") PipelineCrossingForm form,
                                       BindingResult bindingResult,
                                       PwaApplicationContext applicationContext) {
    var detail = applicationContext.getApplicationDetail();
    var crossing = padPipelineCrossingService.getPipelineCrossing(detail, crossingId);
    pipelineCrossingFormValidator.validate(form, bindingResult);
    return controllerHelperService.checkErrorsAndRedirect(bindingResult, repopulateOnError(detail, ScreenActionType.EDIT, form),
        () -> {
          padPipelineCrossingService.updatePipelineCrossing(crossing, form);
          return crossingAgreementsTaskListService.getOverviewRedirect(detail,
              CrossingAgreementTask.PIPELINE_CROSSINGS);
        });
  }

  @GetMapping("/{crossingId}/remove")
  public ModelAndView renderRemoveCrossing(@PathVariable("applicationType")
                                           @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                           @PathVariable("applicationId") Integer applicationId,
                                           @PathVariable("crossingId") Integer crossingId,
                                           PwaApplicationContext applicationContext) {
    var detail = applicationContext.getApplicationDetail();
    var crossing = padPipelineCrossingService.getPipelineCrossing(detail, crossingId);
    var modelAndView = new ModelAndView("pwaApplication/shared/crossings/pipeline/removePipelineCrossing")
        .addObject("view", padPipelineCrossingService.getPipelineCrossingView(crossing))
        .addObject("backUrl",
            crossingAgreementsTaskListService.getRoute(detail, CrossingAgreementTask.PIPELINE_CROSSINGS));
    applicationBreadcrumbService.fromCrossings(detail.getPwaApplication(), modelAndView, "Remove cable crossing");
    return modelAndView;
  }

  @PostMapping("/{crossingId}/remove")
  public ModelAndView postRemoveCrossing(@PathVariable("applicationType")
                                         @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                         @PathVariable("applicationId") Integer applicationId,
                                         @PathVariable("crossingId") Integer crossingId,
                                         PwaApplicationContext applicationContext) {
    var detail = applicationContext.getApplicationDetail();
    var crossing = padPipelineCrossingService.getPipelineCrossing(detail, crossingId);
    padPipelineCrossingService.deleteCascade(crossing);
    return crossingAgreementsTaskListService.getOverviewRedirect(detail, CrossingAgreementTask.PIPELINE_CROSSINGS);
  }
}
