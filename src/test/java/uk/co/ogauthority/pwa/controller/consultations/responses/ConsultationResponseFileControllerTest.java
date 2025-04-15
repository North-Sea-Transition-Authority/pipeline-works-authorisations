package uk.co.ogauthority.pwa.controller.consultations.responses;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.fivium.fileuploadlibrary.fds.FileDeleteResponse;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaAppProcessingContextAbstractControllerTest;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.application.service.PwaApplicationService;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.ProcessingPermissionsDto;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.features.filemanagement.AppFileManagementService;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupTeamMember;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponse;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponseFileLink;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.AppFile;
import uk.co.ogauthority.pwa.model.entity.files.AppFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.consultations.ConsultationFileService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationResponseService;
import uk.co.ogauthority.pwa.testutils.PwaAppProcessingContextDtoTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.RouteUtils;

@WebMvcTest(controllers = ConsultationResponseFileController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaAppProcessingContextService.class))
class ConsultationResponseFileControllerTest extends PwaAppProcessingContextAbstractControllerTest {
  private static final Integer PWA_ID = 1;
  private static final UUID FILE_ID = UUID.randomUUID();
  private static final Class<ConsultationResponseFileController> CONTROLLER = ConsultationResponseFileController.class;

  @MockBean
  private FileService fileService;

  @MockBean
  private AppFileManagementService appFileManagementService;

  @MockBean
  private PwaApplicationService pwaApplicationService;

  @MockBean
  private ConsultationResponseService consultationResponseService;

  @MockBean
  private PwaAppProcessingPermissionService pwaAppProcessingPermissionService;

  @MockBean
  private ConsultationFileService consultationFileService;

  private AuthenticatedUserAccount user;

  private PwaApplicationDetail pwaApplicationDetail;

  private PwaApplication pwaApplication;

  private ConsultationResponse consultationResponse;

  private ConsultationRequest consultationRequest;

  private ConsultationResponseFileLink consultationResponseFileLink;

  private AppFile appFile;

  private ProcessingPermissionsDto permissionsDto;

  private ProcessingPermissionsDto noPermissionsDto;

  @BeforeEach
  void setUp() {
    var webUserAccount = new WebUserAccount(1);
    user = new AuthenticatedUserAccount(webUserAccount, Set.of(PwaUserPrivilege.PWA_ACCESS));

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(PWA_ID);

    when(pwaApplicationDetailService.getLatestDetailForUser(pwaApplicationDetail.getMasterPwaApplicationId(), user))
        .thenReturn(Optional.of(pwaApplicationDetail));

    pwaApplication = pwaApplicationDetail.getPwaApplication();

    consultationRequest = new ConsultationRequest();
    consultationRequest.setId(1);

    consultationResponse = new ConsultationResponse();
    consultationResponse.setConsultationRequest(consultationRequest);

    consultationResponseFileLink = new ConsultationResponseFileLink();
    consultationResponseFileLink.setConsultationResponse(consultationResponse);

    appFile = new AppFile(pwaApplication, String.valueOf(FILE_ID), AppFilePurpose.CONSULTATION_RESPONSE,
        ApplicationFileLinkStatus.FULL);
    appFile.setId(1);
    when(appFileService.getAppFileByPwaApplicationAndFileId(pwaApplication, String.valueOf(FILE_ID))).thenReturn(appFile);

    when(consultationResponseService.getConsultationResponseFileLink(appFile)).thenReturn(Optional.ofNullable(consultationResponseFileLink));

    permissionsDto = new ProcessingPermissionsDto(
        PwaAppProcessingContextDtoTestUtils.appInvolvementWithConsultationRequest("nme", consultationRequest),
        EnumSet.allOf(PwaAppProcessingPermission.class));
    noPermissionsDto = new ProcessingPermissionsDto(
        PwaAppProcessingContextDtoTestUtils.appInvolvementWithConsultationRequest("nme", consultationRequest),
        Set.of());
  }

  @Test
  void download() throws Exception {
    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(any(), eq(user))).thenReturn(permissionsDto);

    when(pwaApplicationService.getApplicationFromId(PWA_ID)).thenReturn(pwaApplication);

    var uploadedFile = new UploadedFile();
    when(fileService.find(FILE_ID)).thenReturn(Optional.of(uploadedFile));

    when(fileService.download(uploadedFile)).thenReturn(ResponseEntity.ok().build());

    mockMvc.perform(get(RouteUtils.routeWithUriVariables(on(CONTROLLER)
            .download(PWA_ID, FILE_ID, null), Map.of("consultationRequestId", "1")))
            .with(user(user)))
        .andExpect(status().isOk());

    verify(appFileManagementService).throwIfFileDoesNotBelongToApplicationOrDocumentType(uploadedFile, pwaApplication, FileDocumentType.CONSULTATION_RESPONSE);
    verify(fileService).download(uploadedFile);
  }

  @Test
  void download_invalidFileId() throws Exception {
    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(any(), eq(user))).thenReturn(permissionsDto);

    when(pwaApplicationService.getApplicationFromId(PWA_ID)).thenReturn(pwaApplication);

    when(fileService.find(FILE_ID)).thenReturn(Optional.empty());
    when(appFileManagementService.getFileNotFoundException(pwaApplication, FILE_ID))
        .thenReturn(new ResponseStatusException(HttpStatus.NOT_FOUND));

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER)
            .download(PWA_ID, FILE_ID, null), Map.of("consultationRequestId", "1")))
            .with(user(user)))
        .andExpect(status().isNotFound());

    verify(fileService, never()).download(any());
  }

  @Test
  void download_fileNotLinkedToApplication() throws Exception {
    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(any(), eq(user))).thenReturn(permissionsDto);

    when(pwaApplicationService.getApplicationFromId(PWA_ID)).thenReturn(pwaApplication);

    var uploadedFile = new UploadedFile();
    when(fileService.find(FILE_ID)).thenReturn(Optional.of(uploadedFile));

    doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND))
        .when(appFileManagementService)
        .throwIfFileDoesNotBelongToApplicationOrDocumentType(uploadedFile, pwaApplication, FileDocumentType.CONSULTATION_RESPONSE);

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER)
            .download(PWA_ID, FILE_ID, null), Map.of("consultationRequestId", "1")))
            .with(user(user)))
        .andExpect(status().isNotFound());

    verify(appFileManagementService).throwIfFileDoesNotBelongToApplicationOrDocumentType(uploadedFile, pwaApplication, FileDocumentType.CONSULTATION_RESPONSE);
    verify(fileService, never()).download(any());
  }

  @Test
  void download_noPermissions() throws Exception {
    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(any(), eq(user))).thenReturn(noPermissionsDto);

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER)
            .download(PWA_ID, FILE_ID, null), Map.of("consultationRequestId", "1")))
            .with(user(user)))
        .andExpect(status().isForbidden());

    verify(fileService, never()).download(any());
  }

  @Test
  void delete() throws Exception {
    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(any(), eq(user))).thenReturn(permissionsDto);
    when(consultationRequestService.getConsultationRequestByIdOrThrow(any())).thenReturn(consultationRequest);
    when(consultationResponseService.isUserAssignedResponderForConsultation(any(), any())).thenReturn(true);

    var teamMember = new ConsulteeGroupTeamMember(consultationRequest.getConsulteeGroup(), user.getLinkedPerson(), Set.of(
        ConsulteeGroupMemberRole.RESPONDER));

    when(consulteeGroupTeamService.getTeamMemberByGroupAndPerson(consultationRequest.getConsulteeGroup(), user.getLinkedPerson()))
        .thenReturn(Optional.of(teamMember));

    when(pwaApplicationService.getApplicationFromId(PWA_ID)).thenReturn(pwaApplication);

    var uploadedFile = new UploadedFile();
    when(fileService.find(FILE_ID)).thenReturn(Optional.of(uploadedFile));

    when(fileService.delete(uploadedFile)).thenReturn(FileDeleteResponse.success(FILE_ID));

    appFile.setPwaApplication(pwaApplication);

    consultationResponseFileLink = new ConsultationResponseFileLink(consultationResponse, appFile);

    when(consultationResponseService.getConsultationResponseFileLink(appFile)).thenReturn(Optional.of(consultationResponseFileLink));

    when(appFileService.getAppFileByPwaApplicationAndFileId(pwaApplication, String.valueOf(FILE_ID)))
        .thenReturn(appFile);

    mockMvc.perform(post(ReverseRouter.route(on(CONTROLLER)
            .delete(PWA_ID, FILE_ID, null), Map.of("consultationRequestId", "1")))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isOk());

    verify(appFileManagementService).throwIfFileDoesNotBelongToApplicationOrDocumentType(uploadedFile, pwaApplication, FileDocumentType.CONSULTATION_RESPONSE);
    verify(appFileService).processFileDeletion(appFile);
    verify(fileService).delete(uploadedFile);
  }

  @Test
  void delete_invalidFileId() throws Exception {
    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(any(), eq(user))).thenReturn(permissionsDto);

    var teamMember = new ConsulteeGroupTeamMember(consultationRequest.getConsulteeGroup(), user.getLinkedPerson(), Set.of(
        ConsulteeGroupMemberRole.RESPONDER));

    when(consulteeGroupTeamService.getTeamMemberByGroupAndPerson(consultationRequest.getConsulteeGroup(), user.getLinkedPerson()))
        .thenReturn(Optional.of(teamMember));

    when(pwaApplicationService.getApplicationFromId(PWA_ID)).thenReturn(pwaApplication);

    when(fileService.find(FILE_ID)).thenReturn(Optional.empty());
    when(appFileManagementService.getFileNotFoundException(pwaApplication, FILE_ID))
        .thenReturn(new ResponseStatusException(HttpStatus.NOT_FOUND));

    mockMvc.perform(post(ReverseRouter.route(on(CONTROLLER)
            .delete(PWA_ID, FILE_ID, null), Map.of("consultationRequestId", "1")))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isNotFound());

    verify(appFileService, never()).processFileDeletion(any());
    verify(fileService, never()).delete(any());
  }

  @Test
  void delete_fileNotLinkedToApplication() throws Exception {
    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(any(), eq(user))).thenReturn(permissionsDto);

    var teamMember = new ConsulteeGroupTeamMember(consultationRequest.getConsulteeGroup(), user.getLinkedPerson(), Set.of(
        ConsulteeGroupMemberRole.RESPONDER));

    when(consulteeGroupTeamService.getTeamMemberByGroupAndPerson(consultationRequest.getConsulteeGroup(), user.getLinkedPerson()))
        .thenReturn(Optional.of(teamMember));

    when(pwaApplicationService.getApplicationFromId(PWA_ID)).thenReturn(pwaApplication);

    var uploadedFile = new UploadedFile();
    when(fileService.find(FILE_ID)).thenReturn(Optional.of(uploadedFile));

    doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND))
        .when(appFileManagementService)
        .throwIfFileDoesNotBelongToApplicationOrDocumentType(uploadedFile, pwaApplication, FileDocumentType.CONSULTATION_RESPONSE);

    mockMvc.perform(post(ReverseRouter.route(on(CONTROLLER)
            .delete(PWA_ID, FILE_ID, null), Map.of("consultationRequestId", "1")))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isNotFound());

    verify(appFileManagementService).throwIfFileDoesNotBelongToApplicationOrDocumentType(uploadedFile, pwaApplication, FileDocumentType.CONSULTATION_RESPONSE);
    verify(appFileService, never()).processFileDeletion(any());
    verify(fileService, never()).delete(any());
  }

  @Test
  void delete_noPermissions() throws Exception {
    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(any(), eq(user))).thenReturn(noPermissionsDto);

    mockMvc.perform(post(ReverseRouter.route(on(CONTROLLER)
            .delete(PWA_ID, FILE_ID, null), Map.of("consultationRequestId", "1")))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isForbidden());

    verify(appFileService, never()).processFileDeletion(any());
    verify(fileService, never()).delete(any());
  }
}