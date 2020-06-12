package uk.co.ogauthority.pwa.model.entity.pwaconsents;


import java.time.Instant;
import java.util.Optional;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;
import uk.co.ogauthority.pwa.model.entity.enums.TreatyAgreement;

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
}
