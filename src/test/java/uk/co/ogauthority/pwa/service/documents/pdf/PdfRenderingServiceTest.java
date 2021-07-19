package uk.co.ogauthority.pwa.service.documents.pdf;

import static org.mockito.Mockito.when;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import io.micrometer.core.instrument.Timer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.config.MetricsProvider;
import uk.co.ogauthority.pwa.testutils.TimerMetricTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class PdfRenderingServiceTest {

  @Mock
  private MetricsProvider metricsProvider;

  @Mock
  private Appender appender;

  @Captor
  private ArgumentCaptor<LoggingEvent> loggingEventCaptor;

  private Timer timer;


  private PdfRenderingService pdfRenderingService;


  @Before
  public void setUp() {

    pdfRenderingService = new PdfRenderingService(metricsProvider);

    timer = TimerMetricTestUtils.setupTimerMetric(
        PdfRenderingService.class, "pwa.documentGenerationTimer", appender);
    when(metricsProvider.getDocumentGenerationTimer()).thenReturn(timer);

  }


  @Test
  public void renderToBlob_timerMetricStarted_timeRecordedAndLogged() {

    pdfRenderingService.renderToBlob("data");
    TimerMetricTestUtils.assertTimeLogged(loggingEventCaptor, appender, "PDF generated");
  }

}
