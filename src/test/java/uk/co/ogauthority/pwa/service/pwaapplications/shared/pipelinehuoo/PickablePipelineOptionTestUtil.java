package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo;

import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;

public class PickablePipelineOptionTestUtil {


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
