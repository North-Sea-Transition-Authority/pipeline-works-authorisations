package uk.co.ogauthority.pwa.controller.pwaapplications.shared.crossings;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.config.fileupload.FileDeleteResult;
import uk.co.ogauthority.pwa.config.fileupload.FileUploadResult;
import uk.co.ogauthority.pwa.controller.files.PwaApplicationDataFileUploadAndDownloadController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.crossings.CrossingDocumentsForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.PwaApplicationFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.PipelineCrossingFileService;
import uk.co.ogauthority.pwa.util.ControllerUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

public class PipelineCrossingDocumentsController extends PwaApplicationDataFileUploadAndDownloadController {

  private final PwaApplicationFileService applicationFileService;
  private final PipelineCrossingFileService pipelineCrossingFileService;
  private final ApplicationBreadcrumbService applicationBreadcrumbService;

  @Autowired
  public PipelineCrossingDocumentsController(
      PwaApplicationFileService applicationFileService,
      PipelineCrossingFileService pipelineCrossingFileService,
      ApplicationBreadcrumbService applicationBreadcrumbService) {
    this.applicationFileService = applicationFileService;
    this.pipelineCrossingFileService = pipelineCrossingFileService;
    this.applicationBreadcrumbService = applicationBreadcrumbService;
  }

  private ModelAndView createCableCrossingModelAndView(PwaApplicationDetail pwaApplicationDetail,
                                                       CrossingDocumentsForm form) {
    var modelAndView = createModelAndView(
        "pwaApplication/form/uploadFiles",
        ReverseRouter.route(on(CableCrossingDocumentsController.class)
            .handleUpload(pwaApplicationDetail.getPwaApplicationType(),
                pwaApplicationDetail.getMasterPwaApplicationId(), null, null)),
        ReverseRouter.route(on(CableCrossingDocumentsController.class)
            .handleDownload(pwaApplicationDetail.getPwaApplicationType(),
                pwaApplicationDetail.getMasterPwaApplicationId(), null, null)),
        ReverseRouter.route(on(CableCrossingDocumentsController.class)
            .handleDelete(pwaApplicationDetail.getPwaApplicationType(),
                pwaApplicationDetail.getMasterPwaApplicationId(), null, null)),
        // only load fully linked (saved) files
        pipelineCrossingFileService.getUpdatedPipelineCrossingFileViewsWhenFileOnForm(pwaApplicationDetail, form)
    );

    modelAndView.addObject("pageTitle", "Cable crossing agreement documents")
        .addObject("backButtonText", "Back to crossing agreements")
        .addObject("backUrl", ReverseRouter.route(on(CrossingAgreementsController.class)
            .renderCrossingAgreementsOverview(pwaApplicationDetail.getPwaApplicationType(),
                pwaApplicationDetail.getMasterPwaApplicationId(), null, null)));
    applicationBreadcrumbService.fromCrossings(pwaApplicationDetail.getPwaApplication(), modelAndView,
        "Cable crossing agreement documents");
    return modelAndView;
  }

  @GetMapping
  public ModelAndView renderEditCableCrossingDocuments(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @ModelAttribute("form") CrossingDocumentsForm form,
      PwaApplicationContext applicationContext) {

    pipelineCrossingFileService.mapDocumentsToForm(applicationContext.getApplicationDetail(), form);
    return createCableCrossingModelAndView(applicationContext.getApplicationDetail(), form);
  }

  @PostMapping
  public ModelAndView postCableCrossingDocuments(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @ModelAttribute("form") CrossingDocumentsForm form,
      BindingResult bindingResult,
      PwaApplicationContext applicationContext) {

    var detail = applicationContext.getApplicationDetail();
    pipelineCrossingFileService.validate(
        form,
        bindingResult,
        ValidationType.FULL,
        applicationContext.getApplicationDetail()
    );
    var modelAndView = createCableCrossingModelAndView(applicationContext.getApplicationDetail(), form);
    return ControllerUtils.checkErrorsAndRedirect(bindingResult, modelAndView, () -> {

      pipelineCrossingFileService.updateOrDeleteLinkedFilesUsingForm(
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
    var crossingFile = pipelineCrossingFileService.getPipelineCrossingFile(fileId,
        applicationContext.getApplicationDetail());
    return serveFile(applicationFileService.getUploadedFile(crossingFile));
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
        pipelineCrossingFileService::createUploadedFileLink
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
        pipelineCrossingFileService::deleteUploadedFileLink
    );
  }

}
