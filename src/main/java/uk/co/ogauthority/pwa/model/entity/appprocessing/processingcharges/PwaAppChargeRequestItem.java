package uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "pwa_app_charge_request_items")
public class PwaAppChargeRequestItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(referencedColumnName = "id", name = "pwa_app_charge_request_id")
  private PwaAppChargeRequest pwaAppChargeRequest;

  private String description;
  private Integer pennyAmount;

  public PwaAppChargeRequestItem() {
    // default
  }

  public PwaAppChargeRequestItem(PwaAppChargeRequest pwaAppChargeRequest,
                                 String description,
                                 Integer pennyAmount) {
    this.pwaAppChargeRequest = pwaAppChargeRequest;
    this.description = description;
    this.pennyAmount = pennyAmount;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }


  public PwaAppChargeRequest getPwaAppChargeRequest() {
    return pwaAppChargeRequest;
  }

  public void setPwaAppChargeRequest(PwaAppChargeRequest pwaAppChargeRequestId) {
    this.pwaAppChargeRequest = pwaAppChargeRequestId;
  }


  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }


  public Integer getPennyAmount() {
    return pennyAmount;
  }

  public void setPennyAmount(Integer pennyAmount) {
    this.pennyAmount = pennyAmount;
  }

}
