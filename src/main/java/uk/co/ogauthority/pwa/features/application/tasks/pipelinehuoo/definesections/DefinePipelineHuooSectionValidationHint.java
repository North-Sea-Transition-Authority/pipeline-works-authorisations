package uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.definesections;

import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

/**
 * Class to encapsulate information required for validation of
 * {@link DefinePipelineHuooSectionsForm}
 * that is not contained within the form itself.
 */
public class DefinePipelineHuooSectionValidationHint {

  private final PwaApplicationDetail pwaApplicationDetail;
  private final HuooRole huooRole;
  private final PipelineId pipelineId;
  private final int numberOfSections;

  public DefinePipelineHuooSectionValidationHint(PwaApplicationDetail pwaApplicationDetail,
                                                 HuooRole huooRole,
                                                 PipelineId pipelineId,
                                                 int numberOfSections) {
    this.pwaApplicationDetail = pwaApplicationDetail;
    this.huooRole = huooRole;
    this.pipelineId = pipelineId;
    this.numberOfSections = numberOfSections;
  }

  public PwaApplicationDetail getPwaApplicationDetail() {
    return pwaApplicationDetail;
  }

  public HuooRole getHuooRole() {
    return huooRole;
  }

  public PipelineId getPipelineId() {
    return pipelineId;
  }

  public int getNumberOfSections() {
    return numberOfSections;
  }
}
