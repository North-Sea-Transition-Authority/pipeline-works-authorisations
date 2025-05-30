package uk.co.ogauthority.pwa.model.entity.pwaapplications.search;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.Immutable;

/**
 * Basic mapped view which provides values that can be cross-checked with pwa_application_details to retrieve desired application details
 * e.g. last submitted, last accepted or latest draft.
 */
@Entity
@Table(name = "pad_status_versions")
@Immutable
public class PadVersionLookup {

  @Id
  private Integer pwaApplicationId;

  @Column(name = "latest_submission_ts")
  private Instant latestSubmittedTimestamp;

  @Column(name = "latest_satisfactory_ts")
  private Instant latestConfirmedSatisfactoryTimestamp;

  @Column(name = "latest_submission_v_no")
  private Integer latestSubmittedVersionNo;

  @Column(name = "latest_satisfactory_v_no")
  private Integer latestConfirmedSatisfactoryVersionNo;

  @Column(name = "latest_draft_v_no")
  private Integer maxDraftVersionNo;

  public Integer getPwaApplicationId() {
    return pwaApplicationId;
  }

  public void setPwaApplicationId(Integer pwaApplicationId) {
    this.pwaApplicationId = pwaApplicationId;
  }

  public Instant getLatestSubmittedTimestamp() {
    return latestSubmittedTimestamp;
  }

  public void setLatestSubmittedTimestamp(Instant latestSubmittedTimestamp) {
    this.latestSubmittedTimestamp = latestSubmittedTimestamp;
  }

  public Instant getLatestConfirmedSatisfactoryTimestamp() {
    return latestConfirmedSatisfactoryTimestamp;
  }

  public void setLatestConfirmedSatisfactoryTimestamp(Instant latestConfirmedSatisfactoryTimestamp) {
    this.latestConfirmedSatisfactoryTimestamp = latestConfirmedSatisfactoryTimestamp;
  }

  public Integer getMaxDraftVersionNo() {
    return maxDraftVersionNo;
  }

  public void setMaxDraftVersionNo(Integer maxDraftVersionNo) {
    this.maxDraftVersionNo = maxDraftVersionNo;
  }

  public Integer getLatestSubmittedVersionNo() {
    return latestSubmittedVersionNo;
  }

  public void setLatestSubmittedVersionNo(Integer latestSubmittedVersionNo) {
    this.latestSubmittedVersionNo = latestSubmittedVersionNo;
  }

  public Integer getLatestConfirmedSatisfactoryVersionNo() {
    return latestConfirmedSatisfactoryVersionNo;
  }

  public void setLatestConfirmedSatisfactoryVersionNo(Integer latestConfirmedSatisfactoryVersionNo) {
    this.latestConfirmedSatisfactoryVersionNo = latestConfirmedSatisfactoryVersionNo;
  }
}
