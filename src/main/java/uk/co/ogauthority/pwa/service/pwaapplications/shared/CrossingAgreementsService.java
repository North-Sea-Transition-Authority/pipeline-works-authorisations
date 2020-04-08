package uk.co.ogauthority.pwa.service.pwaapplications.shared;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;

@Service
public class CrossingAgreementsService implements ApplicationFormSectionService {

  private final PadMedianLineAgreementService padMedianLineAgreementService;

  @Autowired
  public CrossingAgreementsService(PadMedianLineAgreementService padMedianLineAgreementService) {
    this.padMedianLineAgreementService = padMedianLineAgreementService;
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {

    // Add other sections validation results
    return padMedianLineAgreementService.isComplete(detail);
  }

  @Override
  public BindingResult validate(Object form, BindingResult bindingResult, ValidationType validationType) {
    return bindingResult;
  }
}
