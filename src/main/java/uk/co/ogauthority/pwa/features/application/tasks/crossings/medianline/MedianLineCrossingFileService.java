package uk.co.ogauthority.pwa.features.application.tasks.crossings.medianline;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.formhelpers.CrossingDocumentsForm;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.FileValidationUtils;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;

@Service
public class MedianLineCrossingFileService implements ApplicationFormSectionService {

  private final PadMedianLineAgreementRepository padMedianLineAgreementRepository;
  private final PadFileManagementService padFileManagementService;

  @Autowired
  public MedianLineCrossingFileService(PadMedianLineAgreementRepository padMedianLineAgreementRepository,
                                       PadFileManagementService padFileManagementService) {
    this.padMedianLineAgreementRepository = padMedianLineAgreementRepository;
    this.padFileManagementService = padFileManagementService;
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    var form = new CrossingDocumentsForm();
    padFileManagementService.mapFilesToForm(form, detail, FileDocumentType.MEDIAN_LINE_CROSSING);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    return !validate(form, bindingResult, ValidationType.FULL, detail).hasErrors();
  }

  public boolean requiresMandatoryValidation(PwaApplicationDetail pwaApplicationDetail) {
    var optionalAgreement = padMedianLineAgreementRepository.findByPwaApplicationDetail(pwaApplicationDetail);
    if (optionalAgreement.isEmpty()) {
      return false;
    } else {
      return optionalAgreement.get().getAgreementStatus() != MedianLineStatus.NOT_CROSSED;
    }
  }

  @Override
  public BindingResult validate(Object form, BindingResult bindingResult, ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    var castedForm = (CrossingDocumentsForm) form;

    if (validationType.equals(ValidationType.FULL) && requiresMandatoryValidation(pwaApplicationDetail)) {
      FileValidationUtils.validator()
          .withMinimumNumberOfFiles(1, "Upload at least one file")
          .validate(bindingResult, castedForm.getUploadedFiles());
    } else {
      FileValidationUtils.validator()
          .validate(bindingResult, castedForm.getUploadedFiles());
    }

    return bindingResult;

  }

  @Override
  public void copySectionInformation(PwaApplicationDetail fromDetail, PwaApplicationDetail toDetail) {
    // files copied in PadMedianLineAgreementService
  }
}
