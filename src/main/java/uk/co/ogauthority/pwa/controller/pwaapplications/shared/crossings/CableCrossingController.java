package uk.co.ogauthority.pwa.controller.pwaapplications.shared.crossings;

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
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.enums.CrossingOverview;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.crossings.AddBlockCrossingForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.crossings.AddCableCrossingForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.crossings.CrossingAgreementTask;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.CableCrossingFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.CableCrossingUrlFactory;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.CableCrossingView;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.CrossingAgreementsService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.PadCableCrossingService;
import uk.co.ogauthority.pwa.service.tasklist.CrossingAgreementsTaskListService;
import uk.co.ogauthority.pwa.util.ControllerUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/crossings/cable")
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.DEPOSIT_CONSENT
})
@PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
public class CableCrossingController {

  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final PadCableCrossingService padCableCrossingService;
  private final CableCrossingFileService cableCrossingFileService;
  private final CrossingAgreementsService crossingAgreementsService;
  private final CrossingAgreementsTaskListService crossingAgreementsTaskListService;

  @Autowired
  public CableCrossingController(
      ApplicationBreadcrumbService applicationBreadcrumbService,
      PadCableCrossingService padCableCrossingService,
      CableCrossingFileService cableCrossingFileService,
      CrossingAgreementsService crossingAgreementsService,
      CrossingAgreementsTaskListService crossingAgreementsTaskListService) {
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.padCableCrossingService = padCableCrossingService;
    this.cableCrossingFileService = cableCrossingFileService;
    this.crossingAgreementsService = crossingAgreementsService;
    this.crossingAgreementsTaskListService = crossingAgreementsTaskListService;
  }

  private ModelAndView createOverviewModelAndView(PwaApplicationDetail detail) {
    var modelAndView = new ModelAndView("pwaApplication/shared/crossings/overview")
        .addObject("overview", CrossingOverview.CABLE_CROSSINGS)
        .addObject("cableCrossings", padCableCrossingService.getCableCrossingViews(detail))
        .addObject("cableCrossingUrlFactory", new CableCrossingUrlFactory(detail))
        .addObject("cableCrossingFiles",
            cableCrossingFileService.getCableCrossingFileViews(detail, ApplicationFileLinkStatus.FULL))
        .addObject("backUrl", ReverseRouter.route(on(CrossingAgreementsController.class)
            .renderCrossingAgreementsOverview(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null,
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
    return ControllerUtils.checkErrorsAndRedirect(bindingResult, createRenderAddModelAndView(detail), () -> {
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
    return ControllerUtils.checkErrorsAndRedirect(bindingResult, createRenderEditModelAndView(detail), () -> {
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
