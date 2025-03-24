package uk.co.ogauthority.pwa.controller.consultations.responses;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.fds.FileDeleteResponse;
import uk.co.ogauthority.pwa.domain.pwa.application.service.PwaApplicationService;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.exception.FileLinkNotFoundException;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationNoChecks;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.filemanagement.AppFileManagementService;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponseFileLink;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationFileService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationResponseService;
import uk.co.ogauthority.pwa.service.fileupload.AppFileService;

/**
 * Dedicated file controller to process consultation-response file requests.
 * Reason for @PwaApplicationNoChecks is that we opted for more fine-tuned access checks inside the methods.
 */
@Controller
@PwaApplicationNoChecks
@RequestMapping("/pwa/{applicationId}/consultation/file-management/{consultationRequestId}")
public class ConsultationResponseFileController {

  private static final FileDocumentType DOCUMENT_TYPE = FileDocumentType.CONSULTATION_RESPONSE;

  private final ConsultationResponseService consultationResponseService;
  private final ConsulteeGroupTeamService consulteeGroupTeamService;
  private final ConsultationFileService consultationFileService;
  private final PwaApplicationService pwaApplicationService;
  private final FileService fileService;
  private final AppFileManagementService appFileManagementService;
  private final AppFileService appFileService;

  @Autowired
  public ConsultationResponseFileController(
      ConsultationResponseService consultationResponseService,
      ConsulteeGroupTeamService consulteeGroupTeamService,
      ConsultationFileService consultationFileService,
      PwaApplicationService pwaApplicationService,
      FileService fileService,
      AppFileManagementService appFileManagementService,
      AppFileService appFileService
  ) {
    this.consultationResponseService = consultationResponseService;
    this.consulteeGroupTeamService = consulteeGroupTeamService;
    this.consultationFileService = consultationFileService;
    this.pwaApplicationService = pwaApplicationService;
    this.fileService = fileService;
    this.appFileManagementService = appFileManagementService;
    this.appFileService = appFileService;
  }

  @GetMapping("/download/{fileId}")
  public ResponseEntity<InputStreamResource> download(
      @PathVariable Integer applicationId,
      @PathVariable UUID fileId,
      PwaAppProcessingContext processingContext
  ) {
    var consultationRequest = getConsultationRequestFromProcessingContext(processingContext);

    var isAuthorized = isUserInConsulteeTeamForActiveConsultation(processingContext)
        || processingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.VIEW_ALL_CONSULTATIONS)
        || consultationFileService.industryUserCanAccessFile(processingContext);

    if (!isAuthorized) {
      throw new AccessDeniedException(String.format(
          "User with wua id %s cannot access files for consultation request %s because they do not have sufficient permissions.",
          processingContext.getUser().getWuaId(), consultationRequest.getId()));
    }

    var pwaApplication = pwaApplicationService.getApplicationFromId(applicationId);

    var file = fileService.find(fileId)
        .orElseThrow(() -> appFileManagementService.getFileNotFoundException(pwaApplication, fileId));

    appFileManagementService.throwIfFileDoesNotBelongToApplicationOrDocumentType(file, pwaApplication, DOCUMENT_TYPE);

    return fileService.download(file);
  }

  @PostMapping("/delete/{fileId}")
  public FileDeleteResponse delete(
      @PathVariable Integer applicationId,
      @PathVariable UUID fileId,
      PwaAppProcessingContext processingContext
  ) {
    var consultationRequest = getConsultationRequestFromProcessingContext(processingContext);

    if (!isUserInConsulteeTeamForActiveConsultation(processingContext)) {
      throw new AccessDeniedException(String.format(
          "User with wua id %s cannot access files for consultation request %s because they do not have sufficient permissions.",
          processingContext.getUser().getWuaId(), consultationRequest.getId()));
    }

    var pwaApplication = pwaApplicationService.getApplicationFromId(applicationId);

    var file = fileService.find(fileId)
        .orElseThrow(() -> appFileManagementService.getFileNotFoundException(pwaApplication, fileId));

    appFileManagementService.throwIfFileDoesNotBelongToApplicationOrDocumentType(file, pwaApplication, DOCUMENT_TYPE);

    var appFile = appFileService.getAppFileByPwaApplicationAndFileId(pwaApplication, String.valueOf(fileId));
    appFileService.processFileDeletion(appFile);

    return fileService.delete(file);
  }

  private boolean isUserInConsulteeTeamForActiveConsultation(PwaAppProcessingContext processingContext) {
    var consultationRequest = getConsultationRequestFromProcessingContext(processingContext);
    var consulteeGroup = consultationRequest.getConsulteeGroup();
    return consulteeGroupTeamService.getTeamMemberByGroupAndPerson(consulteeGroup, processingContext.getUser().getLinkedPerson())
        .isPresent();
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
