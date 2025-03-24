package uk.co.ogauthority.pwa.features.application.tasks.optionstemplate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.FileValidationUtils;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;

@Service
public class OptionsTemplateService implements ApplicationFormSectionService {

  private static final FileDocumentType DOCUMENT_TYPE = FileDocumentType.OPTIONS_TEMPLATE;

  private final PadFileManagementService padFileManagementService;

  @Autowired
  public OptionsTemplateService(PadFileManagementService padFileManagementService) {
    this.padFileManagementService = padFileManagementService;
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    var form = new OptionsTemplateForm();
    padFileManagementService.mapFilesToForm(form, detail, DOCUMENT_TYPE);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    return !validate(form, bindingResult, ValidationType.FULL, detail).hasErrors();
  }

  @Override
  public BindingResult validate(Object form,
                                BindingResult bindingResult,
                                ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {

    var castedForm = (OptionsTemplateForm) form;

    if (validationType.equals(ValidationType.FULL)) {
      FileValidationUtils.validator()
          .withMinimumNumberOfFiles(1, "Upload at least one file")
          .withMaximumNumberOfFiles(1, "Provide a single template upload")
          .validate(bindingResult, castedForm.getUploadedFiles());
    } else {
      FileValidationUtils.validator()
          .withMaximumNumberOfFiles(1, "Provide a single template upload")
          .validate(bindingResult, castedForm.getUploadedFiles());
    }

    return bindingResult;
  }

  @Override
  public void copySectionInformation(PwaApplicationDetail fromDetail, PwaApplicationDetail toDetail) {
    padFileManagementService.copyUploadedFiles(fromDetail, toDetail, DOCUMENT_TYPE);
  }

}
