package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.campaignworks.CampaignWorksService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.PadTechnicalDrawingLinkService;

@Service
public class PipelineRemovalService {
  private final ApplicationContext applicationContext;

  @Autowired
  public PipelineRemovalService(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  public void removeLinks(PwaApplicationDetail detail, PadPipeline padPipeline) {
    var padTechnicalDrawingLinkService = applicationContext.getBean(PadTechnicalDrawingLinkService.class);
    var padOrganisationRoleService = applicationContext.getBean(PadOrganisationRoleService.class);
    var campaignWorksService = applicationContext.getBean(CampaignWorksService.class);
    padOrganisationRoleService.deletePadPipelineRoleLinksForPadPipeline(detail, padPipeline);
    padTechnicalDrawingLinkService.removeAllPipelineLinks(detail, padPipeline);
    campaignWorksService.removePipelineFromAllSchedules(detail, padPipeline);
  }
}
