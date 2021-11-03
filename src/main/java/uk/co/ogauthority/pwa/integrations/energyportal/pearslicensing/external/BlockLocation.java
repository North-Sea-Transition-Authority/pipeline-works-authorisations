package uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.external;

import java.util.Arrays;
import java.util.stream.Stream;

public enum BlockLocation {
  ONSHORE, OFFSHORE;

  public static Stream<BlockLocation> stream() {
    return Arrays.stream(BlockLocation.values());
  }
}
