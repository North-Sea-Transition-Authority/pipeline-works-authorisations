package uk.co.ogauthority.pwa.model.entity.enums;

import java.util.Arrays;
import java.util.stream.Stream;

public enum BlockLocation {
  ONSHORE, OFFSHORE;

  public static Stream<BlockLocation> stream() {
    return Arrays.stream(BlockLocation.values());
  }
}
