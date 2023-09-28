package uk.co.ogauthority.pwa.model.view.search.consents;

import java.time.Instant;
import java.util.Objects;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.model.entity.search.consents.ConsentSearchItem;
import uk.co.ogauthority.pwa.util.DateUtils;

public class ConsentSearchResultView {

  private final Integer pwaId;

  private final String pwaReference;

  private final String resourceType;

  private final String fieldNameOrOtherReference;

  private final String holderNamesCsv;

  private final String firstConsentTimestampDisplay;

  private final String latestConsentReference;

  private final String latestConsentTimestampDisplay;

  public ConsentSearchResultView(Integer pwaId,
                                 String pwaReference,
                                 PwaResourceType resourceType, String fieldNameOrOtherReference,
                                 String holderNamesCsv,
                                 Instant firstConsentTimestamp,
                                 String latestConsentReference,
                                 Instant latestConsentTimestamp) {
    this.pwaId = pwaId;
    this.pwaReference = pwaReference;
    this.resourceType = resourceType.getDisplayName();
    this.fieldNameOrOtherReference = fieldNameOrOtherReference;
    this.holderNamesCsv = holderNamesCsv;
    this.firstConsentTimestampDisplay = DateUtils.formatDate(firstConsentTimestamp);
    this.latestConsentReference = latestConsentReference;
    this.latestConsentTimestampDisplay = DateUtils.formatDate(latestConsentTimestamp);
  }

  public static ConsentSearchResultView fromSearchItem(ConsentSearchItem consentSearchItem) {

    return new ConsentSearchResultView(
        consentSearchItem.getPwaId(),
        consentSearchItem.getPwaReference(),
        consentSearchItem.getResourceType(),
        consentSearchItem.getFieldNameOrOtherReference(),
        consentSearchItem.getHolderNamesCsv(),
        consentSearchItem.getFirstConsentTimestamp(),
        consentSearchItem.getLatestConsentReference(),
        consentSearchItem.getLatestConsentTimestamp()
    );

  }

  public Integer getPwaId() {
    return pwaId;
  }

  public String getPwaReference() {
    return pwaReference;
  }

  public String getResourceType() {
    return resourceType;
  }

  public String getFieldNameOrOtherReference() {
    return fieldNameOrOtherReference;
  }

  public String getHolderNamesCsv() {
    return holderNamesCsv;
  }

  public String getFirstConsentTimestampDisplay() {
    return firstConsentTimestampDisplay;
  }

  public String getLatestConsentReference() {
    return latestConsentReference;
  }

  public String getLatestConsentTimestampDisplay() {
    return latestConsentTimestampDisplay;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConsentSearchResultView that = (ConsentSearchResultView) o;
    return Objects.equals(pwaId, that.pwaId) && Objects.equals(pwaReference,
        that.pwaReference) && Objects.equals(fieldNameOrOtherReference,
        that.fieldNameOrOtherReference) && Objects.equals(holderNamesCsv,
        that.holderNamesCsv) && Objects.equals(firstConsentTimestampDisplay,
        that.firstConsentTimestampDisplay) && Objects.equals(latestConsentReference,
        that.latestConsentReference) && Objects.equals(latestConsentTimestampDisplay,
        that.latestConsentTimestampDisplay);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pwaId, pwaReference, fieldNameOrOtherReference, holderNamesCsv, firstConsentTimestampDisplay,
        latestConsentReference, latestConsentTimestampDisplay);
  }
}
