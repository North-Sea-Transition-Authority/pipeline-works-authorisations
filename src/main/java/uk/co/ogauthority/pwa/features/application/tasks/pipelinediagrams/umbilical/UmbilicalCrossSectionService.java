package uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.umbilical;

import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.features.filemanagement.FileValidationUtils;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;

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
    var umbilicalCrossSectionForm = (UmbilicalCrossSectionForm) form;

    FileValidationUtils.validator()
        .withMaximumNumberOfFiles(1, "You may only upload a single umbilical cross-section diagram")
        .validate(bindingResult, umbilicalCrossSectionForm.getUploadedFiles());

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

}
