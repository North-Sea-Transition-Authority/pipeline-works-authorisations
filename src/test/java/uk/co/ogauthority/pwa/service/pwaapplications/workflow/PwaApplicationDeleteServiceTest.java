package uk.co.ogauthority.pwa.service.pwaapplications.workflow;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;

@RunWith(MockitoJUnitRunner.class)
public class PwaApplicationDeleteServiceTest {

  private PwaApplicationDeleteService pwaApplicationDeleteService;

  @Mock
  private PwaApplicationDetailService pwaApplicationDetailService;
  @Mock
  private CamundaWorkflowService camundaWorkflowService;

  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setUp() {
    pwaApplicationDeleteService = new PwaApplicationDeleteService(pwaApplicationDetailService, camundaWorkflowService);

    var pwaApplication = new PwaApplication(null, PwaApplicationType.INITIAL, null);
    pwaApplicationDetail = new PwaApplicationDetail(pwaApplication, null, null, null);
  }


  @Test
  public void deleteApplication() {
    var deletingPerson = PersonTestUtil.createDefaultPerson();
    var deletingUser = new AuthenticatedUserAccount(new WebUserAccount(1, deletingPerson), List.of());

    pwaApplicationDeleteService.deleteApplication(deletingUser, pwaApplicationDetail);

    verify(camundaWorkflowService, times(1)).deleteProcessInstanceAndThenTasks(
        pwaApplicationDetail.getPwaApplication());
    verify(pwaApplicationDetailService, times(1)).setDeleted(pwaApplicationDetail, deletingPerson);
  }


  @Test(expected = IllegalArgumentException.class)
  public void deleteApplication_notTipVersion() {
    pwaApplicationDetail.setTipFlag(false);
    var deletingUser = new AuthenticatedUserAccount(new WebUserAccount(1, null), List.of());

    pwaApplicationDeleteService.deleteApplication(deletingUser, pwaApplicationDetail);
  }


  @Test(expected = IllegalArgumentException.class)
  public void deleteApplication_notDraftStatus() {
    pwaApplicationDetail.setStatus(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);
    var deletingUser = new AuthenticatedUserAccount(new WebUserAccount(1, null), List.of());

    pwaApplicationDeleteService.deleteApplication(deletingUser, pwaApplicationDetail);
  }

}