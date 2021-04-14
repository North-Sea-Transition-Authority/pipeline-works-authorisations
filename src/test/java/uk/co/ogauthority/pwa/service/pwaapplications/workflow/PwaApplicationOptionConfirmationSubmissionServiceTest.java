package uk.co.ogauthority.pwa.service.pwaapplications.workflow;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.appprocessing.ApplicationInvolvementService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.workflow.application.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PwaApplicationOptionConfirmationSubmissionServiceTest {

  @Mock
  private ApplicationInvolvementService applicationInvolvementService;

  private PwaApplicationOptionConfirmationSubmissionService pwaApplicationOptionConfirmationSubmissionService;

  private PwaApplicationDetail pwaApplicationDetail;
  private static final PersonId PERSON_ID = new PersonId(10);

  @Before
  public void setup() {
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.OPTIONS_VARIATION);

    pwaApplicationOptionConfirmationSubmissionService = new PwaApplicationOptionConfirmationSubmissionService(
        applicationInvolvementService
    );
  }

  @Test
  public void getSubmissionWorkflowResult() {
    assertThat(pwaApplicationOptionConfirmationSubmissionService.getSubmissionWorkflowResult()).isEmpty();
  }

  @Test
  public void getTaskToComplete() {
    assertThat(pwaApplicationOptionConfirmationSubmissionService.getTaskToComplete())
        .isEqualTo(PwaApplicationWorkflowTask.UPDATE_APPLICATION);
  }

  @Test
  public void getSubmittedApplicationDetailStatus_caseOfficerAssigned() {
    when(applicationInvolvementService.getCaseOfficerPersonId(pwaApplicationDetail.getPwaApplication()))
        .thenReturn(Optional.of(PERSON_ID));

    assertThat(pwaApplicationOptionConfirmationSubmissionService.getSubmittedApplicationDetailStatus(pwaApplicationDetail))
        .isEqualTo(PwaApplicationStatus.CASE_OFFICER_REVIEW);

  }

  @Test
  public void getSubmittedApplicationDetailStatus_noCaseOfficer() {
    assertThat(pwaApplicationOptionConfirmationSubmissionService.getSubmittedApplicationDetailStatus(pwaApplicationDetail))
        .isEqualTo(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);
  }

  @Test
  public void doBeforeSubmit_doesNothing() {
    verifyNoInteractions(applicationInvolvementService);
  }

  @Test
  public void doAfterSubmit_doesNothing() {
    verifyNoInteractions(applicationInvolvementService);
  }
}