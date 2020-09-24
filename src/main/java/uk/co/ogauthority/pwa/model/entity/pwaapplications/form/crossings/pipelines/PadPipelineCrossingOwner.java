package uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.pipelines;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
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
