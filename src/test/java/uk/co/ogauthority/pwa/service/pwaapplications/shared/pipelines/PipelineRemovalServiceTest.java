package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.dto.pipelines.PadPipelineId;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines.PadPipelineRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.campaignworks.CampaignWorksService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits.PermanentDepositService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.PadTechnicalDrawingLinkService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.PadTechnicalDrawingService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PipelineRemovalServiceTest {

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

  private PipelineRemovalService pipelineRemovalService;
  private PwaApplicationDetail pwaApplicationDetail;
  private PadPipeline padPipeline;

  @Before
  public void setUp() {
    pipelineRemovalService = new PipelineRemovalService(padTechnicalDrawingService, padTechnicalDrawingLinkService,
        padOrganisationRoleService, campaignWorksService, padPipelineIdentService, padPipelineRepository,
        permanentDepositService);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    padPipeline = new PadPipeline();
    padPipeline.setId(1);
  }

  @Test
  public void removePipeline() {
    pipelineRemovalService.removePipeline(pwaApplicationDetail, padPipeline);
    verify(padPipelineRepository, times(1)).delete(padPipeline);
  }

  @Test
  public void removeLinks_serviceInteraction() {
    pipelineRemovalService.removeLinks(pwaApplicationDetail, padPipeline);
    verify(padOrganisationRoleService, times(1))
        .deletePipelineRoleLinksForPadPipeline(pwaApplicationDetail, padPipeline);

    verify(padTechnicalDrawingLinkService, times(1))
        .removeAllPipelineLinks(pwaApplicationDetail, padPipeline);

    verify(campaignWorksService, times(1)).
        removePipelineFromAllSchedules(pwaApplicationDetail, padPipeline);

    verify(permanentDepositService, times(1)).removePipelineFromDeposits(padPipeline);
  }

  @Test
  public void removeIdents_serviceInteraction() {
    pipelineRemovalService.removeIdents(padPipeline);
    var pipelineIdentifier = PadPipelineId.from(padPipeline);
    verify(padPipelineIdentService, times(1)).getAllIdentsByPadPipelineIds(eq(List.of(pipelineIdentifier)));
    verify(padPipelineIdentService, times(1)).removeAllIdents(padPipeline);
  }

  @Test
  public void cleanUnlinkedData_serviceInteraction() {
    pipelineRemovalService.cleanUnlinkedData(pwaApplicationDetail);
    verify(padTechnicalDrawingService, times(1)).cleanUnlinkedDrawings(pwaApplicationDetail);
    verify(permanentDepositService, times(1)).cleanUnlinkedDeposits(pwaApplicationDetail);
    verify(campaignWorksService, times(1)).cleanUnlinkedSchedules(pwaApplicationDetail);
  }
}