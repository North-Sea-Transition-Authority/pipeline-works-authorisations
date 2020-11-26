package uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.techdetails.AdmiraltyChartDocumentForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.service.pwaapplications.options.PadOptionConfirmedService;

@Service
public class TechnicalDrawingSectionService implements ApplicationFormSectionService {

  private static final Logger LOGGER = LoggerFactory.getLogger(TechnicalDrawingSectionService.class);

  private final AdmiraltyChartFileService admiraltyChartFileService;
  private final PadTechnicalDrawingService padTechnicalDrawingService;
  private final UmbilicalCrossSectionService umbilicalCrossSectionService;
  private final PadFileService padFileService;
  private final PadOptionConfirmedService padOptionConfirmedService;

  @Autowired
  public TechnicalDrawingSectionService(
      AdmiraltyChartFileService admiraltyChartFileService,
      PadTechnicalDrawingService padTechnicalDrawingService,
      UmbilicalCrossSectionService umbilicalCrossSectionService,
      PadFileService padFileService,
      PadOptionConfirmedService padOptionConfirmedService) {
    this.admiraltyChartFileService = admiraltyChartFileService;
    this.padTechnicalDrawingService = padTechnicalDrawingService;
    this.umbilicalCrossSectionService = umbilicalCrossSectionService;
    this.padFileService = padFileService;
    this.padOptionConfirmedService = padOptionConfirmedService;
  }

  @Override
  public boolean canShowInTaskList(PwaApplicationDetail pwaApplicationDetail) {
    // do not do additional type checks as this is covered by the controller markup
    return !PwaApplicationType.OPTIONS_VARIATION.equals(pwaApplicationDetail.getPwaApplicationType())
        || padOptionConfirmedService.approvedOptionConfirmed(pwaApplicationDetail);
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
      padFileService.mapFilesToForm(admiraltyForm, pwaApplicationDetail, ApplicationDetailFilePurpose.ADMIRALTY_CHART);
      admiraltyChartFileService.validate(admiraltyForm, bindingResult, validationType, pwaApplicationDetail);
    }
    padTechnicalDrawingService.validateSection(bindingResult, pwaApplicationDetail);
    return bindingResult;
  }

  @Override
  public void copySectionInformation(PwaApplicationDetail fromDetail, PwaApplicationDetail toDetail) {
    var copiedUmbilicalDiagramEntityIds = padFileService.copyPadFilesToPwaApplicationDetail(
        fromDetail,
        toDetail,
        ApplicationDetailFilePurpose.UMBILICAL_CROSS_SECTION,
        ApplicationFileLinkStatus.FULL
    );

    var copiedAdmiraltyChartEntityIds = padFileService.copyPadFilesToPwaApplicationDetail(
        fromDetail,
        toDetail,
        ApplicationDetailFilePurpose.ADMIRALTY_CHART,
        ApplicationFileLinkStatus.FULL
    );
  }

  @Override
  public void cleanupData(PwaApplicationDetail detail) {
    padTechnicalDrawingService.cleanupData(detail);
  }
}
