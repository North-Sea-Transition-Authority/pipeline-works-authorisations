package uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.AdmiralityChartDocumentForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;

@Service
public class TechnicalDrawingsService implements ApplicationFormSectionService {

  private final AdmiralityChartFileService admiralityChartFileService;

  @Autowired
  public TechnicalDrawingsService(
      AdmiralityChartFileService admiralityChartFileService) {
    this.admiralityChartFileService = admiralityChartFileService;
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    return admiralityChartFileService.isUploadRequired(detail) && admiralityChartFileService.isComplete(detail);
  }

  @Override
  public BindingResult validate(Object form, BindingResult bindingResult, ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    var admiralityForm = new AdmiralityChartDocumentForm();
    admiralityChartFileService.mapDocumentsToForm(pwaApplicationDetail, admiralityForm);
    admiralityChartFileService.validate(admiralityForm, bindingResult, validationType, pwaApplicationDetail);
    return bindingResult;
  }
}
