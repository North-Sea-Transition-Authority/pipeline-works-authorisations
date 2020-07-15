package uk.co.ogauthority.pwa.validators;

import java.util.Objects;

public class ProjectInformationFormValidationHints {

  private final boolean isAnyDepositQuestionRequired;
  private final boolean isPermanentDepositQuestionRequired;
  private final Boolean isLinkedToField;

  public ProjectInformationFormValidationHints(boolean isAnyDepositQuestionRequired,
                                               boolean isPermanentDepositQuestionRequired, Boolean isLinkedToField) {
    this.isAnyDepositQuestionRequired = isAnyDepositQuestionRequired;
    this.isPermanentDepositQuestionRequired = isPermanentDepositQuestionRequired;
    this.isLinkedToField = isLinkedToField;
  }

  public boolean isAnyDepositQuestionRequired() {
    return isAnyDepositQuestionRequired;
  }

  public boolean isPermanentDepositQuestionRequired() {
    return isPermanentDepositQuestionRequired;
  }

  public Boolean getLinkedToField() {
    return isLinkedToField;
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
