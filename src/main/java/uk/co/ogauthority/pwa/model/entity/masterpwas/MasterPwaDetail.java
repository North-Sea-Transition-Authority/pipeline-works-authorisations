package uk.co.ogauthority.pwa.model.entity.masterpwas;

import static uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType.PETROLEUM;

import java.time.Instant;
import java.util.Objects;
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
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.model.entity.enums.MasterPwaDetailStatus;

@Entity
@Table(name = "pwa_details")
public class MasterPwaDetail {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "pwa_id", referencedColumnName = "id")
  private MasterPwa masterPwa;

  @Enumerated(EnumType.STRING)
  @Column(name = "pwa_status")
  private MasterPwaDetailStatus masterPwaDetailStatus;

  private String reference;

  @Enumerated(EnumType.STRING)
  private PwaResourceType resourceType;

  private Boolean isLinkedToFields;

  private String pwaLinkedToDescription;

  @Column(name = "start_timestamp")
  private Instant startInstant;

  @Column(name = "end_timestamp")
  private Instant endInstant;

  public MasterPwaDetail() {
  }

  public MasterPwaDetail(MasterPwa masterPwa,
                         MasterPwaDetailStatus status,
                         String reference,
                         Instant startInstant,
                         PwaResourceType resourceType) {
    this.masterPwa = masterPwa;
    this.masterPwaDetailStatus = status;
    this.resourceType = resourceType;
    this.reference = reference;
    this.startInstant = startInstant;
  }

  public MasterPwaDetail(MasterPwa masterPwa,
                         MasterPwaDetailStatus status,
                         String reference,
                         PwaResourceType resourceType,
                         Instant startInstant) {
    this.masterPwa = masterPwa;
    this.masterPwaDetailStatus = status;
    this.reference = reference;
    this.resourceType = resourceType;
    this.startInstant = startInstant;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public MasterPwa getMasterPwa() {
    return masterPwa;
  }

  public void setMasterPwa(MasterPwa masterPwa) {
    this.masterPwa = masterPwa;
  }

  public MasterPwaDetailStatus getMasterPwaDetailStatus() {
    return masterPwaDetailStatus;
  }

  public void setMasterPwaDetailStatus(MasterPwaDetailStatus masterPwaDetailStatus) {
    this.masterPwaDetailStatus = masterPwaDetailStatus;
  }

  public PwaResourceType getResourceType() {
    return resourceType;
  }

  public void setResourceType(PwaResourceType resourceType) {
    this.resourceType = resourceType;
  }

  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  public Instant getStartInstant() {
    return startInstant;
  }

  public void setStartInstant(Instant startInstant) {
    this.startInstant = startInstant;
  }

  public Instant getEndInstant() {
    return endInstant;
  }

  public void setEndInstant(Instant endInstant) {
    this.endInstant = endInstant;
  }

  public int getMasterPwaId() {
    return this.masterPwa.getId();
  }

  public Boolean getLinkedToFields() {
    return isLinkedToFields;
  }

  public void setLinkedToFields(Boolean linkedToFields) {
    isLinkedToFields = linkedToFields;
  }

  public String getPwaLinkedToDescription() {
    return pwaLinkedToDescription;
  }

  public void setPwaLinkedToDescription(String pwaLinkedToDescription) {
    this.pwaLinkedToDescription = pwaLinkedToDescription;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MasterPwaDetail that = (MasterPwaDetail) o;
    return Objects.equals(id, that.id)
        && Objects.equals(masterPwa, that.masterPwa)
        && masterPwaDetailStatus == that.masterPwaDetailStatus
        && Objects.equals(reference, that.reference)
        && Objects.equals(isLinkedToFields, that.isLinkedToFields)
        && Objects.equals(pwaLinkedToDescription, that.pwaLinkedToDescription)
        && Objects.equals(startInstant, that.startInstant)
        && Objects.equals(endInstant, that.endInstant);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        id,
        masterPwa,
        masterPwaDetailStatus,
        reference,
        isLinkedToFields,
        pwaLinkedToDescription,
        startInstant,
        endInstant);
  }
}
