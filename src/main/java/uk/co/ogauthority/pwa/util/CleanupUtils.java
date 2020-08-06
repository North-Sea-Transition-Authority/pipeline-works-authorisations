package uk.co.ogauthority.pwa.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class CleanupUtils {

  private CleanupUtils() {
    throw new AssertionError();
  }

  /**
   * Return values of {keyCollection} that are not keys in {map}
   * Needed as GroupingBy ignores unlinked keys, which need to be removed.
   * @param keyCollection Collection of all keys
   * @param map Map of Key => Value where Key is {T} and Value are links to {T}.
   * @param comparator Comparison BiFunction to prevent requiring interface implementation on IDs.
   * @param <T> Key.
   * @param <U> Value (generic type as will not be the same as {T}).
   * @return List of {T} not found as keys of map based on {comparator}.
   */
  public static <T, U> List<T> getUnlinkedKeys(Collection<T> keyCollection, Map<T, U> map,
                                                  BiFunction<T, T, Boolean> comparator) {
    return keyCollection.stream()
        .filter(key -> map.keySet().stream()
            .noneMatch(mapKey -> comparator.apply(key, mapKey)))
        .collect(Collectors.toUnmodifiableList());
  }

}
