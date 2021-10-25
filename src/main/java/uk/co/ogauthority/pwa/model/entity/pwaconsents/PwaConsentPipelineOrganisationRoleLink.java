package uk.co.ogauthority.pwa.model.entity.pwaconsents;

import java.time.Instant;
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
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.IdentLocationInclusionMode;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdentifier;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineSection;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.TreatyAgreement;
import uk.co.ogauthority.pwa.model.entity.enums.pipelinehuoo.OrgRoleInstanceType;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinehuoo.PipelineOrganisationRoleLink;

@Entity
@Table(name = "pipeline_org_role_links")
public class PwaConsentPipelineOrganisationRoleLink implements PipelineOrganisationRoleLink {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "pipeline_id")
  private Pipeline pipeline;

  @ManyToOne
  @JoinColumn(name = "pwa_consent_org_role_id")
  private PwaConsentOrganisationRole pwaConsentOrganisationRole;

  @ManyToOne
  @JoinColumn(name = "added_by_pwa_consent_id")
  private PwaConsent addedByPwaConsent;

  @ManyToOne
  @JoinColumn(name = "ended_by_pwa_consent_id")
  private PwaConsent endedByPwaConsent;

  private String fromLocation;

  @Column(name = "from_location_mode")
  @Enumerated(EnumType.STRING)
  private IdentLocationInclusionMode fromLocationIdentInclusionMode;

  private String toLocation;

  @Column(name = "to_location_mode")
  @Enumerated(EnumType.STRING)
  private IdentLocationInclusionMode toLocationIdentInclusionMode;

  private Integer sectionNumber;

  private Instant startTimestamp;

  private Instant endTimestamp;

  public PwaConsentPipelineOrganisationRoleLink() {
  }

  public PwaConsentPipelineOrganisationRoleLink(Pipeline pipeline,
                                                PwaConsentOrganisationRole pwaConsentOrganisationRole) {
    this.pipeline = pipeline;
    this.pwaConsentOrganisationRole = pwaConsentOrganisationRole;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  @Override
  public Pipeline getPipeline() {
    return pipeline;
  }

  public void setPipeline(Pipeline pipeline) {
    this.pipeline = pipeline;
  }

  public PwaConsentOrganisationRole getPwaConsentOrganisationRole() {
    return pwaConsentOrganisationRole;
  }

  public void setPwaConsentOrganisationRole(
      PwaConsentOrganisationRole pwaConsentOrganisationRole) {
    this.pwaConsentOrganisationRole = pwaConsentOrganisationRole;
  }

  public PwaConsent getAddedByPwaConsent() {
    return addedByPwaConsent;
  }

  public void setAddedByPwaConsent(PwaConsent addedByPwaConsent) {
    this.addedByPwaConsent = addedByPwaConsent;
  }

  public PwaConsent getEndedByPwaConsent() {
    return endedByPwaConsent;
  }

  public void setEndedByPwaConsent(PwaConsent endedByPwaConsent) {
    this.endedByPwaConsent = endedByPwaConsent;
  }

  public Instant getStartTimestamp() {
    return startTimestamp;
  }

  public void setStartTimestamp(Instant startTimestamp) {
    this.startTimestamp = startTimestamp;
  }

  public Instant getEndTimestamp() {
    return endTimestamp;
  }

  public void setEndTimestamp(Instant endTimestamp) {
    this.endTimestamp = endTimestamp;
  }

  @Override
  public HuooRole getRole() {
    return pwaConsentOrganisationRole.getRole();
  }

  @Override
  public Optional<OrganisationUnitId> getOrgUnitId() {
    return Optional.ofNullable(pwaConsentOrganisationRole.getOrganisationUnitId())
        .map(OrganisationUnitId::new);
  }

  @Override
  public Optional<TreatyAgreement> getAgreement() {
    return Optional.ofNullable(pwaConsentOrganisationRole.getAgreement());
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
        this.fromLocation, this.fromLocationIdentInclusionMode, this.toLocation, this.toLocationIdentInclusionMode)
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
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PwaConsentPipelineOrganisationRoleLink that = (PwaConsentPipelineOrganisationRoleLink) o;
    return Objects.equals(id, that.id) && Objects.equals(pipeline,
        that.pipeline) && Objects.equals(pwaConsentOrganisationRole,
        that.pwaConsentOrganisationRole) && Objects.equals(addedByPwaConsent,
        that.addedByPwaConsent) && Objects.equals(endedByPwaConsent,
        that.endedByPwaConsent) && Objects.equals(fromLocation,
        that.fromLocation) && fromLocationIdentInclusionMode == that.fromLocationIdentInclusionMode && Objects.equals(
        toLocation,
        that.toLocation) && toLocationIdentInclusionMode == that.toLocationIdentInclusionMode && Objects.equals(
        sectionNumber, that.sectionNumber) && Objects.equals(startTimestamp,
        that.startTimestamp) && Objects.equals(endTimestamp, that.endTimestamp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, pipeline, pwaConsentOrganisationRole, addedByPwaConsent, endedByPwaConsent, fromLocation,
        fromLocationIdentInclusionMode, toLocation, toLocationIdentInclusionMode, sectionNumber, startTimestamp,
        endTimestamp);
  }
}
