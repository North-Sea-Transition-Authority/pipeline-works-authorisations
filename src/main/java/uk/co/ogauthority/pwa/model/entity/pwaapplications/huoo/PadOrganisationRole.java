package uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo;

import java.util.Optional;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;
import uk.co.ogauthority.pwa.model.entity.enums.TreatyAgreement;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Entity
@Table(name = "pad_organisation_roles")
public class PadOrganisationRole {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @JoinColumn(name = "application_detail_id")
  @ManyToOne
  private PwaApplicationDetail pwaApplicationDetail;

  @JoinColumn(name = "ou_id")
  @ManyToOne
  private PortalOrganisationUnit organisationUnit;

  @Enumerated(EnumType.STRING)
  private HuooRole role;

  @Enumerated(EnumType.STRING)
  private HuooType type;

  @Enumerated(EnumType.STRING)
  private TreatyAgreement agreement;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public PwaApplicationDetail getPwaApplicationDetail() {
    return pwaApplicationDetail;
  }

  public static PadOrganisationRole fromOrganisationUnit(PwaApplicationDetail pwaApplicationDetail,
                                                         PortalOrganisationUnit portalOrganisationUnit,
                                                         HuooRole huooRole) {
    var padOrganisationRole = new PadOrganisationRole();
    padOrganisationRole.setOrganisationUnit(portalOrganisationUnit);
    padOrganisationRole.setPwaApplicationDetail(pwaApplicationDetail);
    padOrganisationRole.setType(HuooType.PORTAL_ORG);
    padOrganisationRole.setRole(huooRole);
    return padOrganisationRole;

  }

  public static PadOrganisationRole fromTreatyAgreement(PwaApplicationDetail pwaApplicationDetail,
                                                         TreatyAgreement treatyAgreement,
                                                         HuooRole huooRole) {
    var padOrganisationRole = new PadOrganisationRole();
    padOrganisationRole.setAgreement(treatyAgreement);
    padOrganisationRole.setPwaApplicationDetail(pwaApplicationDetail);
    padOrganisationRole.setType(HuooType.TREATY_AGREEMENT);
    padOrganisationRole.setRole(huooRole);
    return padOrganisationRole;

  }

  public void setPwaApplicationDetail(
      PwaApplicationDetail pwaApplicationDetail) {
    this.pwaApplicationDetail = pwaApplicationDetail;
  }

  public PortalOrganisationUnit getOrganisationUnit() {
    return organisationUnit;
  }

  public Optional<PortalOrganisationUnit> getOrganisationUnitOptional() {
    return Optional.ofNullable(this.organisationUnit);
  }

  public void setOrganisationUnit(
      PortalOrganisationUnit organisationUnit) {
    this.organisationUnit = organisationUnit;
  }

  public HuooRole getRole() {
    return role;
  }

  public void setRole(HuooRole role) {
    this.role = role;
  }

  public HuooType getType() {
    return type;
  }

  public void setType(HuooType type) {
    this.type = type;
  }

  public TreatyAgreement getAgreement() {
    return agreement;
  }

  public void setAgreement(TreatyAgreement agreement) {
    this.agreement = agreement;
  }
}
