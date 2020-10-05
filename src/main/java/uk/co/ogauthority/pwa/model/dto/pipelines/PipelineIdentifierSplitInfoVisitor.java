package uk.co.ogauthority.pwa.model.dto.pipelines;

import org.apache.commons.lang3.StringUtils;

public class PipelineIdentifierSplitInfoVisitor implements PipelineIdentifierVisitor {

  // effectively final after visit
  private String splitInfoDetails;

  @Override
  public void visit(PipelineId pipelineId) {
    splitInfoDetails = null;
  }

  @Override
  public void visit(PipelineSegment pipelineSegment) {
    this.splitInfoDetails = StringUtils.capitalize(pipelineSegment.getDisplayString());
  }

  public String getSplitInfoDetails() {
    return splitInfoDetails;
  }

}
