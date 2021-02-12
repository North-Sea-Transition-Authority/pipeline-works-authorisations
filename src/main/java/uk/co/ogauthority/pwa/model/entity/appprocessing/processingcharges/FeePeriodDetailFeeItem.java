package uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

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

}
