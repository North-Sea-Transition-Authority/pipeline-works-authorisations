package uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdepositdrawings;

import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadPermanentDeposit;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "pad_deposit_drawing_links")
public class PadDepositDrawingLink {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @NotNull
  @OneToOne
  @JoinColumn(name = "permanent_deposit_info_id")
  private PadPermanentDeposit padPermanentDeposit;

  @NotNull
  @OneToOne
  @JoinColumn(name = "pad_deposit_drawing_id")
  private PadDepositDrawing padDepositDrawingId;

  public PadDepositDrawingLink() {
  }

  public PadDepositDrawingLink(@NotNull PadPermanentDeposit padPermanentDeposit, @NotNull PadDepositDrawing padDepositDrawingId) {
    this.padPermanentDeposit = padPermanentDeposit;
    this.padDepositDrawingId = padDepositDrawingId;
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

  public PadDepositDrawing getPadDepositDrawingId() {
    return padDepositDrawingId;
  }

  public void setPadDepositDrawingId(PadDepositDrawing padDepositDrawingId) {
    this.padDepositDrawingId = padDepositDrawingId;
  }
}
