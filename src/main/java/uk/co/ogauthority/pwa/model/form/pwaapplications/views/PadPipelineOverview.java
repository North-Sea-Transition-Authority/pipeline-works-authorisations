package uk.co.ogauthority.pwa.model.form.pwaapplications.views;

import com.google.common.annotations.VisibleForTesting;
import java.math.BigDecimal;
import uk.co.ogauthority.pwa.model.dto.pipelines.PadPipelineSummaryDto;
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

  private String fromLocation;

  private String pipelineName;

  private CoordinatePair fromCoordinates;

  private String toLocation;

  private CoordinatePair toCoordinates;

  private String pipelineNumber;

  private PipelineType pipelineType;

  private String componentParts;

  private BigDecimal length;

  private String productsToBeConveyed;

  private Long numberOfIdents;


  private PadPipelineOverview(Integer padPipelineId,
                              String fromLocation,
                              String pipelineName, CoordinatePair fromCoordinates,
                              String toLocation,
                              CoordinatePair toCoordinates,
                              String pipelineNumber,
                              PipelineType pipelineType,
                              String componentParts,
                              BigDecimal length,
                              String productsToBeConveyed,
                              Long numberOfIdents) {
    this.padPipelineId = padPipelineId;
    this.fromLocation = fromLocation;
    this.pipelineName = pipelineName;
    this.fromCoordinates = fromCoordinates;
    this.toLocation = toLocation;
    this.toCoordinates = toCoordinates;
    this.pipelineNumber = pipelineNumber;
    this.pipelineType = pipelineType;
    this.componentParts = componentParts;
    this.length = length;
    this.productsToBeConveyed = productsToBeConveyed;
    this.numberOfIdents = numberOfIdents;
  }

  @VisibleForTesting
  public PadPipelineOverview(PadPipeline padPipeline,
                              Long numberOfIdents) {
    this.padPipelineId = padPipeline.getId();
    this.fromLocation = padPipeline.getFromLocation();
    this.pipelineName = padPipeline.getPipelineName();
    this.fromCoordinates = padPipeline.getFromCoordinates();
    this.toLocation = padPipeline.getToLocation();
    this.toCoordinates = padPipeline.getToCoordinates();
    this.pipelineNumber = padPipeline.getPipelineRef();
    this.pipelineType = padPipeline.getPipelineType();
    this.componentParts = padPipeline.getComponentPartsDescription();
    this.length = padPipeline.getLength();
    this.productsToBeConveyed = padPipeline.getProductsToBeConveyed();
    this.numberOfIdents = numberOfIdents;
  }


  public static PadPipelineOverview from(PadPipelineSummaryDto padPipelineSummaryDto) {

    return new PadPipelineOverview(
        padPipelineSummaryDto.getPadPipelineId(),
        padPipelineSummaryDto.getFromLocation(),
        padPipelineSummaryDto.getPipelineName(), padPipelineSummaryDto.getFromCoordinates(),
        padPipelineSummaryDto.getToLocation(),
        padPipelineSummaryDto.getToCoordinates(),
        padPipelineSummaryDto.getPipelineNumber(),
        padPipelineSummaryDto.getPipelineType(),
        padPipelineSummaryDto.getComponentParts(),
        padPipelineSummaryDto.getLength(),
        padPipelineSummaryDto.getProductsToBeConveyed(),
        padPipelineSummaryDto.getNumberOfIdents()

    );
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
}
