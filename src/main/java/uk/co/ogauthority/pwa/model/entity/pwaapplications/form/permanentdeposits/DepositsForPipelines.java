package uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity(name = "deposits_for_pipelines")
public class DepositsForPipelines {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;
  private Integer padProjectInfoId;
  private Integer permanentDepositInfoId;
  private Integer padPipelineId;


  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Integer getPadProjectInfoId() {
    return padProjectInfoId;
  }

  public void setPadProjectInfoId(Integer padProjectInfoId) {
    this.padProjectInfoId = padProjectInfoId;
  }

  public Integer getPermanentDepositInfoId() {
    return permanentDepositInfoId;
  }

  public void setPermanentDepositInfoId(Integer permanentDepositInfoId) {
    this.permanentDepositInfoId = permanentDepositInfoId;
  }

  public Integer getPadPipelineId() {
    return padPipelineId;
  }

  public void setPadPipelineId(Integer padPipelineId) {
    this.padPipelineId = padPipelineId;
  }

}
