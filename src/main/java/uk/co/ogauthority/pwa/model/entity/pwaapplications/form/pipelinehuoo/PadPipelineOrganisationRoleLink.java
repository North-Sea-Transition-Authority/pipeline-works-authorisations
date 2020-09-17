package uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinehuoo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.apache.commons.lang3.ObjectUtils;
import uk.co.ogauthority.pwa.model.dto.pipelines.IdentLocationInclusionMode;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineIdentifier;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineIdentifierVisitor;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineSegment;
import uk.co.ogauthority.pwa.model.entity.enums.pipelinehuoo.OrgRoleInstanceType;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.PadOrganisationRole;
import uk.co.ogauthority.pwa.service.entitycopier.ChildEntity;

@Entity
@Table(name = "pad_pipeline_org_role_links")
public class PadPipelineOrganisationRoleLink implements PipelineIdentifierVisitor, ChildEntity<Integer, PadOrganisationRole> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne()
  @JoinColumn(name = "pipeline_id")
  private Pipeline pipeline;

  @ManyToOne
  @JoinColumn(name = "pad_org_role_id")
  private PadOrganisationRole padOrgRole;

  private String fromLocation;

  @Column(name = "from_location_mode")
  @Enumerated(EnumType.STRING)
  private IdentLocationInclusionMode fromLocationIdentInclusionMode;

  private String toLocation;

  @Column(name = "to_location_mode")
  @Enumerated(EnumType.STRING)
  private IdentLocationInclusionMode toLocationIdentInclusionMode;

  public PadPipelineOrganisationRoleLink() {
  }

  public PadPipelineOrganisationRoleLink(PadOrganisationRole padOrganisationRole, Pipeline pipeline) {
    this.pipeline = pipeline;
    this.padOrgRole = padOrganisationRole;
  }


  //ChildEntity methods
  @Override
  public void clearId() {
    this.id = null;
  }

  @Override
  public void setParent(PadOrganisationRole parentEntity) {
    this.padOrgRole = parentEntity;
  }

  @Override
  public PadOrganisationRole getParent() {
    return this.padOrgRole;
  }

  public Integer getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Pipeline getPipeline() {
    return pipeline;
  }

  public void setPipeline(Pipeline pipeline) {
    this.pipeline = pipeline;
  }

  public PadOrganisationRole getPadOrgRole() {
    return padOrgRole;
  }

  public void setPadOrgRole(PadOrganisationRole padOrgRole) {
    this.padOrgRole = padOrgRole;
  }

  public String getFromLocation() {
    return fromLocation;
  }

  public void setFromLocation(String fromLocation) {
    this.fromLocation = fromLocation;
  }

  public IdentLocationInclusionMode getFromLocationIdentInclusionMode() {
    return fromLocationIdentInclusionMode;
  }

  public void setFromLocationIdentInclusionMode(
      IdentLocationInclusionMode fromLocationIdentInclusionMode) {
    this.fromLocationIdentInclusionMode = fromLocationIdentInclusionMode;
  }

  public String getToLocation() {
    return toLocation;
  }

  public void setToLocation(String toLocation) {
    this.toLocation = toLocation;
  }

  public IdentLocationInclusionMode getToLocationIdentInclusionMode() {
    return toLocationIdentInclusionMode;
  }

  public void setToLocationIdentInclusionMode(
      IdentLocationInclusionMode toLocationIdentInclusionMode) {
    this.toLocationIdentInclusionMode = toLocationIdentInclusionMode;
  }

  public OrgRoleInstanceType getOrgRoleInstanceType() {
    if (ObjectUtils.allNotNull(
        this.fromLocation, this.fromLocationIdentInclusionMode, this.toLocation, this.toLocationIdentInclusionMode)
    ) {
      return OrgRoleInstanceType.SPLIT_PIPELINE;
    }

    return OrgRoleInstanceType.FULL_PIPELINE;
  }

  public PipelineIdentifier getPipelineIdentifier() {
    if (this.getOrgRoleInstanceType().equals(OrgRoleInstanceType.SPLIT_PIPELINE)) {

      return PipelineSegment.from(
          this.pipeline.getId(),
          this.fromLocation,
          this.fromLocationIdentInclusionMode,
          this.toLocation,
          this.toLocationIdentInclusionMode
      );

    }

    return this.pipeline.getPipelineId();
  }

  @Override
  public void visit(PipelineId pipelineId) {
    // TODO revisit if time, this might be shit.
    this.pipeline = new Pipeline();
    this.pipeline.setId(pipelineId.getPipelineIdAsInt());
    this.fromLocation = null;
    this.fromLocationIdentInclusionMode = null;
    this.toLocation = null;
    this.toLocationIdentInclusionMode = null;
  }

  @Override
  public void visit(PipelineSegment pipelineSegment) {
    // TODO revisit if time, this might be shit.
    this.pipeline = new Pipeline();
    this.pipeline.setId(pipelineSegment.getPipelineIdAsInt());
    this.fromLocation = pipelineSegment.getFromPoint().getLocationName();
    this.fromLocationIdentInclusionMode = pipelineSegment.getFromPointMode();
    this.toLocation = pipelineSegment.getToPoint().getLocationName();
    this.toLocationIdentInclusionMode = pipelineSegment.getToPointMode();
  }
}
