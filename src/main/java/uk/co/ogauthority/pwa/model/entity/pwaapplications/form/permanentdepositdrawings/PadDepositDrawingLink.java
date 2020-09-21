package uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdepositdrawings;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadPermanentDeposit;
import uk.co.ogauthority.pwa.service.entitycopier.ChildEntity;

@Entity
@Table(name = "pad_deposit_drawing_links")
public class PadDepositDrawingLink implements ChildEntity<Integer, PadPermanentDeposit> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @NotNull
  @OneToOne
  @JoinColumn(name = "pad_permanent_deposit_id")
  private PadPermanentDeposit padPermanentDeposit;

  @NotNull
  @OneToOne
  @JoinColumn(name = "pad_deposit_drawing_id")
  private PadDepositDrawing padDepositDrawing;

  public PadDepositDrawingLink() {
  }

  public PadDepositDrawingLink(@NotNull PadPermanentDeposit padPermanentDeposit, @NotNull PadDepositDrawing padDepositDrawing) {
    this.padPermanentDeposit = padPermanentDeposit;
    this.padDepositDrawing = padDepositDrawing;
  }

  @Override
  public void clearId() {
    this.id = null;
  }

  @Override
  public void setParent(PadPermanentDeposit parentEntity) {
    this.padPermanentDeposit = parentEntity;
  }

  @Override
  public PadPermanentDeposit getParent() {
    return this.getPadPermanentDeposit();
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public PadPermanentDeposit getPadPermanentDeposit() {
    return padPermanentDeposit;
  }

  public void setPadPermanentDeposit(PadPermanentDeposit padPermanentDeposit) {
    this.padPermanentDeposit = padPermanentDeposit;
  }

  public PadDepositDrawing getPadDepositDrawing() {
    return padDepositDrawing;
  }

  public void setPadDepositDrawing(PadDepositDrawing padDepositDrawing) {
    this.padDepositDrawing = padDepositDrawing;
  }
}
