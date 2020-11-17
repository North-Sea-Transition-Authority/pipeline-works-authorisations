package uk.co.ogauthority.pwa.model.form.pwaapplications.shared.crossings;

import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public class AddCableCrossingForm {

  @NotNull(message = "Enter the name of the cable")
  @Length(max = 4000, message = "Cable name must be 4000 characters or fewer")
  private String cableName;

  @NotNull(message = "Enter cable location information")
  @Length(max = 4000, message = "Location must be 4000 characters or fewer")
  private String location;

  @NotNull(message = "State the owner of the cable")
  @Length(max = 4000, message = "Cable owner must be 4000 characters or fewer")
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
