package uk.co.ogauthority.pwa.features.application.tasks.pipelines.tasklist;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.controller.PipelinesController;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.importconsented.controller.ModifyPipelineController;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.transfers.PadPipelineTransferClaimController;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

public class PipelineUrlFactory {

  private final Integer applicationId;
  private final PwaApplicationType applicationType;

  public PipelineUrlFactory(PwaApplicationDetail detail) {
    this.applicationId = detail.getMasterPwaApplicationId();
    this.applicationType = detail.getPwaApplicationType();
  }

  public String getAddPipelineUrl() {
    return ReverseRouter.route(on(PipelinesController.class)
        .renderAddPipeline(applicationId, applicationType, null, null));
  }

  public String getModifyPipelineUrl() {
    return ReverseRouter.route(on(ModifyPipelineController.class)
        .renderImportConsentedPipeline(applicationId, applicationType, null, null));
  }

  public String getRemovePipelineUrl(Integer pipelineId) {
    return ReverseRouter.route(on(PipelinesController.class)
        .renderRemovePipeline(applicationId, applicationType, pipelineId, null));
  }

  public String getClaimPipelineUrl() {
    return ReverseRouter.route(on(PadPipelineTransferClaimController.class)
        .renderClaimPipelineForm(applicationId, applicationType, null, null));
  }
}
