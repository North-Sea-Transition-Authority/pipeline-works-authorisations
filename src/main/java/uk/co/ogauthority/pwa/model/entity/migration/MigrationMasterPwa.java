package uk.co.ogauthority.pwa.model.entity.migration;

import com.google.common.annotations.VisibleForTesting;
import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "mig_master_pwas")
public class MigrationMasterPwa implements MigratablePwaConsent {

  @Id
  private int padId; // never null

  private int paId; // never null

  private Integer pappId; // potentially null

  private Integer firstPadId; // potentially null

  private Integer variationNumber; // potentially null

  @Column(name = "consent_date")
  private Instant consentInstant; // when null then not consented

  private String reference;

  public MigrationMasterPwa() {
  }

  @VisibleForTesting
  public MigrationMasterPwa(int padId,
                            int paId,
                            Integer pappId,
                            Integer firstPadId,
                            Integer variationNumber,
                            Instant consentInstant,
                            String reference) {
    this.padId = padId;
    this.paId = paId;
    this.pappId = pappId;
    this.firstPadId = firstPadId;
    this.variationNumber = variationNumber;
    this.consentInstant = consentInstant;
    this.reference = reference;
  }

  public int getPadId() {
    return padId;
  }

  public void setPadId(int padId) {
    this.padId = padId;
  }

  public int getPaId() {
    return paId;
  }

  public void setPaId(int paId) {
    this.paId = paId;
  }

  public Integer getPappId() {
    return pappId;
  }

  public void setPappId(Integer pappId) {
    this.pappId = pappId;
  }

  public Integer getFirstPadId() {
    return firstPadId;
  }

  public void setFirstPadId(Integer firstPadId) {
    this.firstPadId = firstPadId;
  }

  public Integer getVariationNumber() {
    return variationNumber;
  }

  public void setVariationNumber(Integer variationNumber) {
    this.variationNumber = variationNumber;
  }

  public Instant getConsentInstant() {
    return consentInstant;
  }

  public void setConsentInstant(Instant consentInstant) {
    this.consentInstant = consentInstant;
  }

  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  @Override
  public Instant getConsentedInstant() {
    return this.consentInstant;
  }

  @Override
  public String toString() {
    return "MigrationMasterPwa{" +
        "padId=" + padId +
        ", paId=" + paId +
        ", pappId=" + pappId +
        ", firstPadId=" + firstPadId +
        ", variationNumber='" + variationNumber + '\'' +
        ", consentInstant=" + consentInstant +
        ", reference='" + reference + '\'' +
        '}';
  }
}

