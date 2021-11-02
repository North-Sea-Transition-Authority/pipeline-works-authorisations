package uk.co.ogauthority.pwa.service.pickpwa;

import java.util.Comparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.service.masterpwas.ConsentedMasterPwaService;
import uk.co.ogauthority.pwa.service.masterpwas.NonConsentedPwaService;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;
import uk.co.ogauthority.pwa.util.StreamUtils;

@Service
public class PickedPwaRetrievalService {

  private final ConsentedMasterPwaService consentedMasterPwaService;
  private final NonConsentedPwaService nonConsentedPwaService;
  private final PwaHolderTeamService pwaHolderTeamService;

  @Autowired
  public PickedPwaRetrievalService(ConsentedMasterPwaService consentedMasterPwaService,
                                   NonConsentedPwaService nonConsentedPwaService,
                                   PwaHolderTeamService pwaHolderTeamService) {
    this.consentedMasterPwaService = consentedMasterPwaService;
    this.nonConsentedPwaService = nonConsentedPwaService;
    this.pwaHolderTeamService = pwaHolderTeamService;

  }

  public PickableMasterPwaOptions getPickablePwaOptions(WebUserAccount webUserAccount) {

    var potentialHolderOrganisationUnits =  pwaHolderTeamService.getPortalOrganisationUnitsWhereUserHasOrgRole(
        webUserAccount,
        PwaOrganisationRole.APPLICATION_CREATOR
    );

    var consentedMasterPwaMap = consentedMasterPwaService.getMasterPwaDetailsWhereAnyPortalOrgUnitsHolder(
        potentialHolderOrganisationUnits)
        .stream()
        .sorted(Comparator.comparing(MasterPwaDetail::getReference))
        .collect(
            StreamUtils.toLinkedHashMap(mpd -> String.valueOf(mpd.getMasterPwaId()), MasterPwaDetail::getReference));

    var nonConsentedMasterPwaMap = nonConsentedPwaService.getNonConsentedMasterPwaDetailByHolderOrgUnits(
        potentialHolderOrganisationUnits)
        .stream()
        .sorted(Comparator.comparing(MasterPwaDetail::getReference))
        .collect(
            StreamUtils.toLinkedHashMap(mpd -> String.valueOf(mpd.getMasterPwaId()), MasterPwaDetail::getReference));

    return new PickableMasterPwaOptions(consentedMasterPwaMap, nonConsentedMasterPwaMap);

  }

  public MasterPwa getPickedConsentedPwa(Integer pickedPwaId, WebUserAccount user) {

    var potentialHolderOrganisationUnits =  pwaHolderTeamService.getPortalOrganisationUnitsWhereUserHasOrgRole(
        user,
        PwaOrganisationRole.APPLICATION_CREATOR
    );

    return consentedMasterPwaService.getMasterPwaDetailsWhereAnyPortalOrgUnitsHolder(potentialHolderOrganisationUnits)
        .stream()
        .filter(masterPwaDetail -> masterPwaDetail.getMasterPwaId() == pickedPwaId)
        .map(MasterPwaDetail::getMasterPwa)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Could not find authorised consented master pwa with id: " + pickedPwaId));

  }

  public MasterPwa getPickedNonConsentedPwa(Integer pickedPwaId, WebUserAccount user) {

    var potentialHolderOrganisationUnits =  pwaHolderTeamService.getPortalOrganisationUnitsWhereUserHasOrgRole(
        user,
        PwaOrganisationRole.APPLICATION_CREATOR
    );

    return nonConsentedPwaService.getNonConsentedMasterPwaDetailByHolderOrgUnits(potentialHolderOrganisationUnits)
        .stream()
        .filter(masterPwaDetail -> masterPwaDetail.getMasterPwaId() == pickedPwaId)
        .map(MasterPwaDetail::getMasterPwa)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Could not find authorised non-consented master pwa with id: " + pickedPwaId));

  }

}
