package uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.overview;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.features.generalcase.pipelineview.PipelineAndIdentView;

/**
 * Store all validation failure reasons about the pipeline huoo section.
 */
public class PipelineHuooValidationResult {

  private Map<HuooRole, PipelineHuooRoleValidationResult> validationResults;

  private boolean isValid;

  public PipelineHuooValidationResult(
      PipelineAndOrgRoleGroupViewsByRole pipelineAndOrgRoleGroupViewsByRole,
      List<PipelineAndIdentView> pipelineAndIdentViewList) {

    var pipelineIdToPipelineAndView = pipelineAndIdentViewList
        .stream()
        .collect(Collectors.toMap(PipelineAndIdentView::getPipelineId, Function.identity()));
    // linked hash map to keep ordering of roles.
    this.validationResults = new LinkedHashMap<>();
    validationResults.put(
        HuooRole.HOLDER,
        PipelineHuooRoleValidationResult.generateValidationResult(
            pipelineAndOrgRoleGroupViewsByRole.getHolderRoleSummaryView(),
            pipelineIdToPipelineAndView
        )
    );
    validationResults.put(
        HuooRole.USER,
        PipelineHuooRoleValidationResult.generateValidationResult(
            pipelineAndOrgRoleGroupViewsByRole.getUserRoleSummaryView(),
            pipelineIdToPipelineAndView
        )
    );
    validationResults.put(
        HuooRole.OPERATOR,
        PipelineHuooRoleValidationResult.generateValidationResult(
            pipelineAndOrgRoleGroupViewsByRole.getOperatorRoleSummaryView(),
            pipelineIdToPipelineAndView
        )
    );
    validationResults.put(
        HuooRole.OWNER,
        PipelineHuooRoleValidationResult.generateValidationResult(
            pipelineAndOrgRoleGroupViewsByRole.getOwnerRoleSummaryView(),
            pipelineIdToPipelineAndView
        )
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
