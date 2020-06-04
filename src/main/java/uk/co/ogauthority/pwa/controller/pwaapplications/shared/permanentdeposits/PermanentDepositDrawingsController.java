package uk.co.ogauthority.pwa.controller.pwaapplications.shared.permanentdeposits;

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
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.enums.ScreenActionType;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PermanentDepositDrawingsForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.fileupload.PwaApplicationFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdepositdrawings.DepositDrawingUrlFactory;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdepositdrawings.DepositDrawingsService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits.PermanentDepositService;
import uk.co.ogauthority.pwa.util.ControllerUtils;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;


@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/permanent-deposit-drawings")
@PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.DEPOSIT_CONSENT,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.OPTIONS_VARIATION,
    PwaApplicationType.DECOMMISSIONING
})
public class PermanentDepositDrawingsController extends PwaApplicationDataFileUploadAndDownloadController {

  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final DepositDrawingsService depositDrawingsService;
  private final PermanentDepositService permanentDepositService;
  private final PadFileService padFileService;

  private final ApplicationFilePurpose filePurpose = ApplicationFilePurpose.DEPOSIT_DRAWINGS;

  @Autowired
  public PermanentDepositDrawingsController(ApplicationBreadcrumbService applicationBreadcrumbService,
                                            PwaApplicationRedirectService pwaApplicationRedirectService,
                                            DepositDrawingsService depositDrawingsService,
                                            PermanentDepositService permanentDepositService,
                                            PwaApplicationFileService applicationFileService,
                                            PadFileService padFileService) {
    super(padFileService);
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.depositDrawingsService = depositDrawingsService;
    this.permanentDepositService = permanentDepositService;
    this.padFileService = padFileService;
  }

  //Form Endpoints
  @GetMapping
  public ModelAndView renderDepositDrawingsOverview(@PathVariable("applicationType")
                                                      @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                      @PathVariable("applicationId") Integer applicationId,
                                                      PwaApplicationContext applicationContext,
                                                      @ModelAttribute("form") PermanentDepositDrawingsForm form) {
    return getDepositDrawingsOverviewModelAndView(applicationContext.getApplicationDetail());
  }

  @GetMapping("/add-deposit-drawing")
  public ModelAndView renderAddDepositDrawing(@PathVariable("applicationType")
                                                 @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                 @PathVariable("applicationId") Integer applicationId,
                                                 PwaApplicationContext applicationContext,
                                                 @ModelAttribute("form") PermanentDepositDrawingsForm form) {
    return getAddEditDepositDrawingModelAndView(applicationContext.getApplicationDetail(), form, ScreenActionType.ADD);
  }



  @PostMapping
  public ModelAndView postDepositDrawingsOverview(@PathVariable("applicationType")
                                            @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                            @PathVariable("applicationId") Integer applicationId,
                                            PwaApplicationContext applicationContext,
                                            @ModelAttribute("form") PermanentDepositDrawingsForm form,
                                            BindingResult bindingResult,
                                            ValidationType validationType) {
    return pwaApplicationRedirectService.getTaskListRedirect(applicationContext.getPwaApplication());
  }

  @PostMapping("/add-deposit-drawing")
  public ModelAndView postAddDepositDrawing(@PathVariable("applicationType")
                                            @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                            @PathVariable("applicationId") Integer applicationId,
                                            PwaApplicationContext applicationContext,
                                            @ModelAttribute("form") PermanentDepositDrawingsForm form,
                                            BindingResult bindingResult,
                                            ValidationType validationType) {
    bindingResult = depositDrawingsService.validate(form,
        bindingResult,
        validationType,
        applicationContext.getApplicationDetail());

    return ControllerUtils.checkErrorsAndRedirect(bindingResult,
        getAddEditDepositDrawingModelAndView(applicationContext.getApplicationDetail(), form, ScreenActionType.ADD), () -> {
          depositDrawingsService.addDrawing(applicationContext.getApplicationDetail(), form, applicationContext.getUser());
          return ReverseRouter.redirect(on(PermanentDepositDrawingsController.class).renderDepositDrawingsOverview(
              pwaApplicationType, applicationId, null, null));
        });
  }



  //Form model & views
  private ModelAndView getDepositDrawingsOverviewModelAndView(PwaApplicationDetail pwaApplicationDetail) {
    var modelAndView = new ModelAndView("pwaApplication/shared/permanentdepositdrawings/depositDrawingOverview");
    modelAndView.addObject("backUrl", pwaApplicationRedirectService.getTaskListRoute(pwaApplicationDetail.getPwaApplication()))
        .addObject("depositDrawingUrlFactory", new DepositDrawingUrlFactory(pwaApplicationDetail))
        .addObject("depositDrawingSummaryViews", depositDrawingsService.getDepositDrawingSummaryViews(pwaApplicationDetail));

    applicationBreadcrumbService.fromTaskList(pwaApplicationDetail.getPwaApplication(), modelAndView,
        "Permanent deposit drawings");
    return modelAndView;
  }


  private ModelAndView getAddEditDepositDrawingModelAndView(PwaApplicationDetail pwaApplicationDetail,
                                                            PermanentDepositDrawingsForm form, ScreenActionType type) {
    var modelAndView = this.createModelAndView("pwaApplication/shared/permanentdepositdrawings/depositDrawingsForm",
        pwaApplicationDetail,
        filePurpose,
        form);
    modelAndView.addObject("backUrl", ReverseRouter.route(on(PermanentDepositDrawingsController.class)
            .renderDepositDrawingsOverview(
                pwaApplicationDetail.getPwaApplicationType(), pwaApplicationDetail.getMasterPwaApplicationId(),null, null)))
        .addObject("depositOptions", permanentDepositService.getPermanentDeposits(pwaApplicationDetail)
            .stream().collect(StreamUtils.toLinkedHashMap(
                deposit -> String.valueOf(deposit.getId()), deposit -> deposit.getReference())))
        .addObject("screenAction", type);

    applicationBreadcrumbService.fromTaskList(pwaApplicationDetail.getPwaApplication(), modelAndView,
        "Permanent deposits");
    padFileService.getFilesLinkedToForm(form, pwaApplicationDetail, filePurpose);
    return modelAndView;
  }





  //File handling endpoints
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