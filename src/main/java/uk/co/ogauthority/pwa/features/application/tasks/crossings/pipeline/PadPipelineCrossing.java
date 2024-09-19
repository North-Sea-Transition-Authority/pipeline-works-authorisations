package uk.co.ogauthority.pwa.features.application.tasks.crossings.pipeline;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.entitycopier.ChildEntity;
import uk.co.ogauthority.pwa.service.entitycopier.ParentEntity;

@Entity(name = "pad_pipeline_crossings")
public class PadPipelineCrossing implements ChildEntity<Integer, PwaApplicationDetail>, ParentEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "application_detail_id")
  private PwaApplicationDetail pwaApplicationDetail;

  private String pipelineCrossed;

  @Column(name = "pipeline_fully_owned_by_org")
  private Boolean pipelineFullyOwnedByOrganisation;

  //ParentEntity methods
  @Override
  public Object getIdAsParent() {
    return this.id;
  }

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

  public String getPipelineCrossed() {
    return pipelineCrossed;
  }

  public void setPipelineCrossed(String pipelineCrossed) {
    this.pipelineCrossed = pipelineCrossed;
  }

  public Boolean getPipelineFullyOwnedByOrganisation() {
    return pipelineFullyOwnedByOrganisation;
  }

  public void setPipelineFullyOwnedByOrganisation(Boolean pipelineFullyOwnedByOrganisation) {
    this.pipelineFullyOwnedByOrganisation = pipelineFullyOwnedByOrganisation;
  }
}
