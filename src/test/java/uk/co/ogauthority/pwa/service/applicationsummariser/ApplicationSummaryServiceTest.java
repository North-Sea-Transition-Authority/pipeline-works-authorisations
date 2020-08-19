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
  private final static int APP_DETAIL_ID = 2;

  @Mock
  private ApplicationContext springAppContext;

  private PwaApplicationDetail pwaApplicationDetail;

  private ApplicationSummaryService applicationSummaryService;

  @Before
  public void setUp() throws Exception {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(
        PwaApplicationType.INITIAL,
        APP_ID,
        APP_DETAIL_ID);

    applicationSummaryService = new ApplicationSummaryService(springAppContext);
  }

  @Test
  public void summarise_allSectionSummariesProcessedInOrder() {

    var mockAppSummariserService = mock(ApplicationSectionSummariser.class);
    when(mockAppSummariserService.canSummarise(pwaApplicationDetail)).thenReturn(true);

    when(springAppContext.getBean(any(Class.class))).thenAnswer(invocation -> mockAppSummariserService);

    var summaryList = applicationSummaryService.summarise(pwaApplicationDetail);

    InOrder verifyOrder = Mockito.inOrder(mockAppSummariserService);
    ApplicationSectionSummaryType.getSummarySectionByProcessingOrder().forEach(type -> {
          verifyOrder.verify(mockAppSummariserService).canSummarise(pwaApplicationDetail);
          verifyOrder.verify(mockAppSummariserService).summariseSection(
              pwaApplicationDetail,
              type.getTemplatePath()
          );
        }
    );
    verifyOrder.verifyNoMoreInteractions();
    assertThat(summaryList).hasSize(ApplicationSectionSummaryType.values().length);
  }

}