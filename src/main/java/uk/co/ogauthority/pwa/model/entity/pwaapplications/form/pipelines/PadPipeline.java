package uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines;

import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.Transient;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.model.location.LatitudeCoordinate;
import uk.co.ogauthority.pwa.model.location.LongitudeCoordinate;
import uk.co.ogauthority.pwa.service.enums.location.LatitudeDirection;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;

@Entity(name = "pad_pipelines")
public class PadPipeline {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "pad_id")
  private PwaApplicationDetail pwaApplicationDetail;

  @OneToOne
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

  public PadPipeline() {
  }

  public PadPipeline(PwaApplicationDetail pwaApplicationDetail) {
    this.pwaApplicationDetail = pwaApplicationDetail;
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

  public void setPwaApplicationDetail(
      PwaApplicationDetail pwaApplicationDetail) {
    this.pwaApplicationDetail = pwaApplicationDetail;
  }

  public Pipeline getPipeline() {
    return pipeline;
  }

  public void setPipeline(Pipeline pipeline) {
    this.pipeline = pipeline;
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

  public String getToLocation() {
    return toLocation;
  }

  public void setToLocation(String toLocation) {
    this.toLocation = toLocation;
  }

  public String getComponentPartsDescription() {
    return componentPartsDescription;
  }

  public void setComponentPartsDescription(String componentPartsDescription) {
    this.componentPartsDescription = componentPartsDescription;
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

  public Boolean getTrenchedBuriedBackfilled() {
    return trenchedBuriedBackfilled;
  }

  public void setTrenchedBuriedBackfilled(Boolean trenchedBuriedBackfilled) {
    this.trenchedBuriedBackfilled = trenchedBuriedBackfilled;
  }

  public String getTrenchingMethodsDescription() {
    return trenchingMethodsDescription;
  }

  public void setTrenchingMethodsDescription(String trenchingMethodsDescription) {
    this.trenchingMethodsDescription = trenchingMethodsDescription;
  }

  public String getPipelineRef() {
    return pipelineRef;
  }

  public void setPipelineRef(String pipelineRef) {
    this.pipelineRef = pipelineRef;
  }

  public CoordinatePair getFromCoordinates() {
    return fromCoordinates;
  }

  public void setFromCoordinates(CoordinatePair fromCoordinates) {
    this.fromCoordinates = fromCoordinates;
  }

  public CoordinatePair getToCoordinates() {
    return toCoordinates;
  }

  public void setToCoordinates(CoordinatePair toCoordinates) {
    this.toCoordinates = toCoordinates;
  }

  @PrePersist
  public void prePersist() {

    this.fromLatitudeDegrees = this.fromCoordinates.getLatitude().getDegrees();
    this.fromLatitudeMinutes = this.fromCoordinates.getLatitude().getMinutes();
    this.fromLatitudeSeconds = this.fromCoordinates.getLatitude().getSeconds();
    this.fromLatitudeDirection = this.fromCoordinates.getLatitude().getDirection();

    this.fromLongitudeDegrees = this.fromCoordinates.getLongitude().getDegrees();
    this.fromLongitudeMinutes = this.fromCoordinates.getLongitude().getMinutes();
    this.fromLongitudeSeconds = this.fromCoordinates.getLongitude().getSeconds();
    this.fromLongitudeDirection = this.fromCoordinates.getLongitude().getDirection();

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

    this.fromCoordinates = new CoordinatePair(
        new LatitudeCoordinate(this.fromLatitudeDegrees, this.fromLatitudeMinutes, this.fromLatitudeSeconds, this.fromLatitudeDirection),
        new LongitudeCoordinate(
            this.fromLongitudeDegrees,
            this.fromLongitudeMinutes,
            this.fromLongitudeSeconds,
            this.fromLongitudeDirection)
    );

    this.toCoordinates = new CoordinatePair(
        new LatitudeCoordinate(this.toLatitudeDegrees, this.toLatitudeMinutes, this.toLatitudeSeconds, this.toLatitudeDirection),
        new LongitudeCoordinate(this.toLongitudeDegrees, this.toLongitudeMinutes, this.toLongitudeSeconds, this.toLongitudeDirection)
    );

  }

}
