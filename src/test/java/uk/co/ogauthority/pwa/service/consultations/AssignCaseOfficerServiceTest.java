package uk.co.ogauthority.pwa.service.consultations;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.consultation.AssignCaseOfficerForm;
import uk.co.ogauthority.pwa.model.notify.emailproperties.CaseOfficerAssignedEmailProps;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.person.PersonService;
import uk.co.ogauthority.pwa.service.teammanagement.TeamManagementService;
import uk.co.ogauthority.pwa.service.workflow.assignment.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.validators.consultations.AssignCaseOfficerValidator;


@RunWith(MockitoJUnitRunner.class)
public class AssignCaseOfficerServiceTest {

  private AssignCaseOfficerService assignCaseOfficerService;

  @Mock
  private WorkflowAssignmentService workflowAssignmentService;
  @Mock
  private TeamManagementService teamManagementService;
  @Mock
  private NotifyService notifyService;
  @Mock
  private PersonService personService;
  @Mock
  private AssignCaseOfficerValidator assignCaseOfficerValidator;

  PwaApplicationDetail appDetail;

  @Captor
  private ArgumentCaptor<CaseOfficerAssignedEmailProps> emailPropsCaptor;


  @Before
  public void setUp() {
    assignCaseOfficerService = new AssignCaseOfficerService(
        workflowAssignmentService,
        teamManagementService,
        notifyService,
        personService,
        assignCaseOfficerValidator);
    appDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 1, 1);
  }



  @Test
  public void assignCaseOfficer_assignToDifferentUser_emailSent() {

    var form = new AssignCaseOfficerForm();
    form.setCaseOfficerPersonId(2);

    var assigningPerson = new Person(1, "m", "assign", "assign@assign.com", null);
    var assigningUser = new AuthenticatedUserAccount(
        new WebUserAccount(1, assigningPerson), null);

    var caseOfficerPerson = new Person(2, "fore", "sur", "fore@sur.com", null);
    when(teamManagementService.getPerson(2)).thenReturn(caseOfficerPerson);

    appDetail.setSubmittedByPersonId(assigningPerson.getId());
    when(personService.getPersonById(appDetail.getSubmittedByPersonId())).thenReturn(assigningPerson);

    assignCaseOfficerService.assignCaseOfficer(form.getCaseOfficerPerson(), appDetail, assigningUser);

    //verify new case officer assignment done and email is sent
    verify(workflowAssignmentService, times(1)).assign(
        appDetail.getPwaApplication(), PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW, caseOfficerPerson, assigningUser.getLinkedPerson());

    verify(notifyService, times(1)).sendEmail(emailPropsCaptor.capture(), eq(assigningUser.getLinkedPerson().getEmailAddress()));

    var emailProps = emailPropsCaptor.getValue();

    assertThat(emailProps.getEmailPersonalisation()).contains(
        entry("RECIPIENT_FULL_NAME", assigningUser.getLinkedPerson().getFullName()),
        entry("CASE_OFFICER_NAME", caseOfficerPerson.getFullName()),
        entry("APPLICATION_REFERENCE", appDetail.getPwaApplicationRef())
    );

  }

  @Test
  public void canShowInTaskList_hasPermission() {
    appDetail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);
    var processingContext = new PwaAppProcessingContext(appDetail, null, Set.of(PwaAppProcessingPermission.ASSIGN_CASE_OFFICER), null,
        null);

    boolean canShow = assignCaseOfficerService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  public void canShowInTaskList_noPermission() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(), null, null);

    boolean canShow = assignCaseOfficerService.canShowInTaskList(processingContext);

    assertThat(canShow).isFalse();

  }

  @Test
  public void canShowInTaskList_hasPermissionWithIncorrectStatus() {
    appDetail.setStatus(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);
    var processingContext = new PwaAppProcessingContext(appDetail, null, Set.of(PwaAppProcessingPermission.ASSIGN_CASE_OFFICER), null,
        null);

    boolean canShow = assignCaseOfficerService.canShowInTaskList(processingContext);

    assertThat(canShow).isFalse();

  }

}

