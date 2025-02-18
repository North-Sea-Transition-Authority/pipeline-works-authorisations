package uk.co.ogauthority.pwa.service.pwaapplications.workflow;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.exception.ApplicationDeletionException;
import uk.co.ogauthority.pwa.integrations.camunda.external.CamundaWorkflowService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;

@ExtendWith(MockitoExtension.class)
class PwaApplicationDeleteServiceTest {

  private PwaApplicationDeleteService pwaApplicationDeleteService;

  @Mock
  private PwaApplicationDetailService pwaApplicationDetailService;
  @Mock
  private CamundaWorkflowService camundaWorkflowService;

  private PwaApplicationDetail pwaApplicationDetail;

  @BeforeEach
  void setUp() {
    pwaApplicationDeleteService = new PwaApplicationDeleteService(pwaApplicationDetailService, camundaWorkflowService);

    var pwaApplication = new PwaApplication(null, PwaApplicationType.INITIAL, null);
    pwaApplicationDetail = new PwaApplicationDetail(pwaApplication, null, null, null);
  }


  @Test
  void deleteApplication() {
    var deletingPerson = PersonTestUtil.createDefaultPerson();
    var deletingUser = new AuthenticatedUserAccount(new WebUserAccount(1, deletingPerson), List.of());

    when(pwaApplicationDetailService.applicationDetailCanBeDeleted(pwaApplicationDetail)).thenReturn(true);
    pwaApplicationDeleteService.deleteApplication(deletingUser, pwaApplicationDetail);

    verify(camundaWorkflowService, times(1)).deleteProcessInstanceAndThenTasks(
        pwaApplicationDetail.getPwaApplication());
    verify(pwaApplicationDetailService, times(1)).setDeleted(pwaApplicationDetail, deletingPerson);
  }


  @Test
  void deleteApplication_cannotDelete() {
    var deletingUser = new AuthenticatedUserAccount(new WebUserAccount(1, null), List.of());
    when(pwaApplicationDetailService.applicationDetailCanBeDeleted(pwaApplicationDetail)).thenReturn(false);
    assertThrows(ApplicationDeletionException.class, () ->

      pwaApplicationDeleteService.deleteApplication(deletingUser, pwaApplicationDetail));
  }


}