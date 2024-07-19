package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Entity
@Table(name = "fee_period_detail_fee_items")
public class FeePeriodDetailFeeItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(referencedColumnName = "id", name = "fee_period_detail_id")
  private FeePeriodDetail feePeriodDetail;

  @ManyToOne
  @JoinColumn(referencedColumnName = "id", name = "fee_item_id")
  private FeeItem feeItem;

  private Integer pennyAmount;

  @CreatedDate
  private Instant created;

  @LastModifiedDate
  @Column(name = "last_modified")
  private Instant modified;


  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }


  public FeePeriodDetail getFeePeriodDetail() {
    return feePeriodDetail;
  }

  public void setFeePeriodDetail(FeePeriodDetail feePeriodDetailId) {
    this.feePeriodDetail = feePeriodDetailId;
  }


  public FeeItem getFeeItem() {
    return feeItem;
  }

  public void setFeeItem(FeeItem feeItem) {
    this.feeItem = feeItem;
  }


  public Integer getPennyAmount() {
    return pennyAmount;
  }

  public void setPennyAmount(Integer pennyAmount) {
    this.pennyAmount = pennyAmount;
  }

  public Instant getCreated() {
    return created;
  }

  public void setCreated(Instant created) {
    this.created = created;
  }

  public Instant getModified() {
    return modified;
  }

  public void setModified(Instant modified) {
    this.modified = modified;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FeePeriodDetailFeeItem that = (FeePeriodDetailFeeItem) o;
    return Objects.equals(feePeriodDetail, that.feePeriodDetail) && Objects.equals(feeItem,
        that.feeItem) && Objects.equals(pennyAmount, that.pennyAmount);
  }

  @Override
  public int hashCode() {
    return Objects.hash(feePeriodDetail, feeItem, pennyAmount);
  }

  @PrePersist
  @PreUpdate
  private void updateTimeStamps() {
    if (created == null) {
      created = Instant.now();
    }
    modified = Instant.now();
  }
}
