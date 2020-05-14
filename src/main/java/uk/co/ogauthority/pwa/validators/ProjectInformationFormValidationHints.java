package uk.co.ogauthority.pwa.validators;

import java.util.Objects;

public class ProjectInformationFormValidationHints {

  private final boolean isAnyDepositQuestionRequired;
  private final boolean isPermanentDepositQuestionRequired;

  public ProjectInformationFormValidationHints(boolean isAnyDepositQuestionRequired, boolean isPermanentDepositQuestionRequired) {
    this.isAnyDepositQuestionRequired = isAnyDepositQuestionRequired;
    this.isPermanentDepositQuestionRequired = isPermanentDepositQuestionRequired;
  }

  public boolean isAnyDepositQuestionRequired() {
    return isAnyDepositQuestionRequired;
  }

  public boolean isPermanentDepositQuestionRequired() {
    return isPermanentDepositQuestionRequired;
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
        && isPermanentDepositQuestionRequired == that.isPermanentDepositQuestionRequired;
  }

  @Override
  public int hashCode() {
    return Objects.hash(isAnyDepositQuestionRequired, isPermanentDepositQuestionRequired);
  }
}
