
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
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;

@Entity
@Table(name = "pwa_consents")
@NamedEntityGraph(name = "PwaConsent.masterPwaAndSourceApplications",
    attributeNodes = {
        @NamedAttributeNode(PwaConsent_.MASTER_PWA),
        @NamedAttributeNode(PwaConsent_.SOURCE_PWA_APPLICATION)}
)
public class PwaConsent {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "pwa_consent_id_generator")
  @SequenceGenerator(name = "pwa_consent_id_generator", sequenceName = "pwa_consent_id_seq", allocationSize = 1)
  private int id;

  @ManyToOne
  @JoinColumn(name = "pwa_id", referencedColumnName = "id")
  private MasterPwa masterPwa;

  @OneToOne
  @JoinColumn(name = "source_pwa_application_id", referencedColumnName = "id")
  private PwaApplication sourcePwaApplication;

  @Column(name = "created_timestamp")
  private Instant createdInstant;

  @Column(name = "consent_timestamp")
  private Instant consentInstant;

  @Enumerated(EnumType.STRING)
  private PwaConsentType consentType;

  private Integer variationNumber;

  private String reference;

  private boolean isMigratedFlag;

  @Column(name = "docgen_run_id")
  private Long docgenRunId;

  public PwaConsent() {
    this.isMigratedFlag = false;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public MasterPwa getMasterPwa() {
    return masterPwa;
  }

  public void setMasterPwa(MasterPwa masterPwa) {
    this.masterPwa = masterPwa;
  }

  public PwaApplication getSourcePwaApplication() {
    return sourcePwaApplication;
  }

  public void setSourcePwaApplication(PwaApplication sourcePwaApplication) {
    this.sourcePwaApplication = sourcePwaApplication;
  }

  public Instant getCreatedInstant() {
    return createdInstant;
  }

  public void setCreatedInstant(Instant createdInstant) {
    this.createdInstant = createdInstant;
  }

  public PwaConsentType getConsentType() {
    return consentType;
  }

  public void setConsentType(PwaConsentType consentType) {
    this.consentType = consentType;
  }

  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  public boolean isMigratedFlag() {
    return isMigratedFlag;
  }

  public void setMigratedFlag(boolean migratedFlag) {
    isMigratedFlag = migratedFlag;
  }

  public Instant getConsentInstant() {
    return consentInstant;
  }

  public void setConsentInstant(Instant consentInstant) {
    this.consentInstant = consentInstant;
  }

  public Integer getVariationNumber() {
    return variationNumber;
  }

  public void setVariationNumber(Integer variationNumber) {
    this.variationNumber = variationNumber;
  }

  public Long getDocgenRunId() {
    return docgenRunId;
  }

  public void setDocgenRunId(Long docgenRunId) {
    this.docgenRunId = docgenRunId;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PwaConsent)) {
      return false;
    }
    PwaConsent that = (PwaConsent) o;
    return id == that.id
        && isMigratedFlag == that.isMigratedFlag
        && Objects.equals(masterPwa, that.masterPwa)
        && Objects.equals(sourcePwaApplication, that.sourcePwaApplication)
        && Objects.equals(createdInstant, that.createdInstant)
        && Objects.equals(consentInstant, that.consentInstant)
        && consentType == that.consentType
        && Objects.equals(variationNumber, that.variationNumber)
        && Objects.equals(reference, that.reference)
        && Objects.equals(docgenRunId, that.docgenRunId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, masterPwa, sourcePwaApplication, createdInstant, consentInstant, consentType,
        variationNumber,
        reference, isMigratedFlag, docgenRunId);
  }
}
