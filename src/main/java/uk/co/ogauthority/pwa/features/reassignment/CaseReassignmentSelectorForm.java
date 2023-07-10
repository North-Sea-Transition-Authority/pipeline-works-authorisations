package uk.co.ogauthority.pwa.features.reassignment;

import java.util.List;

public class CaseReassignmentSelectorForm {
  private List<String> selectedApplicationIds;

  private Integer assignedCaseOfficerPersonId;

  public CaseReassignmentSelectorForm() {
  }

  public List<String> getSelectedApplicationIds() {
    return selectedApplicationIds;
  }

  public void setSelectedApplicationIds(List<String> selectedApplicationIds) {
    this.selectedApplicationIds = selectedApplicationIds;
  }

  public Integer getAssignedCaseOfficerPersonId() {
    return assignedCaseOfficerPersonId;
  }

  public void setAssignedCaseOfficerPersonId(Integer assignedCaseOfficerPersonId) {
    this.assignedCaseOfficerPersonId = assignedCaseOfficerPersonId;
  }
}
