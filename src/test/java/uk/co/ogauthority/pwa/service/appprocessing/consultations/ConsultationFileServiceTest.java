package uk.co.ogauthority.pwa.service.appprocessing.consultations;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.controller.consultations.responses.ConsultationResponseFileController;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequestTestUtil;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponse;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponseFileLink;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.AppFile;
import uk.co.ogauthority.pwa.model.entity.files.AppFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileViewTestUtil;
import uk.co.ogauthority.pwa.repository.consultations.ConsultationResponseFileLinkRepository;
import uk.co.ogauthority.pwa.service.consultations.ConsultationFileService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.fileupload.AppFileService;
import uk.co.ogauthority.pwa.util.RouteUtils;

@RunWith(MockitoJUnitRunner.class)
public class ConsultationFileServiceTest {

  private ConsultationFileService consultationFileService;

  @Mock
  private AppFileService appFileService;

  private PwaApplication pwaApplication;

  private ConsultationResponse consultationResponse;

  private ConsultationRequest consultationRequest;

  private UploadedFileView uploadedFileView;

  private ConsultationResponseFileLink consultationResponseFileLink;

  private AppFile appFile;

  private static final String FILE_ID = "file_id";

  @Mock
  private ConsultationResponseFileLinkRepository consultationResponseFileLinkRepository;

  @Before
  public void setup() {
    consultationFileService = new ConsultationFileService(appFileService, consultationResponseFileLinkRepository);

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
  public void getConsultationResponseIdToFileViewsMap_isPopulatedCorrectly() {
    when(appFileService.getUploadedFileViewsWithNoUrl(any(), any(), any())).thenReturn(List.of(uploadedFileView));

    when(consultationResponseFileLinkRepository.findALlByConsultationResponseIn(Set.of(consultationResponse))).thenReturn(Set.of(consultationResponseFileLink));

    var responseIdToFileViewsMap = consultationFileService.getConsultationResponseIdToFileViewsMap(pwaApplication, Set.of(consultationResponse));
    assertThat(responseIdToFileViewsMap).isEqualTo(Map.of(consultationResponse.getId(), List.of(uploadedFileView)));

  }

  @Test
  public void getConsultationFileViewUrl_getsCorrectUrl() {
    assertThat(consultationFileService.getConsultationFileViewUrl(consultationRequest)).isEqualTo(RouteUtils.routeWithUriVariables(on(
        ConsultationResponseFileController.class)
            .handleDownload(pwaApplication.getApplicationType(), pwaApplication.getId(), null, null),
        Map.of("consultationRequestId", consultationRequest.getId())));
  }

}
