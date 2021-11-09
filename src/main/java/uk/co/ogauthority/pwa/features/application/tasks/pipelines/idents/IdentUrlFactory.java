package uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.controller.PipelineIdentsController;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

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
        .renderRemoveIdent(applicationId, pwaApplicationType, pipelineId, null, identId, null));
  }

  public String getEditUrl(Integer identId) {
    return ReverseRouter.route(on(PipelineIdentsController.class)
        .renderEditIdent(applicationId, pwaApplicationType, pipelineId, identId, null, null, null));
  }

  public String getInsertAboveUrl(Integer identId) {
    return ReverseRouter.route(on(PipelineIdentsController.class)
        .renderInsertIdentAbove(applicationId, pwaApplicationType, pipelineId, identId, null, null, null));
  }
}
