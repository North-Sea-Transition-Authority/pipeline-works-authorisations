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
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineCoreType;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineIdent;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.model.location.CoordinatePairEntity;
import uk.co.ogauthority.pwa.service.entitycopier.ChildEntity;
import uk.co.ogauthority.pwa.service.entitycopier.ParentEntity;
import uk.co.ogauthority.pwa.service.enums.location.LatitudeDirection;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;
import uk.co.ogauthority.pwa.util.CoordinateUtils;

@Entity
@Table(name = "pad_pipeline_idents")
public class PadPipelineIdent implements PipelineIdent, ChildEntity<Integer, PadPipeline>, ParentEntity, CoordinatePairEntity {

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

  private Boolean isDefiningStructure;

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
    this.fromCoordinates = CoordinateUtils.buildFromCoordinatePair(this);
    this.toCoordinates = CoordinateUtils.buildToCoordinatePair(this);
  }

  // PipelineIdentMethods
  @Override
  public Integer getPipelineIdentId() {
    return this.id;
  }

  @Override
  public PipelineId getPipelineId() {
    return this.padPipeline.getPipelineId();
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
    return this.padPipeline.getCoreType();
  }

  // ChildEntity methods
  @Override
  public void clearId() {
    this.id = null;
  }

  @Override
  public void setParent(PadPipeline parentEntity) {
    this.padPipeline = parentEntity;
  }

  // ParentEntity Methods
  @Override
  public Object getIdAsParent() {
    return this.getId();
  }

  @Override
  public PadPipeline getParent() {
    return this.padPipeline;
  }

  // Getters
  public Integer getId() {
    return id;
  }

  public PadPipeline getPadPipeline() {
    return padPipeline;
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

  public void setPadPipeline(PadPipeline padPipeline) {
    this.padPipeline = padPipeline;
  }

  @Override
  public void setIdentNo(int identNo) {
    this.identNo = identNo;
  }

  @Override
  public void setFromLocation(String fromLocation) {
    this.fromLocation = fromLocation;
  }

  @Override
  public void setToLocation(String toLocation) {
    this.toLocation = toLocation;
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
