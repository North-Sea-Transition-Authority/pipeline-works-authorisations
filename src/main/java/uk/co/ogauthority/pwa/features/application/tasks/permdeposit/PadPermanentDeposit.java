package uk.co.ogauthority.pwa.features.application.tasks.permdeposit;

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
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.model.location.CoordinatePairEntity;
import uk.co.ogauthority.pwa.service.entitycopier.ChildEntity;
import uk.co.ogauthority.pwa.service.entitycopier.ParentEntity;
import uk.co.ogauthority.pwa.service.enums.location.LatitudeDirection;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;
import uk.co.ogauthority.pwa.util.CoordinateUtils;

@Entity
@Table(name = "pad_permanent_deposits")
public class PadPermanentDeposit implements ChildEntity<Integer, PwaApplicationDetail>, ParentEntity, CoordinatePairEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;
  private Boolean depositForConsentedPipeline;
  private String reference;
  @Column(name = "dep_for_other_app_pipelines")
  private Boolean depositIsForPipelinesOnOtherApp;
  private String appRefAndPipelineNum;

  @JoinColumn(name = "application_detail_id")
  @OneToOne
  private PwaApplicationDetail pwaApplicationDetail;

  private Integer fromMonth;
  private Integer fromYear;
  private Integer toMonth;
  private Integer toYear;

  @Enumerated(EnumType.STRING)
  private MaterialType materialType;
  private String otherMaterialType;
  private String materialSize;
  private BigDecimal concreteMattressLength;
  private BigDecimal concreteMattressWidth;
  private BigDecimal concreteMattressDepth;

  private Boolean groutBagsBioDegradable;
  private String bagsNotUsedDescription;

  private double quantity;
  private String contingencyAmount;

  @Transient
  private CoordinatePair fromCoordinates;

  @Transient
  private CoordinatePair toCoordinates;

  private String footnote;



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

  //ParentEntity methods
  @Override
  public Object getIdAsParent() {
    return this.id;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  public PwaApplicationDetail getPwaApplicationDetail() {
    return pwaApplicationDetail;
  }

  public void setPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail) {
    this.pwaApplicationDetail = pwaApplicationDetail;
  }

  public Integer getFromMonth() {
    return fromMonth;
  }

  public void setFromMonth(Integer fromMonth) {
    this.fromMonth = fromMonth;
  }

  public Integer getFromYear() {
    return fromYear;
  }

  public void setFromYear(Integer fromYear) {
    this.fromYear = fromYear;
  }

  public Integer getToMonth() {
    return toMonth;
  }

  public void setToMonth(Integer toMonth) {
    this.toMonth = toMonth;
  }

  public Integer getToYear() {
    return toYear;
  }

  public void setToYear(Integer toYear) {
    this.toYear = toYear;
  }

  public MaterialType getMaterialType() {
    return materialType;
  }

  public void setMaterialType(MaterialType materialType) {
    this.materialType = materialType;
  }

  public String getOtherMaterialType() {
    return otherMaterialType;
  }

  public void setOtherMaterialType(String otherMaterialType) {
    this.otherMaterialType = otherMaterialType;
  }

  public String getMaterialSize() {
    return materialSize;
  }

  public void setMaterialSize(String materialSize) {
    this.materialSize = materialSize;
  }

  public BigDecimal getConcreteMattressLength() {
    return concreteMattressLength;
  }

  public void setConcreteMattressLength(BigDecimal concreteMattressLength) {
    this.concreteMattressLength = concreteMattressLength;
  }

  public BigDecimal getConcreteMattressWidth() {
    return concreteMattressWidth;
  }

  public void setConcreteMattressWidth(BigDecimal concreteMattressWidth) {
    this.concreteMattressWidth = concreteMattressWidth;
  }

  public BigDecimal getConcreteMattressDepth() {
    return concreteMattressDepth;
  }

  public void setConcreteMattressDepth(BigDecimal concreteMattressDepth) {
    this.concreteMattressDepth = concreteMattressDepth;
  }

  public Boolean getGroutBagsBioDegradable() {
    return groutBagsBioDegradable;
  }

  public void setGroutBagsBioDegradable(Boolean groutBagsBioDegradable) {
    this.groutBagsBioDegradable = groutBagsBioDegradable;
  }

  public String getBagsNotUsedDescription() {
    return bagsNotUsedDescription;
  }

  public void setBagsNotUsedDescription(String bagsNotUsedDescription) {
    this.bagsNotUsedDescription = bagsNotUsedDescription;
  }

  public double getQuantity() {
    return quantity;
  }

  public void setQuantity(double quantity) {
    this.quantity = quantity;
  }

  public String getContingencyAmount() {
    return contingencyAmount;
  }

  public void setContingencyAmount(String contingencyAmount) {
    this.contingencyAmount = contingencyAmount;
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

  public Boolean getDepositForConsentedPipeline() {
    return depositForConsentedPipeline;
  }

  public void setDepositForConsentedPipeline(Boolean depositForConsentedPipeline) {
    this.depositForConsentedPipeline = depositForConsentedPipeline;
  }

  public Boolean getDepositIsForPipelinesOnOtherApp() {
    return depositIsForPipelinesOnOtherApp;
  }

  public void setDepositIsForPipelinesOnOtherApp(Boolean depositIsForPipelinesOnOtherApp) {
    this.depositIsForPipelinesOnOtherApp = depositIsForPipelinesOnOtherApp;
  }

  public String getAppRefAndPipelineNum() {
    return appRefAndPipelineNum;
  }

  public void setAppRefAndPipelineNum(String appRefAndPipelineNum) {
    this.appRefAndPipelineNum = appRefAndPipelineNum;
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

  public String getFootnote() {
    return footnote;
  }

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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PadPermanentDeposit that = (PadPermanentDeposit) o;
    return Double.compare(that.quantity, quantity) == 0
        && Objects.equals(id, that.id)
        && Objects.equals(depositForConsentedPipeline, that.depositForConsentedPipeline)
        && Objects.equals(reference, that.reference)
        && Objects.equals(depositIsForPipelinesOnOtherApp, that.depositIsForPipelinesOnOtherApp)
        && Objects.equals(appRefAndPipelineNum, that.appRefAndPipelineNum)
        && Objects.equals(pwaApplicationDetail, that.pwaApplicationDetail)
        && Objects.equals(fromMonth, that.fromMonth)
        && Objects.equals(fromYear, that.fromYear)
        && Objects.equals(toMonth, that.toMonth)
        && Objects.equals(toYear, that.toYear)
        && materialType == that.materialType
        && Objects.equals(otherMaterialType, that.otherMaterialType)
        && Objects.equals(materialSize, that.materialSize)
        && Objects.equals(concreteMattressLength, that.concreteMattressLength)
        && Objects.equals(concreteMattressWidth, that.concreteMattressWidth)
        && Objects.equals(concreteMattressDepth, that.concreteMattressDepth)
        && Objects.equals(groutBagsBioDegradable, that.groutBagsBioDegradable)
        && Objects.equals(bagsNotUsedDescription, that.bagsNotUsedDescription)
        && Objects.equals(contingencyAmount, that.contingencyAmount)
        && Objects.equals(fromCoordinates, that.fromCoordinates)
        && Objects.equals(toCoordinates, that.toCoordinates)
        && Objects.equals(fromLatitudeDegrees, that.fromLatitudeDegrees)
        && Objects.equals(fromLatitudeMinutes, that.fromLatitudeMinutes)
        && Objects.equals(fromLatitudeSeconds, that.fromLatitudeSeconds)
        && fromLatitudeDirection == that.fromLatitudeDirection
        && Objects.equals(fromLongitudeDegrees, that.fromLongitudeDegrees)
        && Objects.equals(fromLongitudeMinutes, that.fromLongitudeMinutes)
        && Objects.equals(fromLongitudeSeconds, that.fromLongitudeSeconds)
        && fromLongitudeDirection == that.fromLongitudeDirection
        && Objects.equals(toLatitudeDegrees, that.toLatitudeDegrees)
        && Objects.equals(toLatitudeMinutes, that.toLatitudeMinutes)
        && Objects.equals(toLatitudeSeconds, that.toLatitudeSeconds)
        && toLatitudeDirection == that.toLatitudeDirection
        && Objects.equals(toLongitudeDegrees, that.toLongitudeDegrees)
        && Objects.equals(toLongitudeMinutes, that.toLongitudeMinutes)
        && Objects.equals(toLongitudeSeconds, that.toLongitudeSeconds)
        && toLongitudeDirection == that.toLongitudeDirection
        && Objects.equals(footnote, that.footnote);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, depositForConsentedPipeline, reference, depositIsForPipelinesOnOtherApp, appRefAndPipelineNum,
        pwaApplicationDetail, fromMonth, fromYear, toMonth, toYear, materialType, otherMaterialType,
        materialSize, concreteMattressLength, concreteMattressWidth, concreteMattressDepth, groutBagsBioDegradable,
        bagsNotUsedDescription, quantity, contingencyAmount, fromCoordinates, toCoordinates,
        fromLatitudeDegrees, fromLatitudeMinutes, fromLatitudeSeconds, fromLatitudeDirection, fromLongitudeDegrees,
        fromLongitudeMinutes, fromLongitudeSeconds, fromLongitudeDirection, toLatitudeDegrees,
        toLatitudeMinutes, toLatitudeSeconds, toLatitudeDirection, toLongitudeDegrees,
        toLongitudeMinutes, toLongitudeSeconds, toLongitudeDirection, footnote);
  }
}
