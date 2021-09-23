package uk.co.ogauthority.pwa.model.form.pwaapplications.shared.crossings;

import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import uk.co.ogauthority.pwa.util.ValidatorUtils;

public class AddCableCrossingForm {

  @NotNull(message = "Enter the name of the cable")
  @Length(max = ValidatorUtils.MAX_DEFAULT_STRING_LENGTH, message = "Cable name" + ValidatorUtils.MAX_DEFAULT_STRING_LENGTH_MESSAGE)
  private String cableName;

  @NotNull(message = "Enter cable location information")
  @Length(max = ValidatorUtils.MAX_DEFAULT_STRING_LENGTH, message = "Location" + ValidatorUtils.MAX_DEFAULT_STRING_LENGTH_MESSAGE)
  private String location;

  @NotNull(message = "State the owner of the cable")
  @Length(max = ValidatorUtils.MAX_DEFAULT_STRING_LENGTH, message = "Cable owner" + ValidatorUtils.MAX_DEFAULT_STRING_LENGTH_MESSAGE)
  private String cableOwner;

  public String getCableName() {
    return cableName;
  }

  public void setCableName(String cableName) {
    this.cableName = cableName;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public String getCableOwner() {
    return cableOwner;
  }

  public void setCableOwner(String cableOwner) {
    this.cableOwner = cableOwner;
  }
}
