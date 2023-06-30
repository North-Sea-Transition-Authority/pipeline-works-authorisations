package uk.co.ogauthority.pwa.features.reassignment;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.util.EnumSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.controller.PwaMvcTestConfiguration;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.consultations.AssignCaseOfficerService;

@RunWith(SpringRunner.class)
@WebMvcTest(CaseReassignmentController.class)
@Import(PwaMvcTestConfiguration.class)
public class CaseReassignmentControllerTest extends AbstractControllerTest {

  private AuthenticatedUserAccount userAccount;

  @MockBean
  ReviewIdentifierService reviewIdentifierService;

  @MockBean
  WorkflowAssignmentService workflowAssignmentService;

  @MockBean
  AssignCaseOfficerService assignCaseOfficerService;

  @Before
  public void setup() {
    userAccount = new AuthenticatedUserAccount(
        new WebUserAccount(1, new Person()),
        EnumSet.of(PwaUserPrivilege.PWA_MANAGER));
  }

  @Test
  public void caseReasignmentService_renderTopBarScreen_authenticatedTest() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(
        on(CaseReassignmentController.class)
            .renderCaseReassignment(null, userAccount, null, null)))
        .with(authenticatedUserAndSession(userAccount))).andExpect(status().isOk());
  }

  @Test
  public void caseReasignmentService_renderTopBarScreen_unauthenticatedTest() throws Exception {
    userAccount = new AuthenticatedUserAccount(
        new WebUserAccount(1, new Person()), Set.of());

    mockMvc.perform(get(ReverseRouter.route(
        on(CaseReassignmentController.class)
            .renderCaseReassignment(null, userAccount, null, null)))
        .with(authenticatedUserAndSession(userAccount))).andExpect(status().isForbidden());
  }

  @Test
  public void renderTopBarScreen_SmokeTest() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(
        on(CaseReassignmentController.class)
            .renderCaseReassignment(null, userAccount, null, null)))
        .with(authenticatedUserAndSession(userAccount))).andExpect(status().isOk())
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
                new CaseReassignmentFilterForm()))));
  }
}
