package uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinehuoo;

import java.util.Objects;
import java.util.Optional;
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
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.OrgRoleInstanceType;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.TreatyAgreement;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.IdentLocationInclusionMode;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdentifier;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdentifierVisitor;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineSection;
import uk.co.ogauthority.pwa.features.application.tasks.huoo.PadOrganisationRole;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.service.entitycopier.ChildEntity;

@Entity
@Table(name = "pad_pipeline_org_role_links")
public class PadPipelineOrganisationRoleLink implements PipelineIdentifierVisitor,
    ChildEntity<Integer, PadOrganisationRole>, PipelineOrganisationRoleLink {

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

  private Integer sectionNumber;

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

  @Override
  public HuooRole getRole() {
    return padOrgRole.getRole();
  }

  @Override
  public Optional<OrganisationUnitId> getOrgUnitId() {
    return Optional.ofNullable(padOrgRole.getOrganisationUnit())
        .map(orgUnit -> new OrganisationUnitId(orgUnit.getOuId()));
  }

  @Override
  public Optional<TreatyAgreement> getAgreement() {
    return Optional.ofNullable(padOrgRole.getAgreement());
  }

  @Override
  public String getFromLocation() {
    return fromLocation;
  }

  public void setFromLocation(String fromLocation) {
    this.fromLocation = fromLocation;
  }

  @Override
  public IdentLocationInclusionMode getFromLocationIdentInclusionMode() {
    return fromLocationIdentInclusionMode;
  }

  public void setFromLocationIdentInclusionMode(
      IdentLocationInclusionMode fromLocationIdentInclusionMode) {
    this.fromLocationIdentInclusionMode = fromLocationIdentInclusionMode;
  }

  @Override
  public String getToLocation() {
    return toLocation;
  }

  public void setToLocation(String toLocation) {
    this.toLocation = toLocation;
  }

  @Override
  public IdentLocationInclusionMode getToLocationIdentInclusionMode() {
    return toLocationIdentInclusionMode;
  }

  public void setToLocationIdentInclusionMode(
      IdentLocationInclusionMode toLocationIdentInclusionMode) {
    this.toLocationIdentInclusionMode = toLocationIdentInclusionMode;
  }

  @Override
  public Integer getSectionNumber() {
    return sectionNumber;
  }

  public void setSectionNumber(Integer sectionNumber) {
    this.sectionNumber = sectionNumber;
  }

  public OrgRoleInstanceType getOrgRoleInstanceType() {
    if (ObjectUtils.allNotNull(
        this.fromLocation, this.fromLocationIdentInclusionMode, this.toLocation, this.toLocationIdentInclusionMode, this.sectionNumber)
    ) {
      return OrgRoleInstanceType.SPLIT_PIPELINE;
    }

    return OrgRoleInstanceType.FULL_PIPELINE;
  }

  public PipelineIdentifier getPipelineIdentifier() {
    if (this.getOrgRoleInstanceType().equals(OrgRoleInstanceType.SPLIT_PIPELINE)) {

      return PipelineSection.from(
          this.pipeline.getId(),
          this.fromLocation,
          this.fromLocationIdentInclusionMode,
          this.toLocation,
          this.toLocationIdentInclusionMode,
          this.sectionNumber
      );

    }

    return this.pipeline.getPipelineId();
  }

  @Override
  public void visit(PipelineId pipelineId) {
    this.pipeline = new Pipeline();
    this.pipeline.setId(pipelineId.getPipelineIdAsInt());
    this.fromLocation = null;
    this.fromLocationIdentInclusionMode = null;
    this.toLocation = null;
    this.toLocationIdentInclusionMode = null;
    this.sectionNumber = null;
  }

  @Override
  public void visit(PipelineSection pipelineSection) {
    this.pipeline = new Pipeline();
    this.pipeline.setId(pipelineSection.getPipelineIdAsInt());
    this.fromLocation = pipelineSection.getFromPoint().getLocationName();
    this.fromLocationIdentInclusionMode = pipelineSection.getFromPointMode();
    this.toLocation = pipelineSection.getToPoint().getLocationName();
    this.toLocationIdentInclusionMode = pipelineSection.getToPointMode();
    this.sectionNumber = pipelineSection.getSectionNumber();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PadPipelineOrganisationRoleLink that = (PadPipelineOrganisationRoleLink) o;
    return Objects.equals(id, that.id)
        && Objects.equals(pipeline, that.pipeline)
        && Objects.equals(padOrgRole, that.padOrgRole)
        && Objects.equals(fromLocation, that.fromLocation)
        && fromLocationIdentInclusionMode == that.fromLocationIdentInclusionMode
        && Objects.equals(toLocation, that.toLocation)
        && toLocationIdentInclusionMode == that.toLocationIdentInclusionMode
        && Objects.equals(sectionNumber, that.sectionNumber);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, pipeline, padOrgRole, fromLocation, fromLocationIdentInclusionMode, toLocation,
        toLocationIdentInclusionMode, sectionNumber);
  }
}
