package uk.co.ogauthority.pwa.service.appprocessing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ApplicationInvolvementDto;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

@RunWith(MockitoJUnitRunner.class)
public class PwaAppProcessingPermissionServiceTest {

  @Mock
  private ApplicationInvolvementService applicationInvolvementService;

  private PwaAppProcessingPermissionService processingPermissionService;

  private AuthenticatedUserAccount user = new AuthenticatedUserAccount(new WebUserAccount(1), List.of());

  private PwaApplication application;

  @Before
  public void setUp() {

    processingPermissionService = new PwaAppProcessingPermissionService(applicationInvolvementService);

    application = new PwaApplication();

  }

  @Test
  public void getGenericProcessingPermissions_acceptInitialReviewPermission_success() {

    replacePrivileges(user, PwaUserPrivilege.PWA_MANAGER);

    var permissions = processingPermissionService.getGenericProcessingPermissions(user);

    assertThat(permissions).contains(PwaAppProcessingPermission.ACCEPT_INITIAL_REVIEW);

  }

  @Test
  public void getGenericProcessingPermissions_acceptInitialReviewPermission_failed() {

    clearPrivileges(user);

    var permissions = processingPermissionService.getGenericProcessingPermissions(user);

    assertThat(permissions).doesNotContain(PwaAppProcessingPermission.ACCEPT_INITIAL_REVIEW);

  }

  @Test
  public void getGenericProcessingPermissions_acceptAssignCaseOfficerPermission_success() {

    replacePrivileges(user, PwaUserPrivilege.PWA_MANAGER);

    var permissions = processingPermissionService.getGenericProcessingPermissions(user);

    assertThat(permissions).contains(PwaAppProcessingPermission.ASSIGN_CASE_OFFICER);

  }

  @Test
  public void getGenericProcessingPermissions_acceptAssignCaseOfficerPermission_failed() {

    clearPrivileges(user);

    var permissions = processingPermissionService.getGenericProcessingPermissions(user);

    assertThat(permissions).doesNotContain(PwaAppProcessingPermission.ASSIGN_CASE_OFFICER);

  }

  @Test
  public void getGenericProcessingPermissions_hasAddCaseNotePermission_pwaManager() {

    replacePrivileges(user, PwaUserPrivilege.PWA_MANAGER);

    var permissions = processingPermissionService.getGenericProcessingPermissions(user);
    assertThat(permissions).contains(PwaAppProcessingPermission.ADD_CASE_NOTE);

  }

  @Test
  public void getGenericProcessingPermissions_hasAddCaseNotePermission_caseOfficer() {

    replacePrivileges(user, PwaUserPrivilege.PWA_CASE_OFFICER);

    var permissions = processingPermissionService.getGenericProcessingPermissions(user);
    assertThat(permissions).contains(PwaAppProcessingPermission.ADD_CASE_NOTE);

  }

  @Test
  public void getGenericProcessingPermissions_noAddCaseNotePermission() {

    clearPrivileges(user);

    var permissions = processingPermissionService.getGenericProcessingPermissions(user);
    assertThat(permissions).doesNotContain(PwaAppProcessingPermission.ADD_CASE_NOTE);

  }

  @Test
  public void getGenericProcessingPermissions_acceptViewAllConsultationsPermission_co_success() {

    replacePrivileges(user, PwaUserPrivilege.PWA_CASE_OFFICER);

    var permissions = processingPermissionService.getGenericProcessingPermissions(user);
    assertThat(permissions).contains(PwaAppProcessingPermission.VIEW_ALL_CONSULTATIONS);

  }

  @Test
  public void getGenericProcessingPermissions_acceptViewAllConsultationsPermission_pwaManager_success() {

    replacePrivileges(user, PwaUserPrivilege.PWA_MANAGER);

    var permissions = processingPermissionService.getGenericProcessingPermissions(user);
    assertThat(permissions).contains(PwaAppProcessingPermission.VIEW_ALL_CONSULTATIONS);

  }

  @Test
  public void getGenericProcessingPermissions_acceptViewAllConsultationsPermission_notCoOrPwaManagerfailed() {

    clearPrivileges(user);

    var permissions = processingPermissionService.getGenericProcessingPermissions(user);

    assertThat(permissions).doesNotContain(PwaAppProcessingPermission.VIEW_ALL_CONSULTATIONS);

  }

  @Test
  public void getAppProcessingPermissions_acceptWithdrawConsultationsPermission_assignedCo_success() {

    replacePrivileges(user, PwaUserPrivilege.PWA_CASE_OFFICER);

    var appInvolvement = new ApplicationInvolvementDto(application, Set.of(), false, Set.of(), true);
    when(applicationInvolvementService.getApplicationInvolvementDto(application, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissions(application, user);
    assertThat(permissions).contains(PwaAppProcessingPermission.WITHDRAW_CONSULTATION);
  }

  @Test
  public void getAppProcessingPermissions_acceptWithdrawConsultationsPermission_notAssignedCo_failed() {

    replacePrivileges(user, PwaUserPrivilege.PWA_CASE_OFFICER);

    var appInvolvement = new ApplicationInvolvementDto(application, Set.of(), false, Set.of(), false);
    when(applicationInvolvementService.getApplicationInvolvementDto(application, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissions(application, user);
    assertThat(permissions).doesNotContain(PwaAppProcessingPermission.WITHDRAW_CONSULTATION);
  }

  @Test
  public void getAppProcessingPermissions_acceptWithdrawConsultationsPermission_notCo_failed() {

    clearPrivileges(user);

    var appInvolvement = new ApplicationInvolvementDto(application, Set.of(), false, Set.of(), false);
    when(applicationInvolvementService.getApplicationInvolvementDto(application, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissions(application, user);
    assertThat(permissions).doesNotContain(PwaAppProcessingPermission.WITHDRAW_CONSULTATION);
  }


  @Test
  public void getAppProcessingPermissions_hasAssignResponderPermission_recipient() {

    replacePrivileges(user, PwaUserPrivilege.PWA_CONSULTEE);

    var appInvolvement = new ApplicationInvolvementDto(application, Set.of(), false, Set.of(ConsulteeGroupMemberRole.RECIPIENT), false);
    when(applicationInvolvementService.getApplicationInvolvementDto(application, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissions(application, user);
    assertThat(permissions).contains(PwaAppProcessingPermission.ASSIGN_RESPONDER);

  }

  @Test
  public void getAppProcessingPermissions_hasAssignResponderPermission_responder() {

    replacePrivileges(user, PwaUserPrivilege.PWA_CONSULTEE);

    var appInvolvement = new ApplicationInvolvementDto(application, Set.of(), false, Set.of(ConsulteeGroupMemberRole.RESPONDER), false);
    when(applicationInvolvementService.getApplicationInvolvementDto(application, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissions(application, user);
    assertThat(permissions).contains(PwaAppProcessingPermission.ASSIGN_RESPONDER);

  }

  @Test
  public void getAppProcessingPermissions_noAssignResponderPermission() {

    replacePrivileges(user, PwaUserPrivilege.PWA_CONSULTEE);

    var appInvolvement = new ApplicationInvolvementDto(application, Set.of(), false, Set.of(ConsulteeGroupMemberRole.ACCESS_MANAGER), false);
    when(applicationInvolvementService.getApplicationInvolvementDto(application, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissions(application, user);
    assertThat(permissions).doesNotContain(PwaAppProcessingPermission.ASSIGN_RESPONDER);

  }

  @Test
  public void getAppProcessingPermissions_hasConsultationResponderPermission_ifAssignedAsResponder() {

    replacePrivileges(user, PwaUserPrivilege.PWA_CONSULTEE);

    var appInvolvement = new ApplicationInvolvementDto(application, Set.of(), true, Set.of(ConsulteeGroupMemberRole.RESPONDER), false);
    when(applicationInvolvementService.getApplicationInvolvementDto(application, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissions(application, user);
    assertThat(permissions).contains(PwaAppProcessingPermission.CONSULTATION_RESPONDER);
  }

  @Test
  public void getAppProcessingPermissions_noConsultationResponderPermission_ifNotAssignedAsResponder() {

    replacePrivileges(user, PwaUserPrivilege.PWA_CONSULTEE);

    var appInvolvement = new ApplicationInvolvementDto(application, Set.of(), false, Set.of(ConsulteeGroupMemberRole.RESPONDER), false);
    when(applicationInvolvementService.getApplicationInvolvementDto(application, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissions(application, user);
    assertThat(permissions).doesNotContain(PwaAppProcessingPermission.CONSULTATION_RESPONDER);
  }

  @Test
  public void getAppProcessingPermissions_noConsultationResponderPermission() {

    replacePrivileges(user, PwaUserPrivilege.PWA_CONSULTEE);

    var appInvolvement = new ApplicationInvolvementDto(application, Set.of(), false, Set.of(ConsulteeGroupMemberRole.RECIPIENT, ConsulteeGroupMemberRole.ACCESS_MANAGER), false);
    when(applicationInvolvementService.getApplicationInvolvementDto(application, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissions(application, user);
    assertThat(permissions).doesNotContain(PwaAppProcessingPermission.CONSULTATION_RESPONDER);
  }

  @Test
  public void getAppProcessingPermissions_hasEditConsentDocumentPermission_assignedCaseOfficer() {

    replacePrivileges(user, PwaUserPrivilege.PWA_CASE_OFFICER);

    var appInvolvement = new ApplicationInvolvementDto(application, Set.of(), false, Set.of(), true);
    when(applicationInvolvementService.getApplicationInvolvementDto(application, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissions(application, user);
    assertThat(permissions).contains(PwaAppProcessingPermission.EDIT_CONSENT_DOCUMENT);

  }

  @Test
  public void getAppProcessingPermissions_hasEditConsentDocumentPermission_notAssignedCaseOfficer() {

    replacePrivileges(user, PwaUserPrivilege.PWA_CASE_OFFICER);

    var appInvolvement = new ApplicationInvolvementDto(application, Set.of(), false, Set.of(), false);
    when(applicationInvolvementService.getApplicationInvolvementDto(application, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissions(application, user);
    assertThat(permissions).doesNotContain(PwaAppProcessingPermission.EDIT_CONSENT_DOCUMENT);

  }

  @Test
  public void getAppProcessingPermissions_noEditConsentDocumentPermission_notCaseOfficer() {

    clearPrivileges(user);

    var appInvolvement = new ApplicationInvolvementDto(application, Set.of(), false, Set.of(), false);
    when(applicationInvolvementService.getApplicationInvolvementDto(application, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissions(application, user);
    assertThat(permissions).doesNotContain(PwaAppProcessingPermission.EDIT_CONSENT_DOCUMENT);

  }

  @Test
  public void getAppPermissions_hasUpdateApplicationPermission_isContact_Preparer() {

    var appInvolvement = new ApplicationInvolvementDto(application, Set.of(PwaContactRole.PREPARER), false, Set.of(), false);
    when(applicationInvolvementService.getApplicationInvolvementDto(application, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissions(application, user);

    assertThat(permissions).contains(PwaAppProcessingPermission.UPDATE_APPLICATION);

  }

  @Test
  public void getAppPermissions_noUpdateApplicationPermission_isContact_notPreparer() {

    var appInvolvement = new ApplicationInvolvementDto(application, Set.of(PwaContactRole.VIEWER), false, Set.of(), false);
    when(applicationInvolvementService.getApplicationInvolvementDto(application, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissions(application, user);

    assertThat(permissions).doesNotContain(PwaAppProcessingPermission.UPDATE_APPLICATION);

  }

  @Test
  public void getAppPermissions_noUpdateApplicationPermission_notContact() {

    var appInvolvement = new ApplicationInvolvementDto(application, Set.of(), false, Set.of(), false);
    when(applicationInvolvementService.getApplicationInvolvementDto(application, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissions(application, user);

    assertThat(permissions).doesNotContain(PwaAppProcessingPermission.UPDATE_APPLICATION);

  }

  @Test
  public void getAppPermissions_hasCaseManagementIndustryPermission_andViewApplicationSummary() {

    var appInvolvement = new ApplicationInvolvementDto(application, Set.of(PwaContactRole.PREPARER), false, Set.of(), false);
    when(applicationInvolvementService.getApplicationInvolvementDto(application, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissions(application, user);
    assertThat(permissions).contains(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY);
    assertThat(permissions).contains(PwaAppProcessingPermission.VIEW_APPLICATION_SUMMARY);

  }

  @Test
  public void getAppPermissions_industryUser_andOptionsAppType() {
    application.setApplicationType(PwaApplicationType.OPTIONS_VARIATION);
    var appInvolvement = new ApplicationInvolvementDto(application, Set.of(PwaContactRole.PREPARER), false, Set.of(), false);
    when(applicationInvolvementService.getApplicationInvolvementDto(application, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissions(application, user);
    assertThat(permissions).contains(PwaAppProcessingPermission.APPROVE_OPTIONS_VIEW);
    assertThat(permissions).doesNotContain(PwaAppProcessingPermission.APPROVE_OPTIONS);
  }

  @Test
  public void getAppPermissions_regualtorUser_andOptionsAppType() {
    application.setApplicationType(PwaApplicationType.OPTIONS_VARIATION);
    var appInvolvement = new ApplicationInvolvementDto(application, Set.of(PwaContactRole.PREPARER), false, Set.of(), false);
    when(applicationInvolvementService.getApplicationInvolvementDto(application, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissions(application, user);
    assertThat(permissions).doesNotContain(PwaAppProcessingPermission.APPROVE_OPTIONS_VIEW);
    assertThat(permissions).contains(PwaAppProcessingPermission.APPROVE_OPTIONS);
  }

  @Test
  public void getAppPermissions_industryUser_andNotOptionsAppType() {
    application.setApplicationType(PwaApplicationType.INITIAL);
    var appInvolvement = new ApplicationInvolvementDto(application, Set.of(PwaContactRole.PREPARER), false, Set.of(), false);
    when(applicationInvolvementService.getApplicationInvolvementDto(application, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissions(application, user);
    assertThat(permissions).doesNotContain(PwaAppProcessingPermission.APPROVE_OPTIONS_VIEW);
    assertThat(permissions).doesNotContain(PwaAppProcessingPermission.APPROVE_OPTIONS);
  }

  @Test
  public void getAppPermissions_regualtorUser_andNotOptionsAppType() {
    application.setApplicationType(PwaApplicationType.INITIAL);
    var appInvolvement = new ApplicationInvolvementDto(application, Set.of(PwaContactRole.PREPARER), false, Set.of(), false);
    when(applicationInvolvementService.getApplicationInvolvementDto(application, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissions(application, user);
    assertThat(permissions).doesNotContain(PwaAppProcessingPermission.APPROVE_OPTIONS_VIEW);
    assertThat(permissions).doesNotContain(PwaAppProcessingPermission.APPROVE_OPTIONS);
  }

  @Test
  public void getAppPermissions_noCaseManagementIndustryPermission_andNoViewApplicationSummary() {

    var appInvolvement = new ApplicationInvolvementDto(application, Set.of(), false, Set.of(), false);
    when(applicationInvolvementService.getApplicationInvolvementDto(application, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissions(application, user);
    assertThat(permissions).doesNotContain(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY);
    assertThat(permissions).doesNotContain(PwaAppProcessingPermission.VIEW_APPLICATION_SUMMARY);

  }

  @Test
  public void getAppPermissions_hasCaseManagementOgaPermission_andViewApplicationSummary() {

    replacePrivileges(user, PwaUserPrivilege.PWA_REGULATOR);

    var appInvolvement = new ApplicationInvolvementDto(application, Set.of(), false, Set.of(), false);
    when(applicationInvolvementService.getApplicationInvolvementDto(application, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissions(application, user);
    assertThat(permissions).contains(PwaAppProcessingPermission.CASE_MANAGEMENT_OGA);
    assertThat(permissions).contains(PwaAppProcessingPermission.VIEW_APPLICATION_SUMMARY);

  }

  @Test
  public void getAppPermissions_noCaseManagementOgaPermission_andNoViewApplicationSummary() {

    clearPrivileges(user);

    var appInvolvement = new ApplicationInvolvementDto(application, Set.of(), false, Set.of(), false);
    when(applicationInvolvementService.getApplicationInvolvementDto(application, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissions(application, user);
    assertThat(permissions).doesNotContain(PwaAppProcessingPermission.CASE_MANAGEMENT_OGA);
    assertThat(permissions).doesNotContain(PwaAppProcessingPermission.VIEW_APPLICATION_SUMMARY);

  }

  @Test
  public void getAppPermissions_hasCaseManagementConsulteePermission_andViewApplicationSummary() {

    var appInvolvement = new ApplicationInvolvementDto(application, Set.of(), false, Set.of(ConsulteeGroupMemberRole.RESPONDER), false);
    when(applicationInvolvementService.getApplicationInvolvementDto(application, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissions(application, user);
    assertThat(permissions).contains(PwaAppProcessingPermission.CASE_MANAGEMENT_CONSULTEE);
    assertThat(permissions).contains(PwaAppProcessingPermission.VIEW_APPLICATION_SUMMARY);

  }

  @Test
  public void getAppPermissions_noCaseManagementConsulteePermission_andnoViewApplicationSummary() {

    var appInvolvement = new ApplicationInvolvementDto(application, Set.of(), false, Set.of(), false);
    when(applicationInvolvementService.getApplicationInvolvementDto(application, user)).thenReturn(appInvolvement);

    var permissions = processingPermissionService.getProcessingPermissions(application, user);
    assertThat(permissions).doesNotContain(PwaAppProcessingPermission.CASE_MANAGEMENT_CONSULTEE);
    assertThat(permissions).doesNotContain(PwaAppProcessingPermission.VIEW_APPLICATION_SUMMARY);

  }

  private void clearPrivileges(AuthenticatedUserAccount userArg) {
    user = new AuthenticatedUserAccount(userArg, Set.of());
  }

  private void replacePrivileges(AuthenticatedUserAccount userArg, PwaUserPrivilege... privileges) {
    user = new AuthenticatedUserAccount(userArg, Arrays.stream(privileges).collect(Collectors.toSet()));
  }

}
