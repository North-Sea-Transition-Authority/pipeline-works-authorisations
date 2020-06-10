package uk.co.ogauthority.pwa.service.pickpwa;

import java.util.ArrayList;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaAuthorisationService;

@Service
public class PickedPwaRetrievalService {

  private final MasterPwaAuthorisationService masterPwaAuthorisationService;

  @Autowired
  public PickedPwaRetrievalService(MasterPwaAuthorisationService masterPwaAuthorisationService) {
    this.masterPwaAuthorisationService = masterPwaAuthorisationService;
  }


  public List<PickablePwaDto> getPickablePwasWhereAuthorised(WebUserAccount webUserAccount) {
    List<MasterPwaDetail> masterPwas = masterPwaAuthorisationService.getMasterPwasWhereUserIsAuthorised(webUserAccount);

    List<PickablePwaDto> pickablePwaDtos = new ArrayList<>();
    for (MasterPwaDetail masterPwaDetail : masterPwas) {
      pickablePwaDtos.add(PickablePwaDto.from(masterPwaDetail));
    }

    return pickablePwaDtos;
  }

  @Transactional
  public MasterPwa getPickedPwa(PickablePwa pickedPwaForVariation, WebUserAccount user) {

    switch (pickedPwaForVariation.getPickablePwaSource()) {
      case MASTER:
        return masterPwaAuthorisationService.getMasterPwaIfAuthorised(
            pickedPwaForVariation.getContentId(),
            user
        );
      default:
        throw new IllegalStateException("Unexpected value: " + pickedPwaForVariation.toString());
    }

  }

}
