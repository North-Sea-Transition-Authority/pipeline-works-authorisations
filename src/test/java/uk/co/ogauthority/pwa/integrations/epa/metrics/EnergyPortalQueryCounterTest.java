package uk.co.ogauthority.pwa.integrations.epa.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EnergyPortalQueryCounterTest {
  private EnergyPortalQueryCounter energyPortalQueryCounter;

  @BeforeEach
  void setUp() {
    energyPortalQueryCounter = new EnergyPortalQueryCounter();
  }

  @Test
  void getAndResetEpa() throws ExecutionException, InterruptedException {
    testMultiThreaded(EnergyPortalQueryCounter::incrementEpa, EnergyPortalQueryCounter::getAndResetEpa);
  }

  /*
  This simulates multiple requests happening concurrently.

  You can verify this test works by changing the implementation of QueryCounter to use an int
  instead of a ThreadLocal<Integer>. The QueryCounter will run into race conditions where the count will be updated
  concurrently across multiple threads, returning a value much greater than the expected value.
   */
  private void testMultiThreaded(
      Consumer<EnergyPortalQueryCounter> queryCounterConsumer,
      Function<EnergyPortalQueryCounter, Integer> valueExtractor
  ) throws InterruptedException, ExecutionException {
    var threadCount = 10;
    var executorService = Executors.newFixedThreadPool(threadCount);

    assertThat(valueExtractor.apply(energyPortalQueryCounter)).isZero();

    var queryCountPerThread = 5;
    var futures = new ArrayList<Future<Integer>>(threadCount);

    for (var i = 0; i < threadCount; i++) {
      futures.add(executorService.submit(() -> invokeQueryCounter(queryCountPerThread, queryCounterConsumer, valueExtractor)));
    }

    var timedOut = executorService.awaitTermination(5, TimeUnit.SECONDS);
    assertThat(timedOut).isFalse();

    for (var future : futures) {
      var queryCount = future.get();
      assertThat(queryCount).isEqualTo(queryCountPerThread);
    }
  }

  private <T> T invokeQueryCounter(
      int queryCount,
      Consumer<EnergyPortalQueryCounter> queryCounterConsumer,
      Function<EnergyPortalQueryCounter, T> valueExtractor
  ) {
    for (var i = 0; i < queryCount; i++) {
      queryCounterConsumer.accept(energyPortalQueryCounter);
    }
    return valueExtractor.apply(energyPortalQueryCounter);
  }
}