package uk.co.ogauthority.pwa.features.reassignment;

import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PadProjectInformation;

public class SelectedReassignmentPwasView {
  private Integer id;

  private String name;

  private Boolean valid = true;

  private SelectedReassignmentPwasView() {}

  public static SelectedReassignmentPwasView fromApplication(PwaApplication pwaApplication, PadProjectInformation projectInformation) {
    var selected = new SelectedReassignmentPwasView();
    selected.id = pwaApplication.getId();
    selected.name = pwaApplication.getAppReference() + " : " + projectInformation.getProjectName();
    return selected;
  }

  public Integer getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Boolean isValid() {
    return valid;
  }
}
