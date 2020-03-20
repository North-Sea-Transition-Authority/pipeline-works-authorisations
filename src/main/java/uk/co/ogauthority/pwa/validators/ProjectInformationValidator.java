package uk.co.ogauthority.pwa.validators;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.ProjectInformationForm;
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
    ValidatorUtils.validateDate(
        "proposedStart", "proposed start",
        form.getProposedStartDay(), form.getProposedStartMonth(), form.getProposedStartYear(), errors);
    ValidatorUtils.validateDate(
        "mobilisation", "mobilisation",
        form.getMobilisationDay(), form.getMobilisationMonth(), form.getMobilisationYear(), errors);
    ValidatorUtils.validateDate(
        "earliestCompletion", "earliest completion",
        form.getEarliestCompletionDay(), form.getEarliestCompletionMonth(), form.getEarliestCompletionYear(), errors);
    ValidatorUtils.validateDate(
        "latestCompletion", "latest completion",
        form.getLatestCompletionDay(), form.getLatestCompletionMonth(), form.getLatestCompletionYear(), errors);
  }
}
