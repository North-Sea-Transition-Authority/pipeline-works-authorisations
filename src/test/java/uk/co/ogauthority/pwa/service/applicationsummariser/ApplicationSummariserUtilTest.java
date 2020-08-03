package uk.co.ogauthority.pwa.service.applicationsummariser;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.function.Function;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationSummariserUtilTest {

  private final static int APP_ID = 1;
  private final static int NEW_APP_DETAIL_ID = 2;
  private final static int OLD_APP_DETAIL_ID = 1;

  private PwaApplicationDetail newPwaApplicationDetail;
  private PwaApplicationDetail oldPwaApplicationDetail;

  @Mock
  private Function<PwaApplicationDetail, Boolean> mockFunction;

  @Before
  public void setUp() throws Exception {

    newPwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(
        PwaApplicationType.INITIAL,
        APP_ID,
        NEW_APP_DETAIL_ID);

    oldPwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(
        PwaApplicationType.INITIAL,
        APP_ID,
        OLD_APP_DETAIL_ID);

    when(mockFunction.apply(any())).thenReturn(false);

  }

  @Test
  public void canSummariseOptimised_whenSameAppDetail() {

    ApplicationSummariserUtil.canSummariseOptimised(newPwaApplicationDetail, newPwaApplicationDetail, mockFunction);

    verify(mockFunction, times(1)).apply(newPwaApplicationDetail);
    verifyNoMoreInteractions(mockFunction);
  }

  @Test
  public void canSummariseOptimised_whenDifferentAppDetails() {

    ApplicationSummariserUtil.canSummariseOptimised(newPwaApplicationDetail, oldPwaApplicationDetail, mockFunction);

    verify(mockFunction, times(1)).apply(newPwaApplicationDetail);
    verify(mockFunction, times(1)).apply(oldPwaApplicationDetail);
    verifyNoMoreInteractions(mockFunction);
  }

  @Test
  public void canSummariseOptimised_whenSameAppDetail_andFunctionReturnsTrue() {

    when(mockFunction.apply(newPwaApplicationDetail)).thenReturn(true);
    assertThat(
        ApplicationSummariserUtil.canSummariseOptimised(newPwaApplicationDetail, newPwaApplicationDetail, mockFunction)
    ).isTrue();

  }

  @Test
  public void canSummariseOptimised_whenDifferentAppDetail_andOnlyOldDetailFunctionReturnsTrue() {

    when(mockFunction.apply(oldPwaApplicationDetail)).thenReturn(true);
    assertThat(
        ApplicationSummariserUtil.canSummariseOptimised(newPwaApplicationDetail, oldPwaApplicationDetail, mockFunction)
    ).isTrue();

  }

  @Test
  public void canSummariseOptimised_whenDifferentAppDetail_andNeitherFunctionReturnsTrue() {
    assertThat(
        ApplicationSummariserUtil.canSummariseOptimised(newPwaApplicationDetail, oldPwaApplicationDetail, mockFunction)
    ).isFalse();

  }
}