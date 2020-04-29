package uk.co.ogauthority.pwa.controller.pwaapplications.shared.crossings;

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
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.crossings.CrossingDocumentsForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.PwaApplicationFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.MedianLineCrossingFileService;
import uk.co.ogauthority.pwa.util.ControllerUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/crossings/median-line-documents")
@PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.DEPOSIT_CONSENT
})
public class MedianLineDocumentsController extends PwaApplicationDataFileUploadAndDownloadController {

  private final PwaApplicationFileService applicationFileService;
  private final MedianLineCrossingFileService medianLineCrossingFileService;
  private final ApplicationBreadcrumbService applicationBreadcrumbService;

  @Autowired
  public MedianLineDocumentsController(
      PwaApplicationFileService applicationFileService,
      MedianLineCrossingFileService medianLineCrossingFileService,
      ApplicationBreadcrumbService applicationBreadcrumbService) {
    this.applicationFileService = applicationFileService;
    this.medianLineCrossingFileService = medianLineCrossingFileService;
    this.applicationBreadcrumbService = applicationBreadcrumbService;
  }

  private ModelAndView createMedianLineCrossingModelAndView(PwaApplicationDetail pwaApplicationDetail,
                                                            CrossingDocumentsForm form) {
    var modelAndView = createModelAndView(
        "pwaApplication/form/uploadFiles",
        ReverseRouter.route(on(MedianLineDocumentsController.class)
            .handleUpload(pwaApplicationDetail.getPwaApplicationType(),
                pwaApplicationDetail.getMasterPwaApplicationId(), null, null)),
        ReverseRouter.route(on(MedianLineDocumentsController.class)
            .handleDownload(pwaApplicationDetail.getPwaApplicationType(),
                pwaApplicationDetail.getMasterPwaApplicationId(), null, null)),
        ReverseRouter.route(on(MedianLineDocumentsController.class)
            .handleDelete(pwaApplicationDetail.getPwaApplicationType(),
                pwaApplicationDetail.getMasterPwaApplicationId(), null, null)),
        // only load fully linked (saved) files
        medianLineCrossingFileService.getUpdatedMedianLineCrossingFileViewsWhenFileOnForm(pwaApplicationDetail, form)
    );

    modelAndView.addObject("pageTitle", "Median line agreement documents")
        .addObject("backButtonText", "Back to crossing agreements")
        .addObject("backUrl", ReverseRouter.route(on(CrossingAgreementsController.class)
            .renderCrossingAgreementsOverview(pwaApplicationDetail.getPwaApplicationType(),
                pwaApplicationDetail.getMasterPwaApplicationId(), null, null)));
    applicationBreadcrumbService.fromCrossings(pwaApplicationDetail.getPwaApplication(), modelAndView,
        "Median line agreement documents");
    return modelAndView;
  }

  @GetMapping
  public ModelAndView renderEditMedianLineCrossingDocuments(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @ModelAttribute("form") CrossingDocumentsForm form,
      PwaApplicationContext applicationContext) {

    medianLineCrossingFileService.mapDocumentsToForm(applicationContext.getApplicationDetail(), form);
    return createMedianLineCrossingModelAndView(applicationContext.getApplicationDetail(), form);
  }

  @PostMapping
  public ModelAndView postMedianLineCrossingDocuments(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @ModelAttribute("form") CrossingDocumentsForm form,
      BindingResult bindingResult,
      PwaApplicationContext applicationContext) {

    var detail = applicationContext.getApplicationDetail();
    medianLineCrossingFileService.validate(
        form,
        bindingResult,
        ValidationType.FULL,
        applicationContext.getApplicationDetail()
    );
    var modelAndView = createMedianLineCrossingModelAndView(applicationContext.getApplicationDetail(), form);
    return ControllerUtils.checkErrorsAndRedirect(bindingResult, modelAndView, () -> {

      medianLineCrossingFileService.updateOrDeleteLinkedFilesUsingForm(
          applicationContext.getApplicationDetail(),
          form,
          applicationContext.getUser());
      return ReverseRouter.redirect(on(CrossingAgreementsController.class)
          .renderCrossingAgreementsOverview(applicationType, detail.getMasterPwaApplicationId(), null, null));
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
    var medianLineCrossingFile = medianLineCrossingFileService.getMedianLineCrossingFile(fileId,
        applicationContext.getApplicationDetail());
    return serveFile(applicationFileService.getUploadedFile(medianLineCrossingFile));
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
        medianLineCrossingFileService::createUploadedFileLink
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
        medianLineCrossingFileService::deleteUploadedFileLink
    );
  }
}
