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
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineCoreType;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineIdent;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.model.location.LatitudeCoordinate;
import uk.co.ogauthority.pwa.model.location.LongitudeCoordinate;
import uk.co.ogauthority.pwa.service.enums.location.LatitudeDirection;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;

@Entity
@Table(name = "pad_pipeline_idents")
public class PadPipelineIdent implements PipelineIdent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "pp_id")
  private PadPipeline padPipeline;

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

  @Transient
  private CoordinatePair fromCoordinates;

  @Transient
  private CoordinatePair toCoordinates;

  public PadPipelineIdent() {
  }

  public PadPipelineIdent(PadPipeline padPipeline, int identNo) {
    this.padPipeline = padPipeline;
    this.identNo = identNo;
  }

  //Custom behaviour
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

  // Interface implementations
  @Override
  public Integer getPipelineIdentId() {
    return this.id;
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
  public CoordinatePair getFromCoordinates() {
    return this.fromCoordinates;
  }

  @Override
  public CoordinatePair getToCoordinates() {
    return this.toCoordinates;
  }

  @Override
  public PipelineCoreType getPipelineCoreType() {
    return this.padPipeline.getCoreType();
  }

  // Getters

  public Integer getId() {
    return id;
  }

  public PadPipeline getPadPipeline() {
    return padPipeline;
  }

  // Setters
  public void setFromCoordinates(CoordinatePair fromCoordinates) {
    this.fromCoordinates = fromCoordinates;
    updateFromCoordinateValues();
  }

  public void setToCoordinates(CoordinatePair toCoordinates) {
    this.toCoordinates = toCoordinates;
    updateToCoordinateValues();
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public void setPadPipeline(PadPipeline padPipeline) {
    this.padPipeline = padPipeline;
  }

  public void setIdentNo(int identNo) {
    this.identNo = identNo;
  }

  public void setFromLocation(String fromLocation) {
    this.fromLocation = fromLocation;
  }

  public void setToLocation(String toLocation) {
    this.toLocation = toLocation;
  }

  public void setLength(BigDecimal length) {
    this.length = length;
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


}
