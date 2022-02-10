package uk.co.ogauthority.pwa.features.application.submission;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.exception.ApplicationSubmissionException;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.applicationupdate.ApplicationUpdateRequestService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationSubmissionServiceProviderTest {

  @Mock
  private PwaApplicationFirstDraftSubmissionService pwaApplicationFirstDraftSubmissionService;

  @Mock
  private PwaApplicationUpdateRequestedSubmissionService pwaApplicationUpdateRequestedSubmissionService;

  @Mock
  private ApplicationUpdateRequestService applicationUpdateRequestService;

  @Mock
  private PwaApplicationOptionConfirmationSubmissionService pwaApplicationOptionConfirmationSubmissionService;


  private ApplicationSubmissionServiceProvider applicationSubmissionServiceProvider;

  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setUp() throws Exception {

    applicationSubmissionServiceProvider = new ApplicationSubmissionServiceProvider(
        pwaApplicationFirstDraftSubmissionService,
        pwaApplicationUpdateRequestedSubmissionService,
        applicationUpdateRequestService,
        pwaApplicationOptionConfirmationSubmissionService);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 10, 100, 1);
  }

  @Test
  public void getSubmissionService_whenDetailIsFirstVersion() {

    assertThat(
        applicationSubmissionServiceProvider.getSubmissionService(pwaApplicationDetail)
    ).isEqualTo(pwaApplicationFirstDraftSubmissionService);
  }

  @Test
  public void getSubmissionService_whenDetailIsNotFirstVersion_andOpenUpdateRequest() {

    when(applicationUpdateRequestService.applicationHasOpenUpdateRequest(pwaApplicationDetail))
        .thenReturn(true);

    pwaApplicationDetail.setVersionNo(2);

    assertThat(
        applicationSubmissionServiceProvider.getSubmissionService(pwaApplicationDetail)
    ).isEqualTo(pwaApplicationUpdateRequestedSubmissionService);
  }

  @Test
  public void getSubmissionService_whenDetailIsNotFirstVersion_andNoOpenUpdateRequest_andOptions() {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(
        PwaApplicationType.OPTIONS_VARIATION,
        10,
        100,
        2
    );

    assertThat(
        applicationSubmissionServiceProvider.getSubmissionService(pwaApplicationDetail)
    ).isEqualTo(pwaApplicationOptionConfirmationSubmissionService);
  }

  @Test(expected = ApplicationSubmissionException.class)
  public void getSubmissionService_whenDetailIsNotFirstVersion_andNoOpenUpdateRequest_andNotOptions() {

    pwaApplicationDetail.setVersionNo(2);

    applicationSubmissionServiceProvider.getSubmissionService(pwaApplicationDetail);

  }
}