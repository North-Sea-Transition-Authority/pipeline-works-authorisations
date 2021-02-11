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
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationSearchParamsValidatorTest {

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
        new ApplicationSearchParametersBuilder().setCaseOfficerId("1").createApplicationSearchParameters(),
        context);

    assertThat(validationErrors).contains(Map.entry(
        "caseOfficerId", Set.of("caseOfficerId" + FieldValidationErrorCodes.INVALID.getCode())));
  }

  @Test
  public void validate_caseOfficerIdProvided_ogaUserType() {
    var context = ApplicationSearchContextTestUtil.emptyUserContext(authenticatedUserAccount, UserType.OGA);
    var validationErrors = ValidatorTestUtils.getFormValidationErrors(
        applicationSearchParamsValidator,
        new ApplicationSearchParametersBuilder().setCaseOfficerId("1").createApplicationSearchParameters(),
        context);

    assertThat(validationErrors).doesNotContain(Map.entry(
        "caseOfficerId", Set.of("caseOfficerId" + FieldValidationErrorCodes.INVALID.getCode())));
  }

}