package uk.co.ogauthority.pwa.features.application.tasks.pipelines.core;

import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.tasks.campaignworks.CampaignWorksService;
import uk.co.ogauthority.pwa.features.application.tasks.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.PermanentDepositService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.PadTechnicalDrawingLinkService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.PadTechnicalDrawingService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdentService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.transfers.PadPipelineTransferService;

@Service
public class PipelineRemovalService {

  private final PadTechnicalDrawingService padTechnicalDrawingService;
  private final PadTechnicalDrawingLinkService padTechnicalDrawingLinkService;
  private final PadOrganisationRoleService padOrganisationRoleService;
  private final CampaignWorksService campaignWorksService;
  private final PadPipelineIdentService padPipelineIdentService;
  private final PadPipelineRepository padPipelineRepository;
  private final PermanentDepositService permanentDepositService;
  private final PadPipelineTransferService padPipelineTransferService;

  @Autowired
  public PipelineRemovalService(
      PadTechnicalDrawingService padTechnicalDrawingService,
      PadTechnicalDrawingLinkService padTechnicalDrawingLinkService,
      PadOrganisationRoleService padOrganisationRoleService,
      CampaignWorksService campaignWorksService,
      PadPipelineIdentService padPipelineIdentService,
      PadPipelineRepository padPipelineRepository,
      PermanentDepositService permanentDepositService,
      PadPipelineTransferService padPipelineTransferService) {
    this.padTechnicalDrawingService = padTechnicalDrawingService;
    this.padTechnicalDrawingLinkService = padTechnicalDrawingLinkService;
    this.padOrganisationRoleService = padOrganisationRoleService;
    this.campaignWorksService = campaignWorksService;
    this.padPipelineIdentService = padPipelineIdentService;
    this.padPipelineRepository = padPipelineRepository;
    this.permanentDepositService = permanentDepositService;
    this.padPipelineTransferService = padPipelineTransferService;
  }

  @Transactional
  public void removePipeline(PadPipeline padPipeline) {
    this.removeIdents(padPipeline);
    this.removeAndClean(padPipeline);
    padPipelineTransferService.checkAndRemoveFromTransfer(padPipeline.getPipeline());
    padPipelineRepository.delete(padPipeline);
  }

  @VisibleForTesting
  public void removeIdents(PadPipeline padPipeline) {
    var padPipelineId = PadPipelineId.from(padPipeline);
    padPipelineIdentService.getAllIdentsByPadPipelineIds(List.of(padPipelineId));
    padPipelineIdentService.removeAllIdents(padPipeline);
  }

  @VisibleForTesting
  public void removeAndClean(PadPipeline padPipeline) {
    padOrganisationRoleService.deletePipelineRoleLinksForPadPipeline(padPipeline);
    padTechnicalDrawingService.removePadPipelineFromDrawings(padPipeline);
    permanentDepositService.removePadPipelineFromDeposits(padPipeline);
    campaignWorksService.removePadPipelineFromCampaignWorks(padPipeline);
  }
}
