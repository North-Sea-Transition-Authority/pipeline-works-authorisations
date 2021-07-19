package uk.co.ogauthority.pwa.testutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.List;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;

public class TimerMetricTestUtils {

  private TimerMetricTestUtils() {
    throw new AssertionError();
  }

  public static Timer setupTimerMetric(Class testClass, String timerName, Appender appender) {
    var meterRegistry = new SimpleMeterRegistry();
    var timer = meterRegistry.timer(timerName);
    Logger root = (Logger) LoggerFactory.getLogger(testClass);
    root.addAppender(appender);
    root.setLevel(Level.INFO);
    return timer;
  }

  public static void assertTimeLogged(ArgumentCaptor<LoggingEvent> loggingEventCaptor, Appender appender, String loggerMessage) {
    verify(appender).doAppend(loggingEventCaptor.capture());
    LoggingEvent loggingEvent = loggingEventCaptor.getAllValues().get(0);
    assertThat(loggingEvent.getMessage()).containsIgnoringCase(loggerMessage);
    assertThat(loggingEvent.getMessage()).containsIgnoringCase("ms");
  }

  public static void assertTimeLogged(ArgumentCaptor<LoggingEvent> loggingEventCaptor, Appender appender, List<String> loggerMessageWords) {
    verify(appender).doAppend(loggingEventCaptor.capture());
    LoggingEvent loggingEvent = loggingEventCaptor.getAllValues().get(0);
    loggerMessageWords.forEach(word -> assertThat(loggingEvent.getMessage()).containsIgnoringCase(word));
    assertThat(loggingEvent.getMessage()).containsIgnoringCase("ms");
  }


}
