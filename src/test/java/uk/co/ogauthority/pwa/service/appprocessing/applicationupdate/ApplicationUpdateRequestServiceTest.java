package uk.co.ogauthority.pwa.service.appprocessing.applicationupdate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.model.entity.appprocessing.applicationupdates.ApplicationUpdateRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.repository.appprocessing.applicationupdates.ApplicationUpdateRequestRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;


@RunWith(MockitoJUnitRunner.class)
public class ApplicationUpdateRequestServiceTest {

  private static final String REASON = "REASON";
  private static final int PERSON_ID = 1;

  @Mock
  private ApplicationUpdateRequestRepository applicationUpdateRequestRepository;

  private Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

  private ApplicationUpdateRequestService applicationUpdateRequestService;

  @Captor
  private ArgumentCaptor<ApplicationUpdateRequest> appUpdateArgCapture;

  private Person person;

  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setUp() throws Exception {
    person = new Person(PERSON_ID, "test", "person", "email", "telephone");
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    applicationUpdateRequestService = new ApplicationUpdateRequestService(applicationUpdateRequestRepository, clock);
  }

  @Test
  public void createApplicationUpdateRequest_savedRequestHasExpectedAttributes() {

    applicationUpdateRequestService.createApplicationUpdateRequest(pwaApplicationDetail, person, REASON);

    verify(applicationUpdateRequestRepository, times(1)).save(appUpdateArgCapture.capture());

    var updateRequest = appUpdateArgCapture.getValue();

    assertThat(updateRequest.getRequestReason()).isEqualTo(REASON);
    assertThat(updateRequest.getRequestedByPersonId()).isEqualTo(new PersonId(PERSON_ID));
    assertThat(updateRequest.getRequestedTimestamp()).isEqualTo(clock.instant());
    assertThat(updateRequest.getPwaApplicationDetail()).isEqualTo(pwaApplicationDetail);
  }

  @Test
  public void applicationDetailHasOpenUpdateRequest_serviceInteraction() {
    when(applicationUpdateRequestRepository.existsByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(true);
    assertThat(applicationUpdateRequestService.applicationDetailHasOpenUpdateRequest(pwaApplicationDetail)).isTrue();
  }
}