package uk.co.ogauthority.pwa.service.appprocessing.options;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.model.entity.appprocessing.options.OptionsApplicationApproval;
import uk.co.ogauthority.pwa.model.entity.appprocessing.options.OptionsApprovalDeadlineHistory;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.repository.appprocessing.options.OptionsApplicationApprovalRepository;
import uk.co.ogauthority.pwa.repository.appprocessing.options.OptionsApprovalDeadlineHistoryRepository;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class OptionsApprovalPersisterTest {
  private static final String NOTE = "a note";
  private static final PersonId PERSON_ID = new PersonId(1);

  @Mock
  private OptionsApplicationApprovalRepository optionsApplicationApprovalRepository;

  @Mock
  private OptionsApprovalDeadlineHistoryRepository optionsApprovalDeadlineHistoryRepository;

  @Captor
  private ArgumentCaptor<OptionsApprovalDeadlineHistory> deadlineCaptor;

  private OptionsApprovalPersister optionsApprovalPersister;

  private Clock clock;

  private Instant deadlineDate;

  private PwaApplicationDetail pwaApplicationDetail;
  private PwaApplication pwaApplication;

  private Person person;

  @Before
  public void setUp() throws Exception {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.OPTIONS_VARIATION);
    pwaApplication = pwaApplicationDetail.getPwaApplication();

    person = PersonTestUtil.createPersonFrom(PERSON_ID);
    deadlineDate = Instant.MAX;

    clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

    optionsApprovalPersister = new OptionsApprovalPersister(
        optionsApplicationApprovalRepository,
        optionsApprovalDeadlineHistoryRepository,
        clock
    );

    when(optionsApplicationApprovalRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(optionsApprovalDeadlineHistoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
  }

  @Test
  public void createInitialOptionsApproval_serviceInteractions_andAttributesSetAsExpected() {

    var history = optionsApprovalPersister.createInitialOptionsApproval(pwaApplication, person, deadlineDate);
    var approval = history.getOptionsApplicationApproval();

    verify(optionsApplicationApprovalRepository, times(1)).save(any());
    verify(optionsApprovalDeadlineHistoryRepository, times(1)).save(any());

    assertThat(history.getCreatedByPersonId()).isEqualTo(PERSON_ID);
    assertThat(history.getCreatedTimestamp()).isEqualTo(clock.instant());
    assertThat(history.getNote()).isNull();
    assertThat(history.getDeadlineDate()).isEqualTo(deadlineDate);
    assertThat(history.getOptionsApplicationApproval()).isNotNull();

    assertThat(approval.getCreatedByPersonId()).isEqualTo(PERSON_ID);
    assertThat(approval.getCreatedTimestamp()).isEqualTo(clock.instant());
    assertThat(approval.getPwaApplication()).isEqualTo(pwaApplication);
  }

  @Test
  public void endTipDeadlineHistoryItem_setsAndSavesTipFlagOfExistingDeadline() {
    var approval = new OptionsApplicationApproval();
    var deadline = new OptionsApprovalDeadlineHistory();
    when(optionsApprovalDeadlineHistoryRepository.findByOptionsApplicationApprovalAndTipFlagIsTrue(approval))
        .thenReturn(Optional.of(deadline));

    optionsApprovalPersister.endTipDeadlineHistoryItem(approval);

    verify(optionsApprovalDeadlineHistoryRepository, times(1)).save(deadlineCaptor.capture());

    assertThat(deadlineCaptor.getValue().isTipFlag()).isFalse();

  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void endTipDeadlineHistoryItem_noTipDeadline() {
    var approval = new OptionsApplicationApproval();

    when(optionsApprovalDeadlineHistoryRepository.findByOptionsApplicationApprovalAndTipFlagIsTrue(approval))
        .thenReturn(Optional.empty());

    optionsApprovalPersister.endTipDeadlineHistoryItem(approval);

  }

  @Test
  public void createTipDeadlineHistoryItem_createNewtipDeadlineItemAndSetsValues() {
    var approval = new OptionsApplicationApproval();

    optionsApprovalPersister.createTipDeadlineHistoryItem(approval, person, deadlineDate, NOTE);

    verify(optionsApprovalDeadlineHistoryRepository, times(1)).save(deadlineCaptor.capture());
    var deadline = deadlineCaptor.getValue();

    assertThat(deadline.getOptionsApplicationApproval()).isSameAs(approval);
    assertThat(deadline.getDeadlineDate()).isEqualTo(deadlineDate);
    assertThat(deadline.getNote()).isEqualTo(NOTE);
    assertThat(deadline.isTipFlag()).isTrue();
    assertThat(deadline.getCreatedTimestamp()).isEqualTo(clock.instant());
    assertThat(deadline.getCreatedByPersonId()).isEqualTo(person.getId());
  }
}