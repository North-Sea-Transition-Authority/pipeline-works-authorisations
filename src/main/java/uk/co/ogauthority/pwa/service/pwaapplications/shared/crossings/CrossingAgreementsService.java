package uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings;

import java.util.EnumSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;

@Service
public class CrossingAgreementsService implements ApplicationFormSectionService {

  private final PadMedianLineAgreementService padMedianLineAgreementService;
  private final BlockCrossingFileService blockCrossingFileService;

  @Autowired
  public CrossingAgreementsService(PadMedianLineAgreementService padMedianLineAgreementService,
                                   BlockCrossingFileService blockCrossingFileService) {
    this.padMedianLineAgreementService = padMedianLineAgreementService;
    this.blockCrossingFileService = blockCrossingFileService;
  }

  public CrossingAgreementsValidationResult getValidationResult(PwaApplicationDetail detail) {
    var validSections = EnumSet.noneOf(CrossingAgreementsSection.class);

    if (padMedianLineAgreementService.isComplete(detail)) {
      validSections.add(CrossingAgreementsSection.MEDIAN_LINE);
    }

    if (blockCrossingFileService.isComplete(detail)) {
      validSections.add(CrossingAgreementsSection.BLOCK_CROSSINGS);
    }

    return new CrossingAgreementsValidationResult(validSections);

  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    return getValidationResult(detail).isCrossingAgreementsValid();
  }

  @Override
  public BindingResult validate(Object form,
                                BindingResult bindingResult,
                                ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    throw new AssertionError("validate doesnt make sense.");
  }
}
