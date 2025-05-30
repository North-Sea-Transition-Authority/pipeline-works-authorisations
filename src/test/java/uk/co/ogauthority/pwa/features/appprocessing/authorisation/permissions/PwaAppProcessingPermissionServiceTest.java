package uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.co.ogauthority.pwa.features.application.authorisation.involvement.ApplicationInvolvementDtoTestUtil.InvolvementFlag.CASE_OFFICER_STAGE_AND_USER_ASSIGNED;
import static uk.co.ogauthority.pwa.features.application.authorisation.involvement.ApplicationInvolvementDtoTestUtil.InvolvementFlag.PWA_MANAGER_STAGE;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.HasTeamRoleService;
import uk.co.ogauthority.pwa.auth.RoleGroup;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactRole;
import uk.co.ogauthority.pwa.features.application.authorisation.involvement.ApplicationInvolvementDtoTestUtil;
import uk.co.ogauthority.pwa.features.application.authorisation.involvement.ApplicationInvolvementService;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ConsultationInvolvementDto;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ApplicationState;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.service.users.UserTypeService;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamType;
import uk.co.ogauthority.pwa.testutils.AssertionTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaAppProcessingContextDtoTestUtils;

@ExtendWith(MockitoExtension.class)
class PwaAppProcessingPermissionServiceTest {

  @Mock
  private ApplicationInvolvementService applicationInvolvementService;

  @Mock
  private UserTypeService userTypeService;

  @Mock
  private HasTeamRoleService hasTeamRoleService;

  @InjectMocks
  private PwaAppProcessingPermissionService processingPermissionService;

  private AuthenticatedUserAccount user = new AuthenticatedUserAccount(new WebUserAccount(1), List.of());

  private PwaApplicationDetail detail;
  private PwaApplication application;
  private static Set<PwaApplicationType> VALID_PUBLIC_NOTICE_APP_TYPES;

  @BeforeEach
  void setUp() {

    application = new PwaApplication();
    application.setApplicationType(PwaApplicationType.INITIAL);
    detail = new PwaApplicationDetail();
    detail.setPwaApplication(application);
    VALID_PUBLIC_NOTICE_APP_TYPES = Set.of(PwaApplicationType.INITIAL, PwaApplicationType.CAT_1_VARIATION);

  }

  @Test
  void getGenericProcessingPermissions_acceptInitialReviewPermission_success() {
    boolean userIsCaseOfficer = false;
    boolean userIsPwaManager = true;
    var permissions = processingPermissionService.getGenericProcessingPermissions(userIsCaseOfficer, userIsPwaManager);

    AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.ACCEPT_INITIAL_REVIEW);

  }

  @Test
  void getGenericProcessingPermissions_noPrivs_noPermissions() {

    clearPrivileges(user);

    boolean userIsCaseOfficer = false;
    boolean userIsPwaManager = false;
    var permissions = processingPermissionService.getGenericProcessingPermissions(userIsCaseOfficer, userIsPwaManager);

    assertThat(permissions).isEmpty();

  }

  @Test
  void getGenericProcessingPermissions_acceptAssignCaseOfficerPermission_success() {

    boolean userIsCaseOfficer = true;
    boolean userIsPwaManager = true;
    var permissions = processingPermissionService.getGenericProcessingPermissions(userIsCaseOfficer, userIsPwaManager);

    AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.ASSIGN_CASE_OFFICER);

  }

  @Test
  void getGenericProcessingPermissions_hasAddCaseNotePermission_pwaManager() {

    boolean userIsCaseOfficer = true;
    boolean userIsPwaManager = true;
    var permissions = processingPermissionService.getGenericProcessingPermissions(userIsCaseOfficer, userIsPwaManager);
    AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.ADD_CASE_NOTE);

  }

  @Test
  void getGenericProcessingPermissions_hasAddCaseNotePermission_caseOfficer() {

    boolean userIsCaseOfficer = true;
    boolean userIsPwaManager = false;
    var permissions = processingPermissionService.getGenericProcessingPermissions(userIsCaseOfficer, userIsPwaManager);
    AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.ADD_CASE_NOTE);

  }

  @Test
  void getGenericProcessingPermissions_hasShowAllTasks_pwaManagerOnly_hasPermission() {
    boolean userIsCaseOfficer = false;
    boolean userIsPwaManager = true;
    var permissions = processingPermissionService.getGenericProcessingPermissions(userIsCaseOfficer, userIsPwaManager);
    AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.SHOW_ALL_TASKS_AS_PWA_MANAGER_ONLY);

  }

  @Test
  void getGenericProcessingPermissions_hasShowAllTasks_pwaManagerAndCaseOfficer_noPermission() {

    boolean userIsCaseOfficer = true;
    boolean userIsPwaManager = true;
    var permissions = processingPermissionService.getGenericProcessingPermissions(userIsCaseOfficer, userIsPwaManager);
    AssertionTestUtils.assertNotEmptyAndDoesNotContain(permissions, PwaAppProcessingPermission.SHOW_ALL_TASKS_AS_PWA_MANAGER_ONLY);

  }

  @Test
  void getGenericProcessingPermissions_acceptViewAllConsultationsPermission_co_success() {

    boolean userIsCaseOfficer = true;
    boolean userIsPwaManager = false;
    var permissions = processingPermissionService.getGenericProcessingPermissions(userIsCaseOfficer, userIsPwaManager);
    AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.VIEW_ALL_CONSULTATIONS);

  }

  @Test
  void getGenericProcessingPermissions_acceptViewAllConsultationsPermission_pwaManager_success() {

    boolean userIsCaseOfficer = true;
    boolean userIsPwaManager = true;
    var permissions = processingPermissionService.getGenericProcessingPermissions(userIsCaseOfficer, userIsPwaManager);
    AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.VIEW_ALL_CONSULTATIONS);

  }

  @Test
  void getGenericProcessingPermissions_consentReview_pwaManager_success() {

    boolean userIsCaseOfficer = true;
    boolean userIsPwaManager = true;
    var permissions = processingPermissionService.getGenericProcessingPermissions(userIsCaseOfficer, userIsPwaManager);
    AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.CONSENT_REVIEW);

  }

  @Test
  void getAppProcessingPermissions_acceptWithdrawConsultationsPermission_assignedCo_success() {

    mockUserIsCaseOfficer();

    var appInvolvement = ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(
        application,
        EnumSet.of(CASE_OFFICER_STAGE_AND_USER_ASSIGNED)
    );
    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.WITHDRAW_CONSULTATION);
  }

  @Test
  void getAppProcessingPermissions_acceptWithdrawConsultationsPermission_notAssignedCo_failed() {

    mockUserIsCaseOfficer();

    var appInvolvement = ApplicationInvolvementDtoTestUtil.noInvolvementAndNoFlags(application);
    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    AssertionTestUtils.assertNotEmptyAndDoesNotContain(permissions, PwaAppProcessingPermission.WITHDRAW_CONSULTATION);
  }

  @Test
  void getAppProcessingPermissions_noPrivs_noPermissions() {

    clearPrivileges(user);

    var appInvolvement = ApplicationInvolvementDtoTestUtil.noInvolvementAndNoFlags(application);
    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    assertThat(permissions).isEmpty();
  }


  @Test
  void getAppProcessingPermissions_hasAssignResponderPermission_recipient() {

    var appInvolvement = ApplicationInvolvementDtoTestUtil.generateConsulteeInvolvement(
        application,
        getConsultationInvolvement(false, Set.of(Role.RECIPIENT))
    );

    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.ASSIGN_RESPONDER);

  }

  @Test
  void getAppProcessingPermissions_hasAssignResponderPermission_responder() {

    var appInvolvement = ApplicationInvolvementDtoTestUtil.generateConsulteeInvolvement(
        application,
        getConsultationInvolvement(false, Set.of(Role.RESPONDER))
    );
    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.ASSIGN_RESPONDER);

  }

  @Test
  void getAppProcessingPermissions_noAssignResponderPermission() {
    var appInvolvement = ApplicationInvolvementDtoTestUtil.generateConsulteeInvolvement(
        application,
        getConsultationInvolvement(false, Set.of(Role.TEAM_ADMINISTRATOR))
    );

    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    AssertionTestUtils.assertNotEmptyAndDoesNotContain(permissions, PwaAppProcessingPermission.ASSIGN_RESPONDER);

  }

  @Test
  void getAppProcessingPermissions_hasConsultationResponderPermission_ifAssignedAsResponder() {

    var appInvolvement = ApplicationInvolvementDtoTestUtil.generateConsulteeInvolvement(
        application,
        getConsultationInvolvement(true, Set.of(Role.RESPONDER))
    );
    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.CONSULTATION_RESPONDER);
  }

  @Test
  void getAppProcessingPermissions_noConsultationResponderPermission_ifNotAssignedAsResponder() {

    var appInvolvement = ApplicationInvolvementDtoTestUtil.generateConsulteeInvolvement(
        application,
        getConsultationInvolvement(false, Set.of(Role.RESPONDER))
    );
    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    AssertionTestUtils.assertNotEmptyAndDoesNotContain(permissions, PwaAppProcessingPermission.CONSULTATION_RESPONDER);
  }

  @Test
  void getAppProcessingPermissions_noConsultationResponderPermission() {

    var appInvolvement = ApplicationInvolvementDtoTestUtil.generateConsulteeInvolvement(
        application,
        getConsultationInvolvement(false,
            Set.of(Role.RECIPIENT, Role.TEAM_ADMINISTRATOR))
    );

    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    AssertionTestUtils.assertNotEmptyAndDoesNotContain(permissions, PwaAppProcessingPermission.CONSULTATION_RESPONDER);
  }

  @Test
  void getAppProcessingPermissions_hasEditConsentDocumentPermission_assignedCaseOfficer() {

    mockUserIsCaseOfficer();

    var appInvolvement = ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(
        application,
        EnumSet.of(CASE_OFFICER_STAGE_AND_USER_ASSIGNED)
    );
    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.EDIT_CONSENT_DOCUMENT);

  }

  @Test
  void getAppProcessingPermissions_hasEditConsentDocumentPermission_notAssignedCaseOfficer() {
    mockUserIsCaseOfficer();

    var appInvolvement = ApplicationInvolvementDtoTestUtil.noInvolvementAndNoFlags(application);
    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    AssertionTestUtils.assertNotEmptyAndDoesNotContain(permissions, PwaAppProcessingPermission.EDIT_CONSENT_DOCUMENT);

  }

  @Test
  void getAppProcessingPermissions_hasEditConsentDocumentPermission_pwaManager() {

    mockUserIsPwaManager();

    var appInvolvement = ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(
        application,
        EnumSet.noneOf(ApplicationInvolvementDtoTestUtil.InvolvementFlag.class)
    );
    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.EDIT_CONSENT_DOCUMENT);

  }


  @Test
  void getAppProcessingPermissions_hasViewConsentDocumentPermission_userInHolderTeam() {

    user = new AuthenticatedUserAccount(user, Set.of());

    var appInvolvement = ApplicationInvolvementDtoTestUtil.generatePwaHolderTeamInvolvement(
        application, Set.of(Role.APPLICATION_CREATOR));
    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.VIEW_CONSENT_DOCUMENT);

  }

  @Test
  void getAppProcessingPermissions_hasViewConsentDocumentPermission_validUserPrivileges() {

    user = new AuthenticatedUserAccount(user, Set.of());

    when(hasTeamRoleService.userHasAnyRoleInTeamTypes(user, RoleGroup.CONSENT_SEARCH.getRolesByTeamType())).thenReturn(true);

    var appInvolvement = ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(
        application,
        EnumSet.noneOf(ApplicationInvolvementDtoTestUtil.InvolvementFlag.class)
    );
    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.VIEW_CONSENT_DOCUMENT);

  }

  @Test
  void getAppProcessingPermissions_noViewConsentDocumentPermission_invalidUserPrivileges() {

    user = new AuthenticatedUserAccount(user, Set.of());

    var appInvolvement = ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(
        application,
        EnumSet.of(CASE_OFFICER_STAGE_AND_USER_ASSIGNED)
    );
    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    assertThat(permissions).isEmpty();

  }


  @Test
  void getAppPermissions_hasUpdateApplicationPermission_isContact_Preparer() {

    var appInvolvement = ApplicationInvolvementDtoTestUtil.generatePwaContactInvolvement(
        application,
        Set.of(PwaContactRole.PREPARER)
    );
    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();

    AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.UPDATE_APPLICATION);

  }

  @Test
  void getAppPermissions_noUpdateApplicationPermission_isContact_notPreparer() {

    var appInvolvement = ApplicationInvolvementDtoTestUtil.generatePwaContactInvolvement(
        application,
        Set.of(PwaContactRole.VIEWER)
    );
    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();

    AssertionTestUtils.assertNotEmptyAndDoesNotContain(permissions, PwaAppProcessingPermission.UPDATE_APPLICATION);

  }

  @Test
  void getAppPermissions_noUpdateApplicationPermission_notContact() {

    var appInvolvement = ApplicationInvolvementDtoTestUtil.noInvolvementAndNoFlags(application);
    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();

    assertThat(permissions).isEmpty();

  }

  @Test
  void getAppPermissions_isContact_notInHolderTeam_appNotComplete_hasCaseManagementIndustryPermission_andViewApplicationSummary() {

    setUserAsIndustryContactPreparer();

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY);
    AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.VIEW_APPLICATION_SUMMARY);

  }

  @Test
  void getAppPermissions_isContact_notInHolderTeam_appComplete_noCaseManagementIndustryOrViewAppSummaryPermissions() {

    setUserAsIndustryContactPreparer();

    detail.setStatus(PwaApplicationStatus.COMPLETE);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    AssertionTestUtils.assertNotEmptyAndDoesNotContain(permissions, PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY);
    AssertionTestUtils.assertNotEmptyAndDoesNotContain(permissions, PwaAppProcessingPermission.VIEW_APPLICATION_SUMMARY);

  }

  @Test
  void getAppPermissions_isNotContact_isInHolderTeam_appNotComplete_hasCaseManagementIndustryPermission_andViewApplicationSummary() {

    setUserAsHolderTeamMember();

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY);
    AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.VIEW_APPLICATION_SUMMARY);

  }

  @Test
  void getAppPermissions_isNotContact_isInHolderTeam_appComplete_hasCaseManagementIndustryPermission_andViewApplicationSummary() {

    setUserAsHolderTeamMember();

    detail.setStatus(PwaApplicationStatus.COMPLETE);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY);
    AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.VIEW_APPLICATION_SUMMARY);

  }

  @Test
  void getAppPermissions_notInHolderTeam_notContact_noCaseManagementIndustryOrViewAppSummaryPermissions() {

    when(applicationInvolvementService.getApplicationInvolvementDto(any(), any()))
        .thenReturn(PwaAppProcessingContextDtoTestUtils.emptyAppInvolvement(detail.getPwaApplication()));

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    assertThat(permissions).isEmpty();

  }

  @Test
  void getAppPermissions_industryUser_andOptionsAppType() {
    application.setApplicationType(PwaApplicationType.OPTIONS_VARIATION);
    setUserAsIndustryContactPreparer();

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.APPROVE_OPTIONS_VIEW);
    AssertionTestUtils.assertNotEmptyAndDoesNotContain(permissions, PwaAppProcessingPermission.APPROVE_OPTIONS);
  }

  @Test
  void getAppPermissions_regulatorUser_andOptionsAppType() {
    application.setApplicationType(PwaApplicationType.OPTIONS_VARIATION);
    setUserAsCaseOfficer();

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    AssertionTestUtils.assertNotEmptyAndDoesNotContain(permissions, PwaAppProcessingPermission.APPROVE_OPTIONS_VIEW);
    AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.APPROVE_OPTIONS);
  }

  @Test
  void getAppPermissions_industryUser_andNotOptionsAppType() {
    application.setApplicationType(PwaApplicationType.INITIAL);
    setUserAsIndustryContactPreparer();

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    AssertionTestUtils.assertNotEmptyAndDoesNotContain(permissions, PwaAppProcessingPermission.APPROVE_OPTIONS_VIEW);
    AssertionTestUtils.assertNotEmptyAndDoesNotContain(permissions, PwaAppProcessingPermission.APPROVE_OPTIONS);
  }

  private void setUserAsIndustryContactPreparer() {
    var appInvolvement = ApplicationInvolvementDtoTestUtil.generatePwaContactInvolvement(
        application,
        Set.of(PwaContactRole.PREPARER)
    );

    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);
  }

  private void setUserAsHolderTeamMember() {
    var appInvolvement = ApplicationInvolvementDtoTestUtil.generatePwaHolderTeamInvolvement(
        application,
        EnumSet.copyOf(TeamType.ORGANISATION.getAllowedRoles())
    );
    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);
  }

  private void setUserAsCaseOfficer() {
    mockUserIsCaseOfficer();

    var appInvolvement = ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(
        application,
        EnumSet.of(CASE_OFFICER_STAGE_AND_USER_ASSIGNED)
    );
    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);
  }

  @Test
  void getAppPermissions_regulatorUser_andNotOptionsAppType() {
    application.setApplicationType(PwaApplicationType.INITIAL);
    setUserAsCaseOfficer();

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    AssertionTestUtils.assertNotEmptyAndDoesNotContain(permissions, PwaAppProcessingPermission.APPROVE_OPTIONS_VIEW);
    AssertionTestUtils.assertNotEmptyAndDoesNotContain(permissions, PwaAppProcessingPermission.APPROVE_OPTIONS);
  }

  @Test
  void getAppPermissions_noCaseManagementIndustryPermission_andNoViewApplicationSummary() {

    var appInvolvement = ApplicationInvolvementDtoTestUtil.noInvolvementAndNoFlags(application);
    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    assertThat(permissions).isEmpty();

  }

  @Test
  void getAppPermissions_hasCaseManagementOgaPermission_andViewApplicationSummary() {
    when(userTypeService.getUserTypes(user)).thenReturn(Set.of(UserType.OGA));
    var appInvolvement = ApplicationInvolvementDtoTestUtil.noInvolvementAndNoFlags(application);
    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.CASE_MANAGEMENT_OGA);
    AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.VIEW_APPLICATION_SUMMARY);

  }

  @Test
  void getAppPermissions_hasCaseManagementConsulteePermission_andViewApplicationSummary() {

    var appInvolvement = ApplicationInvolvementDtoTestUtil.generateConsulteeInvolvement(
        application,
        getConsultationInvolvement(false, Set.of(Role.RESPONDER))
    );

    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.CASE_MANAGEMENT_CONSULTEE);
    AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.VIEW_APPLICATION_SUMMARY);

  }

  @Test
  void getAppPermissions_noCaseManagementConsulteePermission_andnoViewApplicationSummary() {

    var appInvolvement = ApplicationInvolvementDtoTestUtil.noInvolvementAndNoFlags(application);
    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    assertThat(permissions).isEmpty();

  }

  @Test
  void getAppPermissions_consulteeAdvicePermission_whenConsultee_andHistoricRequest() {

    var consultationInvolvement = new ConsultationInvolvementDto(null, Set.of(Role.RESPONDER), null, List.of(new ConsultationRequest()), false);
    var appInvolvement = ApplicationInvolvementDtoTestUtil.generateConsulteeInvolvement(
        application,
        consultationInvolvement
    );
    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.CONSULTEE_ADVICE);

  }

  @Test
  void getAppPermissions_noConsulteeAdvicePermission_whenConsultee_noHistoricRequest() {

    var consultationInvolvement = new ConsultationInvolvementDto(null, Set.of(Role.RESPONDER), null, List.of(), false);
    var appInvolvement = ApplicationInvolvementDtoTestUtil.generateConsulteeInvolvement(
        application,
        consultationInvolvement
    );
    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    AssertionTestUtils.assertNotEmptyAndDoesNotContain(permissions, PwaAppProcessingPermission.CONSULTEE_ADVICE);

  }

  @Test
  void getAppPermissions_noConsulteeAdvicePermission_whenConsultee_noInvolvement() {

    var appInvolvement = ApplicationInvolvementDtoTestUtil.noInvolvementAndNoFlags(application);
    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    assertThat(permissions).isEmpty();

  }

  @Test
  void getAppPermissions_noConsulteeAdvicePermission_whenIndustry() {

    var appInvolvement = ApplicationInvolvementDtoTestUtil.noInvolvementAndNoFlags(application);
    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    assertThat(permissions).isEmpty();

  }

  @Test
  void getAppPermissions_noConsulteeAdvicePermission_whenRegulator() {
    when(userTypeService.getUserTypes(user)).thenReturn(Set.of(UserType.OGA));
    var appInvolvement = ApplicationInvolvementDtoTestUtil.noInvolvementAndNoFlags(application);
    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    AssertionTestUtils.assertNotEmptyAndDoesNotContain(permissions, PwaAppProcessingPermission.CONSULTEE_ADVICE);

  }

  @Test
  void getAppProcessingPermissions_hasConfirmSatisfactoryPermission_assignedCaseOfficer() {

    mockUserIsCaseOfficer();

    var appInvolvement = ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(
        application,
        EnumSet.of(CASE_OFFICER_STAGE_AND_USER_ASSIGNED)
    );

    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.CONFIRM_SATISFACTORY_APPLICATION);

  }

  @Test
  void getAppProcessingPermissions_hasConfirmSatisfactoryPermission_notAssignedCaseOfficer() {

    mockUserIsCaseOfficer();

    var appInvolvement = ApplicationInvolvementDtoTestUtil.noInvolvementAndNoFlags(application);
    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    AssertionTestUtils.assertNotEmptyAndDoesNotContain(permissions, PwaAppProcessingPermission.CONFIRM_SATISFACTORY_APPLICATION);

  }

  @Test
  void getAppProcessingPermissions_requestUpdate_assignedCaseOfficer() {

    mockUserIsCaseOfficer();
    var appInvolvement = ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(
        application,
        EnumSet.of(CASE_OFFICER_STAGE_AND_USER_ASSIGNED)
    );

    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.REQUEST_APPLICATION_UPDATE);

  }

  @Test
  void getAppProcessingPermissions_requestUpdate_notAssignedCaseOfficer() {

    mockUserIsCaseOfficer();
    var appInvolvement = ApplicationInvolvementDtoTestUtil.noInvolvementAndNoFlags(application);
    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    AssertionTestUtils.assertNotEmptyAndDoesNotContain(permissions, PwaAppProcessingPermission.REQUEST_APPLICATION_UPDATE);

  }

  @Test
  void getAppProcessingPermissions_requestUpdate_pwaManager_andPwaManagerStage() {

    mockUserIsPwaManager();

    var appInvolvement = ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(
        application,
        EnumSet.of(PWA_MANAGER_STAGE)
    );

    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.REQUEST_APPLICATION_UPDATE);

  }

  @Test
  void getAppProcessingPermissions_cancelPayment_pwaManager_andAwaitingPayment() {

    mockUserIsPwaManager();

    detail.setStatus(PwaApplicationStatus.AWAITING_APPLICATION_PAYMENT);

    var appInvolvement = ApplicationInvolvementDtoTestUtil.noInvolvementAndNoFlags(
        application
    );

    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.CANCEL_PAYMENT);

  }

  @Test
  void getAppProcessingPermissions_cancelPayment_pwaManager_andNotAwaitingPayment() {

    mockUserIsPwaManager();

    var noCancelPaymentPermissionStatuses = EnumSet.allOf(PwaApplicationStatus.class);
    noCancelPaymentPermissionStatuses.remove(PwaApplicationStatus.AWAITING_APPLICATION_PAYMENT);

    var appInvolvement = ApplicationInvolvementDtoTestUtil.noInvolvementAndNoFlags(
        application
    );

    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    for (PwaApplicationStatus appStatus : noCancelPaymentPermissionStatuses) {
      try {
        detail.setStatus(PwaApplicationStatus.AWAITING_APPLICATION_PAYMENT);

        var permissions = processingPermissionService.getProcessingPermissionsDto(
            detail,
            user
        ).getProcessingPermissions();
        AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.CANCEL_PAYMENT);
      } catch (AssertionError e) {
        throw new AssertionError("Failed at status:" + appStatus, e);
      }
    }

  }

  @Test
  void getAppProcessingPermissions_requestUpdate_pwaManager_andNotPwaManagerStage() {

    mockUserIsPwaManager();

    var appInvolvement = ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(
        application,
        EnumSet.of(CASE_OFFICER_STAGE_AND_USER_ASSIGNED)
    );
    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    AssertionTestUtils.assertNotEmptyAndDoesNotContain(permissions, PwaAppProcessingPermission.REQUEST_APPLICATION_UPDATE);

  }

  @Test
  void getAppProcessingPermissions_withdrawApplication_assignedCaseOfficer() {

    mockUserIsCaseOfficer();

    var appInvolvement = ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(
        application,
        EnumSet.of(CASE_OFFICER_STAGE_AND_USER_ASSIGNED)
    );
    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.WITHDRAW_APPLICATION);

  }

  @Test
  void getAppProcessingPermissions_withdrawApplication_notAssignedCaseOfficer() {

    mockUserIsCaseOfficer();
    var appInvolvement = ApplicationInvolvementDtoTestUtil.noInvolvementAndNoFlags(application);
    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    AssertionTestUtils.assertNotEmptyAndDoesNotContain(permissions, PwaAppProcessingPermission.WITHDRAW_APPLICATION);

  }

  @Test
  void getAppProcessingPermissions_withdrawApplication_pwaManager_andPwaManagerStage() {

    mockUserIsPwaManager();

    var appInvolvement = ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(
        application,
        EnumSet.of(PWA_MANAGER_STAGE)
    );
    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.WITHDRAW_APPLICATION);

  }

  @Test
  void getAppProcessingPermissions_withdrawApplication_pwaManager_andNotPwaManagerStage() {

    mockUserIsPwaManager();

    var appInvolvement = ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(
        application,
        EnumSet.of(CASE_OFFICER_STAGE_AND_USER_ASSIGNED)
    );
    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    AssertionTestUtils.assertNotEmptyAndDoesNotContain(permissions, PwaAppProcessingPermission.WITHDRAW_APPLICATION);

  }


  @Test
  void getAppProcessingPermissions_hasViewAllPublicNoticePermission_allValidAppTypes_caseOfficerPrivilege() {

    PwaApplicationType.stream()
        .filter(appType -> VALID_PUBLIC_NOTICE_APP_TYPES.contains(appType))
        .forEach(pwaApplicationType -> {

          detail.getPwaApplication().setApplicationType(pwaApplicationType);
          mockUserIsCaseOfficer();

          var appInvolvement = ApplicationInvolvementDtoTestUtil.noInvolvementAndNoFlags(application);
          when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

          var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
          AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.VIEW_ALL_PUBLIC_NOTICES);
        });
  }

  @Test
  void getAppProcessingPermissions_hasViewAllPublicNoticePermission_allValidAppTypes_managerPrivilege() {

    PwaApplicationType.stream()
        .filter(appType -> VALID_PUBLIC_NOTICE_APP_TYPES.contains(appType))
        .forEach(pwaApplicationType -> {

          detail.getPwaApplication().setApplicationType(pwaApplicationType);

          mockUserIsPwaManager();


          var appInvolvement = ApplicationInvolvementDtoTestUtil.noInvolvementAndNoFlags(application);
          when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

          var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
          AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.VIEW_ALL_PUBLIC_NOTICES);
        });
  }

  @Test
  void getAppProcessingPermissions_hasDraftPublicNoticePermission_allInvalidAppTypes_caseOfficerPrivilege() {

    PwaApplicationType.stream()
        .filter(appType -> !VALID_PUBLIC_NOTICE_APP_TYPES.contains(appType))
        .forEach(pwaApplicationType -> {

          detail.getPwaApplication().setApplicationType(pwaApplicationType);
          mockUserIsCaseOfficer();

          var appInvolvement = ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(
              application,
              EnumSet.of(CASE_OFFICER_STAGE_AND_USER_ASSIGNED)
          );
          when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

          var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
          AssertionTestUtils.assertNotEmptyAndDoesNotContain(permissions, PwaAppProcessingPermission.VIEW_ALL_PUBLIC_NOTICES);
        });
  }


  @Test
  void getAppProcessingPermissions_hasOgaEditPublicNoticePermission_llValidAppTypes_caseOfficerPrivilege() {

    PwaApplicationType.stream()
        .filter(appType -> VALID_PUBLIC_NOTICE_APP_TYPES.contains(appType))
        .forEach(pwaApplicationType -> {

          detail.getPwaApplication().setApplicationType(pwaApplicationType);
          mockUserIsCaseOfficer();

          var appInvolvement = ApplicationInvolvementDtoTestUtil.noInvolvementAndNoFlags(application);
          when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

          var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
          AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.OGA_EDIT_PUBLIC_NOTICE);
        });
  }

  @Test
  void getAppProcessingPermissions_hasOgaEditPublicNoticePermission_allValidAppTypes_managerPrivilege() {

    PwaApplicationType.stream()
        .filter(appType -> VALID_PUBLIC_NOTICE_APP_TYPES.contains(appType))
        .forEach(pwaApplicationType -> {

          detail.getPwaApplication().setApplicationType(pwaApplicationType);
          mockUserIsPwaManager();

          var appInvolvement = ApplicationInvolvementDtoTestUtil.noInvolvementAndNoFlags(application);
          when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

          var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
          AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.OGA_EDIT_PUBLIC_NOTICE);
        });
  }

  @Test
  void getAppProcessingPermissions_hasOgaEditPublicNoticePermission_allInvalidAppTypes_caseOfficerPrivilege() {

    PwaApplicationType.stream()
        .filter(appType -> !VALID_PUBLIC_NOTICE_APP_TYPES.contains(appType))
        .forEach(pwaApplicationType -> {

          detail.getPwaApplication().setApplicationType(pwaApplicationType);
          mockUserIsPwaManager();

          var appInvolvement = ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(
              application,
              EnumSet.of(CASE_OFFICER_STAGE_AND_USER_ASSIGNED)
          );
          when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

          var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
          AssertionTestUtils.assertNotEmptyAndDoesNotContain(permissions, PwaAppProcessingPermission.OGA_EDIT_PUBLIC_NOTICE);
        });
  }


  @Test
  void getAppProcessingPermissions_hasDraftPublicNoticePermission_allValidAppTypes_assignedCaseOfficer() {

    PwaApplicationType.stream()
        .filter(appType -> VALID_PUBLIC_NOTICE_APP_TYPES.contains(appType))
        .forEach(pwaApplicationType -> {

          detail.getPwaApplication().setApplicationType(pwaApplicationType);
          mockUserIsCaseOfficer();

          var appInvolvement = ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(
              application,
              EnumSet.of(CASE_OFFICER_STAGE_AND_USER_ASSIGNED)
          );
          when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

          var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
          AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.DRAFT_PUBLIC_NOTICE);
        });
  }


  @Test
  void getAppProcessingPermissions_hasDraftPublicNoticePermission_validAppType_notAssignedCaseOfficer() {

    detail.getPwaApplication().setApplicationType(VALID_PUBLIC_NOTICE_APP_TYPES.iterator().next());
    mockUserIsCaseOfficer();

    var appInvolvement = ApplicationInvolvementDtoTestUtil.noInvolvementAndNoFlags(application);
    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    AssertionTestUtils.assertNotEmptyAndDoesNotContain(permissions, PwaAppProcessingPermission.DRAFT_PUBLIC_NOTICE);

  }

  @Test
  void getAppProcessingPermissions_hasDraftPublicNoticePermission_allInvalidAppTypes_assignedCaseOfficer() {

    PwaApplicationType.stream()
        .filter(appType -> !VALID_PUBLIC_NOTICE_APP_TYPES.contains(appType))
        .forEach(pwaApplicationType -> {

          detail.getPwaApplication().setApplicationType(pwaApplicationType);
          mockUserIsCaseOfficer();

          var appInvolvement = ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(
              application,
              EnumSet.of(CASE_OFFICER_STAGE_AND_USER_ASSIGNED)
          );
          when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

          var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
          AssertionTestUtils.assertNotEmptyAndDoesNotContain(permissions, PwaAppProcessingPermission.DRAFT_PUBLIC_NOTICE);
        });
  }


  @Test
  void getAppProcessingPermissions_hasViewPublicNoticePermission_allInvalidAppTypes_validUserPrivilege() {

    PwaApplicationType.stream()
        .filter(appType -> !VALID_PUBLIC_NOTICE_APP_TYPES.contains(appType))
        .forEach(pwaApplicationType -> {

          detail.getPwaApplication().setApplicationType(pwaApplicationType);

          var appInvolvement = ApplicationInvolvementDtoTestUtil.generatePwaHolderTeamInvolvement(
              application, EnumSet.of(Role.APPLICATION_SUBMITTER));
          when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

          var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
          assertThat(permissions).isNotEmpty();
          assertThat(permissions).doesNotContain(PwaAppProcessingPermission.VIEW_PUBLIC_NOTICE);
        });
  }


  @Test
  void getAppProcessingPermissions_hasViewPublicNoticePermission_allValidAppTypes_invalidUserPrivilege() {

    PwaApplicationType.stream()
        .filter(appType -> VALID_PUBLIC_NOTICE_APP_TYPES.contains(appType))
        .forEach(pwaApplicationType -> {

          detail.getPwaApplication().setApplicationType(pwaApplicationType);
          mockUserIsCaseOfficer();

          var appInvolvement = ApplicationInvolvementDtoTestUtil.generatePwaHolderTeamInvolvement(
              application, EnumSet.of(Role.APPLICATION_SUBMITTER));
          when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

          var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
          assertThat(permissions).isNotEmpty();
          assertThat(permissions).doesNotContain(PwaAppProcessingPermission.VIEW_PUBLIC_NOTICE);
        });
  }

  @Test
  void getAppProcessingPermissions_hasViewPublicNoticePermission_allValidAppTypes_validUserPrivilege() {

    PwaApplicationType.stream()
        .filter(appType -> VALID_PUBLIC_NOTICE_APP_TYPES.contains(appType))
        .forEach(pwaApplicationType -> {
          when(userTypeService.getUserTypes(user)).thenReturn(Set.of(UserType.INDUSTRY));

          detail.getPwaApplication().setApplicationType(pwaApplicationType);

          var appInvolvement = ApplicationInvolvementDtoTestUtil.generatePwaHolderTeamInvolvement(
              application, EnumSet.of(Role.APPLICATION_SUBMITTER));
          when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

          var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
          AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.VIEW_PUBLIC_NOTICE);
        });
  }


  @Test
  void getAppProcessingPermissions_hasUpdatePublicNoticePermission_allInvalidAppTypes_validUserPrivilege() {

    PwaApplicationType.stream()
        .filter(appType -> !VALID_PUBLIC_NOTICE_APP_TYPES.contains(appType))
        .forEach(pwaApplicationType -> {

          detail.getPwaApplication().setApplicationType(pwaApplicationType);

          var appInvolvement = ApplicationInvolvementDtoTestUtil.generatePwaHolderTeamInvolvement(
              application, EnumSet.of(Role.APPLICATION_SUBMITTER));
          when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

          var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
          assertThat(permissions).isNotEmpty();
          assertThat(permissions).doesNotContain(PwaAppProcessingPermission.UPDATE_PUBLIC_NOTICE_DOC);
        });
  }


  @Test
  void getAppProcessingPermissions_hasUpdatePublicNoticePermission_allValidAppTypes_invalidUserPrivilege() {

    PwaApplicationType.stream()
        .filter(appType -> VALID_PUBLIC_NOTICE_APP_TYPES.contains(appType))
        .forEach(pwaApplicationType -> {

          detail.getPwaApplication().setApplicationType(pwaApplicationType);
          mockUserIsCaseOfficer();

          var appInvolvement = ApplicationInvolvementDtoTestUtil.generatePwaHolderTeamInvolvement(
              application, EnumSet.of(Role.APPLICATION_SUBMITTER));
          when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

          var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
          assertThat(permissions).isNotEmpty();
          assertThat(permissions).doesNotContain(PwaAppProcessingPermission.UPDATE_PUBLIC_NOTICE_DOC);
        });
  }


  @Test
  void getAppProcessingPermissions_hasUpdatePublicNoticePermission_allValidAppTypes_validUserPrivilege() {

    PwaApplicationType.stream()
        .filter(appType -> VALID_PUBLIC_NOTICE_APP_TYPES.contains(appType))
        .forEach(pwaApplicationType -> {

          when(userTypeService.getUserTypes(user)).thenReturn(Set.of(UserType.INDUSTRY));

          detail.getPwaApplication().setApplicationType(pwaApplicationType);

          var appInvolvement = ApplicationInvolvementDtoTestUtil.generatePwaHolderTeamInvolvement(
              application, EnumSet.of(Role.APPLICATION_SUBMITTER));
          when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

          var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
          AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.UPDATE_PUBLIC_NOTICE_DOC);
        });
  }


  @Test
  void getAppProcessingPermissions_hasRequestPublicNoticeUpdatePermission_allInvalidAppTypes_caseOfficerPrivilege() {

    PwaApplicationType.stream()
        .filter(appType -> !VALID_PUBLIC_NOTICE_APP_TYPES.contains(appType))
        .forEach(pwaApplicationType -> {

          detail.getPwaApplication().setApplicationType(pwaApplicationType);
          mockUserIsCaseOfficer();

          var appInvolvement = ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(
              application,
              EnumSet.of(CASE_OFFICER_STAGE_AND_USER_ASSIGNED)
          );
          when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

          var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
          AssertionTestUtils.assertNotEmptyAndDoesNotContain(permissions, PwaAppProcessingPermission.REQUEST_PUBLIC_NOTICE_UPDATE);
        });
  }


  @Test
  void getAppProcessingPermissions_hasRequestPublicNoticeUpdatePermission_allValidAppTypes_assignedCaseOfficer() {

    PwaApplicationType.stream()
        .filter(appType -> VALID_PUBLIC_NOTICE_APP_TYPES.contains(appType))
        .forEach(pwaApplicationType -> {

          detail.getPwaApplication().setApplicationType(pwaApplicationType);
          mockUserIsCaseOfficer();

          var appInvolvement = ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(
              application,
              EnumSet.of(CASE_OFFICER_STAGE_AND_USER_ASSIGNED)
          );
          when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

          var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
          AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.REQUEST_PUBLIC_NOTICE_UPDATE);
        });
  }


  @Test
  void getAppProcessingPermissions_hasRequestPublicNoticeUpdatePermission_notAssignedCaseOfficer() {

    detail.getPwaApplication().setApplicationType(VALID_PUBLIC_NOTICE_APP_TYPES.iterator().next());
    mockUserIsCaseOfficer();

    var appInvolvement = ApplicationInvolvementDtoTestUtil.noInvolvementAndNoFlags(application);
    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    AssertionTestUtils.assertNotEmptyAndDoesNotContain(permissions, PwaAppProcessingPermission.REQUEST_PUBLIC_NOTICE_UPDATE);

  }

  @Test
  void getAppProcessingPermissions_hasRequestPublicNoticeUpdatePermission_allInvalidAppTypes_assignedCaseOfficer() {

    PwaApplicationType.stream()
        .filter(appType -> !VALID_PUBLIC_NOTICE_APP_TYPES.contains(appType))
        .forEach(pwaApplicationType -> {

          detail.getPwaApplication().setApplicationType(pwaApplicationType);
          mockUserIsCaseOfficer();

          var appInvolvement = ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(
              application,
              EnumSet.of(CASE_OFFICER_STAGE_AND_USER_ASSIGNED)
          );
          when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

          var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
          AssertionTestUtils.assertNotEmptyAndDoesNotContain(permissions, PwaAppProcessingPermission.REQUEST_PUBLIC_NOTICE_UPDATE);
        });
  }


  @Test
  void getAppProcessingPermissions_hasWithdrawPublicNoticePermission_allInvalidAppTypes_caseOfficerPrivilege() {

    PwaApplicationType.stream()
        .filter(appType -> !VALID_PUBLIC_NOTICE_APP_TYPES.contains(appType))
        .forEach(pwaApplicationType -> {

          detail.getPwaApplication().setApplicationType(pwaApplicationType);
          mockUserIsCaseOfficer();

          var appInvolvement = ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(
              application,
              EnumSet.of(CASE_OFFICER_STAGE_AND_USER_ASSIGNED)
          );
          when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

          var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
          AssertionTestUtils.assertNotEmptyAndDoesNotContain(permissions, PwaAppProcessingPermission.WITHDRAW_PUBLIC_NOTICE);
        });
  }


  @Test
  void getAppProcessingPermissions_hasWithdrawPublicNoticePermission_allValidAppTypes_assignedCaseOfficer() {

    PwaApplicationType.stream()
        .filter(appType -> VALID_PUBLIC_NOTICE_APP_TYPES.contains(appType))
        .forEach(pwaApplicationType -> {

          detail.getPwaApplication().setApplicationType(pwaApplicationType);
          mockUserIsCaseOfficer();

          var appInvolvement = ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(
              application,
              EnumSet.of(CASE_OFFICER_STAGE_AND_USER_ASSIGNED)
          );
          when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

          var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
          AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.WITHDRAW_PUBLIC_NOTICE);
        });
  }


  @Test
  void getAppProcessingPermissions_hasWithdrawPublicNoticePermission_notAssignedCaseOfficer() {

    detail.getPwaApplication().setApplicationType(VALID_PUBLIC_NOTICE_APP_TYPES.iterator().next());
    mockUserIsCaseOfficer();

    var appInvolvement = ApplicationInvolvementDtoTestUtil.noInvolvementAndNoFlags(application);
    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    AssertionTestUtils.assertNotEmptyAndDoesNotContain(permissions, PwaAppProcessingPermission.WITHDRAW_PUBLIC_NOTICE);

  }

  @Test
  void getAppProcessingPermissions_hasWithdrawPublicNoticePermission_allInvalidAppTypes_assignedCaseOfficer() {

    PwaApplicationType.stream()
        .filter(appType -> !VALID_PUBLIC_NOTICE_APP_TYPES.contains(appType))
        .forEach(pwaApplicationType -> {

          detail.getPwaApplication().setApplicationType(pwaApplicationType);
          mockUserIsCaseOfficer();

          var appInvolvement = ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(
              application,
              EnumSet.of(CASE_OFFICER_STAGE_AND_USER_ASSIGNED)
          );
          when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

          var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
          AssertionTestUtils.assertNotEmptyAndDoesNotContain(permissions, PwaAppProcessingPermission.WITHDRAW_PUBLIC_NOTICE);
        });
  }


  @Test
  void getAppProcessingPermissions_hasFinalisePublicNoticePermission_allInvalidAppTypes_caseOfficerPrivilege() {

    PwaApplicationType.stream()
        .filter(appType -> !VALID_PUBLIC_NOTICE_APP_TYPES.contains(appType))
        .forEach(pwaApplicationType -> {

          detail.getPwaApplication().setApplicationType(pwaApplicationType);
          mockUserIsCaseOfficer();

          var appInvolvement = ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(
              application,
              EnumSet.of(CASE_OFFICER_STAGE_AND_USER_ASSIGNED)
          );
          when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

          var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
          AssertionTestUtils.assertNotEmptyAndDoesNotContain(permissions, PwaAppProcessingPermission.FINALISE_PUBLIC_NOTICE);
        });
  }


  @Test
  void getAppProcessingPermissions_hasFinalisePublicNoticePermission_allValidAppTypes_assignedCaseOfficer() {

    PwaApplicationType.stream()
        .filter(appType -> VALID_PUBLIC_NOTICE_APP_TYPES.contains(appType))
        .forEach(pwaApplicationType -> {

          detail.getPwaApplication().setApplicationType(pwaApplicationType);
          mockUserIsCaseOfficer();

          var appInvolvement = ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(
              application,
              EnumSet.of(CASE_OFFICER_STAGE_AND_USER_ASSIGNED)
          );
          when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

          var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
          AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.FINALISE_PUBLIC_NOTICE);
        });
  }

  @Test
  void getAppProcessingPermissions_hasFinalisePublicNoticePermission_notAssignedCaseOfficer() {

    detail.getPwaApplication().setApplicationType(VALID_PUBLIC_NOTICE_APP_TYPES.iterator().next());
    mockUserIsCaseOfficer();

    var appInvolvement = ApplicationInvolvementDtoTestUtil.noInvolvementAndNoFlags(application);
    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    AssertionTestUtils.assertNotEmptyAndDoesNotContain(permissions, PwaAppProcessingPermission.FINALISE_PUBLIC_NOTICE);

  }

  @Test
  void getAppProcessingPermissions_hasFinalisePublicNoticePermission_allInvalidAppTypes_assignedCaseOfficer() {

    PwaApplicationType.stream()
        .filter(appType -> !VALID_PUBLIC_NOTICE_APP_TYPES.contains(appType))
        .forEach(pwaApplicationType -> {

          detail.getPwaApplication().setApplicationType(pwaApplicationType);
          mockUserIsCaseOfficer();

          var appInvolvement = ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(
              application,
              EnumSet.of(CASE_OFFICER_STAGE_AND_USER_ASSIGNED)
          );
          when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

          var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
          AssertionTestUtils.assertNotEmptyAndDoesNotContain(permissions, PwaAppProcessingPermission.FINALISE_PUBLIC_NOTICE);
        });
  }


  @Test
  void getAppProcessingPermissions_payForApplicationPermission_inAwaitingPaymentStatus_holderTeamFinanceRole() {

    detail.setStatus(PwaApplicationStatus.AWAITING_APPLICATION_PAYMENT);

    var appInvolvement = ApplicationInvolvementDtoTestUtil.generatePwaHolderTeamInvolvement(
        application, EnumSet.of(Role.FINANCE_ADMIN)
    );

    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();

    AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.PAY_FOR_APPLICATION);
  }

  @Test
  void getAppProcessingPermissions_payForApplicationPermission_notInAwaitingPaymentStatus_holderTeamFinanceRole() {

    var appInvolvement = ApplicationInvolvementDtoTestUtil.generatePwaHolderTeamInvolvement(
        application, EnumSet.of(Role.FINANCE_ADMIN)
    );

    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();

    AssertionTestUtils.assertNotEmptyAndDoesNotContain(permissions, PwaAppProcessingPermission.PAY_FOR_APPLICATION);
  }

  @Test
  void getAppProcessingPermissions_payForApplicationPermission_inAwaitingPaymentStatus_nonViewerContactTeamRole() {

    detail.setStatus(PwaApplicationStatus.AWAITING_APPLICATION_PAYMENT);

    var appInvolvement = ApplicationInvolvementDtoTestUtil.generatePwaContactInvolvement(
        application, EnumSet.of(PwaContactRole.ACCESS_MANAGER, PwaContactRole.PREPARER)
    );

    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();

    AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.PAY_FOR_APPLICATION);
  }

  @Test
  void getAppProcessingPermissions_payForApplicationPermission_inAwaitingPaymentStatus_ViewerContactTeamRole() {

    detail.setStatus(PwaApplicationStatus.AWAITING_APPLICATION_PAYMENT);

    var appInvolvement = ApplicationInvolvementDtoTestUtil.generatePwaContactInvolvement(
        application, EnumSet.of(PwaContactRole.VIEWER)
    );

    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();

    AssertionTestUtils.assertNotEmptyAndDoesNotContain(permissions, PwaAppProcessingPermission.PAY_FOR_APPLICATION);
  }

  @Test
  void getAppProcessingPermissions_payForApplicationPermission_NotInAwaitingPaymentStatus_validContactTeamRole() {

    detail.setStatus(PwaApplicationStatus.AWAITING_APPLICATION_PAYMENT);

    var appInvolvement = ApplicationInvolvementDtoTestUtil.generatePwaContactInvolvement(
        application, EnumSet.of(PwaContactRole.PREPARER)
    );

    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();

    AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.PAY_FOR_APPLICATION);
  }

  @Test
  void getAppProcessingPermissions_sendConsentForApproval_assignedCaseOfficer() {

    mockUserIsCaseOfficer();
    var appInvolvement = ApplicationInvolvementDtoTestUtil.fromInvolvementFlags(application, Set.of(CASE_OFFICER_STAGE_AND_USER_ASSIGNED));
    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.SEND_CONSENT_FOR_APPROVAL);

  }

  @Test
  void getAppProcessingPermissions_sendConsentForApproval_notAssignedCaseOfficer() {

    mockUserIsCaseOfficer();
    var appInvolvement = ApplicationInvolvementDtoTestUtil.noInvolvementAndNoFlags(application);
    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    AssertionTestUtils.assertNotEmptyAndDoesNotContain(permissions, PwaAppProcessingPermission.SEND_CONSENT_FOR_APPROVAL);

  }

  @Test
  void getAppProcessingPermissions_sendConsentForApproval_notCaseOfficer() {

    mockUserIsPwaManager();

    var appInvolvement = ApplicationInvolvementDtoTestUtil.noInvolvementAndNoFlags(application);
    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    AssertionTestUtils.assertNotEmptyAndDoesNotContain(permissions, PwaAppProcessingPermission.SEND_CONSENT_FOR_APPROVAL);

  }

  @Test
  void getAppProcessingPermissions_manageAppContacts_noRequiredHolderRoles() {

    var appInvolvement = ApplicationInvolvementDtoTestUtil.generatePwaHolderTeamInvolvement(
        application,
        Set.of(Role.FINANCE_ADMIN)
    );
    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    AssertionTestUtils.assertNotEmptyAndDoesNotContain(permissions,
        PwaAppProcessingPermission.MANAGE_APPLICATION_CONTACTS);

  }

  @Test
  void getAppProcessingPermissions_manageAppContacts_hasRequiredHolderRoles() {

    var holderRoles = PwaApplicationPermission.MANAGE_CONTACTS.getHolderTeamRoles();
    for (Role role : holderRoles) {
      var appInvolvement = ApplicationInvolvementDtoTestUtil.generatePwaHolderTeamInvolvement(
          application,
          Set.of(role)
      );
      when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

      var permissions = processingPermissionService.getProcessingPermissionsDto(detail,
          user).getProcessingPermissions();
      AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.MANAGE_APPLICATION_CONTACTS);
    }

  }

  @Test
  void getAppProcessingPermissions_manageAppContacts_hasAppContactAccessManager() {

    var appInvolvement = ApplicationInvolvementDtoTestUtil.generatePwaContactInvolvement(
        application,
        Set.of(PwaContactRole.ACCESS_MANAGER)
    );
    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.MANAGE_APPLICATION_CONTACTS);
  }

  @Test
  void getAppProcessingPermissions_manageAppContacts_AppContactButNotAccessManager() {

    var appContactRoles = EnumSet.complementOf(EnumSet.of(PwaContactRole.ACCESS_MANAGER));

    for (PwaContactRole contactrole: appContactRoles) {
      var appInvolvement = ApplicationInvolvementDtoTestUtil.generatePwaContactInvolvement(
          application,
          Set.of(contactrole)
      );
      when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

      var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
      AssertionTestUtils.assertNotEmptyAndDoesNotContain(permissions, PwaAppProcessingPermission.MANAGE_APPLICATION_CONTACTS);
    }
  }

  @Test
  void getAppProcessingPermissions_manageAppContacts_hasAppContactAccessManager_appIsComplete() {


    detail.setStatus(PwaApplicationStatus.WITHDRAWN);
    var appInvolvement = ApplicationInvolvementDtoTestUtil.generatePwaContactInvolvement(
        application,
        Set.of(PwaContactRole.ACCESS_MANAGER)
    );
    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    AssertionTestUtils.assertNotEmptyAndDoesNotContain(permissions, PwaAppProcessingPermission.MANAGE_APPLICATION_CONTACTS);

  }

  @Test
  void getAppProcessingPermissions_manageAppContacts_hasValidHolderRole_appIsInProgress() {

    for (PwaApplicationStatus appStatus: ApplicationState.IN_PROGRESS.getStatuses()) {
      detail.setStatus(appStatus);
      var appInvolvement = ApplicationInvolvementDtoTestUtil.generatePwaHolderTeamInvolvement(
          application,
          Set.of(Role.APPLICATION_CREATOR)
      );
      when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

      var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
      AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.MANAGE_APPLICATION_CONTACTS);
    }
  }

  @Test
  void getAppProcessingPermissions_ViewAppPaymentDetails_hasHolderRole() {

      var appInvolvement = ApplicationInvolvementDtoTestUtil.generatePwaHolderTeamInvolvement(
          application,
          Set.of(Role.FINANCE_ADMIN)
      );
      when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

      var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
      AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.VIEW_PAYMENT_DETAILS_IF_EXISTS);
  }

  @Test
  void getAppProcessingPermissions_viewAppPaymentDetails_hasAppContactHolderRole() {

    var appInvolvement = ApplicationInvolvementDtoTestUtil.generatePwaContactInvolvement(
        application,
        Set.of(PwaContactRole.VIEWER)
    );

    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaAppProcessingPermission.VIEW_PAYMENT_DETAILS_IF_EXISTS);
  }

  @Test
  void getAppProcessingPermissions_viewAppPaymentDetails_hasNoHolderOrContactRole() {

    var appInvolvement = ApplicationInvolvementDtoTestUtil.noInvolvementAndNoFlags(
        application
    );

    when(applicationInvolvementService.getApplicationInvolvementDto(detail, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissionsDto(detail, user).getProcessingPermissions();
    assertThat(permissions).doesNotContain(PwaAppProcessingPermission.VIEW_PAYMENT_DETAILS_IF_EXISTS);
  }


  private void clearPrivileges(AuthenticatedUserAccount userArg) {
    user = new AuthenticatedUserAccount(userArg, Set.of());
  }

  private ConsultationInvolvementDto getConsultationInvolvement(boolean assignedResponder,
                                                                Set<Role> consulteeRoles) {
    return new ConsultationInvolvementDto(null, consulteeRoles, null, null, assignedResponder);
  }

  private void mockUserIsCaseOfficer() {
    when(hasTeamRoleService.userHasAnyRoleInTeamType(user, TeamType.REGULATOR, Set.of(Role.PWA_MANAGER))).thenReturn(false);
    when(hasTeamRoleService.userHasAnyRoleInTeamType(user, TeamType.REGULATOR, Set.of(Role.CASE_OFFICER))).thenReturn(true);
  }

  private void mockUserIsPwaManager() {
    when(hasTeamRoleService.userHasAnyRoleInTeamType(user, TeamType.REGULATOR, Set.of(Role.PWA_MANAGER))).thenReturn(true);
    when(hasTeamRoleService.userHasAnyRoleInTeamType(user, TeamType.REGULATOR, Set.of(Role.CASE_OFFICER))).thenReturn(false);
  }

}
