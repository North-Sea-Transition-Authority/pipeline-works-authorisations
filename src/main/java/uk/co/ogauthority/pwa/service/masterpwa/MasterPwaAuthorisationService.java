package uk.co.ogauthority.pwa.service.masterpwa;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.enums.MasterPwaDetailStatus;
import uk.co.ogauthority.pwa.model.entity.masterpwa.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwa.MasterPwaDetail;
import uk.co.ogauthority.pwa.repository.masterpwa.MasterPwaDetailRepository;
import uk.co.ogauthority.pwa.repository.masterpwa.MasterPwaRepository;

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
    MasterPwa masterPwa = masterPwaRepository.findById(masterPwaId)
        .orElseThrow(() -> new PwaEntityNotFoundException("Could not find master pwa with id:" + masterPwaId));

    // TODO actual authorisation
    return masterPwa;
  }


  /*
   * Skeleton implementation until we have the authorisation model done
   * */
  public List<MasterPwaDetail> getMasterPwasWhereUserIsAuthorised(WebUserAccount requestingWebUserAccount) {
    // TODO authorisation
    return masterPwaDetailRepository.findByEndInstantIsNullAndMasterPwaDetailStatus(MasterPwaDetailStatus.CONSENTED);

  }

  /*
   * Skeleton implementation until we have the authorisation model done
   * */
  public List<MasterPwaDto> getMasterPwaDtosWhereUserIsAuthorised(WebUserAccount requestingWebUserAccount) {
    return getMasterPwasWhereUserIsAuthorised(requestingWebUserAccount)
        .stream()
        .map(mpd -> new MasterPwaDto(mpd.getReference(), mpd.getMasterPwa().getId()))
        .collect(Collectors.toUnmodifiableList());
  }


}
