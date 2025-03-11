package uk.co.ogauthority.pwa.service.asbuilt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamType;

@Service
public class AsBuiltNotificationAuthService {

  private final AsBuiltNotificationGroupService asBuiltNotificationGroupService;
  private final PwaHolderTeamService pwaHolderTeamService;
  private final TeamQueryService teamQueryService;

  @Autowired
  public AsBuiltNotificationAuthService(
      AsBuiltNotificationGroupService asBuiltNotificationGroupService,
      PwaHolderTeamService pwaHolderTeamService,
      TeamQueryService teamQueryService) {
    this.asBuiltNotificationGroupService = asBuiltNotificationGroupService;
    this.pwaHolderTeamService = pwaHolderTeamService;
    this.teamQueryService = teamQueryService;
  }

  public boolean canPersonAccessAsbuiltNotificationGroup(AuthenticatedUserAccount user, Integer ngId) {
    var masterPwa = asBuiltNotificationGroupService.getMasterPwaForAsBuiltNotificationGroup(ngId);
    return isUserAsBuiltNotificationAdmin(user) || isPersonAsBuiltSubmitterInHolderTeam(user.getLinkedPerson(), masterPwa);
  }

  public boolean isUserAsBuiltNotificationAdmin(WebUserAccount user) {
    return teamQueryService.userHasStaticRole((long) user.getWuaId(), TeamType.REGULATOR, Role.AS_BUILT_NOTIFICATION_ADMIN);
  }

  private boolean isPersonAsBuiltSubmitterInHolderTeam(Person person, MasterPwa masterPwa) {
    return pwaHolderTeamService.isPersonInHolderTeamWithRole(masterPwa, person, PwaOrganisationRole.AS_BUILT_NOTIFICATION_SUBMITTER);
  }

}
