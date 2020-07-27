package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo;

import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineIdentifier;

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
