package uk.co.ogauthority.pwa.controller.pwaapplications.shared.location;

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
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.model.entity.devuk.DevukFacility;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.enums.HseSafetyZone;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.location.LocationDetailsForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.devuk.DevukFacilityService;
import uk.co.ogauthority.pwa.service.devuk.PadFacilityService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.location.LocationDetailsUrlFactory;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.location.PadLocationDetailFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.location.PadLocationDetailsService;
import uk.co.ogauthority.pwa.util.ControllerUtils;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;
import uk.co.ogauthority.pwa.validators.LocationDetailsValidator;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/location")
@PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.OPTIONS_VARIATION,
    PwaApplicationType.DECOMMISSIONING,
    PwaApplicationType.DEPOSIT_CONSENT
})
public class LocationDetailsController {

  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final DevukFacilityService devukFacilityService;
  private final PadFacilityService padFacilityService;
  private final PadLocationDetailsService padLocationDetailsService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final PadLocationDetailFileService padLocationDetailFileService;

  @Autowired
  public LocationDetailsController(
      ApplicationBreadcrumbService applicationBreadcrumbService,
      PadFacilityService padFacilityService,
      DevukFacilityService devukFacilityService,
      PadLocationDetailsService padLocationDetailsService,
      PwaApplicationRedirectService pwaApplicationRedirectService,
      LocationDetailsValidator locationDetailsValidator,
      PadLocationDetailFileService padLocationDetailFileService) {
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.padFacilityService = padFacilityService;
    this.devukFacilityService = devukFacilityService;
    this.padLocationDetailsService = padLocationDetailsService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.padLocationDetailFileService = padLocationDetailFileService;
  }

  private ModelAndView getLocationModelAndView(PwaApplicationDetail detail) {
    var facilities = devukFacilityService.getFacilities();
    var modelAndView = new ModelAndView("pwaApplication/shared/locationDetails")
        .addObject("safetyZoneOptions", HseSafetyZone.stream()
            .sorted()
            .collect(StreamUtils.toLinkedHashMap(Enum::name, HseSafetyZone::getDisplayText)))
        .addObject("facilityOptions", facilities.stream()
            .collect(
                StreamUtils.toLinkedHashMap(facility -> facility.getId().toString(), DevukFacility::getFacilityName)))
        .addObject("urlFactory",
            new LocationDetailsUrlFactory(detail.getPwaApplicationType(), detail.getPwaApplication().getId()))
        .addObject("uploadedFiles",
            padLocationDetailFileService.getLocationDetailFileViews(detail, ApplicationFileLinkStatus.FULL));
    applicationBreadcrumbService.fromTaskList(detail.getPwaApplication(), modelAndView, "Location details");
    return modelAndView;
  }

  @GetMapping
  public ModelAndView renderLocationDetails(@PathVariable("applicationType")
                                            @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                            PwaApplicationContext applicationContext,
                                            @ModelAttribute("form") LocationDetailsForm form,
                                            AuthenticatedUserAccount user) {
    var locationDetail = padLocationDetailsService.getLocationDetailsForDraft(
        applicationContext.getApplicationDetail());
    var facilities = padFacilityService.getFacilities(applicationContext.getApplicationDetail());
    padLocationDetailsService.mapEntityToForm(locationDetail, form);
    padFacilityService.mapFacilitiesToForm(facilities, form);
    return getLocationModelAndView(applicationContext.getApplicationDetail());
  }

  @PostMapping
  public ModelAndView postLocationDetails(@PathVariable("applicationType")
                                          @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                          PwaApplicationContext applicationContext,
                                          @ModelAttribute("form") LocationDetailsForm form,
                                          BindingResult bindingResult,
                                          AuthenticatedUserAccount user,
                                          ValidationType validationType) {

    var detail = applicationContext.getApplicationDetail();
    bindingResult = padLocationDetailsService.validate(form,
        bindingResult,
        validationType,
        applicationContext.getApplicationDetail());

    return ControllerUtils.checkErrorsAndRedirect(bindingResult, getLocationModelAndView(detail), () -> {
      var locationDetail = padLocationDetailsService.getLocationDetailsForDraft(detail);
      padLocationDetailsService.saveEntityUsingForm(locationDetail, form);
      padFacilityService.setFacilities(detail, form);
      return pwaApplicationRedirectService.getTaskListRedirect(detail.getPwaApplication());
    });

  }

  @PostMapping(params = "Add, edit or remove pipeline route documents")
  public ModelAndView postLocationDetailsToUploadDocuments(@PathVariable("applicationType")
                                                           @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                           PwaApplicationContext applicationContext,
                                                           @ModelAttribute("form") LocationDetailsForm form,
                                                           BindingResult bindingResult,
                                                           AuthenticatedUserAccount user) {

    var detail = applicationContext.getApplicationDetail();
    bindingResult = padLocationDetailsService.validate(form,
        bindingResult,
        ValidationType.PARTIAL,
        applicationContext.getApplicationDetail());

    return ControllerUtils.checkErrorsAndRedirect(bindingResult, getLocationModelAndView(detail), () -> {
      var locationDetail = padLocationDetailsService.getLocationDetailsForDraft(detail);
      padLocationDetailsService.saveEntityUsingForm(locationDetail, form);
      padFacilityService.setFacilities(detail, form);
      return ReverseRouter.redirect(on(LocationDetailsDocumentsController.class)
          .renderEditDocuments(pwaApplicationType, detail.getMasterPwaApplicationId(), null, null));
    });

  }

}
