package uk.co.ogauthority.pwa.util;

import com.google.common.base.Stopwatch;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;


public class MetricTimerUtils {

  private MetricTimerUtils() {
    throw new UnsupportedOperationException();
  }


  public static void recordTime(Stopwatch stopWatch, Logger logger, Timer timer, String loggerMessage) {
    var elapsedMs = stopWatch.elapsed(TimeUnit.MILLISECONDS);
    logger.info(loggerMessage + " Took [{}ms]", elapsedMs);
    timer.record(elapsedMs, TimeUnit.MILLISECONDS);
  }

}
