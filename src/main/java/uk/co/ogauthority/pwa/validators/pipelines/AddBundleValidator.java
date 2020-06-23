package uk.co.ogauthority.pwa.validators.pipelines;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.BundleForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines.PadBundleRepository;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;

@Service
public class AddBundleValidator implements SmartValidator {

  private final PadBundleRepository padBundleRepository;

  @Autowired
  public AddBundleValidator(
      PadBundleRepository padBundleRepository) {
    this.padBundleRepository = padBundleRepository;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(BundleForm.class);
  }

  @Override
  @Deprecated
  public void validate(Object target, Errors errors) {
    throw new ActionNotAllowedException("Please use the other validate method");
  }

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {
    var form = (BundleForm) target;
    var detail = (PwaApplicationDetail) validationHints[0];
    ValidationUtils.rejectIfEmpty(errors, "bundleName",
        "bundleName" + FieldValidationErrorCodes.REQUIRED.getCode(),
        "Enter a name for the bundle");

    if (StringUtils.isNotBlank(form.getBundleName())) {
      var isNameUnique = padBundleRepository.getAllByPwaApplicationDetail(detail)
          .stream()
          .noneMatch(bundle -> bundle.getBundleName().equals(form.getBundleName()));
      if (!isNameUnique) {
        errors.rejectValue("bundleName", "bundleName" + FieldValidationErrorCodes.NOT_UNIQUE.getCode(),
            "The bundle name must be unique on this application");
      }
    }

    if (ListUtils.emptyIfNull(form.getPipelineIds()).size() < 2) {
      errors.rejectValue("pipelineIds", "pipelineIds" + FieldValidationErrorCodes.INVALID.getCode(),
          "At least two pipelines must be selected");
    }
  }
}
