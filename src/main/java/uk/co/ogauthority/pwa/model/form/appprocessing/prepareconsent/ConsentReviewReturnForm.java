package uk.co.ogauthority.pwa.model.form.appprocessing.prepareconsent;

import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;

public class ConsentReviewReturnForm {

  private PersonId caseOfficerPersonId;

  private String returnReason;

  public String getReturnReason() {
    return returnReason;
  }

  public void setReturnReason(String returnReason) {
    this.returnReason = returnReason;
  }

  public Integer getCaseOfficerPersonId() {
    return caseOfficerPersonId == null ? null : caseOfficerPersonId.asInt();
  }

  public void setCaseOfficerPersonId(Integer caseOfficerPersonId) {
    this.caseOfficerPersonId = new PersonId(caseOfficerPersonId);
  }

}
