package uk.co.ogauthority.pwa.service.pwaapplications.context;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;

@Service
public class PwaApplicationPermissionService {

  private final PwaContactService pwaContactService;
  private final PwaHolderTeamService pwaHolderTeamService;

  @Autowired
  public PwaApplicationPermissionService(PwaContactService pwaContactService,
                                         PwaHolderTeamService pwaHolderTeamService) {
    this.pwaContactService = pwaContactService;
    this.pwaHolderTeamService = pwaHolderTeamService;
  }

  public Set<PwaApplicationPermission> getPermissions(PwaApplicationDetail detail, Person person) {

    var contactRoles = pwaContactService.getContactRoles(detail.getPwaApplication(), person);

    var holderTeamRoles = pwaHolderTeamService.getRolesInHolderTeam(detail, person);

    return PwaApplicationPermission.stream()
        .filter(permission -> userHasPermission(permission, contactRoles, holderTeamRoles))
        .collect(Collectors.toSet());

  }

  private boolean userHasPermission(PwaApplicationPermission permission,
                                    Set<PwaContactRole> usersContactRoles,
                                    Set<PwaOrganisationRole> userHolderTeamRoles) {

    boolean userHasContactRoles = !Collections.disjoint(permission.getContactRoles(), usersContactRoles);

    boolean userHasHolderTeamRoles = !Collections.disjoint(permission.getHolderTeamRoles(), userHolderTeamRoles);

    return userHasContactRoles || userHasHolderTeamRoles;

  }

}
