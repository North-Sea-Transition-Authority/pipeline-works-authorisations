package uk.co.ogauthority.pwa.service.pwaapplications.workflow;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.EnumSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PwaApplicationSubmissionServiceTest {

  @Mock
  private PwaApplicationDetailService pwaApplicationDetailService;

  @Mock
  private CamundaWorkflowService camundaWorkflowService;

  private PwaApplicationSubmissionService pwaApplicationSubmissionService;

  private Instant fixedInstant = LocalDate
      .of(2020, 2, 6)
      .atStartOfDay(ZoneId.systemDefault())
      .toInstant();

  private PwaApplicationDetail pwaApplicationDetail;
  private WebUserAccount user = new WebUserAccount(1);

  @Before
  public void setup() {
    pwaApplicationSubmissionService = new PwaApplicationSubmissionService(
        pwaApplicationDetailService,
        camundaWorkflowService
    );

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);


  }


  @Test(expected = IllegalArgumentException.class)
  public void submitApplication_whenDetailIsNotTip() {
    pwaApplicationDetail.setTipFlag(false);
    pwaApplicationSubmissionService.submitApplication(user, pwaApplicationDetail);

  }


  @Test
  public void submitApplication_whenDetailIsNotDraft() {
    var invalidSubmitStatuses = EnumSet.allOf(PwaApplicationStatus.class);
    invalidSubmitStatuses.remove(PwaApplicationStatus.DRAFT);

    // test each status where error expected
    for (PwaApplicationStatus invalidStatus : invalidSubmitStatuses) {

      PwaApplicationTestUtil.tryAssertionWithStatus(
          invalidStatus,
          (status) -> {
            pwaApplicationDetail.setStatus(status);
            assertThatThrownBy(() ->
                pwaApplicationSubmissionService.submitApplication(user, pwaApplicationDetail)).isInstanceOf(
                IllegalArgumentException.class);
          }
      );
    }
  }


  @Test
  public void submitApplication_whenDetailIsTipDraft() {

    ArgumentCaptor<PwaApplicationDetail> detailCapture = ArgumentCaptor.forClass(PwaApplicationDetail.class);
    pwaApplicationSubmissionService.submitApplication(user, pwaApplicationDetail);

    verify(pwaApplicationDetailService, times(1)).setSubmitted(pwaApplicationDetail, user);
    verify(camundaWorkflowService, times(1)).completeTask(eq(new WorkflowTaskInstance(pwaApplicationDetail.getPwaApplication(), PwaApplicationWorkflowTask.PREPARE_APPLICATION)));

  }


}