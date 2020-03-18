package uk.co.ogauthority.pwa.service.masterpwas;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.enums.MasterPwaDetailStatus;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.repository.masterpwas.MasterPwaDetailRepository;
import uk.co.ogauthority.pwa.repository.masterpwas.MasterPwaRepository;

/**
 * Get Master PWAs a given web user account has authorisation to access.
 */
@Service
public class MasterPwaAuthorisationService {

  private final MasterPwaRepository masterPwaRepository;
  private final MasterPwaDetailRepository masterPwaDetailRepository;

  @Autowired
  public MasterPwaAuthorisationService(
      MasterPwaRepository masterPwaRepository,
      MasterPwaDetailRepository masterPwaDetailRepository) {
    this.masterPwaRepository = masterPwaRepository;
    this.masterPwaDetailRepository = masterPwaDetailRepository;
  }

  /*
   * Skeleton implementation until we have the authorisation model done
   * */
  public MasterPwa getMasterPwaIfAuthorised(int masterPwaId, WebUserAccount requestingWebUserAccount) {
    // TODO actual authorisation
    MasterPwa masterPwa = masterPwaRepository.findById(masterPwaId)
        .orElseThrow(() -> new PwaEntityNotFoundException("Could not find master pwa with id:" + masterPwaId));
    return masterPwa;
  }


  /*
   * Skeleton implementation until we have the authorisation model done
   * */
  public List<MasterPwaDetail> getMasterPwasWhereUserIsAuthorised(WebUserAccount requestingWebUserAccount) {
    // TODO authorisation
    return masterPwaDetailRepository.findByEndInstantIsNullAndMasterPwaDetailStatus(MasterPwaDetailStatus.CONSENTED);

  }



}
