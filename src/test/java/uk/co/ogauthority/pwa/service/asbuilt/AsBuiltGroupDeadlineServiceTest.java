package uk.co.ogauthority.pwa.service.asbuilt;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroup;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupDetail;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupDetailTestUtil;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupTestUtil;
import uk.co.ogauthority.pwa.repository.asbuilt.AsBuiltNotificationGroupDetailRepository;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaHolderService;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;
import uk.co.ogauthority.pwa.teams.Role;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AsBuiltGroupDeadlineServiceTest {

  private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

  private AsBuiltGroupDeadlineService asBuiltGroupDeadlineService;

  @Mock
  private AsBuiltNotificationGroupDetailRepository asBuiltNotificationGroupDetailRepository;

  @Mock
  private AsBuiltNotificationGroupStatusService asBuiltNotificationGroupStatusService;

  @Mock
  private PwaHolderTeamService pwaHolderTeamService;

  @Mock
  private PwaHolderService pwaHolderService;

  @Mock
  private AsBuiltNotificationEmailService asBuiltNotificationEmailService;

  @Captor
  private ArgumentCaptor<AsBuiltNotificationGroupDetail> groupDetailArgumentCaptor;

  private final Person person = PersonTestUtil.createDefaultPerson();

  private final AsBuiltNotificationGroup asBuiltGroup = AsBuiltNotificationGroupTestUtil.createDefaultGroupWithConsent();

  private final PortalOrganisationGroup portalOrganisationGroup = PortalOrganisationTestUtils
      .generateOrganisationGroup(20, "Organisation", "Org");

  private final Multimap<PortalOrganisationGroup, Integer> orgToMasterPwaIdMultiMap = ArrayListMultimap.create();

  @BeforeEach
  void setUp() throws Exception {
    asBuiltGroupDeadlineService = new AsBuiltGroupDeadlineService(asBuiltNotificationGroupDetailRepository,
        asBuiltNotificationGroupStatusService, pwaHolderTeamService, pwaHolderService, asBuiltNotificationEmailService, clock);

    orgToMasterPwaIdMultiMap.put(portalOrganisationGroup, asBuiltGroup.getPwaConsent().getMasterPwa().getId());

    when(pwaHolderService.getHolderOrgGroupsForMasterPwaIds(Set.of(asBuiltGroup.getPwaConsent().getMasterPwa().getId())))
        .thenReturn(orgToMasterPwaIdMultiMap);
    when(asBuiltNotificationGroupStatusService.getAllNonCompleteAsBuiltNotificationGroups()).thenReturn(
        List.of(asBuiltGroup));
    when(pwaHolderTeamService.getPeopleWithHolderTeamRoleForOrgGroup(portalOrganisationGroup, Role.AS_BUILT_NOTIFICATION_SUBMITTER))
        .thenReturn(Set.of(person));
  }

  @Test
  void setNewDeadline_noCurrentGroupDetailExists() {
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
  void setNewDeadline_currentGroupDetailExists() {
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

  @Test
  void notifyHoldersOfAsBuiltGroupDeadlines_upcomingDeadline() {
    var asBuiltNotificationGroupDetail = AsBuiltNotificationGroupDetailTestUtil
            .createAsBuiltNotificationGroupDetail_fromAsBuiltNotificationGroupAndDeadlineDateAndPerson(
                asBuiltGroup, LocalDate.now().plusDays(7), person);

    when(asBuiltNotificationGroupDetailRepository.findAllByAsBuiltNotificationGroupInAndDeadlineDate(List.of(asBuiltGroup),
        LocalDate.now(clock).plusWeeks(1)))
        .thenReturn(List.of(asBuiltNotificationGroupDetail));

    asBuiltGroupDeadlineService.notifyHoldersOfAsBuiltGroupDeadlines();
    verify(asBuiltNotificationEmailService).sendUpcomingDeadlineEmail(person.getEmailAddress(),
        person.getFullName(), asBuiltGroup.getReference());
    verify(asBuiltNotificationEmailService, never()).sendDeadlinePassedEmail(person.getEmailAddress(),
        person.getFullName(), asBuiltGroup.getReference());
  }

  @Test
  void notifyHoldersOfAsBuiltGroupDeadlines_passedDeadline() {
    var asBuiltNotificationGroupDetail = AsBuiltNotificationGroupDetailTestUtil
        .createAsBuiltNotificationGroupDetail_fromAsBuiltNotificationGroupAndDeadlineDateAndPerson(
            asBuiltGroup, LocalDate.now(clock).plusDays(7), person);

    when(asBuiltNotificationGroupDetailRepository.findAllByAsBuiltNotificationGroupInAndDeadlineDate(List.of(asBuiltGroup),
        LocalDate.now(clock).minusDays(1)))
        .thenReturn(List.of(asBuiltNotificationGroupDetail));

    asBuiltGroupDeadlineService.notifyHoldersOfAsBuiltGroupDeadlines();
    verify(asBuiltNotificationEmailService).sendDeadlinePassedEmail(person.getEmailAddress(),
        person.getFullName(), asBuiltGroup.getReference());
    verify(asBuiltNotificationEmailService, never()).sendUpcomingDeadlineEmail(person.getEmailAddress(),
        person.getFullName(), asBuiltGroup.getReference());
  }

  @Test
  void notifyHoldersOfAsBuiltGroupDeadlines_noDeadlines() {
    when(asBuiltNotificationGroupDetailRepository.findAllByAsBuiltNotificationGroupInAndDeadlineDate(eq(List.of(asBuiltGroup)), any()))
        .thenReturn(List.of());

    asBuiltGroupDeadlineService.notifyHoldersOfAsBuiltGroupDeadlines();
    verify(asBuiltNotificationEmailService, never()).sendDeadlinePassedEmail(person.getEmailAddress(), person.getFullName(),
        asBuiltGroup.getReference());
    verify(asBuiltNotificationEmailService, never()).sendUpcomingDeadlineEmail(person.getEmailAddress(), person.getFullName(),
        asBuiltGroup.getReference());
  }

}