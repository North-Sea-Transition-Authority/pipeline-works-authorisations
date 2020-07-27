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
import uk.co.ogauthority.pwa.model.dto.pipelines.IdentLocationInclusionMode;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.PadOrganisationRole;

@Entity
@Table(name = "pad_pipeline_org_role_links")
public class PadPipelineOrganisationRoleLink {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

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

  public int getId() {
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
}
