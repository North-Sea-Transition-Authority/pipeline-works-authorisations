package uk.co.ogauthority.pwa.service.appprocessing.consultations;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import uk.co.ogauthority.pwa.model.form.files.UploadedFileViewTestUtil;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.repository.consultations.ConsultationResponseFileLinkRepository;
import uk.co.ogauthority.pwa.service.consultations.ConsultationFileService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.fileupload.AppFileService;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.RouteUtils;

@ExtendWith(MockitoExtension.class)
class ConsultationFileServiceTest {

  private ConsultationFileService consultationFileService;

  @Mock
  private AppFileService appFileService;

  @Mock
  private PwaConsentService pwaConsentService;

  @Mock
  private AppFileManagementService appFileManagementService;

  private PwaApplication pwaApplication;

  private ConsultationResponse consultationResponse;

  private ConsultationRequest consultationRequest;

  private UploadedFileView uploadedFileView;

  private ConsultationResponseFileLink consultationResponseFileLink;

  private AppFile appFile;

  private static final String FILE_ID = "file_id";

  @Mock
  private ConsultationResponseFileLinkRepository consultationResponseFileLinkRepository;

  @BeforeEach
  void setup() {
    consultationFileService = new ConsultationFileService(appFileService, consultationResponseFileLinkRepository,
        pwaConsentService, appFileManagementService);

     pwaApplication = new PwaApplication();
     pwaApplication.setId(10);

    consultationRequest = ConsultationRequestTestUtil.createWithStatus(pwaApplication, ConsultationRequestStatus.RESPONDED);

    consultationResponse = new ConsultationResponse();
     consultationRequest.setId(20);

    consultationResponse.setId(30);
    consultationResponse.setConsultationRequest(consultationRequest);

    uploadedFileView = UploadedFileViewTestUtil.createDefaultFileView();
    uploadedFileView.setFileId(FILE_ID);

    appFile = new AppFile(pwaApplication, FILE_ID, AppFilePurpose.CONSULTATION_RESPONSE, ApplicationFileLinkStatus.FULL);

    consultationResponseFileLink = new ConsultationResponseFileLink(consultationResponse, appFile);
  }

  @Test
  void getConsultationResponseIdToFileViewsMap_isPopulatedCorrectly() {
    when(appFileManagementService.getUploadedFileViews(any(), eq(FileDocumentType.CONSULTATION_RESPONSE))).thenReturn(List.of(uploadedFileView));

    when(consultationResponseFileLinkRepository.findAllByConsultationResponseIn(Set.of(consultationResponse))).thenReturn(Set.of(consultationResponseFileLink));

    var responseIdToFileViewsMap = consultationFileService.getConsultationResponseIdToFileViewsMap(pwaApplication, Set.of(consultationResponse));
    assertThat(responseIdToFileViewsMap).isEqualTo(Map.of(consultationResponse.getId(), List.of(uploadedFileView)));
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
            Set.of(PwaOrganisationRole.APPLICATION_SUBMITTER)));
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

}
