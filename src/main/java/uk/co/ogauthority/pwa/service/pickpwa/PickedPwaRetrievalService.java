package uk.co.ogauthority.pwa.service.pickpwa;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaAuthorisationService;

@Service
public class PickedPwaRetrievalService {

  private final MasterPwaAuthorisationService masterPwaAuthorisationService;

  @Autowired
  public PickedPwaRetrievalService(MasterPwaAuthorisationService masterPwaAuthorisationService) {
    this.masterPwaAuthorisationService = masterPwaAuthorisationService;
  }

  /**
   * A pick-able pwa is where the user has the application creator role within the PWA's holder team.
   */
  public List<PickablePwaDto> getPickablePwasWhereAuthorised(WebUserAccount webUserAccount) {
    Set<MasterPwa> authorisedAccessPwas = masterPwaAuthorisationService.getMasterPwasWhereUserIsAuthorised(
        webUserAccount,
        PwaOrganisationRole.APPLICATION_CREATOR);

    List<MasterPwaDetail> masterPwas = masterPwaAuthorisationService.getCurrentMasterPwaDetails(authorisedAccessPwas);

    List<PickablePwaDto> pickablePwaDtos = new ArrayList<>();
    for (MasterPwaDetail masterPwaDetail : masterPwas) {
      pickablePwaDtos.add(PickablePwaDto.from(masterPwaDetail));
    }

    return pickablePwaDtos;
  }

  @Transactional
  public MasterPwa getPickedPwa(PickablePwa pickedPwaForVariation, WebUserAccount user) {

    if (pickedPwaForVariation.getPickablePwaSource() == PickablePwaSource.MASTER) {
      return masterPwaAuthorisationService.getMasterPwaIfAuthorised(
          pickedPwaForVariation.getContentId(),
          user,
          PwaOrganisationRole.APPLICATION_CREATOR
      );
    }
    throw new IllegalStateException("Unexpected value: " + pickedPwaForVariation.toString());

  }

}
