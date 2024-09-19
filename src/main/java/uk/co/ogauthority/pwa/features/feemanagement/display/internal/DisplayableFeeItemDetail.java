package uk.co.ogauthority.pwa.features.feemanagement.display.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.data.annotation.Immutable;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.PwaApplicationFeeType;
import uk.co.ogauthority.pwa.util.CurrencyUtils;

@Entity
@Immutable
@Table(name = "vw_fee_items")
public class DisplayableFeeItemDetail {

  @Id
  private Integer feePeriodDetailItemId;

  @Column(name = "fee_period_id")
  private Integer feePeriodId;

  @Column(name = "display_description")
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(name = "pwa_application_type")
  private PwaApplicationType applicationType;

  @Enumerated(EnumType.STRING)
  @Column(name = "pwa_application_fee_type")
  private PwaApplicationFeeType applicationFeeType;

  @Column(name = "penny_amount")
  private Integer pennyAmount;

  public Integer getFeePeriodDetailItemId() {
    return feePeriodDetailItemId;
  }

  public void setFeePeriodDetailItemId(Integer id) {
    this.feePeriodDetailItemId = id;
  }

  public Integer getFeePeriodId() {
    return feePeriodId;
  }

  public void setFeePeriodId(Integer feePeriodId) {
    this.feePeriodId = feePeriodId;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public PwaApplicationType getApplicationType() {
    return applicationType;
  }

  public void setApplicationType(PwaApplicationType applicationType) {
    this.applicationType = applicationType;
  }

  public PwaApplicationFeeType getApplicationFeeType() {
    return applicationFeeType;
  }

  public void setApplicationFeeType(
      PwaApplicationFeeType applicationFeeType) {
    this.applicationFeeType = applicationFeeType;
  }

  public Integer getPennyAmount() {
    return pennyAmount;
  }

  public String getCurrencyAmount() {
    return CurrencyUtils.pennyAmountToCurrency(pennyAmount);
  }

  public void setPennyAmount(Integer pennyAmount) {
    this.pennyAmount = pennyAmount;
  }
}
