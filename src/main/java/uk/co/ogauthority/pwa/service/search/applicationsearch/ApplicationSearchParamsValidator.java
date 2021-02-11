package uk.co.ogauthority.pwa.service.search.applicationsearch;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;


@Service
class ApplicationSearchParamsValidator implements SmartValidator {

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(ApplicationSearchParameters.class);
  }

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {
    var params = (ApplicationSearchParameters) target;
    var context = (ApplicationSearchContext) validationHints[0];

    if (params.getCaseOfficerId() != null && !context.getUserType().equals(UserType.OGA)) {
      errors.rejectValue("caseOfficerId", "caseOfficerId" + FieldValidationErrorCodes.INVALID.getCode(), "User must be an OGA user");
    }


  }

  @Override
  public void validate(Object target, Errors errors) {
    throw new UnsupportedOperationException("Cannot validate params without search context");
  }
}
