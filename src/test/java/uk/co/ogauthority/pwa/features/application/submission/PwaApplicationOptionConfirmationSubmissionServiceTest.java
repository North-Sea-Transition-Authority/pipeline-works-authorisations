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
import uk.co.ogauthority.pwa.features.appprocessing.workflow.appworkflowmappings.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class PwaApplicationOptionConfirmationSubmissionServiceTest {

  @Mock
  private ApplicationInvolvementService applicationInvolvementService;

  @Mock
  private PadPipelineNumberingService padPipelineNumberingService;

  private PwaApplicationOptionConfirmationSubmissionService pwaApplicationOptionConfirmationSubmissionService;

  private PwaApplicationDetail pwaApplicationDetail;
  private static final PersonId PERSON_ID = new PersonId(10);

  @BeforeEach
  void setup() {
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.OPTIONS_VARIATION);

    pwaApplicationOptionConfirmationSubmissionService = new PwaApplicationOptionConfirmationSubmissionService(
        applicationInvolvementService,
        padPipelineNumberingService);
  }

  @Test
  void getSubmissionWorkflowResult() {
    assertThat(pwaApplicationOptionConfirmationSubmissionService.getSubmissionWorkflowResult()).isEmpty();
  }

  @Test
  void getTaskToComplete() {
    assertThat(pwaApplicationOptionConfirmationSubmissionService.getTaskToComplete())
        .isEqualTo(PwaApplicationWorkflowTask.UPDATE_APPLICATION);
  }

  @Test
  void getSubmittedApplicationDetailStatus_caseOfficerAssigned() {
    when(applicationInvolvementService.getCaseOfficerPersonId(pwaApplicationDetail.getPwaApplication()))
        .thenReturn(Optional.of(PERSON_ID));

    assertThat(pwaApplicationOptionConfirmationSubmissionService.getSubmittedApplicationDetailStatus(pwaApplicationDetail))
        .isEqualTo(PwaApplicationStatus.CASE_OFFICER_REVIEW);

  }

  @Test
  void getSubmittedApplicationDetailStatus_noCaseOfficer() {
    assertThat(pwaApplicationOptionConfirmationSubmissionService.getSubmittedApplicationDetailStatus(pwaApplicationDetail))
        .isEqualTo(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);
  }

  @Test
  void doBeforeSubmit_triesToAssignPipelineNumbers() {

    pwaApplicationOptionConfirmationSubmissionService.doBeforeSubmit(pwaApplicationDetail, null ,null);

    verify(padPipelineNumberingService).assignPipelineReferences(pwaApplicationDetail);

    verifyNoMoreInteractions(applicationInvolvementService, padPipelineNumberingService);
  }

  @Test
  void doAfterSubmit_doesNothing() {
    verifyNoInteractions(applicationInvolvementService, padPipelineNumberingService);
  }

  @Test
  void getSubmissionType() {
    assertThat(pwaApplicationOptionConfirmationSubmissionService.getSubmissionType()).isEqualTo(ApplicationSubmissionType.OPTIONS_CONFIRMATION);
  }

}