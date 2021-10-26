package uk.co.ogauthority.pwa.validators.huoo;

import java.util.Set;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooType;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.TreatyAgreement;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.PadOrganisationRole;

/**
 * Use when validating addition or edit of a single huoo.
 */
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
    var role = padOrganisationRoles.iterator().next();
    huooType = role.getType();
    pwaApplicationDetail = role.getPwaApplicationDetail();
    treatyAgreement = role.getAgreement();
    portalOrganisationUnit = role.getOrganisationUnit();
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
