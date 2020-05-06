package uk.co.ogauthority.pwa.service.pwaapplications.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import uk.co.ogauthority.pwa.repository.pwaapplications.PwaApplicationRepository;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowType;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaManagementService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;

@RunWith(MockitoJUnitRunner.class)
public class PwaApplicationCreationServiceTest {

  @Mock
  private MasterPwaManagementService masterPwaManagementService;

  @Mock
  private PwaApplicationRepository pwaApplicationRepository;

  @Mock
  private CamundaWorkflowService camundaWorkflowService;

  @Mock
  private PwaContactService pwaContactService;

  @Mock
  private MasterPwaDetail masterPwaDetail;

  @Mock
  private MasterPwa masterPwa;

  @Mock
  private PwaApplicationDetailService pwaApplicationDetailService;

  private PwaApplicationCreationService pwaApplicationCreationService;

  private Instant fixedInstant = LocalDate
      .of(2020, 2, 6)
      .atStartOfDay(ZoneId.systemDefault())
      .toInstant();

  @Before
  public void setUp() {
    pwaApplicationCreationService = new PwaApplicationCreationService(
        masterPwaManagementService,
        pwaApplicationRepository,
        camundaWorkflowService,
        pwaContactService,
        pwaApplicationDetailService);
  }


  @Test
  public void createInitialPwa() {

    WebUserAccount user = new WebUserAccount(123);

    when(masterPwaDetail.getMasterPwa()).thenReturn(masterPwa);
    when(masterPwaManagementService.createMasterPwa(any(), any())).thenReturn(masterPwaDetail);
    when(pwaApplicationRepository.getNextRefNum()).thenReturn((long) 1);

    PwaApplication createdApplication = pwaApplicationCreationService.createInitialPwaApplication(user);

    ArgumentCaptor<PwaApplication> applicationArgumentCaptor = ArgumentCaptor.forClass(PwaApplication.class);

    verify(pwaApplicationRepository, times(1)).save(applicationArgumentCaptor.capture());
    verify(pwaApplicationDetailService, times(1)).createFirstDetail(createdApplication, user);
    verify(camundaWorkflowService, times(1)).startWorkflow(WorkflowType.PWA_APPLICATION,
        applicationArgumentCaptor.getValue().getId());

    PwaApplication application = applicationArgumentCaptor.getValue();

    verify(pwaContactService, times(1)).addContact(application, user.getLinkedPerson(),
        Set.of(PwaContactRole.ACCESS_MANAGER, PwaContactRole.PREPARER));

    // check application set up correctly
    assertThat(application.getMasterPwa()).isEqualTo(masterPwa);
    assertThat(application.getApplicationType()).isEqualTo(PwaApplicationType.INITIAL);
    assertThat(application.getAppReference()).isEqualTo("PA/1/1");
    assertThat(application.getConsentReference()).isNull();
    assertThat(application.getVariationNo()).isEqualTo(0);
    assertThat(application.getDecision()).isEmpty();
    assertThat(application.getDecisionTimestamp()).isEmpty();

    assertThat(createdApplication).isEqualTo(application);

  }


  @Test
  public void createVariationPwaApplication_createsApplicationsAsExpected_whenCategory1() {
    createVariationPwaApplication_assertUsingType(PwaApplicationType.CAT_1_VARIATION);
  }

  private void createVariationPwaApplication_assertUsingType(PwaApplicationType pwaApplicationType) {
    WebUserAccount user = new WebUserAccount(123);

    MasterPwa masterPwa = new MasterPwa(fixedInstant);
    masterPwa.setId(1);

    when(pwaApplicationRepository.getNextRefNum()).thenReturn((long) 1);

    PwaApplication createdApplication = pwaApplicationCreationService.createVariationPwaApplication(
        user,
        masterPwa,
        pwaApplicationType
    );

    ArgumentCaptor<PwaApplication> applicationArgumentCaptor = ArgumentCaptor.forClass(PwaApplication.class);

    verify(pwaApplicationRepository, times(1)).save(applicationArgumentCaptor.capture());
    verify(pwaApplicationDetailService, times(1)).createFirstDetail(createdApplication, user);
    verify(camundaWorkflowService, times(1)).startWorkflow(
        WorkflowType.PWA_APPLICATION,
        applicationArgumentCaptor.getValue().getId()
    );

    PwaApplication application = applicationArgumentCaptor.getValue();

    verify(pwaContactService, times(1)).addContact(application, user.getLinkedPerson(),
        Set.of(PwaContactRole.ACCESS_MANAGER, PwaContactRole.PREPARER));

    // check application set up correctly
    assertThat(application.getMasterPwa()).isEqualTo(masterPwa);
    assertThat(application.getApplicationType()).isEqualTo(pwaApplicationType);
    assertThat(application.getAppReference()).isEqualTo("PA/1/1");
    assertThat(application.getConsentReference()).isNull();
    assertThat(application.getVariationNo()).isEqualTo(0);
    assertThat(application.getDecision()).isEmpty();
    assertThat(application.getDecisionTimestamp()).isEmpty();

    assertThat(createdApplication).isEqualTo(application);

  }


}