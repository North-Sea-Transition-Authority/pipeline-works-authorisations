package uk.co.ogauthority.pwa.features.application.tasks.locationdetails;

import java.util.Set;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;

public class LocationDetailsFormValidationHints {

  private final ValidationType validationType;
  private final Set<LocationDetailsQuestion> requiredQuestions;

  public LocationDetailsFormValidationHints(
      ValidationType validationType,
      Set<LocationDetailsQuestion> requiredQuestions) {
    this.validationType = validationType;
    this.requiredQuestions = requiredQuestions;
  }

  public ValidationType getValidationType() {
    return validationType;
  }

  public Set<LocationDetailsQuestion> getRequiredQuestions() {
    return requiredQuestions;
  }

}
