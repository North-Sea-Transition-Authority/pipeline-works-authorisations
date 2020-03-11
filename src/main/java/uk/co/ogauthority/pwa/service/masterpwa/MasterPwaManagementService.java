package uk.co.ogauthority.pwa.service.masterpwa;

import java.time.Clock;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.enums.MasterPwaDetailStatus;
import uk.co.ogauthority.pwa.model.entity.masterpwa.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwa.MasterPwaDetail;
import uk.co.ogauthority.pwa.repository.masterpwa.MasterPwaDetailRepository;
import uk.co.ogauthority.pwa.repository.masterpwa.MasterPwaRepository;

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

    var masterPwa = new MasterPwa(clock.instant());
    var masterPwaDetail = new MasterPwaDetail(clock.instant());
    masterPwaDetail.setMasterPwa(masterPwa);
    masterPwaDetail.setReference(reference);
    masterPwaDetail.setMasterPwaDetailStatus(masterPwaDetailStatus);


    masterPwaRepository.save(masterPwa);
    masterPwaDetailRepository.save(masterPwaDetail);
    return masterPwaDetail;
  }

}
