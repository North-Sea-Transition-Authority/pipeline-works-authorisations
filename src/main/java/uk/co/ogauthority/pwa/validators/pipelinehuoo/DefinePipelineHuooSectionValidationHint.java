package uk.co.ogauthority.pwa.validators.pipelinehuoo;

import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

/**
 * Class to encapsulate information required for validation of
 * {@link uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinehuoo.form.DefinePipelineHuooSectionsForm}
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
