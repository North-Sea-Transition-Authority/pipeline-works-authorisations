package uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.techdetails.UmbilicalCrossSectionForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.util.validationgroups.FullValidation;
import uk.co.ogauthority.pwa.util.validationgroups.PartialValidation;

@Service
public class UmbilicalCrossSectionService {

  private final SpringValidatorAdapter groupValidator;

  @Autowired
  public UmbilicalCrossSectionService(
      SpringValidatorAdapter groupValidator) {
    this.groupValidator = groupValidator;
  }

  public boolean isComplete(PwaApplicationDetail detail) {
    // This intentionally always returns true.
    // Guidance informs users when the document is required, but this is not validated.
    return true;
  }

  public BindingResult validate(Object form, BindingResult bindingResult, ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    List<Object> hints = new ArrayList<>();
    if (validationType.equals(ValidationType.FULL)) {
      hints.add(FullValidation.class);
    } else {
      hints.add(PartialValidation.class);
    }
    groupValidator.validate(form, bindingResult, hints.toArray());

    if (((UmbilicalCrossSectionForm) form).getUploadedFileWithDescriptionForms().size() > 1) {
      bindingResult.rejectValue("uploadedFileWithDescriptionForms",
          "uploadedFileWithDescriptionForms" + FieldValidationErrorCodes.EXCEEDED_MAXIMUM_FILE_UPLOAD_COUNT.getCode(),
          "You may only upload a single umbilical cross-section diagram");
    }

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

}
