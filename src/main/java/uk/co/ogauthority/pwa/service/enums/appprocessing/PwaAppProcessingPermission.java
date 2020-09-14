package uk.co.ogauthority.pwa.service.enums.appprocessing;

import java.util.stream.Stream;

/**
 * Enumeration of all of the permissions that are linked to case processing actions.
 */
public enum PwaAppProcessingPermission {

  ACCEPT_INITIAL_REVIEW,
  CASE_OFFICER_REVIEW,
  VIEW_ALL_CONSULTATIONS,
  EDIT_CONSULTATIONS,
  ASSIGN_RESPONDER,
  CONSULTATION_RESPONDER,
  WITHDRAW_CONSULTATION,
  ASSIGN_CASE_OFFICER,
  REQUEST_APPLICATION_UPDATE,
  CASE_MANAGEMENT,
  CASE_MANAGEMENT_INDUSTRY,
  ADD_CASE_NOTE,
  EDIT_CONSENT_DOCUMENT;

  public static Stream<PwaAppProcessingPermission> stream() {
    return Stream.of(PwaAppProcessingPermission.values());
  }

}
