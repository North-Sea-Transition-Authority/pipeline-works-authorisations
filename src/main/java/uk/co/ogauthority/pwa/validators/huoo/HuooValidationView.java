package uk.co.ogauthority.pwa.validators.huoo;

import java.util.Set;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;
import uk.co.ogauthority.pwa.model.entity.enums.TreatyAgreement;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.PadOrganisationRole;

public class HuooValidationView {

  private HuooType huooType;
  private TreatyAgreement treatyAgreement;
  private Set<HuooRole> roles;
  private PwaApplicationDetail pwaApplicationDetail;
  private PortalOrganisationUnit portalOrganisationUnit;

  public HuooValidationView(Set<PadOrganisationRole> padOrganisationRoles) {
    roles = padOrganisationRoles.stream()
        .map(PadOrganisationRole::getRole)
        .collect(Collectors.toSet());
    padOrganisationRoles.forEach(padOrganisationRole -> {
      huooType = padOrganisationRole.getType();
      pwaApplicationDetail = padOrganisationRole.getPwaApplicationDetail();
      treatyAgreement = padOrganisationRole.getAgreement();
      portalOrganisationUnit = padOrganisationRole.getOrganisationUnit();
    });
  }

  public HuooType getHuooType() {
    return huooType;
  }

  public TreatyAgreement getTreatyAgreement() {
    return treatyAgreement;
  }

  public Set<HuooRole> getRoles() {
    return roles;
  }

  public PwaApplicationDetail getPwaApplicationDetail() {
    return pwaApplicationDetail;
  }

  public PortalOrganisationUnit getPortalOrganisationUnit() {
    return portalOrganisationUnit;
  }
}
