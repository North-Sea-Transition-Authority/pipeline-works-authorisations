package uk.co.ogauthority.pwa.features.appprocessing.tasks.initialreview;

import java.time.Instant;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Entity
@Table(name = "pad_initial_review")
public final class PadInitialReview {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "application_detail_id")
  private PwaApplicationDetail pwaApplicationDetail;

  @Column(name = "init_review_approved_by_wua_id")
  private Integer initialReviewApprovedByWuaId;

  @Column(name = "init_review_approved_timestamp")
  private Instant initialReviewApprovedTimestamp;

  private Integer approvalRevokedByWuaId;

  private Instant approvalRevokedTimestamp;


  public PadInitialReview() {}

  public PadInitialReview(PwaApplicationDetail pwaApplicationDetail, Integer initialReviewApprovedByWuaId,
                          Instant initialReviewApprovedTimestamp) {
    this.pwaApplicationDetail = pwaApplicationDetail;
    this.initialReviewApprovedByWuaId = initialReviewApprovedByWuaId;
    this.initialReviewApprovedTimestamp = initialReviewApprovedTimestamp;
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

  public void setPwaApplicationDetail(
      PwaApplicationDetail pwaApplicationDetail) {
    this.pwaApplicationDetail = pwaApplicationDetail;
  }

  public Integer getInitialReviewApprovedByWuaId() {
    return initialReviewApprovedByWuaId;
  }

  public void setInitialReviewApprovedByWuaId(Integer initialReviewApprovedByWuaId) {
    this.initialReviewApprovedByWuaId = initialReviewApprovedByWuaId;
  }

  public Instant getInitialReviewApprovedTimestamp() {
    return initialReviewApprovedTimestamp;
  }

  public void setInitialReviewApprovedTimestamp(Instant initialReviewApprovedTimestamp) {
    this.initialReviewApprovedTimestamp = initialReviewApprovedTimestamp;
  }

  public Integer getApprovalRevokedByWuaId() {
    return approvalRevokedByWuaId;
  }

  public void setApprovalRevokedByWuaId(Integer approvalRevokedByWuaId) {
    this.approvalRevokedByWuaId = approvalRevokedByWuaId;
  }

  public Instant getApprovalRevokedTimestamp() {
    return approvalRevokedTimestamp;
  }

  public void setApprovalRevokedTimestamp(Instant approvalRevokedTimestamp) {
    this.approvalRevokedTimestamp = approvalRevokedTimestamp;
  }

  public boolean isInitialReviewRevoked() {
    return approvalRevokedTimestamp != null;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PadInitialReview that = (PadInitialReview) o;
    return Objects.equals(id, that.id)
        && Objects.equals(pwaApplicationDetail, that.pwaApplicationDetail)
        && Objects.equals(initialReviewApprovedByWuaId, that.initialReviewApprovedByWuaId)
        && Objects.equals(initialReviewApprovedTimestamp, that.initialReviewApprovedTimestamp)
        && Objects.equals(approvalRevokedByWuaId, that.approvalRevokedByWuaId)
        && Objects.equals(approvalRevokedTimestamp, that.approvalRevokedTimestamp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, pwaApplicationDetail, initialReviewApprovedByWuaId, initialReviewApprovedTimestamp,
        approvalRevokedByWuaId, approvalRevokedTimestamp);
  }
}
