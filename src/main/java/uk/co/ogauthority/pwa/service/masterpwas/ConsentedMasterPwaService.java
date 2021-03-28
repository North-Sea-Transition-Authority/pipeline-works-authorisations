package uk.co.ogauthority.pwa.service.masterpwas;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.repository.masterpwas.MasterPwaDetailRepository;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentOrganisationRoleService;

/**
 * Get Master PWAs a given web user account has authorisation to access.
 */
@Service
public class ConsentedMasterPwaService {

  private final MasterPwaDetailRepository masterPwaDetailRepository;
  private final PwaConsentOrganisationRoleService pwaConsentOrganisationRoleService;


  @Autowired
  public ConsentedMasterPwaService(MasterPwaDetailRepository masterPwaDetailRepository,
                                   PwaConsentOrganisationRoleService pwaConsentOrganisationRoleService) {
    this.masterPwaDetailRepository = masterPwaDetailRepository;
    this.pwaConsentOrganisationRoleService = pwaConsentOrganisationRoleService;
  }

  private List<MasterPwaDetail> getCurrentMasterPwaDetails(Collection<MasterPwa> masterPwas) {
    return masterPwaDetailRepository.findByMasterPwaInAndEndInstantIsNull(masterPwas);
  }


  /*
   * Return all MasterPwa's where the user exists in the desired role with the PWA's holder team.
   */
  public List<MasterPwaDetail> getMasterPwaDetailsWhereAnyPortalOrgUnitsHolder(
      Collection<PortalOrganisationUnit> potentialHolderOrganisationUnits) {

    var masterPwas = pwaConsentOrganisationRoleService.getPwaConsentsWhereCurrentHolderWasAdded(
        potentialHolderOrganisationUnits
    ).stream()
        .map(PwaConsent::getMasterPwa)
        .collect(Collectors.toSet());

    return getCurrentMasterPwaDetails(masterPwas);


  }


}
