package uk.co.ogauthority.pwa.service.documents.pdf;

import static org.mockito.Mockito.when;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.config.MetricsProvider;
import uk.co.ogauthority.pwa.testutils.TimerMetricTestUtils;

@ExtendWith(MockitoExtension.class)
class PdfRenderingServiceTest {

  @Mock
  private MetricsProvider metricsProvider;

  @Mock
  private Appender appender;

  @Captor
  private ArgumentCaptor<LoggingEvent> loggingEventCaptor;

  private Timer timer;


  private PdfRenderingService pdfRenderingService;


  @BeforeEach
  void setUp() {

    pdfRenderingService = new PdfRenderingService(metricsProvider);

    timer = TimerMetricTestUtils.setupTimerMetric(
        PdfRenderingService.class, "pwa.documentGenerationTimer", appender);
    when(metricsProvider.getDocumentGenerationTimer()).thenReturn(timer);

  }


  @Test
  void render_timerMetricStarted_timeRecordedAndLogged() {

    pdfRenderingService.render("data");
    TimerMetricTestUtils.assertTimeLogged(loggingEventCaptor, appender, "PDF generated");
  }

}
