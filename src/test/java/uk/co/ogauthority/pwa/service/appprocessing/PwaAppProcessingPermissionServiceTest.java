package uk.co.ogauthority.pwa.service.appprocessing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupTeamMember;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.teams.PwaRole;
import uk.co.ogauthority.pwa.model.teams.PwaTeamMember;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;
import uk.co.ogauthority.pwa.service.teams.TeamService;
import uk.co.ogauthority.pwa.testutils.ConsulteeGroupTestingUtils;

@RunWith(MockitoJUnitRunner.class)
public class PwaAppProcessingPermissionServiceTest {

  @Mock
  private TeamService teamService;

  @Mock
  private ConsulteeGroupTeamService consulteeGroupTeamService;

  @Mock
  private PwaContactService pwaContactService;

  @Mock
  private ConsultationRequestService consultationRequestService;

  private PwaAppProcessingPermissionService processingPermissionService;

  private AuthenticatedUserAccount user = new AuthenticatedUserAccount(new WebUserAccount(1), List.of());
  private PwaTeamMember regTeamMember;

  private PwaApplication application;

  @Before
  public void setUp() {

    processingPermissionService = new PwaAppProcessingPermissionService(teamService, consulteeGroupTeamService, pwaContactService, consultationRequestService);

    regTeamMember = new PwaTeamMember(null, user.getLinkedPerson(), Set.of(new PwaRole("PWA_MANAGER", "Pwa Manager", null, 10)));
    when(teamService.getMembershipOfPersonInTeam(teamService.getRegulatorTeam(), user.getLinkedPerson())).thenReturn(Optional.of(regTeamMember));

    application = new PwaApplication();

  }

  @Test
  public void getGenericProcessingPermissions_acceptInitialReviewPermission_success() {

    var permissions = processingPermissionService.getGenericProcessingPermissions(user);

    assertThat(permissions).contains(PwaAppProcessingPermission.ACCEPT_INITIAL_REVIEW);

  }

  @Test
  public void getGenericProcessingPermissions_acceptInitialReviewPermission_failed() {

    regTeamMember = new PwaTeamMember(null, user.getLinkedPerson(), Set.of(new PwaRole("ORGANISATION_MANAGER", "Org Manager", null, 10)));
    when(teamService.getMembershipOfPersonInTeam(teamService.getRegulatorTeam(), user.getLinkedPerson())).thenReturn(Optional.of(regTeamMember));

    var permissions = processingPermissionService.getGenericProcessingPermissions(user);

    assertThat(permissions).doesNotContain(PwaAppProcessingPermission.ACCEPT_INITIAL_REVIEW);

  }

  @Test
  public void getGenericProcessingPermissions_acceptAssignCaseOfficerPermission_success() {

    var permissions = processingPermissionService.getGenericProcessingPermissions(user);

    assertThat(permissions).contains(PwaAppProcessingPermission.ASSIGN_CASE_OFFICER);

  }

  @Test
  public void getGenericProcessingPermissions_acceptAssignCaseOfficerPermission_failed() {

    regTeamMember = new PwaTeamMember(null, user.getLinkedPerson(), Set.of(new PwaRole("ORGANISATION_MANAGER", "Org Manager", null, 10)));
    when(teamService.getMembershipOfPersonInTeam(teamService.getRegulatorTeam(), user.getLinkedPerson())).thenReturn(Optional.of(regTeamMember));

    var permissions = processingPermissionService.getGenericProcessingPermissions(user);

    assertThat(permissions).doesNotContain(PwaAppProcessingPermission.ASSIGN_CASE_OFFICER);

  }

  @Test
  public void getGenericProcessingPermissions_acceptWithdrawConsultationsPermission_success() {

    regTeamMember = new PwaTeamMember(null, user.getLinkedPerson(), Set.of(new PwaRole("CASE_OFFICER", "Case Officer", null, 10)));
    when(teamService.getMembershipOfPersonInTeam(teamService.getRegulatorTeam(), user.getLinkedPerson())).thenReturn(Optional.of(regTeamMember));

    var permissions = processingPermissionService.getGenericProcessingPermissions(user);
    assertThat(permissions).contains(PwaAppProcessingPermission.WITHDRAW_CONSULTATION);
  }

  @Test
  public void getGenericProcessingPermissions_acceptWithdrawConsultationsPermission_failed() {

    regTeamMember = new PwaTeamMember(null, user.getLinkedPerson(), Set.of(new PwaRole("ORGANISATION_MANAGER", "Org Manager", null, 10)));
    when(teamService.getMembershipOfPersonInTeam(teamService.getRegulatorTeam(), user.getLinkedPerson())).thenReturn(Optional.of(regTeamMember));

    var permissions = processingPermissionService.getGenericProcessingPermissions(user);
    assertThat(permissions).doesNotContain(PwaAppProcessingPermission.WITHDRAW_CONSULTATION);
  }

  @Test
  public void getGenericProcessingPermissions_acceptViewAllConsultationsPermission_success() {

    var permissions = processingPermissionService.getGenericProcessingPermissions(user);
    assertThat(permissions).contains(PwaAppProcessingPermission.VIEW_ALL_CONSULTATIONS);

  }

  @Test
  public void getGenericProcessingPermissions_acceptViewAllConsultationsPermission_failed() {

    regTeamMember = new PwaTeamMember(null, user.getLinkedPerson(), Set.of(new PwaRole("ORGANISATION_MANAGER", "Org Manager", null, 10)));
    when(teamService.getMembershipOfPersonInTeam(teamService.getRegulatorTeam(), user.getLinkedPerson())).thenReturn(Optional.of(regTeamMember));

    var permissions = processingPermissionService.getGenericProcessingPermissions(user);

    assertThat(permissions).doesNotContain(PwaAppProcessingPermission.VIEW_ALL_CONSULTATIONS);

  }


  @Test
  public void getGenericProcessingPermissions_hasAssignResponderPermission() {

    when(teamService.getMembershipOfPersonInTeam(teamService.getRegulatorTeam(), user.getLinkedPerson())).thenReturn(Optional.empty());

    var consulteeGroupTeamMember = new ConsulteeGroupTeamMember();
    consulteeGroupTeamMember.setRoles(Set.of(ConsulteeGroupMemberRole.RESPONDER));
    when(consulteeGroupTeamService.getTeamMembersByPerson(user.getLinkedPerson())).thenReturn(List.of(consulteeGroupTeamMember));

    var permissions = processingPermissionService.getGenericProcessingPermissions(user);
    assertThat(permissions).contains(PwaAppProcessingPermission.ASSIGN_RESPONDER);

  }


  @Test
  public void getGenericProcessingPermissions_noAssignResponderPermission() {

    when(teamService.getMembershipOfPersonInTeam(teamService.getRegulatorTeam(), user.getLinkedPerson())).thenReturn(Optional.empty());

    var consulteeGroupTeamMember = new ConsulteeGroupTeamMember();
    consulteeGroupTeamMember.setRoles(Set.of(ConsulteeGroupMemberRole.ACCESS_MANAGER));
    when(consulteeGroupTeamService.getTeamMembersByPerson(user.getLinkedPerson())).thenReturn(List.of(consulteeGroupTeamMember));

    var permissions = processingPermissionService.getGenericProcessingPermissions(user);
    assertThat(permissions).doesNotContain(PwaAppProcessingPermission.ASSIGN_RESPONDER);

  }


  @Test
  public void getGenericProcessingPermissions_hasConsultationResponderPermission() {
    var consulteeGroupTeamMember = new ConsulteeGroupTeamMember();
    consulteeGroupTeamMember.setRoles(Set.of(ConsulteeGroupMemberRole.RESPONDER));
    when(consulteeGroupTeamService.getTeamMembersByPerson(user.getLinkedPerson())).thenReturn(List.of(consulteeGroupTeamMember));

    var permissions = processingPermissionService.getGenericProcessingPermissions(user);
    assertThat(permissions).contains(PwaAppProcessingPermission.CONSULTATION_RESPONDER);
  }


  @Test
  public void getGenericProcessingPermissions_noConsultationResponderPermission() {
    var consulteeGroupTeamMember = new ConsulteeGroupTeamMember();
    consulteeGroupTeamMember.setRoles(Set.of(ConsulteeGroupMemberRole.RECIPIENT));
    when(consulteeGroupTeamService.getTeamMembersByPerson(user.getLinkedPerson())).thenReturn(List.of(consulteeGroupTeamMember));

    var permissions = processingPermissionService.getGenericProcessingPermissions(user);
    assertThat(permissions).doesNotContain(PwaAppProcessingPermission.CONSULTATION_RESPONDER);
  }

  @Test
  public void getGenericProcessingPermissions_hasAddCaseNotePermission_pwaManager() {

    var permissions = processingPermissionService.getGenericProcessingPermissions(user);
    assertThat(permissions).contains(PwaAppProcessingPermission.ADD_CASE_NOTE);

  }

  @Test
  public void getGenericProcessingPermissions_hasAddCaseNotePermission_caseOfficer() {

    regTeamMember = new PwaTeamMember(null, user.getLinkedPerson(), Set.of(new PwaRole("CASE_OFFICER", "Case officer", null, 10)));
    when(teamService.getMembershipOfPersonInTeam(teamService.getRegulatorTeam(), user.getLinkedPerson())).thenReturn(Optional.of(regTeamMember));

    var permissions = processingPermissionService.getGenericProcessingPermissions(user);
    assertThat(permissions).contains(PwaAppProcessingPermission.ADD_CASE_NOTE);

  }

  @Test
  public void getGenericProcessingPermissions_noAddCaseNotePermission() {

    when(teamService.getMembershipOfPersonInTeam(teamService.getRegulatorTeam(), user.getLinkedPerson())).thenReturn(Optional.empty());

    var permissions = processingPermissionService.getGenericProcessingPermissions(user);
    assertThat(permissions).doesNotContain(PwaAppProcessingPermission.ADD_CASE_NOTE);

  }

  @Test
  public void getGenericProcessingPermissions_hasEditConsentDocumentPermission_caseOfficer() {

    regTeamMember = new PwaTeamMember(null, user.getLinkedPerson(), Set.of(new PwaRole("CASE_OFFICER", "Case officer", null, 10)));
    when(teamService.getMembershipOfPersonInTeam(teamService.getRegulatorTeam(), user.getLinkedPerson())).thenReturn(Optional.of(regTeamMember));

    var permissions = processingPermissionService.getGenericProcessingPermissions(user);
    assertThat(permissions).contains(PwaAppProcessingPermission.EDIT_CONSENT_DOCUMENT);

  }

  @Test
  public void getGenericProcessingPermissions_noEditConsentDocumentPermission() {

    when(teamService.getMembershipOfPersonInTeam(teamService.getRegulatorTeam(), user.getLinkedPerson())).thenReturn(Optional.empty());

    var permissions = processingPermissionService.getGenericProcessingPermissions(user);
    assertThat(permissions).doesNotContain(PwaAppProcessingPermission.EDIT_CONSENT_DOCUMENT);

  }

  @Test
  public void getAppPermissions_hasUpdateApplicationPermission_isContact_Preparer() {

    when(pwaContactService.getContactRoles(application, user.getLinkedPerson())).thenReturn(Set.of(PwaContactRole.PREPARER));

    var permissions = processingPermissionService.getProcessingPermissions(application, user);

    assertThat(permissions).contains(PwaAppProcessingPermission.UPDATE_APPLICATION);

  }

  @Test
  public void getAppPermissions_noUpdateApplicationPermission_isContact_notPreparer() {

    when(pwaContactService.getContactRoles(application, user.getLinkedPerson())).thenReturn(Set.of(PwaContactRole.VIEWER));

    var permissions = processingPermissionService.getProcessingPermissions(application, user);

    assertThat(permissions).doesNotContain(PwaAppProcessingPermission.UPDATE_APPLICATION);

  }

  @Test
  public void getAppPermissions_noUpdateApplicationPermission_notContact() {

    when(pwaContactService.getContactRoles(application, user.getLinkedPerson())).thenReturn(Set.of());

    var permissions = processingPermissionService.getProcessingPermissions(application, user);

    assertThat(permissions).doesNotContain(PwaAppProcessingPermission.UPDATE_APPLICATION);

  }

  @Test
  public void getAppPermissions_hasCaseManagementIndustryPermission_andViewApplicationSummary() {

    when(pwaContactService.getContactRoles(application, user.getLinkedPerson()))
        .thenReturn(Set.of(PwaContactRole.PREPARER));

    var permissions = processingPermissionService.getProcessingPermissions(application, user);
    assertThat(permissions).contains(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY);
    assertThat(permissions).contains(PwaAppProcessingPermission.VIEW_APPLICATION_SUMMARY);

  }

  @Test
  public void getAppPermissions_noCaseManagementIndustryPermission_andNoViewApplicationSummary() {

    when(pwaContactService.getContactRoles(application, user.getLinkedPerson()))
        .thenReturn(Set.of());

    var permissions = processingPermissionService.getProcessingPermissions(application, user);
    assertThat(permissions).doesNotContain(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY);
    assertThat(permissions).doesNotContain(PwaAppProcessingPermission.VIEW_APPLICATION_SUMMARY);

  }

  @Test
  public void getAppPermissions_hasCaseManagementOgaPermission_andViewApplicationSummary() {

    user = new AuthenticatedUserAccount(new WebUserAccount(1), List.of(PwaUserPrivilege.PWA_REGULATOR));

    var permissions = processingPermissionService.getProcessingPermissions(application, user);
    assertThat(permissions).contains(PwaAppProcessingPermission.CASE_MANAGEMENT_OGA);
    assertThat(permissions).contains(PwaAppProcessingPermission.VIEW_APPLICATION_SUMMARY);

  }

  @Test
  public void getAppPermissions_noCaseManagementOgaPermission_andNoViewApplicationSummary() {

    when(pwaContactService.getContactRoles(application, user.getLinkedPerson()))
        .thenReturn(Set.of());

    var permissions = processingPermissionService.getProcessingPermissions(application, user);
    assertThat(permissions).doesNotContain(PwaAppProcessingPermission.CASE_MANAGEMENT_OGA);
    assertThat(permissions).doesNotContain(PwaAppProcessingPermission.VIEW_APPLICATION_SUMMARY);

  }

  @Test
  public void getAppPermissions_hasCaseManagementConsulteePermission_andViewApplicationSummary() {

    var groupDetail = ConsulteeGroupTestingUtils.createConsulteeGroup("name", "abb");
    when(consulteeGroupTeamService.getTeamMembersByPerson(user.getLinkedPerson())).thenReturn(List.of(
        new ConsulteeGroupTeamMember(groupDetail.getConsulteeGroup(), user.getLinkedPerson(), Set.of())
    ));

    var request = new ConsultationRequest();
    request.setConsulteeGroup(groupDetail.getConsulteeGroup());
    when(consultationRequestService.getAllRequestsByApplication(application)).thenReturn(List.of(request));

    var permissions = processingPermissionService.getProcessingPermissions(application, user);
    assertThat(permissions).contains(PwaAppProcessingPermission.CASE_MANAGEMENT_CONSULTEE);
    assertThat(permissions).contains(PwaAppProcessingPermission.VIEW_APPLICATION_SUMMARY);

  }

  @Test
  public void getAppPermissions_noCaseManagementConsulteePermission_andnoViewApplicationSummary() {

    var permissions = processingPermissionService.getProcessingPermissions(application, user);
    assertThat(permissions).doesNotContain(PwaAppProcessingPermission.CASE_MANAGEMENT_CONSULTEE);
    assertThat(permissions).doesNotContain(PwaAppProcessingPermission.VIEW_APPLICATION_SUMMARY);

  }

}
