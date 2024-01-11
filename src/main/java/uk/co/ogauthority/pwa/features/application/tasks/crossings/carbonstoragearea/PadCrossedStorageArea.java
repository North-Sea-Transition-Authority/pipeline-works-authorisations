package uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea;

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
import uk.co.ogauthority.pwa.features.application.tasks.crossings.CrossingOwner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.entitycopier.ChildEntity;
import uk.co.ogauthority.pwa.service.entitycopier.ParentEntity;

@Entity(name = "pad_crossed_storage_areas")
public class PadCrossedStorageArea implements ChildEntity<Integer, PwaApplicationDetail>, ParentEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "pad_id")
  private PwaApplicationDetail pwaApplicationDetail;

  @Column(name = "storage_area_ref")
  private String storageAreaReference;

  @Enumerated(EnumType.STRING)
  private CrossingOwner crossingOwnerType;
  private String location;

  @Column(name = "created_timestamp")
  private Instant createdInstant;

  //ParentEntity methods
  @Override
  public Object getIdAsParent() {
    return this.id;
  }

  //ChildEntity methods
  @Override
  public void clearId() {
    this.id = null;
  }

  @Override
  public void setParent(PwaApplicationDetail parentEntity) {
    this.pwaApplicationDetail = parentEntity;
  }

  @Override
  public PwaApplicationDetail getParent() {
    return this.pwaApplicationDetail;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public PwaApplicationDetail getPwaApplicationDetail() {
    return pwaApplicationDetail;
  }

  public void setPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail) {
    this.pwaApplicationDetail = pwaApplicationDetail;
  }

  public String getStorageAreaReference() {
    return storageAreaReference;
  }

  public PadCrossedStorageArea setStorageAreaReference(String storageAreaReference) {
    this.storageAreaReference = storageAreaReference;
    return this;
  }

  public String getLocation() {
    return location;
  }

  public PadCrossedStorageArea setLocation(String location) {
    this.location = location;
    return this;
  }

  public Instant getCreatedInstant() {
    return createdInstant;
  }

  public void setCreatedInstant(Instant createdInstant) {
    this.createdInstant = createdInstant;
  }

  public CrossingOwner getCrossingOwnerType() {
    return crossingOwnerType;
  }

  public void setCrossingOwnerType(CrossingOwner crossingOwnerType) {
    this.crossingOwnerType = crossingOwnerType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PadCrossedStorageArea that = (PadCrossedStorageArea) o;
    return Objects.equals(id, that.id)
        && pwaApplicationDetail.equals(that.pwaApplicationDetail)
        && Objects.equals(storageAreaReference, that.storageAreaReference)
        && crossingOwnerType == that.crossingOwnerType
        && location == that.location
        && Objects.equals(createdInstant, that.createdInstant);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        id,
        pwaApplicationDetail,
        storageAreaReference,
        crossingOwnerType,
        location,
        createdInstant
    );
  }
}
