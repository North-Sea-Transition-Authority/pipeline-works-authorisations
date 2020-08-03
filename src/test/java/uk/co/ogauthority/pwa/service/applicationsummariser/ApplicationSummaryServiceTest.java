package uk.co.ogauthority.pwa.service.applicationsummariser;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationSummaryServiceTest {

  private final static int APP_ID = 1;
  private final static int NEW_APP_DETAIL_ID = 2;
  private final static int OLD_APP_DETAIL_ID = 1;

  @Mock
  private ApplicationContext springAppContext;

  private PwaApplicationDetail newPwaApplicationDetail;
  private PwaApplicationDetail oldPwaApplicationDetail;

  private ApplicationSummaryService applicationSummaryService;

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


    applicationSummaryService = new ApplicationSummaryService(springAppContext);
  }

  @Test
  public void summarise_allSectionSummariesProcessedInOrder_withIdenticalAppDetail() {

    var mockAppSummariserService = mock(ApplicationSectionSummariser.class);
    when(mockAppSummariserService.canSummarise(newPwaApplicationDetail, newPwaApplicationDetail)).thenReturn(true);

    when(springAppContext.getBean(any(Class.class))).thenAnswer(invocation -> mockAppSummariserService);

    var summaryList = applicationSummaryService.summariseAsDiff(newPwaApplicationDetail, newPwaApplicationDetail);

    InOrder verifyOrder = Mockito.inOrder(mockAppSummariserService);
    ApplicationSectionSummaryType.getSummarySectionByProcessingOrder().forEach(type -> {
          verifyOrder.verify(mockAppSummariserService).canSummarise(newPwaApplicationDetail, newPwaApplicationDetail);
          verifyOrder.verify(mockAppSummariserService).summariseDifferences(
              newPwaApplicationDetail,
              newPwaApplicationDetail,
              type.getTemplatePath()
          );
        }
    );
    verifyOrder.verifyNoMoreInteractions();
    assertThat(summaryList).hasSize(ApplicationSectionSummaryType.values().length);
  }

  @Test
  public void summariseAsDiff_allSectionSummariesProcessedInOrder() {

    var mockAppSummariserService = mock(ApplicationSectionSummariser.class);
    when(mockAppSummariserService.canSummarise(newPwaApplicationDetail, oldPwaApplicationDetail)).thenReturn(true);

    when(springAppContext.getBean(any(Class.class))).thenAnswer(invocation -> mockAppSummariserService);

    var summaryList = applicationSummaryService.summariseAsDiff(newPwaApplicationDetail, oldPwaApplicationDetail);

    InOrder verifyOrder = Mockito.inOrder(mockAppSummariserService);
    ApplicationSectionSummaryType.getSummarySectionByProcessingOrder().forEach(type -> {
          verifyOrder.verify(mockAppSummariserService).canSummarise(newPwaApplicationDetail, oldPwaApplicationDetail);
          verifyOrder.verify(mockAppSummariserService).summariseDifferences(
              newPwaApplicationDetail,
              oldPwaApplicationDetail,
              type.getTemplatePath()
          );
        }
    );
    verifyOrder.verifyNoMoreInteractions();
    assertThat(summaryList).hasSize(ApplicationSectionSummaryType.values().length);

  }
}