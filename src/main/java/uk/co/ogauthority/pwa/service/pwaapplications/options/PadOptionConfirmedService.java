package uk.co.ogauthority.pwa.service.pwaapplications.options;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.enums.ConfirmedOptionType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.repository.pwaapplications.options.PadConfirmationOfOptionRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

/**
 * Simple service to indicate if an options variation application has one of its approved options confirmed.
 */
@Service
public class PadOptionConfirmedService {

  private final PadConfirmationOfOptionRepository padConfirmationOfOptionRepository;

  @Autowired
  public PadOptionConfirmedService(PadConfirmationOfOptionRepository padConfirmationOfOptionRepository) {
    this.padConfirmationOfOptionRepository = padConfirmationOfOptionRepository;
  }

  public boolean approvedOptionConfirmed(PwaApplicationDetail pwaApplicationDetail) {
    if (pwaApplicationDetail.getPwaApplicationType().equals(PwaApplicationType.OPTIONS_VARIATION)) {
      return padConfirmationOfOptionRepository.existsByPwaApplicationDetailAndConfirmedOptionType(
          pwaApplicationDetail,
          ConfirmedOptionType.WORK_COMPLETE_AS_PER_OPTIONS
      );
    }
    return false;
  }

  public boolean optionConfirmationExists(PwaApplicationDetail pwaApplicationDetail) {
    if (pwaApplicationDetail.getPwaApplicationType().equals(PwaApplicationType.OPTIONS_VARIATION)) {
      return padConfirmationOfOptionRepository.findByPwaApplicationDetail(pwaApplicationDetail)
          .isPresent();
    }
    return false;
  }
}
