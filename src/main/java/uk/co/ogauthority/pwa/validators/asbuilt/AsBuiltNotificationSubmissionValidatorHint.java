package uk.co.ogauthority.pwa.validators.asbuilt;

import uk.co.ogauthority.pwa.model.entity.asbuilt.PipelineChangeCategory;

public class AsBuiltNotificationSubmissionValidatorHint {

  private final boolean isOgaUser;
  private final PipelineChangeCategory pipelineChangeCategory;

  public AsBuiltNotificationSubmissionValidatorHint(boolean isOgaUser, PipelineChangeCategory pipelineChangeCategory) {
    this.isOgaUser = isOgaUser;
    this.pipelineChangeCategory = pipelineChangeCategory;
  }

  public boolean isOgaUser() {
    return isOgaUser;
  }

  public PipelineChangeCategory getPipelineChangeCategory() {
    return pipelineChangeCategory;
  }

}