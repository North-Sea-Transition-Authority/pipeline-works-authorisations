package uk.co.ogauthority.pwa.features.reassignment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.Errors;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.controller.PwaMvcTestConfiguration;
import uk.co.ogauthority.pwa.domain.pwa.application.service.PwaApplicationService;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PadProjectInformationService;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.consultations.AssignCaseOfficerService;

@WebMvcTest(CaseReassignmentController.class)
@Import(PwaMvcTestConfiguration.class)
class CaseReassignmentControllerTest extends AbstractControllerTest {

  private AuthenticatedUserAccount userAccount;

  @MockBean
  CaseReassignmentService caseReassignmentService;

  @MockBean
  PwaApplicationService applicationService;

  @MockBean
  PadProjectInformationService projectInformationService;

  @MockBean
  WorkflowAssignmentService workflowAssignmentService;

  @MockBean
  AssignCaseOfficerService assignCaseOfficerService;

  @BeforeEach
  void setup() {
    userAccount = new AuthenticatedUserAccount(
        new WebUserAccount(1, new Person()),
        EnumSet.of(PwaUserPrivilege.PWA_ACCESS, PwaUserPrivilege.PWA_MANAGER));
  }

  @Test
  void caseReasignmentService_renderTopBarScreen_authenticatedTest() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(
        on(CaseReassignmentController.class)
            .renderCaseReassignment(null, userAccount, null, null, null, null)))
        .with(user(userAccount))).andExpect(status().isOk());
  }

  @Test
  void caseReasignmentService_renderTopBarScreen_unauthenticatedTest() throws Exception {
    userAccount = new AuthenticatedUserAccount(
        new WebUserAccount(1, new Person()), Set.of());

    mockMvc.perform(get(ReverseRouter.route(
        on(CaseReassignmentController.class)
            .renderCaseReassignment(null, userAccount, null, null, null, null)))
        .with(user(userAccount))).andExpect(status().isForbidden());
  }

  @Test
  void renderTopBarScreen_SmokeTest() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(
        on(CaseReassignmentController.class)
            .renderCaseReassignment(null, userAccount, null, null, null, null)))
        .with(user(userAccount)))
        .andExpect(status().isOk())
        .andExpect(model().attributeExists("filterForm"))
        .andExpect(model().attributeExists("form"))
        .andExpect(model().attribute("filterURL",
            ReverseRouter.route(on(CaseReassignmentController.class).filterCaseReassignment(
                null,
                null,
                null,
                null))))
        .andExpect(model().attribute("clearURL",
            ReverseRouter.route(on(CaseReassignmentController.class).renderCaseReassignment(
                null,
                null,
                null,
                null,
                null,
                new CaseReassignmentFilterForm()))));
  }

  @Test
  void filterByCaseOfficer() throws Exception {
    var form = new CaseReassignmentFilterForm();
    mockMvc.perform(post(ReverseRouter.route(
            on(CaseReassignmentController.class)
                .filterCaseReassignment(null, userAccount, form, null)))
            .with(user(userAccount))
            .with(csrf())
            .param("caseOfficerPersonId", "5000"))
        .andExpect(status().is3xxRedirection());
  }

  @Test
  void postSubmitCaseReassignment_validationPasses() throws Exception {
    var applicationDetail = new PwaApplicationDetail();
    when(pwaApplicationDetailService.getDetailByDetailId(any())).thenReturn(applicationDetail);

    doAnswer(invocationOnMock -> {
      var errors = (Errors) invocationOnMock.getArgument(1);
      return errors;
    }).when(caseReassignmentService).validateCasesForm(any(), any());

    mockMvc.perform(post(ReverseRouter.route(
            on(CaseReassignmentController.class)
                .submitCaseReassignment(null, userAccount, null, null, null)))
            .with(user(userAccount))
            .with(csrf())
            .param("selectedApplicationIds", "5000", "3000"))
        .andExpect(status().is3xxRedirection());
  }

  @Test
  void postSubmitCaseReassignment_validationFails() throws Exception {
    var applicationDetail = new PwaApplicationDetail();
    when(pwaApplicationDetailService.getDetailByDetailId(any())).thenReturn(applicationDetail);

    doAnswer(invocationOnMock -> {
      var errors = (Errors) invocationOnMock.getArgument(1);
      errors.reject("fake", "error");
      return errors;
    }).when(caseReassignmentService).validateCasesForm(any(), any());

    mockMvc.perform(post(ReverseRouter.route(
            on(CaseReassignmentController.class)
                .submitCaseReassignment(null, userAccount, null, null, null)))
            .with(user(userAccount))
            .with(csrf())
            .param("selectedApplicationIds", "5000", "3000"))
        .andExpect(status().is2xxSuccessful())
        .andExpect(view().name("reassignment/reassignment"))
        .andExpect(model().attributeHasErrors("form"));
  }

  @Test
  void renderReassignCaseOfficer() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(
            on(CaseReassignmentController.class)
                .renderSelectNewAssignee(null, userAccount, null, null, null, null)))
            .with(user(userAccount))
            .with(csrf())
            .param("selectedApplicationIds", "5000", "3000"))
        .andExpect(status().isOk())
        .andExpect(model().attributeExists("selectedPwas", "caseOfficerCandidates"));
  }

  @Test
  void postReassignCaseOfficer_validationPasses() throws Exception {
    var applicationDetail = new PwaApplicationDetail();
    when(pwaApplicationDetailService.getTipDetailByAppId(anyInt())).thenReturn(applicationDetail);

    doAnswer(invocationOnMock -> {
      var errors = (Errors) invocationOnMock.getArgument(1);
      return errors;
    }).when(caseReassignmentService).validateOfficerForm(any(), any());

    mockMvc.perform(post(ReverseRouter.route(
            on(CaseReassignmentController.class)
                .submitSelectNewAssignee(null, userAccount, null, null, null, null)))
            .with(user(userAccount))
            .with(csrf())
            .param("selectedApplicationIds", "5000", "3000")
            .param("assignedCaseOfficerPersonId", "1111"))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/reassign-cases"));
    verify(assignCaseOfficerService, times(2)).assignCaseOfficer(applicationDetail, new PersonId(1111), userAccount);
  }

  @Test
  void postReassignCaseOfficer_validationFails() throws Exception {
    var applicationDetail = new PwaApplicationDetail();
    when(pwaApplicationDetailService.getDetailByDetailId(any())).thenReturn(applicationDetail);

    doAnswer(invocationOnMock -> {
      var errors = (Errors) invocationOnMock.getArgument(1);
      errors.reject("fake", "error");
      return errors;
    }).when(caseReassignmentService).validateOfficerForm(any(), any());

    mockMvc.perform(post(ReverseRouter.route(
            on(CaseReassignmentController.class)
                .submitSelectNewAssignee(null, userAccount, null,null, null, null)))
            .with(user(userAccount))
            .with(csrf())
            .param("selectedApplicationIds", "5000", "3000")
            .param("assignedCaseOfficerPersonId", "1111"))
        .andExpect(status().is2xxSuccessful())
        .andExpect(view().name("reassignment/chooseAssignee"))
        .andExpect(model().attributeHasErrors("form"));
    verify(assignCaseOfficerService, never()).assignCaseOfficer(applicationDetail, new PersonId(1111), userAccount);
  }

  private List<CaseReassignmentView> getProjectList() {
    var project1 = new CaseReassignmentView();
    project1.setApplicationId(1111);
    project1.setPadReference("Test");
    project1.setPadName("Test");
    project1.setAssignedCaseOfficerPersonId(1000);
    project1.setAssignedCaseOfficer("Test");
    project1.setInCaseOfficerReviewSince(Instant.now());

    var project2 = new CaseReassignmentView();
    project2.setApplicationId(2222);
    project2.setPadReference("Test");
    project2.setPadName("Test");
    project2.setAssignedCaseOfficerPersonId(1000);
    project2.setAssignedCaseOfficer("Test");
    project2.setInCaseOfficerReviewSince(Instant.now());

    var project3 = new CaseReassignmentView();
    project3.setApplicationId(3333);
    project3.setPadReference("Test");
    project3.setPadName("Test");
    project3.setAssignedCaseOfficerPersonId(5000);
    project3.setAssignedCaseOfficer("Test");
    project3.setInCaseOfficerReviewSince(Instant.now());

    return List.of(project1, project2, project3);
  }
}
