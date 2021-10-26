package uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.modifyhuoo;

import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdentifier;

public class PickablePipelineOptionTestUtil {


  public static ReconciledHuooPickablePipeline createReconciledPickablePipeline(PipelineIdentifier pipelineIdentifier) {

    return new ReconciledHuooPickablePipeline(
        PickableHuooPipelineId.from(PickableHuooPipelineType.createPickableString(pipelineIdentifier)),
        pipelineIdentifier);
  }


  public static PickableHuooPipelineOption createOption(PipelineIdentifier pipelineIdentifier, String pipelineNumber) {
    return new PickableHuooPipelineOption(
        PickableHuooPipelineType.createPickableString(pipelineIdentifier),
        pipelineNumber,
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
