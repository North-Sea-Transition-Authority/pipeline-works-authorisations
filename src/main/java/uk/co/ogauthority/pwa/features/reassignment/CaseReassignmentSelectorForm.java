package uk.co.ogauthority.pwa.features.reassignment;

import java.util.List;

public class CaseReassignmentSelectorForm {
  private List<Integer> selectedApplicationIds;

  private Integer caseOfficerAssignee;

  public CaseReassignmentSelectorForm() {
  }

  public List<Integer> getSelectedApplicationIds() {
    return selectedApplicationIds;
  }

  public void setSelectedApplicationIds(List<Integer> selectedApplicationIds) {
    this.selectedApplicationIds = selectedApplicationIds;
  }

  public Integer getCaseOfficerAssignee() {
    return caseOfficerAssignee;
  }

  public void setCaseOfficerAssignee(Integer caseOfficerAssignee) {
    this.caseOfficerAssignee = caseOfficerAssignee;
  }
}
