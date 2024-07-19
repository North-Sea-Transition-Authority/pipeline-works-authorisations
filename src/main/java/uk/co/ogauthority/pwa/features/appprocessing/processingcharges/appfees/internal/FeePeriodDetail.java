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
@Table(name = "fee_period_details")
public class FeePeriodDetail {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(referencedColumnName = "id", name = "fee_period_id")
  private FeePeriod feePeriod;

  private Instant periodStartTimestamp;
  private Instant periodEndTimestamp;

  private Boolean tipFlag;

  @CreatedDate
  private Instant created;

  @LastModifiedDate
  @Column(name = "last_modified")
  private Instant modified;

  @Column(name = "last_modified_by_person_id")
  private Integer lastModifiedBy;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }


  public FeePeriod getFeePeriod() {
    return feePeriod;
  }

  public void setFeePeriod(FeePeriod feePeriodId) {
    this.feePeriod = feePeriodId;
  }


  public Instant getPeriodStartTimestamp() {
    return periodStartTimestamp;
  }

  public void setPeriodStartTimestamp(Instant periodStartTimestamp) {
    this.periodStartTimestamp = periodStartTimestamp;
  }


  public Instant getPeriodEndTimestamp() {
    return periodEndTimestamp;
  }

  public void setPeriodEndTimestamp(Instant periodEndTimestamp) {
    this.periodEndTimestamp = periodEndTimestamp;
  }


  public Boolean getTipFlag() {
    return tipFlag;
  }

  public void setTipFlag(Boolean tipFlag) {
    this.tipFlag = tipFlag;
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

  public Integer getLastModifiedBy() {
    return lastModifiedBy;
  }

  public void setLastModifiedBy(Integer lastModifiedBy) {
    this.lastModifiedBy = lastModifiedBy;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FeePeriodDetail that = (FeePeriodDetail) o;
    return feePeriod.equals(that.feePeriod) && Objects.equals(periodStartTimestamp,
        that.periodStartTimestamp) && Objects.equals(periodEndTimestamp,
        that.periodEndTimestamp) && tipFlag.equals(that.tipFlag);
  }

  @Override
  public int hashCode() {
    return Objects.hash(feePeriod, periodStartTimestamp, periodEndTimestamp, tipFlag);
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
