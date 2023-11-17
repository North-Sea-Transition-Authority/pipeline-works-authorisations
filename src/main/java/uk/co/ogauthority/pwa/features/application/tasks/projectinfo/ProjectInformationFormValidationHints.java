package uk.co.ogauthority.pwa.features.application.tasks.projectinfo;

import java.util.Objects;
import java.util.Set;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;

public class ProjectInformationFormValidationHints {

  private final PwaApplicationType pwaApplicationType;

  private final PwaResourceType pwaResourceType;
  private final ValidationType validationType;
  private final Set<ProjectInformationQuestion> requiredQuestions;
  private final Boolean isFdpQuestionRequiredBasedOnField;

  public ProjectInformationFormValidationHints(
      PwaApplicationType pwaApplicationType,
      PwaResourceType pwaResourceType,
      ValidationType validationType,
      Set<ProjectInformationQuestion> requiredQuestions,
      Boolean isFdpQuestionRequiredBasedOnField) {
    this.pwaApplicationType = pwaApplicationType;
    this.pwaResourceType = pwaResourceType;
    this.validationType = validationType;
    this.requiredQuestions = requiredQuestions;
    this.isFdpQuestionRequiredBasedOnField = isFdpQuestionRequiredBasedOnField;
  }

  public PwaApplicationType getPwaApplicationType() {
    return pwaApplicationType;
  }

  public ValidationType getValidationType() {
    return validationType;
  }

  public PwaResourceType getPwaResourceType() {
    return pwaResourceType;
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
    return Objects.equals(pwaApplicationType, that.pwaApplicationType)
        && validationType == that.validationType
        && Objects.equals(requiredQuestions, that.requiredQuestions)
        && Objects.equals(isFdpQuestionRequiredBasedOnField, that.isFdpQuestionRequiredBasedOnField);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pwaApplicationType, validationType, requiredQuestions, isFdpQuestionRequiredBasedOnField);
  }
}
