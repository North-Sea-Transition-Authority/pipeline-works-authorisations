package uk.co.ogauthority.pwa.controller.consultations.responses;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.sql.SQLException;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.sql.rowset.serial.SerialBlob;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaAppProcessingContextAbstractControllerTest;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ProcessingPermissionsDto;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupTeamMember;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponse;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponseFileLink;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.AppFile;
import uk.co.ogauthority.pwa.model.entity.files.AppFilePurpose;
import uk.co.ogauthority.pwa.model.entity.files.FileUploadStatus;
import uk.co.ogauthority.pwa.model.entity.files.UploadedFile;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.appprocessing.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationFileService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationResponseService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.testutils.PwaAppProcessingContextDtoTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.RouteUtils;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = ConsultationResponseFileController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {PwaAppProcessingContextService.class}))
public class ConsultationResponseFileControllerTest extends PwaAppProcessingContextAbstractControllerTest {

  @MockBean
  private ConsultationResponseService consultationResponseService;

  @MockBean
  private PwaAppProcessingPermissionService pwaAppProcessingPermissionService;

  @MockBean
  private ConsultationFileService consultationFileService;

  private AuthenticatedUserAccount user;

  private PwaApplicationDetail pwaApplicationDetail;

  private ConsultationRequest consultationRequest;

  private final ConsultationResponse consultationResponse = new ConsultationResponse();

  private UploadedFile uploadedFile;
  private AppFile appFile;

  private ProcessingPermissionsDto permissionsDto;
  private ProcessingPermissionsDto noPermissionsDto;

  private ConsultationResponseFileLink consultationResponseFileLink;

  private final MockMultipartFile file
      = new MockMultipartFile(
      "file",
      "file.txt",
      MediaType.TEXT_PLAIN_VALUE,
      "Test file".getBytes()
  );

  private static final String FILE_ID = "FILE_ID";

  @Before
  public void setup() throws SQLException {
    user = new AuthenticatedUserAccount(
        new WebUserAccount(1),
        EnumSet.of(PwaUserPrivilege.PWA_CONSULTEE));

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(1);
    when(pwaApplicationDetailService.getLatestDetailForUser(pwaApplicationDetail.getMasterPwaApplicationId(), user))
        .thenReturn(Optional.of(pwaApplicationDetail));

    consultationRequest = new ConsultationRequest();
    consultationRequest.setId(1);
    when(consultationRequestService.getConsultationRequestByIdOrThrow(any())).thenReturn(consultationRequest);
    when(consultationResponseService.isUserAssignedResponderForConsultation(any(), any())).thenReturn(true);

    consultationResponse.setConsultationRequest(consultationRequest);
    consultationResponseFileLink = new ConsultationResponseFileLink(consultationResponse, appFile);

    appFile =  new AppFile(pwaApplicationDetail.getPwaApplication(), FILE_ID, AppFilePurpose.CONSULTATION_RESPONSE,
        ApplicationFileLinkStatus.TEMPORARY);
    appFile.setId(90);
    when(appFileService.getAppFileByPwaApplicationAndFileId(pwaApplicationDetail.getPwaApplication(), FILE_ID)).thenReturn(appFile);

    uploadedFile = new UploadedFile(FILE_ID, "File name", "image/jpg", 100L, Instant.now(),
        FileUploadStatus.CURRENT);
    uploadedFile.setFileData(new SerialBlob(new byte[1]));
    uploadedFile.setUploadedByWuaId(user.getWuaId());
    when(appFileService.getUploadedFileById(FILE_ID)).thenReturn(uploadedFile);

    permissionsDto = new ProcessingPermissionsDto(
        PwaAppProcessingContextDtoTestUtils.appInvolvementWithConsultationRequest("nme", consultationRequest),
        EnumSet.allOf(PwaAppProcessingPermission.class));
    noPermissionsDto = new ProcessingPermissionsDto(
        PwaAppProcessingContextDtoTestUtils.appInvolvementWithConsultationRequest("nme", consultationRequest),
        Set.of());

    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user)).thenReturn(permissionsDto);

    when(consultationFileService.industryUserCanAccessFile(any())).thenReturn(false);
  }

  @Test
  public void handleUpload_success() throws Exception {
    mockMvc.perform(multipart(
        RouteUtils.routeWithUriVariables(on(ConsultationResponseFileController.class)
            .handleUpload(pwaApplicationDetail.getPwaApplicationType(),
            pwaApplicationDetail.getPwaApplication().getId(), null, null), Map.of("consultationRequestId", "1"))).file(file)
        .with(csrf())
        .with(authenticatedUserAndSession(user)))
        .andExpect(status().isOk());
  }

  @Test
  public void handleUpload_noActiveConsultation_forbidden() throws Exception {
    var noPermissionsDto = new ProcessingPermissionsDto(
        PwaAppProcessingContextDtoTestUtils.emptyAppInvolvement(pwaApplicationDetail.getPwaApplication()),
        EnumSet.allOf(PwaAppProcessingPermission.class));

    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user)).thenReturn(noPermissionsDto);

    mockMvc.perform(multipart(
        RouteUtils.routeWithUriVariables(on(ConsultationResponseFileController.class)
            .handleUpload(pwaApplicationDetail.getPwaApplicationType(), pwaApplicationDetail.getPwaApplication().getId(),
            null, null), Map.of("consultationRequestId", "1"))).file(file)
        .with(csrf())
        .with(authenticatedUserAndSession(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  public void handleDownload_fileLinkTemporary_success() throws Exception {
    var teamMember = new ConsulteeGroupTeamMember(consultationRequest.getConsulteeGroup(), user.getLinkedPerson(), Set.of(
        ConsulteeGroupMemberRole.RESPONDER));

    when(consulteeGroupTeamService.getTeamMemberByGroupAndPerson(consultationRequest.getConsulteeGroup(), user.getLinkedPerson()))
        .thenReturn(Optional.of(teamMember));

    mockMvc.perform(get(
        RouteUtils.routeWithUriVariables(on(ConsultationResponseFileController.class)
            .handleDownload(pwaApplicationDetail.getPwaApplicationType(), pwaApplicationDetail.getPwaApplication().getId(),
                FILE_ID, null), Map.of("consultationRequestId", "1")))
        .with(authenticatedUserAndSession(user)))
        .andExpect(status().isOk());
  }

  @Test
  public void handleDownload_fileLinkTemporary_userNotOriginalUploader_forbidden() throws Exception {
    uploadedFile.setUploadedByWuaId(999);

    when(consulteeGroupTeamService.getTeamMemberByGroupAndPerson(consultationRequest.getConsulteeGroup(), user.getLinkedPerson()))
        .thenReturn(Optional.empty());

    mockMvc.perform(get(
        RouteUtils.routeWithUriVariables(on(ConsultationResponseFileController.class)
            .handleDownload(pwaApplicationDetail.getPwaApplicationType(), pwaApplicationDetail.getPwaApplication().getId(),
                FILE_ID, null), Map.of("consultationRequestId", "1")))
        .with(authenticatedUserAndSession(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  public void handleDownload_fileLinkNotTemporary_userIsInConsulteeTeam_success() throws Exception {
    var teamMember = new ConsulteeGroupTeamMember(consultationRequest.getConsulteeGroup(), user.getLinkedPerson(), Set.of(
        ConsulteeGroupMemberRole.RESPONDER));

    appFile.setFileLinkStatus(ApplicationFileLinkStatus.FULL);

    when(consulteeGroupTeamService.getTeamMemberByGroupAndPerson(consultationRequest.getConsulteeGroup(), user.getLinkedPerson()))
        .thenReturn(Optional.of(teamMember));
    when(consultationResponseService.getConsultationResponseFileLink(appFile)).thenReturn(Optional.of(consultationResponseFileLink));

    mockMvc.perform(get(
        RouteUtils.routeWithUriVariables(on(ConsultationResponseFileController.class)
            .handleDownload(pwaApplicationDetail.getPwaApplicationType(), pwaApplicationDetail.getPwaApplication().getId(),
                FILE_ID, null), Map.of("consultationRequestId", "1")))
        .with(authenticatedUserAndSession(user)))
        .andExpect(status().isOk());
  }

  @Test
  public void handleDownload_fileLinkNotTemporary_userCanViewAllConsultations_success() throws Exception {
    appFile.setFileLinkStatus(ApplicationFileLinkStatus.FULL);
    permissionsDto = new ProcessingPermissionsDto(
        PwaAppProcessingContextDtoTestUtils.appInvolvementWithConsultationRequest("nme", consultationRequest),
        Set.of(PwaAppProcessingPermission.VIEW_ALL_CONSULTATIONS));

    when(consultationResponseService.getConsultationResponseFileLink(appFile)).thenReturn(Optional.of(consultationResponseFileLink));
    when(consulteeGroupTeamService.getTeamMemberByGroupAndPerson(consultationRequest.getConsulteeGroup(), user.getLinkedPerson()))
        .thenReturn(Optional.empty());

    mockMvc.perform(get(
        RouteUtils.routeWithUriVariables(on(ConsultationResponseFileController.class)
            .handleDownload(pwaApplicationDetail.getPwaApplicationType(), pwaApplicationDetail.getPwaApplication().getId(),
                FILE_ID, null), Map.of("consultationRequestId", "1")))
        .with(authenticatedUserAndSession(user)))
        .andExpect(status().isOk());
  }

  @Test
  public void handleDownload_fileLinkNotTemporary_appIsConsented_userInHolderTeam_success() throws Exception {
    appFile.setFileLinkStatus(ApplicationFileLinkStatus.FULL);

    when(consultationResponseService.getConsultationResponseFileLink(appFile)).thenReturn(Optional.of(consultationResponseFileLink));
    when(consulteeGroupTeamService.getTeamMemberByGroupAndPerson(consultationRequest.getConsulteeGroup(), user.getLinkedPerson()))
        .thenReturn(Optional.empty());
    when(consultationFileService.industryUserCanAccessFile(any())).thenReturn(true);

    mockMvc.perform(get(
        RouteUtils.routeWithUriVariables(on(ConsultationResponseFileController.class)
            .handleDownload(pwaApplicationDetail.getPwaApplicationType(), pwaApplicationDetail.getPwaApplication().getId(),
                FILE_ID, null), Map.of("consultationRequestId", "1")))
        .with(authenticatedUserAndSession(user)))
        .andExpect(status().isOk());
  }

  @Test
  public void handleDownload_fileLinkNotTemporary_noPermissionsUser_forbidden() throws Exception {
    appFile.setFileLinkStatus(ApplicationFileLinkStatus.FULL);

    when(consultationResponseService.getConsultationResponseFileLink(appFile)).thenReturn(Optional.of(consultationResponseFileLink));
    when(consulteeGroupTeamService.getTeamMemberByGroupAndPerson(consultationRequest.getConsulteeGroup(), user.getLinkedPerson()))
        .thenReturn(Optional.empty());
    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user)).thenReturn(noPermissionsDto);

    mockMvc.perform(get(
        RouteUtils.routeWithUriVariables(on(ConsultationResponseFileController.class)
            .handleDownload(pwaApplicationDetail.getPwaApplicationType(), pwaApplicationDetail.getPwaApplication().getId(),
                FILE_ID, null), Map.of("consultationRequestId", "1")))
        .with(authenticatedUserAndSession(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  public void handleDelete_fileLinkTemporary_success() throws Exception {
    var teamMember = new ConsulteeGroupTeamMember(consultationRequest.getConsulteeGroup(), user.getLinkedPerson(), Set.of(
        ConsulteeGroupMemberRole.RESPONDER));

    when(consulteeGroupTeamService.getTeamMemberByGroupAndPerson(consultationRequest.getConsulteeGroup(), user.getLinkedPerson()))
        .thenReturn(Optional.of(teamMember));

    mockMvc.perform(post(
        RouteUtils.routeWithUriVariables(on(ConsultationResponseFileController.class)
            .handleDelete(pwaApplicationDetail.getPwaApplicationType(), pwaApplicationDetail.getPwaApplication().getId(),
                FILE_ID, null), Map.of("consultationRequestId", "1")))
        .with(csrf())
        .with(authenticatedUserAndSession(user)))
        .andExpect(status().isOk());

    verify(appFileService).processFileDeletionWithPreDeleteAction(eq(appFile), eq(user), any());
  }

  @Test
  public void handleDelete_fileLinkTemporary_userNotOriginalUploader_forbidden() throws Exception {
    uploadedFile.setUploadedByWuaId(999);

    when(consulteeGroupTeamService.getTeamMemberByGroupAndPerson(consultationRequest.getConsulteeGroup(), user.getLinkedPerson()))
        .thenReturn(Optional.empty());

    mockMvc.perform(post(
        RouteUtils.routeWithUriVariables(on(ConsultationResponseFileController.class)
            .handleDelete(pwaApplicationDetail.getPwaApplicationType(), pwaApplicationDetail.getPwaApplication().getId(),
                FILE_ID, null), Map.of("consultationRequestId", "1")))
        .with(csrf())
        .with(authenticatedUserAndSession(user)))
        .andExpect(status().isForbidden());

    verify(appFileService, never()).processFileDeletionWithPreDeleteAction(eq(appFile), eq(user), any());
  }

  @Test
  public void handleDelete_fileLinkNotTemporary_userIsInConsulteeTeam_success() throws Exception {
    var teamMember = new ConsulteeGroupTeamMember(consultationRequest.getConsulteeGroup(), user.getLinkedPerson(), Set.of(
        ConsulteeGroupMemberRole.RESPONDER));

    appFile.setFileLinkStatus(ApplicationFileLinkStatus.FULL);

    when(consulteeGroupTeamService.getTeamMemberByGroupAndPerson(consultationRequest.getConsulteeGroup(), user.getLinkedPerson()))
        .thenReturn(Optional.of(teamMember));
    when(consultationResponseService.getConsultationResponseFileLink(appFile)).thenReturn(Optional.of(consultationResponseFileLink));

    mockMvc.perform(post(
        RouteUtils.routeWithUriVariables(on(ConsultationResponseFileController.class)
            .handleDelete(pwaApplicationDetail.getPwaApplicationType(), pwaApplicationDetail.getPwaApplication().getId(),
                FILE_ID, null), Map.of("consultationRequestId", "1")))
        .with(csrf())
        .with(authenticatedUserAndSession(user)))
        .andExpect(status().isOk());

    verify(appFileService).processFileDeletionWithPreDeleteAction(eq(appFile), eq(user), any());
  }

  @Test
  public void handleDelete_fileLinkNotTemporary_userNotInConsulteeTeam_forbidden() throws Exception {
    appFile.setFileLinkStatus(ApplicationFileLinkStatus.FULL);

    when(consulteeGroupTeamService.getTeamMemberByGroupAndPerson(consultationRequest.getConsulteeGroup(), user.getLinkedPerson()))
        .thenReturn(Optional.empty());
    when(consultationResponseService.getConsultationResponseFileLink(appFile)).thenReturn(Optional.of(consultationResponseFileLink));

    mockMvc.perform(post(
        RouteUtils.routeWithUriVariables(on(ConsultationResponseFileController.class)
            .handleDelete(pwaApplicationDetail.getPwaApplicationType(), pwaApplicationDetail.getPwaApplication().getId(),
                FILE_ID, null), Map.of("consultationRequestId", "1")))
        .with(csrf())
        .with(authenticatedUserAndSession(user)))
        .andExpect(status().isForbidden());

    verify(appFileService, never()).processFileDeletionWithPreDeleteAction(eq(appFile), eq(user), any());
  }

}
