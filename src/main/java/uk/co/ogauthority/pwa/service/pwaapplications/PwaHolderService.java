package uk.co.ogauthority.pwa.service.pwaapplications;

import static java.util.stream.Collectors.toList;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.search.consents.PwaHolderOrgUnit;
import uk.co.ogauthority.pwa.repository.search.consents.PwaHolderOrgUnitRepository;

@Service
public class PwaHolderService {

  private final PortalOrganisationsAccessor portalOrganisationsAccessor;
  private final PwaHolderOrgUnitRepository pwaHolderOrgUnitRepository;

  @Autowired
  public PwaHolderService(
      PortalOrganisationsAccessor portalOrganisationsAccessor,
      PwaHolderOrgUnitRepository pwaHolderOrgUnitRepository) {
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
    this.pwaHolderOrgUnitRepository = pwaHolderOrgUnitRepository;
  }

  public Set<PortalOrganisationUnit> getPwaHolderOrgUnits(MasterPwa masterPwa) {

    var holderOuIds = pwaHolderOrgUnitRepository.findAllByPwaId(masterPwa.getId()).stream()
        .map(PwaHolderOrgUnit::getOuId)
        .collect(Collectors.toList());

    return portalOrganisationsAccessor.getOrganisationUnitsByIdIn(holderOuIds).stream()
        .collect(Collectors.toUnmodifiableSet());

  }

  public Set<PortalOrganisationGroup> getPwaHolderOrgGroups(MasterPwa masterPwa) {
    // the base view we are querying handles the logic of consented model lookup or application data lookup.
    // Uses the same logic as the app search so we are consistent across contexts.
    var holderOrgGrpIdsForMasterPwa = pwaHolderOrgUnitRepository.findAllByPwaId(masterPwa.getId())
        .stream()
        .map(PwaHolderOrgUnit::getOrgGrpId)
        .distinct()
        .toList();

    return portalOrganisationsAccessor.getOrganisationGroupsWhereIdIn(holderOrgGrpIdsForMasterPwa)
        .stream()
        .collect(Collectors.toUnmodifiableSet());
  }

  public Multimap<PortalOrganisationGroup, Integer> getHolderOrgGroupsForMasterPwaIds(Set<Integer> masterPwaIds) {
    var allHolderOrgUnitsForMasterPwas = pwaHolderOrgUnitRepository.findAllByPwaIdIn(masterPwaIds);
    var holderOrgGrpIds = allHolderOrgUnitsForMasterPwas.stream()
        .map(PwaHolderOrgUnit::getOrgGrpId)
        .distinct()
        .collect(toList());
    var orgGroups = portalOrganisationsAccessor.getOrganisationGroupsWhereIdIn(holderOrgGrpIds)
        .stream()
        .collect(Collectors.toUnmodifiableSet());

    Multimap<PortalOrganisationGroup, Integer> holderOrgGroupToMasterPwaIdListMap = ArrayListMultimap.create();
    allHolderOrgUnitsForMasterPwas.forEach(pwaHolderOrgUnit -> holderOrgGroupToMasterPwaIdListMap.put(
        orgGroups.stream()
            .filter(orgGroup -> orgGroup.getOrgGrpId().equals(pwaHolderOrgUnit.getOrgGrpId()))
            .findFirst().get(),
        pwaHolderOrgUnit.getPwaId()));
    return holderOrgGroupToMasterPwaIdListMap;
  }

}
