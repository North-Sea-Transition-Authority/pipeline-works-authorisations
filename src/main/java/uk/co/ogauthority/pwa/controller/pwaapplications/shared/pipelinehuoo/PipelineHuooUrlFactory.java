package uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinehuoo;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

public class PipelineHuooUrlFactory {

  private final int applicationId;
  private final PwaApplicationType applicationType;

  public PipelineHuooUrlFactory(int applicationId,
                                PwaApplicationType applicationType) {
    this.applicationId = applicationId;
    this.applicationType = applicationType;
  }

  public String getAddHolderPipelineRoleUrl() {
    return getAddPipelineRoleUrl(HuooRole.HOLDER);
  }

  public String getAddUserPipelineRoleUrl() {
    return getAddPipelineRoleUrl(HuooRole.USER);
  }

  public String getAddOperatorPipelineRoleUrl() {
    return getAddPipelineRoleUrl(HuooRole.OPERATOR);
  }

  public String getAddOwnerPipelineRoleUrl() {
    return getAddPipelineRoleUrl(HuooRole.OWNER);
  }

  private String getAddPipelineRoleUrl(HuooRole huooRole) {
    return ReverseRouter.route(on(AddPipelineHuooJourneyController.class)
        .renderPipelinesForHuooAssignment(applicationType, applicationId, huooRole, null, null));
  }
}
