package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.dto.pipelines.PadPipelineId;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines.PadPipelineRepository;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.campaignworks.CampaignWorksService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits.PermanentDepositService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.PadTechnicalDrawingLinkService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.PadTechnicalDrawingService;

@Service
public class PipelineRemovalService {

  private final PadTechnicalDrawingService padTechnicalDrawingService;
  private final PadTechnicalDrawingLinkService padTechnicalDrawingLinkService;
  private final PadOrganisationRoleService padOrganisationRoleService;
  private final CampaignWorksService campaignWorksService;
  private final PadPipelineIdentService padPipelineIdentService;
  private final PadPipelineRepository padPipelineRepository;
  private final PermanentDepositService permanentDepositService;

  @Autowired
  public PipelineRemovalService(
      PadTechnicalDrawingService padTechnicalDrawingService,
      PadTechnicalDrawingLinkService padTechnicalDrawingLinkService,
      PadOrganisationRoleService padOrganisationRoleService,
      CampaignWorksService campaignWorksService,
      PadPipelineIdentService padPipelineIdentService,
      PadPipelineRepository padPipelineRepository,
      PermanentDepositService permanentDepositService) {
    this.padTechnicalDrawingService = padTechnicalDrawingService;
    this.padTechnicalDrawingLinkService = padTechnicalDrawingLinkService;
    this.padOrganisationRoleService = padOrganisationRoleService;
    this.campaignWorksService = campaignWorksService;
    this.padPipelineIdentService = padPipelineIdentService;
    this.padPipelineRepository = padPipelineRepository;
    this.permanentDepositService = permanentDepositService;
  }

  @Transactional
  public void removePipeline(PwaApplicationDetail detail, PadPipeline padPipeline) {
    this.removeLinks(detail, padPipeline);
    this.removeIdents(padPipeline);
    padPipelineRepository.delete(padPipeline);
    this.cleanUnlinkedData(detail);
  }

  @VisibleForTesting
  public void removeLinks(PwaApplicationDetail detail, PadPipeline padPipeline) {
    padOrganisationRoleService.deletePipelineRoleLinksForPadPipeline(detail, padPipeline);
    padTechnicalDrawingLinkService.removeAllPipelineLinks(detail, padPipeline);
    campaignWorksService.removePipelineFromAllSchedules(detail, padPipeline);
    permanentDepositService.removePipelineFromDeposits(padPipeline);
  }

  @VisibleForTesting
  public void removeIdents(PadPipeline padPipeline) {
    var padPipelineId = PadPipelineId.from(padPipeline);
    padPipelineIdentService.getAllIdentsByPadPipelineIds(List.of(padPipelineId));
    padPipelineIdentService.removeAllIdents(padPipeline);
  }

  @VisibleForTesting
  public void cleanUnlinkedData(PwaApplicationDetail detail) {
    padTechnicalDrawingService.cleanUnlinkedDrawings(detail);
    permanentDepositService.cleanUnlinkedDeposits(detail);
    campaignWorksService.cleanUnlinkedSchedules(detail);
  }
}
