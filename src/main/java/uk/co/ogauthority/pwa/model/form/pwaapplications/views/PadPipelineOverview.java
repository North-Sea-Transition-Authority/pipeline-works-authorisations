package uk.co.ogauthority.pwa.model.form.pwaapplications.views;

import com.google.common.annotations.VisibleForTesting;
import java.math.BigDecimal;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineFlexibility;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineMaterial;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineOverview;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineType;
import uk.co.ogauthority.pwa.model.dto.pipelines.PadPipelineSummaryDto;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineDetailSummaryDto;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.enums.aabuilt.AsBuiltNotificationStatus;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;

/*
 * Simple dto for Pipeline objects which obeys the PipelineOverview interface.
 * */
public class PadPipelineOverview implements PipelineOverview {

  private Integer padPipelineId;

  private Integer pipelineId;
  private String fromLocation;
  private CoordinatePair fromCoordinates;
  private String toLocation;
  private CoordinatePair toCoordinates;
  private String pipelineNumber;
  private String temporaryPipelineNumber;
  private PipelineType pipelineType;
  private String componentParts;
  private BigDecimal length;
  private String productsToBeConveyed;
  private Long numberOfIdents;
  private BigDecimal maxExternalDiameter;
  private Boolean pipelineInBundle;
  private String bundleName;
  private PipelineFlexibility pipelineFlexibility;
  private PipelineMaterial pipelineMaterial;
  private String otherPipelineMaterialUsed;
  private Boolean trenchedBuriedBackfilled;
  private String trenchingMethodsDescription;
  private PipelineStatus pipelineStatus;
  private AsBuiltNotificationStatus asBuiltNotificationStatus;
  private String pipelineStatusReason;

  // TODO PWA-890. Remove this attribute and refactor.
  //   Its a rubbish method of determining if tasks apply to a pipeline on the pipeline task-list.
  private Boolean hasTasks;

  private Boolean alreadyExistsOnSeabed;
  private Boolean pipelineInUse;
  private String footnote;



  private PadPipelineOverview(Integer padPipelineId,
                              Integer pipelineId,
                              String fromLocation,
                              CoordinatePair fromCoordinates,
                              String toLocation,
                              CoordinatePair toCoordinates,
                              String pipelineNumber,
                              String temporaryPipelineNumber,
                              PipelineType pipelineType,
                              String componentParts,
                              BigDecimal length,
                              String productsToBeConveyed,
                              Long numberOfIdents,
                              BigDecimal maxExternalDiameter,
                              Boolean pipelineInBundle,
                              String bundleName,
                              PipelineFlexibility pipelineFlexibility,
                              PipelineMaterial pipelineMaterial,
                              String otherPipelineMaterialUsed,
                              Boolean trenchedBuriedBackfilled,
                              String trenchingMethodsDescription,
                              PipelineStatus pipelineStatus,
                              String pipelineStatusReason,
                              Boolean hasTasks,
                              Boolean alreadyExistsOnSeabed,
                              Boolean pipelineInUse,
                              String footnote) {
    this.padPipelineId = padPipelineId;
    this.pipelineId = pipelineId;
    this.fromLocation = fromLocation;
    this.fromCoordinates = fromCoordinates;
    this.toLocation = toLocation;
    this.toCoordinates = toCoordinates;
    this.pipelineNumber = pipelineNumber;
    this.temporaryPipelineNumber = temporaryPipelineNumber;
    this.pipelineType = pipelineType;
    this.componentParts = componentParts;
    this.length = length;
    this.productsToBeConveyed = productsToBeConveyed;
    this.numberOfIdents = numberOfIdents;
    this.maxExternalDiameter = maxExternalDiameter;
    this.pipelineInBundle = pipelineInBundle;
    this.bundleName = bundleName;
    this.pipelineFlexibility = pipelineFlexibility;
    this.pipelineMaterial = pipelineMaterial;
    this.otherPipelineMaterialUsed = otherPipelineMaterialUsed;
    this.trenchedBuriedBackfilled = trenchedBuriedBackfilled;
    this.trenchingMethodsDescription = trenchingMethodsDescription;
    this.pipelineStatus = pipelineStatus;
    this.pipelineStatusReason = pipelineStatusReason;
    this.hasTasks = hasTasks;
    this.alreadyExistsOnSeabed = alreadyExistsOnSeabed;
    this.pipelineInUse = pipelineInUse;
    this.footnote = footnote;
  }

  @VisibleForTesting
  public PadPipelineOverview(PadPipeline padPipeline,
                             Long numberOfIdents) {
    this.padPipelineId = padPipeline.getId();
    this.pipelineId = padPipeline.getPipeline().getId();
    this.fromLocation = padPipeline.getFromLocation();
    this.fromCoordinates = padPipeline.getFromCoordinates();
    this.toLocation = padPipeline.getToLocation();
    this.toCoordinates = padPipeline.getToCoordinates();
    this.pipelineNumber = padPipeline.getPipelineRef();
    this.temporaryPipelineNumber = padPipeline.getTemporaryRef();
    this.pipelineType = padPipeline.getPipelineType();
    this.componentParts = padPipeline.getComponentPartsDescription();
    this.length = padPipeline.getLength();
    this.productsToBeConveyed = padPipeline.getProductsToBeConveyed();
    this.pipelineFlexibility = padPipeline.getPipelineFlexibility();
    this.numberOfIdents = numberOfIdents;
    this.pipelineMaterial = padPipeline.getPipelineMaterial();
    this.otherPipelineMaterialUsed = padPipeline.getOtherPipelineMaterialUsed();
    this.trenchedBuriedBackfilled = padPipeline.getTrenchedBuriedBackfilled();
    this.trenchingMethodsDescription = padPipeline.getTrenchingMethodsDescription();
    this.pipelineStatus = padPipeline.getPipelineStatus();
    this.alreadyExistsOnSeabed = padPipeline.getAlreadyExistsOnSeabed();
    this.pipelineInUse = padPipeline.getPipelineInUse();
    this.footnote = padPipeline.getFootnote();
  }

  @VisibleForTesting
  public PadPipelineOverview(PadPipeline padPipeline) {
    this.padPipelineId = padPipeline.getId();
    this.pipelineNumber = padPipeline.getPipelineRef();
    this.temporaryPipelineNumber = padPipeline.getTemporaryRef();
    this.pipelineType = padPipeline.getPipelineType();
    this.maxExternalDiameter = padPipeline.getMaxExternalDiameter();
    this.pipelineInBundle = padPipeline.getPipelineInBundle();
    this.bundleName = padPipeline.getBundleName();
    this.fromLocation = padPipeline.getFromLocation();
    this.fromCoordinates = padPipeline.getFromCoordinates();
    this.toLocation = padPipeline.getToLocation();
    this.toCoordinates = padPipeline.getToCoordinates();
    this.componentParts = padPipeline.getComponentPartsDescription();
    this.length = padPipeline.getLength();
    this.footnote = padPipeline.getFootnote();
  }

  public static PadPipelineOverview from(PipelineDetailSummaryDto pipelineDetailSummaryDto) {
    return new PadPipelineOverview(
        null,
        pipelineDetailSummaryDto.getPipelineId().asInt(),
        pipelineDetailSummaryDto.getFromLocation(),
        pipelineDetailSummaryDto.getFromCoordinates(),
        pipelineDetailSummaryDto.getToLocation(),
        pipelineDetailSummaryDto.getToCoordinates(),
        pipelineDetailSummaryDto.getPipelineNumber(),
        null, // temporary number never relevant for consented pipelines
        pipelineDetailSummaryDto.getPipelineType(),
        pipelineDetailSummaryDto.getComponentParts(),
        pipelineDetailSummaryDto.getLength(),
        pipelineDetailSummaryDto.getProductsToBeConveyed(),
        pipelineDetailSummaryDto.getNumberOfIdents(),
        pipelineDetailSummaryDto.getMaxExternalDiameter(),
        pipelineDetailSummaryDto.getPipelineInBundle(),
        pipelineDetailSummaryDto.getBundleName(),
        pipelineDetailSummaryDto.getPipelineFlexibility(),
        pipelineDetailSummaryDto.getPipelineMaterial(),
        pipelineDetailSummaryDto.getOtherPipelineMaterialUsed(),
        pipelineDetailSummaryDto.getTrenchedBuriedBackfilled(),
        pipelineDetailSummaryDto.getTrenchingMethodsDescription(),
        pipelineDetailSummaryDto.getPipelineStatus(),
        pipelineDetailSummaryDto.getPipelineStatusReason(),
        false,
        null,
        null,
        null
    );
  }

  public static PadPipelineOverview from(PadPipelineSummaryDto padPipelineSummaryDto, Boolean hasTasks) {

    return new PadPipelineOverview(
        padPipelineSummaryDto.getPadPipelineId(),
        padPipelineSummaryDto.getPipelineId().asInt(),
        padPipelineSummaryDto.getFromLocation(),
        padPipelineSummaryDto.getFromCoordinates(),
        padPipelineSummaryDto.getToLocation(),
        padPipelineSummaryDto.getToCoordinates(),
        padPipelineSummaryDto.getPipelineNumber(),
        padPipelineSummaryDto.getTemporaryPipelineNumber(),
        padPipelineSummaryDto.getPipelineType(),
        padPipelineSummaryDto.getComponentParts(),
        padPipelineSummaryDto.getLength(),
        padPipelineSummaryDto.getProductsToBeConveyed(),
        padPipelineSummaryDto.getNumberOfIdents(),
        padPipelineSummaryDto.getMaxExternalDiameter(),
        padPipelineSummaryDto.getPipelineInBundle(),
        padPipelineSummaryDto.getBundleName(),
        padPipelineSummaryDto.getPipelineFlexibility(),
        padPipelineSummaryDto.getPipelineMaterial(),
        padPipelineSummaryDto.getOtherPipelineMaterialUsed(),
        padPipelineSummaryDto.getTrenchedBuriedBackfilled(),
        padPipelineSummaryDto.getTrenchingMethodsDescription(),
        padPipelineSummaryDto.getPipelineStatus(),
        padPipelineSummaryDto.getPipelineStatusReason(),
        hasTasks,
        padPipelineSummaryDto.getAlreadyExistsOnSeabed(),
        padPipelineSummaryDto.getPipelineInUse(),
        padPipelineSummaryDto.getFootnote()
    );
  }

  public static PipelineOverview from(PipelineOverview pipelineOverview, AsBuiltNotificationStatus asBuiltNotificationStatus) {
    var overview = new PadPipelineOverview(
        pipelineOverview.getPadPipelineId(),
        pipelineOverview.getPipelineId(),
        pipelineOverview.getFromLocation(),
        pipelineOverview.getFromCoordinates(),
        pipelineOverview.getToLocation(),
        pipelineOverview.getToCoordinates(),
        pipelineOverview.getPipelineNumber(),
        pipelineOverview.getTemporaryPipelineNumber(),
        pipelineOverview.getPipelineType(),
        pipelineOverview.getComponentParts(),
        pipelineOverview.getLength(),
        pipelineOverview.getProductsToBeConveyed(),
        pipelineOverview.getNumberOfIdents(),
        pipelineOverview.getMaxExternalDiameter(),
        pipelineOverview.getPipelineInBundle(),
        pipelineOverview.getBundleName(),
        pipelineOverview.getPipelineFlexibility(),
        pipelineOverview.getPipelineMaterial(),
        pipelineOverview.getOtherPipelineMaterialUsed(),
        pipelineOverview.getTrenchedBuriedBackfilled(),
        pipelineOverview.getTrenchingMethodsDescription(),
        pipelineOverview.getPipelineStatus(),
        pipelineOverview.getPipelineStatusReason(),
        false,
        pipelineOverview.getAlreadyExistsOnSeabed(),
        pipelineOverview.getPipelineInUse(),
        pipelineOverview.getFootnote());
    overview.setAsBuiltNotificationStatus(asBuiltNotificationStatus);
    return overview;
  }

  @Override
  public Integer getPadPipelineId() {
    return padPipelineId;
  }

  @Override
  public Integer getPipelineId() {
    return pipelineId;
  }

  @Override
  public String getFromLocation() {
    return fromLocation;
  }

  @Override
  public CoordinatePair getFromCoordinates() {
    return fromCoordinates;
  }

  @Override
  public String getToLocation() {
    return toLocation;
  }

  @Override
  public CoordinatePair getToCoordinates() {
    return toCoordinates;
  }

  @Override
  public String getPipelineNumber() {
    return pipelineNumber;
  }

  @Override
  public PipelineType getPipelineType() {
    return pipelineType;
  }

  @Override
  public String getComponentParts() {
    return componentParts;
  }

  @Override
  public BigDecimal getLength() {
    return length;
  }

  @Override
  public String getProductsToBeConveyed() {
    return productsToBeConveyed;
  }

  @Override
  public Long getNumberOfIdents() {
    return numberOfIdents;
  }

  @Override
  public BigDecimal getMaxExternalDiameter() {
    return maxExternalDiameter;
  }

  @Override
  public Boolean getPipelineInBundle() {
    return pipelineInBundle;
  }

  @Override
  public String getBundleName() {
    return bundleName;
  }

  @Override
  public PipelineFlexibility getPipelineFlexibility() {
    return this.pipelineFlexibility;
  }

  @Override
  public PipelineMaterial getPipelineMaterial() {
    return this.pipelineMaterial;
  }

  @Override
  public String getOtherPipelineMaterialUsed() {
    return otherPipelineMaterialUsed;
  }

  @Override
  public Boolean getTrenchedBuriedBackfilled() {
    return trenchedBuriedBackfilled;
  }

  @Override
  public String getTrenchingMethodsDescription() {
    return trenchingMethodsDescription;
  }

  @Override
  public PipelineStatus getPipelineStatus() {
    return pipelineStatus;
  }

  @Override
  public String getPipelineStatusReason() {
    return pipelineStatusReason;
  }

  @Override
  public String getTemporaryPipelineNumber() {
    return this.temporaryPipelineNumber;
  }

  @Override
  public Boolean getAlreadyExistsOnSeabed() {
    return this.alreadyExistsOnSeabed;
  }

  @Override
  public Boolean getPipelineInUse() {
    return this.pipelineInUse;
  }

  @Override
  public String getFootnote() {
    return this.footnote;
  }

  @Override
  public AsBuiltNotificationStatus getAsBuiltNotificationStatus() {
    return this.asBuiltNotificationStatus;
  }

  public void setAsBuiltNotificationStatus(AsBuiltNotificationStatus asBuiltNotificationStatus) {
    this.asBuiltNotificationStatus = asBuiltNotificationStatus;
  }

  public Boolean getHasTasks() {
    return hasTasks;
  }
}
