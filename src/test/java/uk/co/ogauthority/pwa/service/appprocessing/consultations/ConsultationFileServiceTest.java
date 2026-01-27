package uk.co.ogauthority.pwa.service.appprocessing.consultations;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.ogauthority.pwa.controller.consultations.responses.ConsultationResponseFileController;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.involvement.ApplicationInvolvementDtoTestUtil;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContextTestUtil;
import uk.co.ogauthority.pwa.features.filemanagement.AppFileManagementService;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequestTestUtil;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponse;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponseFileLink;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.AppFile;
import uk.co.ogauthority.pwa.model.entity.files.AppFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.repository.consultations.ConsultationResponseFileLinkRepository;
import uk.co.ogauthority.pwa.service.consultations.ConsultationFileService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentService;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.RouteUtils;

@ExtendWith(MockitoExtension.class)
class ConsultationFileServiceTest {

  private ConsultationFileService consultationFileService;

  @Mock
  private PwaConsentService pwaConsentService;

  @Mock
  private AppFileManagementService appFileManagementService;

  private PwaApplication pwaApplication;

  private ConsultationResponse consultationResponse;

  private ConsultationRequest consultationRequest;

  private UploadedFile uploadedFile;

  private UploadedFileView uploadedFileView;

  private ConsultationResponseFileLink consultationResponseFileLink;

  private AppFile appFile;

  private static final UUID FILE_ID = UUID.randomUUID();
  private static final FileDocumentType DOCUMENT_TYPE = FileDocumentType.CONSULTATION_RESPONSE;
  private static final String USAGE_TYPE = PwaApplication.class.getSimpleName();

  @Mock
  private ConsultationResponseFileLinkRepository consultationResponseFileLinkRepository;

  @BeforeEach
  void setup() {
    consultationFileService = new ConsultationFileService(
        consultationResponseFileLinkRepository,
        pwaConsentService,
        appFileManagementService
    );

    pwaApplication = new PwaApplication();
    pwaApplication.setId(10);

    consultationRequest = ConsultationRequestTestUtil.createWithStatus(pwaApplication, ConsultationRequestStatus.RESPONDED);

    consultationResponse = new ConsultationResponse();
    consultationRequest.setId(20);

    consultationResponse.setId(30);
    consultationResponse.setConsultationRequest(consultationRequest);

    uploadedFile = createUploadedFile();
    uploadedFileView = createUploadedFileViewForFile(uploadedFile);

    appFile = new AppFile(pwaApplication, String.valueOf(FILE_ID), AppFilePurpose.CONSULTATION_RESPONSE, ApplicationFileLinkStatus.FULL);

    consultationResponseFileLink = new ConsultationResponseFileLink(consultationResponse, appFile);
  }

  @Test
  void getConsultationResponseIdToFileViewsMap_isPopulatedCorrectly() {
    when(appFileManagementService.getUploadedFiles(any(), eq(FileDocumentType.CONSULTATION_RESPONSE))).thenReturn(List.of(uploadedFile));

    when(consultationResponseFileLinkRepository.findAllByConsultationResponseIn(Set.of(consultationResponse))).thenReturn(Set.of(consultationResponseFileLink));

    uploadedFileView.setFileUrl(String.valueOf(uploadedFile.getId()));

    var responseIdToFileViewsMap = consultationFileService.getConsultationResponseIdToFileViewsMap(pwaApplication, Set.of(consultationResponse));

    assertThat(responseIdToFileViewsMap.get(consultationResponse.getId()))
        .usingRecursiveComparison()
        .isEqualTo(List.of(uploadedFileView));
  }

  @Test
  void getConsultationFileViewUrl_getsCorrectUrl() {
    assertThat(consultationFileService.getConsultationFileViewUrl(consultationRequest)).isEqualTo(
        RouteUtils.routeWithUriVariables(on(ConsultationResponseFileController.class).download(pwaApplication.getId(), null, null),
        Map.of("consultationRequestId", consultationRequest.getId())));
  }

  @Test
  void industryUserCanAccessFile_industryInvolvement_canAccess() {
    var processingContext = PwaAppProcessingContextTestUtil.withAppInvolvement(
        PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL),
        ApplicationInvolvementDtoTestUtil.generatePwaHolderTeamInvolvement(pwaApplication,
            Set.of(Role.APPLICATION_SUBMITTER)));
    when(pwaConsentService.getConsentByPwaApplication(processingContext.getPwaApplication()))
        .thenReturn(Optional.of(new PwaConsent()));
    assertTrue(consultationFileService.industryUserCanAccessFile(processingContext));
  }

  @Test
  void industryUserCanAccessFile_noIndustryInvolvement_cannotAccess() {
    var processingContext = PwaAppProcessingContextTestUtil.withAppInvolvement(
        PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL),
        ApplicationInvolvementDtoTestUtil.noInvolvementAndNoFlags(pwaApplication));
    assertFalse(consultationFileService.industryUserCanAccessFile(processingContext));
  }

  @Test
  void getUploadedFileViews() {
    when(appFileManagementService.getUploadedFiles(pwaApplication, DOCUMENT_TYPE)).thenReturn(List.of(uploadedFile));

    assertThat(consultationFileService.getUploadedFileViews(pwaApplication, DOCUMENT_TYPE)).isEqualTo(List.of(uploadedFileView));
  }

  private UploadedFile createUploadedFile() {
    var uploadedFile = new UploadedFile();
    uploadedFile.setId(FILE_ID);
    uploadedFile.setName("name");
    uploadedFile.setContentLength(50000L);
    uploadedFile.setDescription("description");
    uploadedFile.setUploadedAt(Instant.now());
    uploadedFile.setUsageId(pwaApplication.getId().toString());
    uploadedFile.setUsageType(USAGE_TYPE);

    return uploadedFile;
  }

  private UploadedFileView createUploadedFileViewForFile(UploadedFile uploadedFile) {
    return new UploadedFileView(
        String.valueOf(uploadedFile.getId()),
        uploadedFile.getName(),
        uploadedFile.getContentLength(),
        uploadedFile.getDescription(),
        uploadedFile.getUploadedAt(),
        ""
    );
  }

}
