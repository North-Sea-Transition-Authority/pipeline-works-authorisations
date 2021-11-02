package uk.co.ogauthority.pwa.controller.consultations.responses;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import uk.co.ogauthority.pwa.config.fileupload.FileDeleteResult;
import uk.co.ogauthority.pwa.config.fileupload.FileUploadResult;
import uk.co.ogauthority.pwa.controller.files.PwaApplicationDataFileUploadAndDownloadController;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.exception.FileLinkNotFoundException;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationNoChecks;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponseFileLink;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.AppFile;
import uk.co.ogauthority.pwa.model.entity.files.AppFilePurpose;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.consultations.ConsultationFileService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationResponseService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.fileupload.AppFileService;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

/**
 * Dedicated file controller to process consultation-response file requests.
 * Reason for @PwaApplicationNoChecks is that we opted for more fine-tuned access checks inside the methods.
 */
@Controller
@PwaApplicationNoChecks
@RequestMapping("/pwa-application-processing/{applicationType}/{applicationId}/consultation/{consultationRequestId}/respond/" +
    "file-processing")
public class ConsultationResponseFileController extends PwaApplicationDataFileUploadAndDownloadController {

  private static final AppFilePurpose FILE_PURPOSE = AppFilePurpose.CONSULTATION_RESPONSE;

  private final ConsultationResponseService consultationResponseService;
  private final ConsulteeGroupTeamService consulteeGroupTeamService;
  private final ConsultationFileService consultationFileService;

  @Autowired
  public ConsultationResponseFileController(
      ConsultationResponseService consultationResponseService,
      AppFileService appFileService,
      ConsulteeGroupTeamService consulteeGroupTeamService,
      ConsultationFileService consultationFileService) {
    super(appFileService);
    this.consultationResponseService = consultationResponseService;
    this.consulteeGroupTeamService = consulteeGroupTeamService;
    this.consultationFileService = consultationFileService;
  }

  @PostMapping("/file/upload")
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
  @ResponseBody
  public ResponseEntity<Resource> handleDownload(@PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                 @PathVariable Integer applicationId,
                                                 @PathVariable("fileId") String fileId,
                                                 PwaAppProcessingContext processingContext) {
    return checkPermissionsAndDownloadFile(processingContext);
  }

  @PostMapping("/file/delete/{fileId}")
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
            || processingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.VIEW_ALL_CONSULTATIONS)
            || consultationFileService.industryUserCanAccessFile(processingContext),
        () -> serveFile(processingContext.getAppFile())
    );
  }

  private <T> T whenUserCanAccessFile(PwaAppProcessingContext processingContext,
                                      BooleanSupplier booleanSupplier,
                                      Supplier<T> supplier) {
    //if consultation request hasn't been saved yet we can just check that the user is the original uploader
    if (isAppFileTemporary(processingContext.getAppFile())) {
      if (isUserOriginalUploader(processingContext)) {
        return supplier.get();
      } else {
        throw new AccessDeniedException(String.format(
            "User with wua id %s cannot access temporary files as they are not the original uploader.",
            processingContext.getUser().getWuaId()));
      }
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
