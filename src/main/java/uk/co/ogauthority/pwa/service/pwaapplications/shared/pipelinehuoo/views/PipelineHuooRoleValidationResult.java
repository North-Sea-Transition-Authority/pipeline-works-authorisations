package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.ObjectUtils;

public class PipelineHuooRoleValidationResult {

  private final String unassignedPipelineErrorMessage;

  private final String unassignedRoleOwnerErrorMessage;

  private final boolean hasErrors;

  @VisibleForTesting
  PipelineHuooRoleValidationResult(String unassignedPipelineErrorMessage,
                                           String unassignedRoleOwnerErrorMessage) {
    this.unassignedPipelineErrorMessage = unassignedPipelineErrorMessage;
    this.unassignedRoleOwnerErrorMessage = unassignedRoleOwnerErrorMessage;

    this.hasErrors = ObjectUtils.anyNotNull(this.unassignedPipelineErrorMessage, this.unassignedRoleOwnerErrorMessage);
  }

  public PipelineHuooRoleValidationResult(PipelineHuooRoleSummaryView pipelineHuooRoleSummaryView) {
    this.unassignedPipelineErrorMessage = pipelineHuooRoleSummaryView.getUnassignedPipelineIds().isEmpty()
        ? null
        : String.format(
        "At least one %s must be assigned to every pipeline or defined pipeline section",
        pipelineHuooRoleSummaryView.getHuooRole().getDisplayText().toLowerCase()
    );

    this.unassignedRoleOwnerErrorMessage = pipelineHuooRoleSummaryView.getUnassignedRoleOwnerOrganisationIds().isEmpty()
        ? null
        : String.format(
        "Every %s must have at least 1 assigned pipeline or defined pipeline section",
        pipelineHuooRoleSummaryView.getHuooRole().getDisplayText().toLowerCase()
    );

    this.hasErrors = ObjectUtils.anyNotNull(this.unassignedPipelineErrorMessage, this.unassignedRoleOwnerErrorMessage);


  }

  public static PipelineHuooRoleValidationResult createValidResult() {
    return new PipelineHuooRoleValidationResult(null, null);
  }

  public String getUnassignedPipelineErrorMessage() {
    return unassignedPipelineErrorMessage;
  }

  public String getUnassignedRoleOwnerErrorMessage() {
    return unassignedRoleOwnerErrorMessage;
  }

  public boolean hasErrors() {
    return hasErrors;
  }
}
