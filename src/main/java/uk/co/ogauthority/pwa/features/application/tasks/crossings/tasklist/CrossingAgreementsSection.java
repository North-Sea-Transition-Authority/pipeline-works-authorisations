package uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist;

import java.util.Arrays;
import java.util.stream.Stream;

public enum CrossingAgreementsSection {

  BLOCK_CROSSINGS, CARBON_STORAGE_AREA_CROSSINGS, MEDIAN_LINE, CABLE_CROSSINGS, PIPELINE_CROSSINGS, CROSSING_TYPES;

  public static Stream<CrossingAgreementsSection> stream() {
    return Arrays.stream(CrossingAgreementsSection.values());
  }
}
