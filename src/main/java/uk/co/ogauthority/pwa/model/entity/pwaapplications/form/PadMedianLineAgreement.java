package uk.co.ogauthority.pwa.model.entity.pwaapplications.form;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import uk.co.ogauthority.pwa.model.entity.enums.MedianLineStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Entity(name = "pad_median_line_agreements")
public class PadMedianLineAgreement {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @OneToOne
  @JoinColumn(name = "application_detail_id")
  private PwaApplicationDetail pwaApplicationDetail;

  @Enumerated(EnumType.STRING)
  private MedianLineStatus agreementStatus;

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
