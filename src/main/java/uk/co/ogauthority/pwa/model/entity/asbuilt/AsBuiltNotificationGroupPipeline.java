package uk.co.ogauthority.pwa.model.entity.asbuilt;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineDetailId;
import uk.co.ogauthority.pwa.model.entity.converters.PipelineDetailIdConverter;

@Entity
@Table(name = "as_built_notif_grp_pipelines")
public class AsBuiltNotificationGroupPipeline {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "asBuiltNotificationGroupId")
  private AsBuiltNotificationGroup asBuiltNotificationGroup;

  // not mapped entity yet. having that association will lead to hugely expanded entity graph that we probably dont need or care about.
  @Basic
  @Convert(converter = PipelineDetailIdConverter.class)
  @Column(name = "pipeline_detail_id")
  private PipelineDetailId pipelineDetailId;

  @Enumerated(EnumType.STRING)
  private PipelineChangeCategory pipelineChangeCategory;


  public AsBuiltNotificationGroupPipeline() {
    // hibernate
  }

  public AsBuiltNotificationGroupPipeline(AsBuiltNotificationGroup asBuiltNotificationGroup,
                                          PipelineDetailId pipelineDetailId,
                                          PipelineChangeCategory pipelineChangeCategory) {
    this.asBuiltNotificationGroup = asBuiltNotificationGroup;
    this.pipelineDetailId = pipelineDetailId;
    this.pipelineChangeCategory = pipelineChangeCategory;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public AsBuiltNotificationGroup getAsBuiltNotificationGroup() {
    return asBuiltNotificationGroup;
  }

  public void setAsBuiltNotificationGroup(
      AsBuiltNotificationGroup asBuiltNotificationGroup) {
    this.asBuiltNotificationGroup = asBuiltNotificationGroup;
  }

  public PipelineDetailId getPipelineDetailId() {
    return pipelineDetailId;
  }

  public void setPipelineDetailId(PipelineDetailId pipelineDetailId) {
    this.pipelineDetailId = pipelineDetailId;
  }

  public PipelineChangeCategory getPipelineChangeCategory() {
    return pipelineChangeCategory;
  }

  public void setPipelineChangeCategory(
      PipelineChangeCategory pipelineChangeCategory) {
    this.pipelineChangeCategory = pipelineChangeCategory;
  }
}
