package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo;

import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;

public class PickablePipelineOptionTestUtil {


  public static ReconciledPickablePipeline createConsentedReconciledPickablePipeline(
      PipelineId pipelineId){
    return new ReconciledPickablePipeline(
        PickablePipelineId.from(PickablePipelineType.CONSENTED.createIdString(pipelineId.asInt())),
        pipelineId);
  }

  public static ReconciledPickablePipeline createApplicationReconciledPickablePipeline(
      int padPipelineId, PipelineId pipelineId){
    return new ReconciledPickablePipeline(
        PickablePipelineId.from(PickablePipelineType.APPLICATION.createIdString(padPipelineId)),
        pipelineId);
  }

  public static PickablePipelineOption createOption(int id, PickablePipelineType type, String pipelineNumber) {
    return new PickablePipelineOption(id,
        type,
        pipelineNumber,
        null,
        null,
        null,
        null,
        null,
        null
    );
  }

  public static PickablePipelineOption createOption(Pipeline pipeline) {
    return new PickablePipelineOption(pipeline.getId(),
        PickablePipelineType.CONSENTED,
        null,
        null,
        null,
        null,
        null,
        null,
        null
    );
  }

  public static PickablePipelineOption createOption(PadPipeline padPipeline) {
    return new PickablePipelineOption(padPipeline.getId(),
        PickablePipelineType.APPLICATION,
        null,
        null,
        null,
        null,
        null,
        null,
        null
    );

  }
}
