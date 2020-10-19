package uk.co.ogauthority.pwa.model.form.pwaapplications.huoo;

import java.util.Set;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;

public class HuooForm {

  private Set<HuooRole> huooRoles;
  private HuooType huooType;
  private Integer organisationUnitId;

  public Set<HuooRole> getHuooRoles() {
    return huooRoles;
  }

  public void setHuooRoles(Set<HuooRole> huooRoles) {
    this.huooRoles = huooRoles;
  }

  public HuooType getHuooType() {
    return huooType;
  }

  public void setHuooType(HuooType huooType) {
    this.huooType = huooType;
  }

  public Integer getOrganisationUnitId() {
    return organisationUnitId;
  }

  public void setOrganisationUnitId(Integer organisationUnitId) {
    this.organisationUnitId = organisationUnitId;
  }
}
