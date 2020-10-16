package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views;

public class PipelineHuooRoleValidationResultTestUtil {
  private PipelineHuooRoleValidationResultTestUtil() {
    // no instantiation
  }

  public static PipelineHuooRoleValidationResult validResult(){
    return new PipelineHuooRoleValidationResult(null, null);
  }

  public static PipelineHuooRoleValidationResult invalidResult(String unassignedPipelineMessage, String unassignedOrgRoleMessage){
    return new PipelineHuooRoleValidationResult(unassignedPipelineMessage, unassignedOrgRoleMessage);
  }
}
