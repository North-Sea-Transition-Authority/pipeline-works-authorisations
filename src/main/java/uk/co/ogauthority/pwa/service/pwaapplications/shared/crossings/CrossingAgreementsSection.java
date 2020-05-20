package uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings;

import java.util.Arrays;
import java.util.stream.Stream;

public enum CrossingAgreementsSection {

  BLOCK_CROSSINGS, MEDIAN_LINE, CABLE_CROSSINGS, PIPELINE_CROSSINGS, CROSSING_TYPES;

  public static Stream<CrossingAgreementsSection> stream() {
    return Arrays.stream(CrossingAgreementsSection.values());
  }
}
