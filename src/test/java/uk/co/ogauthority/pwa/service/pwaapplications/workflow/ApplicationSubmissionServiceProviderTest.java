package uk.co.ogauthority.pwa.service.pwaapplications.workflow;



import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

public class ApplicationSubmissionServiceProviderTest {

  @Mock
  private PwaApplicationFirstDraftSubmissionService pwaApplicationFirstDraftSubmissionService;

  @Mock
  private PwaApplicationUpdateRequestedSubmissionService pwaApplicationUpdateRequestedSubmissionService;


  private ApplicationSubmissionServiceProvider applicationSubmissionServiceProvider;

  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setUp() throws Exception {

    applicationSubmissionServiceProvider = new ApplicationSubmissionServiceProvider(
        pwaApplicationFirstDraftSubmissionService,
        pwaApplicationUpdateRequestedSubmissionService
    );

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 10, 100, 1);
  }

  @Test
  public void getSubmissionService_whenDetailIsFirstVersion() {

    assertThat(
        applicationSubmissionServiceProvider.getSubmissionService(pwaApplicationDetail)
    ).isEqualTo(pwaApplicationFirstDraftSubmissionService);
  }

  @Test
  public void getSubmissionService_whenDetailIsNotFirstVersion() {

    pwaApplicationDetail.setVersionNo(2);

    assertThat(
        applicationSubmissionServiceProvider.getSubmissionService(pwaApplicationDetail)
    ).isEqualTo(pwaApplicationUpdateRequestedSubmissionService);
  }
}