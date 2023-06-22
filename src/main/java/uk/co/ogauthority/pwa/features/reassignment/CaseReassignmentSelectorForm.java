package uk.co.ogauthority.pwa.features.reassignment;

import java.io.Serializable;
import java.util.List;

public class CaseReassignmentSelectorForm implements Serializable {
  private List<Integer> selectedCases;

  private Integer caseOfficerAssignee;

  public CaseReassignmentSelectorForm() {
  }

  public List<Integer> getSelectedCases() {
    return selectedCases;
  }

  public void setSelectedCases(List<Integer> selectedCases) {
    this.selectedCases = selectedCases;
  }

  public Integer getCaseOfficerAssignee() {
    return caseOfficerAssignee;
  }

  public void setCaseOfficerAssignee(Integer caseOfficerAssignee) {
    this.caseOfficerAssignee = caseOfficerAssignee;
  }
}
