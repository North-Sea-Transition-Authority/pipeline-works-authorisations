package uk.co.ogauthority.pwa.model.entity.search.consents;

import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.Immutable;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;

@Entity
@Table(name = "vw_consent_search")
@Immutable
public class ConsentSearchItem {

  @Id
  private Integer pwaId;

  @Column(name = "reference")
  private String pwaReference;

  @Column(name = "field_or_other_ref_csv")
  private String fieldNameOrOtherReference;

  @Enumerated(EnumType.STRING)
  private PwaResourceType resourceType;

  private String holderNamesCsv;

  private Instant firstConsentTimestamp;

  @Column(name = "latest_consent_ref")
  private String latestConsentReference;

  private Instant latestConsentTimestamp;

  public Integer getPwaId() {
    return pwaId;
  }

  public void setPwaId(Integer pwaId) {
    this.pwaId = pwaId;
  }

  public String getPwaReference() {
    return pwaReference;
  }

  public void setPwaReference(String pwaReference) {
    this.pwaReference = pwaReference;
  }

  public PwaResourceType getResourceType() {
    return resourceType;
  }

  public void setResourceType(PwaResourceType resourceType) {
    this.resourceType = resourceType;
  }

  public String getFieldNameOrOtherReference() {
    return fieldNameOrOtherReference;
  }

  public void setFieldNameOrOtherReference(String fieldNameOrOtherReference) {
    this.fieldNameOrOtherReference = fieldNameOrOtherReference;
  }

  public String getHolderNamesCsv() {
    return holderNamesCsv;
  }

  public void setHolderNamesCsv(String holderNamesCsv) {
    this.holderNamesCsv = holderNamesCsv;
  }

  public Instant getFirstConsentTimestamp() {
    return firstConsentTimestamp;
  }

  public void setFirstConsentTimestamp(Instant firstConsentTimestamp) {
    this.firstConsentTimestamp = firstConsentTimestamp;
  }

  public String getLatestConsentReference() {
    return latestConsentReference;
  }

  public void setLatestConsentReference(String latestConsentReference) {
    this.latestConsentReference = latestConsentReference;
  }

  public Instant getLatestConsentTimestamp() {
    return latestConsentTimestamp;
  }

  public void setLatestConsentTimestamp(Instant latestConsentTimestamp) {
    this.latestConsentTimestamp = latestConsentTimestamp;
  }
}
