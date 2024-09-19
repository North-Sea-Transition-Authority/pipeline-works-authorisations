package uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.Objects;
import uk.co.ogauthority.pwa.service.entitycopier.ChildEntity;

@Entity(name = "pad_storage_area_crossing_owners")
public class PadCrossedStorageAreaOwner implements ChildEntity<Integer, PadCrossedStorageArea> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "pad_storage_area_crossing_id")
  private PadCrossedStorageArea padCrossedStorageArea;

  private Integer ownerOuId;

  private String ownerName;

  public PadCrossedStorageAreaOwner() {
  }

  public PadCrossedStorageAreaOwner(PadCrossedStorageArea padCrossedStorageArea, Integer ownerOuId, String ownerName) {
    this.padCrossedStorageArea = padCrossedStorageArea;
    this.ownerOuId = ownerOuId;
    this.ownerName = ownerName;
  }

  //ChildEntity methods
  @Override
  public void clearId() {
    this.id = null;
  }

  @Override
  public void setParent(PadCrossedStorageArea parentEntity) {
    this.padCrossedStorageArea = parentEntity;
  }

  @Override
  public PadCrossedStorageArea getParent() {
    return this.padCrossedStorageArea;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public PadCrossedStorageArea getPadCrossedStorageArea() {
    return padCrossedStorageArea;
  }

  public PadCrossedStorageAreaOwner setPadCrossedStorageArea(
      PadCrossedStorageArea padCrossedStorageArea) {
    this.padCrossedStorageArea = padCrossedStorageArea;
    return this;
  }

  public Integer getOwnerOuId() {
    return ownerOuId;
  }

  public void setOwnerOuId(Integer ownerOuId) {
    this.ownerOuId = ownerOuId;
  }

  public String getOwnerName() {
    return ownerName;
  }

  public void setOwnerName(String ownerName) {
    this.ownerName = ownerName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PadCrossedStorageAreaOwner that = (PadCrossedStorageAreaOwner) o;
    return id.equals(that.id)
        && padCrossedStorageArea.equals(that.padCrossedStorageArea)
        && Objects.equals(ownerOuId, that.ownerOuId)
        && Objects.equals(ownerName, that.ownerName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, padCrossedStorageArea, ownerOuId, ownerName);
  }
}
