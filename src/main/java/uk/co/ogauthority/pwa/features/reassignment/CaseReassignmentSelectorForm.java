package uk.co.ogauthority.pwa.features.reassignment;

import java.util.List;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;

public class CaseReassignmentSelectorForm {
  private List<Integer> selectedApplicationIds;

  private PersonId assignedCaseOfficerPersonId;

  public CaseReassignmentSelectorForm() {
  }

  public List<Integer> getSelectedApplicationIds() {
    return selectedApplicationIds;
  }

  public void setSelectedApplicationIds(List<Integer> selectedApplicationIds) {
    this.selectedApplicationIds = selectedApplicationIds;
  }

  public PersonId getAssignedCaseOfficerPersonId() {
    return assignedCaseOfficerPersonId;
  }

  public void setAssignedCaseOfficerPersonId(
      PersonId assignedCaseOfficerPersonId) {
    this.assignedCaseOfficerPersonId = assignedCaseOfficerPersonId;
  }
}
