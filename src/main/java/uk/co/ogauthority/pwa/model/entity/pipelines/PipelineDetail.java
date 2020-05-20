package uk.co.ogauthority.pwa.model.entity.pipelines;

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
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.service.enums.location.LatitudeDirection;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;
import uk.co.ogauthority.pwa.temp.model.service.PipelineType;

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
  private String pipelineStatus;
  private String detailStatus;
  private String pipelineNumber;

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

  public String getPipelineStatus() {
    return pipelineStatus;
  }

  public void setPipelineStatus(String pipelineStatus) {
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

  public void setFromLatitudeDegrees(Integer fromLatitudeDegrees) {
    this.fromLatitudeDegrees = fromLatitudeDegrees;
  }

  public Integer getFromLatitudeMinutes() {
    return fromLatitudeMinutes;
  }

  public void setFromLatitudeMinutes(Integer fromLatitudeMinutes) {
    this.fromLatitudeMinutes = fromLatitudeMinutes;
  }

  public BigDecimal getFromLatitudeSeconds() {
    return fromLatitudeSeconds;
  }

  public void setFromLatitudeSeconds(BigDecimal fromLatitudeSeconds) {
    this.fromLatitudeSeconds = fromLatitudeSeconds;
  }

  public LatitudeDirection getFromLatitudeDirection() {
    return fromLatitudeDirection;
  }

  public void setFromLatitudeDirection(LatitudeDirection fromLatitudeDirection) {
    this.fromLatitudeDirection = fromLatitudeDirection;
  }

  public Integer getFromLongitudeDegrees() {
    return fromLongitudeDegrees;
  }

  public void setFromLongitudeDegrees(Integer fromLongitudeDegrees) {
    this.fromLongitudeDegrees = fromLongitudeDegrees;
  }

  public Integer getFromLongitudeMinutes() {
    return fromLongitudeMinutes;
  }

  public void setFromLongitudeMinutes(Integer fromLongitudeMinutes) {
    this.fromLongitudeMinutes = fromLongitudeMinutes;
  }

  public BigDecimal getFromLongitudeSeconds() {
    return fromLongitudeSeconds;
  }

  public void setFromLongitudeSeconds(BigDecimal fromLongitudeSeconds) {
    this.fromLongitudeSeconds = fromLongitudeSeconds;
  }

  public LongitudeDirection getFromLongitudeDirection() {
    return fromLongitudeDirection;
  }

  public void setFromLongitudeDirection(LongitudeDirection fromLongitudeDirection) {
    this.fromLongitudeDirection = fromLongitudeDirection;
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

  public void setToLatitudeDegrees(Integer toLatitudeDegrees) {
    this.toLatitudeDegrees = toLatitudeDegrees;
  }

  public Integer getToLatitudeMinutes() {
    return toLatitudeMinutes;
  }

  public void setToLatitudeMinutes(Integer toLatitudeMinutes) {
    this.toLatitudeMinutes = toLatitudeMinutes;
  }

  public BigDecimal getToLatitudeSeconds() {
    return toLatitudeSeconds;
  }

  public void setToLatitudeSeconds(BigDecimal toLatitudeSeconds) {
    this.toLatitudeSeconds = toLatitudeSeconds;
  }

  public LatitudeDirection getToLatitudeDirection() {
    return toLatitudeDirection;
  }

  public void setToLatitudeDirection(LatitudeDirection toLatitudeDirection) {
    this.toLatitudeDirection = toLatitudeDirection;
  }

  public Integer getToLongitudeDegrees() {
    return toLongitudeDegrees;
  }

  public void setToLongitudeDegrees(Integer toLongitudeDegrees) {
    this.toLongitudeDegrees = toLongitudeDegrees;
  }

  public Integer getToLongitudeMinutes() {
    return toLongitudeMinutes;
  }

  public void setToLongitudeMinutes(Integer toLongitudeMinutes) {
    this.toLongitudeMinutes = toLongitudeMinutes;
  }

  public BigDecimal getToLongitudeSeconds() {
    return toLongitudeSeconds;
  }

  public void setToLongitudeSeconds(BigDecimal toLongitudeSeconds) {
    this.toLongitudeSeconds = toLongitudeSeconds;
  }

  public LongitudeDirection getToLongitudeDirection() {
    return toLongitudeDirection;
  }

  public void setToLongitudeDirection(LongitudeDirection toLongitudeDirection) {
    this.toLongitudeDirection = toLongitudeDirection;
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
}
