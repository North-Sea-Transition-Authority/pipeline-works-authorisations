package uk.co.ogauthority.pwa.model.entity.pipelines;

import java.math.BigDecimal;
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
import javax.persistence.PostLoad;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.apache.commons.lang3.ObjectUtils;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineEntity;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineFlexibility;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineMaterial;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineType;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.CoordinatePair;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.LatitudeCoordinate;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.LatitudeDirection;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.LongitudeCoordinate;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.LongitudeDirection;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineDetailId;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;


@Entity
@Table(name = "pipeline_details")
public class PipelineDetail implements PipelineEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "pipeline_detail_id_generator")
  @SequenceGenerator(name = "pipeline_detail_id_generator", sequenceName = "pipeline_details_id_seq", allocationSize = 1)
  private Integer id;

  @ManyToOne(optional = false)
  private Pipeline pipeline;

  private Instant startTimestamp;
  private Instant endTimestamp;
  private Boolean tipFlag;

  @Enumerated(EnumType.STRING)
  private PipelineStatus pipelineStatus;
  private String pipelineStatusReason;

  private String pipelineNumber;
  private BigDecimal maxExternalDiameter;
  private Boolean pipelineInBundle;
  private String bundleName;

  private String footnote;


  @ManyToOne
  @JoinColumn(name = "pwa_consent_id")
  private PwaConsent pwaConsent;

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

  private String componentPartsDesc;
  private BigDecimal length;
  private String productsToBeConveyed;
  private Boolean trenchedBuriedFilledFlag;
  private String trenchingMethodsDesc;

  @Enumerated(EnumType.STRING)
  private PipelineFlexibility pipelineFlexibility;

  @Enumerated(EnumType.STRING)
  private PipelineMaterial pipelineMaterial;

  private String otherPipelineMaterialUsed;

  private Integer pipelineDesignLife;

  @Transient
  private CoordinatePair fromCoordinates;

  @Transient
  private CoordinatePair toCoordinates;

  @ManyToOne
  @JoinColumn(name = "transferred_from_pipeline_id")
  private Pipeline transferredFromPipeline;

  @ManyToOne
  @JoinColumn(name = "transferred_to_pipeline_id")
  private Pipeline transferredToPipeline;

  public PipelineDetail() {
    // default for hibernate
  }

  public PipelineDetail(Pipeline pipeline) {
    this.setPipeline(pipeline);
  }

  public PipelineDetailId getPipelineDetailId() {
    // worry about caching this if it ever becomes a problem.
    return new PipelineDetailId(this.id);
  }
  @Override
  public Integer getId() {
    return id;
  }

  @Override
  public void setId(Integer id) {
    this.id = id;
  }

  @Override
  public Pipeline getPipeline() {
    return pipeline;
  }

  @Override
  public void setPipeline(Pipeline pipeline) {
    this.pipeline = pipeline;
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

  public Boolean getTipFlag() {
    return tipFlag;
  }

  public void setTipFlag(Boolean tipFlag) {
    this.tipFlag = tipFlag;
  }

  @Override
  public PipelineStatus getPipelineStatus() {
    return pipelineStatus;
  }

  @Override
  public void setPipelineStatus(PipelineStatus pipelineStatus) {
    this.pipelineStatus = pipelineStatus;
  }

  @Override
  public String getPipelineNumber() {
    return pipelineNumber;
  }

  @Override
  public void setPipelineNumber(String pipelineNumber) {
    this.pipelineNumber = pipelineNumber;
  }

  public PwaConsent getPwaConsent() {
    return pwaConsent;
  }

  public void setPwaConsent(PwaConsent pwaConsent) {
    this.pwaConsent = pwaConsent;
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

  public Integer getFromLatitudeDegrees() {
    return fromLatitudeDegrees;
  }

  public Integer getFromLatitudeMinutes() {
    return fromLatitudeMinutes;
  }

  public BigDecimal getFromLatitudeSeconds() {
    return fromLatitudeSeconds;
  }

  public LatitudeDirection getFromLatitudeDirection() {
    return fromLatitudeDirection;
  }

  public Integer getFromLongitudeDegrees() {
    return fromLongitudeDegrees;
  }

  public Integer getFromLongitudeMinutes() {
    return fromLongitudeMinutes;
  }

  public BigDecimal getFromLongitudeSeconds() {
    return fromLongitudeSeconds;
  }

  public LongitudeDirection getFromLongitudeDirection() {
    return fromLongitudeDirection;
  }

  @Override
  public String getToLocation() {
    return toLocation;
  }

  @Override
  public void setToLocation(String toLocation) {
    this.toLocation = toLocation;
  }

  public Integer getToLatitudeDegrees() {
    return toLatitudeDegrees;
  }

  public Integer getToLatitudeMinutes() {
    return toLatitudeMinutes;
  }

  public BigDecimal getToLatitudeSeconds() {
    return toLatitudeSeconds;
  }

  public LatitudeDirection getToLatitudeDirection() {
    return toLatitudeDirection;
  }

  public Integer getToLongitudeDegrees() {
    return toLongitudeDegrees;
  }

  public Integer getToLongitudeMinutes() {
    return toLongitudeMinutes;
  }

  public BigDecimal getToLongitudeSeconds() {
    return toLongitudeSeconds;
  }

  public LongitudeDirection getToLongitudeDirection() {
    return toLongitudeDirection;
  }

  @Override
  public String getComponentPartsDescription() {
    return componentPartsDesc;
  }

  @Override
  public void setComponentPartsDescription(String componentPartsDesc) {
    this.componentPartsDesc = componentPartsDesc;
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
    return trenchedBuriedFilledFlag;
  }

  @Override
  public void setTrenchedBuriedBackfilled(Boolean trenchedBuriedFilledFlag) {
    this.trenchedBuriedFilledFlag = trenchedBuriedFilledFlag;
  }

  @Override
  public String getTrenchingMethodsDescription() {
    return trenchingMethodsDesc;
  }

  @Override
  public void setTrenchingMethodsDescription(String trenchingMethodsDesc) {
    this.trenchingMethodsDesc = trenchingMethodsDesc;
  }

  @Override
  public PipelineId getPipelineId() {
    return this.getPipeline().getPipelineId();
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
  public String getPipelineStatusReason() {
    return pipelineStatusReason;
  }

  @Override
  public void setPipelineStatusReason(String pipelineServiceStatusReason) {
    this.pipelineStatusReason = pipelineServiceStatusReason;
  }

  @Override
  public PipelineFlexibility getPipelineFlexibility() {
    return pipelineFlexibility;
  }

  @Override
  public void setPipelineFlexibility(
      PipelineFlexibility pipelineFlexibility) {
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

  public Pipeline getTransferredFromPipeline() {
    return transferredFromPipeline;
  }

  public void setTransferredFromPipeline(Pipeline transferredFromPipeline) {
    this.transferredFromPipeline = transferredFromPipeline;
  }

  public Pipeline getTransferredToPipeline() {
    return transferredToPipeline;
  }

  public void setTransferredToPipeline(Pipeline transferredToPipeline) {
    this.transferredToPipeline = transferredToPipeline;
  }

  @PostLoad
  public void postLoad() {
    // this method needs to be able to handle nulls given we could be dealing with migrated data
    if (ObjectUtils.allNotNull(
        this.fromLatitudeDegrees, this.fromLatitudeMinutes, this.fromLatitudeSeconds, this.fromLatitudeDirection,
        this.fromLongitudeDegrees, this.fromLongitudeMinutes, this.fromLongitudeSeconds, this.fromLongitudeDirection
    )) {
      this.fromCoordinates = new CoordinatePair(
          new LatitudeCoordinate(
              this.fromLatitudeDegrees,
              this.fromLatitudeMinutes,
              this.fromLatitudeSeconds,
              this.fromLatitudeDirection
          ),
          new LongitudeCoordinate(
              this.fromLongitudeDegrees,
              this.fromLongitudeMinutes,
              this.fromLongitudeSeconds,
              this.fromLongitudeDirection
          )
      );
    }

    if (ObjectUtils.allNotNull(
        this.toLatitudeDegrees, this.toLatitudeMinutes, this.toLatitudeSeconds, this.toLatitudeDirection,
        this.toLongitudeDegrees, this.toLongitudeMinutes, this.toLongitudeSeconds, this.toLongitudeDirection
    )) {
      this.toCoordinates = new CoordinatePair(
          new LatitudeCoordinate(
              this.toLatitudeDegrees,
              this.toLatitudeMinutes,
              this.toLatitudeSeconds,
              this.toLatitudeDirection
          ),
          new LongitudeCoordinate(
              this.toLongitudeDegrees,
              this.toLongitudeMinutes,
              this.toLongitudeSeconds,
              this.toLongitudeDirection
          )
      );
    }

  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PipelineDetail that = (PipelineDetail) o;
    return Objects.equals(id, that.id) && Objects.equals(pipeline,
        that.pipeline) && Objects.equals(startTimestamp, that.startTimestamp) && Objects.equals(
        endTimestamp, that.endTimestamp) && Objects.equals(tipFlag,
        that.tipFlag) && pipelineStatus == that.pipelineStatus && Objects.equals(pipelineStatusReason,
        that.pipelineStatusReason) && Objects.equals(pipelineNumber,
        that.pipelineNumber) && Objects.equals(maxExternalDiameter,
        that.maxExternalDiameter) && Objects.equals(pipelineInBundle,
        that.pipelineInBundle) && Objects.equals(bundleName, that.bundleName) && Objects.equals(
        pwaConsent, that.pwaConsent) && pipelineType == that.pipelineType && Objects.equals(fromLocation,
        that.fromLocation) && Objects.equals(fromLatitudeDegrees,
        that.fromLatitudeDegrees) && Objects.equals(fromLatitudeMinutes,
        that.fromLatitudeMinutes) && Objects.equals(fromLatitudeSeconds,
        that.fromLatitudeSeconds) && fromLatitudeDirection == that.fromLatitudeDirection && Objects.equals(
        fromLongitudeDegrees, that.fromLongitudeDegrees) && Objects.equals(fromLongitudeMinutes,
        that.fromLongitudeMinutes) && Objects.equals(fromLongitudeSeconds,
        that.fromLongitudeSeconds) && fromLongitudeDirection == that.fromLongitudeDirection && Objects.equals(
        toLocation, that.toLocation) && Objects.equals(toLatitudeDegrees,
        that.toLatitudeDegrees) && Objects.equals(toLatitudeMinutes,
        that.toLatitudeMinutes) && Objects.equals(toLatitudeSeconds,
        that.toLatitudeSeconds) && toLatitudeDirection == that.toLatitudeDirection && Objects.equals(
        toLongitudeDegrees, that.toLongitudeDegrees) && Objects.equals(toLongitudeMinutes,
        that.toLongitudeMinutes) && Objects.equals(toLongitudeSeconds,
        that.toLongitudeSeconds) && toLongitudeDirection == that.toLongitudeDirection && Objects.equals(
        componentPartsDesc, that.componentPartsDesc) && Objects.equals(length,
        that.length) && Objects.equals(productsToBeConveyed,
        that.productsToBeConveyed) && Objects.equals(trenchedBuriedFilledFlag,
        that.trenchedBuriedFilledFlag) && Objects.equals(trenchingMethodsDesc,
        that.trenchingMethodsDesc) && pipelineFlexibility == that.pipelineFlexibility && pipelineMaterial == that.pipelineMaterial
        && Objects.equals(otherPipelineMaterialUsed, that.otherPipelineMaterialUsed) && Objects.equals(pipelineDesignLife,
        that.pipelineDesignLife) && Objects.equals(fromCoordinates,
        that.fromCoordinates) && Objects.equals(toCoordinates, that.toCoordinates)
        && Objects.equals(footnote, that.footnote)
        && Objects.equals(transferredFromPipeline, that.transferredFromPipeline)
        && Objects.equals(transferredToPipeline, that.transferredToPipeline);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, pipeline, startTimestamp, endTimestamp, tipFlag, pipelineStatus, pipelineStatusReason,
        pipelineNumber, maxExternalDiameter, pipelineInBundle, bundleName, pwaConsent, pipelineType, fromLocation,
        fromLatitudeDegrees, fromLatitudeMinutes, fromLatitudeSeconds, fromLatitudeDirection, fromLongitudeDegrees,
        fromLongitudeMinutes, fromLongitudeSeconds, fromLongitudeDirection, toLocation, toLatitudeDegrees,
        toLatitudeMinutes, toLatitudeSeconds, toLatitudeDirection, toLongitudeDegrees, toLongitudeMinutes,
        toLongitudeSeconds, toLongitudeDirection, componentPartsDesc, length, productsToBeConveyed,
        trenchedBuriedFilledFlag, trenchingMethodsDesc, pipelineFlexibility, pipelineMaterial,
        otherPipelineMaterialUsed,
        pipelineDesignLife, fromCoordinates, toCoordinates, footnote, transferredFromPipeline, transferredToPipeline);
  }
}
