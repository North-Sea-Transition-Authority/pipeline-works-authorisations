package uk.co.ogauthority.pwa.model.dto.pipelines;

import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;

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
