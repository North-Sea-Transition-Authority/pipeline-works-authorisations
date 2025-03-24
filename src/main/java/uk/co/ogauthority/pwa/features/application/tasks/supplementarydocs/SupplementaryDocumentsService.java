package uk.co.ogauthority.pwa.features.application.tasks.supplementarydocs;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.FileValidationUtils;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;

@Service
public class SupplementaryDocumentsService implements ApplicationFormSectionService {

  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final PadFileManagementService padFileManagementService;

  private static final FileDocumentType DOCUMENT_TYPE = FileDocumentType.SUPPLEMENTARY_DOCUMENTS;

  @Autowired
  public SupplementaryDocumentsService(
      PwaApplicationDetailService pwaApplicationDetailService,
      PadFileManagementService padFileManagementService
  ) {
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.padFileManagementService = padFileManagementService;
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    var form = new SupplementaryDocumentsForm();
    mapSavedDataToForm(detail, form);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    return !validate(form, bindingResult, ValidationType.FULL, detail).hasErrors();
  }

  @Override
  public BindingResult validate(Object form,
                                BindingResult bindingResult,
                                ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    var castForm = (SupplementaryDocumentsForm) form;

    if (validationType.equals(ValidationType.PARTIAL)) {
      FileValidationUtils.validator()
          .validate(bindingResult, castForm.getUploadedFiles());
    }

    if (validationType.equals(ValidationType.FULL)) {
      ValidationUtils.rejectIfEmpty(bindingResult, "hasFilesToUpload",
          "hasFilesToUpload" + FieldValidationErrorCodes.REQUIRED.getCode(),
          "Select yes if you want to upload documents");

      if (BooleanUtils.isTrue(castForm.getHasFilesToUpload())) {
        FileValidationUtils.validator()
            .withMinimumNumberOfFiles(1, "Upload at least one file")
            .validate(bindingResult, castForm.getUploadedFiles());
      }
    }

    return bindingResult;
  }

  @Override
  public void copySectionInformation(PwaApplicationDetail fromDetail, PwaApplicationDetail toDetail) {
    padFileManagementService.copyUploadedFiles(fromDetail, toDetail, DOCUMENT_TYPE);
  }

  public void updateDocumentFlag(PwaApplicationDetail detail, SupplementaryDocumentsForm form) {
    pwaApplicationDetailService.setSupplementaryDocumentsFlag(detail, form.getHasFilesToUpload());
  }

  public void mapSavedDataToForm(PwaApplicationDetail detail, SupplementaryDocumentsForm form) {
    form.setHasFilesToUpload(detail.getSupplementaryDocumentsFlag());
    padFileManagementService.mapFilesToForm(form, detail, DOCUMENT_TYPE);
  }

}
