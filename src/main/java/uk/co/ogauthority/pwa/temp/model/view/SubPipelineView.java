package uk.co.ogauthority.pwa.temp.model.view;

import java.io.Serializable;
import java.util.Objects;
import uk.co.ogauthority.pwa.temp.model.service.PipelineType;

public class SubPipelineView implements Serializable {

  private String pipelineNumber;

  private PipelineType pipelineType;

  public SubPipelineView() {
  }

  public String getPipelineNumber() {
    return pipelineNumber;
  }

  public void setPipelineNumber(String pipelineNumber) {
    this.pipelineNumber = pipelineNumber;
  }

  public PipelineType getPipelineType() {
    return pipelineType;
  }

  public void setPipelineType(PipelineType pipelineType) {
    this.pipelineType = pipelineType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SubPipelineView that = (SubPipelineView) o;
    return Objects.equals(pipelineNumber, that.pipelineNumber)
        && pipelineType == that.pipelineType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(pipelineNumber, pipelineType);
  }
}
