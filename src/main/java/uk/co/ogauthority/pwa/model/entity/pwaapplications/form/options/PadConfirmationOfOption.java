package uk.co.ogauthority.pwa.model.entity.pwaapplications.form.options;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.model.entity.enums.ConfirmedOptionType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Entity
@Table(name = "pad_confirmation_of_option")
public class PadConfirmationOfOption {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @JoinColumn(name = "application_detail_id")
  @ManyToOne
  private PwaApplicationDetail pwaApplicationDetail;

  @Enumerated(EnumType.STRING)
  @Column(name = "confirmation_type")
  private ConfirmedOptionType confirmedOptionType;

  public PadConfirmationOfOption() {
    // hibernate
  }

  public PadConfirmationOfOption(PwaApplicationDetail pwaApplicationDetail) {
    this.pwaApplicationDetail = pwaApplicationDetail;
  }

  private String chosenOptionDesc;

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

  public ConfirmedOptionType getConfirmedOptionType() {
    return confirmedOptionType;
  }

  public void setConfirmedOptionType(ConfirmedOptionType confirmedOptionType) {
    this.confirmedOptionType = confirmedOptionType;
  }

  public String getChosenOptionDesc() {
    return chosenOptionDesc;
  }

  public void setChosenOptionDesc(String chosenOptionDesc) {
    this.chosenOptionDesc = chosenOptionDesc;
  }
}
