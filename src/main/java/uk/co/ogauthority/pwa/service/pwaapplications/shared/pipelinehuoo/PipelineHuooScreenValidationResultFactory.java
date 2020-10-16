package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo;

import java.util.LinkedHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.PipelineHuooValidationResult;
import uk.co.ogauthority.pwa.service.validation.SummaryScreenValidationResult;

/**
 * Constructs the objects required for onscreen display of pipeline huoo validation errors.
 */
@Service
public class PipelineHuooScreenValidationResultFactory {

  @Autowired
  public PipelineHuooScreenValidationResultFactory() {
  }


  public SummaryScreenValidationResult createFromValidationResult(
      PipelineHuooValidationResult pipelineHuooValidationResult) {
    var errorMap = new LinkedHashMap<String, String>();
    pipelineHuooValidationResult.getValidationResults().entrySet()
        .stream()
        .forEachOrdered(huooRolePipelineHuooRoleValidationResultEntry -> {
          var role = huooRolePipelineHuooRoleValidationResultEntry.getKey();
          var result = huooRolePipelineHuooRoleValidationResultEntry.getValue();
          if (result.hasErrors() && result.getUnassignedPipelineErrorMessage() != null) {
            errorMap.put(role.name() + "-UNASSIGNED-PIPELINES", result.getUnassignedPipelineErrorMessage());
          }
          if (result.hasErrors() && result.getUnassignedRoleOwnerErrorMessage() != null) {
            errorMap.put(role.name() + "-UNASSIGNED-ROLES", result.getUnassignedRoleOwnerErrorMessage());
          }
        });

    return new SummaryScreenValidationResult(
        errorMap,
        "huoo",
        "",
        pipelineHuooValidationResult.isValid(),
        "Correct errors to complete section."


    );

  }
}
