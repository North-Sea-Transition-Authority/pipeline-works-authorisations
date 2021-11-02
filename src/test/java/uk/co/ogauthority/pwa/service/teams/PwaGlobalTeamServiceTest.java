package uk.co.ogauthority.pwa.service.teams;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactRole;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupTeamMember;
import uk.co.ogauthority.pwa.model.teams.PwaGlobalRole;
import uk.co.ogauthority.pwa.model.teams.PwaGlobalTeam;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaApplicationContactRoleDto;
import uk.co.ogauthority.pwa.service.teams.events.NonFoxTeamMemberModificationEvent;
import uk.co.ogauthority.pwa.service.users.UserAccountService;

@RunWith(MockitoJUnitRunner.class)
public class PwaGlobalTeamServiceTest {

  @Mock
  private TeamService teamService;

  @Mock
  private UserAccountService userAccountService;

  @Mock
  private PwaContactService pwaContactService;

  @Mock
  private ConsulteeGroupTeamService consulteeGroupTeamService;

  private PwaGlobalTeamService pwaGlobalTeamService;

  private PwaGlobalTeam pwaGlobalTeam;
  private Person person = PersonTestUtil.createDefaultPerson();
  private WebUserAccount systemWebUserAccount = new WebUserAccount();

  @Before
  public void setUp() {

    pwaGlobalTeam = new PwaGlobalTeam(1, "G", "Global");
    when(teamService.getGlobalTeam()).thenReturn(pwaGlobalTeam);

    when(userAccountService.getSystemWebUserAccount()).thenReturn(systemWebUserAccount);

    pwaGlobalTeamService = new PwaGlobalTeamService(teamService, userAccountService, pwaContactService, consulteeGroupTeamService);

  }

  @Test
  public void updateGlobalAccessTeamMembership_addedEvent_alreadyMemberOfTeam() {

    var event = new NonFoxTeamMemberModificationEvent(person, NonFoxTeamMemberModificationEvent.EventType.ADDED);

    when(teamService.isPersonMemberOfTeam(person, pwaGlobalTeam)).thenReturn(true);

    pwaGlobalTeamService.updateGlobalAccessTeamMembership(event);

    verify(teamService, times(0)).addPersonToTeamInRoles(any(), any(), any(), any());

  }

  @Test
  public void updateGlobalAccessTeamMembership_addedEvent_notAlreadyMemberOfTeam() {

    var event = new NonFoxTeamMemberModificationEvent(person, NonFoxTeamMemberModificationEvent.EventType.ADDED);

    when(teamService.isPersonMemberOfTeam(person, pwaGlobalTeam)).thenReturn(false);

    pwaGlobalTeamService.updateGlobalAccessTeamMembership(event);

    verify(teamService, times(1)).addPersonToTeamInRoles(pwaGlobalTeam, person, List.of(PwaGlobalRole.PWA_ACCESS.getPortalTeamRoleName()), systemWebUserAccount);

  }

  @Test
  public void updateGlobalAccessTeamMembership_removedEvent_isStillContact() {

    var event = new NonFoxTeamMemberModificationEvent(person, NonFoxTeamMemberModificationEvent.EventType.REMOVED);

    var roleDto = new PwaApplicationContactRoleDto(person.getId().asInt(), 1, PwaContactRole.PREPARER);
    when(pwaContactService.getPwaContactRolesForPerson(person, EnumSet.allOf(PwaContactRole.class))).thenReturn(Set.of(roleDto));

    pwaGlobalTeamService.updateGlobalAccessTeamMembership(event);

    verify(teamService, times(0)).removePersonFromTeam(any(), any(), any());

  }

  @Test
  public void updateGlobalAccessTeamMembership_removedEvent_isStillConsultee() {

    var event = new NonFoxTeamMemberModificationEvent(person, NonFoxTeamMemberModificationEvent.EventType.REMOVED);

    var member = new ConsulteeGroupTeamMember();
    when(consulteeGroupTeamService.getTeamMemberByPerson(person)).thenReturn(Optional.of(member));

    pwaGlobalTeamService.updateGlobalAccessTeamMembership(event);

    verify(teamService, times(0)).removePersonFromTeam(any(), any(), any());

  }

  @Test
  public void updateGlobalAccessTeamMembership_removedEvent_noFurtherInvolvement() {

    var event = new NonFoxTeamMemberModificationEvent(person, NonFoxTeamMemberModificationEvent.EventType.REMOVED);

    when(pwaContactService.getPwaContactRolesForPerson(person, EnumSet.allOf(PwaContactRole.class))).thenReturn(Set.of());
    when(consulteeGroupTeamService.getTeamMemberByPerson(person)).thenReturn(Optional.empty());

    pwaGlobalTeamService.updateGlobalAccessTeamMembership(event);

    verify(teamService, times(1)).removePersonFromTeam(any(), any(), any());

  }

}