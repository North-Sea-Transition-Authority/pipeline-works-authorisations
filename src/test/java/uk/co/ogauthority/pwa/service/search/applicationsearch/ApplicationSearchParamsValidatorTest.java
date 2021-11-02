package uk.co.ogauthority.pwa.service.search.applicationsearch;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationSearchParamsValidatorTest {

  private static final int CASE_OFFICER_PERSON_ID = 1;
  private static final OrganisationUnitId HOLDER_ORG_UNIT_ID = new OrganisationUnitId(10);

  private ApplicationSearchParamsValidator applicationSearchParamsValidator;
  private AuthenticatedUserAccount authenticatedUserAccount;


  @Before
  public void setUp() throws Exception {
    applicationSearchParamsValidator = new ApplicationSearchParamsValidator();

    authenticatedUserAccount = new AuthenticatedUserAccount(new WebUserAccount(),
        EnumSet.allOf(PwaUserPrivilege.class));
  }

  @Test
  public void validate_whenEmptyParams_userTypeSmokeText() {
    var emptyParams = ApplicationSearchParametersBuilder.createEmptyParams();

    for (UserType userType : UserType.values()) {
      var context = ApplicationSearchContextTestUtil.emptyUserContext(authenticatedUserAccount, userType);
      var validationErrors = ValidatorTestUtils.getFormValidationErrors(
          applicationSearchParamsValidator,
          emptyParams,
          context
      );
      try {
        assertThat(validationErrors).isEmpty();
      } catch (AssertionError e){
        throw new AssertionError("Error at user type:" + userType , e);
      }

    }

  }

  @Test
  public void supports_whenTargetIsValid() {
    assertThat(applicationSearchParamsValidator.supports(ApplicationSearchParameters.class)).isTrue();
  }

  @Test
  public void supports_whenTargetIsInvalid() {
    assertThat(applicationSearchParamsValidator.supports(Object.class)).isFalse();
  }

  @Test
  public void validate_caseOfficerIdProvided_nonOgaUserType() {
    var context = ApplicationSearchContextTestUtil.emptyUserContext(authenticatedUserAccount, UserType.INDUSTRY);
    var validationErrors = ValidatorTestUtils.getFormValidationErrors(
        applicationSearchParamsValidator,
        new ApplicationSearchParametersBuilder().setCaseOfficerPersonId(CASE_OFFICER_PERSON_ID).createApplicationSearchParameters(),
        context);

    assertThat(validationErrors).contains(Map.entry(
        "caseOfficerPersonId", Set.of("caseOfficerPersonId" + FieldValidationErrorCodes.INVALID.getCode())));
  }

  @Test
  public void validate_caseOfficerIdProvided_ogaUserType() {
    var context = ApplicationSearchContextTestUtil.emptyUserContext(authenticatedUserAccount, UserType.OGA);
    var validationErrors = ValidatorTestUtils.getFormValidationErrors(
        applicationSearchParamsValidator,
        new ApplicationSearchParametersBuilder().setCaseOfficerPersonId(CASE_OFFICER_PERSON_ID).createApplicationSearchParameters(),
        context);

    assertThat(validationErrors).doesNotContain(Map.entry(
        "caseOfficerPersonId", Set.of("caseOfficerPersonId" + FieldValidationErrorCodes.INVALID.getCode())));
  }

  @Test
  public void validate_holderOrgUnitId_ogaUserType() {
    var context = ApplicationSearchContextTestUtil.emptyUserContext(authenticatedUserAccount, UserType.OGA);
    var validationErrors = ValidatorTestUtils.getFormValidationErrors(
        applicationSearchParamsValidator,
        new ApplicationSearchParametersBuilder().setHolderOrgUnitId(HOLDER_ORG_UNIT_ID.asInt()).createApplicationSearchParameters(),
        context);

    assertThat(validationErrors).doesNotContain(Map.entry(
        "holderOrgUnitId", Set.of("holderOrgUnitId" + FieldValidationErrorCodes.INVALID.getCode())));
  }

  @Test
  public void validate_holderOrgUnitId_combinedOgaIndustryUserType_selectedHolderOrgIdNotInIndustryOrgs() {
    var context = ApplicationSearchContextTestUtil.combinedIndustryOgaContext(authenticatedUserAccount, Set.of(HOLDER_ORG_UNIT_ID));
    var validationErrors = ValidatorTestUtils.getFormValidationErrors(
        applicationSearchParamsValidator,
        new ApplicationSearchParametersBuilder().setHolderOrgUnitId(9999999).createApplicationSearchParameters(),
        context);

    assertThat(validationErrors).doesNotContain(Map.entry(
        "holderOrgUnitId", Set.of("holderOrgUnitId" + FieldValidationErrorCodes.INVALID.getCode())));
  }

  @Test
  public void validate_holderOrgUnitId_IndustryUserTypeOnly_invalidOrgUnitSelected() {
    var context = ApplicationSearchContextTestUtil.emptyUserContext(authenticatedUserAccount, UserType.INDUSTRY);
    var validationErrors = ValidatorTestUtils.getFormValidationErrors(
        applicationSearchParamsValidator,
        new ApplicationSearchParametersBuilder().setHolderOrgUnitId(HOLDER_ORG_UNIT_ID.asInt()).createApplicationSearchParameters(),
        context);

    assertThat(validationErrors).contains(Map.entry(
        "holderOrgUnitId", Set.of("holderOrgUnitId" + FieldValidationErrorCodes.INVALID.getCode())));
  }

  @Test
  public void validate_holderOrgUnitId_IndustryUserTypeOnly_validOrgUnitSelected() {
    var context = ApplicationSearchContextTestUtil.industryContext(authenticatedUserAccount, Set.of(HOLDER_ORG_UNIT_ID ));
    var validationErrors = ValidatorTestUtils.getFormValidationErrors(
        applicationSearchParamsValidator,
        new ApplicationSearchParametersBuilder().setHolderOrgUnitId(HOLDER_ORG_UNIT_ID.asInt()).createApplicationSearchParameters(),
        context);

    assertThat(validationErrors).doesNotContain(Map.entry(
        "holderOrgUnitId", Set.of("holderOrgUnitId" + FieldValidationErrorCodes.INVALID.getCode())));
  }

}