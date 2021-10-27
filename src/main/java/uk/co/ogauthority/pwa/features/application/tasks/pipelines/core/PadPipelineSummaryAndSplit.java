package uk.co.ogauthority.pwa.features.application.tasks.pipelines.core;

import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdentifier;
import uk.co.ogauthority.pwa.features.generalcase.pipelineview.PipelineIdentifierSplitInfoVisitor;

public class PadPipelineSummaryAndSplit {

  private final PadPipelineSummaryDto padPipelineSummaryDto;
  private final String splitInfo;
  private final HuooRole role;


  public PadPipelineSummaryAndSplit(PadPipelineSummaryDto padPipelineSummaryDto, String splitInfo,
                                    HuooRole role) {
    this.padPipelineSummaryDto = padPipelineSummaryDto;
    this.splitInfo = splitInfo;
    this.role = role;
  }


  public static PadPipelineSummaryAndSplit duplicateOptionForPipelineIdentifier(PipelineIdentifier pipelineIdentifier,
                                                                                HuooRole huooRole,
                                                                                PadPipelineSummaryDto padPipelineSummaryDto) {
    var splitInfoVisitor = new PipelineIdentifierSplitInfoVisitor();
    pipelineIdentifier.accept(splitInfoVisitor);

    return new PadPipelineSummaryAndSplit(
        padPipelineSummaryDto,
        splitInfoVisitor.getSplitInfoDetails(),
        huooRole
    );
  }


  public PadPipelineSummaryDto getPadPipelineSummaryDto() {
    return padPipelineSummaryDto;
  }

  public String getSplitInfo() {
    return splitInfo;
  }

  public HuooRole getRole() {
    return role;
  }







}
