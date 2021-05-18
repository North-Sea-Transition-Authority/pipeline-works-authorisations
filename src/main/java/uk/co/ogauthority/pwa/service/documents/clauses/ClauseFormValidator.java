package uk.co.ogauthority.pwa.service.documents.clauses;

import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.model.entity.enums.mailmerge.MailMergeFieldType;
import uk.co.ogauthority.pwa.model.form.documents.ClauseForm;
import uk.co.ogauthority.pwa.service.documents.DocumentSource;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.mailmerge.MailMergeService;

@Service
public class ClauseFormValidator implements SmartValidator {

  private final MailMergeService mailMergeService;

  @Autowired
  public ClauseFormValidator(MailMergeService mailMergeService) {
    this.mailMergeService = mailMergeService;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(ClauseForm.class);
  }

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {

    var clauseForm = (ClauseForm) target;
    var docSource = (DocumentSource) validationHints[0];

    ValidationUtils.rejectIfEmpty(errors, "name", FieldValidationErrorCodes.REQUIRED.errorCode("name"), "Enter a clause name");

    Optional.ofNullable(clauseForm.getText())
        .ifPresent(text -> {

          Set<String> invalidMergeFields = mailMergeService.validateMailMergeFields(docSource, clauseForm.getText());

          if (!invalidMergeFields.isEmpty()) {
            errors.rejectValue("text", FieldValidationErrorCodes.INVALID.errorCode("text"),
                String.format("Remove invalid mail merge fields: %s", String.join(", ", invalidMergeFields)));
          }

          if (text.contains(MailMergeFieldType.MANUAL.getOpeningDelimiter())
              || text.contains(MailMergeFieldType.MANUAL.getClosingDelimiter())) {
            errors.rejectValue("text", FieldValidationErrorCodes.INVALID.errorCode("text"),
                String.format("Remove '%s' from the clause text", MailMergeFieldType.MANUAL.getOpeningDelimiter()));
          }

        });

  }

  @Override
  public void validate(Object target, Errors errors) {
    throw new NotImplementedException("Use other method.");
  }
}
