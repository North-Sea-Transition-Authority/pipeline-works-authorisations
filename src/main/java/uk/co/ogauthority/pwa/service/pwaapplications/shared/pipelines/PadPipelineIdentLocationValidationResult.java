package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import uk.co.ogauthority.pwa.model.dto.pipelines.PadPipelineId;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;

public class PadPipelineIdentLocationValidationResult {

  private final PadPipeline padPipeline;
  private final boolean firstIdentMatchesHeader;
  private final boolean lastIdentMatchesHeader;

  public PadPipelineIdentLocationValidationResult(PadPipeline padPipeline,
                                                  boolean firstIdentMatchesHeader,
                                                  boolean lastIdentMatchesHeader) {
    this.padPipeline = padPipeline;
    this.firstIdentMatchesHeader = firstIdentMatchesHeader;
    this.lastIdentMatchesHeader = lastIdentMatchesHeader;
  }

  public static PadPipelineIdentLocationValidationResult createUnmatched(PadPipeline padPipeline) {
    return new PadPipelineIdentLocationValidationResult(padPipeline, false, false);
  }


  public PadPipeline getPadPipeline() {
    return padPipeline;
  }

  public boolean firstIdentMatchesHeader() {
    return firstIdentMatchesHeader;
  }

  public boolean lastIdentMatchesHeader() {
    return lastIdentMatchesHeader;
  }

  public boolean identsMatchHeaderLocation() {
    return firstIdentMatchesHeader && lastIdentMatchesHeader;
  }

  public PadPipelineId getPadPipelineId() {
    return new PadPipelineId(padPipeline.getId());
  }
}
