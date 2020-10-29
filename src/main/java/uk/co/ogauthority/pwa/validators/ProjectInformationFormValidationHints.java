package uk.co.ogauthority.pwa.validators;

import java.util.Objects;
import java.util.Set;
import uk.co.ogauthority.pwa.model.entity.enums.ProjectInformationQuestion;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;

public class ProjectInformationFormValidationHints {

  private final ValidationType validationType;
  private final Set<ProjectInformationQuestion> requiredQuestions;
  private final Boolean isFdpQuestionRequiredBasedOnField;

  public ProjectInformationFormValidationHints(ValidationType validationType,
                                               Set<ProjectInformationQuestion> requiredQuestions,
                                               Boolean isFdpQuestionRequiredBasedOnField) {
    this.validationType = validationType;
    this.requiredQuestions = requiredQuestions;
    this.isFdpQuestionRequiredBasedOnField = isFdpQuestionRequiredBasedOnField;
  }

  public ValidationType getValidationType() {
    return validationType;
  }

  public Set<ProjectInformationQuestion> getRequiredQuestions() {
    return requiredQuestions;
  }

  public Boolean isFdpQuestionRequired() {
    return isFdpQuestionRequiredBasedOnField;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ProjectInformationFormValidationHints that = (ProjectInformationFormValidationHints) o;
    return validationType == that.validationType
        && Objects.equals(requiredQuestions, that.requiredQuestions)
        && Objects.equals(isFdpQuestionRequiredBasedOnField, that.isFdpQuestionRequiredBasedOnField);
  }

  @Override
  public int hashCode() {
    return Objects.hash(validationType, requiredQuestions, isFdpQuestionRequiredBasedOnField);
  }
}
