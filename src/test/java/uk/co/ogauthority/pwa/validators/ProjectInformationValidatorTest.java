package uk.co.ogauthority.pwa.validators;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.ProjectInformationForm;
import uk.co.ogauthority.pwa.util.ValidatorTestUtils;

public class ProjectInformationValidatorTest {

  private ProjectInformationValidator validator;

  @Before
  public void setUp() {
    validator = new ProjectInformationValidator();
  }

  @Test
  public void validate_ProposedStartNull() {
    var form = new ProjectInformationForm();
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errors).containsKeys("proposedStartDay", "proposedStartMonth", "proposedStartYear");
  }

  @Test
  public void validate_ProposedStartInPast() {
    var date = LocalDate.now().minusDays(2);
    var form = new ProjectInformationForm();
    form.setProposedStartDay(date.getDayOfMonth());
    form.setProposedStartMonth(date.getMonthValue());
    form.setProposedStartYear(date.getYear());
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errors).containsKeys("proposedStartDay", "proposedStartMonth", "proposedStartYear");
  }

  @Test
  public void validate_ProposedStartValid() {
    var date = LocalDate.now().plusDays(2);
    var form = new ProjectInformationForm();
    form.setProposedStartDay(date.getDayOfMonth());
    form.setProposedStartMonth(date.getMonthValue());
    form.setProposedStartYear(date.getYear());
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errors).doesNotContainKeys("proposedStartDay", "proposedStartMonth", "proposedStartYear");
  }

  @Test
  public void validate_MobilisationNull() {
    var form = new ProjectInformationForm();
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errors).containsKeys("mobilisationDay", "mobilisationMonth", "mobilisationYear");
  }

  @Test
  public void validate_MobilisationInPast() {
    var date = LocalDate.now().minusDays(2);
    var form = new ProjectInformationForm();
    form.setMobilisationDay(date.getDayOfMonth());
    form.setMobilisationMonth(date.getMonthValue());
    form.setMobilisationYear(date.getYear());
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errors).containsKeys("mobilisationDay", "mobilisationMonth", "mobilisationYear");
  }

  @Test
  public void validate_MobilisationValid() {
    var date = LocalDate.now().plusDays(2);
    var form = new ProjectInformationForm();
    form.setMobilisationDay(date.getDayOfMonth());
    form.setMobilisationMonth(date.getMonthValue());
    form.setMobilisationYear(date.getYear());
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errors).doesNotContainKeys("mobilisationDay", "mobilisationMonth", "mobilisationYear");
  }

  @Test
  public void validate_EarliestCompletionNull() {
    var form = new ProjectInformationForm();
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errors).containsKeys("earliestCompletionDay", "earliestCompletionMonth", "earliestCompletionYear");
  }

  @Test
  public void validate_EarliestCompletionInPast() {
    var date = LocalDate.now().minusDays(2);
    var form = new ProjectInformationForm();
    form.setEarliestCompletionDay(date.getDayOfMonth());
    form.setEarliestCompletionMonth(date.getMonthValue());
    form.setEarliestCompletionYear(date.getYear());
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errors).containsKeys("earliestCompletionDay", "earliestCompletionMonth", "earliestCompletionYear");
  }

  @Test
  public void validate_EarliestCompletionValid() {
    var date = LocalDate.now().plusDays(2);
    var form = new ProjectInformationForm();
    form.setEarliestCompletionDay(date.getDayOfMonth());
    form.setEarliestCompletionMonth(date.getMonthValue());
    form.setEarliestCompletionYear(date.getYear());
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errors).doesNotContainKeys("earliestCompletionDay", "earliestCompletionMonth", "earliestCompletionYear");
  }

  @Test
  public void validate_LatestCompletionNull() {
    var form = new ProjectInformationForm();
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errors).containsKeys("latestCompletionDay", "latestCompletionMonth", "latestCompletionYear");
  }

  @Test
  public void validate_LatestCompletionInPast() {
    var date = LocalDate.now().minusDays(2);
    var form = new ProjectInformationForm();
    form.setLatestCompletionDay(date.getDayOfMonth());
    form.setLatestCompletionMonth(date.getMonthValue());
    form.setLatestCompletionYear(date.getYear());
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errors).containsKeys("latestCompletionDay", "latestCompletionMonth", "latestCompletionYear");
  }

  @Test
  public void validate_LatestCompletionValid() {
    var date = LocalDate.now().plusDays(2);
    var form = new ProjectInformationForm();
    form.setLatestCompletionDay(date.getDayOfMonth());
    form.setLatestCompletionMonth(date.getMonthValue());
    form.setLatestCompletionYear(date.getYear());
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errors).doesNotContainKeys("latestCompletionDay", "latestCompletionMonth", "latestCompletionYear");
  }
}