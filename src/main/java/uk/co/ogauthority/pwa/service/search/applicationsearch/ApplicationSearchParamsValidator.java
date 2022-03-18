package uk.co.ogauthority.pwa.service.search.applicationsearch;

import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.INVALID;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import uk.co.ogauthority.pwa.service.enums.users.UserType;


@Service
class ApplicationSearchParamsValidator implements SmartValidator {

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(ApplicationSearchParameters.class);
  }

  @Override
  public void validate(Object target, Errors errors) {
    throw new UnsupportedOperationException("Cannot validate params without search context");
  }

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {
    var params = (ApplicationSearchParameters) target;
    var context = (ApplicationSearchContext) validationHints[0];

    if (params.getCaseOfficerPersonId() != null && !context.getUserTypes().contains(UserType.OGA)) {
      errors.rejectValue("caseOfficerPersonId",  INVALID.errorCode("caseOfficerPersonId"), "User must be an NSTA user");
    }

    validateHolderOrgUnitSelection(errors, params, context);

  }

  private void validateHolderOrgUnitSelection(Errors errors,
                                              ApplicationSearchParameters params,
                                              ApplicationSearchContext context) {
    // only need to do org validation if user is industry only and has selected a holder org
    if (!context.containsSingleUserTypeOf(UserType.INDUSTRY) || params.getHolderOrgUnitId() == null) {
      return;
    }

    var contextContainsSelectedPortalOrgUnit = context.getOrgUnitIdsAssociatedWithHolderTeamMembership()
        .stream()
        .anyMatch(organisationUnitId -> organisationUnitId.asInt() == params.getHolderOrgUnitId());

    if (!contextContainsSelectedPortalOrgUnit) {
      errors.rejectValue(
          "holderOrgUnitId",
          INVALID.errorCode("holderOrgUnitId"),
          "Must select an organisation within user's holder org group");
    }

  }
}
