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
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.config.fileupload.FileDeleteResult;
import uk.co.ogauthority.pwa.config.fileupload.FileUploadResult;
import uk.co.ogauthority.pwa.controller.files.PwaApplicationDataFileUploadAndDownloadController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.techdetails.PipelineDrawingForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.FileUpdateMode;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.PadTechnicalDrawingService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.PipelineDrawingUrlFactory;
import uk.co.ogauthority.pwa.util.ControllerUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/technical-drawings/pipeline-drawings")
@PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION
})
public class PipelineDrawingController extends PwaApplicationDataFileUploadAndDownloadController {

  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final PadPipelineService padPipelineService;
  private final PadTechnicalDrawingService padTechnicalDrawingService;
  private final PadFileService padFileService;

  private final ApplicationFilePurpose filePurpose = ApplicationFilePurpose.PIPELINE_DRAWINGS;

  @Autowired
  public PipelineDrawingController(
      ApplicationBreadcrumbService applicationBreadcrumbService,
      PadPipelineService padPipelineService,
      PadTechnicalDrawingService padTechnicalDrawingService,
      PadFileService padFileService) {
    super(padFileService);
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.padPipelineService = padPipelineService;
    this.padTechnicalDrawingService = padTechnicalDrawingService;
    this.padFileService = padFileService;
  }

  private ModelAndView getDrawingModelAndView(PwaApplicationDetail detail, PipelineDrawingForm form) {

    var modelAndView = this.createModelAndView(
        "pwaApplication/shared/techdrawings/addPipelineDrawing",
        detail,
        filePurpose,
        form)
        .addObject("pipelineViews", padPipelineService.getPipelineOverviews(detail))
        .addObject("backUrl", ReverseRouter.route(on(TechnicalDrawingsController.class)
            .renderOverview(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null)));

    applicationBreadcrumbService.fromTechnicalDrawings(detail.getPwaApplication(), modelAndView,
        "Add pipeline drawing");

    padFileService.getFilesLinkedToForm(form, detail, filePurpose);
    return modelAndView;
  }

  private ModelAndView getRemoveDrawingModelAndView(PwaApplicationDetail detail, Integer drawingId) {
    var modelAndView = new ModelAndView("pwaApplication/shared/techdrawings/removePipelineDrawing")
        .addObject("summary", padTechnicalDrawingService.getPipelineSummaryView(detail, drawingId))
        .addObject("urlFactory", new PipelineDrawingUrlFactory(detail))
        .addObject("backUrl", ReverseRouter.route(on(TechnicalDrawingsController.class)
            .renderOverview(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null)));
    applicationBreadcrumbService.fromTechnicalDrawings(detail.getPwaApplication(), modelAndView,
        "Remove pipeline drawing");
    return modelAndView;
  }

  @GetMapping("/new")
  public ModelAndView renderAddDrawing(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @ModelAttribute("form") PipelineDrawingForm form,
      PwaApplicationContext applicationContext) {

    return getDrawingModelAndView(applicationContext.getApplicationDetail(), form);
  }

  @PostMapping("/new")
  public ModelAndView postAddDrawing(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @ModelAttribute("form") PipelineDrawingForm form,
      BindingResult bindingResult,
      PwaApplicationContext applicationContext) {

    bindingResult = padTechnicalDrawingService.validateDrawing(form, bindingResult, ValidationType.FULL,
        applicationContext.getApplicationDetail());
    var modelAndView = getDrawingModelAndView(applicationContext.getApplicationDetail(), form);
    return ControllerUtils.checkErrorsAndRedirect(bindingResult, modelAndView, () -> {
      padFileService.updateFiles(
          form,
          applicationContext.getApplicationDetail(),
          filePurpose,
          FileUpdateMode.KEEP_UNLINKED_FILES,
          applicationContext.getUser());
      padTechnicalDrawingService.addDrawing(applicationContext.getApplicationDetail(), form);
      return ReverseRouter.redirect(on(TechnicalDrawingsController.class)
          .renderOverview(applicationType, applicationId, null, null));
    });
  }

  @GetMapping("/{drawingId}/remove")
  public ModelAndView renderRemoveDrawing(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @PathVariable("drawingId") Integer drawingId,
      PwaApplicationContext applicationContext) {

    return getRemoveDrawingModelAndView(applicationContext.getApplicationDetail(), drawingId);
  }

  @PostMapping("/{drawingId}/remove")
  public ModelAndView postRemoveDrawing(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @PathVariable("drawingId") Integer drawingId,
      PwaApplicationContext applicationContext,
      AuthenticatedUserAccount user) {

    padTechnicalDrawingService.removeDrawing(applicationContext.getApplicationDetail(), drawingId, user);

    return ReverseRouter.redirect(on(TechnicalDrawingsController.class)
        .renderOverview(applicationType, applicationId, null, null));
  }

  @Override
  @PostMapping("/file/upload")
  @ResponseBody
  public FileUploadResult handleUpload(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
      @PathVariable("applicationId") Integer applicationId,
      @RequestParam("file") MultipartFile file,
      PwaApplicationContext applicationContext) {
    return padFileService.processInitialUpload(
        file,
        applicationContext.getApplicationDetail(),
        filePurpose,
        applicationContext.getUser());
  }

  @Override
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

  @Override
  @PostMapping("/file/delete/{fileId}")
  @ResponseBody
  public FileDeleteResult handleDelete(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
      @PathVariable("applicationId") Integer applicationId,
      @PathVariable("fileId") String fileId,
      PwaApplicationContext applicationContext) {
    return padFileService.processFileDeletion(applicationContext.getPadFile(), applicationContext.getUser());
  }
}
