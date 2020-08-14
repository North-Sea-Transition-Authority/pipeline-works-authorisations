package uk.co.ogauthority.pwa.model.form.pwaapplications.views;

import com.google.common.annotations.VisibleForTesting;
import java.math.BigDecimal;
import uk.co.ogauthority.pwa.model.dto.pipelines.PadPipelineSummaryDto;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineFlexibility;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineMaterial;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineStatus;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;

/*
 * Simple dto for PadPipelines objects which obeys the PipelineOverview interface
 * */
public class PadPipelineOverview implements PipelineOverview {

  // to be more generic, this should be a composite key of type and id, e.g "PAD_PIPELINE_ID++1" or "PIPELINE_ID++1"
  // best to have the interface handle this kind of thing for us imo.
  // e.g Optional<Integer> getPadPipelineId(); Optional<Integer> getMasterPipelineId(); or similar
  private Integer padPipelineId;

  private Integer pipelineId;
  private String fromLocation;
  private CoordinatePair fromCoordinates;
  private String toLocation;
  private CoordinatePair toCoordinates;
  private String pipelineNumber;
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
  private String pipelineStatusReason;
  private Boolean hasTasks;

  private PadPipelineOverview(Integer padPipelineId,
                              Integer pipelineId,
                              String fromLocation,
                              CoordinatePair fromCoordinates,
                              String toLocation,
                              CoordinatePair toCoordinates,
                              String pipelineNumber,
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
                              Boolean hasTasks) {
    this.padPipelineId = padPipelineId;
    this.pipelineId = pipelineId;
    this.fromLocation = fromLocation;
    this.fromCoordinates = fromCoordinates;
    this.toLocation = toLocation;
    this.toCoordinates = toCoordinates;
    this.pipelineNumber = pipelineNumber;
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
  }

  @VisibleForTesting
  public PadPipelineOverview(PadPipeline padPipeline) {
    this.pipelineNumber = padPipeline.getPipelineRef();
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
        hasTasks
    );
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

  public Boolean getHasTasks() {
    return hasTasks;
  }
}
