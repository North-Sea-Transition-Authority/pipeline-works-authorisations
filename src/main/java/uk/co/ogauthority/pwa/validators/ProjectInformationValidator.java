package uk.co.ogauthority.pwa.validators;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.co.ogauthority.pwa.model.form.pwaapplications.initial.ProjectInformationForm;
import uk.co.ogauthority.pwa.util.ValidatorUtils;

@Service
public class ProjectInformationValidator implements Validator {


  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(ProjectInformationForm.class);
  }

  @Override
  public void validate(Object o, Errors errors) {
    var form = (ProjectInformationForm) o;
    ValidatorUtils.validateDateIsPresentOrFuture(
        "proposedStart", "proposed start",
        form.getProposedStartDay(), form.getProposedStartMonth(), form.getProposedStartYear(), errors);
    ValidatorUtils.validateDateIsPresentOrFuture(
        "mobilisation", "mobilisation",
        form.getMobilisationDay(), form.getMobilisationMonth(), form.getMobilisationYear(), errors);
    ValidatorUtils.validateDateIsPresentOrFuture(
        "earliestCompletion", "earliest completion",
        form.getEarliestCompletionDay(), form.getEarliestCompletionMonth(), form.getEarliestCompletionYear(), errors);
    ValidatorUtils.validateDateIsPresentOrFuture(
        "latestCompletion", "latest completion",
        form.getLatestCompletionDay(), form.getLatestCompletionMonth(), form.getLatestCompletionYear(), errors);
  }
}
