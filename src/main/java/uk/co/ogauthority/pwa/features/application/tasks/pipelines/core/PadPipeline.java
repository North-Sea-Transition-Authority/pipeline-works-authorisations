package uk.co.ogauthority.pwa.features.application.tasks.pipelines.core;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.math.BigDecimal;
import java.util.Objects;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineCoreType;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineEntity;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineFlexibility;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineMaterial;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineType;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.CoordinatePair;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.CoordinatePairEntity;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.CoordinateUtils;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.LatitudeDirection;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.LongitudeDirection;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.entitycopier.ChildEntity;
import uk.co.ogauthority.pwa.service.entitycopier.ParentEntity;

@Entity
@Table(name = "pad_pipelines")
public class PadPipeline implements PipelineEntity, ParentEntity, ChildEntity<Integer, PwaApplicationDetail>, CoordinatePairEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "pad_id")
  private PwaApplicationDetail pwaApplicationDetail;

  @ManyToOne
  @JoinColumn(name = "pipeline_id")
  private Pipeline pipeline;

  @Enumerated(EnumType.STRING)
  private PipelineType pipelineType;

  private String fromLocation;

  @Column(name = "from_lat_deg")
  private Integer fromLatitudeDegrees;

  @Column(name = "from_lat_min")
  private Integer fromLatitudeMinutes;

  @Column(name = "from_lat_sec")
  private BigDecimal fromLatitudeSeconds;

  @Column(name = "from_lat_dir")
  @Enumerated(EnumType.STRING)
  private LatitudeDirection fromLatitudeDirection;

  @Column(name = "from_long_deg")
  private Integer fromLongitudeDegrees;

  @Column(name = "from_long_min")
  private Integer fromLongitudeMinutes;

  @Column(name = "from_long_sec")
  private BigDecimal fromLongitudeSeconds;

  @Column(name = "from_long_dir")
  @Enumerated(EnumType.STRING)
  private LongitudeDirection fromLongitudeDirection;

  private String toLocation;

  @Column(name = "to_lat_deg")
  private Integer toLatitudeDegrees;

  @Column(name = "to_lat_min")
  private Integer toLatitudeMinutes;

  @Column(name = "to_lat_sec")
  private BigDecimal toLatitudeSeconds;

  @Column(name = "to_lat_dir")
  @Enumerated(EnumType.STRING)
  private LatitudeDirection toLatitudeDirection;

  @Column(name = "to_long_deg")
  private Integer toLongitudeDegrees;

  @Column(name = "to_long_min")
  private Integer toLongitudeMinutes;

  @Column(name = "to_long_sec")
  private BigDecimal toLongitudeSeconds;

  @Column(name = "to_long_dir")
  @Enumerated(EnumType.STRING)
  private LongitudeDirection toLongitudeDirection;

  @Column(name = "component_parts_desc")
  private String componentPartsDescription;

  private BigDecimal length;

  private String productsToBeConveyed;

  @Column(name = "trenched_buried_filled_flag")
  private Boolean trenchedBuriedBackfilled;

  @Column(name = "trenching_methods_desc")
  private String trenchingMethodsDescription;

  private String pipelineRef;

  @Transient
  private CoordinatePair fromCoordinates;

  @Transient
  private CoordinatePair toCoordinates;

  @Enumerated(EnumType.STRING)
  private PipelineFlexibility pipelineFlexibility;

  @Enumerated(EnumType.STRING)
  private PipelineMaterial pipelineMaterial;
  private String otherPipelineMaterialUsed;

  private Integer pipelineDesignLife;

  private BigDecimal maxExternalDiameter;

  private Boolean pipelineInBundle;
  private String bundleName;

  private String temporaryRef;

  private Integer temporaryNumber;

  @Enumerated(EnumType.STRING)
  private PipelineStatus pipelineStatus;
  private String pipelineStatusReason;
  private Boolean pipelineTransferAgreed;

  private Boolean alreadyExistsOnSeabed;
  private Boolean pipelineInUse;
  private String footnote;



  public PadPipeline() {
  }

  public PadPipeline(PwaApplicationDetail pwaApplicationDetail) {
    this.pwaApplicationDetail = pwaApplicationDetail;
  }

  public PadPipelineId getPadPipelineId() {
    return new PadPipelineId(this.id);
  }

  // ParentEntity Methods
  @Override
  public Object getIdAsParent() {
    return this.getId();
  }

  // Child Entity Methods
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

  // generated methods below
  @Override
  public Integer getId() {
    return id;
  }

  @Override
  public void setId(Integer id) {
    this.id = id;
  }

  public PwaApplicationDetail getPwaApplicationDetail() {
    return pwaApplicationDetail;
  }

  public void setPwaApplicationDetail(
      PwaApplicationDetail pwaApplicationDetail) {
    this.pwaApplicationDetail = pwaApplicationDetail;
  }

  @Override
  public Pipeline getPipeline() {
    return pipeline;
  }

  @Override
  public PipelineId getPipelineId() {
    return pipeline.getPipelineId();
  }

  @Override
  public void setPipeline(Pipeline pipeline) {
    this.pipeline = pipeline;
  }

  @Override
  public PipelineType getPipelineType() {
    return pipelineType;
  }

  @Override
  public void setPipelineType(PipelineType pipelineType) {
    this.pipelineType = pipelineType;
  }

  @Override
  public String getFromLocation() {
    return fromLocation;
  }

  @Override
  public void setFromLocation(String fromLocation) {
    this.fromLocation = fromLocation;
  }

  @Override
  public String getToLocation() {
    return toLocation;
  }

  @Override
  public void setToLocation(String toLocation) {
    this.toLocation = toLocation;
  }

  @Override
  public String getComponentPartsDescription() {
    return componentPartsDescription;
  }

  @Override
  public void setComponentPartsDescription(String componentPartsDescription) {
    this.componentPartsDescription = componentPartsDescription;
  }

  @Override
  public BigDecimal getLength() {
    return length;
  }

  @Override
  public void setLength(BigDecimal length) {
    this.length = length;
  }

  @Override
  public String getProductsToBeConveyed() {
    return productsToBeConveyed;
  }

  @Override
  public void setProductsToBeConveyed(String productsToBeConveyed) {
    this.productsToBeConveyed = productsToBeConveyed;
  }

  @Override
  public Boolean getTrenchedBuriedBackfilled() {
    return trenchedBuriedBackfilled;
  }

  @Override
  public void setTrenchedBuriedBackfilled(Boolean trenchedBuriedBackfilled) {
    this.trenchedBuriedBackfilled = trenchedBuriedBackfilled;
  }

  @Override
  public String getTrenchingMethodsDescription() {
    return trenchingMethodsDescription;
  }

  @Override
  public void setTrenchingMethodsDescription(String trenchingMethodsDescription) {
    this.trenchingMethodsDescription = trenchingMethodsDescription;
  }

  public String getPipelineRef() {
    return pipelineRef;
  }

  public void setPipelineRef(String pipelineRef) {
    this.pipelineRef = pipelineRef;
  }

  @Override
  public CoordinatePair getFromCoordinates() {
    return fromCoordinates;
  }

  @Override
  public void setFromCoordinates(CoordinatePair fromCoordinates) {
    this.fromCoordinates = fromCoordinates;
    updateFromCoordinateValues();
  }

  @Override
  public CoordinatePair getToCoordinates() {
    return toCoordinates;
  }

  @Override
  public void setToCoordinates(CoordinatePair toCoordinates) {
    this.toCoordinates = toCoordinates;
    updateToCoordinateValues();
  }

  @Override
  public PipelineFlexibility getPipelineFlexibility() {
    return pipelineFlexibility;
  }

  @Override
  public void setPipelineFlexibility(PipelineFlexibility pipelineFlexibility) {
    this.pipelineFlexibility = pipelineFlexibility;
  }

  @Override
  public PipelineMaterial getPipelineMaterial() {
    return pipelineMaterial;
  }

  @Override
  public void setPipelineMaterial(PipelineMaterial pipelineMaterial) {
    this.pipelineMaterial = pipelineMaterial;
  }

  @Override
  public String getOtherPipelineMaterialUsed() {
    return otherPipelineMaterialUsed;
  }

  @Override
  public void setOtherPipelineMaterialUsed(String otherPipelineMaterialUsed) {
    this.otherPipelineMaterialUsed = otherPipelineMaterialUsed;
  }

  @Override
  public Integer getPipelineDesignLife() {
    return pipelineDesignLife;
  }

  @Override
  public void setPipelineDesignLife(Integer pipelineDesignLife) {
    this.pipelineDesignLife = pipelineDesignLife;
  }

  @Override
  public PipelineCoreType getCoreType() {
    return pipelineType.getCoreType();
  }

  @Override
  public BigDecimal getMaxExternalDiameter() {
    return maxExternalDiameter;
  }

  @Override
  public void setMaxExternalDiameter(BigDecimal maxExternalDiameter) {
    this.maxExternalDiameter = maxExternalDiameter;
  }

  @Override
  public Boolean getPipelineInBundle() {
    return pipelineInBundle;
  }

  @Override
  public void setPipelineInBundle(Boolean pipelineInBundle) {
    this.pipelineInBundle = pipelineInBundle;
  }

  @Override
  public String getBundleName() {
    return bundleName;
  }

  @Override
  public void setBundleName(String bundleName) {
    this.bundleName = bundleName;
  }

  @Override
  public String getPipelineNumber() {
    return getPipelineRef();
  }

  @Override
  public void setPipelineNumber(String pipelineNumber) {
    setPipelineRef(pipelineNumber);
  }

  public String getTemporaryRef() {
    return temporaryRef;
  }

  public void setTemporaryRef(String temporaryRef) {
    this.temporaryRef = temporaryRef;
  }

  public Boolean getAlreadyExistsOnSeabed() {
    return alreadyExistsOnSeabed;
  }

  public void setAlreadyExistsOnSeabed(Boolean alreadyExistsOnSeabed) {
    this.alreadyExistsOnSeabed = alreadyExistsOnSeabed;
  }

  public Boolean getPipelineInUse() {
    return pipelineInUse;
  }

  public void setPipelineInUse(Boolean pipelineInUse) {
    this.pipelineInUse = pipelineInUse;
  }

  public Integer getFromLatitudeDegrees() {
    return fromLatitudeDegrees;
  }

  public void setFromLatitudeDegrees(Integer fromLatitudeDegrees) {
    this.fromLatitudeDegrees = fromLatitudeDegrees;
  }

  @Override
  public Integer getFromLatDeg() {
    return this.fromLatitudeDegrees;
  }

  @Override
  public Integer getFromLatMin() {
    return this.fromLatitudeMinutes;
  }

  @Override
  public BigDecimal getFromLatSec() {
    return this.fromLatitudeSeconds;
  }

  @Override
  public LatitudeDirection getFromLatDir() {
    return this.fromLatitudeDirection;
  }

  @Override
  public Integer getFromLongDeg() {
    return this.fromLongitudeDegrees;
  }

  @Override
  public Integer getFromLongMin() {
    return this.fromLongitudeMinutes;
  }

  @Override
  public BigDecimal getFromLongSec() {
    return this.fromLongitudeSeconds;
  }

  @Override
  public LongitudeDirection getFromLongDir() {
    return this.fromLongitudeDirection;
  }

  @Override
  public Integer getToLatDeg() {
    return this.toLatitudeDegrees;
  }

  @Override
  public Integer getToLatMin() {
    return this.toLatitudeMinutes;
  }

  @Override
  public BigDecimal getToLatSec() {
    return this.toLatitudeSeconds;
  }

  @Override
  public LatitudeDirection getToLatDir() {
    return this.toLatitudeDirection;
  }

  @Override
  public Integer getToLongDeg() {
    return this.toLongitudeDegrees;
  }

  @Override
  public Integer getToLongMin() {
    return this.toLongitudeMinutes;
  }

  @Override
  public BigDecimal getToLongSec() {
    return this.toLongitudeSeconds;
  }

  @Override
  public LongitudeDirection getToLongDir() {
    return this.toLongitudeDirection;
  }

  @Override
  public String getFootnote() {
    return footnote;
  }

  @Override
  public void setFootnote(String footnote) {
    this.footnote = footnote;
  }

  private void updateFromCoordinateValues() {
    this.fromLatitudeDegrees = this.fromCoordinates.getLatitude().getDegrees();
    this.fromLatitudeMinutes = this.fromCoordinates.getLatitude().getMinutes();
    this.fromLatitudeSeconds = this.fromCoordinates.getLatitude().getSeconds();
    this.fromLatitudeDirection = this.fromCoordinates.getLatitude().getDirection();

    this.fromLongitudeDegrees = this.fromCoordinates.getLongitude().getDegrees();
    this.fromLongitudeMinutes = this.fromCoordinates.getLongitude().getMinutes();
    this.fromLongitudeSeconds = this.fromCoordinates.getLongitude().getSeconds();
    this.fromLongitudeDirection = this.fromCoordinates.getLongitude().getDirection();
  }

  private void updateToCoordinateValues() {
    this.toLatitudeDegrees = this.toCoordinates.getLatitude().getDegrees();
    this.toLatitudeMinutes = this.toCoordinates.getLatitude().getMinutes();
    this.toLatitudeSeconds = this.toCoordinates.getLatitude().getSeconds();
    this.toLatitudeDirection = this.toCoordinates.getLatitude().getDirection();

    this.toLongitudeDegrees = this.toCoordinates.getLongitude().getDegrees();
    this.toLongitudeMinutes = this.toCoordinates.getLongitude().getMinutes();
    this.toLongitudeSeconds = this.toCoordinates.getLongitude().getSeconds();
    this.toLongitudeDirection = this.toCoordinates.getLongitude().getDirection();
  }

  @PostLoad
  public void postLoad() {
    this.fromCoordinates = CoordinateUtils.buildFromCoordinatePair(this);
    this.toCoordinates = CoordinateUtils.buildToCoordinatePair(this);
  }

  public Integer getTemporaryNumber() {
    return temporaryNumber;
  }

  public void setTemporaryNumber(Integer temporaryNumber) {
    this.temporaryNumber = temporaryNumber;
  }

  public PipelineStatus getPipelineStatus() {
    return pipelineStatus;
  }

  public void setPipelineStatus(
      PipelineStatus pipelineServiceStatus) {
    this.pipelineStatus = pipelineServiceStatus;
  }

  public String getPipelineStatusReason() {
    return pipelineStatusReason;
  }

  public void setPipelineStatusReason(String pipelineServiceStatusReason) {
    this.pipelineStatusReason = pipelineServiceStatusReason;
  }

  public Boolean getPipelineTransferAgreed() {
    return pipelineTransferAgreed;
  }

  public void setPipelineTransferAgreed(Boolean pipelineTransferAgreed) {
    this.pipelineTransferAgreed = pipelineTransferAgreed;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PadPipeline that = (PadPipeline) o;
    return Objects.equals(id, that.id)
        && Objects.equals(pwaApplicationDetail, that.pwaApplicationDetail)
        && Objects.equals(pipeline, that.pipeline)
        && pipelineType == that.pipelineType
        && Objects.equals(fromLocation, that.fromLocation)
        && Objects.equals(fromLatitudeDegrees, that.fromLatitudeDegrees)
        && Objects.equals(fromLatitudeMinutes, that.fromLatitudeMinutes)
        && Objects.equals(fromLatitudeSeconds, that.fromLatitudeSeconds)
        && fromLatitudeDirection == that.fromLatitudeDirection
        && Objects.equals(fromLongitudeDegrees, that.fromLongitudeDegrees)
        && Objects.equals(fromLongitudeMinutes, that.fromLongitudeMinutes)
        && Objects.equals(fromLongitudeSeconds, that.fromLongitudeSeconds)
        && fromLongitudeDirection == that.fromLongitudeDirection
        && Objects.equals(toLocation, that.toLocation)
        && Objects.equals(toLatitudeDegrees, that.toLatitudeDegrees)
        && Objects.equals(toLatitudeMinutes, that.toLatitudeMinutes)
        && Objects.equals(toLatitudeSeconds, that.toLatitudeSeconds)
        && toLatitudeDirection == that.toLatitudeDirection
        && Objects.equals(toLongitudeDegrees, that.toLongitudeDegrees)
        && Objects.equals(toLongitudeMinutes, that.toLongitudeMinutes)
        && Objects.equals(toLongitudeSeconds, that.toLongitudeSeconds)
        && toLongitudeDirection == that.toLongitudeDirection
        && Objects.equals(componentPartsDescription, that.componentPartsDescription)
        && Objects.equals(length, that.length)
        && Objects.equals(productsToBeConveyed, that.productsToBeConveyed)
        && Objects.equals(trenchedBuriedBackfilled, that.trenchedBuriedBackfilled)
        && Objects.equals(trenchingMethodsDescription, that.trenchingMethodsDescription)
        && Objects.equals(pipelineRef, that.pipelineRef)
        && Objects.equals(fromCoordinates, that.fromCoordinates)
        && Objects.equals(toCoordinates, that.toCoordinates)
        && pipelineFlexibility == that.pipelineFlexibility
        && pipelineMaterial == that.pipelineMaterial
        && Objects.equals(otherPipelineMaterialUsed, that.otherPipelineMaterialUsed)
        && Objects.equals(pipelineDesignLife, that.pipelineDesignLife)
        && Objects.equals(pipelineInBundle, that.pipelineInBundle)
        && Objects.equals(bundleName, that.bundleName)
        && Objects.equals(pipelineStatus, that.pipelineStatus)
        && Objects.equals(pipelineStatusReason, that.pipelineStatusReason)
        && Objects.equals(pipelineTransferAgreed, that.pipelineTransferAgreed)
        && Objects.equals(alreadyExistsOnSeabed, that.alreadyExistsOnSeabed)
        && Objects.equals(pipelineInUse, that.pipelineInUse)
        && Objects.equals(footnote, that.footnote);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, pwaApplicationDetail, pipeline, pipelineType, fromLocation, fromLatitudeDegrees,
        fromLatitudeMinutes, fromLatitudeSeconds, fromLatitudeDirection, fromLongitudeDegrees, fromLongitudeMinutes,
        fromLongitudeSeconds, fromLongitudeDirection, toLocation, toLatitudeDegrees, toLatitudeMinutes,
        toLatitudeSeconds,
        toLatitudeDirection, toLongitudeDegrees, toLongitudeMinutes, toLongitudeSeconds, toLongitudeDirection,
        componentPartsDescription, length, productsToBeConveyed, trenchedBuriedBackfilled, trenchingMethodsDescription,
        pipelineRef, fromCoordinates, toCoordinates, pipelineFlexibility, pipelineMaterial, otherPipelineMaterialUsed,
        pipelineDesignLife, pipelineInBundle, bundleName, pipelineStatus, pipelineStatusReason, pipelineTransferAgreed,
        alreadyExistsOnSeabed, pipelineInUse, footnote);
  }
}
