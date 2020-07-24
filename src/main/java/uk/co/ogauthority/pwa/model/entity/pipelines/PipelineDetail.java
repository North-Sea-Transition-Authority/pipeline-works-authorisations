package uk.co.ogauthority.pwa.model.entity.pipelines;

import com.google.common.annotations.VisibleForTesting;
import java.math.BigDecimal;
import java.time.Instant;
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
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineStatus;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.model.location.LatitudeCoordinate;
import uk.co.ogauthority.pwa.model.location.LongitudeCoordinate;
import uk.co.ogauthority.pwa.service.enums.location.LatitudeDirection;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;


@Entity
@Table(name = "pipeline_details")
public class PipelineDetail {

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

  private String detailStatus;
  private String pipelineNumber;
  private BigDecimal maxExternalDiameter;
  private Boolean pipelineInBundle;
  private String bundleName;

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

  @Transient
  private CoordinatePair fromCoordinates;

  @Transient
  private CoordinatePair toCoordinates;

  public PipelineDetail() {
    // default for hibernate
  }

  @VisibleForTesting
  public PipelineDetail(Pipeline pipeline) {
    this.setPipeline(pipeline);
  }


  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Pipeline getPipeline() {
    return pipeline;
  }

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

  public PipelineStatus getPipelineStatus() {
    return pipelineStatus;
  }

  public void setPipelineStatus(PipelineStatus pipelineStatus) {
    this.pipelineStatus = pipelineStatus;
  }

  public String getDetailStatus() {
    return detailStatus;
  }

  public void setDetailStatus(String detailStatus) {
    this.detailStatus = detailStatus;
  }

  public String getPipelineNumber() {
    return pipelineNumber;
  }

  public void setPipelineNumber(String pipelineNumber) {
    this.pipelineNumber = pipelineNumber;
  }

  public PwaConsent getPwaConsent() {
    return pwaConsent;
  }

  public void setPwaConsent(PwaConsent pwaConsent) {
    this.pwaConsent = pwaConsent;
  }

  public PipelineType getPipelineType() {
    return pipelineType;
  }

  public void setPipelineType(PipelineType pipelineType) {
    this.pipelineType = pipelineType;
  }

  public String getFromLocation() {
    return fromLocation;
  }

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

  public String getToLocation() {
    return toLocation;
  }

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

  public String getComponentPartsDesc() {
    return componentPartsDesc;
  }

  public void setComponentPartsDesc(String componentPartsDesc) {
    this.componentPartsDesc = componentPartsDesc;
  }

  public BigDecimal getLength() {
    return length;
  }

  public void setLength(BigDecimal length) {
    this.length = length;
  }

  public String getProductsToBeConveyed() {
    return productsToBeConveyed;
  }

  public void setProductsToBeConveyed(String productsToBeConveyed) {
    this.productsToBeConveyed = productsToBeConveyed;
  }

  public Boolean getTrenchedBuriedFilledFlag() {
    return trenchedBuriedFilledFlag;
  }

  public void setTrenchedBuriedFilledFlag(Boolean trenchedBuriedFilledFlag) {
    this.trenchedBuriedFilledFlag = trenchedBuriedFilledFlag;
  }

  public String getTrenchingMethodsDesc() {
    return trenchingMethodsDesc;
  }

  public void setTrenchingMethodsDesc(String trenchingMethodsDesc) {
    this.trenchingMethodsDesc = trenchingMethodsDesc;
  }

  public int getPipelineId() {
    return this.getPipeline().getId();
  }

  public BigDecimal getMaxExternalDiameter() {
    return maxExternalDiameter;
  }

  public void setMaxExternalDiameter(BigDecimal maxExternalDiameter) {
    this.maxExternalDiameter = maxExternalDiameter;
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

  public CoordinatePair getFromCoordinates() {
    return fromCoordinates;
  }

  public void setFromCoordinates(CoordinatePair fromCoordinates) {
    this.fromCoordinates = fromCoordinates;
    updateFromCoordinateValues();
  }

  public CoordinatePair getToCoordinates() {
    return toCoordinates;
  }

  public void setToCoordinates(CoordinatePair toCoordinates) {
    this.toCoordinates = toCoordinates;
    updateToCoordinateValues();
  }

  public Boolean getPipelineInBundle() {
    return pipelineInBundle;
  }

  public void setPipelineInBundle(Boolean pipelineInBundle) {
    this.pipelineInBundle = pipelineInBundle;
  }

  public String getBundleName() {
    return bundleName;
  }

  public void setBundleName(String bundleName) {
    this.bundleName = bundleName;
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

}
