package uk.co.ogauthority.pwa.service.asbuilt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;
import uk.co.ogauthority.pwa.service.teams.PwaTeamService;

@Service
public class AsBuiltNotificationAuthService {

  private final AsBuiltNotificationGroupService asBuiltNotificationGroupService;
  private final PwaTeamService pwaTeamService;
  private final PwaHolderTeamService pwaHolderTeamService;

  @Autowired
  public AsBuiltNotificationAuthService(
      AsBuiltNotificationGroupService asBuiltNotificationGroupService,
      PwaTeamService pwaTeamService, PwaHolderTeamService pwaHolderTeamService) {
    this.asBuiltNotificationGroupService = asBuiltNotificationGroupService;
    this.pwaTeamService = pwaTeamService;
    this.pwaHolderTeamService = pwaHolderTeamService;
  }

  public boolean canPersonAccessAsbuiltNotificationGroup(Person person, Integer ngId) {
    var masterPwa = asBuiltNotificationGroupService.getMasterPwaForAsBuiltNotificationGroup(ngId);
    return isPersonAsBuiltNotificationAdmin(person) || isPersonAsBuiltSubmitterInHolderTeam(person, masterPwa);
  }

  public boolean isPersonAsBuiltNotificationAdmin(Person person) {
    return pwaTeamService.getPeopleWithRegulatorRole(PwaRegulatorRole.AS_BUILT_NOTIFICATION_ADMIN).contains(person);
  }

  private boolean isPersonAsBuiltSubmitterInHolderTeam(Person person, MasterPwa masterPwa) {
    return pwaHolderTeamService.isPersonInHolderTeamWithRole(masterPwa, person, PwaOrganisationRole.AS_BUILT_NOTIFICATION_SUBMITTER);
  }

}
