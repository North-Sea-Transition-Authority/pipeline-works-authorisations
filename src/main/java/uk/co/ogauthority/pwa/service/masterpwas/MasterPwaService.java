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
public class MasterPwaService {

  private final MasterPwaRepository masterPwaRepository;

  private final MasterPwaDetailRepository masterPwaDetailRepository;

  private final Clock clock;

  @Autowired
  public MasterPwaService(MasterPwaRepository masterPwaRepository,
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
    var masterPwaDetail = new MasterPwaDetail(masterPwa, masterPwaDetailStatus, reference, creationInstant);

    masterPwaRepository.save(masterPwa);
    masterPwaDetailRepository.save(masterPwaDetail);

    return masterPwaDetail;

  }

  public MasterPwa getMasterPwaById(Integer pwaId) {
    return masterPwaRepository.findById(pwaId)
        .orElseThrow(() -> new PwaEntityNotFoundException("Could not find master PWA with id " + pwaId));
  }

  public MasterPwaDetail getCurrentDetailOrThrow(MasterPwa masterPwa) {
    return masterPwaDetailRepository.findByMasterPwaAndEndInstantIsNull(masterPwa)
        .orElseThrow(() -> new PwaEntityNotFoundException(
            String.format("Couldn't find current detail for master pwa with id: %s", masterPwa.getId())));
  }

  @Transactional
  public void updateDetail(MasterPwaDetail detail,
                           MasterPwaDetailStatus status,
                           Boolean linkedToFields,
                           String pwaLinkedToDescription) {

    detail.setMasterPwaDetailStatus(status);
    detail.setLinkedToFields(linkedToFields);
    detail.setPwaLinkedToDescription(pwaLinkedToDescription);
    masterPwaDetailRepository.save(detail);

  }

  @Transactional
  public MasterPwaDetail createNewDetail(MasterPwa masterPwa,
                                         Boolean linkedToFields,
                                         String pwaLinkedToDescription) {

    var currentDetail = getCurrentDetailOrThrow(masterPwa);
    currentDetail.setEndInstant(clock.instant());
    masterPwaDetailRepository.save(currentDetail);

    var newDetail = new MasterPwaDetail(masterPwa, MasterPwaDetailStatus.CONSENTED, currentDetail.getReference(), clock.instant());

    newDetail.setLinkedToFields(linkedToFields);
    newDetail.setPwaLinkedToDescription(pwaLinkedToDescription);

    masterPwaDetailRepository.save(newDetail);

    return newDetail;

  }

}
