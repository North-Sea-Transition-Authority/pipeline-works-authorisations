package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo;

import com.google.common.annotations.VisibleForTesting;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.dto.pipelines.PadPipelineSummaryDto;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineIdentifier;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineIdentifierVisitor;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineSection;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;

/**
 * Captures all the info required by the pipeline huoo options table template.
 */
public class PickableHuooPipelineOption {
  private static final String MISSING_PIPELINE_TYPE_DISPLAY_TEXT = "Pipeline type unavailable";

  private final PickableHuooPipelineType pickableHuooPipelineType;
  private final String pipelineNumber;
  private final String splitInfo;
  private final String pipelineTypeDisplay;
  private final String fromLocation;
  private final CoordinatePair fromCoordinates;
  private final String toLocation;
  private final CoordinatePair toCoordinates;
  private final String length;

  private final String pickableString;

  @VisibleForTesting
  PickableHuooPipelineOption(String pickableString,
                                     String pipelineNumber,
                                     String splitInfo,
                                     String pipelineTypeDisplayName,
                                     String fromLocation,
                                     CoordinatePair fromCoordinates,
                                     String toLocation,
                                     CoordinatePair toCoordinates,
                                     String lengthDisplay) {
    //this.rawId = rawId;
    this.pipelineNumber = pipelineNumber;
    this.splitInfo = splitInfo;
    this.pipelineTypeDisplay = pipelineTypeDisplayName;
    this.fromLocation = fromLocation;
    this.fromCoordinates = fromCoordinates;
    this.toLocation = toLocation;
    this.toCoordinates = toCoordinates;
    this.length = lengthDisplay;

    this.pickableString = pickableString;
    this.pickableHuooPipelineType = PickableHuooPipelineType.getTypeIdFromString(pickableString);

  }

  private static String createPipelineTypeDisplayName(PipelineType pipelineType) {
    return pipelineType != null ? pipelineType.getDisplayName() : MISSING_PIPELINE_TYPE_DISPLAY_TEXT;
  }

  private static String createLengthDisplayString(BigDecimal length) {
    return length != null ? length.setScale(2, RoundingMode.HALF_UP).toPlainString() + "m" : null;
  }

  public static PickableHuooPipelineOption from(PipelineDetail pipelineDetail) {
    return new PickableHuooPipelineOption(
        PickableHuooPipelineType.createPickableStringFrom(pipelineDetail.getPipelineId()),
        pipelineDetail.getPipelineNumber(),
        null, // no split info for whole pipelines
        createPipelineTypeDisplayName(pipelineDetail.getPipelineType()),
        pipelineDetail.getFromLocation(),
        pipelineDetail.getFromCoordinates(),
        pipelineDetail.getToLocation(),
        pipelineDetail.getToCoordinates(),
        createLengthDisplayString(pipelineDetail.getLength())
    );
  }

  public static PickableHuooPipelineOption from(PadPipelineSummaryDto padPipelineSummaryDto) {
    return new PickableHuooPipelineOption(
        PickableHuooPipelineType.createPickableStringFrom(padPipelineSummaryDto.getPipelineId()),
        padPipelineSummaryDto.getPipelineNumber(),
        null, // no split info for whole pipelines
        createPipelineTypeDisplayName(padPipelineSummaryDto.getPipelineType()),
        padPipelineSummaryDto.getFromLocation(),
        padPipelineSummaryDto.getFromCoordinates(),
        padPipelineSummaryDto.getToLocation(),
        padPipelineSummaryDto.getToCoordinates(),
        createLengthDisplayString(padPipelineSummaryDto.getLength())
    );
  }

  public static PickableHuooPipelineOption from(PipelineOverview pipelineOverview) {
    return new PickableHuooPipelineOption(
        PickableHuooPipelineType.createPickableStringFrom(PipelineId.from(pipelineOverview)),
        pipelineOverview.getPipelineNumber(),
        null, // no split info for whole pipelines
        createPipelineTypeDisplayName(pipelineOverview.getPipelineType()),
        pipelineOverview.getFromLocation(),
        pipelineOverview.getFromCoordinates(),
        pipelineOverview.getToLocation(),
        pipelineOverview.getToCoordinates(),
        createLengthDisplayString(pipelineOverview.getLength())
    );
  }

  public static PickableHuooPipelineOption duplicateOptionForPipelineIdentifier(PipelineIdentifier pipelineIdentifier,
                                                                                PickableHuooPipelineOption pickableHuooPipelineOption) {
    var pickableStringVisitor = new PipelineIdentifierPickableStringVisitor();
    pipelineIdentifier.accept(pickableStringVisitor);
    var splitInfoVisitor = new PipelineIdentifierSplitInfoVisitor();
    pipelineIdentifier.accept(splitInfoVisitor);

    return new PickableHuooPipelineOption(
        pickableStringVisitor.getPickableString(),
        pickableHuooPipelineOption.getPipelineNumber(),
        splitInfoVisitor.getSplitInfoDetails(),
        pickableHuooPipelineOption.getPipelineTypeDisplay(),
        pickableHuooPipelineOption.getFromLocation(),
        pickableHuooPipelineOption.getFromCoordinates(),
        pickableHuooPipelineOption.getToLocation(),
        pickableHuooPipelineOption.getToCoordinates(),
        pickableHuooPipelineOption.getLength()
    );
  }

  public String getPickableString() {
    return pickableString;
  }

  public PipelineIdentifier asPipelineIdentifier() {
    return PickableHuooPipelineType.decodeString(this.pickableString)
        .orElseThrow(() -> new PwaEntityNotFoundException("Unable to decode pickable string:" + this.pickableString));
  }

  PickableHuooPipelineType getPickableHuooPipelineType() {
    return pickableHuooPipelineType;
  }

  public String getSplitInfo() {
    return splitInfo;
  }

  public String getPipelineNumber() {
    return pipelineNumber;
  }

  public String getPipelineTypeDisplay() {
    return pipelineTypeDisplay;
  }

  public String getFromLocation() {
    return fromLocation;
  }

  public String getToLocation() {
    return toLocation;
  }

  public String getLength() {
    return length;
  }

  public Boolean hasFromCoordinates() {
    return this.fromCoordinates != null;
  }

  public Boolean hasToCoordinates() {
    return this.toCoordinates != null;
  }

  public CoordinatePair getFromCoordinates() {
    return fromCoordinates;
  }

  public CoordinatePair getToCoordinates() {
    return toCoordinates;
  }

  public PickableHuooPipelineId generatePickableHuooPipelineId() {
    return PickableHuooPipelineId.from(this.pickableString);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PickableHuooPipelineOption that = (PickableHuooPipelineOption) o;
    return pickableHuooPipelineType == that.pickableHuooPipelineType
        && Objects.equals(pipelineNumber, that.pipelineNumber)
        && Objects.equals(splitInfo, that.splitInfo)
        && Objects.equals(pipelineTypeDisplay, that.pipelineTypeDisplay)
        && Objects.equals(fromLocation, that.fromLocation)
        && Objects.equals(fromCoordinates, that.fromCoordinates)
        && Objects.equals(toLocation, that.toLocation)
        && Objects.equals(toCoordinates, that.toCoordinates)
        && Objects.equals(length, that.length)
        && Objects.equals(pickableString, that.pickableString);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pickableHuooPipelineType, pipelineNumber, splitInfo, pipelineTypeDisplay, fromLocation,
        fromCoordinates, toLocation, toCoordinates, length, pickableString);
  }

  /* Single use case visitor that doesnt make a great deal of sense outside of this context.
  * Visit pipelineIdentifiers and produce a string that can be used to describe a split in the identified pipeline. */
  private static class PipelineIdentifierSplitInfoVisitor implements PipelineIdentifierVisitor {

    // effectively final after visit
    private String splitInfoDetails;

    @Override
    public void visit(PipelineId pipelineId) {
      splitInfoDetails = null;
    }

    @Override
    public void visit(PipelineSection pipelineSection) {
      this.splitInfoDetails = StringUtils.capitalize(pipelineSection.getDisplayString());
    }

    public String getSplitInfoDetails() {
      return splitInfoDetails;
    }
  }
}
