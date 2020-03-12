package uk.co.ogauthority.pwa.model.entity.migration;

import com.google.common.annotations.VisibleForTesting;
import java.time.Instant;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "mig_pwa_consents")
public class MigrationPwaConsent implements MigratablePwaConsent {

  @Id
  private int padId; // never null

  private int paId; // never null

  private Integer pappId; // potentially null

  @ManyToOne
  @JoinColumn(name = "first_pad_id", referencedColumnName = "padId")
  private MigrationMasterPwa migrationMasterPwa; // potentially null

  private Integer variationNumber; // potentially null

  private Instant consentDate; // when null then not consented

  private String reference;

  public MigrationPwaConsent() {
  }

  @VisibleForTesting
  public MigrationPwaConsent(int padId,
                             int paId,
                             Integer pappId,
                             MigrationMasterPwa migrationMasterPwa,
                             Integer variationNumber,
                             Instant consentDate,
                             String reference) {
    this.padId = padId;
    this.paId = paId;
    this.pappId = pappId;
    this.migrationMasterPwa = migrationMasterPwa;
    this.variationNumber = variationNumber;
    this.consentDate = consentDate;
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

  public MigrationMasterPwa getMigrationMasterPwa() {
    return migrationMasterPwa;
  }

  public void setMigrationMasterPwa(MigrationMasterPwa migrationMasterPwa) {
    this.migrationMasterPwa = migrationMasterPwa;
  }

  public Integer getVariationNumber() {
    return variationNumber;
  }

  public void setVariationNumber(Integer variationNumber) {
    this.variationNumber = variationNumber;
  }

  public Instant getConsentDate() {
    return consentDate;
  }

  public void setConsentDate(Instant consentDate) {
    this.consentDate = consentDate;
  }

  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  @Override
  public Instant getConsentedInstant() {
    return this.consentDate;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MigrationPwaConsent that = (MigrationPwaConsent) o;
    return padId == that.padId
        && paId == that.paId
        && Objects.equals(pappId, that.pappId)
        && Objects.equals(migrationMasterPwa, that.migrationMasterPwa)
        && Objects.equals(variationNumber, that.variationNumber)
        && Objects.equals(consentDate, that.consentDate)
        && Objects.equals(reference, that.reference);
  }

  @Override
  public int hashCode() {
    return Objects.hash(padId, paId, pappId, migrationMasterPwa, variationNumber, consentDate, reference);
  }

  @Override
  public String toString() {
    return "MigrationPwaConsent{" +
        "padId=" + padId +
        ", paId=" + paId +
        ", pappId=" + pappId +
        ", migrationMasterPwa=" + migrationMasterPwa +
        ", variationNumber=" + variationNumber +
        ", consentDate=" + consentDate +
        ", reference='" + reference + '\'' +
        '}';
  }
}

