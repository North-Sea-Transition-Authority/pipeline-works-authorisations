package uk.co.ogauthority.pwa.features.application.submission;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.exception.ApplicationSubmissionException;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.applicationupdate.ApplicationUpdateRequestService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class ApplicationSubmissionServiceProviderTest {

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

  @BeforeEach
  void setUp() throws Exception {

    applicationSubmissionServiceProvider = new ApplicationSubmissionServiceProvider(
        pwaApplicationFirstDraftSubmissionService,
        pwaApplicationUpdateRequestedSubmissionService,
        applicationUpdateRequestService,
        pwaApplicationOptionConfirmationSubmissionService);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 10, 100, 1);
  }

  @Test
  void getSubmissionService_whenDetailIsFirstVersion() {

    assertThat(
        applicationSubmissionServiceProvider.getSubmissionService(pwaApplicationDetail)
    ).isEqualTo(pwaApplicationFirstDraftSubmissionService);
  }

  @Test
  void getSubmissionService_whenDetailIsNotFirstVersion_andOpenUpdateRequest() {

    when(applicationUpdateRequestService.applicationHasOpenUpdateRequest(pwaApplicationDetail))
        .thenReturn(true);

    pwaApplicationDetail.setVersionNo(2);

    assertThat(
        applicationSubmissionServiceProvider.getSubmissionService(pwaApplicationDetail)
    ).isEqualTo(pwaApplicationUpdateRequestedSubmissionService);
  }

  @Test
  void getSubmissionService_whenDetailIsNotFirstVersion_andNoOpenUpdateRequest_andOptions() {

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

  @Test
  void getSubmissionService_whenDetailIsNotFirstVersion_andNoOpenUpdateRequest_andNotOptions() {
    pwaApplicationDetail.setVersionNo(2);
    assertThrows(ApplicationSubmissionException.class, () ->

      applicationSubmissionServiceProvider.getSubmissionService(pwaApplicationDetail));

  }
}