package uk.co.ogauthority.pwa.model.entity.masterpwas;

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
import uk.co.ogauthority.pwa.model.entity.enums.MasterPwaDetailStatus;

@Entity(name = "pwa_details")
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

  @Column(name = "start_timestamp")
  private Instant startInstant;

  @Column(name = "end_timestamp")
  private Instant endInstant;

  public MasterPwaDetail() {
  }

  public MasterPwaDetail(Instant startInstant) {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MasterPwaDetail that = (MasterPwaDetail) o;
    return id.equals(that.id)
        && masterPwa.equals(that.masterPwa)
        && masterPwaDetailStatus == that.masterPwaDetailStatus
        && reference.equals(that.reference)
        && startInstant.equals(that.startInstant)
        && Objects.equals(endInstant, that.endInstant);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, masterPwa, masterPwaDetailStatus, reference, startInstant, endInstant);
  }
}
