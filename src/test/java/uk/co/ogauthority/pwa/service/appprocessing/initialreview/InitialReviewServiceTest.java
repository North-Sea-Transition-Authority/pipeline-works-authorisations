package uk.co.ogauthority.pwa.service.appprocessing.initialreview;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.ActionAlreadyPerformedException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.UserWorkflowTask;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;

@RunWith(MockitoJUnitRunner.class)
public class InitialReviewServiceTest {

  @Mock
  private PwaApplicationDetailService detailService;

  @Mock
  private CamundaWorkflowService camundaWorkflowService;

  private InitialReviewService initialReviewService;

  private PwaApplicationDetail detail;
  private PwaApplication app;
  private WebUserAccount user;

  @Before
  public void setUp() {

    user = new WebUserAccount(1);

    app = new PwaApplication();
    app.setId(1);

    detail = new PwaApplicationDetail();
    detail.setPwaApplication(app);
    detail.setStatus(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);

    initialReviewService = new InitialReviewService(detailService, camundaWorkflowService);

  }

  @Test
  public void acceptApplication_success() {

    initialReviewService.acceptApplication(detail, user);

    verify(detailService, times(1)).setInitialReviewApproved(detail, user);
    verify(camundaWorkflowService, times(1)).completeTask(app.getId(), UserWorkflowTask.APPLICATION_REVIEW);

  }

  @Test(expected = ActionAlreadyPerformedException.class)
  public void acceptApplication_failed_alreadyAccepted() {

    initialReviewService.acceptApplication(detail, user);
    
    detail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);
    initialReviewService.acceptApplication(detail, user);

  }

}
