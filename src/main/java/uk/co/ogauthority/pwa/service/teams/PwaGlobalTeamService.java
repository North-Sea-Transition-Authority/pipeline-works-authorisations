package uk.co.ogauthority.pwa.service.teams;

import java.util.EnumSet;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactRole;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactService;
import uk.co.ogauthority.pwa.model.teams.PwaGlobalRole;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;
import uk.co.ogauthority.pwa.service.teams.events.NonFoxTeamMemberModificationEvent;
import uk.co.ogauthority.pwa.service.users.UserAccountService;

@Service
public class PwaGlobalTeamService {

  private final TeamService teamService;
  private final UserAccountService userAccountService;
  private final PwaContactService pwaContactService;
  private final ConsulteeGroupTeamService consulteeGroupTeamService;

  @Autowired
  public PwaGlobalTeamService(TeamService teamService,
                              UserAccountService userAccountService,
                              PwaContactService pwaContactService,
                              ConsulteeGroupTeamService consulteeGroupTeamService) {
    this.teamService = teamService;
    this.userAccountService = userAccountService;
    this.pwaContactService = pwaContactService;
    this.consulteeGroupTeamService = consulteeGroupTeamService;
  }

  @EventListener
  @Transactional
  public void updateGlobalAccessTeamMembership(NonFoxTeamMemberModificationEvent modificationEvent) {

    var globalTeam = teamService.getGlobalTeam();
    var person = modificationEvent.getPerson();
    var systemWebUserAccount = userAccountService.getSystemWebUserAccount();

    if (modificationEvent.getEventType() == NonFoxTeamMemberModificationEvent.EventType.ADDED) {

      boolean alreadyMember = teamService.isPersonMemberOfTeam(person, globalTeam);

      if (!alreadyMember) {
        teamService.addPersonToTeamInRoles(
            globalTeam,
            person,
            List.of(PwaGlobalRole.PWA_ACCESS.getPortalTeamRoleName()),
            systemWebUserAccount
        );
      }

    } else if (modificationEvent.getEventType() == NonFoxTeamMemberModificationEvent.EventType.REMOVED) {

      boolean noLongerContact = pwaContactService
          .getPwaContactRolesForPerson(person, EnumSet.allOf(PwaContactRole.class))
          .isEmpty();

      boolean noLongerConsultee = consulteeGroupTeamService
          .getTeamMemberByPerson(person)
          .isEmpty();

      if (noLongerContact && noLongerConsultee) {
        teamService.removePersonFromTeam(globalTeam, person, systemWebUserAccount);
      }

    }

  }

}
