package uk.co.ogauthority.pwa.service.appprocessing.applicationupdate;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.enums.appprocessing.applicationupdates.ApplicationUpdateRequestStatus;
import uk.co.ogauthority.pwa.repository.appprocessing.applicationupdates.ApplicationUpdateRequestRepository;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationUpdateRequestViewServiceTest {

  @Mock
  private ApplicationUpdateRequestRepository applicationUpdateRequestRepository;

  private ApplicationUpdateRequestViewService applicationUpdateRequestViewService;

  @Before
  public void setUp() {
    applicationUpdateRequestViewService = new ApplicationUpdateRequestViewService(applicationUpdateRequestRepository);
  }

  @Test
  public void getOpenRequestView_detail_checkCorrectStatusIsUsed() {

    var detail = new PwaApplicationDetail();

    applicationUpdateRequestViewService.getOpenRequestView(detail);

    verify(applicationUpdateRequestRepository, times(1))
        .findByPwaApplicationDetailAndStatus(detail, ApplicationUpdateRequestStatus.OPEN);

  }

  @Test
  public void getOpenRequestView_app_checkCorrectStatusIsUsed() {

    var app = new PwaApplication();

    applicationUpdateRequestViewService.getOpenRequestView(app);

    verify(applicationUpdateRequestRepository, times(1))
        .findByPwaApplicationDetail_pwaApplicationAndStatus(app, ApplicationUpdateRequestStatus.OPEN);

  }

}