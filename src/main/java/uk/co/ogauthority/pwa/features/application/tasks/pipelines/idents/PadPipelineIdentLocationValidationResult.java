package uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents;

import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineId;

public class PadPipelineIdentLocationValidationResult {

  private final PadPipeline padPipeline;
  private final PadPipelineIdent firstIdent;
  private final PadPipelineIdent lastIdent;
  private final boolean firstIdentFromMatchesHeader;
  private final boolean lastIdentToMatchesHeader;

  public PadPipelineIdentLocationValidationResult(PadPipeline padPipeline,
                                                  PadPipelineIdent firstIdent,
                                                  PadPipelineIdent lastIdent,
                                                  boolean firstIdentFromMatchesHeader,
                                                  boolean lastIdentToMatchesHeader) {
    this.padPipeline = padPipeline;
    this.firstIdent = firstIdent;
    this.lastIdent = lastIdent;
    this.firstIdentFromMatchesHeader = firstIdentFromMatchesHeader;
    this.lastIdentToMatchesHeader = lastIdentToMatchesHeader;
  }

  public static PadPipelineIdentLocationValidationResult createUnmatched(PadPipeline padPipeline) {
    return new PadPipelineIdentLocationValidationResult(padPipeline, null, null, false, false);
  }


  public PadPipeline getPadPipeline() {
    return padPipeline;
  }

  public PadPipelineIdent getFirstIdent() {
    return firstIdent;
  }

  public PadPipelineIdent getLastIdent() {
    return lastIdent;
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


  public String getFirstIdentIdAsString() {
    return String.valueOf(firstIdent.getPipelineIdentId());
  }

  public String getLastIdentIdAsString() {
    return String.valueOf(lastIdent.getPipelineIdentId());
  }

}
