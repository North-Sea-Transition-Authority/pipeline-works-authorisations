package uk.co.ogauthority.pwa.validators;

import java.util.Objects;

public class ProjectInformationFormValidationHints {

  private final boolean isAnyDepositQuestionRequired;
  private final boolean isPermanentDepositQuestionRequired;
  private final Boolean isFdpQuestionRequired;

  public ProjectInformationFormValidationHints(boolean isAnyDepositQuestionRequired,
                                               boolean isPermanentDepositQuestionRequired, Boolean isFdpQuestionRequired) {
    this.isAnyDepositQuestionRequired = isAnyDepositQuestionRequired;
    this.isPermanentDepositQuestionRequired = isPermanentDepositQuestionRequired;
    this.isFdpQuestionRequired = isFdpQuestionRequired;
  }

  public boolean isAnyDepositQuestionRequired() {
    return isAnyDepositQuestionRequired;
  }

  public boolean isPermanentDepositQuestionRequired() {
    return isPermanentDepositQuestionRequired;
  }

  public Boolean isFdpQuestionRequired() {
    return isFdpQuestionRequired;
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
    return isAnyDepositQuestionRequired == that.isAnyDepositQuestionRequired
        && isPermanentDepositQuestionRequired == that.isPermanentDepositQuestionRequired
        && isFdpQuestionRequired == that.isFdpQuestionRequired;
  }

  @Override
  public int hashCode() {
    return Objects.hash(isAnyDepositQuestionRequired, isPermanentDepositQuestionRequired, isFdpQuestionRequired);
  }
}
