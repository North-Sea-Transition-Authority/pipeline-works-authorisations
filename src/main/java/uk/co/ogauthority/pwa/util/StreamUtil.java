package uk.co.ogauthority.pwa.util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamUtil {
  private StreamUtil() {}

  public static <T, K, U> Collector<T, ?, Map<K, U>> toLinkedHashMap(Function<? super T, ? extends K> keyMapper,
                                                                     Function<? super T, ? extends U> valueMapper) {

    return Collectors.toMap(
        keyMapper,
        valueMapper,
        (u, v) -> {
          throw new IllegalStateException(String.format("Duplicate key %s", u));
        },
        LinkedHashMap::new
    );
  }

  public static <T> BinaryOperator<T> keepFirst() {
    return (first, duplicate) -> first;
  }

  public static <T> List<T> distinctUnion(List<T> list1, List<T> list2) {
    return Stream.concat(list1.stream(), list2.stream())
        .distinct()
        .collect(Collectors.toList());
  }
}
