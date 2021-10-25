package uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo;

import com.google.common.annotations.VisibleForTesting;
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
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooType;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.TreatyAgreement;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.entitycopier.ChildEntity;
import uk.co.ogauthority.pwa.service.entitycopier.ParentEntity;

@Entity
@Table(name = "pad_organisation_roles")
public class PadOrganisationRole implements ChildEntity<Integer, PwaApplicationDetail>, ParentEntity {

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

  public PadOrganisationRole() { }

  @VisibleForTesting
  public PadOrganisationRole(HuooRole role) {
    this.role = role;
  }

  // ChildEntity methods
  @Override
  public void clearId() {
    this.id = null;
  }

  @Override
  public void setParent(PwaApplicationDetail parentEntity) {
    this.pwaApplicationDetail = parentEntity;
  }

  @Override
  public PwaApplicationDetail getParent() {
    return this.pwaApplicationDetail;
  }

  //ParentEntity methods
  @Override
  public Object getIdAsParent() {
    return this.id;
  }

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

  public static PadOrganisationRole forUnassignedSplitPipeline(PwaApplicationDetail pwaApplicationDetail,
                                                       HuooRole huooRole) {
    var padOrganisationRole = new PadOrganisationRole();
    padOrganisationRole.setPwaApplicationDetail(pwaApplicationDetail);
    padOrganisationRole.setType(HuooType.UNASSIGNED_PIPELINE_SPLIT);
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
