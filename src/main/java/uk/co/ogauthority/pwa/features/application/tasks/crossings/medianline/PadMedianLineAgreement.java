package uk.co.ogauthority.pwa.features.application.tasks.crossings.medianline;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.entitycopier.ChildEntity;

@Entity(name = "pad_median_line_agreements")
public class PadMedianLineAgreement implements ChildEntity<Integer, PwaApplicationDetail> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @OneToOne
  @JoinColumn(name = "application_detail_id")
  private PwaApplicationDetail pwaApplicationDetail;

  @Enumerated(EnumType.STRING)
  private MedianLineStatus agreementStatus;

  //ChildEntity methods
  @Override
  public void clearId() {
    this.id = null;
  }

  @Override
  public void setParent(PwaApplicationDetail parentEntity) {
    this.pwaApplicationDetail = parentEntity;
  }

  @Override
  public PwaApplicationDetail getParent() {
    return this.pwaApplicationDetail;
  }

  private String negotiatorName;
  private String negotiatorEmail;

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

  public MedianLineStatus getAgreementStatus() {
    return agreementStatus;
  }

  public void setAgreementStatus(MedianLineStatus agreementStatus) {
    this.agreementStatus = agreementStatus;
  }

  public String getNegotiatorName() {
    return negotiatorName;
  }

  public void setNegotiatorName(String negotiatorName) {
    this.negotiatorName = negotiatorName;
  }

  public String getNegotiatorEmail() {
    return negotiatorEmail;
  }

  public void setNegotiatorEmail(String negotiatorEmail) {
    this.negotiatorEmail = negotiatorEmail;
  }
}
