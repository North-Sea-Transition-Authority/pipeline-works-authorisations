package uk.co.ogauthority.pwa.service.enums.appprocessing;

import java.util.stream.Stream;

/**
 * Enumeration of all of the permissions that are linked to case processing actions.
 */
public enum PwaAppProcessingPermission {

  ACCEPT_INITIAL_REVIEW,
  CASE_OFFICER_REVIEW;

  public static Stream<PwaAppProcessingPermission> stream() {
    return Stream.of(PwaAppProcessingPermission.values());
  }

}
