package uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo;

import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.model.entity.converters.HuooRoleConverter;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;
import uk.co.ogauthority.pwa.model.entity.enums.TreatyAgreement;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Entity(name = "pad_organisation_roles")
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

  @Column(name = "role")
  @Convert(converter = HuooRoleConverter.class)
  private Set<HuooRole> roles;

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

  public void setPwaApplicationDetail(
      PwaApplicationDetail pwaApplicationDetail) {
    this.pwaApplicationDetail = pwaApplicationDetail;
  }

  public PortalOrganisationUnit getOrganisationUnit() {
    return organisationUnit;
  }

  public void setOrganisationUnit(
      PortalOrganisationUnit organisationUnit) {
    this.organisationUnit = organisationUnit;
  }

  public Set<HuooRole> getRoles() {
    return roles;
  }

  public void setRoles(Set<HuooRole> roles) {
    this.roles = roles;
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
