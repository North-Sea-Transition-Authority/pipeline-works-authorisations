package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;

/**
 * Store all validation failure reasons about the pipeline huoo section.
 */
public class PipelineHuooValidationResult {

  private Map<HuooRole, PipelineHuooRoleValidationResult> validationResults;

  private boolean isValid;

  public PipelineHuooValidationResult(PipelineAndOrgRoleGroupViewsByRole pipelineAndOrgRoleGroupViewsByRole) {
    // linked hash map to keep ordering of roles.
    this.validationResults = new LinkedHashMap<>();
    validationResults.put(
        HuooRole.HOLDER,
        new PipelineHuooRoleValidationResult(pipelineAndOrgRoleGroupViewsByRole.getHolderRoleSummaryView()));
    validationResults.put(
        HuooRole.USER, new PipelineHuooRoleValidationResult(
            pipelineAndOrgRoleGroupViewsByRole.getUserRoleSummaryView())
    );
    validationResults.put(
        HuooRole.OPERATOR, new PipelineHuooRoleValidationResult(
            pipelineAndOrgRoleGroupViewsByRole.getOperatorRoleSummaryView())
    );
    validationResults.put(
        HuooRole.OWNER, new PipelineHuooRoleValidationResult(
            pipelineAndOrgRoleGroupViewsByRole.getOwnerRoleSummaryView())
    );

    this.isValid = validationResults.values().stream().noneMatch(PipelineHuooRoleValidationResult::hasErrors);
  }

  public PipelineHuooRoleValidationResult getValidationResult(HuooRole huooRole) {
    return validationResults.getOrDefault(huooRole, PipelineHuooRoleValidationResult.createValidResult());
  }

  public Map<HuooRole, PipelineHuooRoleValidationResult> getValidationResults() {
    return Collections.unmodifiableMap(validationResults);
  }

  public boolean isValid() {
    return isValid;
  }
}
