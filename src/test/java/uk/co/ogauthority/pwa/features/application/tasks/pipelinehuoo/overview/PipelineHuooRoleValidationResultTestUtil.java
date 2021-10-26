package uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.overview;

public class PipelineHuooRoleValidationResultTestUtil {
  private PipelineHuooRoleValidationResultTestUtil() {
    // no instantiation
  }

  public static PipelineHuooRoleValidationResult validResult(){
    return new PipelineHuooRoleValidationResult(null, null, null);
  }

  public static PipelineHuooRoleValidationResult invalidResultAsUnassigned(String unassignedPipelineMessage, String unassignedOrgRoleMessage){
    return new PipelineHuooRoleValidationResult(unassignedPipelineMessage, unassignedOrgRoleMessage, null);
  }

  public static PipelineHuooRoleValidationResult invalidResultAsBadSection(String badSectionError){
    return new PipelineHuooRoleValidationResult(null, null, badSectionError);
  }

}
