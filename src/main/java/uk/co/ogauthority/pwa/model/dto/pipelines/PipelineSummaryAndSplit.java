package uk.co.ogauthority.pwa.model.dto.pipelines;

import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;

public class PipelineSummaryAndSplit {

  private final PipelineOverview pipelineOverview;
  private final String splitInfo;
  private final HuooRole role;


  public PipelineSummaryAndSplit(PipelineOverview pipelineOverview, String splitInfo,
                                 HuooRole role) {
    this.pipelineOverview = pipelineOverview;
    this.splitInfo = splitInfo;
    this.role = role;
  }

  public static PipelineSummaryAndSplit duplicateOptionForPipelineIdentifier(PipelineIdentifier pipelineIdentifier,
                                                                             HuooRole huooRole,
                                                                             PipelineOverview pipelineOverview) {
    var splitInfoVisitor = new PipelineIdentifierSplitInfoVisitor();
    pipelineIdentifier.accept(splitInfoVisitor);

    return new PipelineSummaryAndSplit(
        pipelineOverview,
        splitInfoVisitor.getSplitInfoDetails(),
        huooRole
    );
  }


  public PipelineOverview getPipelineOverview() {
    return pipelineOverview;
  }

  public String getSplitInfo() {
    return splitInfo;
  }

  public HuooRole getRole() {
    return role;
  }







}
