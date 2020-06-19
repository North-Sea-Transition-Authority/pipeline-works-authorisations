package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelines.PipelinesController;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

public class PipelineUrlFactory {

  private final Integer applicationId;
  private final PwaApplicationType applicationType;

  public PipelineUrlFactory(PwaApplicationDetail detail) {
    this.applicationId = detail.getMasterPwaApplicationId();
    this.applicationType = detail.getPwaApplicationType();
  }

  public String getAddBundleUrl() {
    return ReverseRouter.route(on(PipelinesController.class)
        .renderAddBundle(applicationId, applicationType, null, null));
  }

  public String getAddPipelineUrl() {
    return ReverseRouter.route(on(PipelinesController.class)
        .renderAddPipeline(applicationId, applicationType, null, null));
  }

}
