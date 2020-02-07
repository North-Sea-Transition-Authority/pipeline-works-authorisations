package uk.co.ogauthority.pwa.service.pwaapplications;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwa.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pwa.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwa.PwaApplicationDetail;
import uk.co.ogauthority.pwa.repository.pwaapplications.MasterPwaRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.PwaApplicationDetailRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.PwaApplicationRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowType;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;

@RunWith(MockitoJUnitRunner.class)
public class PwaApplicationServiceTest {

  @Mock
  private MasterPwaRepository masterPwaRepository;

  @Mock
  private PwaApplicationRepository pwaApplicationRepository;

  @Mock
  private PwaApplicationDetailRepository pwaApplicationDetailRepository;

  @Mock
  private CamundaWorkflowService camundaWorkflowService;

  private PwaApplicationService pwaApplicationService;

  private Instant fixedInstant = LocalDate
      .of(2020, 2, 6)
      .atStartOfDay(ZoneId.systemDefault())
      .toInstant();

  @Before
  public void setUp() {
    pwaApplicationService = new PwaApplicationService(
        masterPwaRepository,
        pwaApplicationRepository,
        pwaApplicationDetailRepository,
        camundaWorkflowService,
        Clock.fixed(fixedInstant, ZoneId.systemDefault()));
  }

  @Test
  public void createInitialPwa() {

    WebUserAccount user = new WebUserAccount(123);

    pwaApplicationService.createInitialPwaApplication(user);

    ArgumentCaptor<MasterPwa> pwaArgumentCaptor = ArgumentCaptor.forClass(MasterPwa.class);
    ArgumentCaptor<PwaApplication> applicationArgumentCaptor = ArgumentCaptor.forClass(PwaApplication.class);
    ArgumentCaptor<PwaApplicationDetail> detailArgumentCaptor = ArgumentCaptor.forClass(PwaApplicationDetail.class);

    verify(masterPwaRepository, times(1)).save(pwaArgumentCaptor.capture());
    verify(pwaApplicationRepository, times(1)).save(applicationArgumentCaptor.capture());
    verify(pwaApplicationDetailRepository, times(1)).save(detailArgumentCaptor.capture());
    verify(camundaWorkflowService, times(1)).startWorkflow(WorkflowType.PWA_APPLICATION, applicationArgumentCaptor.getValue().getId());

    MasterPwa masterPwa = pwaArgumentCaptor.getValue();
    PwaApplication application = applicationArgumentCaptor.getValue();
    PwaApplicationDetail detail = detailArgumentCaptor.getValue();

    // check master pwa set up correctly
    assertThat(masterPwa.getCreatedTimestamp()).isEqualTo(fixedInstant);
    assertThat(masterPwa.getPortalOrganisationUnit()).isNull();

    // check application set up correctly
    assertThat(application.getMasterPwa()).isEqualTo(masterPwa);
    assertThat(application.getApplicationType()).isEqualTo(PwaApplicationType.INITIAL);
    assertThat(application.getAppReference()).isNull();
    assertThat(application.getConsentReference()).isNull();
    assertThat(application.getVariationNo()).isEqualTo(0);
    assertThat(application.getDecision()).isEmpty();
    assertThat(application.getDecisionTimestamp()).isEmpty();

    // check detail set up correctly
    assertThat(detail.getPwaApplication()).isEqualTo(application);
    assertThat(detail.getStatus()).isEqualTo(PwaApplicationStatus.DRAFT);
    assertThat(detail.isTipFlag()).isTrue();
    assertThat(detail.getVersionNo()).isEqualTo(1);
    assertThat(detail.getCreatedByWuaId()).isEqualTo(user.getWuaId());
    assertThat(detail.getCreatedTimestamp()).isEqualTo(fixedInstant);
    assertThat(detail.getSubmittedByWuaId()).isNull();
    assertThat(detail.getSubmittedTimestamp()).isNull();
    assertThat(detail.getLastUpdatedByWuaId()).isNull();
    assertThat(detail.getLastUpdatedTimestamp()).isNull();

  }

}
