package uk.co.ogauthority.pwa.integration.service.pwaapplications.generic;

import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdent;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdentData;

/* Test only container for easy access to all pipeline data for a simple padPipeline with one ident */
public class SimplePadPipelineContainer {

  private final PadPipeline padPipeline;

  private final PadPipelineIdent padPipelineIdent;

  private final PadPipelineIdentData padPipelineIdentData;

  public SimplePadPipelineContainer(PadPipelineIdentData padPipelineIdentData) {
    this.padPipelineIdentData = padPipelineIdentData;
    this.padPipelineIdent = padPipelineIdentData.getPadPipelineIdent();
    this.padPipeline = padPipelineIdent.getPadPipeline();
  }

  public PadPipeline getPadPipeline() {
    return padPipeline;
  }

  public PadPipelineIdent getPadPipelineIdent() {
    return padPipelineIdent;
  }

  public PadPipelineIdentData getPadPipelineIdentData() {
    return padPipelineIdentData;
  }

}
