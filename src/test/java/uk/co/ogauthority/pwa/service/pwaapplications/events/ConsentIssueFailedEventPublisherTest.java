package uk.co.ogauthority.pwa.service.pwaapplications.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class ConsentIssueFailedEventPublisherTest {

  @Mock
  private ApplicationEventPublisher applicationEventPublisher;

  private ConsentIssueFailedEventPublisher consentIssueFailedEventPublisher;

  @Captor
  private ArgumentCaptor<ConsentIssueFailedEvent> failedEventArgumentCaptor;

  private PwaApplicationDetail detail;
  private WebUserAccount issuingUser = new WebUserAccount(1, PersonTestUtil.createDefaultPerson());

  @BeforeEach
  void setUp() throws Exception {

    consentIssueFailedEventPublisher = new ConsentIssueFailedEventPublisher(applicationEventPublisher);

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

  }

  @Test
  void publishConsentIssueFailedEvent() {

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