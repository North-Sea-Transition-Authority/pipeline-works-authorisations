package uk.co.ogauthority.pwa.features.appprocessing.tasks.applicationupdate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class ApplicationUpdateRequestViewServiceTest {

  @Mock
  private ApplicationUpdateRequestViewRepository applicationUpdateRequestViewRepository;

  private ApplicationUpdateRequestViewService applicationUpdateRequestViewService;

  private PwaApplicationDetail pwaApplicationDetail;


  @BeforeEach
  void setUp() {
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    applicationUpdateRequestViewService = new ApplicationUpdateRequestViewService(
        applicationUpdateRequestViewRepository
    );
  }

  @Test
  void getOpenRequestView_detail_checkCorrectStatusIsUsed() {

    var detail = new PwaApplicationDetail();

    applicationUpdateRequestViewService.getOpenRequestView(detail);

    verify(applicationUpdateRequestViewRepository, times(1))
        .findByPwaApplicationDetailAndStatus(detail, ApplicationUpdateRequestStatus.OPEN);

  }

  @Test
  void getOpenRequestView_app_checkCorrectStatusIsUsed() {

    var app = new PwaApplication();

    applicationUpdateRequestViewService.getOpenRequestView(app);

    verify(applicationUpdateRequestViewRepository, times(1))
        .findByPwaApplicationDetail_pwaApplicationAndStatus(app, ApplicationUpdateRequestStatus.OPEN);

  }

  @Test
  void getLastRespondedApplicationUpdateView_noRespondedUpdates(){

    var previousUpdate = applicationUpdateRequestViewService.getLastRespondedApplicationUpdateView(pwaApplicationDetail);
    assertThat(previousUpdate).isEmpty();

  }


  private ApplicationUpdateRequestView getMockUpdateRequestViewForVersion(int linkedAppDetailVersionNumber){
    var view = mock(ApplicationUpdateRequestView.class);
    when(view.getRequestedOnApplicationVersionNo()).thenReturn(linkedAppDetailVersionNumber);
    return view;
  }

  @Test
  void getLastRespondedApplicationUpdateView_respondedUpdateExistsForGivenAppVersion(){
    // Make sure responded requests for versions after and including the given app detail are filtered out
    // because requests are linked on the version before the response is submitted.
    var requestVersion1 = getMockUpdateRequestViewForVersion(1);
    var requestVersion2 = getMockUpdateRequestViewForVersion(2);
    var requestVersion3 = getMockUpdateRequestViewForVersion(3);

    pwaApplicationDetail.setVersionNo(3);

    when(applicationUpdateRequestViewRepository.findAllByPwaApplicationDetail_pwaApplicationAndStatus(
        pwaApplicationDetail.getPwaApplication(), ApplicationUpdateRequestStatus.RESPONDED)
    ).thenReturn(List.of(requestVersion1, requestVersion2, requestVersion3));

    var previousUpdate = applicationUpdateRequestViewService.getLastRespondedApplicationUpdateView(pwaApplicationDetail);

    assertThat(previousUpdate).contains(requestVersion2);

  }

}