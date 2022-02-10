package uk.co.ogauthority.pwa.service.pwaapplications.workflow;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.exception.ApplicationDeletionException;
import uk.co.ogauthority.pwa.integrations.camunda.external.CamundaWorkflowService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;

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

    when(pwaApplicationDetailService.applicationDetailCanBeDeleted(pwaApplicationDetail)).thenReturn(true);
    pwaApplicationDeleteService.deleteApplication(deletingUser, pwaApplicationDetail);

    verify(camundaWorkflowService, times(1)).deleteProcessInstanceAndThenTasks(
        pwaApplicationDetail.getPwaApplication());
    verify(pwaApplicationDetailService, times(1)).setDeleted(pwaApplicationDetail, deletingPerson);
  }


  @Test(expected = ApplicationDeletionException.class)
  public void deleteApplication_cannotDelete() {
    var deletingUser = new AuthenticatedUserAccount(new WebUserAccount(1, null), List.of());
    when(pwaApplicationDetailService.applicationDetailCanBeDeleted(pwaApplicationDetail)).thenReturn(false);

    pwaApplicationDeleteService.deleteApplication(deletingUser, pwaApplicationDetail);
  }


}