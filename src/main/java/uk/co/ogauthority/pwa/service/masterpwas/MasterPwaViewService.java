package uk.co.ogauthority.pwa.service.masterpwas;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.repository.masterpwas.MasterPwaDetailRepository;

@Service
public class MasterPwaViewService {

  private final MasterPwaDetailRepository masterPwaDetailRepository;

  @Autowired
  public MasterPwaViewService(
      MasterPwaDetailRepository masterPwaDetailRepository) {
    this.masterPwaDetailRepository = masterPwaDetailRepository;
  }

  public MasterPwaView getCurrentMasterPwaView(PwaApplication pwaApplication) {
    var currentDetail = getCurrentMasterPwaDetail(pwaApplication.getMasterPwa());
    return MasterPwaView.from(currentDetail);

  }

  private MasterPwaDetail getCurrentMasterPwaDetail(MasterPwa masterPwa) {
    return masterPwaDetailRepository.findByMasterPwaAndEndInstantIsNull(masterPwa)
        .orElseThrow(() -> new PwaEntityNotFoundException(
            "could not find current master pwa detail. masterPwaId: " + masterPwa.getId())
        );
  }

}
