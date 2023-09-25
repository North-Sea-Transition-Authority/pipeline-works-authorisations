package uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.pipelines;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdent;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdentData;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;

public class PadPipelineDto {

  private PadPipeline padPipeline;

  private Map<PadPipelineIdent, Set<PadPipelineIdentData>> identToIdentDataSetMap;
  private Pipeline transferredFromPipeline;

  public PadPipeline getPadPipeline() {
    return padPipeline;
  }

  public void setPadPipeline(PadPipeline padPipeline) {
    this.padPipeline = padPipeline;
  }

  public Map<PadPipelineIdent, Set<PadPipelineIdentData>> getIdentToIdentDataSetMap() {
    return identToIdentDataSetMap;
  }

  public void setIdentToIdentDataSetMap(Map<PadPipelineIdent, Set<PadPipelineIdentData>> identToIdentDataSetMap) {
    this.identToIdentDataSetMap = identToIdentDataSetMap;
  }

  public Pipeline getTransferredFromPipeline() {
    return transferredFromPipeline;
  }

  public void setTransferredFromPipeline(Pipeline transferredFromPipeline) {
    this.transferredFromPipeline = transferredFromPipeline;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PadPipelineDto that = (PadPipelineDto) o;
    return Objects.equals(padPipeline, that.padPipeline)
        && Objects.equals(identToIdentDataSetMap, that.identToIdentDataSetMap)
        && Objects.equals(transferredFromPipeline, that.transferredFromPipeline);
  }

  @Override
  public int hashCode() {
    return Objects.hash(padPipeline, identToIdentDataSetMap, transferredFromPipeline);
  }

}
