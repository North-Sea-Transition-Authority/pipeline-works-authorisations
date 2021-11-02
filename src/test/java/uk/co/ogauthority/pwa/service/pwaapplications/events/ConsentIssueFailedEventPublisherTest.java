package uk.co.ogauthority.pwa.service.pwaapplications.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class ConsentIssueFailedEventPublisherTest {

  @Mock
  private ApplicationEventPublisher applicationEventPublisher;

  private ConsentIssueFailedEventPublisher consentIssueFailedEventPublisher;

  @Captor
  private ArgumentCaptor<ConsentIssueFailedEvent> failedEventArgumentCaptor;

  private PwaApplicationDetail detail;
  private WebUserAccount issuingUser = new WebUserAccount(1, PersonTestUtil.createDefaultPerson());

  @Before
  public void setUp() throws Exception {

    consentIssueFailedEventPublisher = new ConsentIssueFailedEventPublisher(applicationEventPublisher);

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

  }

  @Test
  public void publishConsentIssueFailedEvent() {

    var ex = mock(Exception.class);

    consentIssueFailedEventPublisher.publishConsentIssueFailedEvent(detail, ex, issuingUser);

    verify(applicationEventPublisher, times(1)).publishEvent(failedEventArgumentCaptor.capture());

    assertThat(failedEventArgumentCaptor.getValue()).satisfies(failedEvent -> {
      assertThat(failedEvent.getPwaApplicationDetail()).isEqualTo(detail);
      assertThat(failedEvent.getException()).isEqualTo(ex);
      assertThat(failedEvent.getIssuingUser()).isEqualTo(issuingUser);
    });

  }

}