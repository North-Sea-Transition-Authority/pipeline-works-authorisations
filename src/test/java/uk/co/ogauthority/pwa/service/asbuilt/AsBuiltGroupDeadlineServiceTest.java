package uk.co.ogauthority.pwa.service.asbuilt;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroup;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupDetail;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupTestUtil;
import uk.co.ogauthority.pwa.repository.asbuilt.AsBuiltNotificationGroupDetailRepository;

@RunWith(MockitoJUnitRunner.class)
public class AsBuiltGroupDeadlineServiceTest {

  private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

  private AsBuiltGroupDeadlineService asBuiltGroupDeadlineService;

  @Mock
  private AsBuiltNotificationGroupDetailRepository asBuiltNotificationGroupDetailRepository;

  @Captor
  private ArgumentCaptor<AsBuiltNotificationGroupDetail> groupDetailArgumentCaptor;

  private AsBuiltNotificationGroup asBuiltGroup;

  private Person person;


  @Before
  public void setUp() throws Exception {

    asBuiltGroupDeadlineService = new AsBuiltGroupDeadlineService(asBuiltNotificationGroupDetailRepository, clock);

    asBuiltGroup = AsBuiltNotificationGroupTestUtil.createDefaultGroupWithConsent();
    person = PersonTestUtil.createDefaultPerson();
  }

  @Test
  public void setNewDeadline_noCurrentGroupDetailExists() {
    var deadline = LocalDate.now(clock).plusWeeks(1);

    asBuiltGroupDeadlineService.setNewDeadline(asBuiltGroup, deadline, person);

    verify(asBuiltNotificationGroupDetailRepository).save(groupDetailArgumentCaptor.capture());

    assertThat(groupDetailArgumentCaptor.getAllValues()).hasOnlyOneElementSatisfying(asBuiltNotificationGroupDetail -> {
      assertThat(asBuiltNotificationGroupDetail.getDeadlineDate()).isEqualTo(deadline);
      assertThat(asBuiltNotificationGroupDetail.getAsBuiltNotificationGroup()).isEqualTo(asBuiltGroup);
      assertThat(asBuiltNotificationGroupDetail.getCreatedTimestamp()).isEqualTo(clock.instant());
      assertThat(asBuiltNotificationGroupDetail.getCreatedByPersonId()).isEqualTo(person.getId());
      assertThat(asBuiltNotificationGroupDetail.getEndedTimestamp()).isNull();
      assertThat(asBuiltNotificationGroupDetail.getEndedByPersonId()).isNull();
    });

  }

  @Test
  public void setNewDeadline_currentGroupDetailExists() {
    var oldDeadline = LocalDate.now(clock).minusWeeks(1);
    var deadline = LocalDate.now(clock).plusWeeks(1);

    var differentPerson = PersonTestUtil.createPersonFrom(new PersonId(1));

    var oldDetail = new AsBuiltNotificationGroupDetail(
        asBuiltGroup, oldDeadline, person.getId(), clock.instant().minus(2, ChronoUnit.HOURS)
    );

    when(asBuiltNotificationGroupDetailRepository.findByAsBuiltNotificationGroupAndEndedTimestampIsNull(any()))
        .thenReturn(Optional.of(oldDetail));

    asBuiltGroupDeadlineService.setNewDeadline(asBuiltGroup, deadline, differentPerson);

    verify(asBuiltNotificationGroupDetailRepository, times(2)).save(groupDetailArgumentCaptor.capture());

    assertThat(groupDetailArgumentCaptor.getAllValues()).hasSize(2);

    assertThat(groupDetailArgumentCaptor.getAllValues())
        .element(0)
        .satisfies(asBuiltNotificationGroupDetail -> {
          assertThat(asBuiltNotificationGroupDetail.getDeadlineDate()).isEqualTo(oldDetail.getDeadlineDate());
          assertThat(asBuiltNotificationGroupDetail.getAsBuiltNotificationGroup()).isEqualTo(oldDetail.getAsBuiltNotificationGroup());
          assertThat(asBuiltNotificationGroupDetail.getCreatedTimestamp()).isEqualTo(oldDetail.getCreatedTimestamp());
          assertThat(asBuiltNotificationGroupDetail.getCreatedByPersonId()).isEqualTo(oldDetail.getCreatedByPersonId());
          assertThat(asBuiltNotificationGroupDetail.getEndedTimestamp()).isEqualTo(clock.instant());
          assertThat(asBuiltNotificationGroupDetail.getEndedByPersonId()).isEqualTo(differentPerson.getId());
        });

    assertThat(groupDetailArgumentCaptor.getAllValues())
        .element(1)
        .satisfies(asBuiltNotificationGroupDetail -> {
          assertThat(asBuiltNotificationGroupDetail.getDeadlineDate()).isEqualTo(deadline);
          assertThat(asBuiltNotificationGroupDetail.getAsBuiltNotificationGroup()).isEqualTo(asBuiltGroup);
          assertThat(asBuiltNotificationGroupDetail.getCreatedTimestamp()).isEqualTo(clock.instant());
          assertThat(asBuiltNotificationGroupDetail.getCreatedByPersonId()).isEqualTo(differentPerson.getId());
          assertThat(asBuiltNotificationGroupDetail.getEndedTimestamp()).isNull();
          assertThat(asBuiltNotificationGroupDetail.getEndedByPersonId()).isNull();
        });
  }
}