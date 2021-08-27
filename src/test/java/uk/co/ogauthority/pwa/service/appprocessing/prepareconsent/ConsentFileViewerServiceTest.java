package uk.co.ogauthority.pwa.service.appprocessing.prepareconsent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.docgen.DocgenRun;
import uk.co.ogauthority.pwa.model.docgen.DocgenRunStatus;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequestTestUtil;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequestViewUtil;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
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

@RunWith(MockitoJUnitRunner.class)
public class ConsentFileViewerServiceTest {

  @Mock
  private ConsulteeGroupDetailService consulteeGroupDetailService;

  @Mock
  private ConsultationRequestService consultationRequestService;

  @Mock
  private ConsultationViewService consultationViewService;

  private ConsentFileViewerService consentFileViewerService;

  private final ConsulteeGroupDetail consulteeGroupDetail = ConsulteeGroupTestingUtils.createConsulteeGroup("CONSULTEE_GROUP", "CG");

  private final PwaApplication pwaApplication = new PwaApplication();

  private final ConsultationRequest consultationRequest = ConsultationRequestTestUtil.createWithStatus(pwaApplication,
      ConsultationRequestStatus.RESPONDED);

  private ConsultationRequestView consultationRequestView;

  private final PwaConsentApplicationDto pwaConsentApplicationDto = PwaViewTabTestUtil.createConsentApplicationDto(Instant.now(),
      getRunWithInfo());

  private ConsentFileView consentFileView;

  @Before
  public void setup() {
    consentFileViewerService = new ConsentFileViewerService(consulteeGroupDetailService, consultationRequestService, consultationViewService);

    consulteeGroupDetail.setConsultationResponseDocumentType(ConsultationResponseDocumentType.SECRETARY_OF_STATE_DECISION);

    consultationRequest.setConsulteeGroup(consulteeGroupDetail.getConsulteeGroup());

    consultationRequestView = ConsultationRequestViewUtil.createFromRequest(consultationRequest,
        ConsultationResponseDocumentType.SECRETARY_OF_STATE_DECISION);

    consentFileView = new ConsentFileView(pwaConsentApplicationDto, consultationRequestView);
  }

  @Test
  public void getLatestConsultationRequestViewForDocumentType_consentDocAndConsultationFiles() {
    when(consulteeGroupDetailService.getAllConsulteeGroupDetailsByGroup(Set.of(consultationRequest.getConsulteeGroup()))).thenReturn(List.of(consulteeGroupDetail));
    when(consultationRequestService.getAllRequestsByAppRespondedOnly(pwaApplication)).thenReturn(List.of(consultationRequest));
    when(consultationViewService.getConsultationRequestView(consultationRequest)).thenReturn(consultationRequestView);

    assertThat(consentFileViewerService.getConsentFileView(pwaApplication, pwaConsentApplicationDto,
        ConsultationResponseDocumentType.SECRETARY_OF_STATE_DECISION))
        .isEqualTo(consentFileView);
  }

  @Test
  public void getLatestConsultationRequestViewForDocumentType_noConsultationFiles() {
    assertThat(consentFileViewerService.getConsentFileView(pwaApplication, pwaConsentApplicationDto,
        ConsultationResponseDocumentType.SECRETARY_OF_STATE_DECISION))
        .isEqualTo(new ConsentFileView(pwaConsentApplicationDto, null));
  }

  private DocgenRun getRunWithInfo() {
    var run = new DocgenRun();
    run.setId(1000);
    run.setStatus(DocgenRunStatus.COMPLETE);
    return run;
  }

}
