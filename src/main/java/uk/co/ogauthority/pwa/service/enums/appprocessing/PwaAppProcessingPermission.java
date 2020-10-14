package uk.co.ogauthority.pwa.service.enums.appprocessing;

import java.util.stream.Stream;

/**
 * Enumeration of all of the permissions that are linked to case processing actions.
 */
public enum PwaAppProcessingPermission {

  // OGA
  ACCEPT_INITIAL_REVIEW,
  CASE_OFFICER_REVIEW,
  VIEW_ALL_CONSULTATIONS,
  EDIT_CONSULTATIONS,
  WITHDRAW_CONSULTATION,
  ASSIGN_CASE_OFFICER,
  REQUEST_APPLICATION_UPDATE,
  CASE_MANAGEMENT_OGA(ProcessingPermissionType.APP_SPECIFIC),
  ADD_CASE_NOTE,
  EDIT_CONSENT_DOCUMENT,

  // CONSULTEES
  CASE_MANAGEMENT_CONSULTEE(ProcessingPermissionType.APP_SPECIFIC),
  ASSIGN_RESPONDER,
  CONSULTATION_RESPONDER,

  // INDUSTRY
  CASE_MANAGEMENT_INDUSTRY(ProcessingPermissionType.APP_SPECIFIC),
  UPDATE_APPLICATION(ProcessingPermissionType.APP_SPECIFIC),

  // ALL
  VIEW_APPLICATION_SUMMARY(ProcessingPermissionType.APP_SPECIFIC);

  private final ProcessingPermissionType processingPermissionType;

  PwaAppProcessingPermission() {
    this.processingPermissionType = ProcessingPermissionType.GENERIC;
  }

  PwaAppProcessingPermission(ProcessingPermissionType processingPermissionType) {
    this.processingPermissionType = processingPermissionType;
  }

  public ProcessingPermissionType getProcessingPermissionType() {
    return processingPermissionType;
  }

  public static Stream<PwaAppProcessingPermission> streamGenericPermissions() {
    return Stream.of(PwaAppProcessingPermission.values())
        .filter(val -> val.getProcessingPermissionType().equals(ProcessingPermissionType.GENERIC));
  }

  public static Stream<PwaAppProcessingPermission> streamAppPermissions() {
    return Stream.of(PwaAppProcessingPermission.values())
        .filter(val -> val.getProcessingPermissionType().equals(ProcessingPermissionType.APP_SPECIFIC));
  }

}
