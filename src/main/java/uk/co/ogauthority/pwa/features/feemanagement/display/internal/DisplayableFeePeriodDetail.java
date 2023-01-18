package uk.co.ogauthority.pwa.features.feemanagement.display.internal;

import static uk.co.ogauthority.pwa.features.feemanagement.display.DisplayableFeePeriodStatus.ACTIVE;
import static uk.co.ogauthority.pwa.features.feemanagement.display.DisplayableFeePeriodStatus.COMPLETE;
import static uk.co.ogauthority.pwa.features.feemanagement.display.DisplayableFeePeriodStatus.PENDING;

import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.Immutable;
import uk.co.ogauthority.pwa.features.feemanagement.display.DisplayableFeePeriodStatus;
import uk.co.ogauthority.pwa.util.DateUtils;

@Entity
@Immutable
@Table(name = "vw_fee_info")
public class DisplayableFeePeriodDetail {

  @Id
  private Integer feePeriodId;

  private String description;

  private Instant periodStartTimestamp;

  private Instant periodEndTimestamp;

  public Integer getFeePeriodId() {
    return feePeriodId;
  }

  public void setFeePeriodId(Integer id) {
    this.feePeriodId = id;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Instant getPeriodStartTimestamp() {
    return periodStartTimestamp;
  }

  public String getPeriodStartDisplayTime() {
    return DateUtils.formatDateTime(periodStartTimestamp);
  }

  public void setPeriodStartTimestamp(Instant periodStartTimestamp) {
    this.periodStartTimestamp = periodStartTimestamp;
  }

  public Instant getPeriodEndTimestamp() {
    return periodEndTimestamp;
  }

  public String getPeriodEndDisplayTime() {
    if (periodEndTimestamp != null) {
      return DateUtils.formatDateTime(periodEndTimestamp);
    }
    return "";
  }

  public void setPeriodEndTimestamp(Instant periodEndTimestamp) {
    this.periodEndTimestamp = periodEndTimestamp;
  }

  public DisplayableFeePeriodStatus getStatus() {
    if (getPeriodStartTimestamp() != null && getPeriodStartTimestamp().isAfter(Instant.now())) {
      return PENDING;
    } else if (getPeriodEndTimestamp() == null || getPeriodEndTimestamp().isAfter(Instant.now())) {
      return ACTIVE;
    } else {
      return COMPLETE;
    }
  }
}
