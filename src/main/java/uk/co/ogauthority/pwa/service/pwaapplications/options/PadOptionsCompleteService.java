package uk.co.ogauthority.pwa.service.pwaapplications.options;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.enums.ConfirmedOptionType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.repository.pwaapplications.options.PadConfirmationOfOptionRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

/**
 * Simple Service which indicates whether an approved options application has been marked as completed.
 */
@Service
public class PadOptionsCompleteService {

  private final PadConfirmationOfOptionRepository padConfirmationOfOptionRepository;

  @Autowired
  public PadOptionsCompleteService(PadConfirmationOfOptionRepository padConfirmationOfOptionRepository) {
    this.padConfirmationOfOptionRepository = padConfirmationOfOptionRepository;
  }

  public boolean approvedOptionComplete(PwaApplicationDetail pwaApplicationDetail) {
    if (pwaApplicationDetail.getPwaApplicationType().equals(PwaApplicationType.OPTIONS_VARIATION)) {
      return padConfirmationOfOptionRepository.existsByPwaApplicationDetailAndConfirmedOptionType(
          pwaApplicationDetail,
          ConfirmedOptionType.WORK_COMPLETE_AS_PER_OPTIONS
      );
    }
    return false;
  }
}
