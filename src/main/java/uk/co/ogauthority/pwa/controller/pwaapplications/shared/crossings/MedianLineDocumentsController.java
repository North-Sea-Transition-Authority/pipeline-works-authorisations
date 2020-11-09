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
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.config.fileupload.FileDeleteResult;
import uk.co.ogauthority.pwa.config.fileupload.FileUploadResult;
import uk.co.ogauthority.pwa.controller.files.PwaApplicationDetailDataFileUploadAndDownloadController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.crossings.CrossingDocumentsForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.crossings.CrossingAgreementTask;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.FileUpdateMode;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.fileupload.PwaApplicationFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.MedianLineCrossingFileService;
import uk.co.ogauthority.pwa.service.tasklist.CrossingAgreementsTaskListService;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/crossings/median-line-documents")
@PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.DECOMMISSIONING
})
public class MedianLineDocumentsController extends PwaApplicationDetailDataFileUploadAndDownloadController {

  private final PwaApplicationFileService applicationFileService;
  private final MedianLineCrossingFileService medianLineCrossingFileService;
  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final CrossingAgreementsTaskListService crossingAgreementsTaskListService;
  private final ControllerHelperService controllerHelperService;
  private static final ApplicationDetailFilePurpose FILE_PURPOSE = ApplicationDetailFilePurpose.MEDIAN_LINE_CROSSING;

  @Autowired
  public MedianLineDocumentsController(
      PwaApplicationFileService applicationFileService,
      MedianLineCrossingFileService medianLineCrossingFileService,
      ApplicationBreadcrumbService applicationBreadcrumbService,
      CrossingAgreementsTaskListService crossingAgreementsTaskListService,
      PadFileService padFileService,
      ControllerHelperService controllerHelperService) {
    super(padFileService);
    this.applicationFileService = applicationFileService;
    this.medianLineCrossingFileService = medianLineCrossingFileService;
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.crossingAgreementsTaskListService = crossingAgreementsTaskListService;
    this.controllerHelperService = controllerHelperService;
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
        padFileService.getFilesLinkedToForm(form, pwaApplicationDetail, FILE_PURPOSE)
    );

    modelAndView.addObject("pageTitle", "Median line agreement documents")
        .addObject("backButtonText", "Back to median line crossing")
        .addObject("backUrl",
            crossingAgreementsTaskListService.getRoute(pwaApplicationDetail, CrossingAgreementTask.MEDIAN_LINE));
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

    padFileService.mapFilesToForm(form, applicationContext.getApplicationDetail(), FILE_PURPOSE);
    return createMedianLineCrossingModelAndView(applicationContext.getApplicationDetail(), form);
  }

  @PostMapping
  public ModelAndView postMedianLineCrossingDocuments(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @ModelAttribute("form") CrossingDocumentsForm form,
      BindingResult bindingResult,
      PwaApplicationContext applicationContext,
      AuthenticatedUserAccount user) {

    var detail = applicationContext.getApplicationDetail();
    medianLineCrossingFileService.validate(
        form,
        bindingResult,
        ValidationType.FULL,
        applicationContext.getApplicationDetail()
    );
    var modelAndView = createMedianLineCrossingModelAndView(applicationContext.getApplicationDetail(), form);
    return controllerHelperService.checkErrorsAndRedirect(bindingResult, modelAndView, () -> {

      padFileService.updateFiles(form, detail, FILE_PURPOSE, FileUpdateMode.DELETE_UNLINKED_FILES, user);
      return crossingAgreementsTaskListService.getOverviewRedirect(detail, CrossingAgreementTask.MEDIAN_LINE);
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
    return serveFile(applicationContext.getPadFile());
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
