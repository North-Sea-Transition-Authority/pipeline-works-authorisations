package uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.techdetails.AdmiraltyChartDocumentForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;

@Service
public class TechnicalDrawingSectionService implements ApplicationFormSectionService {

  private final AdmiraltyChartFileService admiraltyChartFileService;
  private final PadTechnicalDrawingService padTechnicalDrawingService;
  private final UmbilicalCrossSectionService umbilicalCrossSectionService;
  private final PadFileService padFileService;

  @Autowired
  public TechnicalDrawingSectionService(
      AdmiraltyChartFileService admiraltyChartFileService,
      PadTechnicalDrawingService padTechnicalDrawingService,
      UmbilicalCrossSectionService umbilicalCrossSectionService,
      PadFileService padFileService) {
    this.admiraltyChartFileService = admiraltyChartFileService;
    this.padTechnicalDrawingService = padTechnicalDrawingService;
    this.umbilicalCrossSectionService = umbilicalCrossSectionService;
    this.padFileService = padFileService;
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    return admiraltyChartFileService.isComplete(detail)
        && padTechnicalDrawingService.isComplete(detail)
        && umbilicalCrossSectionService.isComplete(detail);
  }

  @Override
  public BindingResult validate(Object form, BindingResult bindingResult, ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    var admiraltyForm = new AdmiraltyChartDocumentForm();
    if (admiraltyChartFileService.canUploadDocuments(pwaApplicationDetail)) {
      padFileService.mapFilesToForm(admiraltyForm, pwaApplicationDetail, ApplicationFilePurpose.ADMIRALTY_CHART);
      admiraltyChartFileService.validate(admiraltyForm, bindingResult, validationType, pwaApplicationDetail);
    }
    padTechnicalDrawingService.validateSection(bindingResult, pwaApplicationDetail);
    return bindingResult;
  }
}
