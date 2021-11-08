package uk.co.ogauthority.pwa.service.pwaapplications.workflow;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.involvement.ApplicationInvolvementService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.appprocessing.applicationupdate.ApplicationUpdateRequestService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.application.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.submission.PadPipelineNumberingService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PwaApplicationUpdateRequestedSubmissionServiceTest {

  private static final PersonId PERSON_ID = new PersonId(10);
  private static final String SUBMISSION_DESC = "desc";

  @Mock
  private ApplicationInvolvementService applicationInvolvementService;

  @Mock
  private ApplicationUpdateRequestService applicationUpdateRequestService;

  @Mock
  private PadPipelineNumberingService padPipelineNumberingService;

  private PwaApplicationDetail pwaApplicationDetail;

  private PwaApplicationUpdateRequestedSubmissionService pwaApplicationUpdateRequestedSubmissionService;

  private Person person;

  @Before
  public void setUp() {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    pwaApplicationUpdateRequestedSubmissionService = new PwaApplicationUpdateRequestedSubmissionService(
        applicationUpdateRequestService,
        applicationInvolvementService,
        padPipelineNumberingService);

    person = new Person(PERSON_ID.asInt(), "first", "second", "email", "tel");

  }

  @Test
  public void getSubmissionWorkflowResult_returnsExpected() {
    assertThat(pwaApplicationUpdateRequestedSubmissionService.getSubmissionWorkflowResult()).isEmpty();
  }

  @Test
  public void getTaskToComplete_returnsExpected() {
    assertThat(pwaApplicationUpdateRequestedSubmissionService.getTaskToComplete())
        .isEqualTo(PwaApplicationWorkflowTask.UPDATE_APPLICATION);
  }

  @Test
  public void doBeforeSubmit_serviceInteractions() {
    pwaApplicationUpdateRequestedSubmissionService.doBeforeSubmit(pwaApplicationDetail, person, SUBMISSION_DESC);

    verify(applicationUpdateRequestService)
        .respondToApplicationOpenUpdateRequest(pwaApplicationDetail, person, SUBMISSION_DESC);

    verify(padPipelineNumberingService).assignPipelineReferences(pwaApplicationDetail);
    verifyNoMoreInteractions(
        applicationInvolvementService,
        applicationUpdateRequestService,
        padPipelineNumberingService);
  }



  @Test
  public void doAfterSubmit_serviceInteractions() {
    pwaApplicationUpdateRequestedSubmissionService.doAfterSubmit(pwaApplicationDetail);
    verifyNoInteractions(applicationInvolvementService, applicationUpdateRequestService);
  }

  @Test
  public void getSubmittedApplicationDetailStatus_caseOfficerAssigned() {
    when(applicationInvolvementService.getCaseOfficerPersonId(pwaApplicationDetail.getPwaApplication()))
        .thenReturn(Optional.of(PERSON_ID));

    assertThat(pwaApplicationUpdateRequestedSubmissionService.getSubmittedApplicationDetailStatus(pwaApplicationDetail))
        .isEqualTo(PwaApplicationStatus.CASE_OFFICER_REVIEW);

  }

  @Test
  public void getSubmittedApplicationDetailStatus_noCaseOfficer() {
    assertThat(pwaApplicationUpdateRequestedSubmissionService.getSubmittedApplicationDetailStatus(pwaApplicationDetail))
        .isEqualTo(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);
  }
}