package uk.co.ogauthority.pwa.service.pwaconsents;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentOrganisationRole;
import uk.co.ogauthority.pwa.repository.pwaconsents.PwaConsentOrganisationRoleRepository;
import uk.co.ogauthority.pwa.repository.pwaconsents.PwaConsentRepository;

@Service
public class PwaConsentOrganisationRolesService {

  private static final Logger LOGGER = LoggerFactory.getLogger(PwaConsentOrganisationRolesService.class);

  private final PwaConsentOrganisationRoleRepository pwaConsentOrganisationRoleRepository;
  private final PwaConsentRepository pwaConsentRepository;
  private final PortalOrganisationsAccessor portalOrganisationsAccessor;

  @Autowired
  public PwaConsentOrganisationRolesService(
      PwaConsentOrganisationRoleRepository pwaConsentOrganisationRoleRepository,
      PwaConsentRepository pwaConsentRepository,
      PortalOrganisationsAccessor portalOrganisationsAccessor) {
    this.pwaConsentOrganisationRoleRepository = pwaConsentOrganisationRoleRepository;
    this.pwaConsentRepository = pwaConsentRepository;
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
  }


  /**
   * This will return consents where one of the given organisation units was added a a Holder.
   * This is not necessarily the same as the latest consent of a PWA as the Holder is still the holder
   * if later consents add/replace other more holders.
   */
  public Set<PwaConsent> getPwaConsentsWhereCurrentHolderWasAdded(
      Collection<PortalOrganisationUnit> organisationUnits) {
    var orgUnitIds = organisationUnits.stream()
        .map(PortalOrganisationUnit::getOuId).collect(Collectors.toSet());
    return pwaConsentOrganisationRoleRepository.findByOrganisationUnitIdInAndRoleInAndEndTimestampIsNull(
        orgUnitIds,
        EnumSet.of(HuooRole.HOLDER))
        .stream()
        .map(PwaConsentOrganisationRole::getAddedByPwaConsent)
        .collect(Collectors.toSet());
  }


  public Set<MasterPwaHolderDto> getCurrentHoldersOrgRolesForMasterPwa(MasterPwa masterPwa) {
    var pwaConsents = pwaConsentRepository.findByMasterPwa(masterPwa);
    var activeHolders = pwaConsentOrganisationRoleRepository.findByAddedByPwaConsentInAndRoleInAndEndTimestampIsNull(
        pwaConsents,
        Set.of(HuooRole.HOLDER));

    var distinctHolderOrgUnitIds = activeHolders.stream()
        .map(PwaConsentOrganisationRole::getOrganisationUnitId)
        .collect(Collectors.toSet());

    var holderOrganisationUnitsLookup = portalOrganisationsAccessor.getOrganisationUnitsByIdIn(distinctHolderOrgUnitIds)
        .stream()
        .collect(Collectors.toMap(PortalOrganisationUnit::getOuId, ou -> ou));

    var masterPwaHolders = new HashSet<MasterPwaHolderDto>();
    for (PwaConsentOrganisationRole holderRole : activeHolders) {
      if (holderOrganisationUnitsLookup.containsKey(holderRole.getOrganisationUnitId())) {
        masterPwaHolders.add(new MasterPwaHolderDto(
            holderOrganisationUnitsLookup.get(holderRole.getOrganisationUnitId()),
            holderRole.getAddedByPwaConsent()
        ));
      } else {
        LOGGER.debug(
            String.format(
                "Could not reconcile all holder with current org unit. MasterPwaId: %s pwaConsentOrgRoleId: %s",
                masterPwa.getId(),
                holderRole.getId()
            )
        );
      }
    }

    return masterPwaHolders;

  }
}
