package uk.co.ogauthority.pwa.service.pwaapplications.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.repository.pwaapplications.PwaApplicationDetailRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.PwaApplicationRepository;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowType;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaManagementService;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;

@RunWith(MockitoJUnitRunner.class)
public class PwaApplicationCreationServiceTest {

  @Mock
  private MasterPwaManagementService masterPwaManagementService;

  @Mock
  private PwaApplicationRepository pwaApplicationRepository;

  @Mock
  private PwaApplicationDetailRepository pwaApplicationDetailRepository;

  @Mock
  private CamundaWorkflowService camundaWorkflowService;

  @Mock
  private PwaContactService pwaContactService;

  private PwaApplicationCreationService pwaApplicationCreationService;

  private Instant fixedInstant = LocalDate
      .of(2020, 2, 6)
      .atStartOfDay(ZoneId.systemDefault())
      .toInstant();

  @Mock
  private MasterPwaDetail masterPwaDetail;

  @Mock
  private MasterPwa masterPwa;

  @Before
  public void setUp() {
    pwaApplicationCreationService = new PwaApplicationCreationService(
        masterPwaManagementService,
        pwaApplicationRepository,
        pwaApplicationDetailRepository,
        camundaWorkflowService,
        pwaContactService,
        Clock.fixed(fixedInstant, ZoneId.systemDefault()));
  }


  @Test
  public void createInitialPwa() {

    WebUserAccount user = new WebUserAccount(123);

    when(masterPwaDetail.getMasterPwa()).thenReturn(masterPwa);
    when(masterPwaManagementService.createMasterPwa(any(), any())).thenReturn(masterPwaDetail);

    PwaApplication createdApplication = pwaApplicationCreationService.createInitialPwaApplication(user);

    ArgumentCaptor<PwaApplication> applicationArgumentCaptor = ArgumentCaptor.forClass(PwaApplication.class);
    ArgumentCaptor<PwaApplicationDetail> detailArgumentCaptor = ArgumentCaptor.forClass(PwaApplicationDetail.class);

    verify(pwaApplicationRepository, times(1)).save(applicationArgumentCaptor.capture());
    verify(pwaApplicationDetailRepository, times(1)).save(detailArgumentCaptor.capture());
    verify(camundaWorkflowService, times(1)).startWorkflow(WorkflowType.PWA_APPLICATION,
        applicationArgumentCaptor.getValue().getId());

    PwaApplication application = applicationArgumentCaptor.getValue();
    PwaApplicationDetail detail = detailArgumentCaptor.getValue();

    verify(pwaContactService, times(1)).addContact(application, user.getLinkedPerson(),
        Set.of(PwaContactRole.ACCESS_MANAGER));

    // check application set up correctly
    assertThat(application.getMasterPwa()).isEqualTo(masterPwa);
    assertThat(application.getApplicationType()).isEqualTo(PwaApplicationType.INITIAL);
    assertThat(application.getAppReference()).startsWith("APP/");
    assertThat(application.getConsentReference()).isNull();
    assertThat(application.getVariationNo()).isEqualTo(0);
    assertThat(application.getDecision()).isEmpty();
    assertThat(application.getDecisionTimestamp()).isEmpty();

    assertThat(createdApplication).isEqualTo(application);

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


  @Test
  public void createVariationPwaApplication_createsApplicationsAsExpected_whenCategory1() {
    createVariationPwaApplication_assertUsingType(PwaApplicationType.CAT_1_VARIATION);
  }

  private void createVariationPwaApplication_assertUsingType(PwaApplicationType pwaApplicationType) {
    WebUserAccount user = new WebUserAccount(123);

    MasterPwa masterPwa = new MasterPwa(fixedInstant);
    masterPwa.setId(1);

    PwaApplication createdApplication = pwaApplicationCreationService.createVariationPwaApplication(
        user,
        masterPwa,
        pwaApplicationType
    );

    ArgumentCaptor<PwaApplication> applicationArgumentCaptor = ArgumentCaptor.forClass(PwaApplication.class);
    ArgumentCaptor<PwaApplicationDetail> detailArgumentCaptor = ArgumentCaptor.forClass(PwaApplicationDetail.class);

    verify(pwaApplicationRepository, times(1)).save(applicationArgumentCaptor.capture());
    verify(pwaApplicationDetailRepository, times(1)).save(detailArgumentCaptor.capture());
    verify(camundaWorkflowService, times(1)).startWorkflow(
        WorkflowType.PWA_APPLICATION,
        applicationArgumentCaptor.getValue().getId()
    );

    PwaApplication application = applicationArgumentCaptor.getValue();
    PwaApplicationDetail detail = detailArgumentCaptor.getValue();

    verify(pwaContactService, times(1)).addContact(application, user.getLinkedPerson(),
        Set.of(PwaContactRole.ACCESS_MANAGER));

    // check application set up correctly
    assertThat(application.getMasterPwa()).isEqualTo(masterPwa);
    assertThat(application.getApplicationType()).isEqualTo(pwaApplicationType);
    assertThat(application.getAppReference()).startsWith("APP/");
    assertThat(application.getConsentReference()).isNull();
    assertThat(application.getVariationNo()).isEqualTo(0);
    assertThat(application.getDecision()).isEmpty();
    assertThat(application.getDecisionTimestamp()).isEmpty();

    assertThat(createdApplication).isEqualTo(application);

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