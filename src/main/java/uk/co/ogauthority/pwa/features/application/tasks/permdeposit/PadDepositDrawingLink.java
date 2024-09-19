package uk.co.ogauthority.pwa.features.application.tasks.permdeposit;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
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
  @ManyToOne
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PadDepositDrawingLink that = (PadDepositDrawingLink) o;
    return Objects.equals(id, that.id)
        && Objects.equals(padPermanentDeposit, that.padPermanentDeposit)
        && Objects.equals(padDepositDrawing, that.padDepositDrawing);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, padPermanentDeposit, padDepositDrawing);
  }
}
