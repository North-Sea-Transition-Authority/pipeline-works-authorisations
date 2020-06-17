package uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinetechinfo;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Entity
@Table(name = "pad_pipeline_tech_info")
public class PadPipelineTechInfo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @JoinColumn(name = "application_detail_id")
  @OneToOne
  private PwaApplicationDetail pwaApplicationDetail;

  private Integer estimatedFieldLife;
  private Boolean pipelineDesignedToStandards;
  private String pipelineStandardsDescription;
  private String corrosionDescription;
  private Boolean plannedPipelineTieInPoints;
  private String tieInPointsDescription;


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

  public Integer getEstimatedFieldLife() {
    return estimatedFieldLife;
  }

  public void setEstimatedFieldLife(Integer estimatedFieldLife) {
    this.estimatedFieldLife = estimatedFieldLife;
  }

  public Boolean getPipelineDesignedToStandards() {
    return pipelineDesignedToStandards;
  }

  public void setPipelineDesignedToStandards(Boolean pipelineDesignedToStandards) {
    this.pipelineDesignedToStandards = pipelineDesignedToStandards;
  }

  public String getPipelineStandardsDescription() {
    return pipelineStandardsDescription;
  }

  public void setPipelineStandardsDescription(String pipelineStandardsDescription) {
    this.pipelineStandardsDescription = pipelineStandardsDescription;
  }

  public String getCorrosionDescription() {
    return corrosionDescription;
  }

  public void setCorrosionDescription(String corrosionDescription) {
    this.corrosionDescription = corrosionDescription;
  }

  public Boolean getPlannedPipelineTieInPoints() {
    return plannedPipelineTieInPoints;
  }

  public void setPlannedPipelineTieInPoints(Boolean plannedPipelineTieInPoints) {
    this.plannedPipelineTieInPoints = plannedPipelineTieInPoints;
  }

  public String getTieInPointsDescription() {
    return tieInPointsDescription;
  }

  public void setTieInPointsDescription(String tieInPointsDescription) {
    this.tieInPointsDescription = tieInPointsDescription;
  }


}
