package uk.co.ogauthority.pwa.features.application.tasks.generaltech;


import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.entitycopier.ChildEntity;

@Entity
@Table(name = "pad_pipeline_tech_info")
public class PadPipelineTechInfo implements ChildEntity<Integer, PwaApplicationDetail> {

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

  //ChildEntity methods
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


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PadPipelineTechInfo that = (PadPipelineTechInfo) o;
    return Objects.equals(id, that.id)
        && Objects.equals(pwaApplicationDetail, that.pwaApplicationDetail)
        && Objects.equals(estimatedFieldLife, that.estimatedFieldLife)
        && Objects.equals(pipelineDesignedToStandards, that.pipelineDesignedToStandards)
        && Objects.equals(pipelineStandardsDescription, that.pipelineStandardsDescription)
        && Objects.equals(corrosionDescription, that.corrosionDescription)
        && Objects.equals(plannedPipelineTieInPoints, that.plannedPipelineTieInPoints)
        && Objects.equals(tieInPointsDescription, that.tieInPointsDescription);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, pwaApplicationDetail, estimatedFieldLife, pipelineDesignedToStandards,
        pipelineStandardsDescription, corrosionDescription, plannedPipelineTieInPoints, tieInPointsDescription);
  }
}
