package uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.Instant;
import java.util.Objects;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.CrossingOwner;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.external.BlockLocation;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.external.PearsLicence;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.entitycopier.ChildEntity;
import uk.co.ogauthority.pwa.service.entitycopier.ParentEntity;

@Entity(name = "pad_blocks")
public class PadCrossedBlock implements ChildEntity<Integer, PwaApplicationDetail>, ParentEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "application_detail_id")
  private PwaApplicationDetail pwaApplicationDetail;

  @ManyToOne
  @JoinColumn(name = "plm_id")
  private PearsLicence licence;

  @Column(name = "block_ref")
  private String blockReference;

  @Column(name = "quadrant_no")
  private String quadrantNumber;

  @Column(name = "block_no")
  private String blockNumber;

  @Enumerated(EnumType.STRING)
  private CrossingOwner blockOwner;

  private String suffix;
  private BlockLocation location;

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

  public PearsLicence getLicence() {
    return licence;
  }

  public void setLicence(PearsLicence licence) {
    this.licence = licence;
  }

  public String getBlockReference() {
    return blockReference;
  }

  public void setBlockReference(String blockReference) {
    this.blockReference = blockReference;
  }

  public String getQuadrantNumber() {
    return quadrantNumber;
  }

  public void setQuadrantNumber(String quadrantNumber) {
    this.quadrantNumber = quadrantNumber;
  }

  public String getBlockNumber() {
    return blockNumber;
  }

  public void setBlockNumber(String blockNumber) {
    this.blockNumber = blockNumber;
  }

  public String getSuffix() {
    return suffix;
  }

  public void setSuffix(String suffix) {
    this.suffix = suffix;
  }

  public BlockLocation getLocation() {
    return location;
  }

  public void setLocation(BlockLocation location) {
    this.location = location;
  }

  public Instant getCreatedInstant() {
    return createdInstant;
  }

  public void setCreatedInstant(Instant createdInstant) {
    this.createdInstant = createdInstant;
  }

  public CrossingOwner getBlockOwner() {
    return blockOwner;
  }

  public void setBlockOwner(CrossingOwner blockOwner) {
    this.blockOwner = blockOwner;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PadCrossedBlock that = (PadCrossedBlock) o;
    return Objects.equals(id, that.id)
        && pwaApplicationDetail.equals(that.pwaApplicationDetail)
        && Objects.equals(licence, that.licence)
        && Objects.equals(blockReference, that.blockReference)
        && Objects.equals(quadrantNumber, that.quadrantNumber)
        && Objects.equals(blockNumber, that.blockNumber)
        && blockOwner == that.blockOwner
        && Objects.equals(suffix, that.suffix)
        && location == that.location
        && Objects.equals(createdInstant, that.createdInstant);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        id,
        pwaApplicationDetail,
        licence,
        blockReference,
        quadrantNumber,
        blockNumber,
        blockOwner,
        suffix,
        location,
        createdInstant
    );
  }
}
