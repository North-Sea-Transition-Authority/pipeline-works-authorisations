package uk.co.ogauthority.pwa.validators.appprocessing.casenote;


import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.co.ogauthority.pwa.features.filemanagement.FileValidationUtils;
import uk.co.ogauthority.pwa.model.form.appprocessing.casenotes.AddCaseNoteForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;

@Service
public class CaseNoteFormValidator implements Validator {

  @Override
  public boolean supports(Class<?> clazz) {
    return AddCaseNoteForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {

    var form = (AddCaseNoteForm) target;

    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors,
        "noteText",
        FieldValidationErrorCodes.REQUIRED.errorCode("noteText"),
        "Enter some note text"
    );

    FileValidationUtils.validator().validate(errors, form.getUploadedFiles());
  }

}
