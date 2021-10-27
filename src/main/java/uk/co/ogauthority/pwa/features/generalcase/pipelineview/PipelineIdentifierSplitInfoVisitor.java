package uk.co.ogauthority.pwa.features.generalcase.pipelineview;

import org.apache.commons.lang3.StringUtils;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdentifierVisitor;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineSection;

public class PipelineIdentifierSplitInfoVisitor implements PipelineIdentifierVisitor {

  // effectively final after visit
  private String splitInfoDetails;

  @Override
  public void visit(PipelineId pipelineId) {
    splitInfoDetails = null;
  }

  @Override
  public void visit(PipelineSection pipelineSection) {
    this.splitInfoDetails = StringUtils.capitalize(pipelineSection.getDisplayString());
  }

  public String getSplitInfoDetails() {
    return splitInfoDetails;
  }

}
