package uk.co.ogauthority.pwa.externalapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.co.ogauthority.pwa.util.pipelines.PipelineNumberSortingUtil;

public class PipelineDto implements Comparable<PipelineDto> {
  private final Integer id;
  private final String pipelineNumber;
  private final PwaDto pwa;

  public PipelineDto(Integer id, String pipelineNumber, Integer pwaId, String pwaReference) {
    this.id = id;
    this.pipelineNumber = pipelineNumber;
    this.pwa = new PwaDto(pwaId, pwaReference);
  }

  // No-args constructor required for Jackson mapping in controller test
  private PipelineDto() {
    id = null;
    pipelineNumber = null;
    pwa = null;
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
  public PwaDto getPwa() {
    return pwa;
  }

  @Override
  public int compareTo(PipelineDto pipelineDtoToCompare) {
    return PipelineNumberSortingUtil.compare(this.getPipelineNumber(), pipelineDtoToCompare.getPipelineNumber());
  }
}
