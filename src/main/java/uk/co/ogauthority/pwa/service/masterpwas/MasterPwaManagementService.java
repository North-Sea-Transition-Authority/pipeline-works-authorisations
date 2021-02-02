package uk.co.ogauthority.pwa.service.masterpwas;

import java.time.Clock;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.enums.MasterPwaDetailStatus;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.repository.masterpwas.MasterPwaDetailRepository;
import uk.co.ogauthority.pwa.repository.masterpwas.MasterPwaRepository;

@Service
public class MasterPwaManagementService {

  private final MasterPwaRepository masterPwaRepository;

  private final MasterPwaDetailRepository masterPwaDetailRepository;

  private final Clock clock;

  @Autowired
  public MasterPwaManagementService(MasterPwaRepository masterPwaRepository,
                                    MasterPwaDetailRepository masterPwaDetailRepository,
                                    @Qualifier("utcClock") Clock clock) {
    this.masterPwaRepository = masterPwaRepository;
    this.masterPwaDetailRepository = masterPwaDetailRepository;
    this.clock = clock;
  }


  @Transactional
  public MasterPwaDetail createMasterPwa(MasterPwaDetailStatus masterPwaDetailStatus, String reference) {
    // Changing this code? have you changed the migration script?
    var creationInstant = clock.instant();
    var masterPwa = new MasterPwa(creationInstant);
    var masterPwaDetail = new MasterPwaDetail(creationInstant);
    masterPwaDetail.setMasterPwa(masterPwa);
    masterPwaDetail.setReference(reference);
    masterPwaDetail.setMasterPwaDetailStatus(masterPwaDetailStatus);

    masterPwaRepository.save(masterPwa);
    masterPwaDetailRepository.save(masterPwaDetail);

    return masterPwaDetail;

  }

  public MasterPwa getMasterPwaById(Integer pwaId) {
    return masterPwaRepository.findById(pwaId)
        .orElseThrow(() -> new PwaEntityNotFoundException("Could not find master PWA with id " + pwaId));
  }

}
