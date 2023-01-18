package uk.co.ogauthority.pwa.features.application.tasks.projectextension;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.util.FileUploadUtils;
import uk.co.ogauthority.pwa.util.validationgroups.MandatoryUploadValidation;

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
      FileUploadUtils.validateFiles(form, errors, List.of(MandatoryUploadValidation.class),
          "Upload at least one letter of permission");
    }
  }
}
