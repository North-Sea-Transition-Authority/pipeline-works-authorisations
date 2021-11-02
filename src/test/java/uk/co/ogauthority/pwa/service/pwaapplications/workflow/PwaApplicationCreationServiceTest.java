package uk.co.ogauthority.pwa.service.pwaapplications.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactRole;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactService;
import uk.co.ogauthority.pwa.features.application.tasks.fieldinfo.PadFieldService;
import uk.co.ogauthority.pwa.features.application.tasks.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetailField;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.repository.pwaapplications.PwaApplicationRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaDetailFieldService;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentOrganisationRoleService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;

@RunWith(MockitoJUnitRunner.class)
public class PwaApplicationCreationServiceTest {

  private static final EnumSet<PwaApplicationType> EXPECTED_HUOO_ROLE_CREATION_TYPES = EnumSet.of(
      PwaApplicationType.INITIAL,
      PwaApplicationType.CAT_1_VARIATION,
      PwaApplicationType.CAT_2_VARIATION,
      PwaApplicationType.DECOMMISSIONING,
      PwaApplicationType.HUOO_VARIATION,
      PwaApplicationType.OPTIONS_VARIATION
  );

  @Mock
  private MasterPwaService masterPwaService;

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

  @Mock
  private PwaApplicationReferencingService pwaApplicationReferencingService;

  @Mock
  private PwaConsentOrganisationRoleService pwaConsentOrganisationRoleService;

  @Mock
  private PadOrganisationRoleService padOrganisationRoleService;

  @Mock
  private MasterPwaDetailFieldService masterPwaDetailFieldService;

  @Mock
  private PadFieldService padFieldService;

  private PwaApplicationCreationService pwaApplicationCreationService;

  private final WebUserAccount user = new WebUserAccount(123);

  private final Clock clock = Clock.fixed(LocalDate
      .of(2020, 2, 6)
      .atStartOfDay(ZoneId.systemDefault())
      .toInstant(), ZoneId.systemDefault());

  private final Instant fixedInstant = clock.instant();

  private final PortalOrganisationUnit applicantOrganisationUnit = PortalOrganisationTestUtils.generateOrganisationUnit(1, "Umbrella");

  @Before
  public void setUp() {
    when(pwaApplicationReferencingService.createAppReference()).thenReturn("PA/1");

    when(pwaApplicationDetailService.createFirstDetail(any(), any(), any()))
        .thenAnswer(invocation -> new PwaApplicationDetail(invocation.getArgument(0), 1, 1, Instant.now()));

    pwaApplicationCreationService = new PwaApplicationCreationService(
        masterPwaService,
        pwaApplicationRepository,
        camundaWorkflowService,
        pwaContactService,
        pwaApplicationDetailService,
        pwaApplicationReferencingService,
        pwaConsentOrganisationRoleService,
        padOrganisationRoleService,
        masterPwaDetailFieldService,
        padFieldService,
        clock
    );
  }


  @Test
  public void createInitialPwa() {

    WebUserAccount user = new WebUserAccount(123);

    when(masterPwaDetail.getMasterPwa()).thenReturn(masterPwa);
    when(masterPwaService.createMasterPwa(any(), any())).thenReturn(masterPwaDetail);

    PwaApplicationDetail createdApplication = pwaApplicationCreationService.createInitialPwaApplication(applicantOrganisationUnit, user);

    ArgumentCaptor<PwaApplication> applicationArgumentCaptor = ArgumentCaptor.forClass(PwaApplication.class);

    verify(pwaApplicationRepository, times(1)).save(applicationArgumentCaptor.capture());
    verify(pwaApplicationDetailService, times(1)).createFirstDetail(createdApplication.getPwaApplication(), user, 1L);

    PwaApplication application = applicationArgumentCaptor.getValue();

    verify(camundaWorkflowService, times(1)).startWorkflow(application);

    verify(pwaContactService, times(1)).updateContact(application, user.getLinkedPerson(),
        Set.of(PwaContactRole.ACCESS_MANAGER, PwaContactRole.PREPARER));


    // check application set up correctly
    assertThat(application.getMasterPwa()).isEqualTo(masterPwa);
    assertThat(application.getApplicationType()).isEqualTo(PwaApplicationType.INITIAL);
    assertThat(application.getAppReference()).isEqualTo("PA/1");
    assertThat(application.getConsentReference()).isNull();
    assertThat(application.getVariationNo()).isEqualTo(0);
    assertThat(application.getDecision()).isEmpty();
    assertThat(application.getDecisionTimestamp()).isEmpty();
    assertThat(application.getApplicationCreatedTimestamp()).isEqualTo(clock.instant());
    assertThat(application.getApplicantOrganisationUnitId()).isEqualTo(OrganisationUnitId.from(applicantOrganisationUnit));

    assertThat(createdApplication.getPwaApplication()).isEqualTo(application);
    verify(masterPwaService, times(1)).updateDetailReference(masterPwaDetail, application.getAppReference());

  }


  // The below tests could be much better with a parameterised, repeated test and only defined once. Would be good to figure out how to do this.
  @Test
  public void createVariationPwaApplication_createsApplicationsAsExpected_whenCategory1() {
    createVariationPwaApplication_assertUsingType(PwaApplicationType.CAT_1_VARIATION);

    verify(padOrganisationRoleService, times(1)).createApplicationOrganisationRolesFromSummary(any(), any());

  }

  @Test
  public void createVariationPwaApplication_createsApplicationsAsExpected_whenCategory2() {
    createVariationPwaApplication_assertUsingType(PwaApplicationType.CAT_2_VARIATION);

    verify(padOrganisationRoleService, times(1)).createApplicationOrganisationRolesFromSummary(any(), any());
  }

  @Test
  public void createVariationPwaApplication_createsApplicationsAsExpected_whenHuoo() {
    createVariationPwaApplication_assertUsingType(PwaApplicationType.HUOO_VARIATION);

    verify(padOrganisationRoleService, times(1)).createApplicationOrganisationRolesFromSummary(any(), any());
  }

  @Test
  public void createVariationPwaApplication_createsApplicationsAsExpected_whenOptions() {
    createVariationPwaApplication_assertUsingType(PwaApplicationType.OPTIONS_VARIATION);

    verify(padOrganisationRoleService, times(1)).createApplicationOrganisationRolesFromSummary(any(), any());
  }

  @Test
  public void createVariationPwaApplication_createsApplicationsAsExpected_whenDecom() {
    createVariationPwaApplication_assertUsingType(PwaApplicationType.DECOMMISSIONING);

    verify(padOrganisationRoleService, times(1)).createApplicationOrganisationRolesFromSummary(any(), any());
  }

  @Test
  public void createVariationPwaApplication_createsApplicationsAsExpected_noHuooOrgRolesExpectedToBeCreated() {

    for(PwaApplicationType appType : EnumSet.complementOf(EXPECTED_HUOO_ROLE_CREATION_TYPES)){
      pwaApplicationCreationService.createVariationPwaApplication(masterPwa, appType, applicantOrganisationUnit, user);
    }

    verifyNoInteractions(padOrganisationRoleService);
    verify(pwaConsentOrganisationRoleService, never()).getActiveOrganisationRoleSummaryForSeabedPipelines(masterPwa);
  }


  private void createVariationPwaApplication_assertUsingType(PwaApplicationType pwaApplicationType) {

    MasterPwa masterPwa = new MasterPwa(fixedInstant);
    masterPwa.setId(1);

    var masterPwaDetailField = new MasterPwaDetailField();
    when(masterPwaDetailFieldService.getMasterPwaDetailFields(masterPwa)).thenReturn(List.of(masterPwaDetailField));
    var masterPwaDetail = new MasterPwaDetail();
    when(masterPwaService.getCurrentDetailOrThrow(masterPwa)).thenReturn(masterPwaDetail);

    PwaApplicationDetail createdApplication = pwaApplicationCreationService.createVariationPwaApplication(
        masterPwa,
        pwaApplicationType,
        applicantOrganisationUnit,
        user);

    ArgumentCaptor<PwaApplication> applicationArgumentCaptor = ArgumentCaptor.forClass(PwaApplication.class);

    verify(pwaApplicationRepository, times(1)).save(applicationArgumentCaptor.capture());
    verify(pwaApplicationDetailService, times(1)).createFirstDetail(createdApplication.getPwaApplication(), user, 0L);

    PwaApplication application = applicationArgumentCaptor.getValue();

    verify(camundaWorkflowService, times(1)).startWorkflow(application);

    verify(pwaContactService, times(1)).updateContact(application, user.getLinkedPerson(),
        Set.of(PwaContactRole.ACCESS_MANAGER, PwaContactRole.PREPARER));

    verify(padFieldService, times(1)).createAndSavePadFieldsFromMasterPwa(
        createdApplication, masterPwaDetail, List.of(masterPwaDetailField));;

    // check application set up correctly
    assertThat(application.getMasterPwa()).isEqualTo(masterPwa);
    assertThat(application.getApplicationType()).isEqualTo(pwaApplicationType);
    assertThat(application.getAppReference()).isEqualTo("PA/1");
    assertThat(application.getConsentReference()).isNull();
    assertThat(application.getVariationNo()).isEqualTo(0);
    assertThat(application.getDecision()).isEmpty();
    assertThat(application.getDecisionTimestamp()).isEmpty();
    assertThat(application.getApplicantOrganisationUnitId()).isEqualTo(OrganisationUnitId.from(applicantOrganisationUnit));

    assertThat(createdApplication.getPwaApplication()).isEqualTo(application);

  }


}