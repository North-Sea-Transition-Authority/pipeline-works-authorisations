package uk.co.ogauthority.pwa.features.application.creation;

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
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.domain.pwa.application.repository.PwaApplicationRepository;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactRole;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactService;
import uk.co.ogauthority.pwa.features.application.tasks.fieldinfo.PadAreaService;
import uk.co.ogauthority.pwa.features.application.tasks.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.integrations.camunda.external.CamundaWorkflowService;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetailArea;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaDetailAreaService;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentOrganisationRoleService;

@ExtendWith(MockitoExtension.class)
class PwaApplicationCreationServiceTest {

  private static final EnumSet<PwaApplicationType> EXPECTED_HUOO_ROLE_CREATION_TYPES = EnumSet.of(
      PwaApplicationType.INITIAL,
      PwaApplicationType.CAT_1_VARIATION,
      PwaApplicationType.CAT_2_VARIATION,
      PwaApplicationType.DECOMMISSIONING,
      PwaApplicationType.HUOO_VARIATION,
      PwaApplicationType.OPTIONS_VARIATION
  );

  private ArgumentCaptor<PwaApplication> applicationArgumentCaptor = ArgumentCaptor.forClass(PwaApplication.class);

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
  private MasterPwaDetailAreaService masterPwaDetailAreaService;

  @Mock
  private PadAreaService padAreaService;

  private PwaApplicationCreationService pwaApplicationCreationService;

  private final WebUserAccount user = new WebUserAccount(123);

  private final Clock clock = Clock.fixed(LocalDate
      .of(2020, 2, 6)
      .atStartOfDay(ZoneId.systemDefault())
      .toInstant(), ZoneId.systemDefault());

  private final Instant fixedInstant = clock.instant();

  private final PortalOrganisationUnit applicantOrganisationUnit = PortalOrganisationTestUtils.generateOrganisationUnit(1, "Umbrella");

  @BeforeEach
  void setUp() {
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
        masterPwaDetailAreaService,
        padAreaService,
        clock
    );
  }


  @Test
  void createInitialPwa_Petroleum() {
    WebUserAccount user = new WebUserAccount(123);
    when(masterPwaDetail.getMasterPwa()).thenReturn(masterPwa);
    when(masterPwaService.createMasterPwa(any(), any(), any())).thenReturn(masterPwaDetail);

    PwaApplicationDetail createdApplication = pwaApplicationCreationService.createInitialPwaApplication(
        applicantOrganisationUnit,
        user,
        PwaResourceType.PETROLEUM
    );

    verify(pwaApplicationRepository, times(1)).save(applicationArgumentCaptor.capture());
    PwaApplication application = applicationArgumentCaptor.getValue();

    verify(pwaApplicationDetailService, times(1)).createFirstDetail(createdApplication.getPwaApplication(), user, 1L);
    verify(camundaWorkflowService, times(1)).startWorkflow(application);
    verify(pwaContactService, times(1)).updateContact(application, user.getLinkedPerson(),
        Set.of(PwaContactRole.ACCESS_MANAGER, PwaContactRole.PREPARER));
    verify(masterPwaService, times(1)).updateDetailReference(masterPwaDetail, application.getAppReference());
    assertThat(application)
        .extracting(
            PwaApplication::getMasterPwa,
            PwaApplication::getApplicationType,
            PwaApplication::getResourceType,
            PwaApplication::getAppReference,
            PwaApplication::getConsentReference,
            PwaApplication::getVariationNo,
            PwaApplication::getDecision,
            PwaApplication::getDecisionTimestamp,
            PwaApplication::getApplicationCreatedTimestamp,
            PwaApplication::getApplicantOrganisationUnitId)
        .containsExactly(
            masterPwa,
            PwaApplicationType.INITIAL,
            PwaResourceType.PETROLEUM,
            "PA/1",
            null,
            0,
            Optional.empty(),
            Optional.empty(),
            clock.instant(),
            OrganisationUnitId.from(applicantOrganisationUnit)
        );

    assertThat(createdApplication.getPwaApplication()).isEqualTo(application);
  }

  @Test
  void createInitialPwa_Hydrogen() {
    WebUserAccount user = new WebUserAccount(123);
    when(masterPwaDetail.getMasterPwa()).thenReturn(masterPwa);
    when(masterPwaService.createMasterPwa(any(), any(), any())).thenReturn(masterPwaDetail);

    PwaApplicationDetail createdApplication = pwaApplicationCreationService.createInitialPwaApplication(
        applicantOrganisationUnit,
        user,
        PwaResourceType.HYDROGEN);

    ArgumentCaptor<PwaApplication> applicationArgumentCaptor = ArgumentCaptor.forClass(PwaApplication.class);

    verify(pwaApplicationRepository, times(1)).save(applicationArgumentCaptor.capture());
    PwaApplication application = applicationArgumentCaptor.getValue();

    verify(pwaApplicationDetailService, times(1)).createFirstDetail(createdApplication.getPwaApplication(), user, 1L);
    verify(camundaWorkflowService, times(1)).startWorkflow(application);
    verify(pwaContactService, times(1)).updateContact(application, user.getLinkedPerson(),
        Set.of(PwaContactRole.ACCESS_MANAGER, PwaContactRole.PREPARER));
    verify(masterPwaService, times(1)).updateDetailReference(masterPwaDetail, application.getAppReference());
    assertThat(application)
        .extracting(
            PwaApplication::getMasterPwa,
            PwaApplication::getApplicationType,
            PwaApplication::getResourceType,
            PwaApplication::getAppReference,
            PwaApplication::getConsentReference,
            PwaApplication::getVariationNo,
            PwaApplication::getDecision,
            PwaApplication::getDecisionTimestamp,
            PwaApplication::getApplicationCreatedTimestamp,
            PwaApplication::getApplicantOrganisationUnitId)
        .containsExactly(
            masterPwa,
            PwaApplicationType.INITIAL,
            PwaResourceType.HYDROGEN,
            "PA/1",
            null,
            0,
            Optional.empty(),
            Optional.empty(),
            clock.instant(),
            OrganisationUnitId.from(applicantOrganisationUnit)
        );

    assertThat(createdApplication.getPwaApplication()).isEqualTo(application);
  }

  @Test
  void createInitialPwa_CCUS() {
    WebUserAccount user = new WebUserAccount(123);
    when(masterPwaDetail.getMasterPwa()).thenReturn(masterPwa);
    when(masterPwaService.createMasterPwa(any(), any(), any())).thenReturn(masterPwaDetail);

    PwaApplicationDetail createdApplication = pwaApplicationCreationService.createInitialPwaApplication(
        applicantOrganisationUnit,
        user,
        PwaResourceType.CCUS);

    ArgumentCaptor<PwaApplication> applicationArgumentCaptor = ArgumentCaptor.forClass(PwaApplication.class);

    verify(pwaApplicationRepository, times(1)).save(applicationArgumentCaptor.capture());
    PwaApplication application = applicationArgumentCaptor.getValue();

    verify(pwaApplicationDetailService, times(1)).createFirstDetail(createdApplication.getPwaApplication(), user, 1L);
    verify(camundaWorkflowService, times(1)).startWorkflow(application);
    verify(pwaContactService, times(1)).updateContact(application, user.getLinkedPerson(),
        Set.of(PwaContactRole.ACCESS_MANAGER, PwaContactRole.PREPARER));
    verify(masterPwaService, times(1)).updateDetailReference(masterPwaDetail, application.getAppReference());
    assertThat(application)
        .extracting(
            PwaApplication::getMasterPwa,
            PwaApplication::getApplicationType,
            PwaApplication::getResourceType,
            PwaApplication::getAppReference,
            PwaApplication::getConsentReference,
            PwaApplication::getVariationNo,
            PwaApplication::getDecision,
            PwaApplication::getDecisionTimestamp,
            PwaApplication::getApplicationCreatedTimestamp,
            PwaApplication::getApplicantOrganisationUnitId)
        .containsExactly(
            masterPwa,
            PwaApplicationType.INITIAL,
            PwaResourceType.CCUS,
            "PA/1",
            null,
            0,
            Optional.empty(),
            Optional.empty(),
            clock.instant(),
            OrganisationUnitId.from(applicantOrganisationUnit)
        );

    assertThat(createdApplication.getPwaApplication()).isEqualTo(application);
  }


  // The below tests could be much better with a parameterised, repeated test and only defined once. Would be good to figure out how to do this.
  @Test
  void createVariationPwaApplication_createsApplicationsAsExpected_whenCategory1() {
    createVariationPwaApplication_assertUsingType(PwaApplicationType.CAT_1_VARIATION);

    verify(padOrganisationRoleService, times(1)).createApplicationOrganisationRolesFromSummary(any(), any());

  }

  @Test
  void createVariationPwaApplication_createsApplicationsAsExpected_whenCategory2() {
    createVariationPwaApplication_assertUsingType(PwaApplicationType.CAT_2_VARIATION);

    verify(padOrganisationRoleService, times(1)).createApplicationOrganisationRolesFromSummary(any(), any());
  }

  @Test
  void createVariationPwaApplication_createsApplicationsAsExpected_whenHuoo() {
    createVariationPwaApplication_assertUsingType(PwaApplicationType.HUOO_VARIATION);

    verify(padOrganisationRoleService, times(1)).createApplicationOrganisationRolesFromSummary(any(), any());
  }

  @Test
  void createVariationPwaApplication_createsApplicationsAsExpected_whenOptions() {
    createVariationPwaApplication_assertUsingType(PwaApplicationType.OPTIONS_VARIATION);

    verify(padOrganisationRoleService, times(1)).createApplicationOrganisationRolesFromSummary(any(), any());
  }

  @Test
  void createVariationPwaApplication_createsApplicationsAsExpected_whenDecom() {
    createVariationPwaApplication_assertUsingType(PwaApplicationType.DECOMMISSIONING);

    verify(padOrganisationRoleService, times(1)).createApplicationOrganisationRolesFromSummary(any(), any());
  }

  @Test
  void createVariationPwaApplication_createsApplicationsAsExpected_noHuooOrgRolesExpectedToBeCreated() {

    for(PwaApplicationType appType : EnumSet.complementOf(EXPECTED_HUOO_ROLE_CREATION_TYPES)){
      pwaApplicationCreationService.createVariationPwaApplication(masterPwa, appType, PwaResourceType.PETROLEUM, applicantOrganisationUnit, user);
    }

    verifyNoInteractions(padOrganisationRoleService);
    verify(pwaConsentOrganisationRoleService, never()).getActiveOrganisationRoleSummaryForSeabedPipelines(masterPwa);
  }


  private void createVariationPwaApplication_assertUsingType(PwaApplicationType pwaApplicationType) {

    MasterPwa masterPwa = new MasterPwa(fixedInstant);
    masterPwa.setId(1);

    var masterPwaDetailField = new MasterPwaDetailArea();
    when(masterPwaDetailAreaService.getMasterPwaDetailFields(masterPwa)).thenReturn(List.of(masterPwaDetailField));
    var masterPwaDetail = new MasterPwaDetail();
    when(masterPwaService.getCurrentDetailOrThrow(masterPwa)).thenReturn(masterPwaDetail);

    PwaApplicationDetail createdApplication = pwaApplicationCreationService.createVariationPwaApplication(
        masterPwa,
        pwaApplicationType,
        PwaResourceType.PETROLEUM,
        applicantOrganisationUnit,
        user);

    ArgumentCaptor<PwaApplication> applicationArgumentCaptor = ArgumentCaptor.forClass(PwaApplication.class);

    verify(pwaApplicationRepository, times(1)).save(applicationArgumentCaptor.capture());
    PwaApplication application = applicationArgumentCaptor.getValue();

    verify(pwaApplicationDetailService, times(1)).createFirstDetail(createdApplication.getPwaApplication(), user, 0L);
    verify(camundaWorkflowService, times(1)).startWorkflow(application);
    verify(pwaContactService, times(1)).updateContact(application, user.getLinkedPerson(),
        Set.of(PwaContactRole.ACCESS_MANAGER, PwaContactRole.PREPARER));
    verify(padAreaService, times(1)).createAndSavePadFieldsFromMasterPwa(
        createdApplication, masterPwaDetail, List.of(masterPwaDetailField));;

    assertThat(application)
        .extracting(
            PwaApplication::getMasterPwa,
            PwaApplication::getApplicationType,
            PwaApplication::getResourceType,
            PwaApplication::getAppReference,
            PwaApplication::getConsentReference,
            PwaApplication::getVariationNo,
            PwaApplication::getDecision,
            PwaApplication::getDecisionTimestamp,
            PwaApplication::getApplicationCreatedTimestamp,
            PwaApplication::getApplicantOrganisationUnitId)
        .containsExactly(
            masterPwa,
            pwaApplicationType,
            PwaResourceType.PETROLEUM,
            "PA/1",
            null,
            0,
            Optional.empty(),
            Optional.empty(),
            clock.instant(),
            OrganisationUnitId.from(applicantOrganisationUnit)
        );
    assertThat(createdApplication.getPwaApplication()).isEqualTo(application);
  }
}
