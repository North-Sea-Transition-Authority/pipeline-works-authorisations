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
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.service.entitycopier.ChildEntity;

@Entity
@Table(name = "pad_deposit_pipelines")
public class PadDepositPipeline implements ChildEntity<Integer, PadPermanentDeposit> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @NotNull
  @OneToOne
  @JoinColumn(name = "permanent_deposit_info_id")
  private PadPermanentDeposit padPermanentDeposit;

  @NotNull
  @ManyToOne
  @JoinColumn(name = "pipeline_id")
  private Pipeline pipeline;

  public PadDepositPipeline() {
  }

  public PadDepositPipeline(PadPermanentDeposit padPermanentDeposit, Pipeline pipeline) {
    this.padPermanentDeposit = padPermanentDeposit;
    this.pipeline = pipeline;
  }

  //ChildEntity
  @Override
  public void clearId() {
    this.id = null;
  }

  @Override
  public void setParent(PadPermanentDeposit parentEntity) {
    setPadPermanentDeposit(parentEntity);
  }

  @Override
  public PadPermanentDeposit getParent() {
    return getPadPermanentDeposit();
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

  public void setPadPermanentDeposit(PadPermanentDeposit permanentDepositInfo) {
    this.padPermanentDeposit = permanentDepositInfo;
  }

  public Pipeline getPipeline() {
    return pipeline;
  }

  public void setPipeline(Pipeline pipeline) {
    this.pipeline = pipeline;
  }
}
