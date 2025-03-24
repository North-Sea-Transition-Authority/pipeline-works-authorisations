package uk.co.ogauthority.pwa.features.application.tasks.crossings.cable.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.cable.AddCableCrossingForm;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.cable.CableCrossingUrlFactory;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.cable.CableCrossingView;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.cable.PadCableCrossingService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock.AddBlockCrossingForm;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist.CrossingAgreementsTaskListService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist.CrossingOverview;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist.controller.CrossingAgreementsController;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.crossings.CrossingAgreementTask;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/crossings/cable")
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.DECOMMISSIONING
})
@PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
public class CableCrossingController {

  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final PadCableCrossingService padCableCrossingService;
  private final CrossingAgreementsTaskListService crossingAgreementsTaskListService;
  private final ControllerHelperService controllerHelperService;
  private final PadFileManagementService padFileManagementService;

  @Autowired
  public CableCrossingController(
      ApplicationBreadcrumbService applicationBreadcrumbService,
      PadCableCrossingService padCableCrossingService,
      CrossingAgreementsTaskListService crossingAgreementsTaskListService,
      ControllerHelperService controllerHelperService,
      PadFileManagementService padFileManagementService
  ) {
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.padCableCrossingService = padCableCrossingService;
    this.crossingAgreementsTaskListService = crossingAgreementsTaskListService;
    this.controllerHelperService = controllerHelperService;
    this.padFileManagementService = padFileManagementService;
  }

  private ModelAndView createOverviewModelAndView(PwaApplicationDetail detail) {
    var modelAndView = new ModelAndView("pwaApplication/shared/crossings/overview")
        .addObject("overview", CrossingOverview.CABLE_CROSSINGS)
        .addObject("cableCrossings", padCableCrossingService.getCableCrossingViews(detail))
        .addObject("cableCrossingUrlFactory", new CableCrossingUrlFactory(detail))
        .addObject("cableCrossingFiles",
            padFileManagementService.getUploadedFileViews(detail, FileDocumentType.CABLE_CROSSINGS))
        .addObject("backUrl", ReverseRouter.route(on(CrossingAgreementsController.class)
            .renderCrossingAgreementsOverview(detail.getPwaApplicationType(),
                detail.getMasterPwaApplicationId(), null,
                null)));
    applicationBreadcrumbService.fromCrossings(detail.getPwaApplication(), modelAndView, "Cable crossings");
    return modelAndView;
  }

  private ModelAndView createRenderAddModelAndView(PwaApplicationDetail detail) {
    var modelAndView = new ModelAndView("pwaApplication/shared/crossings/addCableCrossing")
        .addObject("backUrl",
            crossingAgreementsTaskListService.getRoute(detail, CrossingAgreementTask.CABLE_CROSSINGS));
    applicationBreadcrumbService.fromCrossingSection(detail, modelAndView,
        CrossingAgreementTask.CABLE_CROSSINGS, "Add cable crossing");
    return modelAndView;
  }

  private ModelAndView createRenderEditModelAndView(PwaApplicationDetail detail) {
    var modelAndView = new ModelAndView("pwaApplication/shared/crossings/editCableCrossing")
        .addObject("backUrl",
            crossingAgreementsTaskListService.getRoute(detail, CrossingAgreementTask.CABLE_CROSSINGS));
    applicationBreadcrumbService.fromCrossingSection(detail, modelAndView,
        CrossingAgreementTask.CABLE_CROSSINGS, "Edit cable crossing");
    return modelAndView;
  }

  @GetMapping
  public ModelAndView renderOverview(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @ModelAttribute("form") AddCableCrossingForm form,
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
    if (!padCableCrossingService.isComplete(detail)) {
      return createOverviewModelAndView(detail)
          .addObject("errorMessage", "You must have added at least one crossing, and uploaded a document");
    }
    return ReverseRouter.redirect(on(CrossingAgreementsController.class)
        .renderCrossingAgreementsOverview(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null,
            null));
  }

  @GetMapping("/new")
  public ModelAndView renderAddCableCrossing(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @ModelAttribute("form") AddCableCrossingForm form,
      PwaApplicationContext applicationContext) {

    return createRenderAddModelAndView(applicationContext.getApplicationDetail());
  }

  @PostMapping("/new")
  public ModelAndView postAddCableCrossings(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @Valid @ModelAttribute("form") AddCableCrossingForm form,
      BindingResult bindingResult,
      PwaApplicationContext applicationContext) {

    var detail = applicationContext.getApplicationDetail();
    return controllerHelperService.checkErrorsAndRedirect(bindingResult, createRenderAddModelAndView(detail), () -> {
      padCableCrossingService.createCableCrossing(detail, form);
      return crossingAgreementsTaskListService.getOverviewRedirect(detail, CrossingAgreementTask.CABLE_CROSSINGS);
    });
  }

  @GetMapping("/{crossingId}/edit")
  public ModelAndView renderEditCableCrossing(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @PathVariable("crossingId") Integer crossingId,
      @ModelAttribute("form") AddCableCrossingForm form,
      PwaApplicationContext applicationContext) {

    var detail = applicationContext.getApplicationDetail();
    var modelAndView = createRenderEditModelAndView(detail);
    var cableCrossing = padCableCrossingService.getCableCrossing(detail, crossingId);
    padCableCrossingService.mapCrossingToForm(cableCrossing, form);
    return modelAndView;
  }

  @PostMapping("/{crossingId}/edit")
  public ModelAndView postEditCableCrossing(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @PathVariable("crossingId") Integer crossingId,
      @Valid @ModelAttribute("form") AddCableCrossingForm form,
      BindingResult bindingResult,
      PwaApplicationContext applicationContext) {

    var detail = applicationContext.getApplicationDetail();
    return controllerHelperService.checkErrorsAndRedirect(bindingResult, createRenderEditModelAndView(detail), () -> {
      padCableCrossingService.updateCableCrossing(detail, crossingId, form);
      return crossingAgreementsTaskListService.getOverviewRedirect(detail, CrossingAgreementTask.CABLE_CROSSINGS);
    });
  }

  @GetMapping("/{crossingId}/remove")
  public ModelAndView renderRemoveCableCrossing(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @PathVariable("crossingId") Integer crossingId,
      PwaApplicationContext applicationContext) {
    var detail = applicationContext.getApplicationDetail();
    var crossing = padCableCrossingService.getCableCrossing(detail, crossingId);

    var modelAndView = new ModelAndView("pwaApplication/shared/crossings/removeCableCrossing")
        .addObject("cableCrossing", new CableCrossingView(crossing))
        .addObject("backUrl",
            crossingAgreementsTaskListService.getRoute(detail, CrossingAgreementTask.CABLE_CROSSINGS));
    applicationBreadcrumbService.fromCrossingSection(detail, modelAndView,
        CrossingAgreementTask.CABLE_CROSSINGS, "Remove cable crossing");
    return modelAndView;
  }

  @PostMapping("/{crossingId}/remove")
  public ModelAndView postRemoveCableCrossing(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @PathVariable("crossingId") Integer crossingId,
      PwaApplicationContext applicationContext) {

    var detail = applicationContext.getApplicationDetail();
    padCableCrossingService.removeCableCrossing(detail, crossingId);
    return crossingAgreementsTaskListService.getOverviewRedirect(detail, CrossingAgreementTask.CABLE_CROSSINGS);
  }

}
