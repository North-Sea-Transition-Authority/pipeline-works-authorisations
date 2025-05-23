package uk.co.ogauthority.pwa.features.application.tasks.pipelines.core;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.tasks.campaignworks.CampaignWorksService;
import uk.co.ogauthority.pwa.features.application.tasks.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.PermanentDepositService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.PadTechnicalDrawingLinkService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.PadTechnicalDrawingService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdentService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.transfers.PadPipelineTransferService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class PipelineRemovalServiceTest {

  @Mock
  private PadTechnicalDrawingService padTechnicalDrawingService;
  @Mock
  private PadTechnicalDrawingLinkService padTechnicalDrawingLinkService;

  @Mock
  private PadOrganisationRoleService padOrganisationRoleService;

  @Mock
  private CampaignWorksService campaignWorksService;

  @Mock
  private PadPipelineIdentService padPipelineIdentService;

  @Mock
  private PadPipelineRepository padPipelineRepository;

  @Mock
  private PermanentDepositService permanentDepositService;

  @Mock
  private PadPipelineTransferService padPipelineTransferService;

  private PipelineRemovalService pipelineRemovalService;
  private PwaApplicationDetail pwaApplicationDetail;
  private PadPipeline padPipeline;

  @BeforeEach
  void setUp() {
    pipelineRemovalService = new PipelineRemovalService(padTechnicalDrawingService, padTechnicalDrawingLinkService,
        padOrganisationRoleService, campaignWorksService, padPipelineIdentService, padPipelineRepository,
        permanentDepositService, padPipelineTransferService);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    padPipeline = new PadPipeline();
    padPipeline.setId(1);
  }

  @Test
  void removePipeline() {
    pipelineRemovalService.removePipeline(padPipeline);
    verify(padPipelineRepository, times(1)).delete(padPipeline);
  }

  @Test
  void removeAndClean_serviceInteraction() {
    pipelineRemovalService.removeAndClean(padPipeline);
    verify(padOrganisationRoleService, times(1)).deletePipelineRoleLinksForPadPipeline(padPipeline);
    verify(padTechnicalDrawingService, times(1)).removePadPipelineFromDrawings(padPipeline);
    verify(permanentDepositService, times(1)).removePadPipelineFromDeposits(padPipeline);
    verify(campaignWorksService, times(1)).removePadPipelineFromCampaignWorks(padPipeline);
  }

  @Test
  void removeIdents_serviceInteraction() {
    pipelineRemovalService.removeIdents(padPipeline);
    var pipelineIdentifier = PadPipelineId.from(padPipeline);
    verify(padPipelineIdentService, times(1)).getAllIdentsByPadPipelineIds(eq(List.of(pipelineIdentifier)));
    verify(padPipelineIdentService, times(1)).removeAllIdents(padPipeline);
  }
}