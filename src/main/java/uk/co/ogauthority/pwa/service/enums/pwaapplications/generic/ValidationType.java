package uk.co.ogauthority.pwa.service.enums.pwaapplications.generic;

import uk.co.ogauthority.pwa.features.analytics.AnalyticsEventCategory;
import uk.co.ogauthority.pwa.util.validationgroups.FullValidation;
import uk.co.ogauthority.pwa.util.validationgroups.PartialValidation;

public enum ValidationType {

  FULL("Save and complete", FullValidation.class, AnalyticsEventCategory.SAVE_APP_FORM),
  PARTIAL("Save and complete later", PartialValidation.class, AnalyticsEventCategory.SAVE_APP_FORM_COMPLETE_LATER);

  private final String buttonText;

  private final Class<?> validationClass;

  private final AnalyticsEventCategory analyticsEventCategory;

  ValidationType(String buttonText, Class<?> validationClass, AnalyticsEventCategory analyticsEventCategory) {
    this.buttonText = buttonText;
    this.validationClass = validationClass;
    this.analyticsEventCategory = analyticsEventCategory;
  }

  public String getButtonText() {
    return buttonText;
  }

  public Class<?> getValidationClass() {
    return validationClass;
  }

  public AnalyticsEventCategory getAnalyticsEventCategory() {
    return analyticsEventCategory;
  }

}
