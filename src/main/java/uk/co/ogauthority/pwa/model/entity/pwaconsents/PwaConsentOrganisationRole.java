package uk.co.ogauthority.pwa.model.entity.pwaconsents;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooType;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.TreatyAgreement;

@Entity
@Table(name = "pwa_consent_organisation_roles")
public class PwaConsentOrganisationRole {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "added_by_pwa_consent_id")
  private PwaConsent addedByPwaConsent;

  @ManyToOne
  @JoinColumn(name = "ended_by_pwa_consent_id")
  private PwaConsent endedByPwaConsent;

  @Enumerated(EnumType.STRING)
  private HuooRole role;

  @Enumerated(EnumType.STRING)
  private HuooType type;

  @Column(name = "ou_id")
  private Integer organisationUnitId;
  private String migratedOrganisationName;

  @Enumerated(EnumType.STRING)
  private TreatyAgreement agreement;

  private Instant startTimestamp;

  private Instant endTimestamp;

  public PwaConsentOrganisationRole() {
  }

  private PwaConsentOrganisationRole(PwaConsent addedByPwaConsent,
                                     HuooRole role,
                                     Integer organisationUnitId,
                                     Instant startTimestamp) {
    this.addedByPwaConsent = addedByPwaConsent;
    this.role = role;
    this.type = HuooType.PORTAL_ORG;
    this.organisationUnitId = organisationUnitId;
    this.startTimestamp = startTimestamp;
  }

  private PwaConsentOrganisationRole(PwaConsent addedByPwaConsent,
                                     HuooRole role,
                                     TreatyAgreement agreement,
                                     Instant startTimestamp) {
    this.addedByPwaConsent = addedByPwaConsent;
    this.role = role;
    this.type = HuooType.TREATY_AGREEMENT;
    this.agreement = agreement;
    this.startTimestamp = startTimestamp;
  }

  public static PwaConsentOrganisationRole createOrgUnitRole(PwaConsent addedByPwaConsent,
                                                             HuooRole role,
                                                             int organisationUnitId,
                                                             Instant startTimestamp) {
    return new PwaConsentOrganisationRole(addedByPwaConsent, role, organisationUnitId, startTimestamp);
  }

  public static PwaConsentOrganisationRole createTreatyAgreementRole(PwaConsent addedByPwaConsent,
                                                                     HuooRole role,
                                                                     TreatyAgreement agreement,
                                                                     Instant startTimestamp) {
    return new PwaConsentOrganisationRole(addedByPwaConsent, role, agreement, startTimestamp);
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public PwaConsent getAddedByPwaConsent() {
    return addedByPwaConsent;
  }

  public void setAddedByPwaConsent(PwaConsent addedByPwaConsent) {
    this.addedByPwaConsent = addedByPwaConsent;
  }

  public PwaConsent getEndedByPwaConsent() {
    return endedByPwaConsent;
  }

  public void setEndedByPwaConsent(PwaConsent endedByPwaConsent) {
    this.endedByPwaConsent = endedByPwaConsent;
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

  public Integer getOrganisationUnitId() {
    return organisationUnitId;
  }

  public void setOrganisationUnitId(Integer organisationUnitId) {
    this.organisationUnitId = organisationUnitId;
  }

  public String getMigratedOrganisationName() {
    return migratedOrganisationName;
  }

  public void setMigratedOrganisationName(String migratedOrganisationName) {
    this.migratedOrganisationName = migratedOrganisationName;
  }

  public TreatyAgreement getAgreement() {
    return agreement;
  }

  public void setAgreement(TreatyAgreement agreement) {
    this.agreement = agreement;
  }

  public Instant getStartTimestamp() {
    return startTimestamp;
  }

  public void setStartTimestamp(Instant startTimestamp) {
    this.startTimestamp = startTimestamp;
  }

  public Instant getEndTimestamp() {
    return endTimestamp;
  }

  public void setEndTimestamp(Instant endTimestamp) {
    this.endTimestamp = endTimestamp;
  }

  @Override
  public String toString() {
    return "PwaConsentOrganisationRole{" +
        "id=" + id +
        ", addedByPwaConsentId=" + addedByPwaConsent.getId() +
        ", endedByPwaConsentId=" + Optional.ofNullable(endedByPwaConsent).map(PwaConsent::getId).orElse(null) +
        ", role=" + role +
        ", type=" + type +
        ", organisationUnitId=" + organisationUnitId +
        ", migratedOrganisationName='" + migratedOrganisationName + '\'' +
        ", agreement=" + agreement +
        ", startTimestamp=" + startTimestamp +
        ", endTimestamp=" + endTimestamp +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PwaConsentOrganisationRole that = (PwaConsentOrganisationRole) o;
    return Objects.equals(id, that.id) && Objects.equals(addedByPwaConsent,
        that.addedByPwaConsent) && Objects.equals(endedByPwaConsent,
        that.endedByPwaConsent) && role == that.role && type == that.type && Objects.equals(organisationUnitId,
        that.organisationUnitId) && Objects.equals(migratedOrganisationName,
        that.migratedOrganisationName) && agreement == that.agreement && Objects.equals(startTimestamp,
        that.startTimestamp) && Objects.equals(endTimestamp, that.endTimestamp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, addedByPwaConsent, endedByPwaConsent, role, type, organisationUnitId,
        migratedOrganisationName, agreement, startTimestamp, endTimestamp);
  }

}
