package uk.co.ogauthority.pwa.validators.pipelines;

import java.util.List;
import org.apache.commons.collections4.SetUtils;
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
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;

@Service
public class AddBundleValidator implements SmartValidator {

  private final PadBundleRepository padBundleRepository;
  private final PadPipelineService padPipelineService;

  @Autowired
  public AddBundleValidator(
      PadBundleRepository padBundleRepository,
      PadPipelineService padPipelineService) {
    this.padBundleRepository = padBundleRepository;
    this.padPipelineService = padPipelineService;
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

    if (SetUtils.emptyIfNull(form.getPadPipelineIds()).size() < 2) {
      errors.rejectValue("padPipelineIds", "padPipelineIds" + FieldValidationErrorCodes.INVALID.getCode(),
          "At least two pipelines must be selected");
    } else {
      Long validPipelineCount = padPipelineService.getCountOfPipelinesByIdList(detail,
          List.copyOf(form.getPadPipelineIds()));
      if (!validPipelineCount.equals((long) form.getPadPipelineIds().size())) {
        errors.rejectValue("padPipelineIds", "padPipelineIds" + FieldValidationErrorCodes.INVALID.getCode(),
            "Not all selected pipelines exist on the application");
      }
    }

  }
}
