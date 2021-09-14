package uk.co.ogauthority.pwa.service.pwaapplications.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.repository.pwaapplications.events.PwaApplicationEventRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PwaApplicationEventServiceTest {

  @Mock
  private PwaApplicationEventRepository pwaApplicationEventRepository;

  @Mock
  private Clock clock;

  @Captor
  private ArgumentCaptor<PwaApplicationEvent> pwaApplicationEventArgumentCaptor;

  @Captor
  private ArgumentCaptor<List<PwaApplicationEvent>> pwaApplicationEventListArgumentCaptor;

  private PwaApplicationEventService pwaApplicationEventService;

  private final Instant fixedInstant = Instant.now();

  private PwaApplicationDetail pwaApplicationDetail;
  private final WebUserAccount user = new WebUserAccount(1);

  @Before
  public void setUp() throws Exception {

    when(clock.instant()).thenReturn(fixedInstant);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    pwaApplicationEventService = new PwaApplicationEventService(pwaApplicationEventRepository, clock);

  }

  @Test
  public void handleConsentIssueFailure() {

    var ex = mock(Exception.class);

    var event = new ConsentIssueFailedEvent(pwaApplicationDetail, ex, user);

    pwaApplicationEventService.handleConsentIssueFailure(event);

    verify(pwaApplicationEventRepository, times(1)).save(pwaApplicationEventArgumentCaptor.capture());

    assertThat(pwaApplicationEventArgumentCaptor.getValue()).satisfies(appEvent -> {

      assertThat(appEvent.getPwaApplication()).isEqualTo(pwaApplicationDetail.getPwaApplication());
      assertThat(appEvent.getEventType()).isEqualTo(PwaApplicationEventType.CONSENT_ISSUE_FAILED);
      assertThat(appEvent.getEventInstant()).isEqualTo(clock.instant());
      assertThat(appEvent.getEventClearedInstant()).isNull();
      assertThat(appEvent.getEventWuaId()).isEqualTo(user.getWuaId());
      assertThat(appEvent.getMessage()).isEqualTo(ExceptionUtils.getStackTrace(ex));

    });

  }

  @Test
  public void getUnclearedEventsByApplicationAndType() {

    pwaApplicationEventService.getUnclearedEventsByApplicationAndType(pwaApplicationDetail.getPwaApplication(), PwaApplicationEventType.CONSENT_ISSUE_FAILED);

    verify(pwaApplicationEventRepository, times(1))
        .findPwaApplicationEventsByPwaApplicationAndEventTypeAndEventClearedInstantIsNull(pwaApplicationDetail.getPwaApplication(), PwaApplicationEventType.CONSENT_ISSUE_FAILED);

  }

  @Test
  public void clearEvents() {

    var event1 = new PwaApplicationEvent(pwaApplicationDetail.getPwaApplication(), PwaApplicationEventType.CONSENT_ISSUE_FAILED, clock.instant(), user);
    var event2 = new PwaApplicationEvent(pwaApplicationDetail.getPwaApplication(), PwaApplicationEventType.CONSENT_ISSUE_FAILED, clock.instant().minusSeconds(60), user);

    when(pwaApplicationEventRepository.findPwaApplicationEventsByPwaApplicationAndEventTypeAndEventClearedInstantIsNull(
        pwaApplicationDetail.getPwaApplication(), PwaApplicationEventType.CONSENT_ISSUE_FAILED))
        .thenReturn(List.of(event1, event2));

    pwaApplicationEventService.clearEvents(pwaApplicationDetail.getPwaApplication(), PwaApplicationEventType.CONSENT_ISSUE_FAILED);

    verify(pwaApplicationEventRepository, times(1)).saveAll(pwaApplicationEventListArgumentCaptor.capture());

    assertThat(pwaApplicationEventListArgumentCaptor.getValue()).containsExactlyInAnyOrder(event1, event2);

    assertThat(pwaApplicationEventListArgumentCaptor.getValue()).allSatisfy(event -> {
      assertThat(event.getEventClearedInstant()).isEqualTo(clock.instant());
    });

  }

}