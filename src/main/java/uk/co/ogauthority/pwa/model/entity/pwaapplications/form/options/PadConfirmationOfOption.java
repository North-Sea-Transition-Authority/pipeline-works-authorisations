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
import uk.co.ogauthority.pwa.service.entitycopier.ChildEntity;

@Entity
@Table(name = "pad_confirmation_of_option")
public class PadConfirmationOfOption implements ChildEntity<Integer, PwaApplicationDetail> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @JoinColumn(name = "application_detail_id")
  @ManyToOne
  private PwaApplicationDetail pwaApplicationDetail;

  @Enumerated(EnumType.STRING)
  @Column(name = "confirmation_type")
  private ConfirmedOptionType confirmedOptionType;

  private String chosenOptionDesc;

  public PadConfirmationOfOption() {
    // hibernate
  }

  // ChildEntity methods
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

  // Getters/Setters
  public PadConfirmationOfOption(PwaApplicationDetail pwaApplicationDetail) {
    this.pwaApplicationDetail = pwaApplicationDetail;
  }

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
