package uk.co.ogauthority.pwa.controller.pwaapplications.shared;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.config.fileupload.FileDeleteResult;
import uk.co.ogauthority.pwa.config.fileupload.FileUploadResult;
import uk.co.ogauthority.pwa.controller.files.PwaApplicationDetailDataFileUploadAndDownloadController;
import uk.co.ogauthority.pwa.controller.pwaapplications.rest.DevukRestController;
import uk.co.ogauthority.pwa.model.entity.devuk.DevukFacility;
import uk.co.ogauthority.pwa.model.entity.enums.HseSafetyZone;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.location.LocationDetailsForm;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.devuk.DevukFacilityService;
import uk.co.ogauthority.pwa.service.devuk.PadFacilityService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.FileUpdateMode;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.location.PadLocationDetailsService;
import uk.co.ogauthority.pwa.service.search.SearchSelectorService;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/location")
@PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.DECOMMISSIONING,
    PwaApplicationType.DEPOSIT_CONSENT
})
public class LocationDetailsController extends PwaApplicationDetailDataFileUploadAndDownloadController {

  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final DevukFacilityService devukFacilityService;
  private final PadFacilityService padFacilityService;
  private final PadLocationDetailsService padLocationDetailsService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final ControllerHelperService controllerHelperService;

  private static final ApplicationDetailFilePurpose FILE_PURPOSE = ApplicationDetailFilePurpose.LOCATION_DETAILS;

  @Autowired
  public LocationDetailsController(
      ApplicationBreadcrumbService applicationBreadcrumbService,
      PadFacilityService padFacilityService,
      DevukFacilityService devukFacilityService,
      PadLocationDetailsService padLocationDetailsService,
      PwaApplicationRedirectService pwaApplicationRedirectService,
      PadFileService padFileService,
      ControllerHelperService controllerHelperService) {
    super(padFileService);
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.padFacilityService = padFacilityService;
    this.devukFacilityService = devukFacilityService;
    this.padLocationDetailsService = padLocationDetailsService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.controllerHelperService = controllerHelperService;
  }

  private ModelAndView getLocationModelAndView(PwaApplicationDetail detail, LocationDetailsForm form) {

    var modelAndView = this.createModelAndView(
        "pwaApplication/shared/locationDetails",
        detail,
        FILE_PURPOSE,
        form
    );

    var facilities = devukFacilityService.getFacilities("");
    modelAndView.addObject("safetyZoneOptions", HseSafetyZone.stream()
        .sorted()
        .collect(StreamUtils.toLinkedHashMap(Enum::name, HseSafetyZone::getDisplayText)))
        .addObject("facilityOptions", facilities.stream()
            .collect(
                StreamUtils.toLinkedHashMap(facility -> facility.getId().toString(), DevukFacility::getFacilityName)))
        .addObject("facilityRestUrl",
            SearchSelectorService.route(on(DevukRestController.class).searchFacilities(null)))
        .addObject("requiredQuestions", padLocationDetailsService.getRequiredQuestions(detail.getPwaApplicationType()));

    // Add preselection options in case validation fails
    if (form.getSafetyZoneQuestionForm().getWithinSafetyZone() == HseSafetyZone.YES) {
      modelAndView.addObject("preselectedFacilitiesIfYes",
          padLocationDetailsService.reapplyFacilitySelections(form));
    } else if (form.getSafetyZoneQuestionForm().getWithinSafetyZone() == HseSafetyZone.PARTIALLY) {
      modelAndView.addObject("preselectedFacilitiesIfPartially",
          padLocationDetailsService.reapplyFacilitySelections(form));
    }
    applicationBreadcrumbService.fromTaskList(detail.getPwaApplication(), modelAndView, "Location details");
    return modelAndView;
  }

  @GetMapping
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

    padFileService.mapFilesToForm(form, detail, FILE_PURPOSE);
    var modelAndView = getLocationModelAndView(applicationContext.getApplicationDetail(), form);
    padFacilityService.mapFacilitiesToView(facilities, form, modelAndView);
    return modelAndView;
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
    bindingResult = padLocationDetailsService.validate(form, bindingResult, validationType, detail);

    return controllerHelperService.checkErrorsAndRedirect(bindingResult, getLocationModelAndView(detail, form), () -> {

      var locationDetail = padLocationDetailsService.getLocationDetailsForDraft(detail);
      padLocationDetailsService.saveEntityUsingForm(locationDetail, form);
      padFacilityService.setFacilities(detail, form);

      padFileService.updateFiles(
          form,
          applicationContext.getApplicationDetail(),
          FILE_PURPOSE,
          FileUpdateMode.DELETE_UNLINKED_FILES,
          applicationContext.getUser());

      return pwaApplicationRedirectService.getTaskListRedirect(detail.getPwaApplication());

    });

  }

  @PostMapping("/files/upload")
  @ResponseBody
  public FileUploadResult handleUpload(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
      @PathVariable("applicationId") Integer applicationId,
      @RequestParam("file") MultipartFile file,
      PwaApplicationContext applicationContext) {

    return padFileService.processInitialUpload(
        file,
        applicationContext.getApplicationDetail(),
        FILE_PURPOSE,
        applicationContext.getUser());

  }

  @GetMapping("/files/download/{fileId}")
  @PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.VIEW})
  @ResponseBody
  public ResponseEntity<Resource> handleDownload(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @PathVariable("fileId") String fileId,
      PwaApplicationContext applicationContext) {
    return serveFile(applicationContext.getPadFile());
  }

  @PostMapping("/files/delete/{fileId}")
  @ResponseBody
  public FileDeleteResult handleDelete(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
      @PathVariable("applicationId") Integer applicationId,
      @PathVariable("fileId") String fileId,
      PwaApplicationContext applicationContext) {
    return padFileService.processFileDeletion(applicationContext.getPadFile(), applicationContext.getUser());
  }

}
