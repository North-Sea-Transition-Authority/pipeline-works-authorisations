package uk.co.ogauthority.pwa.model.entity.pwaconsents;

import java.time.Instant;
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

@Entity
@Table(name = "pipeline_org_role_links")
public class PwaConsentPipelineOrganisationRoleLink {

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

  private Instant startTimestamp;

  private Instant endTimestamp;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

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
