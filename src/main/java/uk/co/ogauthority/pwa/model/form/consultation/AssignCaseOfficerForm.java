package uk.co.ogauthority.pwa.model.form.consultation;


import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;

public class AssignCaseOfficerForm {

  private PersonId caseOfficerPersonId;

  public PersonId getCaseOfficerPerson() {
    return caseOfficerPersonId;
  }

  public Integer getCaseOfficerPersonId() {
    return caseOfficerPersonId == null ? null : caseOfficerPersonId.asInt();
  }

  public void setCaseOfficerPersonId(Integer caseOfficerPersonId) {
    this.caseOfficerPersonId = new PersonId(caseOfficerPersonId);
  }
}
