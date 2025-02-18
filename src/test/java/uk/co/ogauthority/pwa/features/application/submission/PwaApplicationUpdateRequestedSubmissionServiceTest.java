package uk.co.ogauthority.pwa.features.application.submission;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.involvement.ApplicationInvolvementService;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.applicationupdate.ApplicationUpdateRequestService;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.appworkflowmappings.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class PwaApplicationUpdateRequestedSubmissionServiceTest {

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

  @BeforeEach
  void setUp() {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    pwaApplicationUpdateRequestedSubmissionService = new PwaApplicationUpdateRequestedSubmissionService(
        applicationUpdateRequestService,
        applicationInvolvementService,
        padPipelineNumberingService);

    person = new Person(PERSON_ID.asInt(), "first", "second", "email", "tel");

  }

  @Test
  void getSubmissionWorkflowResult_returnsExpected() {
    assertThat(pwaApplicationUpdateRequestedSubmissionService.getSubmissionWorkflowResult()).isEmpty();
  }

  @Test
  void getTaskToComplete_returnsExpected() {
    assertThat(pwaApplicationUpdateRequestedSubmissionService.getTaskToComplete())
        .isEqualTo(PwaApplicationWorkflowTask.UPDATE_APPLICATION);
  }

  @Test
  void doBeforeSubmit_serviceInteractions() {
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
  void doAfterSubmit_serviceInteractions() {
    pwaApplicationUpdateRequestedSubmissionService.doAfterSubmit(pwaApplicationDetail);
    verifyNoInteractions(applicationInvolvementService, applicationUpdateRequestService);
  }

  @Test
  void getSubmittedApplicationDetailStatus_caseOfficerAssigned() {
    when(applicationInvolvementService.getCaseOfficerPersonId(pwaApplicationDetail.getPwaApplication()))
        .thenReturn(Optional.of(PERSON_ID));

    assertThat(pwaApplicationUpdateRequestedSubmissionService.getSubmittedApplicationDetailStatus(pwaApplicationDetail))
        .isEqualTo(PwaApplicationStatus.CASE_OFFICER_REVIEW);

  }

  @Test
  void getSubmittedApplicationDetailStatus_noCaseOfficer() {
    assertThat(pwaApplicationUpdateRequestedSubmissionService.getSubmittedApplicationDetailStatus(pwaApplicationDetail))
        .isEqualTo(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);
  }

  @Test
  void getSubmissionType() {
    assertThat(pwaApplicationUpdateRequestedSubmissionService.getSubmissionType()).isEqualTo(ApplicationSubmissionType.UPDATE);
  }

}