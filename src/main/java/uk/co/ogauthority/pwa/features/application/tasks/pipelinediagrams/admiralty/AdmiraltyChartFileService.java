package uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.admiralty;

import java.util.ArrayList;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.files.PadFileService;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.FileValidationUtils;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementService;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;

@Service
public class AdmiraltyChartFileService {

  private final PadFileService padFileService;
  private final PadFileManagementService padFileManagementService;
  private final SpringValidatorAdapter groupValidator;

  @Autowired
  public AdmiraltyChartFileService(
      PadFileService padFileService,
      PadFileManagementService padFileManagementService,
      SpringValidatorAdapter groupValidator
  ) {
    this.padFileService = padFileService;
    this.padFileManagementService = padFileManagementService;
    this.groupValidator = groupValidator;
  }

  public boolean isComplete(PwaApplicationDetail detail) {
    var form = new AdmiraltyChartDocumentForm();
    padFileManagementService.mapFilesToForm(form, detail, FileDocumentType.ADMIRALTY_CHART);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    return !validate(form, bindingResult, ValidationType.FULL, detail).hasErrors();
  }

  public boolean isUploadRequired(PwaApplicationDetail pwaApplicationDetail) {
    var appType = pwaApplicationDetail.getPwaApplicationType();
    return appType.equals(PwaApplicationType.INITIAL) || appType.equals(PwaApplicationType.CAT_1_VARIATION);
  }

  public BindingResult validate(Object form, BindingResult bindingResult, ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    var uploadFilesForm = (AdmiraltyChartDocumentForm) form;

    if (validationType.equals(ValidationType.FULL) && isUploadRequired(pwaApplicationDetail)) {
      FileValidationUtils.validator()
          .withMinimumNumberOfFiles(1, "Upload at least one file")
          .withMaximumNumberOfFiles(1, "Provide a single admiralty chart")
          .validate(bindingResult, uploadFilesForm.getUploadedFiles());
    } else {
      FileValidationUtils.validator()
          .withMaximumNumberOfFiles(1, "Provide a single admiralty chart")
          .validate(bindingResult, uploadFilesForm.getUploadedFiles());
    }
    groupValidator.validate(form, bindingResult, new ArrayList<>());

    return bindingResult;
  }

  public boolean canUploadDocuments(PwaApplicationDetail detail) {
    switch (detail.getPwaApplicationType()) {
      case INITIAL:
      case CAT_1_VARIATION:
        return true;
      default:
        return false;
    }
  }

  public Optional<UploadedFileView> getAdmiraltyChartFile(PwaApplicationDetail pwaApplicationDetail) {
    return padFileManagementService.getUploadedFileViews(pwaApplicationDetail, FileDocumentType.ADMIRALTY_CHART).stream()
        .findFirst();
  }

}
