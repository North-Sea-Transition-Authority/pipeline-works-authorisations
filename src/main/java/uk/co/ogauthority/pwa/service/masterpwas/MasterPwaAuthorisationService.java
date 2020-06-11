package uk.co.ogauthority.pwa.service.masterpwas;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationTeam;
import uk.co.ogauthority.pwa.repository.masterpwas.MasterPwaDetailRepository;
import uk.co.ogauthority.pwa.repository.masterpwas.MasterPwaRepository;
import uk.co.ogauthority.pwa.service.pwaconsents.MasterPwaHolderDto;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentOrganisationRoleService;
import uk.co.ogauthority.pwa.service.teams.TeamService;

/**
 * Get Master PWAs a given web user account has authorisation to access.
 */
@Service
public class MasterPwaAuthorisationService {

  private final MasterPwaRepository masterPwaRepository;
  private final MasterPwaDetailRepository masterPwaDetailRepository;
  private final TeamService teamService;
  private final PortalOrganisationsAccessor portalOrganisationsAccessor;
  private final PwaConsentOrganisationRoleService pwaConsentOrganisationRoleService;

  @Autowired
  public MasterPwaAuthorisationService(
      MasterPwaRepository masterPwaRepository,
      MasterPwaDetailRepository masterPwaDetailRepository, TeamService teamService,
      PortalOrganisationsAccessor portalOrganisationsAccessor,
      PwaConsentOrganisationRoleService pwaConsentOrganisationRoleService) {
    this.masterPwaRepository = masterPwaRepository;
    this.masterPwaDetailRepository = masterPwaDetailRepository;
    this.teamService = teamService;
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
    this.pwaConsentOrganisationRoleService = pwaConsentOrganisationRoleService;
  }

  /*
   * Return PWA where the user has a given role within the PWA organisation team and where an organisation unit linked to
   * the team's organisation group is a current HOLDER of the PWA.
   */
  public MasterPwa getMasterPwaIfAuthorised(int masterPwaId, WebUserAccount requestingWebUserAccount,
                                            PwaOrganisationRole requiredOrganisationRole) {
    // get all operator teams where the user has the desired role
    var organisationGroups = teamService.getOrganisationTeamListIfPersonInRole(
        requestingWebUserAccount.getLinkedPerson(), Set.of(requiredOrganisationRole))
        .stream()
        .map(PwaOrganisationTeam::getPortalOrganisationGroup)
        .collect(Collectors.toSet());

    var masterPwa = masterPwaRepository.findById(masterPwaId).orElseThrow(() ->
        new PwaEntityNotFoundException("No master pwa with id:" + masterPwaId));

    var masterPwaHolders = pwaConsentOrganisationRoleService.getCurrentHoldersOrgRolesForMasterPwa(masterPwa);

    // provided the organisation group user has role within holder team for master pwa, return the master pwa
    if (masterPwaHolders.stream()
        .map(MasterPwaHolderDto::getHolderOrganisationGroup)
        .map(optionalPortalOrg -> optionalPortalOrg.orElse(null))
        .filter(Objects::nonNull)
        .noneMatch(organisationGroups::contains)
    ) {
      throw new AccessDeniedException(
          String.format(
              "User not in necessary role within holder team. role:%s masterPwaId:%s",
              requiredOrganisationRole,
              masterPwaId));
    }

    return masterPwa;

  }

  public List<MasterPwaDetail> getCurrentMasterPwaDetails(Collection<MasterPwa> masterPwas) {
    return masterPwaDetailRepository.findByMasterPwaInAndEndInstantIsNull(masterPwas);
  }


  /*
   * Return all MasterPwa's where the user exists in the desired role with the PWa's holder team.
   */
  public Set<MasterPwa> getMasterPwasWhereUserIsAuthorised(WebUserAccount requestingWebUserAccount,
                                                           PwaOrganisationRole requiredOrganisationRole) {
    // 1. get all operator teams where the user has the desired role
    var organisationTeams = teamService.getOrganisationTeamListIfPersonInRole(
        requestingWebUserAccount.getLinkedPerson(), Set.of(requiredOrganisationRole));

    // 2. get all organisationUnits scoped to those teams
    var potentialHolderOrganisationUnits = portalOrganisationsAccessor.getOrganisationUnitsForOrganisationGroupsIn(
        organisationTeams.stream()
            .map(PwaOrganisationTeam::getPortalOrganisationGroup)
            .collect(Collectors.toList())
    );

    // 3. return the consent's master pwa when one of the user's holder org units matches the consent's holder
    return pwaConsentOrganisationRoleService.getPwaConsentsWhereCurrentHolderWasAdded(
        potentialHolderOrganisationUnits
    ).stream()
        .map(PwaConsent::getMasterPwa)
        .collect(Collectors.toSet());


  }


}
