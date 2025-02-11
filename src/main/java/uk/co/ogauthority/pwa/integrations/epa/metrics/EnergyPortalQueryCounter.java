package uk.co.ogauthority.pwa.integrations.epa.metrics;

import org.springframework.stereotype.Component;

/**
 * A thread-safe implementation to keep a running count of queries. All values are stored on a per-thread level
 * and can therefore be accessed concurrently without the risk of race conditions.
 */
@Component
public class EnergyPortalQueryCounter {

  private final ThreadLocal<Integer> epa = ThreadLocal.withInitial(() -> 0);

  public int getAndResetEpa() {
    return getAndReset(epa);
  }

  public void incrementEpa() {
    increment(epa);
  }

  private int getAndReset(ThreadLocal<Integer> threadLocalInteger) {
    var c = threadLocalInteger.get();
    threadLocalInteger.remove();
    return c;
  }

  private void increment(ThreadLocal<Integer> threadLocalInteger) {
    threadLocalInteger.set(threadLocalInteger.get() + 1);
  }

}
