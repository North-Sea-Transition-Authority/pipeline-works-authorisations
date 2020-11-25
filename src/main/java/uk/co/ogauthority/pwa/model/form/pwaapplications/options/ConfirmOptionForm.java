package uk.co.ogauthority.pwa.model.form.pwaapplications.options;

import uk.co.ogauthority.pwa.model.entity.enums.ConfirmedOptionType;

public class ConfirmOptionForm {

  private ConfirmedOptionType confirmedOptionType;

  private String optionCompletedDescription;

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
}
