package uk.co.ogauthority.pwa.util;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CleanupUtilsTest {

  @Test
  void getUnlinkedKeys_comparatorMatching() {
    var collection = List.of(1, 2);
    var map = Map.of(1, List.of("Test"));
    var cleanup = CleanupUtils.getUnlinkedKeys(collection, map, Integer::equals);
    assertThat(cleanup).containsExactly(2);
  }

  @Test
  void getUnlinkedKeys_comparatorNotMatching() {
    var collection = List.of(1, 2);
    var map = Map.of(1, List.of("Test"));
    var cleanup = CleanupUtils.getUnlinkedKeys(collection, map, (integer, integer2) -> integer.equals(3));
    assertThat(cleanup).containsExactly(1, 2);
  }
}