package uk.co.ogauthority.pwa.controller.appprocessing.prepareconsent;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaAppProcessingContextAbstractControllerTest;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonService;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ProcessingPermissionsDto;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.workflow.assignment.Assignment;
import uk.co.ogauthority.pwa.model.enums.consultations.ConsultationResponseDocumentType;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.service.appprocessing.appprocessingwarning.AppProcessingTaskWarningService;
import uk.co.ogauthority.pwa.service.appprocessing.appprocessingwarning.AppProcessingTaskWarningTestUtil;
import uk.co.ogauthority.pwa.service.appprocessing.consentreview.ConsentReviewReturnFormValidator;
import uk.co.ogauthority.pwa.service.appprocessing.consentreview.ConsentReviewService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.service.appprocessing.prepareconsent.ConsentFileViewerService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.workflow.assignment.WorkflowAssignment;
import uk.co.ogauthority.pwa.service.teams.PwaTeamService;
import uk.co.ogauthority.pwa.service.workflow.assignment.AssignmentService;
import uk.co.ogauthority.pwa.testutils.ControllerTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaAppProcessingContextDtoTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = ConsentReviewController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {PwaAppProcessingContextService.class}))
public class ConsentReviewControllerTest extends PwaAppProcessingContextAbstractControllerTest {

  @MockBean
  private PwaAppProcessingPermissionService pwaAppProcessingPermissionService;

  @MockBean
  private ConsentReviewService consentReviewService;

  @MockBean
  private PwaTeamService pwaTeamService;

  @MockBean
  private AssignmentService assignmentService;

  @MockBean
  private PersonService personService;

  @MockBean
  private ConsentReviewReturnFormValidator consentReviewReturnFormValidator;

  @MockBean
  private AppProcessingTaskWarningService appProcessingTaskWarningService;

  @MockBean
  private ConsentFileViewerService consentFileViewerService;

  private PwaApplicationEndpointTestBuilder endpointTester;

  private AuthenticatedUserAccount user;
  private Person caseOfficerPerson = PersonTestUtil.createDefaultPerson();

  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setUp() {

    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationDetailService, pwaAppProcessingPermissionService)
        .setAllowedStatuses(PwaApplicationStatus.CONSENT_REVIEW)
        .setAllowedProcessingPermissions(PwaAppProcessingPermission.CONSENT_REVIEW);

    user = new AuthenticatedUserAccount(
        new WebUserAccount(1),
        EnumSet.allOf(PwaUserPrivilege.class));

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(1);
    pwaApplicationDetail.setStatus(PwaApplicationStatus.CONSENT_REVIEW);

    when(pwaApplicationDetailService.getLatestDetailForUser(pwaApplicationDetail.getMasterPwaApplicationId(), user))
        .thenReturn(Optional.of(pwaApplicationDetail));

    var permissionsDto = new ProcessingPermissionsDto(PwaAppProcessingContextDtoTestUtils.appInvolvementSatisfactoryVersions(
        pwaApplicationDetail.getPwaApplication()), EnumSet.allOf(PwaAppProcessingPermission.class));

    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user)).thenReturn(permissionsDto);

    when(personService.getPersonById(new PersonId(2))).thenReturn(caseOfficerPerson);

    when(appProcessingTaskWarningService.getNonBlockingTasksWarning(any(), any()))
        .thenReturn(AppProcessingTaskWarningTestUtil.createWithNoWarning());

    when(consentFileViewerService.getLatestConsultationRequestViewForDocumentType(
        pwaApplicationDetail.getPwaApplication(), ConsultationResponseDocumentType.SECRETARY_OF_STATE_DECISION)).thenReturn(Optional.empty());

  }

  @Test
  public void renderReturnToCaseOfficer_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ConsentReviewController.class)
                .renderReturnToCaseOfficer(applicationDetail.getMasterPwaApplicationId(), type, null, null, null)));

    endpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderReturnToCaseOfficer_statusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ConsentReviewController.class)
                .renderReturnToCaseOfficer(applicationDetail.getMasterPwaApplicationId(), type, null, null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void renderReturnToCaseOfficer_caseOfficerPrefilled() throws Exception {

    var caseOfficerPerson = PersonTestUtil.createPersonFrom(new PersonId(2));
    var assignment = new Assignment(
        pwaApplicationDetail.getPwaApplication().getId(),
        pwaApplicationDetail.getPwaApplication().getWorkflowType(),
        WorkflowAssignment.CASE_OFFICER,
        caseOfficerPerson.getId());

    when(pwaTeamService.getPeopleWithRegulatorRole(PwaRegulatorRole.CASE_OFFICER))
        .thenReturn(Set.of(caseOfficerPerson, PersonTestUtil.createPersonFrom(new PersonId(55))));

    when(assignmentService.getAssignments(pwaApplicationDetail.getPwaApplication())).thenReturn(List.of(assignment));

    mockMvc.perform(get(ReverseRouter.route(on(ConsentReviewController.class).renderReturnToCaseOfficer(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(model().attribute("form", hasProperty("caseOfficerPersonId", is(caseOfficerPerson.getId().asInt()))));

  }

  @Test
  public void renderReturnToCaseOfficer_caseOfficerNotPrefilled() throws Exception {

    var caseOfficerPerson = PersonTestUtil.createPersonFrom(new PersonId(2));
    var assignment = new Assignment(
        pwaApplicationDetail.getPwaApplication().getId(),
        pwaApplicationDetail.getPwaApplication().getWorkflowType(),
        WorkflowAssignment.CASE_OFFICER,
        caseOfficerPerson.getId());

    when(pwaTeamService.getPeopleWithRegulatorRole(PwaRegulatorRole.CASE_OFFICER))
        .thenReturn(Set.of(PersonTestUtil.createPersonFrom(new PersonId(55))));

    when(assignmentService.getAssignments(pwaApplicationDetail.getPwaApplication())).thenReturn(List.of(assignment));

    mockMvc.perform(get(ReverseRouter.route(on(ConsentReviewController.class).renderReturnToCaseOfficer(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(model().attribute("form", hasProperty("caseOfficerPersonId", nullValue())));

  }

  @Test
  public void returnToCaseOfficer_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ConsentReviewController.class)
                .returnToCaseOfficer(applicationDetail.getMasterPwaApplicationId(), type, null, null, null, null, null)))
        .addRequestParam("caseOfficerPersonId", "2");

    endpointTester.performProcessingPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void returnToCaseOfficer_statusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ConsentReviewController.class)
                .returnToCaseOfficer(applicationDetail.getMasterPwaApplicationId(), type, null, null, null, null, null)))
        .addRequestParam("caseOfficerPersonId", "2");

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void returnToCaseOfficer_success() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(ConsentReviewController.class).returnToCaseOfficer(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
        .param("returnReason", "my reason")
        .param("caseOfficerPersonId", "2"))
        .andExpect(status().is3xxRedirection());

    verify(consentReviewService, times(1)).returnToCaseOfficer(pwaApplicationDetail, "my reason", caseOfficerPerson, user);

  }

  @Test
  public void returnToCaseOfficer_validationFail() throws Exception {

    ControllerTestUtils.mockSmartValidatorErrors(consentReviewReturnFormValidator, List.of("returnReason"));

    mockMvc.perform(post(ReverseRouter.route(on(ConsentReviewController.class).returnToCaseOfficer(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
        .param("returnReason", "my reason")
        .param("caseOfficerPersonId", "2"))
        .andExpect(status().isOk());

    verify(consentReviewService, times(0)).returnToCaseOfficer(any(), any(), any(), any());

  }

  @Test
  public void renderIssueConsent_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ConsentReviewController.class)
                .renderIssueConsent(applicationDetail.getMasterPwaApplicationId(), type, null, null)));

    endpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderIssueConsent_statusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ConsentReviewController.class)
                .renderIssueConsent(applicationDetail.getMasterPwaApplicationId(), type, null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void scheduleConsentIssue_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ConsentReviewController.class)
                .scheduleConsentIssue(applicationDetail.getMasterPwaApplicationId(), type, null, null, null)));

    endpointTester.performProcessingPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void scheduleConsentIssue_statusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ConsentReviewController.class)
                .scheduleConsentIssue(applicationDetail.getMasterPwaApplicationId(), type, null, null, null)));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void scheduleConsentIssue_success() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(ConsentReviewController.class).scheduleConsentIssue(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf()))
        .andExpect(status().is3xxRedirection());

    verify(consentReviewService, times(1)).scheduleConsentIssue(pwaApplicationDetail, user);

  }

}