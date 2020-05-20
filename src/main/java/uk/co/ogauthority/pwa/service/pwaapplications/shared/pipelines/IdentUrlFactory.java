package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelines.PipelineIdentsController;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

public class IdentUrlFactory {

  private final PwaApplicationType pwaApplicationType;
  private final Integer applicationId;
  private final Integer pipelineId;


  public IdentUrlFactory(PwaApplicationType pwaApplicationType, Integer applicationId, Integer pipelineId) {
    this.pwaApplicationType = pwaApplicationType;
    this.applicationId = applicationId;
    this.pipelineId = pipelineId;
  }

  public String getRemoveUrl(Integer identId) {
    return ReverseRouter.route(on(PipelineIdentsController.class)
        .renderRemoveIdent(applicationId, pwaApplicationType, pipelineId, null, identId));
  }
}
