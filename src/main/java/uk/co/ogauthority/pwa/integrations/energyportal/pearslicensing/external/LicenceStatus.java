package uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.external;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Blocks in PEARS have statuses represented by internalCharacter.
 * New applications should use extant at time of selection, but may have their status change at a later date.
 */
public enum LicenceStatus {
  EXTANT("E"),
  PROVISIONAL("P"),
  SURRENDERED("S"),
  EXPIRED("X");

  private final String internalCharacter;

  LicenceStatus(String internalCharacter) {
    this.internalCharacter = internalCharacter;
  }

  public String getInternalCharacter() {
    return internalCharacter;
  }

  public static Stream<LicenceStatus> stream() {
    return Arrays.stream(LicenceStatus.values());
  }
}
