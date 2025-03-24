package uk.co.ogauthority.pwa.features.application.tasks.locationdetails.controller;

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
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.tasks.locationdetails.HseSafetyZone;
import uk.co.ogauthority.pwa.features.application.tasks.locationdetails.LocationDetailsForm;
import uk.co.ogauthority.pwa.features.application.tasks.locationdetails.PadFacilityService;
import uk.co.ogauthority.pwa.features.application.tasks.locationdetails.PadLocationDetailsService;
import uk.co.ogauthority.pwa.features.application.tasks.locationdetails.PsrNotification;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementService;
import uk.co.ogauthority.pwa.integrations.energyportal.devukfacilities.controller.DevukRestController;
import uk.co.ogauthority.pwa.integrations.energyportal.devukfacilities.external.DevukFacility;
import uk.co.ogauthority.pwa.integrations.energyportal.devukfacilities.external.DevukFacilityService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.searchselector.SearchSelectorService;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/location")
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.DECOMMISSIONING,
    PwaApplicationType.DEPOSIT_CONSENT
})
public class LocationDetailsController {

  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final DevukFacilityService devukFacilityService;
  private final PadFacilityService padFacilityService;
  private final PadLocationDetailsService padLocationDetailsService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final ControllerHelperService controllerHelperService;
  private final PadFileManagementService padFileManagementService;

  //private static final ApplicationDetailFilePurpose FILE_PURPOSE = ApplicationDetailFilePurpose.LOCATION_DETAILS;

  @Autowired
  public LocationDetailsController(
      ApplicationBreadcrumbService applicationBreadcrumbService,
      PadFacilityService padFacilityService,
      DevukFacilityService devukFacilityService,
      PadLocationDetailsService padLocationDetailsService,
      PwaApplicationRedirectService pwaApplicationRedirectService,
      ControllerHelperService controllerHelperService,
      PadFileManagementService padFileManagementService
  ) {
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.padFacilityService = padFacilityService;
    this.devukFacilityService = devukFacilityService;
    this.padLocationDetailsService = padLocationDetailsService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.controllerHelperService = controllerHelperService;
    this.padFileManagementService = padFileManagementService;
  }

  private ModelAndView getLocationModelAndView(PwaApplicationDetail detail, LocationDetailsForm form) {
    var safetyZoneOptions = HseSafetyZone.stream().sorted()
        .collect(StreamUtils.toLinkedHashMap(Enum::name, HseSafetyZone::getDisplayText));

    var psrNotificationOptions = PsrNotification.stream().sorted()
        .collect(StreamUtils.toLinkedHashMap(Enum::name, PsrNotification::getDisplayText));

    var facilities = devukFacilityService.getFacilities("");

    var toShoreGuidanceText = detail.getResourceType() == PwaResourceType.PETROLEUM
        ? "Processed oil is stored on the FPSO before being exported onshore by tanker. Gas is either exported via a 16\" " +
        "flowline to Platform and onward to the SAGE system, or used as fuel or lift gas."
        : "";

    var fileUploadAttributes = padFileManagementService.getFileUploadComponentAttributes(
        form.getUploadedFiles(),
        detail,
        FileDocumentType.LOCATION_DETAILS
    );

    var modelAndView = new ModelAndView("pwaApplication/shared/locationDetails")
        .addObject("safetyZoneOptions", safetyZoneOptions)
        .addObject("psrNotificationOptions", psrNotificationOptions)
        .addObject("facilityOptions", facilities.stream()
            .collect(
                StreamUtils.toLinkedHashMap(facility -> facility.getId().toString(), DevukFacility::getFacilityName)))
        .addObject("facilityRestUrl",
            SearchSelectorService.route(on(DevukRestController.class).searchFacilities(null)))
        .addObject("requiredQuestions", padLocationDetailsService.getRequiredQuestions(detail))
        .addObject("toShoreGuidanceText", toShoreGuidanceText)
        .addObject("fileUploadAttributes", fileUploadAttributes);

    // Add preselection options in case validation fails
    if (form.getWithinSafetyZone() == HseSafetyZone.YES) {
      modelAndView.addObject("preselectedFacilitiesIfYes",
          padLocationDetailsService.reapplyFacilitySelections(form));
    } else if (form.getWithinSafetyZone() == HseSafetyZone.PARTIALLY) {
      modelAndView.addObject("preselectedFacilitiesIfPartially",
          padLocationDetailsService.reapplyFacilitySelections(form));
    }
    applicationBreadcrumbService.fromTaskList(detail.getPwaApplication(), modelAndView, "Location details");
    return modelAndView;
  }

  @GetMapping
  @PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
  public ModelAndView renderLocationDetails(@PathVariable("applicationType")
                                            @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                            PwaApplicationContext applicationContext,
                                            @ModelAttribute("form") LocationDetailsForm form,
                                            AuthenticatedUserAccount user) {
    var detail = applicationContext.getApplicationDetail();
    var locationDetail = padLocationDetailsService.getLocationDetailsForDraft(
        applicationContext.getApplicationDetail());
    var facilities = padFacilityService.getFacilities(applicationContext.getApplicationDetail());
    padLocationDetailsService.mapEntityToForm(locationDetail, form);

    padFileManagementService.mapFilesToForm(form, detail, FileDocumentType.LOCATION_DETAILS);
    var modelAndView = getLocationModelAndView(applicationContext.getApplicationDetail(), form);
    padFacilityService.mapFacilitiesToView(facilities, form, modelAndView);
    return modelAndView;
  }

  @PostMapping
  @PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
  public ModelAndView postLocationDetails(@PathVariable("applicationType")
                                          @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                          PwaApplicationContext applicationContext,
                                          @ModelAttribute("form") LocationDetailsForm form,
                                          BindingResult bindingResult,
                                          AuthenticatedUserAccount user,
                                          ValidationType validationType) {

    var detail = applicationContext.getApplicationDetail();
    bindingResult = padLocationDetailsService.validate(form, bindingResult, validationType, detail);

    return controllerHelperService.checkErrorsAndRedirect(bindingResult, getLocationModelAndView(detail, form), () -> {

      var locationDetail = padLocationDetailsService.getLocationDetailsForDraft(detail);
      padLocationDetailsService.saveEntityUsingForm(locationDetail, form);
      padFacilityService.setFacilities(detail, form);

      padFileManagementService.saveFiles(form, detail, FileDocumentType.LOCATION_DETAILS);

      return pwaApplicationRedirectService.getTaskListRedirect(detail.getPwaApplication());

    });

  }
}
