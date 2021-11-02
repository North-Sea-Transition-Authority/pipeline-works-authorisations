package uk.co.ogauthority.pwa.features.application.tasks.optionconfirmation;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.enums.ConfirmedOptionType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
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

  public Optional<ConfirmedOptionType> getConfirmedOptionType(PwaApplicationDetail pwaApplicationDetail) {
    return pwaApplicationDetail.getPwaApplicationType() == PwaApplicationType.OPTIONS_VARIATION
        ? padConfirmationOfOptionRepository.findByPwaApplicationDetail(pwaApplicationDetail)
            .map(PadConfirmationOfOption::getConfirmedOptionType)
        : Optional.empty();
  }

}
