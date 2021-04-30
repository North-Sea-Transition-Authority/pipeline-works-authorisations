
package uk.co.ogauthority.pwa.model.entity.pwaconsents;

import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;

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
}
