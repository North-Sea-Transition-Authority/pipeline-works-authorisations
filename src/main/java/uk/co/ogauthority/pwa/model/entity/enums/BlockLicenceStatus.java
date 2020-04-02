package uk.co.ogauthority.pwa.model.entity.enums;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Blocks in PEARS have statuses represented by internalCharacter.
 * New applications should use extant at time of selection, but may have their status change at a later date.
 */
public enum BlockLicenceStatus {
  EXTANT("E"),
  PROVISIONAL("P"),
  SURRENDERED("S"),
  EXPIRED("X");

  private final String internalCharacter;

  BlockLicenceStatus(String internalCharacter) {
    this.internalCharacter = internalCharacter;
  }

  public String getInternalCharacter() {
    return internalCharacter;
  }

  public static Stream<BlockLicenceStatus> stream() {
    return Arrays.stream(BlockLicenceStatus.values());
  }
}
