package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import uk.co.ogauthority.pwa.model.dto.pipelines.PadPipelineId;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;

public class PadPipelineIdentLocationValidationResult {

  private final PadPipeline padPipeline;
  private final boolean firstIdentFromMatchesHeader;
  private final boolean lastIdentToMatchesHeader;

  public PadPipelineIdentLocationValidationResult(PadPipeline padPipeline,
                                                  boolean firstIdentFromMatchesHeader,
                                                  boolean lastIdentToMatchesHeader) {
    this.padPipeline = padPipeline;
    this.firstIdentFromMatchesHeader = firstIdentFromMatchesHeader;
    this.lastIdentToMatchesHeader = lastIdentToMatchesHeader;
  }

  public static PadPipelineIdentLocationValidationResult createUnmatched(PadPipeline padPipeline) {
    return new PadPipelineIdentLocationValidationResult(padPipeline, false, false);
  }


  public PadPipeline getPadPipeline() {
    return padPipeline;
  }

  public boolean firstIdentFromMatchesHeader() {
    return firstIdentFromMatchesHeader;
  }

  public boolean lastIdentToMatchesHeader() {
    return lastIdentToMatchesHeader;
  }

  public boolean identsMatchHeaderLocation() {
    return firstIdentFromMatchesHeader && lastIdentToMatchesHeader;
  }

  public PadPipelineId getPadPipelineId() {
    return new PadPipelineId(padPipeline.getId());
  }
}
