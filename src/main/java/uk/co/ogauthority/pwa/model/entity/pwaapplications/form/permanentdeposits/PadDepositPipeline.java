package uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;

@Entity
@Table(name = "pad_deposit_pipelines")
public class PadDepositPipeline {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @NotNull
  @OneToOne
  @JoinColumn(name = "permanent_deposit_info_id")
  private PadPermanentDeposit permanentDepositInfo;

  @NotNull
  @OneToOne
  @JoinColumn(name = "pad_pipeline_id")
  private PadPipeline padPipeline;

  public PadDepositPipeline() {
  }

  public PadDepositPipeline(@NotNull PadPermanentDeposit permanentDepositInfo, @NotNull PadPipeline padPipeline) {
    this.permanentDepositInfo = permanentDepositInfo;
    this.padPipeline = padPipeline;
  }


  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public PadPermanentDeposit getPermanentDepositInfo() {
    return permanentDepositInfo;
  }

  public void setPermanentDepositInfo(PadPermanentDeposit permanentDepositInfo) {
    this.permanentDepositInfo = permanentDepositInfo;
  }

  public PadPipeline getPadPipeline() {
    return padPipeline;
  }

  public void setPadPipeline(PadPipeline padPipelineId) {
    this.padPipeline = padPipelineId;
  }
}
