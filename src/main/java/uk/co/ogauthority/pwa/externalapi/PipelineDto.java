package uk.co.ogauthority.pwa.externalapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.co.ogauthority.pwa.model.entity.enums.MasterPwaDetailStatus;
import uk.co.ogauthority.pwa.util.pipelines.PipelineNumberSortingUtil;

public class PipelineDto implements Comparable<PipelineDto> {
  private final Integer id;
  private final String pipelineNumber;
  private final String toLocation;
  private final String fromLocation;
  private final PwaDto pwa;

  public PipelineDto(Integer id, String pipelineNumber, String toLocation, String fromLocation,
                     Integer pwaId, String pwaReference, MasterPwaDetailStatus pwaStatus) {
    this.id = id;
    this.pipelineNumber = pipelineNumber;
    this.toLocation = toLocation;
    this.fromLocation = fromLocation;
    this.pwa = new PwaDto(pwaId, pwaReference, pwaStatus);
  }

  // No-args constructor required for Jackson mapping in controller test
  private PipelineDto() {
    id = null;
    pipelineNumber = null;
    pwa = null;
    toLocation = null;
    fromLocation = null;
  }

  @JsonProperty
  public Integer getId() {
    return id;
  }

  @JsonProperty
  public String getPipelineNumber() {
    return pipelineNumber;
  }

  @JsonProperty
  public String getToLocation() {
    return toLocation;
  }

  @JsonProperty
  public String getFromLocation() {
    return fromLocation;
  }

  @JsonProperty
  public PwaDto getPwa() {
    return pwa;
  }

  @Override
  public int compareTo(PipelineDto pipelineDtoToCompare) {
    return PipelineNumberSortingUtil.compare(this.getPipelineNumber(), pipelineDtoToCompare.getPipelineNumber());
  }
}
