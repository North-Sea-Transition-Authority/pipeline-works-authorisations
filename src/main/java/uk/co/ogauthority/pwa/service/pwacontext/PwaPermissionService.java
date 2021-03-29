package uk.co.ogauthority.pwa.service.pwacontext;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationTeam;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.service.pwaconsents.MasterPwaHolderDto;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentOrganisationRoleService;
import uk.co.ogauthority.pwa.service.teams.TeamService;

@Service
public class PwaPermissionService {

  private final TeamService teamService;
  private final PwaConsentOrganisationRoleService pwaConsentOrganisationRoleService;

  @Autowired
  public PwaPermissionService(TeamService teamService,
                              PwaConsentOrganisationRoleService pwaConsentOrganisationRoleService) {
    this.teamService = teamService;
    this.pwaConsentOrganisationRoleService = pwaConsentOrganisationRoleService;
  }


  public Set<PwaPermission> getPwaPermissions(MasterPwa masterPwa,
                                              AuthenticatedUserAccount user) {
    return Arrays.stream(PwaPermission.values())
        .filter(permission -> {
          if (permission == PwaPermission.VIEW_PWA) {
            return isUserInHolderTeam(masterPwa, user.getLinkedPerson()) || userHasRegulatorRole(
                user.getLinkedPerson());
          }
          return false;

        })
        .collect(Collectors.toSet());
  }

  private boolean isUserInHolderTeam(MasterPwa masterPwa, Person person) {
    Set<PortalOrganisationGroup> holderOrgGroups = pwaConsentOrganisationRoleService
        .getCurrentHoldersOrgRolesForMasterPwa(masterPwa)
        .stream()
        .map(MasterPwaHolderDto::getHolderOrganisationGroup)
        .flatMap(Optional::stream)
        .collect(Collectors.toSet());

    return teamService.getOrganisationTeamListIfPersonInRole(person, EnumSet.allOf(PwaOrganisationRole.class)).stream()
        .map(PwaOrganisationTeam::getPortalOrganisationGroup)
        .anyMatch(holderOrgGroups::contains);
  }

  private boolean userHasRegulatorRole(Person person) {
    return teamService.getRegulatorTeamIfPersonInRole(person, EnumSet.allOf(PwaRegulatorRole.class)).isPresent();
  }




}
