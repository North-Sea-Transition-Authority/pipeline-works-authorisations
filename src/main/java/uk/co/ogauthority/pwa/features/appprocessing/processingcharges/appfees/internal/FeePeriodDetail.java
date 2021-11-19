package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.internal;

import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

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

}
