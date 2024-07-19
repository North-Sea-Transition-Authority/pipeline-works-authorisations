package uk.co.ogauthority.pwa.features.application.tasks.crossings.pipeline;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.service.entitycopier.ChildEntity;

@Entity(name = "pad_pipeline_crossing_owners")
public class PadPipelineCrossingOwner implements ChildEntity<Integer, PadPipelineCrossing> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "ppc_id")
  private PadPipelineCrossing padPipelineCrossing;

  @ManyToOne
  @JoinColumn(name = "ou_id")
  private PortalOrganisationUnit organisationUnit;

  @Column(name = "org_manual_entry")
  private String manualOrganisationEntry;

  //ChildEntity methods
  @Override
  public void clearId() {
    this.id = null;
  }

  @Override
  public void setParent(PadPipelineCrossing parentEntity) {
    this.padPipelineCrossing = parentEntity;
  }

  @Override
  public PadPipelineCrossing getParent() {
    return this.padPipelineCrossing;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public PadPipelineCrossing getPadPipelineCrossing() {
    return padPipelineCrossing;
  }

  public void setPadPipelineCrossing(
      PadPipelineCrossing padPipelineCrossing) {
    this.padPipelineCrossing = padPipelineCrossing;
  }

  public PortalOrganisationUnit getOrganisationUnit() {
    return organisationUnit;
  }

  public void setOrganisationUnit(
      PortalOrganisationUnit organisationUnit) {
    this.organisationUnit = organisationUnit;
  }

  public String getManualOrganisationEntry() {
    return manualOrganisationEntry;
  }

  public void setManualOrganisationEntry(String manualOrganisationEntry) {
    this.manualOrganisationEntry = manualOrganisationEntry;
  }

  public boolean isManualEntry() {
    return this.organisationUnit == null;
  }
}
