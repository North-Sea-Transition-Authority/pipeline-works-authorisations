package uk.co.ogauthority.pwa.service.enums.pwaapplications.generic;

import uk.co.ogauthority.pwa.util.validationgroups.FullValidation;
import uk.co.ogauthority.pwa.util.validationgroups.PartialValidation;

public enum ValidationType {

  FULL("Save and complete", FullValidation.class),
  PARTIAL("Save and complete later", PartialValidation.class);

  private final String buttonText;

  private final Class<?> validationClass;

  ValidationType(String buttonText, Class<?> validationClass) {
    this.buttonText = buttonText;
    this.validationClass = validationClass;
  }

  public String getButtonText() {
    return buttonText;
  }


  public Class<?> getValidationClass() {
    return validationClass;
  }

}
