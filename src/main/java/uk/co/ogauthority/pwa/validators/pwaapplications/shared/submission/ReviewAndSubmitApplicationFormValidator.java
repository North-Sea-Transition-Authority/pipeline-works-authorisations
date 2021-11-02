package uk.co.ogauthority.pwa.validators.pwaapplications.shared.submission;

import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.REQUIRED;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.submission.ReviewAndSubmitApplicationForm;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.service.appprocessing.applicationupdate.ApplicationUpdateRequestService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;
import uk.co.ogauthority.pwa.util.ValidatorUtils;

@Service
public class ReviewAndSubmitApplicationFormValidator implements SmartValidator {

  private final PwaHolderTeamService pwaHolderTeamService;
  private final ApplicationUpdateRequestService applicationUpdateRequestService;

  @Autowired
  public ReviewAndSubmitApplicationFormValidator(PwaHolderTeamService pwaHolderTeamService,
                                                 ApplicationUpdateRequestService applicationUpdateRequestService) {
    this.pwaHolderTeamService = pwaHolderTeamService;
    this.applicationUpdateRequestService = applicationUpdateRequestService;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return ReviewAndSubmitApplicationForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {

    var form = (ReviewAndSubmitApplicationForm) target;
    var appContext = (PwaApplicationContext) validationHints[0];

    var userAppPermissions =  appContext.getPermissions();
    boolean openUpdateRequest = applicationUpdateRequestService.applicationHasOpenUpdateRequest(appContext.getApplicationDetail());

    // only preparers can edit update response or select a submitter
    if (userAppPermissions.contains(PwaApplicationPermission.EDIT)) {

      if (openUpdateRequest) {

        ValidationUtils.rejectIfEmpty(
            errors,
            "madeOnlyRequestedChanges",
            REQUIRED.errorCode("madeOnlyRequestedChanges"),
            "Select the option which describes your update"
        );

        if (BooleanUtils.isFalse(form.getMadeOnlyRequestedChanges())) {

          ValidationUtils.rejectIfEmpty(
              errors,
              "otherChangesDescription",
              REQUIRED.errorCode("otherChangesDescription"),
              "Enter a description of the changes"
          );

          ValidatorUtils.validateDefaultStringLength(
              errors, "otherChangesDescription", form::getOtherChangesDescription, "Description of changes");

        }

      }

      // if you can't submit yourself, need to have selected someone else to do it
      if (!userAppPermissions.contains(PwaApplicationPermission.SUBMIT)) {

        ValidationUtils.rejectIfEmpty(
            errors,
            "submitterPersonId",
            FieldValidationErrorCodes.REQUIRED.errorCode("submitterPersonId"),
            "Select a person to submit the application");

        if (form.getSubmitterPersonId() != null) {

          // verify that person matches a suitable submitter candidate
          boolean validSubmitterPerson = pwaHolderTeamService
              .getPeopleWithHolderTeamRole(appContext.getApplicationDetail(), PwaOrganisationRole.APPLICATION_SUBMITTER)
              .stream()
              .anyMatch(submitter -> submitter.getId().asInt() == form.getSubmitterPersonId());

          if (!validSubmitterPerson) {
            errors.rejectValue("submitterPersonId", FieldValidationErrorCodes.INVALID.errorCode("submitterPersonId"),
                "Select a valid submitter, this person is not allowed to submit this application");
          }

        }

      }

    }


  }

  @Override
  public void validate(Object target, Errors errors) {
    throw new NotImplementedException("Use other validate method.");
  }

}
