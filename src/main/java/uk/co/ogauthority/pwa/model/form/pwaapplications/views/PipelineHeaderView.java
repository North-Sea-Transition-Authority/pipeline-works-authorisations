package uk.co.ogauthority.pwa.model.form.pwaapplications.views;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineFlexibility;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineMaterial;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineOverview;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineType;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PipelineHeaderQuestion;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.CoordinatePair;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;

public class PipelineHeaderView implements PipelineOverview {

  private final Integer padPipelineId;
  private final String pipelineName;
  private final Integer pipelineId;
  private final String fromLocation;
  private final CoordinatePair fromCoordinates;
  private final String toLocation;
  private final CoordinatePair toCoordinates;
  private final String pipelineNumber;
  private final String temporaryPipelineNumber;
  private final PipelineType pipelineType;
  private final String componentParts;
  private final String length;
  private final String productsToBeConveyed;
  private final Integer numberOfIdents;
  private final String maxExternalDiameter;
  private final Boolean pipelineInBundle;
  private final String bundleName;
  private final PipelineFlexibility pipelineFlexibility;
  private final PipelineMaterial pipelineMaterial;
  private final String otherPipelineMaterialUsed;
  private final Boolean trenchedBuriedBackfilled;
  private final String trenchingMethodsDescription;
  private final PipelineStatus pipelineStatus;
  private final String pipelineStatusDisplayStr;
  private final String pipelineStatusReason;
  private final Boolean alreadyExistsOnSeabed;
  private final Boolean pipelineInUse;
  private final String footnote;
  private final Set<PipelineHeaderQuestion> headerQuestions;
  private final String transferredFromRef;
  private final String transferredToRef;

  public PipelineHeaderView() {
    this.padPipelineId = null;
    this.pipelineName = null;
    this.pipelineId = null;
    this.fromLocation = null;
    this.fromCoordinates = null;
    this.toLocation = null;
    this.toCoordinates = null;
    this.pipelineNumber = null;
    this.temporaryPipelineNumber = null;
    this.pipelineType = null;
    this.componentParts = null;
    this.length = null;
    this.productsToBeConveyed = null;
    this.numberOfIdents = null;
    this.maxExternalDiameter = null;
    this.pipelineInBundle = null;
    this.bundleName = null;
    this.pipelineFlexibility = null;
    this.pipelineMaterial = null;
    this.otherPipelineMaterialUsed = null;
    this.trenchedBuriedBackfilled = null;
    this.trenchingMethodsDescription = null;
    this.pipelineStatus = null;
    this.pipelineStatusDisplayStr = null;
    this.pipelineStatusReason = null;
    this.alreadyExistsOnSeabed = null;
    this.pipelineInUse = null;
    this.footnote = null;
    this.headerQuestions = Set.of();
    this.transferredFromRef = null;
    this.transferredToRef = null;
  }

  public PipelineHeaderView(PipelineOverview pipelineOverview, String transferredFromRef, String transferredToRef) {
    this.pipelineId = pipelineOverview.getPipelineId();
    this.pipelineName = pipelineOverview.getPipelineName();
    this.padPipelineId = pipelineOverview.getPadPipelineId();
    this.fromLocation = pipelineOverview.getFromLocation();
    this.fromCoordinates = pipelineOverview.getFromCoordinates();
    this.toLocation = pipelineOverview.getToLocation();
    this.toCoordinates = pipelineOverview.getToCoordinates();
    this.pipelineNumber = pipelineOverview.getPipelineNumber();
    this.temporaryPipelineNumber = pipelineOverview.getTemporaryPipelineNumber();
    this.pipelineType = pipelineOverview.getPipelineType();
    this.componentParts = pipelineOverview.getComponentParts();
    this.length = String.valueOf(pipelineOverview.getLength());
    this.productsToBeConveyed = pipelineOverview.getProductsToBeConveyed();
    this.numberOfIdents = (int) (long) pipelineOverview.getNumberOfIdents();
    this.maxExternalDiameter = pipelineOverview.getMaxExternalDiameter() != null
        ? String.valueOf(pipelineOverview.getMaxExternalDiameter()) : null;
    this.pipelineInBundle = pipelineOverview.getPipelineInBundle();
    this.bundleName = pipelineOverview.getBundleName();
    this.pipelineFlexibility = pipelineOverview.getPipelineFlexibility();
    this.pipelineMaterial = pipelineOverview.getPipelineMaterial();
    this.otherPipelineMaterialUsed = pipelineOverview.getOtherPipelineMaterialUsed();
    this.trenchedBuriedBackfilled = pipelineOverview.getTrenchedBuriedBackfilled();
    this.trenchingMethodsDescription = pipelineOverview.getTrenchingMethodsDescription();
    this.pipelineStatus = pipelineOverview.getPipelineStatus();
    this.pipelineStatusDisplayStr = pipelineOverview.getPipelineStatus().getDisplayText();
    this.pipelineStatusReason = pipelineOverview.getPipelineStatusReason();
    this.alreadyExistsOnSeabed = pipelineOverview.getAlreadyExistsOnSeabed();
    this.pipelineInUse = pipelineOverview.getPipelineInUse();
    this.footnote = pipelineOverview.getFootnote();
    this.headerQuestions = getRelevantQuestions();
    this.transferredFromRef = transferredFromRef;
    this.transferredToRef = transferredToRef;
  }

  public PipelineHeaderView(PipelineDetail pipelineDetail, String transferredFromRef, String transferredToRef) {
    this.padPipelineId = null;
    this.pipelineName = null;
    this.pipelineId = pipelineDetail.getPipelineId().asInt();
    this.fromLocation = pipelineDetail.getFromLocation();
    this.fromCoordinates = pipelineDetail.getFromCoordinates();
    this.toLocation = pipelineDetail.getToLocation();
    this.toCoordinates = pipelineDetail.getToCoordinates();
    this.pipelineNumber = pipelineDetail.getPipelineNumber();
    this.temporaryPipelineNumber = null; // never makes sense for this to have value here
    this.pipelineType = pipelineDetail.getPipelineType();
    this.componentParts = pipelineDetail.getComponentPartsDescription();
    this.length = String.valueOf(pipelineDetail.getLength());
    this.productsToBeConveyed = pipelineDetail.getProductsToBeConveyed();
    this.maxExternalDiameter = pipelineDetail.getMaxExternalDiameter() != null
        ? String.valueOf(pipelineDetail.getMaxExternalDiameter()) : null;
    this.pipelineInBundle = pipelineDetail.getPipelineInBundle();
    this.bundleName = pipelineDetail.getBundleName();
    this.pipelineStatus = pipelineDetail.getPipelineStatus();
    this.pipelineStatusDisplayStr = pipelineDetail.getPipelineStatus().getDisplayText();
    this.pipelineStatusReason = pipelineDetail.getPipelineStatusReason();
    this.numberOfIdents = null;
    this.pipelineFlexibility = null;
    this.pipelineMaterial = null;
    this.otherPipelineMaterialUsed = null;
    this.trenchedBuriedBackfilled = null;
    this.trenchingMethodsDescription = null;
    this.alreadyExistsOnSeabed = null;
    this.pipelineInUse = null;
    this.footnote = pipelineDetail.getFootnote();
    this.headerQuestions = getRelevantQuestions();
    this.transferredFromRef = transferredFromRef;
    this.transferredToRef = transferredToRef;
  }

  @Override
  public Integer getPadPipelineId() {
    return padPipelineId;
  }

  @Override
  public String getPipelineName() {
    return pipelineName;
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
    return new BigDecimal(length);
  }

  @Override
  public String getProductsToBeConveyed() {
    return productsToBeConveyed;
  }

  @Override
  public Long getNumberOfIdents() {
    return Long.valueOf(numberOfIdents);
  }

  @Override
  public BigDecimal getMaxExternalDiameter() {
    return maxExternalDiameter != null ? new BigDecimal(maxExternalDiameter) : null;
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

  public String getPipelineStatusDisplayStr() {
    return pipelineStatusDisplayStr;
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
    return alreadyExistsOnSeabed;
  }

  @Override
  public Boolean getPipelineInUse() {
    return pipelineInUse;
  }

  @Override
  public String getFootnote() {
    return footnote;
  }

  public Set<PipelineHeaderQuestion> getHeaderQuestions() {
    return headerQuestions;
  }

  public String getTransferredFromRef() {
    return transferredFromRef;
  }

  public String getTransferredToRef() {
    return transferredToRef;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PipelineHeaderView that = (PipelineHeaderView) o;
    return Objects.equals(padPipelineId, that.padPipelineId)
        && Objects.equals(pipelineName, that.pipelineName)
        && Objects.equals(pipelineId, that.pipelineId)
        && Objects.equals(fromLocation, that.fromLocation)
        && Objects.equals(fromCoordinates, that.fromCoordinates)
        && Objects.equals(toLocation, that.toLocation)
        && Objects.equals(toCoordinates, that.toCoordinates)
        && Objects.equals(pipelineNumber, that.pipelineNumber)
        && Objects.equals(temporaryPipelineNumber, that.temporaryPipelineNumber)
        && pipelineType == that.pipelineType
        && Objects.equals(componentParts, that.componentParts)
        && Objects.equals(length, that.length)
        && Objects.equals(productsToBeConveyed, that.productsToBeConveyed)
        && Objects.equals(numberOfIdents, that.numberOfIdents)
        && Objects.equals(maxExternalDiameter, that.maxExternalDiameter)
        && Objects.equals(pipelineInBundle, that.pipelineInBundle)
        && Objects.equals(bundleName, that.bundleName)
        && pipelineFlexibility == that.pipelineFlexibility
        && pipelineMaterial == that.pipelineMaterial
        && Objects.equals(otherPipelineMaterialUsed, that.otherPipelineMaterialUsed)
        && Objects.equals(trenchedBuriedBackfilled, that.trenchedBuriedBackfilled)
        && Objects.equals(trenchingMethodsDescription, that.trenchingMethodsDescription)
        && pipelineStatus == that.pipelineStatus
        && Objects.equals(pipelineStatusDisplayStr, that.pipelineStatusDisplayStr)
        && Objects.equals(pipelineStatusReason, that.pipelineStatusReason)
        && Objects.equals(alreadyExistsOnSeabed, that.alreadyExistsOnSeabed)
        && Objects.equals(pipelineInUse, that.pipelineInUse)
        && Objects.equals(footnote, that.footnote);
  }

  @Override
  public int hashCode() {
    return Objects.hash(padPipelineId, pipelineName, pipelineId, fromLocation, fromCoordinates, toLocation,
        toCoordinates,
        pipelineNumber, temporaryPipelineNumber, pipelineType, componentParts, length, productsToBeConveyed,
        numberOfIdents, maxExternalDiameter, pipelineInBundle, bundleName, pipelineFlexibility, pipelineMaterial,
        otherPipelineMaterialUsed, trenchedBuriedBackfilled, trenchingMethodsDescription, pipelineStatus,
        pipelineStatusDisplayStr, pipelineStatusReason, alreadyExistsOnSeabed, pipelineInUse, footnote);
  }
}
