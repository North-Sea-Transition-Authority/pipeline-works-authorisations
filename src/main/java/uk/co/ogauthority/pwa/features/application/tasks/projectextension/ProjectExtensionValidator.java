package uk.co.ogauthority.pwa.features.application.tasks.projectextension;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import uk.co.ogauthority.pwa.features.filemanagement.FileValidationUtils;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;

@Service
public class ProjectExtensionValidator implements SmartValidator {
  @Override
  public boolean supports(Class<?> clazz) {
    return false;
  }

  @Override
  public void validate(Object target, Errors errors) {
    validate(target, errors, ValidationType.FULL);
  }

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {
    var validationType = (ValidationType) validationHints[0];
    var form = (ProjectExtensionForm) target;

    if (validationType.equals(ValidationType.FULL)) {
      FileValidationUtils.validator()
          .withMinimumNumberOfFiles(1, "Upload at least one letter of permission")
          .validate(errors, form.getUploadedFiles());
    }
  }
}
