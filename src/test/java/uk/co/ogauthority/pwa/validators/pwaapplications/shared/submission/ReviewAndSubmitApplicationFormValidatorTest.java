package uk.co.ogauthority.pwa.validators.pwaapplications.shared.submission;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.applicationupdate.ApplicationUpdateRequestService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.submission.ReviewAndSubmitApplicationForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@ExtendWith(MockitoExtension.class)
class ReviewAndSubmitApplicationFormValidatorTest {

  @Mock
  private ApplicationUpdateRequestService applicationUpdateRequestService;

  @Mock
  private PwaHolderTeamService pwaHolderTeamService;

  private ReviewAndSubmitApplicationFormValidator validator;

  private ReviewAndSubmitApplicationForm form;

  private PwaApplicationContext applicationContext;

  private static final String MADE_ONLY_REQUESTED_CHANGES = "madeOnlyRequestedChanges";
  private static final String OTHER_CHANGES_DESCRIPTION = "otherChangesDescription";
  private static final String SUBMITTER_PERSON_ID = "submitterPersonId";

  @BeforeEach
  void setUp() {

    validator = new ReviewAndSubmitApplicationFormValidator(pwaHolderTeamService, applicationUpdateRequestService);
    form = new ReviewAndSubmitApplicationForm();

    var detail = new PwaApplicationDetail();
    applicationContext = new PwaApplicationContext(detail, new WebUserAccount(1), Set.of(PwaApplicationPermission.EDIT));
  }

  @Test
  void validate_openUpdateRequest_editPermission_blank_allErrors() {

    when(applicationUpdateRequestService.applicationHasOpenUpdateRequest(applicationContext.getApplicationDetail()))
        .thenReturn(true);

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, applicationContext);

    assertThat(errors).containsOnly(
        entry(MADE_ONLY_REQUESTED_CHANGES, Set.of(FieldValidationErrorCodes.REQUIRED.errorCode(MADE_ONLY_REQUESTED_CHANGES))),
        entry(SUBMITTER_PERSON_ID, Set.of(FieldValidationErrorCodes.REQUIRED.errorCode(SUBMITTER_PERSON_ID)))
    );
  }

  @Test
  void validate_noUpdateRequest_editPermission_blank_submitterErrorOnly() {

    when(applicationUpdateRequestService.applicationHasOpenUpdateRequest(applicationContext.getApplicationDetail()))
        .thenReturn(false);

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, applicationContext);

    assertThat(errors).containsOnly(
        entry(SUBMITTER_PERSON_ID, Set.of(FieldValidationErrorCodes.REQUIRED.errorCode(SUBMITTER_PERSON_ID)))
    );

  }

  @Test
  void validate_openUpdateRequest_editAndSubmitPermission_blank_updateErrorOnly() {

    when(applicationUpdateRequestService.applicationHasOpenUpdateRequest(applicationContext.getApplicationDetail()))
        .thenReturn(true);

    var context = getContextWithPermissions(PwaApplicationPermission.EDIT, PwaApplicationPermission.SUBMIT);

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, context);

    assertThat(errors).containsOnly(
        entry(MADE_ONLY_REQUESTED_CHANGES, Set.of(FieldValidationErrorCodes.REQUIRED.errorCode(MADE_ONLY_REQUESTED_CHANGES)))
    );

  }

  @Test
  void validate_noUpdateRequest_editAndSubmitPermission_blank_noErrors() {

    when(applicationUpdateRequestService.applicationHasOpenUpdateRequest(applicationContext.getApplicationDetail()))
        .thenReturn(false);

    var context = getContextWithPermissions(PwaApplicationPermission.EDIT, PwaApplicationPermission.SUBMIT);

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, context);

    assertThat(errors).isEmpty();

  }

  @Test
  void validate_noUpdateRequest_editPermission_submitterPersonIdValid_noErrors() {

    var person = PersonTestUtil.createDefaultPerson();

    when(pwaHolderTeamService.getPeopleWithHolderTeamRole(applicationContext.getApplicationDetail(), Role.APPLICATION_SUBMITTER))
        .thenReturn(Set.of(person));

    form.setSubmitterPersonId(person.getId().asInt());

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, applicationContext);

    assertThat(errors).isEmpty();

  }

  @Test
  void validate_noUpdateRequest_editPermission_submitterPersonIdNotValid_submitterErrorOnly() {

    when(pwaHolderTeamService.getPeopleWithHolderTeamRole(applicationContext.getApplicationDetail(), Role.APPLICATION_SUBMITTER))
        .thenReturn(Set.of());

    form.setSubmitterPersonId(5);

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, applicationContext);

    assertThat(errors).containsOnly(
        entry(SUBMITTER_PERSON_ID, Set.of(FieldValidationErrorCodes.INVALID.errorCode(SUBMITTER_PERSON_ID)))
    );

  }

  @Test
  void validate_openUpdateRequest_isSubmitterOnly_blank_noErrors() {

    when(applicationUpdateRequestService.applicationHasOpenUpdateRequest(applicationContext.getApplicationDetail()))
        .thenReturn(true);

    applicationContext = getContextWithPermissions(PwaApplicationPermission.SUBMIT);

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, applicationContext);

    assertThat(errors).isEmpty();

  }


  @Test
  void validate_openUpdateRequest_editPermission_whenExtraChanges_noDesc() {

    when(applicationUpdateRequestService.applicationHasOpenUpdateRequest(applicationContext.getApplicationDetail()))
        .thenReturn(true);

    form.setMadeOnlyRequestedChanges(false);

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, applicationContext);

    assertThat(errors).contains(
        entry(OTHER_CHANGES_DESCRIPTION, Set.of(FieldValidationErrorCodes.REQUIRED.errorCode(OTHER_CHANGES_DESCRIPTION)))
    );

  }

  @Test
  void validate_openUpdateRequest_editPermission_whenExtraChanges_tooLong() {

    when(applicationUpdateRequestService.applicationHasOpenUpdateRequest(applicationContext.getApplicationDetail()))
        .thenReturn(true);

    form.setMadeOnlyRequestedChanges(false);
    form.setOtherChangesDescription(ValidatorTestUtils.overMaxDefaultCharLength());

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, applicationContext);

    assertThat(errors).contains(
        entry(OTHER_CHANGES_DESCRIPTION, Set.of(FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.errorCode(OTHER_CHANGES_DESCRIPTION)))
    );

  }

  @Test
  void validate_openUpdateRequest_editPermission_onlyRequestedChanges() {

    when(applicationUpdateRequestService.applicationHasOpenUpdateRequest(applicationContext.getApplicationDetail()))
        .thenReturn(true);

    form.setMadeOnlyRequestedChanges(true);

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, applicationContext);

    assertThat(errors).containsOnlyKeys(SUBMITTER_PERSON_ID);

  }

  private PwaApplicationContext getContextWithPermissions(PwaApplicationPermission... permissions) {
    return new PwaApplicationContext(applicationContext.getApplicationDetail(), applicationContext.getUser(), Set.of(permissions));
  }


}