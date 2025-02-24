package uk.co.ogauthority.pwa.features.consents.viewconsent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.model.docgen.DocgenRun;
import uk.co.ogauthority.pwa.model.docgen.DocgenRunStatus;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequestTestUtil;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequestViewUtil;
import uk.co.ogauthority.pwa.model.enums.consultations.ConsultationResponseDocumentType;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationRequestView;
import uk.co.ogauthority.pwa.model.view.consent.ConsentFileView;
import uk.co.ogauthority.pwa.repository.pwaconsents.PwaConsentApplicationDto;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupDetailService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationViewService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.search.consents.pwaviewtab.testutil.PwaViewTabTestUtil;
import uk.co.ogauthority.pwa.testutils.ConsulteeGroupTestingUtils;

@ExtendWith(MockitoExtension.class)
class ConsentFileViewerServiceTest {

  @Mock
  private ConsulteeGroupDetailService consulteeGroupDetailService;

  @Mock
  private ConsultationRequestService consultationRequestService;

  @Mock
  private ConsultationViewService consultationViewService;

  private ConsentFileViewerService consentFileViewerService;

  private final ConsulteeGroupDetail consulteeGroupDetail = ConsulteeGroupTestingUtils.createConsulteeGroup("CONSULTEE_GROUP", "CG");

  private final PwaApplication pwaApplication = new PwaApplication();

  private ConsultationRequest consultationRequest;

  private ConsultationRequestView consultationRequestView;

  private final PwaConsentApplicationDto pwaConsentApplicationDto = PwaViewTabTestUtil.createConsentApplicationDto(Instant.now(),
      getRunWithInfo());

  private ConsentFileView consentFileView;

  @BeforeEach
  void setup() {
    consentFileViewerService = new ConsentFileViewerService(consulteeGroupDetailService, consultationRequestService, consultationViewService);

    consulteeGroupDetail.setConsultationResponseDocumentType(ConsultationResponseDocumentType.SECRETARY_OF_STATE_DECISION);

    consultationRequest = ConsultationRequestTestUtil.createWithStatus(pwaApplication,
        ConsultationRequestStatus.RESPONDED);
    consultationRequest.setConsulteeGroup(consulteeGroupDetail.getConsulteeGroup());
    consultationRequest.setStartTimestamp(Instant.now());

    consultationRequestView = ConsultationRequestViewUtil.createFromRequest(consultationRequest,
        ConsultationResponseDocumentType.SECRETARY_OF_STATE_DECISION);

    consentFileView = new ConsentFileView(pwaConsentApplicationDto, consultationRequestView);
  }

  @Test
  void getLatestConsultationRequestViewForDocumentType_consentDocAndConsultationFiles() {
    when(consulteeGroupDetailService.getAllConsulteeGroupDetailsByGroup(Set.of(consultationRequest.getConsulteeGroup()))).thenReturn(List.of(consulteeGroupDetail));
    when(consultationRequestService.getAllRequestsByAppRespondedOnly(pwaApplication)).thenReturn(List.of(consultationRequest));
    when(consultationViewService.getConsultationRequestView(consultationRequest)).thenReturn(consultationRequestView);

    assertThat(consentFileViewerService.getConsentFileView(pwaApplication, pwaConsentApplicationDto,
        ConsultationResponseDocumentType.SECRETARY_OF_STATE_DECISION))
        .isEqualTo(consentFileView);
  }

  @Test
  void getLatestConsultationRequestViewForDocumentType_noConsultationFiles() {
    assertThat(consentFileViewerService.getConsentFileView(pwaApplication, pwaConsentApplicationDto,
        ConsultationResponseDocumentType.SECRETARY_OF_STATE_DECISION))
        .isEqualTo(new ConsentFileView(pwaConsentApplicationDto, null));
  }

  @Test
  void getLatestConsultationRequestForResponseDocType_multipleRespondedRequests() {

    var latestRequest = new ConsultationRequest();
    latestRequest.setConsulteeGroup(consulteeGroupDetail.getConsulteeGroup());
    latestRequest.setStartTimestamp(consultationRequest.getStartTimestamp().plusSeconds(5));

    when(consultationRequestService.getAllRequestsByAppRespondedOnly(pwaApplication))
        .thenReturn(List.of(consultationRequest, latestRequest));

    when(consulteeGroupDetailService.getAllConsulteeGroupDetailsByGroup(any()))
        .thenReturn(List.of(consulteeGroupDetail));

    var result = consentFileViewerService
        .getLatestConsultationRequestForResponseDocType(pwaApplication, ConsultationResponseDocumentType.SECRETARY_OF_STATE_DECISION);

    assertThat(result).contains(latestRequest);

  }

  private DocgenRun getRunWithInfo() {
    var run = new DocgenRun();
    run.setId(1000);
    run.setStatus(DocgenRunStatus.COMPLETE);
    return run;
  }

}
