package uk.co.ogauthority.pwa.controller.consultations.responses;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
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
import uk.co.ogauthority.pwa.controller.appprocessing.shared.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.controller.files.PwaApplicationDataFileUploadAndDownloadController;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.exception.FileLinkNotFoundException;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponseFileLink;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.AppFile;
import uk.co.ogauthority.pwa.model.entity.files.AppFilePurpose;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationResponseDataForm;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationResponseForm;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOptionGroup;
import uk.co.ogauthority.pwa.service.appprocessing.AppProcessingBreadcrumbService;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationResponseService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationViewService;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.fileupload.AppFileService;
import uk.co.ogauthority.pwa.util.CaseManagementUtils;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;
import uk.co.ogauthority.pwa.validators.consultations.ConsultationResponseValidator;

@Controller
@RequestMapping("/pwa-application-processing/{applicationType}/{applicationId}/consultation/{consultationRequestId}/respond")
public class ConsultationResponseController extends PwaApplicationDataFileUploadAndDownloadController {

  private final ConsultationResponseService consultationResponseService;
  private final ConsultationRequestService consultationRequestService;
  private final ConsultationViewService consultationViewService;
  private final ControllerHelperService controllerHelperService;
  private final AppProcessingBreadcrumbService breadcrumbService;
  private final ConsultationResponseValidator consultationResponseValidator;
  private final ConsulteeGroupTeamService consulteeGroupTeamService;

  private static final AppFilePurpose FILE_PURPOSE = AppFilePurpose.CONSULTATION_RESPONSE;

  @Autowired
  public ConsultationResponseController(ConsultationResponseService consultationResponseService,
                                        ConsultationRequestService consultationRequestService,
                                        ConsultationViewService consultationViewService,
                                        ControllerHelperService controllerHelperService,
                                        AppProcessingBreadcrumbService breadcrumbService,
                                        ConsultationResponseValidator consultationResponseValidator,
                                        AppFileService appFileService,
                                        ConsulteeGroupTeamService consulteeGroupTeamService) {
    super(appFileService);
    this.consultationResponseService = consultationResponseService;
    this.consultationRequestService = consultationRequestService;
    this.consultationViewService = consultationViewService;
    this.controllerHelperService = controllerHelperService;
    this.breadcrumbService = breadcrumbService;
    this.consultationResponseValidator = consultationResponseValidator;
    this.consulteeGroupTeamService = consulteeGroupTeamService;
  }

  @GetMapping
  @PwaAppProcessingPermissionCheck(permissions = {PwaAppProcessingPermission.CONSULTATION_RESPONDER})
  public ModelAndView renderResponder(@PathVariable("applicationId") Integer applicationId,
                                      @PathVariable("applicationType")
                                      @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                      @PathVariable("consultationRequestId") Integer consultationRequestId,
                                      PwaAppProcessingContext processingContext,
                                      @ModelAttribute("form") ConsultationResponseForm form) {

    return withAccessibleConsultation(processingContext, consultationRequestId, () -> {

      var formMap = processingContext.getActiveConsultationRequestOrThrow()
          .getConsultationResponseOptionGroups()
          .stream()
          .collect(Collectors.toMap(Function.identity(), g -> new ConsultationResponseDataForm()));

      form.setResponseDataForms(formMap);

      return getResponderModelAndView(processingContext, consultationRequestId, form);

    });

  }

  @PostMapping
  @PwaAppProcessingPermissionCheck(permissions = {PwaAppProcessingPermission.CONSULTATION_RESPONDER})
  public ModelAndView postResponder(@PathVariable("applicationId") Integer applicationId,
                                    @PathVariable("applicationType")
                                    @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                    @PathVariable("consultationRequestId") Integer consultationRequestId,
                                    PwaAppProcessingContext processingContext,
                                    AuthenticatedUserAccount authenticatedUserAccount,
                                    @ModelAttribute("form") ConsultationResponseForm form,
                                    BindingResult bindingResult) {

    return withAccessibleConsultation(processingContext, consultationRequestId, () -> {

      consultationResponseValidator.validate(form, bindingResult);

      var request = processingContext.getActiveConsultationRequestOrThrow().getConsultationRequest();

      return controllerHelperService.checkErrorsAndRedirect(bindingResult,
          getResponderModelAndView(processingContext, consultationRequestId, form), () -> {
            consultationResponseService.saveResponseAndCompleteWorkflow(form, request, authenticatedUserAccount);
            return CaseManagementUtils.redirectCaseManagement(processingContext);
          });

    });

  }

  private ModelAndView withAccessibleConsultation(PwaAppProcessingContext processingContext,
                                                  Integer consultationRequestId,
                                                  Supplier<ModelAndView> successSupplier) {

    // if consultation request linked to user on context is equal to the one we are hitting in the URL, ok to continue
    if (Objects.equals(processingContext.getActiveConsultationRequestId(), consultationRequestId)) {
      return successSupplier.get();
    }

    // otherwise error
    throw new AccessDeniedException(
        String.format("User with WUA ID: %s cannot respond to consultation request with id [%s] as they are not the assigned responder",
            processingContext.getUser().getWuaId(),
            consultationRequestId));

  }

  private ModelAndView getResponderModelAndView(PwaAppProcessingContext processingContext,
                                                Integer consultationRequestId,
                                                ConsultationResponseForm form) {

    var application = processingContext.getPwaApplication();
    var requestDto = processingContext.getActiveConsultationRequestOrThrow();

    var responseOptionGroupMap = requestDto.getConsultationResponseOptionGroups().stream()
        .sorted(Comparator.comparing(ConsultationResponseOptionGroup::getDisplayOrder))
        .collect(StreamUtils.toLinkedHashMap(Function.identity(), ConsultationResponseOptionGroup::getOptions));

    var modelAndView = createModelAndView(
        "consultation/responses/responderForm",
        processingContext.getPwaApplication(),
        FILE_PURPOSE,
        form
    );

    modelAndView.addObject("cancelUrl", CaseManagementUtils.routeCaseManagement(application))
        .addObject("responseOptionGroupMap", responseOptionGroupMap)
        .addObject("appRef", processingContext.getPwaApplication().getAppReference())
        .addObject("previousResponses", consultationViewService
            .getConsultationRequestViewsRespondedOnly(application, requestDto.getConsultationRequest()))
        .addObject("caseSummaryView", processingContext.getCaseSummaryView())
        .addObject("consulteeGroupName", requestDto.getConsulteeGroupName())
        .addObject("consultationResponseDocumentType", requestDto.getConsultationResponseDocumentType());

    breadcrumbService.fromCaseManagement(processingContext.getPwaApplication(), modelAndView, "Consultation response");

    return modelAndView;

  }

  @PostMapping("/file/upload")
  @PwaAppProcessingPermissionCheck(permissions = {PwaAppProcessingPermission.VIEW_APPLICATION_SUMMARY})
  @ResponseBody
  public FileUploadResult handleUpload(@PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                       @PathVariable Integer applicationId,
                                       @RequestParam("file") MultipartFile file,
                                       PwaAppProcessingContext processingContext) {
    return withActiveConsultationRequest(
        processingContext,
        () -> appFileService.processInitialUpload(
            file,
            processingContext.getPwaApplication(),
            FILE_PURPOSE,
            processingContext.getUser())
    );
  }

  @GetMapping("/files/download/{fileId}")
  @PwaAppProcessingPermissionCheck(permissions = {PwaAppProcessingPermission.VIEW_APPLICATION_SUMMARY})
  @ResponseBody
  public ResponseEntity<Resource> handleDownload(@PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                 @PathVariable Integer applicationId,
                                                 @PathVariable("fileId") String fileId,
                                                 PwaAppProcessingContext processingContext) {
    return checkPermissionsAndDownloadFile(processingContext);
  }

  @PostMapping("/file/delete/{fileId}")
  @PwaAppProcessingPermissionCheck(permissions = {PwaAppProcessingPermission.VIEW_APPLICATION_SUMMARY})
  @ResponseBody
  public FileDeleteResult handleDelete(@PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                       @PathVariable Integer applicationId,
                                       @PathVariable("fileId") String fileId,
                                       PwaAppProcessingContext processingContext) {
    return checkPermissionsAndDeleteFile(processingContext);
  }

  private FileUploadResult withActiveConsultationRequest(PwaAppProcessingContext processingContext,
                                                 Supplier<FileUploadResult> supplier) {
    if (processingContext.getActiveConsultationRequest().isPresent()) {
      return supplier.get();
    } else {
      throw new AccessDeniedException(String.format(
          "User with wua id %s access files for this consultation request because there is no active consultation request.",
          processingContext.getUser().getWuaId()));
    }
  }

  private FileDeleteResult checkPermissionsAndDeleteFile(PwaAppProcessingContext processingContext) {
    return whenUserCanAccessFile(
        processingContext,
        () -> isUserInConsulteeTeamForActiveConsultation(processingContext),
        () -> appFileService.processFileDeletionWithPreDeleteAction(
            processingContext.getAppFile(),
            processingContext.getUser(),
            appFile -> consultationResponseService.getConsultationResponseFileLink(appFile)
                .ifPresent(consultationResponseService::deleteConsultationResponseFileLink)
        )
    );
  }

  private ResponseEntity<Resource> checkPermissionsAndDownloadFile(PwaAppProcessingContext processingContext) {
    return whenUserCanAccessFile(
        processingContext,
        () -> isUserInConsulteeTeamForActiveConsultation(processingContext)
            || processingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.VIEW_ALL_CONSULTATIONS),
        () -> serveFile(processingContext.getAppFile())
        );
  }

  private <T> T whenUserCanAccessFile(PwaAppProcessingContext processingContext,
                                      BooleanSupplier booleanSupplier,
                                      Supplier<T> supplier) {
    //if consultation request hasn't been saved yet we can just check that the user is the original uploader
    if (isAppFileTemporary(processingContext.getAppFile()) && isUserOriginalUploader(processingContext)) {
      return supplier.get();
    }

    var consultationRequest = getConsultationRequestFromProcessingContext(processingContext);

    if (booleanSupplier.getAsBoolean()) {
      return supplier.get();
    } else {
      throw new AccessDeniedException(String.format(
          "User with wua id %s cannot access files for consultation request %s because they do not have sufficient permissions.",
          processingContext.getUser().getWuaId(), consultationRequest.getId()));
    }
  }

  private boolean isUserInConsulteeTeamForActiveConsultation(PwaAppProcessingContext processingContext) {
    var consultationRequest = getConsultationRequestFromProcessingContext(processingContext);
    var consulteeGroup = consultationRequest.getConsulteeGroup();
    return consulteeGroupTeamService.getTeamMemberByGroupAndPerson(consulteeGroup, processingContext.getUser().getLinkedPerson())
        .isPresent();
  }

  private boolean isUserOriginalUploader(PwaAppProcessingContext processingContext) {
    var uploadedFile = appFileService.getUploadedFileById(processingContext.getAppFile().getFileId());
    return uploadedFile.getUploadedByWuaId().equals(processingContext.getUser().getWuaId());
  }

  private boolean isAppFileTemporary(AppFile appFile) {
    return appFile.getFileLinkStatus() == ApplicationFileLinkStatus.TEMPORARY;
  }

  private ConsultationRequest getConsultationRequestFromProcessingContext(PwaAppProcessingContext processingContext) {
    var consultationResponseFileLink = getConsultationResponseFileLinkOrThrow(processingContext);
    return consultationResponseFileLink.getConsultationResponse().getConsultationRequest();
  }

  private ConsultationResponseFileLink getConsultationResponseFileLinkOrThrow(PwaAppProcessingContext processingContext) {
    return consultationResponseService.getConsultationResponseFileLink(processingContext.getAppFile())
        .orElseThrow(() -> new FileLinkNotFoundException(String.format(
            "No file link found for file with id %s in pwa app with id %s",
            processingContext.getAppFile().getFileId(), processingContext.getPwaApplication().getId())));
  }

}