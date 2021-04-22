package uk.co.ogauthority.pwa.service.pwacontext;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;
import uk.co.ogauthority.pwa.service.teams.TeamService;

@Service
public class PwaPermissionService {

  private final TeamService teamService;
  private final PwaHolderTeamService pwaHolderTeamService;

  @Autowired
  public PwaPermissionService(TeamService teamService,
                              PwaHolderTeamService pwaHolderTeamService) {
    this.teamService = teamService;
    this.pwaHolderTeamService = pwaHolderTeamService;
  }


  public Set<PwaPermission> getPwaPermissions(MasterPwa masterPwa,
                                              AuthenticatedUserAccount user) {
    return Arrays.stream(PwaPermission.values())
        .filter(permission -> {
          if (permission == PwaPermission.VIEW_PWA) {
            return pwaHolderTeamService.isPersonInHolderTeam(masterPwa, user.getLinkedPerson()) || userHasRegulatorRole(
                user.getLinkedPerson());
          }
          return false;

        })
        .collect(Collectors.toSet());
  }



  private boolean userHasRegulatorRole(Person person) {
    return teamService.getRegulatorTeamIfPersonInRole(person, EnumSet.allOf(PwaRegulatorRole.class)).isPresent();
  }


}
