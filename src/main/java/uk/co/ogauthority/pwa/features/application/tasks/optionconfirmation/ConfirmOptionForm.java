package uk.co.ogauthority.pwa.features.application.tasks.optionconfirmation;

import uk.co.ogauthority.pwa.model.entity.enums.ConfirmedOptionType;

public class ConfirmOptionForm {

  private ConfirmedOptionType confirmedOptionType;

  private String optionCompletedDescription;

  private String otherWorkDescription;

  public ConfirmedOptionType getConfirmedOptionType() {
    return confirmedOptionType;
  }

  public void setConfirmedOptionType(ConfirmedOptionType confirmedOptionType) {
    this.confirmedOptionType = confirmedOptionType;
  }

  public String getOptionCompletedDescription() {
    return optionCompletedDescription;
  }

  public void setOptionCompletedDescription(String optionCompletedDescription) {
    this.optionCompletedDescription = optionCompletedDescription;
  }

  public String getOtherWorkDescription() {
    return otherWorkDescription;
  }

  public void setOtherWorkDescription(String otherWorkDescription) {
    this.otherWorkDescription = otherWorkDescription;
  }
}
