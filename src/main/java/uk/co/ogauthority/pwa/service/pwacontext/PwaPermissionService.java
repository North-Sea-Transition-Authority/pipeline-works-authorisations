package uk.co.ogauthority.pwa.service.pwacontext;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamType;

@Service
public class PwaPermissionService {

  private final PwaHolderTeamService pwaHolderTeamService;
  private final TeamQueryService teamQueryService;

  @Autowired
  public PwaPermissionService(PwaHolderTeamService pwaHolderTeamService,
                              TeamQueryService teamQueryService) {
    this.pwaHolderTeamService = pwaHolderTeamService;
    this.teamQueryService = teamQueryService;
  }


  public Set<PwaPermission> getPwaPermissions(MasterPwa masterPwa,
                                              AuthenticatedUserAccount user) {
    var personInHolderTeam = pwaHolderTeamService.isPersonInHolderTeam(masterPwa, user);
    var userIsRegulator = userIsRegulator(user);

    return Arrays.stream(PwaPermission.values())
        .filter(permission -> switch (permission) {
          case VIEW_PWA_PIPELINE ->
              personInHolderTeam
              || userIsRegulator
              || user.hasPrivilege(PwaUserPrivilege.PIPELINE_VIEW);
          case VIEW_PWA, SHOW_PWA_NAVIGATION ->
              // split out show nav priv so users with external access only do not see other system areas they cannot access.
              personInHolderTeam
              || userIsRegulator;
        })
        .collect(Collectors.toSet());
  }

  private boolean userIsRegulator(AuthenticatedUserAccount userAccount) {
    return teamQueryService.userIsMemberOfStaticTeam((long) userAccount.getWuaId(), TeamType.REGULATOR);
  }
}
