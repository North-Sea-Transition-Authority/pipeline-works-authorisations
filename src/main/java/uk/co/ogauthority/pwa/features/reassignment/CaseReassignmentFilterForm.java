package uk.co.ogauthority.pwa.features.reassignment;

import java.io.Serializable;

public class CaseReassignmentFilterForm implements Serializable {
  private Integer caseOfficerPersonId;

  public Integer getCaseOfficerPersonId() {
    return caseOfficerPersonId;
  }

  public void setCaseOfficerPersonId(Integer caseOfficerPersonId) {
    this.caseOfficerPersonId = caseOfficerPersonId;
  }
}
