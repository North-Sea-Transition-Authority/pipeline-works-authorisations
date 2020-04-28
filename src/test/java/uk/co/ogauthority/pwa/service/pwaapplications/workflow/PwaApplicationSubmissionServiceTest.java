package uk.co.ogauthority.pwa.service.pwaapplications.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Clock;
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
import uk.co.ogauthority.pwa.repository.pwaapplications.PwaApplicationDetailRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.workflow.UserWorkflowTask;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.util.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PwaApplicationSubmissionServiceTest {

  @Mock
  private PwaApplicationDetailRepository pwaApplicationDetailRepository;

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
        pwaApplicationDetailRepository,
        camundaWorkflowService,
        Clock.fixed(fixedInstant, ZoneId.systemDefault())
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

    verify(pwaApplicationDetailRepository, times(1)).save(detailCapture.capture());
    assertThat(detailCapture.getValue()).satisfies((detail) -> {
      assertThat(detail.getStatus()).isEqualTo(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);
      assertThat(detail.getSubmittedByWuaId()).isEqualTo(user.getWuaId());
      assertThat(detail.getSubmittedTimestamp()).isEqualTo(fixedInstant);
    });

    verify(camundaWorkflowService, times(1)).completeTask(pwaApplicationDetail.getMasterPwaApplicationId(), UserWorkflowTask.PREPARE_APPLICATION);


  }


}