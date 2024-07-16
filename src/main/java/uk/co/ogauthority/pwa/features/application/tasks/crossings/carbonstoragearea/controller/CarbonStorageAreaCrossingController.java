package uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import jakarta.validation.Valid;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
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
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.features.application.files.PadFileService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.CrossingOwner;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea.AddCarbonStorageAreaCrossingForm;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea.AddCarbonStorageAreaFormValidator;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea.CarbonStorageAreaCrossingFileService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea.CarbonStorageAreaCrossingService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea.CarbonStorageCrossingUrlFactory;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea.EditCarbonStorageAreaCrossingForm;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea.EditCarbonStorageAreaCrossingFormValidator;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea.PadCrossedStorageArea;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock.AddBlockCrossingForm;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist.CrossingAgreementsTaskListService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist.CrossingOverview;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist.controller.CrossingAgreementsController;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationSearchUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.crossings.CrossingAgreementTask;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/crossings/storageArea")
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.DEPOSIT_CONSENT,
    PwaApplicationType.DECOMMISSIONING
})
@PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
public class CarbonStorageAreaCrossingController {
  private final ApplicationBreadcrumbService breadcrumbService;
  private final PortalOrganisationsAccessor portalOrganisationsAccessor;
  private final CrossingAgreementsTaskListService crossingAgreementsTaskListService;
  private final AddCarbonStorageAreaFormValidator addCarbonStorageAreaFormValidator;
  private final CarbonStorageAreaCrossingFileService fileService;

  private final PadFileService padFileService;
  private final EditCarbonStorageAreaCrossingFormValidator editCarbonStorageAreaCrossingFormValidator;
  private final CarbonStorageAreaCrossingService carbonStorageAreaCrossingService;
  private final ControllerHelperService controllerHelperService;

  @Autowired
  public CarbonStorageAreaCrossingController(
      ApplicationBreadcrumbService breadcrumbService,
      PortalOrganisationsAccessor portalOrganisationsAccessor,
      ControllerHelperService controllerHelperService,
      CrossingAgreementsTaskListService crossingAgreementsTaskListService,
      AddCarbonStorageAreaFormValidator addCarbonStorageAreaFormValidator,
      CarbonStorageAreaCrossingFileService fileService,
      PadFileService padFileService,
      EditCarbonStorageAreaCrossingFormValidator editCarbonStorageAreaCrossingFormValidator,
      CarbonStorageAreaCrossingService carbonStorageAreaCrossingService) {
    this.breadcrumbService = breadcrumbService;
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
    this.crossingAgreementsTaskListService = crossingAgreementsTaskListService;
    this.addCarbonStorageAreaFormValidator = addCarbonStorageAreaFormValidator;
    this.fileService = fileService;
    this.padFileService = padFileService;
    this.editCarbonStorageAreaCrossingFormValidator = editCarbonStorageAreaCrossingFormValidator;
    this.carbonStorageAreaCrossingService = carbonStorageAreaCrossingService;
    this.controllerHelperService = controllerHelperService;
  }

  private ModelAndView redirectToCrossingOverview(PwaApplicationContext applicationContext) {
    var detail = applicationContext.getApplicationDetail();
    return crossingAgreementsTaskListService.getOverviewRedirect(detail,
        CrossingAgreementTask.CARBON_STORAGE_AREAS);
  }

  @GetMapping
  public ModelAndView renderCarbonStorageCrossingOverview(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @ModelAttribute("form") Object form,
      PwaApplicationContext applicationContext) {
    var applicationDetail = applicationContext.getApplicationDetail();
    var urlFactory = new CarbonStorageCrossingUrlFactory(applicationDetail);
    var modelAndView = new ModelAndView("pwaApplication/shared/crossings/overview")
        .addObject("overview", CrossingOverview.CARBON_STORAGE_AREA)
        .addObject("carbonStorageCrossings",
            carbonStorageAreaCrossingService.getCrossedAreaViews(applicationDetail))
        .addObject("carbonStorageCrossingUrlFactory", urlFactory)
        .addObject("backUrl", ReverseRouter.route(on(CrossingAgreementsController.class)
            .renderCrossingAgreementsOverview(
                applicationDetail.getPwaApplicationType(),
                applicationDetail.getMasterPwaApplicationId(),
                null,
                null)))
        .addObject("isStorageAreaDocumentsRequired",
            carbonStorageAreaCrossingService.isDocumentsRequired(applicationDetail))
        .addObject("carbonStorageCrossingFiles",
        padFileService.getUploadedFileViews(applicationDetail, ApplicationDetailFilePurpose.CARBON_STORAGE_CROSSINGS,
            ApplicationFileLinkStatus.FULL));
    breadcrumbService.fromCrossings(
        applicationDetail.getPwaApplication(),
        modelAndView,
        "Carbon storage areas");
    return modelAndView;
  }

  @PostMapping
  public ModelAndView postOverview(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @ModelAttribute("form") AddBlockCrossingForm form,
      PwaApplicationContext applicationContext) {

    var detail = applicationContext.getApplicationDetail();
    return ReverseRouter.redirect(on(CrossingAgreementsController.class)
        .renderCrossingAgreementsOverview(
            detail.getPwaApplicationType(),
            detail.getMasterPwaApplicationId(),
            null,
            null));
  }

  @GetMapping("/new")
  public ModelAndView renderAddAreaCrossing(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @ModelAttribute("form") AddCarbonStorageAreaCrossingForm form,
      PwaApplicationContext applicationContext) {

    var urlFactory = new CarbonStorageCrossingUrlFactory(applicationContext.getApplicationDetail());
    var sortedOrganisationUnits = portalOrganisationsAccessor.getAllActiveOrganisationUnitsSearch()
        .stream()
        .sorted(Comparator.comparing(o -> o.getOrgSearchableUnitName().toLowerCase()))
        .collect(StreamUtils.toLinkedHashMap(o ->
            String.valueOf(o.getOrgUnitId()), PortalOrganisationSearchUnit::getOrgSearchableUnitName));

    var modelAndView = new ModelAndView("pwaApplication/shared/crossings/carbonStorageArea/addCarbonStorageArea")
        .addObject("errorList", List.of())
        .addObject("crossedOwnerOptions", CrossingOwner.asList())
        .addObject("backUrl", urlFactory.getOverviewCarbonStorageCrossingUrl())
        .addObject("orgUnits", sortedOrganisationUnits);
    breadcrumbService.fromCrossingSection(
        applicationContext.getApplicationDetail(),
        modelAndView,
        CrossingAgreementTask.CARBON_STORAGE_AREAS,
        "Add carbon storage area crossing");
    return modelAndView;
  }

  @PostMapping("/new")
  public ModelAndView actionAddAreaCrossing(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @Valid @ModelAttribute("form") AddCarbonStorageAreaCrossingForm form,
      BindingResult bindingResult,
      PwaApplicationContext applicationContext) {

    addCarbonStorageAreaFormValidator.validate(form, bindingResult, applicationContext.getApplicationDetail());

    return controllerHelperService.checkErrorsAndRedirect(
        bindingResult,
        renderAddAreaCrossing(
            applicationType,
            applicationId,
            form,
            applicationContext),
        () -> {
          carbonStorageAreaCrossingService.saveStorageAreaCrossings(
              applicationContext.getApplicationDetail(),
              form);
          return redirectToCrossingOverview(applicationContext);
        }
    );
  }

  @GetMapping("/{crossingId}/edit")
  public ModelAndView renderEditAreaCrossing(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @PathVariable("crossingId") Integer crossingId,
      @ModelAttribute("form") EditCarbonStorageAreaCrossingForm form,
      PwaApplicationContext applicationContext) {

    var crossedArea = carbonStorageAreaCrossingService.getById(crossingId);
    carbonStorageAreaCrossingService.mapToEditForm(crossedArea, form);
    return renderEditModelAndView(applicationContext, form, crossedArea);
  }

  private ModelAndView renderEditModelAndView(PwaApplicationContext applicationContext,
                                              EditCarbonStorageAreaCrossingForm form,
                                              PadCrossedStorageArea crossedStorageArea) {
    var sortedOrganisationUnits = portalOrganisationsAccessor.getAllActiveOrganisationUnitsSearch()
        .stream()
        .sorted(Comparator.comparing(o -> o.getOrgSearchableUnitName().toLowerCase()))
        .collect(StreamUtils.toLinkedHashMap(o ->
            String.valueOf(o.getOrgUnitId()), PortalOrganisationSearchUnit::getOrgSearchableUnitName));

    var urlFactory = new CarbonStorageCrossingUrlFactory(applicationContext.getApplicationDetail());


    var modelAndView = new ModelAndView("pwaApplication/shared/crossings/carbonStorageArea/editCarbonStorageAreaCrossing")
        .addObject("errorList", List.of())
        .addObject("crossedOwnerOptions", CrossingOwner.asList())
        .addObject("orgUnits", sortedOrganisationUnits)
        .addObject("reference", crossedStorageArea.getStorageAreaReference())
        .addObject("backUrl", urlFactory.getOverviewCarbonStorageCrossingUrl());
    breadcrumbService.fromCrossingSection(applicationContext.getApplicationDetail(), modelAndView,
        CrossingAgreementTask.CARBON_STORAGE_AREAS, "Edit carbon storage area crossing");
    return modelAndView;
  }

  @PostMapping("/{crossingId}/edit")
  public ModelAndView actionEditAreaCrossing(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @PathVariable("crossingId") Integer crossingId,
      @Valid @ModelAttribute("form") EditCarbonStorageAreaCrossingForm form,
      BindingResult bindingResult,
      PwaApplicationContext applicationContext) {

    editCarbonStorageAreaCrossingFormValidator.validate(form, bindingResult);
    var crossedArea = carbonStorageAreaCrossingService.getById(crossingId);
    return controllerHelperService.checkErrorsAndRedirect(
        bindingResult,
        renderEditModelAndView(
            applicationContext,
            form,
            crossedArea),
        () -> {
          carbonStorageAreaCrossingService.updateStorageAreaCrossings(form, crossingId);
          return redirectToCrossingOverview(applicationContext);
        }
    );
  }

  @GetMapping("/{crossingId}/remove")
  public ModelAndView renderRemoveAreaCrossing(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @PathVariable("crossingId") Integer crossingId,
      PwaApplicationContext applicationContext) {
    var crossedArea = carbonStorageAreaCrossingService.getCrossedAreaViews(applicationContext.getApplicationDetail())
        .stream()
        .filter(view -> Objects.equals(view.getId(), crossingId))
        .findFirst()
        .orElseThrow(() -> new PwaEntityNotFoundException("Crossed storage area not found with id:" + crossingId));

    var urlFactory = new CarbonStorageCrossingUrlFactory(applicationContext.getApplicationDetail());
    var modelAndView = new ModelAndView("pwaApplication/shared/crossings/carbonStorageArea/removeCarbonStorageAreaCrossing")
        .addObject("crossing", crossedArea)
        .addObject("backUrl", urlFactory.getOverviewCarbonStorageCrossingUrl());
    breadcrumbService.fromCrossingSection(applicationContext.getApplicationDetail(), modelAndView,
        CrossingAgreementTask.CARBON_STORAGE_AREAS, "Remove carbon storage area");
    return modelAndView;
  }

  @PostMapping("/{crossingId}/remove")
  public ModelAndView actionRemoveAreaCrossing(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @PathVariable("crossingId") Integer blockCrossingId,
      PwaApplicationContext applicationContext) {

    var crossedStorageArea = carbonStorageAreaCrossingService.getById(blockCrossingId);
    carbonStorageAreaCrossingService.removeCrossing(crossedStorageArea);
    return redirectToCrossingOverview(applicationContext);
  }
}
