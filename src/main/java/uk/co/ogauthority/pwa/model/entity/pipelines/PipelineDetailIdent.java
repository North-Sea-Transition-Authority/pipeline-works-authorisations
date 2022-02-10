package uk.co.ogauthority.pwa.model.entity.pipelines;

import java.math.BigDecimal;
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
import javax.persistence.Table;
import javax.persistence.Transient;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineCoreType;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdent;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.CoordinatePair;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.CoordinatePairEntity;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.CoordinateUtils;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.LatitudeDirection;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.LongitudeDirection;

@Entity
@Table(name = "pipeline_detail_idents")
public class PipelineDetailIdent implements PipelineIdent, CoordinatePairEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "pipeline_detail_id")
  private PipelineDetail pipelineDetail;

  private int identNo;

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

  private BigDecimal length;

  private Boolean isDefiningStructure;

  @Transient
  private CoordinatePair fromCoordinates;

  @Transient
  private CoordinatePair toCoordinates;

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

  public PipelineDetailIdent() {
    // no-args for hibernate
  }

  public PipelineDetailIdent(PipelineDetail pipelineDetail) {
    this.pipelineDetail = pipelineDetail;
  }

  // Interface implementations
  @Override
  public Integer getPipelineIdentId() {
    return this.id;
  }

  @Override
  public PipelineId getPipelineId() {
    return this.pipelineDetail.getPipelineId();
  }

  @Override
  public int getIdentNo() {
    return this.identNo;
  }

  @Override
  public String getFromLocation() {
    return this.fromLocation;
  }

  @Override
  public String getToLocation() {
    return this.toLocation;
  }

  @Override
  public BigDecimal getLength() {
    return this.length;
  }

  @Override
  public Boolean getIsDefiningStructure() {
    return this.isDefiningStructure;
  }

  @Override
  public CoordinatePair getFromCoordinates() {
    return this.fromCoordinates;
  }

  @Override
  public CoordinatePair getToCoordinates() {
    return this.toCoordinates;
  }

  @Override
  public PipelineCoreType getPipelineCoreType() {
    return this.pipelineDetail.getPipelineType() != null
        ? this.pipelineDetail.getPipelineType().getCoreType()
        : PipelineCoreType.SINGLE_CORE;
  }

  // Custom Behaviour
  @PostLoad
  public void postLoad() {
    this.fromCoordinates = CoordinateUtils.buildFromCoordinatePair(this);
    this.toCoordinates = CoordinateUtils.buildToCoordinatePair(this);
  }

  //  Getters
  public Integer getId() {
    return id;
  }

  public PipelineDetail getPipelineDetail() {
    return pipelineDetail;
  }

  @Override
  public Integer getFromLatDeg() {
    return fromLatitudeDegrees;
  }

  @Override
  public Integer getFromLatMin() {
    return fromLatitudeMinutes;
  }

  @Override
  public BigDecimal getFromLatSec() {
    return fromLatitudeSeconds;
  }

  @Override
  public LatitudeDirection getFromLatDir() {
    return fromLatitudeDirection;
  }

  @Override
  public Integer getFromLongDeg() {
    return fromLongitudeDegrees;
  }

  @Override
  public Integer getFromLongMin() {
    return fromLongitudeMinutes;
  }

  @Override
  public BigDecimal getFromLongSec() {
    return fromLongitudeSeconds;
  }

  @Override
  public LongitudeDirection getFromLongDir() {
    return fromLongitudeDirection;
  }

  @Override
  public Integer getToLatDeg() {
    return toLatitudeDegrees;
  }

  @Override
  public Integer getToLatMin() {
    return toLatitudeMinutes;
  }

  @Override
  public BigDecimal getToLatSec() {
    return toLatitudeSeconds;
  }

  @Override
  public LatitudeDirection getToLatDir() {
    return toLatitudeDirection;
  }

  @Override
  public Integer getToLongDeg() {
    return toLongitudeDegrees;
  }

  @Override
  public Integer getToLongMin() {
    return toLongitudeMinutes;
  }

  @Override
  public BigDecimal getToLongSec() {
    return toLongitudeSeconds;
  }

  @Override
  public LongitudeDirection getToLongDir() {
    return toLongitudeDirection;
  }

  public Boolean getDefiningStructure() {
    return isDefiningStructure;
  }

  // Setters

  @Override
  public void setFromCoordinates(CoordinatePair fromCoordinates) {
    this.fromCoordinates = fromCoordinates;
    updateFromCoordinateValues();
  }

  @Override
  public void setToCoordinates(CoordinatePair toCoordinates) {
    this.toCoordinates = toCoordinates;
    updateToCoordinateValues();
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public void setPipelineDetail(PipelineDetail pipelineDetail) {
    this.pipelineDetail = pipelineDetail;
  }

  @Override
  public void setIdentNo(int identNo) {
    this.identNo = identNo;
  }

  @Override
  public void setFromLocation(String fromLocation) {
    this.fromLocation = fromLocation;
  }

  public void setFromLatitudeDegrees(Integer fromLatitudeDegrees) {
    this.fromLatitudeDegrees = fromLatitudeDegrees;
  }

  public void setFromLatitudeMinutes(Integer fromLatitudeMinutes) {
    this.fromLatitudeMinutes = fromLatitudeMinutes;
  }

  public void setFromLatitudeSeconds(BigDecimal fromLatitudeSeconds) {
    this.fromLatitudeSeconds = fromLatitudeSeconds;
  }

  public void setFromLatitudeDirection(LatitudeDirection fromLatitudeDirection) {
    this.fromLatitudeDirection = fromLatitudeDirection;
  }

  public void setFromLongitudeDegrees(Integer fromLongitudeDegrees) {
    this.fromLongitudeDegrees = fromLongitudeDegrees;
  }

  public void setFromLongitudeMinutes(Integer fromLongitudeMinutes) {
    this.fromLongitudeMinutes = fromLongitudeMinutes;
  }

  public void setFromLongitudeSeconds(BigDecimal fromLongitudeSeconds) {
    this.fromLongitudeSeconds = fromLongitudeSeconds;
  }

  public void setFromLongitudeDirection(LongitudeDirection fromLongitudeDirection) {
    this.fromLongitudeDirection = fromLongitudeDirection;
  }

  @Override
  public void setToLocation(String toLocation) {
    this.toLocation = toLocation;
  }

  public void setToLatitudeDegrees(Integer toLatitudeDegrees) {
    this.toLatitudeDegrees = toLatitudeDegrees;
  }

  public void setToLatitudeMinutes(Integer toLatitudeMinutes) {
    this.toLatitudeMinutes = toLatitudeMinutes;
  }

  public void setToLatitudeSeconds(BigDecimal toLatitudeSeconds) {
    this.toLatitudeSeconds = toLatitudeSeconds;
  }

  public void setToLatitudeDirection(LatitudeDirection toLatitudeDirection) {
    this.toLatitudeDirection = toLatitudeDirection;
  }

  public void setToLongitudeDegrees(Integer toLongitudeDegrees) {
    this.toLongitudeDegrees = toLongitudeDegrees;
  }

  public void setToLongitudeMinutes(Integer toLongitudeMinutes) {
    this.toLongitudeMinutes = toLongitudeMinutes;
  }

  public void setToLongitudeSeconds(BigDecimal toLongitudeSeconds) {
    this.toLongitudeSeconds = toLongitudeSeconds;
  }

  public void setToLongitudeDirection(LongitudeDirection toLongitudeDirection) {
    this.toLongitudeDirection = toLongitudeDirection;
  }

  @Override
  public void setLength(BigDecimal length) {
    this.length = length;
  }

  @Override
  public void setDefiningStructure(Boolean definingStructure) {
    isDefiningStructure = definingStructure;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PipelineDetailIdent that = (PipelineDetailIdent) o;
    return identNo == that.identNo && Objects.equals(id, that.id) && Objects.equals(pipelineDetail,
        that.pipelineDetail) && Objects.equals(fromLocation, that.fromLocation) && Objects.equals(
        fromLatitudeDegrees, that.fromLatitudeDegrees) && Objects.equals(fromLatitudeMinutes,
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
        that.toLongitudeSeconds) && toLongitudeDirection == that.toLongitudeDirection && Objects.equals(length,
        that.length) && Objects.equals(isDefiningStructure,
        that.isDefiningStructure) && Objects.equals(fromCoordinates,
        that.fromCoordinates) && Objects.equals(toCoordinates, that.toCoordinates);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, pipelineDetail, identNo, fromLocation, fromLatitudeDegrees, fromLatitudeMinutes,
        fromLatitudeSeconds, fromLatitudeDirection, fromLongitudeDegrees, fromLongitudeMinutes, fromLongitudeSeconds,
        fromLongitudeDirection, toLocation, toLatitudeDegrees, toLatitudeMinutes, toLatitudeSeconds,
        toLatitudeDirection,
        toLongitudeDegrees, toLongitudeMinutes, toLongitudeSeconds, toLongitudeDirection, length, isDefiningStructure,
        fromCoordinates, toCoordinates);
  }
}
