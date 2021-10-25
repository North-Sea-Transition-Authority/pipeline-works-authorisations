package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo;

import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdentifierVisitor;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineSection;

/**
* Create the appropriate pickable string for pipelineIdentifiers base don what is being identified
* Package private to prevent random initialisation across the codebase.
*/
class PipelineIdentifierPickableStringVisitor implements PipelineIdentifierVisitor {

  // effectively final after visit
  private String pickableString;

  @Override
  public void visit(PipelineId pipelineId) {
    this.pickableString = PickableHuooPipelineType.createPickableStringFrom(pipelineId);
  }

  @Override
  public void visit(PipelineSection pipelineSection) {
    this.pickableString = PickableHuooPipelineType.createPickableStringFrom(pipelineSection);
  }

  public String getPickableString() {
    return pickableString;
  }
}
