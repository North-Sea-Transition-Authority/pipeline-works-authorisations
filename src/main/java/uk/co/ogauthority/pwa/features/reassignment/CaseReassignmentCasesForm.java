package uk.co.ogauthority.pwa.features.reassignment;

import java.util.List;

public class CaseReassignmentCasesForm {
  private List<String> selectedApplicationIds;

  public CaseReassignmentCasesForm() {
  }

  public List<String> getSelectedApplicationIds() {
    return selectedApplicationIds;
  }

  public void setSelectedApplicationIds(List<String> selectedApplicationIds) {
    this.selectedApplicationIds = selectedApplicationIds;
  }
}
