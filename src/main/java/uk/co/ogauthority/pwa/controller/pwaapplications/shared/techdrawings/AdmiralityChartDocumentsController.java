package uk.co.ogauthority.pwa.controller.pwaapplications.shared.techdrawings;

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
import uk.co.ogauthority.pwa.config.fileupload.FileDeleteResult;
import uk.co.ogauthority.pwa.config.fileupload.FileUploadResult;
import uk.co.ogauthority.pwa.controller.files.PwaApplicationDataFileUploadAndDownloadController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.AdmiralityChartDocumentForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.PwaApplicationFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.AdmiralityChartFileService;
import uk.co.ogauthority.pwa.util.ControllerUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/technical-drawings/admirality-chart")
@PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.DEPOSIT_CONSENT
})
public class AdmiralityChartDocumentsController extends PwaApplicationDataFileUploadAndDownloadController {

  private final PwaApplicationFileService applicationFileService;
  private final AdmiralityChartFileService admiralityChartFileService;
  private final ApplicationBreadcrumbService applicationBreadcrumbService;

  @Autowired
  public AdmiralityChartDocumentsController(
      PwaApplicationFileService applicationFileService,
      AdmiralityChartFileService admiralityChartFileService,
      ApplicationBreadcrumbService applicationBreadcrumbService) {
    this.applicationFileService = applicationFileService;
    this.admiralityChartFileService = admiralityChartFileService;
    this.applicationBreadcrumbService = applicationBreadcrumbService;
  }

  private ModelAndView createCableCrossingModelAndView(PwaApplicationDetail pwaApplicationDetail,
                                                       AdmiralityChartDocumentForm form) {
    var modelAndView = createModelAndView(
        "pwaApplication/form/uploadFiles",
        ReverseRouter.route(on(AdmiralityChartDocumentsController.class)
            .handleUpload(pwaApplicationDetail.getPwaApplicationType(),
                pwaApplicationDetail.getMasterPwaApplicationId(), null, null)),
        ReverseRouter.route(on(AdmiralityChartDocumentsController.class)
            .handleDownload(pwaApplicationDetail.getPwaApplicationType(),
                pwaApplicationDetail.getMasterPwaApplicationId(), null, null)),
        ReverseRouter.route(on(AdmiralityChartDocumentsController.class)
            .handleDelete(pwaApplicationDetail.getPwaApplicationType(),
                pwaApplicationDetail.getMasterPwaApplicationId(), null, null)),
        // only load fully linked (saved) files
        admiralityChartFileService.getUpdatedAdmiralityChartFileViewsWhenFileOnForm(pwaApplicationDetail, form)
    );

    modelAndView.addObject("pageTitle", "Admirality chart")
        .addObject("backButtonText", "Back to technical drawings")
        .addObject("backUrl", ReverseRouter.route(on(TechnicalDrawingsController.class)
            .renderOverview(pwaApplicationDetail.getPwaApplicationType(),
                pwaApplicationDetail.getMasterPwaApplicationId(), null, null)));
    applicationBreadcrumbService.fromTechnicalDrawings(pwaApplicationDetail.getPwaApplication(), modelAndView,
        "Admirality chart");
    return modelAndView;
  }

  @GetMapping
  public ModelAndView renderEditCableCrossingDocuments(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @ModelAttribute("form") AdmiralityChartDocumentForm form,
      PwaApplicationContext applicationContext) {

    admiralityChartFileService.mapDocumentsToForm(applicationContext.getApplicationDetail(), form);
    return createCableCrossingModelAndView(applicationContext.getApplicationDetail(), form);
  }

  @PostMapping
  public ModelAndView postCableCrossingDocuments(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @ModelAttribute("form") AdmiralityChartDocumentForm form,
      BindingResult bindingResult,
      PwaApplicationContext applicationContext) {

    var detail = applicationContext.getApplicationDetail();
    admiralityChartFileService.validate(
        form,
        bindingResult,
        ValidationType.FULL,
        applicationContext.getApplicationDetail()
    );
    var modelAndView = createCableCrossingModelAndView(applicationContext.getApplicationDetail(), form);
    return ControllerUtils.checkErrorsAndRedirect(bindingResult, modelAndView, () -> {

      admiralityChartFileService.updateOrDeleteLinkedFilesUsingForm(
          applicationContext.getApplicationDetail(),
          form,
          applicationContext.getUser());
      return ReverseRouter.redirect(on(TechnicalDrawingsController.class)
          .renderOverview(applicationType, detail.getMasterPwaApplicationId(), null, null));
    });
  }

  @GetMapping("/files/download/{fileId}")
  @PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.VIEW})
  @ResponseBody
  public ResponseEntity<Resource> handleDownload(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @PathVariable("fileId") String fileId,
      PwaApplicationContext applicationContext) {
    var cableCrossingFile = admiralityChartFileService.getAdmiralityChartFile(fileId,
        applicationContext.getApplicationDetail());
    return serveFile(applicationFileService.getUploadedFile(cableCrossingFile));
  }

  @PostMapping("/files/upload")
  @ResponseBody
  public FileUploadResult handleUpload(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
      @PathVariable("applicationId") Integer applicationId,
      @RequestParam("file") MultipartFile file,
      PwaApplicationContext applicationContext) {

    // not creating full link until Save is clicked.
    return applicationFileService.processApplicationFileUpload(
        file,
        applicationContext.getUser(),
        applicationContext.getApplicationDetail(),
        admiralityChartFileService::createUploadedFileLink
    );
  }

  @PostMapping("/files/delete/{fileId}")
  @ResponseBody
  public FileDeleteResult handleDelete(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
      @PathVariable("applicationId") Integer applicationId,
      @PathVariable("fileId") String fileId,
      PwaApplicationContext applicationContext) {
    return applicationFileService.processApplicationFileDelete(
        fileId,
        applicationContext.getApplicationDetail(),
        applicationContext.getUser(),
        admiralityChartFileService::deleteUploadedFileLink
    );
  }
}
