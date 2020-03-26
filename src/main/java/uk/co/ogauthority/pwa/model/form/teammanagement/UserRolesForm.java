package uk.co.ogauthority.pwa.model.form.teammanagement;

import java.util.List;
import javax.validation.constraints.NotEmpty;

public class UserRolesForm {

  @NotEmpty(message = "Select at least one role")
  private List<String> userRoles;

  public List<String> getUserRoles() {
    return userRoles;
  }

  public void setUserRoles(List<String> userRoles) {
    this.userRoles = userRoles;
  }
}

