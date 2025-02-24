package uk.co.ogauthority.pwa.service.consultations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequestTestUtil;
import uk.co.ogauthority.pwa.repository.consultations.ConsultationRequestRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class ConsultationsStatusViewFactoryTest {

  @Mock
  private ConsultationRequestRepository consultationRequestRepository;

  private ConsultationsStatusViewFactory consultationsStatusViewFactory;

  private PwaApplication pwaApplication;

  @BeforeEach
  void setUp() throws Exception {
    var pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplication = pwaApplicationDetail.getPwaApplication();

    consultationsStatusViewFactory = new ConsultationsStatusViewFactory(consultationRequestRepository);
    when(consultationRequestRepository.findByPwaApplicationOrderByConsulteeGroupDescStartTimestampDesc(pwaApplication))
        .thenReturn(Collections.emptyList());
  }

  @Test
  void getApplicationStatusView_whenNoConsultations() {

    var applicationStatusView = consultationsStatusViewFactory.getApplicationStatusView(pwaApplication);

    for (ConsultationRequestStatus status : ConsultationRequestStatus.values()) {
      try {
        assertThat(applicationStatusView.getCountOfRequestsWithStatus(status)).isEqualTo(0L);
      } catch (AssertionError e) {
        throw new AssertionError("Failed for status:" + status, e);
      }
    }
  }

  @Test
  void getApplicationStatusView_whenConsultationsFoundForEveryPossibleStatus() {

    var consultationList = new ArrayList<ConsultationRequest>();

    Arrays.stream(ConsultationRequestStatus.values())
        .map(consultationRequestStatus -> ConsultationRequestTestUtil.createWithStatus(
            pwaApplication,
            consultationRequestStatus
        ))
        .forEach(consultationList::add);


    when(consultationRequestRepository.findByPwaApplicationOrderByConsulteeGroupDescStartTimestampDesc(pwaApplication))
        .thenReturn(consultationList);

    var applicationStatusView = consultationsStatusViewFactory.getApplicationStatusView(pwaApplication);

    for (ConsultationRequestStatus status : ConsultationRequestStatus.values()) {
      try {
        assertThat(applicationStatusView.getCountOfRequestsWithStatus(status)).isEqualTo(1L);
      } catch (AssertionError e) {
        throw new AssertionError("Failed for status:" + status, e);
      }
    }
  }
}